package com.example.data.repository

import android.content.Context
import com.example.data.model.AcademicYearConfig
import com.example.data.model.AppLanguage
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyInspiration
import com.example.data.model.DisciplineNote
import com.example.data.model.FontScaleOption
import com.example.data.model.PicnicData
import com.example.data.model.PicnicPlanItem
import com.example.data.model.SchoolClass
import com.example.data.model.SmartAnalyticsSummary
import com.example.data.model.Student
import com.example.data.model.StudentDailyDetail
import com.example.data.model.StudentReportStat
import com.example.data.model.TajikDateUtils
import com.example.data.model.TajikInspirationCatalog
import com.example.data.model.TeacherLesson
import com.example.data.model.TodoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AttendanceRepository(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val storageFile = File(context.filesDir, "attendance_data.json")
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

  private val _classesFlow = MutableStateFlow<List<SchoolClass>>(emptyList())
  val classesFlow: StateFlow<List<SchoolClass>> = _classesFlow.asStateFlow()

  private val _schedulesFlow = MutableStateFlow<Map<String, Map<Int, List<String>>>>(emptyMap())
  val schedulesFlow: StateFlow<Map<String, Map<Int, List<String>>>> = _schedulesFlow.asStateFlow()

  private val _attendanceFlow = MutableStateFlow<Map<String, Map<String, Map<Int, Map<String, String>>>>>(emptyMap())
  val attendanceFlow: StateFlow<Map<String, Map<String, Map<Int, Map<String, String>>>>> = _attendanceFlow.asStateFlow()

  private val _confirmedPeriodsFlow = MutableStateFlow<Set<String>>(emptySet())
  val confirmedPeriodsFlow: StateFlow<Set<String>> = _confirmedPeriodsFlow.asStateFlow()

  private val _smsTemplateFlow = MutableStateFlow(TajikDateUtils.DEFAULT_SMS_TEMPLATE)
  val smsTemplateFlow: StateFlow<String> = _smsTemplateFlow.asStateFlow()

  private val _isDarkModeFlow = MutableStateFlow(false)
  val isDarkModeFlow: StateFlow<Boolean> = _isDarkModeFlow.asStateFlow()

  private val _fontScaleFlow = MutableStateFlow(FontScaleOption.NORMAL)
  val fontScaleFlow: StateFlow<FontScaleOption> = _fontScaleFlow.asStateFlow()

  private val _schoolNameFlow = MutableStateFlow("ЛБХБ ТУРСУНЗОДА")
  val schoolNameFlow: StateFlow<String> = _schoolNameFlow.asStateFlow()

  private val _schoolLogoPathFlow = MutableStateFlow("")
  val schoolLogoPathFlow: StateFlow<String> = _schoolLogoPathFlow.asStateFlow()

  private val _todoItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
  val todoItemsFlow: StateFlow<List<TodoItem>> = _todoItemsFlow.asStateFlow()

  private val _picnicDataFlow = MutableStateFlow(PicnicData())
  val picnicDataFlow: StateFlow<PicnicData> = _picnicDataFlow.asStateFlow()

  private val _teacherScheduleFlow = MutableStateFlow<List<TeacherLesson>>(emptyList())
  val teacherScheduleFlow: StateFlow<List<TeacherLesson>> = _teacherScheduleFlow.asStateFlow()

  private val _academicYearConfigFlow = MutableStateFlow(AcademicYearConfig())
  val academicYearConfigFlow: StateFlow<AcademicYearConfig> = _academicYearConfigFlow.asStateFlow()

  private val _appLanguageFlow = MutableStateFlow(AppLanguage.TJ)
  val appLanguageFlow: StateFlow<AppLanguage> = _appLanguageFlow.asStateFlow()

  private val _disciplineNotesFlow = MutableStateFlow<List<DisciplineNote>>(emptyList())
  val disciplineNotesFlow: StateFlow<List<DisciplineNote>> = _disciplineNotesFlow.asStateFlow()

  val defaultDisciplineCategories = listOf(
    "Либоси мактабӣ",
    "Бе тайёрӣ ба дарс (вазифаи хонагӣ)",
    "Халал расонидан дар дарс",
    "Дермонӣ ба дарс",
    "Истифодаи телефон дар дарс",
    "Надоштани дафтар ё китоб",
    "Дигар"
  )
  private val _customDisciplineCategoriesFlow = MutableStateFlow<List<String>>(emptyList())
  val customDisciplineCategoriesFlow: StateFlow<List<String>> = _customDisciplineCategoriesFlow.asStateFlow()

  private val initialDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
  private val _dailyInspirationFlow = MutableStateFlow(TajikInspirationCatalog.getForDateString(initialDateStr))
  val dailyInspirationFlow: StateFlow<DailyInspiration> = _dailyInspirationFlow.asStateFlow()

  private var rotationCounter: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

  init {
    loadData()
  }

  private fun loadData() {
    if (!storageFile.exists()) {
      initDefaultData()
      return
    }

    try {
      val jsonString = storageFile.readText()
      parseAndApplyJson(jsonString)
    } catch (e: Exception) {
      e.printStackTrace()
      initDefaultData()
    }
  }

  private fun parseAndApplyJson(jsonString: String): Boolean {
    return try {
      val root = JSONObject(jsonString)

      // 1. Classes & Students (including full profile fields)
      val classesList = mutableListOf<SchoolClass>()
      val classesJson = root.optJSONArray("classes") ?: JSONArray()
      for (i in 0 until classesJson.length()) {
        val classObj = classesJson.getJSONObject(i)
        val classId = classObj.getString("id")
        val className = classObj.getString("name")
        val studentsJson = classObj.optJSONArray("students") ?: JSONArray()
        val studentsList = mutableListOf<Student>()
        for (j in 0 until studentsJson.length()) {
          val stObj = studentsJson.getJSONObject(j)
          studentsList.add(
            Student(
              id = stObj.getString("id"),
              name = stObj.getString("name"),
              parentPhone = stObj.optString("parentPhone", ""),
              birthDate = stObj.optString("birthDate", ""),
              photoPath = stObj.optString("photoPath", ""),
              gender = stObj.optString("gender", "MALE"),
              nationality = stObj.optString("nationality", "Тоҷик"),
              address = stObj.optString("address", ""),
              fatherName = stObj.optString("fatherName", ""),
              motherName = stObj.optString("motherName", ""),
              motherPhone = stObj.optString("motherPhone", ""),
              chosenProfession = stObj.optString("chosenProfession", "")
            )
          )
        }
        val isHomeroom = classObj.optBoolean("isHomeroom", classId == "1")
        classesList.add(SchoolClass(classId, className, studentsList, isHomeroom))
      }

      if (classesList.isEmpty()) {
        initDefaultData()
        return false
      }
      _classesFlow.value = classesList

      // 2. Schedules
      val schedulesMap = mutableMapOf<String, Map<Int, List<String>>>()
      val schedulesJson = root.optJSONObject("schedules") ?: JSONObject()
      val classKeys = schedulesJson.keys()
      while (classKeys.hasNext()) {
        val cKey = classKeys.next()
        val dayObj = schedulesJson.getJSONObject(cKey)
        val dayMap = mutableMapOf<Int, List<String>>()
        val dayKeys = dayObj.keys()
        while (dayKeys.hasNext()) {
          val dKey = dayKeys.next()
          val dayIdx = dKey.toIntOrNull() ?: continue
          val arr = dayObj.getJSONArray(dKey)
          val subjects = mutableListOf<String>()
          for (s in 0 until arr.length()) {
            subjects.add(arr.getString(s))
          }
          dayMap[dayIdx] = subjects.toList()
        }
        schedulesMap[cKey] = dayMap.toMap()
      }
      _schedulesFlow.value = schedulesMap.toMap()

      // 3. Attendance
      val attMap = mutableMapOf<String, Map<String, Map<Int, Map<String, String>>>>()
      val attJson = root.optJSONObject("attendance") ?: JSONObject()
      val dateKeys = attJson.keys()
      while (dateKeys.hasNext()) {
        val dStr = dateKeys.next()
        val cObj = attJson.getJSONObject(dStr)
        val cMap = mutableMapOf<String, Map<Int, Map<String, String>>>()
        val cIter = cObj.keys()
        while (cIter.hasNext()) {
          val cId = cIter.next()
          val pObj = cObj.getJSONObject(cId)
          val pMap = mutableMapOf<Int, Map<String, String>>()
          val pIter = pObj.keys()
          while (pIter.hasNext()) {
            val pStr = pIter.next()
            val pIdx = pStr.toIntOrNull() ?: continue
            val stObj = pObj.getJSONObject(pStr)
            val stMap = mutableMapOf<String, String>()
            val stIter = stObj.keys()
            while (stIter.hasNext()) {
              val stId = stIter.next()
              stMap[stId] = stObj.getString(stId)
            }
            pMap[pIdx] = stMap.toMap()
          }
          cMap[cId] = pMap.toMap()
        }
        attMap[dStr] = cMap.toMap()
      }
      _attendanceFlow.value = attMap.toMap()

      // 4. Confirmed Periods
      val confirmedArr = root.optJSONArray("confirmedPeriods")
      if (confirmedArr != null) {
        val set = mutableSetOf<String>()
        for (i in 0 until confirmedArr.length()) {
          set.add(confirmedArr.getString(i))
        }
        _confirmedPeriodsFlow.value = set.toSet()
      }

      // 5. Settings & School Branding
      val savedSms = root.optString("smsTemplate", "")
      if (savedSms.isNotBlank()) {
        _smsTemplateFlow.value = savedSms
      }
      _isDarkModeFlow.value = root.optBoolean("isDarkMode", false)
      val scaleName = root.optString("fontScale", FontScaleOption.NORMAL.name)
      _fontScaleFlow.value = try {
        FontScaleOption.valueOf(scaleName)
      } catch (e: Exception) {
        FontScaleOption.NORMAL
      }

      val savedSchoolName = root.optString("schoolName", "ЛБХБ ТУРСУНЗОДА")
      _schoolNameFlow.value = if (savedSchoolName.isNotBlank()) savedSchoolName else "ЛБХБ ТУРСУНЗОДА"
      _schoolLogoPathFlow.value = root.optString("schoolLogoPath", "")

      // 6. To-Do List
      val todoArr = root.optJSONArray("todoItems")
      if (todoArr != null) {
        val list = mutableListOf<TodoItem>()
        for (i in 0 until todoArr.length()) {
          val obj = todoArr.getJSONObject(i)
          list.add(
            TodoItem(
              id = obj.getString("id"),
              title = obj.getString("title"),
              isDone = obj.optBoolean("isDone", false)
            )
          )
        }
        _todoItemsFlow.value = list
      } else {
        _todoItemsFlow.value = listOf(
          TodoItem(UUID.randomUUID().toString(), "Тафтиши давомоти синф", false),
          TodoItem(UUID.randomUUID().toString(), "Тайёр кардани нақшаи дарси фардо", false)
        )
      }

      // 7. Picnic Data
      val picnicObj = root.optJSONObject("picnicData")
      if (picnicObj != null) {
        val pDate = picnicObj.optString("dateStr", "")
        val pNotes = picnicObj.optString("locationAndNotes", "")
        val pItemsArr = picnicObj.optJSONArray("items") ?: JSONArray()
        val pItems = mutableListOf<PicnicPlanItem>()
        for (i in 0 until pItemsArr.length()) {
          val itemObj = pItemsArr.getJSONObject(i)
          pItems.add(
            PicnicPlanItem(
              id = itemObj.getString("id"),
              title = itemObj.getString("title"),
              isDone = itemObj.optBoolean("isDone", false)
            )
          )
        }
        _picnicDataFlow.value = PicnicData(pDate, pNotes, pItems)
      }

      // 8. Daily Inspiration (strictly 1 unique quote & poem per calendar day)
      val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
      _dailyInspirationFlow.value = TajikInspirationCatalog.getForDateString(todayDateStr)

      // 9. Teacher Schedule
      val teacherSchedArr = root.optJSONArray("teacherSchedule") ?: JSONArray()
      val tList = mutableListOf<TeacherLesson>()
      for (i in 0 until teacherSchedArr.length()) {
        val tObj = teacherSchedArr.getJSONObject(i)
        tList.add(
          TeacherLesson(
            id = tObj.optString("id", UUID.randomUUID().toString()),
            dayOfWeek = tObj.optInt("dayOfWeek", 1),
            period = tObj.optInt("period", 1),
            isFreePeriod = tObj.optBoolean("isFreePeriod", false),
            className = tObj.optString("className", ""),
            subject = tObj.optString("subject", ""),
            shortSubject = tObj.optString("shortSubject", ""),
            note = tObj.optString("note", "")
          )
        )
      }
      _teacherScheduleFlow.value = tList.sortedWith(compareBy({ it.dayOfWeek }, { it.period }))

      // 10. Academic Year & Quarters Config
      val acadObj = root.optJSONObject("academicYearConfig")
      if (acadObj != null) {
        _academicYearConfigFlow.value = AcademicYearConfig(
          schoolYearName = acadObj.optString("schoolYearName", "2026-2027"),
          q1Start = acadObj.optString("q1Start", "2026-09-01"),
          q1End = acadObj.optString("q1End", "2026-10-31"),
          q2Start = acadObj.optString("q2Start", "2026-11-11"),
          q2End = acadObj.optString("q2End", "2026-12-31"),
          q3Start = acadObj.optString("q3Start", "2027-01-11"),
          q3End = acadObj.optString("q3End", "2027-03-20"),
          q4Start = acadObj.optString("q4Start", "2027-04-01"),
          q4End = acadObj.optString("q4End", "2027-06-14")
        )
      }

      // 11. App Language
      val langCode = root.optString("appLanguage", AppLanguage.TJ.code)
      _appLanguageFlow.value = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.TJ

      // 12. Discipline Notes & Custom Categories
      val discArr = root.optJSONArray("disciplineNotes") ?: JSONArray()
      val dList = mutableListOf<DisciplineNote>()
      for (i in 0 until discArr.length()) {
        val dObj = discArr.getJSONObject(i)
        dList.add(
          DisciplineNote(
            id = dObj.optString("id", UUID.randomUUID().toString()),
            classId = dObj.optString("classId", "1"),
            studentId = dObj.optString("studentId", ""),
            studentName = dObj.optString("studentName", ""),
            dateStr = dObj.optString("dateStr", ""),
            category = dObj.optString("category", ""),
            comment = dObj.optString("comment", ""),
            createdAt = dObj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      _disciplineNotesFlow.value = dList.sortedByDescending { it.createdAt }

      val catArr = root.optJSONArray("customDisciplineCategories") ?: JSONArray()
      val cList = mutableListOf<String>()
      for (i in 0 until catArr.length()) {
        val cName = catArr.optString(i, "").trim()
        if (cName.isNotBlank()) cList.add(cName)
      }
      _customDisciplineCategoriesFlow.value = cList

      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  private fun initDefaultData() {
    val defaultClasses = listOf(
      SchoolClass(
        id = "1",
        name = "10 \"А\"",
        isHomeroom = true,
        students = listOf(
          Student(
            id = "s1",
            name = "Алиев Алишер Аҳмадович",
            parentPhone = "+992900112233",
            birthDate = "06.11.2010",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода, м. 1-ум",
            fatherName = "Алиев Аҳмад",
            motherName = "Алиева Зӯҳро",
            chosenProfession = "Барномасоз (IT)"
          ),
          Student(
            id = "s2",
            name = "Гадоев Некрӯз Рустамович",
            parentPhone = "+992935001122",
            birthDate = "15.03.2010",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода, кӯчаи И. Сомонӣ",
            fatherName = "Гадоев Рустам",
            motherName = "Гадоева Саодат",
            chosenProfession = "Муҳандис"
          ),
          Student(
            id = "s3",
            name = "Каримов Фаридун Давлатович",
            parentPhone = "+992918223344",
            birthDate = "22.05.2010",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода, м. 3-юм",
            fatherName = "Каримов Давлат",
            motherName = "Каримова Гулчеҳра",
            chosenProfession = "Духтур"
          ),
          Student(
            id = "s4",
            name = "Раҳимова Мадина Бахтиёровна",
            parentPhone = "",
            birthDate = "10.09.2010",
            gender = "FEMALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            fatherName = "Раҳимов Бахтиёр",
            motherName = "Раҳимова Нилуфар",
            chosenProfession = "Омӯзгор"
          ),
          Student(
            id = "s5",
            name = "Саидова Нигина Фарҳодовна",
            parentPhone = "",
            birthDate = "18.02.2010",
            gender = "FEMALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            fatherName = "Саидов Фарҳод",
            motherName = "Саидова Меҳринисо",
            chosenProfession = "Дипломат"
          ),
          Student(
            id = "s6",
            name = "Шарифов Муҳаммад Комилович",
            parentPhone = "",
            birthDate = "29.07.2010",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            fatherName = "Шарифов Комил",
            motherName = "Шарифова Мавзуна",
            chosenProfession = "Иқтисодчӣ"
          )
        )
      ),
      SchoolClass(
        id = "2",
        name = "11 \"Б\"",
        students = listOf(
          Student(
            id = "s201",
            name = "Зокирова Манижа Сайфуллоевна",
            parentPhone = "+992907554433",
            birthDate = "12.04.2009",
            gender = "FEMALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            chosenProfession = "Ҳуқуқшинос"
          ),
          Student(
            id = "s202",
            name = "Умаров Беҳрӯз Насимҷонович",
            parentPhone = "",
            birthDate = "05.08.2009",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            chosenProfession = "Меъмор"
          ),
          Student(
            id = "s203",
            name = "Ҳакимов Ҷамшед Илҳомович",
            parentPhone = "",
            birthDate = "21.11.2009",
            gender = "MALE",
            nationality = "Тоҷик",
            address = "ш. Турсунзода",
            chosenProfession = "Бонкдор"
          )
        )
      )
    )

    val defaultSchedule10A = mapOf(
      1 to listOf("Алгебра", "Физика", "Химия", "Адабиёт", "Таърих", "Заб. англисӣ", "Тарб. ҷисмонӣ"),
      2 to listOf("Геометрия", "Биология", "Адабиёт", "Заб. тоҷикӣ", "Химия", "Информатика", "Технология"),
      3 to listOf("Физика", "Алгебра", "Таърих", "Заб. англисӣ", "Биология", "Тарб. ҷисмонӣ", "Суруд"),
      4 to listOf("Химия", "Адабиёт", "Геометрия", "Физика", "Заб. тоҷикӣ", "География", "Технология"),
      5 to listOf("Алгебра", "Заб. англисӣ", "Информатика", "Таърих", "Биология", "Адабиёт", "Ҳуқуқ"),
      6 to listOf("Геометрия", "Физика", "Химия", "Заб. тоҷикӣ", "Заб. русӣ", "Информатика")
    )

    val defaultSchedules = mapOf(
      "1" to defaultSchedule10A,
      "2" to defaultSchedule10A
    )

    _classesFlow.value = defaultClasses
    _schedulesFlow.value = defaultSchedules
    _attendanceFlow.value = emptyMap()
    _todoItemsFlow.value = listOf(
      TodoItem(UUID.randomUUID().toString(), "Тафтиши давомоти синф", false),
      TodoItem(UUID.randomUUID().toString(), "Тайёр кардани нақшаи дарси фардо", false)
    )
    _picnicDataFlow.value = PicnicData(
      dateStr = "",
      locationAndNotes = "Сайругашти табиат ва бозиҳои даставӣ бо хонандагон",
      items = listOf(
        PicnicPlanItem(UUID.randomUUID().toString(), "Ҷамъоварии розигии волидайн", false),
        PicnicPlanItem(UUID.randomUUID().toString(), "Омода кардани нақлиёт ва хӯрокворӣ", false)
      )
    )

    persistData()
  }

  fun exportBackupJsonString(): String {
    val root = JSONObject()

    // Classes & Students
    val classesJson = JSONArray()
    for (c in _classesFlow.value) {
      val cObj = JSONObject()
      cObj.put("id", c.id)
      cObj.put("name", c.name)
      cObj.put("isHomeroom", c.isHomeroom)
      val stArr = JSONArray()
      for (s in c.students) {
        val sObj = JSONObject()
        sObj.put("id", s.id)
        sObj.put("name", s.name)
        sObj.put("parentPhone", s.parentPhone)
        sObj.put("birthDate", s.birthDate)
        sObj.put("photoPath", s.photoPath)
        sObj.put("gender", s.gender)
        sObj.put("nationality", s.nationality)
        sObj.put("address", s.address)
        sObj.put("fatherName", s.fatherName)
        sObj.put("motherName", s.motherName)
        sObj.put("motherPhone", s.motherPhone)
        sObj.put("chosenProfession", s.chosenProfession)
        stArr.put(sObj)
      }
      cObj.put("students", stArr)
      classesJson.put(cObj)
    }
    root.put("classes", classesJson)

    // Schedules
    val schedulesJson = JSONObject()
    for ((cId, dayMap) in _schedulesFlow.value) {
      val dayObj = JSONObject()
      for ((dIdx, subjs) in dayMap) {
        val subjsArr = JSONArray()
        subjs.forEach { subjsArr.put(it) }
        dayObj.put(dIdx.toString(), subjsArr)
      }
      schedulesJson.put(cId, dayObj)
    }
    root.put("schedules", schedulesJson)

    // Attendance
    val attJson = JSONObject()
    for ((dStr, cMap) in _attendanceFlow.value) {
      val cObj = JSONObject()
      for ((cId, pMap) in cMap) {
        val pObj = JSONObject()
        for ((pIdx, stMap) in pMap) {
          if (stMap.isNotEmpty()) {
            val stObj = JSONObject()
            for ((stId, status) in stMap) {
              stObj.put(stId, status)
            }
            pObj.put(pIdx.toString(), stObj)
          }
        }
        if (pObj.length() > 0) {
          cObj.put(cId, pObj)
        }
      }
      if (cObj.length() > 0) {
        attJson.put(dStr, cObj)
      }
    }
    root.put("attendance", attJson)

    val confArr = JSONArray()
    _confirmedPeriodsFlow.value.forEach { confArr.put(it) }
    root.put("confirmedPeriods", confArr)

    root.put("smsTemplate", _smsTemplateFlow.value)
    root.put("isDarkMode", _isDarkModeFlow.value)
    root.put("fontScale", _fontScaleFlow.value.name)
    root.put("schoolName", _schoolNameFlow.value)
    root.put("schoolLogoPath", _schoolLogoPathFlow.value)
    root.put("rotationCounter", rotationCounter)

    // To-Do Items
    val todoArr = JSONArray()
    for (t in _todoItemsFlow.value) {
      val tObj = JSONObject()
      tObj.put("id", t.id)
      tObj.put("title", t.title)
      tObj.put("isDone", t.isDone)
      todoArr.put(tObj)
    }
    root.put("todoItems", todoArr)

    // Picnic Data
    val picnicObj = JSONObject()
    val pData = _picnicDataFlow.value
    picnicObj.put("dateStr", pData.dateStr)
    picnicObj.put("locationAndNotes", pData.locationAndNotes)
    val pItemsArr = JSONArray()
    for (item in pData.items) {
      val iObj = JSONObject()
      iObj.put("id", item.id)
      iObj.put("title", item.title)
      iObj.put("isDone", item.isDone)
      pItemsArr.put(iObj)
    }
    picnicObj.put("items", pItemsArr)
    root.put("picnicData", picnicObj)

    // Teacher Schedule
    val tArr = JSONArray()
    for (t in _teacherScheduleFlow.value) {
      val tObj = JSONObject()
      tObj.put("id", t.id)
      tObj.put("dayOfWeek", t.dayOfWeek)
      tObj.put("period", t.period)
      tObj.put("isFreePeriod", t.isFreePeriod)
      tObj.put("className", t.className)
      tObj.put("subject", t.subject)
      tObj.put("shortSubject", t.shortSubject)
      tObj.put("note", t.note)
      tArr.put(tObj)
    }
    root.put("teacherSchedule", tArr)

    // Academic Year Config
    val acad = _academicYearConfigFlow.value
    val acadObj = JSONObject()
    acadObj.put("schoolYearName", acad.schoolYearName)
    acadObj.put("q1Start", acad.q1Start)
    acadObj.put("q1End", acad.q1End)
    acadObj.put("q2Start", acad.q2Start)
    acadObj.put("q2End", acad.q2End)
    acadObj.put("q3Start", acad.q3Start)
    acadObj.put("q3End", acad.q3End)
    acadObj.put("q4Start", acad.q4Start)
    acadObj.put("q4End", acad.q4End)
    root.put("academicYearConfig", acadObj)

    // App Language
    root.put("appLanguage", _appLanguageFlow.value.code)

    // Discipline Notes
    val dArr = JSONArray()
    for (d in _disciplineNotesFlow.value) {
      val dObj = JSONObject()
      dObj.put("id", d.id)
      dObj.put("classId", d.classId)
      dObj.put("studentId", d.studentId)
      dObj.put("studentName", d.studentName)
      dObj.put("dateStr", d.dateStr)
      dObj.put("category", d.category)
      dObj.put("comment", d.comment)
      dObj.put("createdAt", d.createdAt)
      dArr.put(dObj)
    }
    root.put("disciplineNotes", dArr)

    val catArr = JSONArray()
    _customDisciplineCategoriesFlow.value.forEach { catArr.put(it) }
    root.put("customDisciplineCategories", catArr)

    return root.toString(2)
  }

  fun importBackupJsonString(jsonString: String): Boolean {
    val ok = parseAndApplyJson(jsonString)
    if (ok) {
      persistData()
    }
    return ok
  }

  private fun persistData() {
    coroutineScope.launch {
      try {
        storageFile.writeText(exportBackupJsonString())
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun updateSmsTemplate(newTemplate: String) {
    if (newTemplate.isBlank()) return
    _smsTemplateFlow.value = newTemplate.trim()
    persistData()
  }

  fun toggleDarkMode() {
    _isDarkModeFlow.value = !_isDarkModeFlow.value
    persistData()
  }

  fun setDarkMode(enabled: Boolean) {
    _isDarkModeFlow.value = enabled
    persistData()
  }

  fun setFontScale(option: FontScaleOption) {
    _fontScaleFlow.value = option
    persistData()
  }

  // --- School Branding (Name & Custom Logo) ---

  fun updateSchoolName(newName: String) {
    val clean = newName.trim()
    if (clean.isBlank()) return
    _schoolNameFlow.value = clean
    persistData()
  }

  fun updateSchoolLogoPath(path: String) {
    _schoolLogoPathFlow.value = path
    persistData()
  }

  fun resetSchoolBranding() {
    _schoolNameFlow.value = "ЛБХБ ТУРСУНЗОДА"
    _schoolLogoPathFlow.value = ""
    persistData()
  }

  // --- To-Do List operations ---

  fun addTodoItem(title: String) {
    val clean = title.trim()
    if (clean.isBlank()) return
    val newItem = TodoItem(id = UUID.randomUUID().toString(), title = clean, isDone = false)
    _todoItemsFlow.value = listOf(newItem) + _todoItemsFlow.value
    persistData()
  }

  fun toggleTodoItem(id: String) {
    _todoItemsFlow.value = _todoItemsFlow.value.map { item ->
      if (item.id == id) item.copy(isDone = !item.isDone) else item
    }
    persistData()
  }

  fun deleteTodoItem(id: String) {
    _todoItemsFlow.value = _todoItemsFlow.value.filter { it.id != id }
    persistData()
  }

  // --- Picnic operations ---

  fun updatePicnicDate(dateStr: String) {
    _picnicDataFlow.value = _picnicDataFlow.value.copy(dateStr = dateStr)
    persistData()
  }

  fun updatePicnicNotes(notes: String) {
    _picnicDataFlow.value = _picnicDataFlow.value.copy(locationAndNotes = notes)
    persistData()
  }

  fun addPicnicPlanItem(title: String) {
    val clean = title.trim()
    if (clean.isBlank()) return
    val newItem = PicnicPlanItem(id = UUID.randomUUID().toString(), title = clean, isDone = false)
    val current = _picnicDataFlow.value
    _picnicDataFlow.value = current.copy(items = current.items + newItem)
    persistData()
  }

  fun togglePicnicPlanItem(id: String) {
    val current = _picnicDataFlow.value
    _picnicDataFlow.value = current.copy(
      items = current.items.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
    )
    persistData()
  }

  fun deletePicnicPlanItem(id: String) {
    val current = _picnicDataFlow.value
    _picnicDataFlow.value = current.copy(
      items = current.items.filter { it.id != id }
    )
    persistData()
  }

  // --- Daily Inspiration ---

  fun updateDailyInspiration(inspiration: DailyInspiration) {
    _dailyInspirationFlow.value = inspiration
  }

  // --- Classes operations ---

  fun addClass(name: String, isHomeroom: Boolean = false): SchoolClass {
    val newId = UUID.randomUUID().toString()
    val newClass = SchoolClass(
      id = newId,
      name = name.trim(),
      students = emptyList(),
      isHomeroom = isHomeroom
    )
    _classesFlow.value = _classesFlow.value + newClass

    if (isHomeroom) {
      val currentSchedules = _schedulesFlow.value.toMutableMap()
      val base = currentSchedules.values.firstOrNull { it.isNotEmpty() } ?: mapOf(
        1 to listOf("Алгебра", "Физика", "Химия", "Адабиёт", "Таърих", "Заб. англисӣ", "Тарб. ҷисмонӣ"),
        2 to listOf("Геометрия", "Биология", "Адабиёт", "Заб. тоҷикӣ", "Химия", "Информатика", "Технология"),
        3 to listOf("Физика", "Алгебра", "Таърих", "Заб. англисӣ", "Биология", "Тарб. ҷисмонӣ", "Суруд"),
        4 to listOf("Химия", "Адабиёт", "Геометрия", "Физика", "Заб. тоҷикӣ", "География", "Технология"),
        5 to listOf("Алгебра", "Заб. англисӣ", "Информатика", "Таърих", "Биология", "Адабиёт", "Ҳуқуқ"),
        6 to listOf("Геометрия", "Физика", "Химия", "Заб. тоҷикӣ", "Заб. русӣ", "Информатика")
      )
      currentSchedules[newId] = base
      _schedulesFlow.value = currentSchedules.toMap()
    }

    persistData()
    return newClass
  }

  fun updateClass(id: String, newName: String, isHomeroom: Boolean) {
    if (newName.isBlank()) return
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.id == id) {
        c.copy(name = newName.trim(), isHomeroom = isHomeroom)
      } else c
    }
    persistData()
  }

  fun deleteClass(id: String): Boolean {
    if (_classesFlow.value.size <= 1) return false
    _classesFlow.value = _classesFlow.value.filter { it.id != id }
    persistData()
    return true
  }

  // --- Students operations ---

  fun addStudent(classId: String, name: String, parentPhone: String = "", birthDate: String = "") {
    if (name.isBlank()) return
    val newStudent = Student(
      id = UUID.randomUUID().toString(),
      name = name.trim(),
      parentPhone = parentPhone.trim(),
      birthDate = birthDate.trim()
    )
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.id == classId) {
        c.copy(students = c.students + newStudent)
      } else c
    }
    persistData()
  }

  fun updateStudent(
    classId: String,
    studentId: String,
    newName: String,
    newPhone: String,
    newBirthDate: String
  ) {
    if (newName.isBlank()) return
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.students.any { it.id == studentId } || c.id == classId) {
        c.copy(
          students = c.students.map { s ->
            if (s.id == studentId) {
              s.copy(
                name = newName.trim(),
                parentPhone = newPhone.trim(),
                birthDate = newBirthDate.trim()
              )
            } else s
          }
        )
      } else c
    }
    persistData()
  }

  fun updateStudentPhone(studentId: String, phone: String) {
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.students.any { it.id == studentId }) {
        c.copy(
          students = c.students.map { s ->
            if (s.id == studentId) {
              s.copy(parentPhone = phone.trim())
            } else s
          }
        )
      } else c
    }
    persistData()
  }

  fun updateStudentFullProfile(classId: String, updatedStudent: Student) {
    if (updatedStudent.name.isBlank()) return
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.students.any { it.id == updatedStudent.id } || c.id == classId) {
        c.copy(
          students = c.students.map { s ->
            if (s.id == updatedStudent.id) updatedStudent else s
          }
        )
      } else c
    }
    persistData()
  }

  fun deleteStudent(classId: String, studentId: String) {
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.id == classId) {
        c.copy(students = c.students.filter { it.id != studentId })
      } else c
    }
    persistData()
  }

  fun sortStudentsAlphabetically(classId: String) {
    val collator = Collator.getInstance(Locale("ru", "RU"))
    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.id == classId) {
        val sorted = c.students.sortedWith { a, b ->
          collator.compare(a.name.trim(), b.name.trim())
        }
        c.copy(students = sorted)
      } else c
    }
    persistData()
  }

  fun importStudentsFromText(classId: String, rawText: String): Int {
    val lines = rawText.lines()
    val newStudents = mutableListOf<Student>()
    for (line in lines) {
      val clean = line.replace(Regex("^[0-9]+[\\.\\)\\-\\s]+"), "").trim()
      if (clean.length >= 2) {
        newStudents.add(Student(id = UUID.randomUUID().toString(), name = clean))
      }
    }
    if (newStudents.isEmpty()) return 0

    _classesFlow.value = _classesFlow.value.map { c ->
      if (c.id == classId) {
        c.copy(students = c.students + newStudents)
      } else c
    }
    persistData()
    return newStudents.size
  }

  // --- Schedule operations ---

  fun getAllKnownSubjects(): List<String> {
    val customSet = mutableSetOf<String>()
    customSet.addAll(TajikDateUtils.COMMON_SUBJECTS)
    for ((_, dayMap) in _schedulesFlow.value) {
      for ((_, list) in dayMap) {
        list.forEach { subj ->
          if (subj.isNotBlank() && !subj.startsWith("Дарси ")) {
            customSet.add(subj.trim())
          }
        }
      }
    }
    return customSet.sorted()
  }

  fun copyScheduleFromClass(targetClassId: String, sourceClassId: String) {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val sourceMap = currentAll[sourceClassId] ?: return
    val deepCopy = sourceMap.mapValues { (_, list) -> list.toList() }
    currentAll[targetClassId] = deepCopy
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  fun copyDayScheduleToDay(classId: String, fromDayIdx: Int, toDayIdx: Int) {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    val sourceList = classSchedule[fromDayIdx] ?: emptyList()
    classSchedule[toDayIdx] = sourceList.toList()
    currentAll[classId] = classSchedule.toMap()
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  fun replaceDaySchedule(classId: String, dayIdx: Int, subjects: List<String>) {
    if (subjects.isEmpty()) return
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    classSchedule[dayIdx] = subjects.map { it.trim() }.filter { it.isNotBlank() }
    currentAll[classId] = classSchedule.toMap()
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  fun replaceWeekSchedule(classId: String, weekMap: Map<Int, List<String>>) {
    if (weekMap.isEmpty()) return
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    for ((dayIdx, list) in weekMap) {
      val cleanList = list.map { it.trim() }.filter { it.isNotBlank() }
      if (cleanList.isNotEmpty()) {
        classSchedule[dayIdx] = cleanList
      }
    }
    currentAll[classId] = classSchedule.toMap()
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  fun updateSchedulePeriod(classId: String, dayIdx: Int, periodIdxZeroBased: Int, subjectName: String) {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    val dayList = (classSchedule[dayIdx] ?: emptyList()).toMutableList()
    if (periodIdxZeroBased in dayList.indices) {
      dayList[periodIdxZeroBased] = subjectName
      classSchedule[dayIdx] = dayList.toList()
      currentAll[classId] = classSchedule.toMap()
      _schedulesFlow.value = currentAll.toMap()
      persistData()
    }
  }

  fun addPeriodToDay(classId: String, dayIdx: Int, subjectName: String = "") {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    val dayList = (classSchedule[dayIdx] ?: emptyList()).toMutableList()
    val name = if (subjectName.isNotBlank()) subjectName.trim() else "Дарси ${dayList.size + 1}"
    dayList.add(name)
    classSchedule[dayIdx] = dayList.toList()
    currentAll[classId] = classSchedule.toMap()
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  fun deletePeriodFromDay(classId: String, dayIdx: Int, periodIdxZeroBased: Int) {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    val dayList = (classSchedule[dayIdx] ?: emptyList()).toMutableList()
    if (periodIdxZeroBased in dayList.indices) {
      dayList.removeAt(periodIdxZeroBased)
      classSchedule[dayIdx] = dayList.toList()
      currentAll[classId] = classSchedule.toMap()
      _schedulesFlow.value = currentAll.toMap()
      persistData()
    }
  }

  fun adjustPeriodsCount(classId: String, dayIdx: Int, amount: Int) {
    val currentAll = _schedulesFlow.value.toMutableMap()
    val classSchedule = (currentAll[classId] ?: emptyMap()).toMutableMap()
    val dayList = (classSchedule[dayIdx] ?: emptyList()).toMutableList()
    if (amount > 0) {
      dayList.add("Дарси ${dayList.size + 1}")
    } else if (amount < 0 && dayList.isNotEmpty()) {
      dayList.removeAt(dayList.lastIndex)
    }
    classSchedule[dayIdx] = dayList.toList()
    currentAll[classId] = classSchedule.toMap()
    _schedulesFlow.value = currentAll.toMap()
    persistData()
  }

  // --- Attendance operations ---

  fun getMaxPeriodsForDate(classId: String, dateStr: String): Int {
    val dayIdx = getDayOfWeekIndex(dateStr)
    val daySubjects = _schedulesFlow.value[classId]?.get(dayIdx)
    return if (!daySubjects.isNullOrEmpty()) daySubjects.size else 7
  }

  fun getDayOfWeekIndex(dateStr: String): Int {
    return try {
      val cal = Calendar.getInstance()
      val date = dateFormat.parse(dateStr)
      if (date != null) {
        cal.time = date
        cal.get(Calendar.DAY_OF_WEEK) - 1
      } else 1
    } catch (e: Exception) {
      1
    }
  }

  fun confirmPeriodAttendance(date: String, classId: String, period: Int) {
    val key = "$date|$classId|$period"
    _confirmedPeriodsFlow.value = _confirmedPeriodsFlow.value + key
    persistData()
  }

  fun toggleStudentAttendance(
    date: String,
    classId: String,
    period: Int,
    studentId: String,
    targetStatus: AttendanceStatus
  ) {
    val maxPeriodForDay = getMaxPeriodsForDate(classId, date).coerceAtLeast(period)

    val currentAll = _attendanceFlow.value
    val oldDateMap = currentAll[date] ?: emptyMap()
    val oldClassMap = oldDateMap[classId] ?: emptyMap()
    val currentPeriodMap = oldClassMap[period] ?: emptyMap()

    val currentStatusStr = currentPeriodMap[studentId]
    val newStatusStr: String? = if (currentStatusStr == targetStatus.code) {
      null
    } else {
      targetStatus.code
    }

    val newClassMap = oldClassMap.toMutableMap()
    val newConfirmed = _confirmedPeriodsFlow.value.toMutableSet()

    for (p in period..maxPeriodForDay) {
      val pMap = (newClassMap[p] ?: emptyMap()).toMutableMap()
      if (newStatusStr == null) {
        pMap.remove(studentId)
      } else {
        pMap[studentId] = newStatusStr
      }
      newClassMap[p] = pMap.toMap()
      newConfirmed.add("$date|$classId|$p")
    }

    val newDateMap = oldDateMap.toMutableMap()
    newDateMap[classId] = newClassMap.toMap()

    val newAll = currentAll.toMutableMap()
    newAll[date] = newDateMap.toMap()

    _attendanceFlow.value = newAll.toMap()
    _confirmedPeriodsFlow.value = newConfirmed.toSet()
    persistData()
  }

  fun recordLateArrival(
    date: String,
    classId: String,
    period: Int,
    studentId: String,
    minutes: Int
  ) {
    val currentAll = _attendanceFlow.value
    val oldDateMap = currentAll[date] ?: emptyMap()
    val oldClassMap = oldDateMap[classId] ?: emptyMap()
    val currentPeriodMap = (oldClassMap[period] ?: emptyMap()).toMutableMap()

    if (minutes <= 0) {
      currentPeriodMap.remove(studentId)
    } else {
      currentPeriodMap[studentId] = "L_$minutes"
    }

    val newClassMap = oldClassMap.toMutableMap()
    newClassMap[period] = currentPeriodMap.toMap()

    val newDateMap = oldDateMap.toMutableMap()
    newDateMap[classId] = newClassMap.toMap()

    val newAll = currentAll.toMutableMap()
    newAll[date] = newDateMap.toMap()

    val newConfirmed = _confirmedPeriodsFlow.value.toMutableSet()
    newConfirmed.add("$date|$classId|$period")

    _attendanceFlow.value = newAll.toMap()
    _confirmedPeriodsFlow.value = newConfirmed.toSet()
    persistData()
  }

  fun saveTeacherLesson(lesson: TeacherLesson) {
    val current = _teacherScheduleFlow.value.filterNot {
      it.id == lesson.id || (it.dayOfWeek == lesson.dayOfWeek && it.period == lesson.period)
    }.toMutableList()
    current.add(lesson)
    _teacherScheduleFlow.value = current.sortedWith(compareBy({ it.dayOfWeek }, { it.period }))
    persistData()
  }

  fun deleteTeacherLesson(lessonId: String) {
    _teacherScheduleFlow.value = _teacherScheduleFlow.value.filterNot { it.id == lessonId }
    persistData()
  }

  fun setTeacherPeriodFree(dayOfWeek: Int, period: Int) {
    saveTeacherLesson(
      TeacherLesson(
        dayOfWeek = dayOfWeek,
        period = period,
        isFreePeriod = true,
        note = "Соати озод (тиреза)"
      )
    )
  }

  // --- Academic Year, Language & Discipline Notes ---

  fun updateAcademicYearConfig(config: AcademicYearConfig) {
    _academicYearConfigFlow.value = config
    persistData()
  }

  fun setAppLanguage(language: AppLanguage) {
    _appLanguageFlow.value = language
    persistData()
  }

  fun saveDisciplineNote(note: DisciplineNote) {
    val updated = _disciplineNotesFlow.value.filterNot { it.id == note.id }.toMutableList()
    updated.add(0, note)
    _disciplineNotesFlow.value = updated.sortedByDescending { it.createdAt }
    persistData()
  }

  fun deleteDisciplineNote(noteId: String) {
    _disciplineNotesFlow.value = _disciplineNotesFlow.value.filterNot { it.id == noteId }
    persistData()
  }

  fun addCustomDisciplineCategory(categoryName: String) {
    val clean = categoryName.trim()
    if (clean.isBlank()) return
    val current = _customDisciplineCategoriesFlow.value
    if (defaultDisciplineCategories.any { it.equals(clean, ignoreCase = true) } ||
      current.any { it.equals(clean, ignoreCase = true) }
    ) {
      return
    }
    _customDisciplineCategoriesFlow.value = listOf(clean) + current
    persistData()
  }

  fun calculateReportForRange(
    classId: String,
    startDateStr: String,
    endDateStr: String
  ): List<StudentReportStat> {
    val targetClass = _classesFlow.value.find { it.id == classId } ?: return emptyList()
    val classSched = _schedulesFlow.value[classId] ?: emptyMap()
    val attMap = _attendanceFlow.value

    val matchingDates = attMap.keys.filter { d ->
      d >= startDateStr && d <= endDateStr
    }.sorted()

    return targetClass.students.map { student ->
      var sbHours = 0
      var bsbHours = 0
      var sbFullDays = 0
      var bsbFullDays = 0
      var lateCount = 0
      var totalLateMinutes = 0
      val details = mutableListOf<StudentDailyDetail>()

      for (dateStr in matchingDates) {
        val dayClassAtt = attMap[dateStr]?.get(classId) ?: continue
        val dayIdx = getDayOfWeekIndex(dateStr)
        val daySubjects = classSched[dayIdx] ?: emptyList()
        val totalPeriodsForDay = if (daySubjects.isNotEmpty()) {
          daySubjects.size
        } else {
          (dayClassAtt.keys.maxOrNull() ?: 7).coerceAtLeast(1)
        }

        var daySbCount = 0
        var dayBsbCount = 0
        val missedList = mutableListOf<Pair<Int, Pair<String, AttendanceStatus>>>()

        val maxPeriodToInspect = maxOf(totalPeriodsForDay, dayClassAtt.keys.maxOrNull() ?: 0)
        for (p in 1..maxPeriodToInspect) {
          val stStatus = dayClassAtt[p]?.get(student.id)
          val subjName = daySubjects.getOrNull(p - 1) ?: "Соати $p"
          if (stStatus == "SB") {
            sbHours++
            if (p <= totalPeriodsForDay) daySbCount++
            missedList.add(p to (subjName to AttendanceStatus.SB))
          } else if (stStatus == "BSB") {
            bsbHours++
            if (p <= totalPeriodsForDay) dayBsbCount++
            missedList.add(p to (subjName to AttendanceStatus.BSB))
          } else if (stStatus?.startsWith("L_") == true || stStatus?.startsWith("LATE_") == true || stStatus == "L") {
            val mins = stStatus.substringAfter("_").toIntOrNull() ?: 15
            lateCount++
            totalLateMinutes += mins
            missedList.add(p to (subjName to AttendanceStatus.LATE))
          }
        }

        if (missedList.isNotEmpty()) {
          val isFullSb = totalPeriodsForDay > 0 && daySbCount >= totalPeriodsForDay
          val isFullBsb = totalPeriodsForDay > 0 && dayBsbCount >= totalPeriodsForDay
          if (isFullSb) sbFullDays++
          if (isFullBsb) bsbFullDays++

          val dayName = TajikDateUtils.DAYS_OF_WEEK.getOrElse(dayIdx) { "" }
          details.add(
            StudentDailyDetail(
              dateStr = dateStr,
              formattedDate = TajikDateUtils.formatDateDisplay(dateStr),
              dayName = dayName,
              isFullDaySB = isFullSb,
              isFullDayBSB = isFullBsb,
              missedPeriods = missedList
            )
          )
        }
      }

      StudentReportStat(
        studentId = student.id,
        studentName = student.name,
        parentPhone = student.parentPhone,
        sbHours = sbHours,
        bsbHours = bsbHours,
        sbFullDays = sbFullDays,
        bsbFullDays = bsbFullDays,
        lateCount = lateCount,
        totalLateMinutes = totalLateMinutes,
        dailyDetails = details.reversed()
      )
    }
  }

  fun calculateSmartAnalytics(
    classId: String,
    startDateStr: String,
    endDateStr: String
  ): SmartAnalyticsSummary {
    val classSched = _schedulesFlow.value[classId] ?: emptyMap()
    val attMap = _attendanceFlow.value

    val matchingDates = attMap.keys.filter { d ->
      d >= startDateStr && d <= endDateStr
    }

    val dayMissedCounts = mutableMapOf<Int, Int>()
    val periodMissedCounts = mutableMapOf<Int, Int>()
    val subjectMissedCounts = mutableMapOf<String, Int>()
    val subjectBsbCounts = mutableMapOf<String, Int>()

    for (dateStr in matchingDates) {
      val dayClassAtt = attMap[dateStr]?.get(classId) ?: continue
      val dayIdx = getDayOfWeekIndex(dateStr)
      val daySubjects = classSched[dayIdx] ?: emptyList()

      for ((periodNum, stMap) in dayClassAtt) {
        val subjName = daySubjects.getOrNull(periodNum - 1)?.takeIf { it.isNotBlank() } ?: "Соати $periodNum"
        for ((_, status) in stMap) {
          if (status == "SB" || status == "BSB") {
            dayMissedCounts[dayIdx] = (dayMissedCounts[dayIdx] ?: 0) + 1
            periodMissedCounts[periodNum] = (periodMissedCounts[periodNum] ?: 0) + 1
            subjectMissedCounts[subjName] = (subjectMissedCounts[subjName] ?: 0) + 1
            if (status == "BSB") {
              subjectBsbCounts[subjName] = (subjectBsbCounts[subjName] ?: 0) + 1
            }
          }
        }
      }
    }

    val topDayEntry = dayMissedCounts.maxByOrNull { it.value }
    val topPeriodEntry = periodMissedCounts.maxByOrNull { it.value }
    val topSubjectEntry = subjectMissedCounts.maxByOrNull { it.value }
    val topBsbSubjectEntry = subjectBsbCounts.maxByOrNull { it.value }

    return SmartAnalyticsSummary(
      mostMissedDayName = topDayEntry?.key?.let { TajikDateUtils.DAYS_OF_WEEK.getOrNull(it) },
      mostMissedDayHours = topDayEntry?.value ?: 0,
      mostMissedPeriod = topPeriodEntry?.key,
      mostMissedPeriodHours = topPeriodEntry?.value ?: 0,
      mostMissedSubject = topSubjectEntry?.key,
      mostMissedSubjectHours = topSubjectEntry?.value ?: 0,
      mostBsbSubject = topBsbSubjectEntry?.key,
      mostBsbSubjectHours = topBsbSubjectEntry?.value ?: 0
    )
  }
}
