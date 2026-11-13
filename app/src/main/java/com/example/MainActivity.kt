package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TajikDateUtils
import com.example.ui.AppScreen
import com.example.ui.AppStringsCatalog
import com.example.ui.AttendanceViewModel
import com.example.ui.SettingsSubScreen
import com.example.ui.components.ClassPickerDialog
import com.example.ui.components.PeriodPickerDialog
import com.example.ui.components.SchoolHeader
import com.example.ui.components.TajikCalendarDialog
import com.example.ui.components.TelegramBottomBar
import com.example.ui.components.WindowsSplashOverlay
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.discipline.DisciplineScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.reports.ReportScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      AttendanceAppRoot()
    }
  }
}

@Composable
fun AttendanceAppRoot(viewModel: AttendanceViewModel = viewModel()) {
  val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
  MyApplicationTheme(darkTheme = isDarkMode) {
    AttendanceApp(viewModel = viewModel, isDarkMode = isDarkMode)
  }
}

@Composable
fun AttendanceApp(
  viewModel: AttendanceViewModel,
  isDarkMode: Boolean
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val classes by viewModel.classes.collectAsStateWithLifecycle()
  val schedules by viewModel.schedules.collectAsStateWithLifecycle()
  val attendance by viewModel.attendance.collectAsStateWithLifecycle()
  val confirmedPeriods by viewModel.confirmedPeriods.collectAsStateWithLifecycle()
  val smsTemplate by viewModel.smsTemplate.collectAsStateWithLifecycle()
  val bellCountdown by viewModel.bellCountdown.collectAsStateWithLifecycle()
  val currentClass by viewModel.currentClass.collectAsStateWithLifecycle()
  val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
  val schoolLogoPath by viewModel.schoolLogoPath.collectAsStateWithLifecycle()
  val todoItems by viewModel.todoItems.collectAsStateWithLifecycle()
  val picnicData by viewModel.picnicData.collectAsStateWithLifecycle()
  val dailyInspiration by viewModel.dailyInspiration.collectAsStateWithLifecycle()
  val teacherSchedule by viewModel.teacherSchedule.collectAsStateWithLifecycle()
  val academicYearConfig by viewModel.academicYearConfig.collectAsStateWithLifecycle()
  val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
  val disciplineNotes by viewModel.disciplineNotes.collectAsStateWithLifecycle()
  val customDisciplineCategories by viewModel.customDisciplineCategories.collectAsStateWithLifecycle()

  val appStrings = remember(appLanguage, schoolName) {
    AppStringsCatalog.forLanguage(appLanguage, schoolName)
  }

  val allDisciplineCategories = remember(customDisciplineCategories) {
    (viewModel.defaultDisciplineCategories + customDisciplineCategories).distinct()
  }

  var showSplash by rememberSaveable { mutableStateOf(true) }
  var showClassPicker by remember { mutableStateOf(false) }
  var showCalendarPicker by remember { mutableStateOf(false) }
  var showPeriodPicker by remember { mutableStateOf(false) }
  var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

  // 3-second startup loading screen as requested
  LaunchedEffect(Unit) {
    delay(3000L)
    showSplash = false
  }

  val notifPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ -> }

  LaunchedEffect(Unit) {
    viewModel.refreshAutoPeriodIfToday()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  LaunchedEffect(Unit) {
    viewModel.toastEvent.collectLatest { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }

  // Handle hardware Back button to return to HOME screen
  BackHandler(
    enabled = !showSplash &&
      (uiState.currentScreen != AppScreen.HOME || uiState.settingsSubScreen != SettingsSubScreen.MAIN)
  ) {
    if (uiState.currentScreen == AppScreen.SETTINGS && uiState.settingsSubScreen != SettingsSubScreen.MAIN) {
      viewModel.setSettingsSubScreen(SettingsSubScreen.MAIN)
    } else {
      viewModel.setScreen(AppScreen.HOME)
    }
  }

  val effectiveClassId = currentClass?.id ?: uiState.selectedClassId

  LaunchedEffect(uiState.currentScreen, effectiveClassId) {
    if (uiState.currentScreen != AppScreen.ATTENDANCE) {
      isSearchExpanded = false
    }
  }

  val formattedDate = remember(uiState.selectedDate) {
    TajikDateUtils.formatDateDisplay(uiState.selectedDate)
  }

  val dayIdx = remember(uiState.selectedDate) {
    viewModel.getDayOfWeekIndex(uiState.selectedDate)
  }

  val todaySchedule = remember(schedules, effectiveClassId, dayIdx) {
    schedules[effectiveClassId]?.get(dayIdx) ?: emptyList()
  }

  val allKnownSubjects = remember(schedules) {
    viewModel.getAllKnownSubjects()
  }

  val currentPeriodAttendanceMap = remember(
    attendance,
    uiState.selectedDate,
    effectiveClassId,
    uiState.selectedPeriod
  ) {
    attendance[uiState.selectedDate]?.get(effectiveClassId)?.get(uiState.selectedPeriod)
      ?: emptyMap()
  }

  val isCurrentPeriodConfirmed = remember(
    confirmedPeriods,
    currentPeriodAttendanceMap,
    uiState.selectedDate,
    effectiveClassId,
    uiState.selectedPeriod
  ) {
    val key = "${uiState.selectedDate}|$effectiveClassId|${uiState.selectedPeriod}"
    confirmedPeriods.contains(key) || currentPeriodAttendanceMap.isNotEmpty()
  }

  val reportData = remember(
    attendance,
    classes,
    schedules,
    effectiveClassId,
    uiState.selectedClassId,
    uiState.reportMode,
    uiState.selectedDate,
    uiState.reportYearMonth,
    uiState.customStartDate,
    uiState.customEndDate
  ) {
    viewModel.calculateCurrentReport(uiState)
  }

  val reportSchoolDates = remember(
    attendance,
    confirmedPeriods,
    effectiveClassId,
    uiState.selectedClassId,
    uiState.reportMode,
    uiState.selectedDate,
    uiState.reportYearMonth,
    uiState.customStartDate,
    uiState.customEndDate
  ) {
    viewModel.getReportSchoolDates(uiState)
  }

  val smartAnalytics = remember(
    attendance,
    schedules,
    effectiveClassId,
    uiState.selectedClassId,
    uiState.reportMode,
    uiState.selectedDate,
    uiState.reportYearMonth,
    uiState.customStartDate,
    uiState.customEndDate
  ) {
    viewModel.calculateSmartAnalytics(uiState)
  }

  val periodSubtitle = remember(
    uiState.reportMode,
    uiState.selectedDate,
    uiState.reportYearMonth,
    uiState.customStartDate,
    uiState.customEndDate
  ) {
    viewModel.getReportPeriodTitle(uiState)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9),
      topBar = {
        SchoolHeader(
          schoolName = schoolName,
          schoolLogoPath = schoolLogoPath,
          className = currentClass?.name ?: "Интихоб",
          formattedDate = formattedDate,
          periodNumber = uiState.selectedPeriod,
          bellCountdown = bellCountdown,
          isDarkMode = isDarkMode,
          appStrings = appStrings,
          showSelectors = (uiState.currentScreen == AppScreen.ATTENDANCE || uiState.currentScreen == AppScreen.DISCIPLINE),
          showSearchIcon = (uiState.currentScreen == AppScreen.ATTENDANCE && currentClass?.students?.isNotEmpty() == true),
          onSearchClick = {
            isSearchExpanded = !isSearchExpanded
            if (!isSearchExpanded) {
              viewModel.setSearchQuery("")
            }
          },
          onClassClick = { showClassPicker = true },
          onDateClick = { showCalendarPicker = true },
          onPeriodClick = { showPeriodPicker = true }
        )
      },
      bottomBar = {
        TelegramBottomBar(
          currentScreen = uiState.currentScreen,
          isDarkMode = isDarkMode,
          appStrings = appStrings,
          onSelectScreen = { viewModel.setScreen(it) }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = uiState.currentScreen,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "screen_transition"
        ) { targetScreen ->
          when (targetScreen) {
            AppScreen.HOME -> {
              HomeScreen(
                dailyInspiration = dailyInspiration,
                todoItems = todoItems,
                picnicData = picnicData,
                selectedDate = uiState.selectedDate,
                dayOfWeekIndex = dayIdx,
                selectedPeriod = uiState.selectedPeriod,
                isCurrentPeriodConfirmed = isCurrentPeriodConfirmed,
                hasStudents = (currentClass?.students?.isNotEmpty() == true),
                isDarkMode = isDarkMode,
                teacherSchedule = teacherSchedule,
                onOpenScheduleSettings = {
                  viewModel.setSettingsSubScreen(SettingsSubScreen.SCHEDULE)
                  viewModel.setScreen(AppScreen.SETTINGS)
                },
                onConfirmPeriodAttendance = { viewModel.confirmCurrentPeriodAttendance() },
                onAddTodo = { viewModel.addTodoItem(it) },
                onToggleTodo = { viewModel.toggleTodoItem(it) },
                onDeleteTodo = { viewModel.deleteTodoItem(it) },
                onUpdatePicnicDate = { viewModel.updatePicnicDate(it) },
                onUpdatePicnicNotes = { viewModel.updatePicnicNotes(it) },
                onAddPicnicItem = { viewModel.addPicnicPlanItem(it) },
                onTogglePicnicItem = { viewModel.togglePicnicPlanItem(it) },
                onDeletePicnicItem = { viewModel.deletePicnicPlanItem(it) }
              )
            }

            AppScreen.ATTENDANCE -> {
              AttendanceScreen(
                currentClass = currentClass,
                selectedDate = uiState.selectedDate,
                selectedPeriod = uiState.selectedPeriod,
                dayOfWeekIndex = dayIdx,
                todaySchedule = todaySchedule,
                currentPeriodAttendanceMap = currentPeriodAttendanceMap,
                isCurrentPeriodConfirmed = isCurrentPeriodConfirmed,
                smsTemplate = smsTemplate,
                isDarkMode = isDarkMode,
                fontScale = 1.0f,
                academicYearConfig = academicYearConfig,
                appStrings = appStrings,
                searchQuery = uiState.searchQuery,
                isSearchVisible = isSearchExpanded,
                onPeriodSelected = { viewModel.selectPeriod(it) },
                onQuarterSelected = { qNum ->
                  val targetDate = when (qNum) {
                    1 -> academicYearConfig.q1Start
                    2 -> academicYearConfig.q2Start
                    3 -> academicYearConfig.q3Start
                    4 -> academicYearConfig.q4Start
                    else -> uiState.selectedDate
                  }
                  if (academicYearConfig.getQuarterForDate(uiState.selectedDate) != qNum) {
                    viewModel.selectDate(targetDate)
                  }
                },
                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                onCloseSearch = {
                  isSearchExpanded = false
                  viewModel.setSearchQuery("")
                },
                onConfirmPeriodAttendance = { viewModel.confirmCurrentPeriodAttendance() },
                onToggleAttendance = { stId, status -> viewModel.toggleAttendance(stId, status) },
                onDialParent = { phone -> viewModel.dialParentPhone(context, phone) },
                onSendSms = { phone, msg -> viewModel.sendSmsToParent(context, phone, msg) },
                onSendWhatsApp = { phone, msg -> viewModel.sendWhatsAppToParent(context, phone, msg) },
                onSavePhone = { stId, phone -> viewModel.updateStudentPhone(stId, phone) },
                onRecordLate = { stId, mins -> viewModel.recordLateMinutes(stId, mins) },
                onSaveSmsTemplate = { newTpl -> viewModel.updateSmsTemplate(newTpl) },
                onNavigateToClasses = { viewModel.openClassesManagement() }
              )
            }

            AppScreen.DISCIPLINE -> {
              DisciplineScreen(
                currentClass = currentClass,
                selectedDate = uiState.selectedDate,
                academicYearConfig = academicYearConfig,
                disciplineNotes = disciplineNotes,
                defaultCategories = viewModel.defaultDisciplineCategories,
                customCategories = customDisciplineCategories,
                appStrings = appStrings,
                isDarkMode = isDarkMode,
                onPrevDate = { viewModel.shiftSelectedDateByDays(-1) },
                onNextDate = { viewModel.shiftSelectedDateByDays(1) },
                onSelectDate = { viewModel.selectDate(it) },
                onSaveNote = { noteId, stId, stName, dateStr, category, comment ->
                  viewModel.saveDisciplineNote(
                    noteId,
                    stId,
                    stName,
                    dateStr,
                    category,
                    comment
                  )
                },
                onDeleteNote = { viewModel.deleteDisciplineNote(it) },
                onAddCustomCategory = { viewModel.addCustomDisciplineCategory(it) }
              )
            }

            AppScreen.SCHEDULE -> {
              ScheduleScreen(
                classes = classes,
                currentClass = currentClass,
                schedules = schedules,
                teacherSchedule = teacherSchedule,
                allKnownSubjects = allKnownSubjects,
                isDarkMode = isDarkMode,
                fontScale = 1.0f,
                onUpdateSubject = { dIdx, pIdx, subj ->
                  viewModel.updateSchedulePeriod(dIdx, pIdx, subj)
                },
                onAddPeriod = { dIdx, subj ->
                  viewModel.addPeriodToDay(dIdx, subj)
                },
                onDeletePeriod = { dIdx, pIdx ->
                  viewModel.deletePeriodFromDay(dIdx, pIdx)
                },
                onAdjustPeriods = { dIdx, amount ->
                  viewModel.adjustPeriodsCount(dIdx, amount)
                },
                onCopyFromClass = { srcId, srcName ->
                  viewModel.copyScheduleFromClass(srcId, srcName)
                },
                onCopyDayToDay = { fromDay, toDay ->
                  viewModel.copyDayScheduleToDay(fromDay, toDay)
                },
                onSaveTeacherLesson = { dayOfWeek, period, isFree, className, subject, shortSubject, note ->
                  viewModel.saveTeacherLesson(dayOfWeek, period, isFree, className, subject, shortSubject, note)
                },
                onDeleteTeacherLesson = { viewModel.deleteTeacherLesson(it) },
                onSetTeacherPeriodFree = { dayOfWeek, period -> viewModel.setTeacherPeriodFree(dayOfWeek, period) }
              )
            }

            AppScreen.REPORTS -> {
              ReportScreen(
                classes = classes,
                currentClass = currentClass,
                onSelectClass = { viewModel.selectClass(it) },
                reportMode = uiState.reportMode,
                reportYearMonth = uiState.reportYearMonth,
                customStartDate = uiState.customStartDate,
                customEndDate = uiState.customEndDate,
                periodSubtitle = periodSubtitle,
                schoolDates = reportSchoolDates,
                reportData = reportData,
                smartAnalytics = smartAnalytics,
                isDarkMode = isDarkMode,
                fontScale = 1.0f,
                onReportModeChanged = { viewModel.setReportMode(it) },
                onMonthChanged = { viewModel.setReportMonth(it) },
                onCustomRangeChanged = { s, e -> viewModel.setCustomDateRange(s, e) },
                onSaveExcelToDownloads = { ctx -> viewModel.saveExcelToDownloads(ctx) },
                onSaveExcelToUri = { ctx, uri -> viewModel.writeExcelToUri(ctx, uri) }
              )
            }

            AppScreen.SETTINGS -> {
              SettingsScreen(
                subScreen = uiState.settingsSubScreen,
                schoolName = schoolName,
                schoolLogoPath = schoolLogoPath,
                classes = classes,
                currentClass = currentClass,
                picnicData = picnicData,
                selectedDate = uiState.selectedDate,
                isDarkMode = isDarkMode,
                academicYearConfig = academicYearConfig,
                appLanguage = appLanguage,
                appStrings = appStrings,
                onSelectSubScreen = { viewModel.setSettingsSubScreen(it) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onUpdateAcademicYearConfig = { viewModel.updateAcademicYearConfig(it) },
                onSelectLanguage = { viewModel.setAppLanguage(it) },
                onUpdateSchoolName = { viewModel.updateSchoolName(it) },
                onPickSchoolLogo = { ctx, uri -> viewModel.saveCustomSchoolLogoFromUri(ctx, uri) },
                onResetSchoolBranding = { viewModel.resetSchoolBranding() },
                onSelectClass = { viewModel.selectClass(it) },
                onAddClass = { name, isHr -> viewModel.addClass(name, isHr) },
                onUpdateClass = { id, name, isHr -> viewModel.updateClass(id, name, isHr) },
                onDeleteClass = { viewModel.deleteClass(it) },
                onAddStudent = { name, phone, dob -> viewModel.addStudent(name, phone, dob) },
                onUpdateStudent = { stId, name, phone, dob ->
                  viewModel.updateStudent(stId, name, phone, dob)
                },
                onDeleteStudent = { viewModel.deleteStudent(it) },
                onSortAlphabetically = { viewModel.sortStudentsAlphabetically() },
                onImportExcel = { text -> viewModel.importStudentsFromText(text) },
                onSaveBackup = { ctx, uri -> viewModel.saveBackupToUri(ctx, uri) },
                onRestoreBackup = { ctx, uri -> viewModel.restoreBackupFromUri(ctx, uri) },
                onSaveStudentProfile = { classId, updatedSt ->
                  viewModel.updateStudentFullProfile(classId, updatedSt)
                },
                onPickStudentPhoto = { classId, student, uri, onSaved ->
                  viewModel.saveStudentPhotoFromUri(context, classId, student, uri, onSaved)
                },
                onDialPhone = { phone -> viewModel.dialParentPhone(context, phone) },
                onUpdatePicnicDate = { viewModel.updatePicnicDate(it) },
                onUpdatePicnicNotes = { viewModel.updatePicnicNotes(it) },
                onAddPicnicItem = { viewModel.addPicnicPlanItem(it) },
                onTogglePicnicItem = { viewModel.togglePicnicPlanItem(it) },
                onDeletePicnicItem = { viewModel.deletePicnicPlanItem(it) },
                schedules = schedules,
                allKnownSubjects = allKnownSubjects,
                onUpdateSubject = { dIdx, pIdx, subj ->
                  viewModel.updateSchedulePeriod(dIdx, pIdx, subj)
                },
                onAddPeriod = { dIdx, subj ->
                  viewModel.addPeriodToDay(dIdx, subj)
                },
                onDeletePeriod = { dIdx, pIdx ->
                  viewModel.deletePeriodFromDay(dIdx, pIdx)
                },
                onAdjustPeriods = { dIdx, amount ->
                  viewModel.adjustPeriodsCount(dIdx, amount)
                },
                onCopyFromClass = { srcId, srcName ->
                  viewModel.copyScheduleFromClass(srcId, srcName)
                },
                onCopyDayToDay = { fromDay, toDay ->
                  viewModel.copyDayScheduleToDay(fromDay, toDay)
                },
                teacherSchedule = teacherSchedule,
                onSaveTeacherLesson = { dayOfWeek, period, isFree, className, subject, shortSubject, note ->
                  viewModel.saveTeacherLesson(dayOfWeek, period, isFree, className, subject, shortSubject, note)
                },
                onDeleteTeacherLesson = { viewModel.deleteTeacherLesson(it) },
                onSetTeacherPeriodFree = { dayOfWeek, period -> viewModel.setTeacherPeriodFree(dayOfWeek, period) }
              )
            }
          }
        }
      }

      // Dialogs
      if (showClassPicker) {
        ClassPickerDialog(
          classes = classes,
          selectedClassId = uiState.selectedClassId,
          onDismiss = { showClassPicker = false },
          onClassSelected = { viewModel.selectClass(it) },
          onManageClassesClick = { viewModel.openClassesManagement() }
        )
      }

      if (showCalendarPicker) {
        TajikCalendarDialog(
          currentDateString = uiState.selectedDate,
          onDismiss = { showCalendarPicker = false },
          onDateSelected = { viewModel.selectDate(it) }
        )
      }

      if (showPeriodPicker) {
        PeriodPickerDialog(
          todaySchedule = todaySchedule,
          selectedPeriod = uiState.selectedPeriod,
          onDismiss = { showPeriodPicker = false },
          onPeriodSelected = { viewModel.selectPeriod(it) }
        )
      }
    }

    // 5-Second Windows-style Startup Splash Overlay
    AnimatedVisibility(
      visible = showSplash,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      WindowsSplashOverlay(
        schoolName = schoolName,
        schoolLogoPath = schoolLogoPath
      )
    }
  }
}
