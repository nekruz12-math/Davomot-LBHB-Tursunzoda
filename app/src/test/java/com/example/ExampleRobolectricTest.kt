package com.example

import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.TajikDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testSchoolClassesAndStudentsStructure() {
    val student1 = Student("s1", "Гадоев Некрӯз")
    val student2 = Student("s2", "Каримов Фаридун")
    val schoolClass = SchoolClass("1", "10 \"А\"", listOf(student1, student2))

    assertEquals("10 \"А\"", schoolClass.name)
    assertEquals(2, schoolClass.students.size)
    assertEquals("Гадоев Некрӯз", schoolClass.students[0].name)
  }

  @Test
  fun testAttendanceStatuses() {
    assertEquals("SB", AttendanceStatus.SB.code)
    assertEquals("BSB", AttendanceStatus.BSB.code)
    assertEquals("P", AttendanceStatus.PRESENT.code)
  }
}
