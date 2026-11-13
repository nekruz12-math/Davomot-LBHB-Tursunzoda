package com.example

import com.example.data.export.XlsxReportGenerator
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentDailyDetail
import com.example.data.model.StudentReportStat
import com.example.data.model.TajikDateUtils
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testStudentReportStatCalculation() {
    val stat = StudentReportStat(
      studentId = "s1",
      studentName = "Гадоев Некрӯз",
      sbHours = 7,
      bsbHours = 14,
      sbFullDays = 1,
      bsbFullDays = 2
    )
    assertEquals(21, stat.totalMissedHours)
    assertEquals(3, stat.totalFullDays)
  }

  @Test
  fun testTajikDateUtilsAndLessonIntervals() {
    assertEquals(7, TajikDateUtils.DAYS_OF_WEEK.size)
    assertEquals(12, TajikDateUtils.MONTHS.size)
    assertEquals("Душанбе", TajikDateUtils.DAYS_OF_WEEK[1])
    assertEquals("Ноябр", TajikDateUtils.MONTHS[10])
    assertEquals(7, TajikDateUtils.LESSON_INTERVALS.size)
    assertEquals(8 * 60 + 0, TajikDateUtils.LESSON_INTERVALS[0].second)
    assertEquals(8 * 60 + 45, TajikDateUtils.LESSON_INTERVALS[0].third)
    assertEquals(11 * 60 + 50, TajikDateUtils.LESSON_INTERVALS[4].second)
    assertEquals(12 * 60 + 35, TajikDateUtils.LESSON_INTERVALS[4].third)
    assertEquals(13 * 60 + 30, TajikDateUtils.LESSON_INTERVALS[5].second)
    assertEquals(14 * 60 + 15, TajikDateUtils.LESSON_INTERVALS[5].third)
    assertEquals(14 * 60 + 25, TajikDateUtils.LESSON_INTERVALS[6].second)
    assertEquals(15 * 60 + 10, TajikDateUtils.LESSON_INTERVALS[6].third)
    val autoP = TajikDateUtils.getAutoPeriodByCurrentTime(7)
    assertTrue(autoP in 1..7)
  }

  @Test
  fun testXlsxReportGeneratorCreatesValidZipAndSheetXml() {
    val schoolDates = listOf("2026-11-09", "2026-11-10")
    val reportData = listOf(
      StudentReportStat(
        studentId = "s1",
        studentName = "Алиев Фирдавс",
        sbHours = 2,
        bsbHours = 7,
        sbFullDays = 0,
        bsbFullDays = 1,
        dailyDetails = listOf(
          StudentDailyDetail(
            dateStr = "2026-11-09",
            formattedDate = "09.11.2026",
            dayName = "Душанбе",
            isFullDaySB = false,
            isFullDayBSB = false,
            missedPeriods = listOf(
              1 to ("Алгебра" to AttendanceStatus.SB),
              2 to ("Физика" to AttendanceStatus.SB)
            )
          ),
          StudentDailyDetail(
            dateStr = "2026-11-10",
            formattedDate = "10.11.2026",
            dayName = "Сешанбе",
            isFullDaySB = false,
            isFullDayBSB = true,
            missedPeriods = (1..7).map { it to ("Фан" to AttendanceStatus.BSB) }
          )
        )
      )
    )

    val bytes = XlsxReportGenerator.generateAttendanceReportXlsx(
      className = "10 «А»",
      schoolName = "ЛБХБ Турсунзода",
      periodTitle = "Моҳона — Ноябр 2026",
      monthName = "Ноябр",
      schoolDates = schoolDates,
      reportData = reportData
    )

    // ZIP local file header magic 'PK'
    assertTrue(bytes.size > 100)
    assertEquals('P'.code.toByte(), bytes[0])
    assertEquals('K'.code.toByte(), bytes[1])

    val entries = mutableMapOf<String, String>()
    ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        entries[entry.name] = zis.readBytes().toString(StandardCharsets.UTF_8)
        entry = zis.nextEntry
      }
    }

    assertTrue(entries.containsKey("[Content_Types].xml"))
    assertTrue(entries.containsKey("xl/workbook.xml"))
    assertTrue(entries.containsKey("xl/styles.xml"))
    assertTrue(entries.containsKey("xl/worksheets/sheet1.xml"))

    val sheetXml = entries["xl/worksheets/sheet1.xml"].orEmpty()
    assertTrue(sheetXml.contains("Алиев Фирдавс"))
    assertTrue(sheetXml.contains("БСБ (пурра)"))
    assertTrue(sheetXml.contains("2с СБ"))
    assertTrue(sheetXml.contains("ҲАМАГӢ:"))
    assertTrue(sheetXml.contains("СБ (соат)"))
    assertTrue(sheetXml.contains("БСБ (рӯзи пурра)"))
  }

  @Test
  fun testDailyInspirationUniquePerConsecutiveDays() {
    assertEquals(40, com.example.data.model.TajikInspirationCatalog.QUOTES.distinct().size)
    assertEquals(40, com.example.data.model.TajikInspirationCatalog.POEMS.distinct().size)

    val day1 = com.example.data.model.TajikInspirationCatalog.getForDateString("2026-11-13")
    val day2 = com.example.data.model.TajikInspirationCatalog.getForDateString("2026-11-14")
    val day3 = com.example.data.model.TajikInspirationCatalog.getForDateString("2026-11-15")

    // Calling again on the same date returns the exact same quote and poem
    val day1Repeat = com.example.data.model.TajikInspirationCatalog.getForDateString("2026-11-13")
    assertEquals(day1.quoteText, day1Repeat.quoteText)
    assertEquals(day1.poemLines, day1Repeat.poemLines)

    // Consecutive days never repeat
    org.junit.Assert.assertNotEquals(day1.quoteText, day2.quoteText)
    org.junit.Assert.assertNotEquals(day2.quoteText, day3.quoteText)
    org.junit.Assert.assertNotEquals(day1.poemLines, day2.poemLines)
    org.junit.Assert.assertNotEquals(day2.poemLines, day3.poemLines)
  }

  @Test
  fun testAcademicYearQuartersAndSemesters() {
    val config = com.example.data.model.AcademicYearConfig(
      schoolYearName = "2026-2027",
      q1Start = "2026-09-01",
      q1End = "2026-10-31",
      q2Start = "2026-11-10",
      q2End = "2026-12-30",
      q3Start = "2027-01-11",
      q3End = "2027-03-20",
      q4Start = "2027-04-01",
      q4End = "2027-06-07"
    )
    assertEquals(1, config.getQuarterForDate("2026-09-15"))
    assertEquals(1, config.getSemesterForDate("2026-09-15"))

    assertEquals(2, config.getQuarterForDate("2026-11-13"))
    assertEquals(1, config.getSemesterForDate("2026-11-13"))

    assertEquals(3, config.getQuarterForDate("2027-02-10"))
    assertEquals(2, config.getSemesterForDate("2027-02-10"))

    assertEquals(4, config.getQuarterForDate("2027-05-05"))
    assertEquals(2, config.getSemesterForDate("2027-05-05"))
  }

  @Test
  fun testAppLanguageStringsCatalog() {
    val tj = com.example.ui.AppStringsCatalog.forLanguage(com.example.data.model.AppLanguage.TJ)
    val ru = com.example.ui.AppStringsCatalog.forLanguage(com.example.data.model.AppLanguage.RU)
    val en = com.example.ui.AppStringsCatalog.forLanguage(com.example.data.model.AppLanguage.EN)

    assertEquals("Интизом", tj.navDiscipline)
    assertEquals("Дисциплина", ru.navDiscipline)
    assertEquals("Discipline", en.navDiscipline)

    assertEquals("Ч", tj.quarterPrefix)
    assertEquals("Ч", ru.quarterPrefix)
    assertEquals("Q", en.quarterPrefix)
  }
}
