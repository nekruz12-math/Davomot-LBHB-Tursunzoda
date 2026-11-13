package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.XlsxReportGenerator
import com.example.data.model.AcademicYearConfig
import com.example.data.model.AppLanguage
import com.example.data.model.AttendanceStatus
import com.example.data.model.BellCountdownState
import com.example.data.model.DailyInspiration
import com.example.data.model.DisciplineNote
import com.example.data.model.FontScaleOption
import com.example.data.model.PicnicData
import com.example.data.model.ReportPeriodMode
import com.example.data.model.SchoolClass
import com.example.data.model.SmartAnalyticsSummary
import com.example.data.model.Student
import com.example.data.model.StudentReportStat
import com.example.data.model.TajikDateUtils
import com.example.data.model.TajikInspirationCatalog
import com.example.data.model.TeacherLesson
import com.example.data.model.TodoItem
import com.example.data.repository.AttendanceRepository
import com.example.notifications.MorningReminderReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "AttendanceViewModel"

enum class AppScreen {
  HOME,
  ATTENDANCE,
  DISCIPLINE,
  SCHEDULE,
  REPORTS,
  SETTINGS
}

enum class SettingsSubScreen {
  MAIN,
  SCHOOL_BRANDING,
  CLASSES,
  STUDENT_PROFILES,
  PICNIC,
  SCHEDULE,
  ACADEMIC_YEAR
}

data class AttendanceUiState(
  val selectedClassId: String = "1",
  val selectedDate: String = "",
  val selectedPeriod: Int = 1,
  val currentScreen: AppScreen = AppScreen.HOME,
  val settingsSubScreen: SettingsSubScreen = SettingsSubScreen.MAIN,
  val searchQuery: String = "",
  val reportMode: ReportPeriodMode = ReportPeriodMode.MONTHLY,
  val reportYearMonth: String = "",
  val customStartDate: String = "",
  val customEndDate: String = ""
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = AttendanceRepository(application)

  val classes: StateFlow<List<SchoolClass>> = repository.classesFlow
  val schedules: StateFlow<Map<String, Map<Int, List<String>>>> = repository.schedulesFlow
  val attendance: StateFlow<Map<String, Map<String, Map<Int, Map<String, String>>>>> = repository.attendanceFlow
  val confirmedPeriods: StateFlow<Set<String>> = repository.confirmedPeriodsFlow
  val smsTemplate: StateFlow<String> = repository.smsTemplateFlow
  val isDarkMode: StateFlow<Boolean> = repository.isDarkModeFlow
  val fontScale: StateFlow<FontScaleOption> = repository.fontScaleFlow
  val schoolName: StateFlow<String> = repository.schoolNameFlow
  val schoolLogoPath: StateFlow<String> = repository.schoolLogoPathFlow
  val todoItems: StateFlow<List<TodoItem>> = repository.todoItemsFlow
  val picnicData: StateFlow<PicnicData> = repository.picnicDataFlow
  val dailyInspiration: StateFlow<DailyInspiration> = repository.dailyInspirationFlow
  val teacherSchedule: StateFlow<List<TeacherLesson>> = repository.teacherScheduleFlow
  val academicYearConfig: StateFlow<AcademicYearConfig> = repository.academicYearConfigFlow
  val appLanguage: StateFlow<AppLanguage> = repository.appLanguageFlow
  val disciplineNotes: StateFlow<List<DisciplineNote>> = repository.disciplineNotesFlow
  val customDisciplineCategories: StateFlow<List<String>> = repository.customDisciplineCategoriesFlow
  val defaultDisciplineCategories: List<String> = repository.defaultDisciplineCategories

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)

  private val todayStr = dateFormat.format(Date())
  private val initialMaxPeriods = repository.getMaxPeriodsForDate("1", todayStr)
  private val initialAutoPeriod = TajikDateUtils.getAutoPeriodByCurrentTime(initialMaxPeriods)

  private val _uiState = MutableStateFlow(
    AttendanceUiState(
      selectedDate = todayStr,
      selectedPeriod = initialAutoPeriod,
      currentScreen = AppScreen.HOME,
      reportYearMonth = yearMonthFormat.format(Date()),
      customStartDate = yearMonthFormat.format(Date()) + "-01",
      customEndDate = todayStr
    )
  )
  val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

  private val _bellCountdown = MutableStateFlow(TajikDateUtils.getBellCountdownState())
  val bellCountdown: StateFlow<BellCountdownState> = _bellCountdown.asStateFlow()

  private val _toastEvent = MutableSharedFlow<String>()
  val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

  val currentClass: StateFlow<SchoolClass?> = combine(classes, uiState) { clsList, state ->
    clsList.find { it.id == state.selectedClassId } ?: clsList.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  init {
    MorningReminderReceiver.scheduleDailyMorningReminder(application)

    viewModelScope.launch {
      while (true) {
        _bellCountdown.value = TajikDateUtils.getBellCountdownState()
        delay(15_000L)
      }
    }

    autoRefreshInspirationWithAi()
  }

  fun autoRefreshInspirationWithAi() {
    viewModelScope.launch {
      val today = dateFormat.format(Date())
      val current = dailyInspiration.value
      if (current.fetchedDate == today && current.quoteText.isNotBlank() && current.poemLines.isNotBlank()) {
        return@launch
      }
      val inspiration = TajikInspirationCatalog.getForDateString(today)
      repository.updateDailyInspiration(inspiration)
    }
  }

  private fun getEffectiveSelectedClassId(state: AttendanceUiState = _uiState.value): String {
    val clsList = classes.value
    return (clsList.find { it.id == state.selectedClassId } ?: clsList.firstOrNull())?.id
      ?: state.selectedClassId
  }

  fun refreshAutoPeriodIfToday() {
    val state = _uiState.value
    val nowDateStr = dateFormat.format(Date())
    _bellCountdown.value = TajikDateUtils.getBellCountdownState()
    if (dailyInspiration.value.fetchedDate != nowDateStr) {
      repository.updateDailyInspiration(TajikInspirationCatalog.getForDateString(nowDateStr))
    }
    if (state.selectedDate == nowDateStr) {
      val maxPeriods = repository.getMaxPeriodsForDate(getEffectiveSelectedClassId(state), state.selectedDate)
      val autoP = TajikDateUtils.getAutoPeriodByCurrentTime(maxPeriods)
      _uiState.value = state.copy(selectedPeriod = autoP)
    }
  }

  fun setScreen(screen: AppScreen) {
    _uiState.value = _uiState.value.copy(
      currentScreen = screen,
      settingsSubScreen = SettingsSubScreen.MAIN
    )
  }

  fun setSettingsSubScreen(subScreen: SettingsSubScreen) {
    _uiState.value = _uiState.value.copy(
      currentScreen = AppScreen.SETTINGS,
      settingsSubScreen = subScreen
    )
  }

  fun openClassesManagement() {
    _uiState.value = _uiState.value.copy(
      currentScreen = AppScreen.SETTINGS,
      settingsSubScreen = SettingsSubScreen.CLASSES
    )
  }

  fun selectClass(classId: String) {
    val state = _uiState.value
    val maxPeriods = repository.getMaxPeriodsForDate(classId, state.selectedDate)
    val clampedPeriod = state.selectedPeriod.coerceIn(1, maxPeriods.coerceAtLeast(1))
    _uiState.value = state.copy(
      selectedClassId = classId,
      selectedPeriod = clampedPeriod
    )
  }

  fun selectDate(dateString: String) {
    val state = _uiState.value
    val maxPeriods = repository.getMaxPeriodsForDate(state.selectedClassId, dateString)
    val nowDateStr = dateFormat.format(Date())
    val nextPeriod = if (dateString == nowDateStr) {
      TajikDateUtils.getAutoPeriodByCurrentTime(maxPeriods)
    } else {
      state.selectedPeriod.coerceIn(1, maxPeriods.coerceAtLeast(1))
    }
    _uiState.value = state.copy(
      selectedDate = dateString,
      selectedPeriod = nextPeriod,
      reportYearMonth = dateString.substring(0, 7)
    )
  }

  fun selectPeriod(period: Int) {
    _uiState.value = _uiState.value.copy(selectedPeriod = period)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setReportMode(mode: ReportPeriodMode) {
    _uiState.value = _uiState.value.copy(reportMode = mode)
  }

  fun setReportMonth(yearMonth: String) {
    _uiState.value = _uiState.value.copy(reportYearMonth = yearMonth)
  }

  fun setCustomDateRange(start: String, end: String) {
    val safeStart = if (start <= end) start else end
    val safeEnd = if (end >= start) end else start
    _uiState.value = _uiState.value.copy(
      customStartDate = safeStart,
      customEndDate = safeEnd
    )
  }

  fun getDayOfWeekIndex(dateStr: String): Int {
    return repository.getDayOfWeekIndex(dateStr)
  }

  fun toggleAttendance(studentId: String, target: AttendanceStatus) {
    val state = _uiState.value
    repository.toggleStudentAttendance(
      date = state.selectedDate,
      classId = getEffectiveSelectedClassId(state),
      period = state.selectedPeriod,
      studentId = studentId,
      targetStatus = target
    )
  }

  fun confirmCurrentPeriodAttendance() {
    val state = _uiState.value
    repository.confirmPeriodAttendance(state.selectedDate, getEffectiveSelectedClassId(state), state.selectedPeriod)
    viewModelScope.launch {
      _toastEvent.emit("Давомоти соати ${state.selectedPeriod}-ум тасдиқ карда шуд ✅")
    }
  }

  // --- School Branding (Name & Custom Logo) ---

  fun updateSchoolName(newName: String) {
    if (newName.isBlank()) return
    repository.updateSchoolName(newName)
    viewModelScope.launch {
      _toastEvent.emit("Номи муассиса иваз карда шуд ✅")
    }
  }

  fun saveCustomSchoolLogoFromUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val destFile = withContext(Dispatchers.IO) {
          val file = File(context.filesDir, "custom_school_logo_${System.currentTimeMillis()}.jpg")
          context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
              input.copyTo(output)
            }
          }
          file
        }
        if (destFile.exists() && destFile.length() > 0) {
          repository.updateSchoolLogoPath(destFile.absolutePath)
          _toastEvent.emit("Логотипи нав бомуваффақият гузошта шуд ✅")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to save custom logo", e)
        _toastEvent.emit("Хатогӣ ҳангоми боркунии сурат")
      }
    }
  }

  fun resetSchoolBranding() {
    repository.resetSchoolBranding()
    viewModelScope.launch {
      _toastEvent.emit("Ном ва логотипи аслии ЛБХБ барқарор шуд")
    }
  }

  // --- To-Do List ---

  fun addTodoItem(title: String) {
    if (title.isBlank()) return
    repository.addTodoItem(title)
  }

  fun toggleTodoItem(id: String) {
    repository.toggleTodoItem(id)
  }

  fun deleteTodoItem(id: String) {
    repository.deleteTodoItem(id)
  }

  // --- Picnic Planner ---

  fun updatePicnicDate(dateStr: String) {
    repository.updatePicnicDate(dateStr)
  }

  fun updatePicnicNotes(notes: String) {
    repository.updatePicnicNotes(notes)
  }

  fun addPicnicPlanItem(title: String) {
    if (title.isBlank()) return
    repository.addPicnicPlanItem(title)
  }

  fun togglePicnicPlanItem(id: String) {
    repository.togglePicnicPlanItem(id)
  }

  fun deletePicnicPlanItem(id: String) {
    repository.deletePicnicPlanItem(id)
  }

  // --- Settings & SMS ---

  fun updateSmsTemplate(newTemplate: String) {
    repository.updateSmsTemplate(newTemplate)
    viewModelScope.launch {
      _toastEvent.emit("Қолаби паём (SMS) захира шуд")
    }
  }

  fun formatParentMessage(
    template: String,
    studentName: String,
    dateStr: String,
    subjectName: String,
    statusCode: String?
  ): String {
    val formattedDate = TajikDateUtils.formatDateDisplay(dateStr)
    val statusText = when (statusCode) {
      "SB" -> "СБ - бо сабаб"
      "BSB" -> "БСБ - бе сабаб"
      else -> "ғоиб"
    }
    return template
      .replace("[Номи хонанда]", studentName)
      .replace("[Сана]", formattedDate)
      .replace("[Номи фан]", subjectName)
      .replace("[Ҳолат]", statusText)
  }

  fun dialParentPhone(context: Context, phone: String) {
    if (phone.isBlank()) return
    try {
      val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${phone.trim()}")
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      viewModelScope.launch {
        _toastEvent.emit("Хатогӣ ҳангоми занг задан")
      }
    }
  }

  fun sendSmsToParent(context: Context, phone: String, message: String) {
    if (phone.isBlank()) return
    try {
      val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${phone.trim()}")
        putExtra("sms_body", message)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      viewModelScope.launch {
        _toastEvent.emit("Барномаи Паём (SMS) кушода нашуд")
      }
    }
  }

  fun sendWhatsAppToParent(context: Context, phone: String, message: String) {
    if (phone.isBlank()) return
    try {
      var digits = phone.replace(Regex("[^0-9]"), "")
      if (digits.length == 9) {
        digits = "992$digits"
      }
      val encodedMsg = URLEncoder.encode(message, "UTF-8")
      val uri = Uri.parse("https://wa.me/$digits?text=$encodedMsg")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      context.startActivity(intent)
    } catch (e: Exception) {
      viewModelScope.launch {
        _toastEvent.emit("WhatsApp пайдо нашуд")
      }
    }
  }

  fun toggleDarkMode() {
    repository.toggleDarkMode()
  }

  fun setDarkMode(enabled: Boolean) {
    repository.setDarkMode(enabled)
  }

  fun setFontScale(option: FontScaleOption) {
    repository.setFontScale(option)
  }

  // --- Classes & Students Management ---

  fun addClass(name: String, isHomeroom: Boolean = false) {
    if (name.isBlank()) return
    val newCls = repository.addClass(name, isHomeroom)
    selectClass(newCls.id)
    viewModelScope.launch {
      val typeStr = if (isHomeroom) " (Роҳбари синф)" else ""
      _toastEvent.emit("Синфи «${newCls.name}»$typeStr илова карда шуд")
    }
  }

  fun updateClass(id: String, newName: String, isHomeroom: Boolean) {
    repository.updateClass(id, newName, isHomeroom)
    viewModelScope.launch {
      _toastEvent.emit("Маълумоти синф нав карда шуд")
    }
  }

  fun deleteClass(id: String) {
    val success = repository.deleteClass(id)
    if (!success) {
      viewModelScope.launch {
        _toastEvent.emit("Дар барнома бояд камаш як синф мавҷуд бошад!")
      }
    } else {
      val first = classes.value.firstOrNull()
      if (first != null) selectClass(first.id)
      viewModelScope.launch {
        _toastEvent.emit("Синф нест карда шуд")
      }
    }
  }

  fun addStudent(name: String, parentPhone: String = "", birthDate: String = "") {
    val cId = getEffectiveSelectedClassId()
    repository.addStudent(cId, name, parentPhone, birthDate)
    viewModelScope.launch {
      _toastEvent.emit("Хонанда «$name» илова карда шуд")
    }
  }

  fun updateStudent(studentId: String, newName: String, newPhone: String, newBirthDate: String) {
    val cId = getEffectiveSelectedClassId()
    repository.updateStudent(cId, studentId, newName, newPhone, newBirthDate)
    viewModelScope.launch {
      _toastEvent.emit("Маълумоти хонанда нав карда шуд")
    }
  }

  fun updateStudentPhone(studentId: String, phone: String) {
    repository.updateStudentPhone(studentId, phone)
    viewModelScope.launch {
      _toastEvent.emit("Рақами телефон сабт шуд ✅")
    }
  }

  fun updateStudentFullProfile(classId: String, updatedStudent: Student) {
    repository.updateStudentFullProfile(classId, updatedStudent)
    viewModelScope.launch {
      _toastEvent.emit("Маълумоти пурраи «${updatedStudent.name}» захира шуд ✅")
    }
  }

  fun saveStudentPhotoFromUri(
    context: Context,
    classId: String,
    student: Student,
    uri: Uri,
    onSaved: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val destFile = withContext(Dispatchers.IO) {
          val file = File(context.filesDir, "student_photo_${student.id}_${System.currentTimeMillis()}.jpg")
          context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
              input.copyTo(output)
            }
          }
          file
        }
        if (destFile.exists() && destFile.length() > 0) {
          val path = destFile.absolutePath
          repository.updateStudentFullProfile(classId, student.copy(photoPath = path))
          onSaved(path)
          _toastEvent.emit("Сурати хонанда гузошта шуд ✅")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to save student photo", e)
        _toastEvent.emit("Хатогӣ ҳангоми гузоштани сурат")
      }
    }
  }

  fun deleteStudent(studentId: String) {
    val cId = getEffectiveSelectedClassId()
    repository.deleteStudent(cId, studentId)
    viewModelScope.launch {
      _toastEvent.emit("Хонанда аз рӯйхат хориҷ карда шуд")
    }
  }

  fun sortStudentsAlphabetically() {
    val cId = getEffectiveSelectedClassId()
    repository.sortStudentsAlphabetically(cId)
    viewModelScope.launch {
      _toastEvent.emit("Рӯйхати хонандагон аз рӯи алифбо (А–Я) чида шуд!")
    }
  }

  fun importStudentsFromText(text: String) {
    val cId = getEffectiveSelectedClassId()
    val count = repository.importStudentsFromText(cId, text)
    viewModelScope.launch {
      if (count > 0) {
        _toastEvent.emit("$count хонанда бомуваффақият илова шуд!")
      } else {
        _toastEvent.emit("Ҳеҷ хонандае пайдо нашуд")
      }
    }
  }

  // --- Schedules Management ---

  fun getAllKnownSubjects(): List<String> = repository.getAllKnownSubjects()

  fun copyScheduleFromClass(sourceClassId: String, sourceClassName: String) {
    val targetClassId = getTargetScheduleClassId()
    repository.copyScheduleFromClass(targetClassId, sourceClassId)
    viewModelScope.launch {
      _toastEvent.emit("Ҷадвал аз синфи «$sourceClassName» нусхабардорӣ шуд!")
    }
  }

  fun copyDayScheduleToDay(fromDayIdx: Int, toDayIdx: Int) {
    val cId = getTargetScheduleClassId()
    repository.copyDayScheduleToDay(cId, fromDayIdx, toDayIdx)
    val fromName = TajikDateUtils.DAYS_OF_WEEK.getOrElse(fromDayIdx) { "" }
    val toName = TajikDateUtils.DAYS_OF_WEEK.getOrElse(toDayIdx) { "" }
    viewModelScope.launch {
      _toastEvent.emit("Ҷадвали $fromName ба $toName нусхабардорӣ шуд!")
    }
  }

  private fun getTargetScheduleClassId(): String {
    val clsList = classes.value
    val selected = clsList.find { it.id == _uiState.value.selectedClassId }
    val target = selected?.takeIf { it.isHomeroom }
      ?: clsList.find { it.isHomeroom }
      ?: selected
      ?: clsList.firstOrNull()
    return target?.id ?: _uiState.value.selectedClassId
  }

  fun updateSchedulePeriod(dayIdx: Int, periodZeroBased: Int, subjectName: String) {
    val cId = getTargetScheduleClassId()
    repository.updateSchedulePeriod(cId, dayIdx, periodZeroBased, subjectName)
  }

  fun addPeriodToDay(dayIdx: Int, subjectName: String = "") {
    val cId = getTargetScheduleClassId()
    repository.addPeriodToDay(cId, dayIdx, subjectName)
    viewModelScope.launch {
      _toastEvent.emit("Дарси нав ба ҷадвал илова шуд")
    }
  }

  fun deletePeriodFromDay(dayIdx: Int, periodZeroBased: Int) {
    val cId = getTargetScheduleClassId()
    repository.deletePeriodFromDay(cId, dayIdx, periodZeroBased)
    viewModelScope.launch {
      _toastEvent.emit("Дарс аз ҷадвал нест карда шуд")
    }
  }

  fun adjustPeriodsCount(dayIdx: Int, amount: Int) {
    val cId = getTargetScheduleClassId()
    repository.adjustPeriodsCount(cId, dayIdx, amount)
  }

  // --- Teacher Personal Schedule Operations ---

  fun saveTeacherLesson(
    dayOfWeek: Int,
    period: Int,
    isFreePeriod: Boolean,
    className: String,
    subject: String,
    shortSubject: String = "",
    note: String = ""
  ) {
    val cleanSubj = subject.trim()
    val cleanShort = shortSubject.trim().ifBlank {
      when {
        cleanSubj.equals("Алгебра", ignoreCase = true) -> "А"
        cleanSubj.equals("Геометрия", ignoreCase = true) -> "Г"
        cleanSubj.contains("тарбияв", ignoreCase = true) -> "С.Т"
        else -> cleanSubj.take(3).uppercase()
      }
    }
    repository.saveTeacherLesson(
      TeacherLesson(
        dayOfWeek = dayOfWeek,
        period = period,
        isFreePeriod = isFreePeriod,
        className = className.trim(),
        subject = cleanSubj,
        shortSubject = cleanShort,
        note = note.trim()
      )
    )
    viewModelScope.launch {
      _toastEvent.emit(if (isFreePeriod) "Соати озод гузошта шуд ☕" else "Дарс дар «Ҷадвали ман» сабт шуд 📚")
    }
  }

  fun updateAcademicYearConfig(config: AcademicYearConfig) {
    repository.updateAcademicYearConfig(config)
    viewModelScope.launch {
      _toastEvent.emit("Соли хониш ва санаҳои чорякҳо захира шуданд ✅")
    }
  }

  fun setAppLanguage(language: AppLanguage) {
    repository.setAppLanguage(language)
    viewModelScope.launch {
      val msg = when (language) {
        AppLanguage.TJ -> "Забони барнома: Тоҷикӣ 🇹🇯"
        AppLanguage.RU -> "Язык приложения: Русский 🇷🇺"
        AppLanguage.EN -> "App language: English 🇬🇧"
      }
      _toastEvent.emit(msg)
    }
  }

  fun saveDisciplineNote(
    noteId: String? = null,
    studentId: String,
    studentName: String,
    dateStr: String,
    category: String,
    comment: String
  ) {
    val classId = getEffectiveSelectedClassId()
    val note = DisciplineNote(
      id = noteId ?: java.util.UUID.randomUUID().toString(),
      classId = classId,
      studentId = studentId,
      studentName = studentName.trim(),
      dateStr = dateStr,
      category = category.trim(),
      comment = comment.trim()
    )
    repository.saveDisciplineNote(note)
    viewModelScope.launch {
      _toastEvent.emit("Қайди интизом сабт шуд ✅")
    }
  }

  fun deleteDisciplineNote(noteId: String) {
    repository.deleteDisciplineNote(noteId)
    viewModelScope.launch {
      _toastEvent.emit("Қайди интизом нест карда шуд")
    }
  }

  fun addCustomDisciplineCategory(categoryName: String) {
    val clean = categoryName.trim()
    if (clean.isBlank()) return
    repository.addCustomDisciplineCategory(clean)
    viewModelScope.launch {
      _toastEvent.emit("Категорияи «$clean» илова шуд")
    }
  }

  fun shiftSelectedDateByDays(deltaDays: Int) {
    try {
      val cal = Calendar.getInstance()
      val parsed = dateFormat.parse(_uiState.value.selectedDate) ?: Date()
      cal.time = parsed
      cal.add(Calendar.DAY_OF_MONTH, deltaDays)
      val nextDateStr = dateFormat.format(cal.time)
      _uiState.value = _uiState.value.copy(selectedDate = nextDateStr)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun deleteTeacherLesson(lessonId: String) {
    repository.deleteTeacherLesson(lessonId)
    viewModelScope.launch {
      _toastEvent.emit("Аз «Ҷадвали ман» нест карда шуд")
    }
  }

  fun setTeacherPeriodFree(dayOfWeek: Int, period: Int) {
    repository.setTeacherPeriodFree(dayOfWeek, period)
    viewModelScope.launch {
      _toastEvent.emit("Соати озод (тиреза) сабт шуд ☕")
    }
  }

  // --- Late Arrival (Дермонӣ) ---

  fun recordLateMinutes(studentId: String, minutes: Int) {
    val state = _uiState.value
    repository.recordLateArrival(
      date = state.selectedDate,
      classId = getEffectiveSelectedClassId(state),
      period = state.selectedPeriod,
      studentId = studentId,
      minutes = minutes
    )
    viewModelScope.launch {
      if (minutes > 0) {
        _toastEvent.emit("Дермонии $minutes дақиқа сабт шуд ⏰")
      } else {
        _toastEvent.emit("Ҳолати хонанда навсозӣ шуд ✅")
      }
    }
  }

  // --- Reports & Smart Analytics ---

  fun getReportDateRange(state: AttendanceUiState): Pair<String, String> {
    return when (state.reportMode) {
      ReportPeriodMode.DAILY -> state.selectedDate to state.selectedDate
      ReportPeriodMode.WEEKLY -> {
        try {
          val cal = Calendar.getInstance()
          val parsed = dateFormat.parse(state.selectedDate) ?: Date()
          cal.time = parsed
          val dow = cal.get(Calendar.DAY_OF_WEEK)
          val diffToMonday = if (dow == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dow)
          cal.add(Calendar.DAY_OF_MONTH, diffToMonday)
          val startStr = dateFormat.format(cal.time)
          cal.add(Calendar.DAY_OF_MONTH, 6)
          val endStr = dateFormat.format(cal.time)
          startStr to endStr
        } catch (e: Exception) {
          state.selectedDate to state.selectedDate
        }
      }
      ReportPeriodMode.MONTHLY -> {
        val ym = state.reportYearMonth
        try {
          val parts = ym.split("-")
          val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
          val monthOneBased = parts.getOrNull(1)?.toIntOrNull() ?: 1
          val cal = Calendar.getInstance()
          cal.set(year, monthOneBased - 1, 1)
          val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
          val maxDayStr = maxDay.toString().padStart(2, '0')
          "$ym-01" to "$ym-$maxDayStr"
        } catch (e: Exception) {
          "$ym-01" to "$ym-31"
        }
      }
      ReportPeriodMode.CUSTOM -> state.customStartDate to state.customEndDate
    }
  }

  fun getReportPeriodTitle(state: AttendanceUiState): String {
    return when (state.reportMode) {
      ReportPeriodMode.DAILY -> "Сана: ${TajikDateUtils.formatDateDisplay(state.selectedDate)}"
      ReportPeriodMode.WEEKLY -> {
        val (s, e) = getReportDateRange(state)
        "Ҳафта: ${TajikDateUtils.formatDateDisplay(s)} – ${TajikDateUtils.formatDateDisplay(e)}"
      }
      ReportPeriodMode.MONTHLY -> {
        val parts = state.reportYearMonth.split("-")
        val y = parts.getOrNull(0) ?: ""
        val mIdx = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val mName = TajikDateUtils.MONTHS.getOrElse(mIdx - 1) { "" }
        "$mName $y"
      }
      ReportPeriodMode.CUSTOM -> {
        "${TajikDateUtils.formatDateDisplay(state.customStartDate)} – ${TajikDateUtils.formatDateDisplay(state.customEndDate)}"
      }
    }
  }

  fun calculateCurrentReport(state: AttendanceUiState): List<StudentReportStat> {
    val (start, end) = getReportDateRange(state)
    return repository.calculateReportForRange(getEffectiveSelectedClassId(state), start, end)
  }

  fun calculateSmartAnalytics(state: AttendanceUiState): SmartAnalyticsSummary {
    val (start, end) = getReportDateRange(state)
    return repository.calculateSmartAnalytics(getEffectiveSelectedClassId(state), start, end)
  }

  fun getReportSchoolDates(state: AttendanceUiState): List<String> {
    val (startDateStr, endDateStr) = getReportDateRange(state)
    val classId = getEffectiveSelectedClassId(state)
    val attMap = attendance.value
    val confirmedSet = confirmedPeriods.value

    val markedDatesInRange = mutableSetOf<String>()
    attMap.keys.filter { it in startDateStr..endDateStr }.forEach { dStr ->
      if (!attMap[dStr]?.get(classId).isNullOrEmpty()) {
        markedDatesInRange.add(dStr)
      }
    }
    confirmedSet.forEach { key ->
      val parts = key.split("|")
      if (parts.size >= 2 && parts[1] == classId && parts[0] in startDateStr..endDateStr) {
        markedDatesInRange.add(parts[0])
      }
    }

    val schoolDates = mutableListOf<String>()
    try {
      val effectiveStartStr = if (state.reportMode == ReportPeriodMode.MONTHLY && markedDatesInRange.isNotEmpty()) {
        markedDatesInRange.minOrNull() ?: startDateStr
      } else {
        startDateStr
      }
      val startCal = Calendar.getInstance()
      val endCal = Calendar.getInstance()
      val parsedStart = dateFormat.parse(effectiveStartStr)
      val parsedEnd = dateFormat.parse(endDateStr)
      if (parsedStart != null && parsedEnd != null) {
        startCal.time = parsedStart
        endCal.time = parsedEnd
        while (!startCal.after(endCal)) {
          val dStr = dateFormat.format(startCal.time)
          val dow = startCal.get(Calendar.DAY_OF_WEEK)
          if (dow != Calendar.SUNDAY || markedDatesInRange.contains(dStr)) {
            schoolDates.add(dStr)
          }
          startCal.add(Calendar.DAY_OF_MONTH, 1)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return schoolDates
  }

  fun generateReportXlsxBytes(): ByteArray {
    val state = _uiState.value
    val cls = currentClass.value
    val className = cls?.name ?: "Синф"
    val currentSchoolTitle = schoolName.value
    val periodTitle = getReportPeriodTitle(state)
    val reportData = calculateCurrentReport(state)
    val schoolDates = getReportSchoolDates(state)
    val (startDateStr, _) = getReportDateRange(state)
    val monthIdx = startDateStr.split("-").getOrNull(1)?.toIntOrNull() ?: 1
    val monthName = TajikDateUtils.MONTHS.getOrElse(monthIdx - 1) { "Моҳ" }

    return XlsxReportGenerator.generateAttendanceReportXlsx(
      className = className,
      schoolName = currentSchoolTitle,
      periodTitle = periodTitle,
      monthName = monthName,
      schoolDates = schoolDates,
      reportData = reportData
    )
  }

  fun saveExcelToDownloads(context: Context) {
    viewModelScope.launch {
      try {
        val state = _uiState.value
        val cls = currentClass.value
        val cleanClass = (cls?.name ?: "Sinf").replace(Regex("[^a-zA-Zа-яА-Я0-9_\\-]"), "_")
        val fileName = "Hisobot_${cleanClass}_${state.reportYearMonth}_${System.currentTimeMillis() % 10000}.xlsx"
        val xlsxBytes = withContext(Dispatchers.IO) { generateReportXlsxBytes() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val resolver = context.contentResolver
          val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(
              MediaStore.MediaColumns.MIME_TYPE,
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
          }
          val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
          if (uri != null) {
            withContext(Dispatchers.IO) {
              resolver.openOutputStream(uri)?.use { os ->
                os.write(xlsxBytes)
              }
            }
            _toastEvent.emit("Ҳисобот дар папкаи Downloads захира шуд: $fileName")
          } else {
            _toastEvent.emit("Хатогӣ ҳангоми сохтани файл")
          }
        } else {
          val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
          if (!downloadsDir.exists()) downloadsDir.mkdirs()
          val outFile = File(downloadsDir, fileName)
          withContext(Dispatchers.IO) {
            FileOutputStream(outFile).use { fos ->
              fos.write(xlsxBytes)
            }
          }
          _toastEvent.emit("Ҳисобот захира шуд: ${outFile.absolutePath}")
        }
      } catch (e: Exception) {
        e.printStackTrace()
        _toastEvent.emit("Хатогӣ: ${e.localizedMessage}")
      }
    }
  }

  fun writeExcelToUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val xlsxBytes = withContext(Dispatchers.IO) { generateReportXlsxBytes() }
        withContext(Dispatchers.IO) {
          context.contentResolver.openOutputStream(uri)?.use { os ->
            os.write(xlsxBytes)
          }
        }
        _toastEvent.emit("Ҳисобот бомуваффақият захира шуд!")
      } catch (e: Exception) {
        e.printStackTrace()
        _toastEvent.emit("Хатогӣ ҳангоми захираи ҳисобот")
      }
    }
  }

  // --- Backup & Restore ---

  fun saveBackupToUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val json = repository.exportBackupJsonString()
        context.contentResolver.openOutputStream(uri)?.use { os ->
          OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
            writer.write(json)
          }
        }
        _toastEvent.emit("Нусхаи эҳтиётӣ (Backup) бомуваффақият захира шуд!")
      } catch (e: Exception) {
        e.printStackTrace()
        _toastEvent.emit("Хатогӣ ҳангоми захираи база")
      }
    }
  }

  fun restoreBackupFromUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use {
          it.readText()
        }
        if (!json.isNullOrBlank()) {
          val ok = repository.importBackupJsonString(json)
          if (ok) {
            val firstId = repository.classesFlow.value.firstOrNull()?.id ?: "1"
            selectClass(firstId)
            _toastEvent.emit("Базаи маълумот бомуваффақият барқарор шуд!")
          } else {
            _toastEvent.emit("Файли интихобшуда нодуруст аст")
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        _toastEvent.emit("Хатогӣ ҳангоми барқароркунии база")
      }
    }
  }
}
