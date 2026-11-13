package com.example.data.model

import java.util.Calendar

enum class AttendanceStatus(val code: String, val title: String) {
  PRESENT("P", "Ҳозир"),
  SB("SB", "СБ (Сабабнок)"),
  BSB("BSB", "БСБ (Бе сабаб)"),
  LATE("L", "Дермонӣ")
}

data class TeacherLesson(
  val id: String = java.util.UUID.randomUUID().toString(),
  val dayOfWeek: Int, // 1=Душанбе .. 6=Шанбе
  val period: Int, // 1..8
  val isFreePeriod: Boolean = false, // true = Соати озод (тиреза)
  val className: String = "", // e.g. "10 \"А\""
  val subject: String = "", // e.g. "Физика"
  val note: String = ""
)

fun parseAttendanceCode(rawCode: String?): Triple<AttendanceStatus, Int, String> {
  if (rawCode.isNullOrBlank()) return Triple(AttendanceStatus.PRESENT, 0, "Ҳозир")
  return when {
    rawCode == "SB" -> Triple(AttendanceStatus.SB, 0, "СБ (Сабабнок)")
    rawCode == "BSB" -> Triple(AttendanceStatus.BSB, 0, "БСБ (Бе сабаб)")
    rawCode.startsWith("L_") || rawCode.startsWith("LATE_") -> {
      val min = rawCode.substringAfter("_").toIntOrNull() ?: 15
      Triple(AttendanceStatus.LATE, min, "Дермонӣ ($min дақ)")
    }
    rawCode == "L" || rawCode == "LATE" -> Triple(AttendanceStatus.LATE, 15, "Дермонӣ (15 дақ)")
    else -> Triple(AttendanceStatus.PRESENT, 0, "Ҳозир")
  }
}

enum class ReportPeriodMode(val title: String) {
  DAILY("Рӯзона"),
  WEEKLY("Ҳафтаина"),
  MONTHLY("Моҳона"),
  CUSTOM("Аз сана то сана")
}

enum class FontScaleOption(val label: String, val scale: Float) {
  NORMAL("Миёна", 1.0f),
  LARGE("Калон", 1.15f),
  EXTRA_LARGE("Хеле калон", 1.3f)
}

data class Student(
  val id: String,
  val name: String,
  val parentPhone: String = "",
  val birthDate: String = "", // Format: "dd.MM" or "dd.MM.yyyy"
  val photoPath: String = "",
  val gender: String = "MALE", // "MALE" (Писар) or "FEMALE" (Духтар)
  val nationality: String = "Тоҷик",
  val address: String = "",
  val fatherName: String = "",
  val motherName: String = "",
  val motherPhone: String = "",
  val chosenProfession: String = ""
) {
  fun isBirthdayOn(dateStr: String): Boolean {
    if (birthDate.isBlank()) return false
    val parts = dateStr.split("-")
    if (parts.size != 3) return false
    val targetDayMonth = "${parts[2]}.${parts[1]}" // "dd.MM"
    val cleanBirth = birthDate.trim()
    return cleanBirth.startsWith(targetDayMonth)
  }
}

data class TodoItem(
  val id: String,
  val title: String,
  val isDone: Boolean = false
)

data class PicnicPlanItem(
  val id: String,
  val title: String,
  val isDone: Boolean = false
)

data class PicnicData(
  val dateStr: String = "",
  val locationAndNotes: String = "",
  val items: List<PicnicPlanItem> = emptyList()
)

data class DailyInspiration(
  val quoteText: String,
  val quoteAuthor: String,
  val poemLines: String,
  val poemAuthor: String,
  val fetchedDate: String = ""
)

data class SchoolClass(
  val id: String,
  val name: String,
  val students: List<Student> = emptyList(),
  val isHomeroom: Boolean = false
)

data class StudentDailyDetail(
  val dateStr: String,
  val formattedDate: String,
  val dayName: String,
  val isFullDaySB: Boolean,
  val isFullDayBSB: Boolean,
  val missedPeriods: List<Pair<Int, Pair<String, AttendanceStatus>>>
)

data class StudentReportStat(
  val studentId: String,
  val studentName: String,
  val parentPhone: String = "",
  val sbHours: Int = 0,
  val bsbHours: Int = 0,
  val sbFullDays: Int = 0,
  val bsbFullDays: Int = 0,
  val lateCount: Int = 0,
  val totalLateMinutes: Int = 0,
  val dailyDetails: List<StudentDailyDetail> = emptyList()
) {
  val totalMissedHours: Int get() = sbHours + bsbHours
  val totalFullDays: Int get() = sbFullDays + bsbFullDays
  val hasHighBsbWarning: Boolean get() = bsbHours >= 15 || bsbFullDays >= 3
}

data class SmartAnalyticsSummary(
  val mostMissedDayName: String?,
  val mostMissedDayHours: Int,
  val mostMissedPeriod: Int?,
  val mostMissedPeriodHours: Int,
  val mostMissedSubject: String?,
  val mostMissedSubjectHours: Int,
  val mostBsbSubject: String?,
  val mostBsbSubjectHours: Int
)

data class BellCountdownState(
  val isSchoolHours: Boolean,
  val isInLesson: Boolean,
  val currentOrNextPeriod: Int,
  val minutesRemaining: Int,
  val displayText: String
)

object TajikInspirationCatalog {
  val QUOTES: List<Pair<String, String>> = listOf(
    "Омӯзгор шамъест, ки худ месӯзад ва ба дигарон рӯшноӣ мебахшад." to "Абӯалӣ ибни Сино",
    "Беҳтарин сармоягузорӣ дар ҷаҳон — ин сармоягузорӣ ба илму дониш аст." to "Бенҷамин Франклин",
    "Ҳар кӣ аз омӯхтан ор дорад, дар зиндагӣ хор мемонад." to "Саъдии Шерозӣ",
    "Таълим пурқудраттарин силоҳест, ки бо он шумо метавонед ҷаҳонро тағйир диҳед." to "Нелсон Мандела",
    "Муваффақият натиҷаи қадамҳои хурдест, ки ҳар рӯз бе таваққуф гузошта мешаванд." to "Роберт Коллиер",
    "Ояндаи миллат дар дасти омӯзгор ва дар партаи мактаб сохта мешавад." to "Эмомалӣ Раҳмон",
    "Касе, ки як мактаб мекушояд, дари як зиндонро мебандад." to "Виктор Гюго",
    "Дониш чароғи ақл ва қуввати дил аст; ҳар кӣ дониш дорад, ҳамеша пирӯз аст." to "Абулқосим Фирдавсӣ",
    "Омӯзгори хуб метавонад умед бахшад, тахайюлро бедор кунад ва муҳаббат ба омӯзишро дар дил ҷой диҳад." to "Брэд Генри",
    "Ҳеҷ гоҳ аз кӯшиш даст накашед: корҳои бузург вақт ва сабр мехоҳанд." to "Стив Ҷобс"
  )

  val POEMS: List<Pair<String, String>> = listOf(
    "Дониш андар дил чароғи равшан аст,\nВ-аз ҳама бад бар тани ту ҷавшан аст." to "Абӯабдуллоҳи Рӯдакӣ",
    "Тавоно бувад ҳар кӣ доно бувад,\nЗи дониш дили пир барно бувад." to "Абулқосим Фирдавсӣ",
    "Бани одам аъзои якдигаранд,\nКи дар офариниш зи як гавҳаранд." to "Саъдии Шерозӣ",
    "То ба ҷое расид дониши ман,\nКи бидонам ҳаме, ки нодонам." to "Абӯалӣ ибни Сино",
    "Дарси муаллим ار бувад замзамаи муҳаббате,\nҶумъа ба мактаб оварад тифли гурезпойро." to "Соиби Табрезӣ",
    "Агар дар ҷаҳон набвад омӯзгор,\nШавад тира аз бехирад рӯзгор." to "Абдураҳмони Ҷомӣ",
    "Дарахти дӯстӣ биншон, ки коми дил ба бор орад,\nНиҳоли душманӣ баркан, ки ранҷи бешумор орад." to "Ҳофизи Шерозӣ",
    "Ҳар кӣ н-омӯхт аз гузашти рӯзгор,\nНиз н-омӯзад зи ҳеҷ омӯзгор." to "Абӯабдуллоҳи Рӯдакӣ",
    "Забони модарии ман — саодати ҳастии ман,\nСуруди гаҳвораи ман, ифтихору мастии ман." to "Лоиқ Шералӣ",
    "Тоҷикам, бо Тоҷикистон зиндаам,\nБо фазои кӯҳсорон зиндаам." to "Мирзо Турсунзода"
  )

  fun getFallbackForSeed(seed: Int): DailyInspiration {
    val qIdx = (seed.coerceAtLeast(0)) % QUOTES.size
    val pIdx = (seed.coerceAtLeast(0)) % POEMS.size
    val (qText, qAuthor) = QUOTES[qIdx]
    val (pLines, pAuthor) = POEMS[pIdx]
    return DailyInspiration(
      quoteText = qText,
      quoteAuthor = qAuthor,
      poemLines = pLines,
      poemAuthor = pAuthor
    )
  }
}

object TajikDateUtils {
  val DAYS_OF_WEEK = listOf(
    "Якшанбе",
    "Душанбе",
    "Сешанбе",
    "Чоршанбе",
    "Панҷшанбе",
    "Ҷумъа",
    "Шанбе"
  )

  val MONTHS = listOf(
    "Январ",
    "Феврал",
    "Март",
    "Апрел",
    "Май",
    "Июн",
    "Июл",
    "Август",
    "Сентябр",
    "Октябр",
    "Ноябр",
    "Декабр"
  )

  val COMMON_SUBJECTS = listOf(
    "Алгебра",
    "Геометрия",
    "Физика",
    "Химия",
    "Биология",
    "Адабиёт",
    "Заб. тоҷикӣ",
    "Заб. русӣ",
    "Заб. англисӣ",
    "Таърих",
    "Информатика",
    "Тарб. ҷисмонӣ",
    "География",
    "Технология",
    "Суруд",
    "Ҳуқуқ",
    "Астрономия",
    "Омодагии дифои ҳарбӣ",
    "Санъати тасвирӣ",
    "Экология",
    "Иқтисодиёт"
  )

  const val DEFAULT_SMS_TEMPLATE =
    "Ассалому алайкум! Ба маълумоти Шумо мерасонем, ки фарзандатон [Номи хонанда] имрӯз ([Сана]) дар дарси [Номи фан]-и ЛБХБ Турсунзода иштирок надорад ([Ҳолат]). Лутфан сабабашро маълум намоед."

  val LESSON_INTERVALS = listOf(
    Triple(1, 8 * 60 + 0, 8 * 60 + 45),
    Triple(2, 8 * 60 + 55, 9 * 60 + 40),
    Triple(3, 10 * 60 + 0, 10 * 60 + 45),
    Triple(4, 10 * 60 + 55, 11 * 60 + 40),
    Triple(5, 11 * 60 + 50, 12 * 60 + 35),
    Triple(6, 13 * 60 + 30, 14 * 60 + 15),
    Triple(7, 14 * 60 + 25, 15 * 60 + 10)
  )

  fun getAutoPeriodByCurrentTime(maxPeriodsToday: Int = 7): Int {
    val cal = Calendar.getInstance()
    val totalMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

    val rawPeriod = when {
      totalMinutes < (8 * 60 + 50) -> 1
      totalMinutes < (9 * 60 + 50) -> 2
      totalMinutes < (10 * 60 + 50) -> 3
      totalMinutes < (11 * 60 + 45) -> 4
      totalMinutes < (12 * 60 + 35) -> 5
      totalMinutes < (14 * 60 + 20) -> 6
      else -> 7
    }
    val safeMax = if (maxPeriodsToday > 0) maxPeriodsToday else 7
    return rawPeriod.coerceIn(1, safeMax)
  }

  fun getBellCountdownState(): BellCountdownState {
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val nowSec = cal.get(Calendar.HOUR_OF_DAY) * 3600 +
      cal.get(Calendar.MINUTE) * 60 +
      cal.get(Calendar.SECOND)

    if (dayOfWeek == Calendar.SUNDAY) {
      val remSecToMonday8am = (24 * 3600 - nowSec) + 8 * 3600
      val hours = ((remSecToMonday8am + 1800) / 3600).coerceAtLeast(1)
      return BellCountdownState(
        isSchoolHours = false,
        isInLesson = false,
        currentOrNextPeriod = 1,
        minutesRemaining = remSecToMonday8am / 60,
        displayText = "🏠 Акнун ба хона • Занг баъди $hours соат"
      )
    }

    val morningGreetingSec = 6 * 3600 // 06:00 (2 hours before 08:00)
    val startOfSchoolSec = 8 * 3600   // 08:00
    val lunchStartSec = (12 * 60 + 35) * 60 // 12:35
    val lunchEndSec = (13 * 60 + 30) * 60   // 13:30

    if (nowSec < morningGreetingSec) {
      val remSec = startOfSchoolSec - nowSec
      val hours = ((remSec + 1800) / 3600).coerceAtLeast(1)
      return BellCountdownState(
        isSchoolHours = false,
        isInLesson = false,
        currentOrNextPeriod = 1,
        minutesRemaining = remSec / 60,
        displayText = "🏠 Акнун ба хона • Занг баъди $hours соат"
      )
    }

    if (nowSec < startOfSchoolSec) {
      val remSec = startOfSchoolSec - nowSec
      val remMin = (remSec + 59) / 60
      val remText = if (remMin >= 60) {
        val hrs = remMin / 60
        val mins = remMin % 60
        if (mins == 0) "$hrs соат" else "$hrs соат $mins дақ."
      } else {
        "$remMin дақ."
      }
      return BellCountdownState(
        isSchoolHours = false,
        isInLesson = false,
        currentOrNextPeriod = 1,
        minutesRemaining = remMin,
        displayText = "☀️ Субҳ ба хайр • Занг баъди $remText"
      )
    }

    if (nowSec in lunchStartSec until lunchEndSec) {
      val remMin = ((lunchEndSec - nowSec) + 59) / 60
      return BellCountdownState(
        isSchoolHours = true,
        isInLesson = false,
        currentOrNextPeriod = 6,
        minutesRemaining = remMin,
        displayText = "🍽️ Хӯроки нисфирӯзӣ: $remMin дақ. монд"
      )
    }

    for (i in LESSON_INTERVALS.indices) {
      val (pNum, startMin, endMin) = LESSON_INTERVALS[i]
      val startSec = startMin * 60
      val endSec = endMin * 60
      if (nowSec in startSec until endSec) {
        val remMin = ((endSec - nowSec) + 59) / 60
        return BellCountdownState(
          isSchoolHours = true,
          isInLesson = true,
          currentOrNextPeriod = pNum,
          minutesRemaining = remMin,
          displayText = "🔔 Дарси $pNum: $remMin дақ. монд"
        )
      }
      if (i < LESSON_INTERVALS.lastIndex) {
        val (nextP, nextStartMin, _) = LESSON_INTERVALS[i + 1]
        val nextStartSec = nextStartMin * 60
        if (nowSec in endSec until nextStartSec) {
          val remMin = ((nextStartSec - nowSec) + 59) / 60
          return BellCountdownState(
            isSchoolHours = true,
            isInLesson = false,
            currentOrNextPeriod = nextP,
            minutesRemaining = remMin,
            displayText = "☕ Танаффус: то Дарси $nextP — $remMin дақ."
          )
        }
      }
    }

    // After 7th lesson (>= 15:10): calculate hours until next school day 08:00
    val extraSundaySec = if (dayOfWeek == Calendar.SATURDAY) 24 * 3600 else 0
    val remSecToNextBell = (24 * 3600 - nowSec) + startOfSchoolSec + extraSundaySec
    val hoursUntilBell = ((remSecToNextBell + 1800) / 3600).coerceAtLeast(1)

    return BellCountdownState(
      isSchoolHours = false,
      isInLesson = false,
      currentOrNextPeriod = 1,
      minutesRemaining = remSecToNextBell / 60,
      displayText = "🏠 Акнун ба хона • Занг баъди $hoursUntilBell соат"
    )
  }

  fun formatDateDisplay(dateStr: String): String {
    val parts = dateStr.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else dateStr
  }
}
