package com.example.ui.screens.schedule

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.data.model.TeacherLesson
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun ScheduleScreen(
  classes: List<SchoolClass>,
  currentClass: SchoolClass?,
  schedules: Map<String, Map<Int, List<String>>>,
  teacherSchedule: List<TeacherLesson> = emptyList(),
  allKnownSubjects: List<String>,
  isDarkMode: Boolean,
  fontScale: Float,
  onUpdateSubject: (dayIdx: Int, periodIdxZeroBased: Int, subjectName: String) -> Unit,
  onAddPeriod: (dayIdx: Int, subjectName: String) -> Unit,
  onDeletePeriod: (dayIdx: Int, periodIdxZeroBased: Int) -> Unit,
  onAdjustPeriods: (dayIdx: Int, amount: Int) -> Unit,
  onCopyFromClass: (sourceClassId: String, sourceClassName: String) -> Unit,
  onCopyDayToDay: (fromDayIdx: Int, toDayIdx: Int) -> Unit,
  onSaveTeacherLesson: (dayOfWeek: Int, period: Int, isFree: Boolean, className: String, subject: String, shortSubject: String, note: String) -> Unit = { _, _, _, _, _, _, _ -> },
  onDeleteTeacherLesson: (lessonId: String) -> Unit = {},
  onSetTeacherPeriodFree: (dayOfWeek: Int, period: Int) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  var activeMainTab by remember { mutableIntStateOf(0) } // 0 = 👑 Ҷадвали синф, 1 = 👨‍🏫 Ҷадвали ман
  var selectedDayIdx by remember { mutableIntStateOf(1) } // 1=Душанбе .. 6=Шанбе
  var newSubjectText by remember { mutableStateOf("") }
  var editingTeacherLesson by remember { mutableStateOf<TeacherLesson?>(null) }
  var editingPeriodNum by remember { mutableIntStateOf(1) }
  var showTeacherEditDialog by remember { mutableStateOf(false) }

  val homeroomClass = currentClass?.takeIf { it.isHomeroom } ?: classes.find { it.isHomeroom } ?: currentClass
  val classSchedules = schedules[homeroomClass?.id] ?: emptyMap()
  val currentDaySubjects = classSchedules[selectedDayIdx] ?: emptyList()

  val schoolDays = listOf(
    1 to "Душанбе",
    2 to "Сешанбе",
    3 to "Чоршанбе",
    4 to "Панҷшанбе",
    5 to "Ҷумъа",
    6 to "Шанбе"
  )

  // Autocomplete suggestions for "newSubjectText" when user types at least 1 letter!
  val newSubjectSuggestions = remember(newSubjectText, allKnownSubjects) {
    val q = newSubjectText.trim()
    if (q.isEmpty()) emptyList()
    else allKnownSubjects.filter {
      it.startsWith(q, ignoreCase = true) && !it.equals(q, ignoreCase = true)
    }
  }

  val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6F9)
  val surfaceBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Navy900
  val secondaryText = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(screenBg)
  ) {
    // Main Top Tabs: 👑 Ҷадвали синф vs 👨‍🏫 Ҷадвали ман
    TabRow(
      selectedTabIndex = activeMainTab,
      containerColor = surfaceBg,
      contentColor = if (isDarkMode) Gold500 else Navy800
    ) {
      Tab(
        selected = activeMainTab == 0,
        onClick = { activeMainTab = 0 },
        text = {
          Text(
            text = "👑 Ҷадвали синф",
            fontWeight = if (activeMainTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 13.5.sp
          )
        }
      )
      Tab(
        selected = activeMainTab == 1,
        onClick = { activeMainTab = 1 },
        text = {
          Text(
            text = "👨‍🏫 Ҷадвали ман",
            fontWeight = if (activeMainTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 13.5.sp
          )
        }
      )
    }

    if (activeMainTab == 0) {
    // Days Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedDayIdx - 1,
      edgePadding = 12.dp,
      containerColor = surfaceBg,
      contentColor = if (isDarkMode) Gold500 else Navy800
    ) {
      schoolDays.forEach { (dayIdx, dayName) ->
        val count = classSchedules[dayIdx]?.size ?: 0
        Tab(
          selected = selectedDayIdx == dayIdx,
          onClick = { selectedDayIdx = dayIdx },
          text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = dayName,
                fontWeight = if (selectedDayIdx == dayIdx) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = (13.5f * fontScale).sp,
                color = when {
                  selectedDayIdx == dayIdx && isDarkMode -> Gold500
                  selectedDayIdx == dayIdx -> Navy800
                  else -> secondaryText
                }
              )
              Text(
                text = "$count дарс",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedDayIdx == dayIdx) (if (isDarkMode) Gold500 else Navy800) else Color(0xFF64748B)
              )
            }
          }
        )
      }
    }

    // Unified scrollable content for Add Lesson + Subjects List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Add New Lesson Box & Adjuster
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = surfaceBg),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val dayName = schoolDays.find { it.first == selectedDayIdx }?.second ?: ""
              Text(
                text = "$dayName • ${currentDaySubjects.size} дарс",
                fontSize = (15.5f * fontScale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryText
              )

              // +/- Quick Adjuster
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                  .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                  .padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                IconButton(
                  onClick = { onAdjustPeriods(selectedDayIdx, -1) },
                  enabled = currentDaySubjects.isNotEmpty(),
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("decrease_periods_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Нест кардани охирин дарс",
                    tint = if (currentDaySubjects.isNotEmpty()) StatusRed else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                  )
                }

                Text(
                  text = "${currentDaySubjects.size}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black,
                  color = Navy900,
                  modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                  onClick = { onAdjustPeriods(selectedDayIdx, 1) },
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("increase_periods_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Илова кардани дарс",
                    tint = StatusGreen,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explicit Input + Add Lesson Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = newSubjectText,
                onValueChange = { newSubjectText = it },
                textStyle = TextStyle(
                  color = Color(0xFF0F172A),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                  Text(
                    text = "Номи фанни нав...",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                  )
                },
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("new_subject_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color(0xFF0F172A),
                  unfocusedTextColor = Color(0xFF0F172A),
                  focusedPlaceholderColor = Color(0xFF475569),
                  unfocusedPlaceholderColor = Color(0xFF475569),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = Navy800,
                  unfocusedBorderColor = Color(0xFF94A3B8),
                  cursorColor = Navy800
                )
              )

              Spacer(modifier = Modifier.width(8.dp))

              Button(
                onClick = {
                  onAddPeriod(selectedDayIdx, newSubjectText)
                  newSubjectText = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .height(52.dp)
                  .testTag("add_period_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Ворид",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp
                )
              }
            }

            // Autocomplete Suggestions (Подсказка) ONLY when user starts typing!
            AnimatedVisibility(visible = newSubjectSuggestions.isNotEmpty()) {
              Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                  text = "💡 Подсказка (пахш кунед барои интихоб):",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isDarkMode) Gold500 else Navy800,
                  modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(newSubjectSuggestions) { suggestion ->
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7))
                        .border(1.dp, StatusGreen, RoundedCornerShape(8.dp))
                        .clickable {
                          onAddPeriod(selectedDayIdx, suggestion)
                          newSubjectText = ""
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Text(
                        text = "+ $suggestion",
                        fontSize = 12.5.sp,
                        color = Color(0xFF14532D),
                        fontWeight = FontWeight.ExtraBold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Subjects List for Selected Day with Individual Autocomplete & Delete Buttons
      if (currentDaySubjects.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Барои ин рӯз дарс муайян нашудааст.\nДар боло номи фанро нависед ва «Ворид»-ро пахш кунед.",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = secondaryText,
              lineHeight = 20.sp
            )
          }
        }
      } else {
        itemsIndexed(currentDaySubjects) { pIndex, subject ->
          SubjectEditCard(
            periodNumber = pIndex + 1,
            subjectName = subject,
            allKnownSubjects = allKnownSubjects,
            isDarkMode = isDarkMode,
            fontScale = fontScale,
            onSubjectChanged = { newName ->
              onUpdateSubject(selectedDayIdx, pIndex, newName)
            },
            onDeleteSubject = {
              onDeletePeriod(selectedDayIdx, pIndex)
            }
          )
        }
      }
    }
  } else {
      TeacherPersonalScheduleSection(
        selectedDayIdx = selectedDayIdx,
        schoolDays = schoolDays,
        teacherSchedule = teacherSchedule,
        isDarkMode = isDarkMode,
        fontScale = fontScale,
        onSelectDay = { selectedDayIdx = it },
        onEditPeriod = { pNum, existingLesson ->
          editingPeriodNum = pNum
          editingTeacherLesson = existingLesson
          showTeacherEditDialog = true
        },
        onSetPeriodFree = { pNum ->
          onSetTeacherPeriodFree(selectedDayIdx, pNum)
        },
        onDeleteLesson = { lessonId ->
          onDeleteTeacherLesson(lessonId)
        }
      )
    }

    if (showTeacherEditDialog) {
      TeacherLessonEditDialog(
        dayIdx = selectedDayIdx,
        dayName = schoolDays.find { it.first == selectedDayIdx }?.second ?: "Рӯз",
        periodNumber = editingPeriodNum,
        initialLesson = editingTeacherLesson,
        classes = classes,
        allKnownSubjects = allKnownSubjects,
        isDarkMode = isDarkMode,
        onDismiss = {
          showTeacherEditDialog = false
          editingTeacherLesson = null
        },
        onSave = { isFree, className, subject, shortSubject, note ->
          onSaveTeacherLesson(selectedDayIdx, editingPeriodNum, isFree, className, subject, shortSubject, note)
          showTeacherEditDialog = false
          editingTeacherLesson = null
        }
      )
    }
  }
}

@Composable
private fun SubjectEditCard(
  periodNumber: Int,
  subjectName: String,
  allKnownSubjects: List<String>,
  isDarkMode: Boolean,
  fontScale: Float,
  onSubjectChanged: (String) -> Unit,
  onDeleteSubject: () -> Unit
) {
  // Show inline autocomplete suggestions when user is typing a subject name (e.g. "А" -> "Алгебра", "Адабиёт")
  val matchingSuggestions = remember(subjectName, allKnownSubjects) {
    val q = subjectName.trim()
    if (q.isEmpty() || q.startsWith("Дарси ")) emptyList()
    else allKnownSubjects.filter {
      it.startsWith(q, ignoreCase = true) && !it.equals(q, ignoreCase = true)
    }
  }

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Navy800),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$periodNumber",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        OutlinedTextField(
          value = subjectName,
          onValueChange = onSubjectChanged,
          textStyle = TextStyle(
            color = Color(0xFF0F172A),
            fontSize = (14.5f * fontScale).sp,
            fontWeight = FontWeight.Bold
          ),
          label = {
            Text(
              text = "Соати $periodNumber-ум",
              color = Color(0xFF334155),
              fontWeight = FontWeight.SemiBold
            )
          },
          placeholder = {
            Text(
              text = "Номи фан...",
              color = Color(0xFF475569),
              fontWeight = FontWeight.Medium
            )
          },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("subject_input_$periodNumber"),
          shape = RoundedCornerShape(8.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
            focusedPlaceholderColor = Color(0xFF475569),
            unfocusedPlaceholderColor = Color(0xFF475569),
            focusedLabelColor = Navy800,
            unfocusedLabelColor = Color(0xFF334155),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Navy800,
            unfocusedBorderColor = Color(0xFF94A3B8),
            cursorColor = Navy800
          )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Explicit Delete Lesson Button
        IconButton(
          onClick = onDeleteSubject,
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEE2E2))
            .testTag("delete_period_btn_$periodNumber")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Нест кардани дарс",
            tint = StatusRed,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Inline Autocomplete Chips below the text field when typing first letter(s)
      AnimatedVisibility(visible = matchingSuggestions.isNotEmpty()) {
        LazyRow(
          modifier = Modifier.padding(top = 6.dp, start = 46.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(matchingSuggestions) { suggestion ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEFF6FF))
                .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(6.dp))
                .clickable { onSubjectChanged(suggestion) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "💡 $suggestion",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy900
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun TeacherPersonalScheduleSection(
  selectedDayIdx: Int,
  schoolDays: List<Pair<Int, String>>,
  teacherSchedule: List<TeacherLesson>,
  isDarkMode: Boolean,
  fontScale: Float,
  onSelectDay: (Int) -> Unit,
  onEditPeriod: (periodNum: Int, existingLesson: TeacherLesson?) -> Unit,
  onSetPeriodFree: (periodNum: Int) -> Unit,
  onDeleteLesson: (lessonId: String) -> Unit
) {
  val surfaceBg = if (isDarkMode) Color(0xFF1E293B) else Color.White

  val currentDayLessons = remember(teacherSchedule, selectedDayIdx) {
    teacherSchedule.filter { it.dayOfWeek == selectedDayIdx }
  }

  // Days Tabs for Teacher Schedule
  ScrollableTabRow(
    selectedTabIndex = selectedDayIdx - 1,
    edgePadding = 12.dp,
    containerColor = surfaceBg,
    contentColor = if (isDarkMode) Gold500 else Navy800
  ) {
    schoolDays.forEach { (dayIdx, dayName) ->
      val count = teacherSchedule.count { it.dayOfWeek == dayIdx && !it.isFreePeriod }
      Tab(
        selected = selectedDayIdx == dayIdx,
        onClick = { onSelectDay(dayIdx) },
        text = {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            Text(
              text = dayName,
              fontWeight = if (selectedDayIdx == dayIdx) FontWeight.ExtraBold else FontWeight.Medium,
              fontSize = 13.sp
            )
            Text(
              text = "$count дарс",
              fontSize = 10.5.sp,
              color = if (selectedDayIdx == dayIdx) {
                if (isDarkMode) Gold500 else Navy800
              } else {
                Color.Gray
              }
            )
          }
        }
      )
    }
  }

  val context = LocalContext.current
  val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }

  fun formatSlot(h: Int, m: Int): String {
    return if (is24Hour) {
      "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
    } else {
      val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
      }
      val suffix = if (h < 12) "AM" else "PM"
      "$h12:${m.toString().padStart(2, '0')} $suffix"
    }
  }

  // List of 7 school periods (08:00 – 15:10 with 12:35 – 13:30 lunch)
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(7) { pIdx ->
      val periodNum = pIdx + 1
      val lesson = currentDayLessons.find { it.period == periodNum }
      val periodTime = when (periodNum) {
        1 -> "${formatSlot(8, 0)} - ${formatSlot(8, 45)}"
        2 -> "${formatSlot(8, 55)} - ${formatSlot(9, 40)}"
        3 -> "${formatSlot(10, 0)} - ${formatSlot(10, 45)}"
        4 -> "${formatSlot(10, 55)} - ${formatSlot(11, 40)}"
        5 -> "${formatSlot(11, 50)} - ${formatSlot(12, 35)}"
        6 -> "${formatSlot(13, 30)} - ${formatSlot(14, 15)}"
        7 -> "${formatSlot(14, 25)} - ${formatSlot(15, 10)}"
        else -> "Дарси $periodNum"
      }

      TeacherPeriodCard(
        periodNumber = periodNum,
        periodTime = periodTime,
        lesson = lesson,
        isDarkMode = isDarkMode,
        fontScale = fontScale,
        onEdit = { onEditPeriod(periodNum, lesson) },
        onSetFree = { onSetPeriodFree(periodNum) },
        onDelete = {
          if (lesson != null) onDeleteLesson(lesson.id)
        }
      )
    }
  }
}

@Composable
private fun TeacherPeriodCard(
  periodNumber: Int,
  periodTime: String,
  lesson: TeacherLesson?,
  isDarkMode: Boolean,
  fontScale: Float,
  onEdit: () -> Unit,
  onSetFree: () -> Unit,
  onDelete: () -> Unit
) {
  val isFree = lesson?.isFreePeriod == true
  val hasLesson = lesson != null && !isFree

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isFree -> if (isDarkMode) Color(0xFF292524) else Color(0xFFFEF3C7)
        hasLesson -> if (isDarkMode) Color(0xFF1E293B) else Color.White
        else -> if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
      }
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = when {
        isFree -> Color(0xFFFCD34D)
        hasLesson -> if (isDarkMode) Color(0xFF334155) else Color(0xFFBFDBFE)
        else -> if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
      }
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Period number badge
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
              when {
                isFree -> Color(0xFFD97706)
                hasLesson -> Navy800
                else -> Color(0xFF94A3B8)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$periodNumber",
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 15.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = periodTime,
              fontSize = 11.sp,
              color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
              fontWeight = FontWeight.SemiBold
            )
          }

          when {
            isFree -> {
              Text(
                text = "☕ Соати озод",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkMode) Gold500 else Color(0xFF92400E)
              )
            }
            hasLesson -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = lesson?.subject ?: "",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isDarkMode) Color.White else Navy900
                )
                val badgeShort = lesson?.shortSubject?.takeIf { it.isNotBlank() }
                if (badgeShort != null) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Gold500.copy(alpha = 0.25f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = badgeShort,
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Black,
                      color = if (isDarkMode) Gold500 else Navy900
                    )
                  }
                }
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Navy800)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "📘 Синфи ${lesson?.className ?: ""}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
            else -> {
              Text(
                text = "Муайян нашудааст (Холӣ)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8)
              )
            }
          }
        }
      }

      // Actions
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (lesson != null) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Таҳрир",
              tint = if (isDarkMode) Gold500 else Navy800,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Тоза кардан",
              tint = StatusRed,
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          Button(
            onClick = onEdit,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text("Дарс", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
          }

          IconButton(
            onClick = onSetFree,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Coffee,
              contentDescription = "Соати озод",
              tint = Color(0xFFD97706),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun TeacherLessonEditDialog(
  dayIdx: Int,
  dayName: String,
  periodNumber: Int,
  initialLesson: TeacherLesson?,
  classes: List<SchoolClass>,
  allKnownSubjects: List<String>,
  isDarkMode: Boolean,
  onDismiss: () -> Unit,
  onSave: (isFree: Boolean, className: String, subject: String, shortSubject: String, note: String) -> Unit
) {
  var isFree by remember { mutableStateOf(initialLesson?.isFreePeriod ?: false) }
  var selectedClass by remember {
    mutableStateOf(initialLesson?.className?.takeIf { it.isNotBlank() } ?: classes.firstOrNull()?.name ?: "10 \"А\"")
  }
  val allowedSubjects = remember {
    listOf(
      "Алгебра" to "А",
      "Геометрия" to "Г",
      "Соати тарбиявӣ" to "С.Т"
    )
  }
  var subjectText by remember {
    val initSubj = initialLesson?.subject?.takeIf { it.isNotBlank() && it != "Соати озод" }
    mutableStateOf(initSubj ?: "Алгебра")
  }
  var shortSubjectText by remember {
    val initShort = initialLesson?.shortSubject?.takeIf { it.isNotBlank() }
      ?: when {
        subjectText.equals("Алгебра", ignoreCase = true) -> "А"
        subjectText.equals("Геометрия", ignoreCase = true) -> "Г"
        subjectText.contains("тарбияв", ignoreCase = true) -> "С.Т"
        else -> subjectText.take(3).uppercase()
      }
    mutableStateOf(initShort)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "👨‍🏫 ", fontSize = 20.sp)
        Text(
          text = "Соати $periodNumber — рӯзи $dayName",
          fontSize = 16.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Toggle: Дарс vs Соати озод
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!isFree) Navy800 else if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
              .clickable { isFree = false }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "📚 Дарс дорам",
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp,
              color = if (!isFree) Color.White else if (isDarkMode) Color.White else Navy900
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isFree) Color(0xFFD97706) else if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
              .clickable { isFree = true }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "☕ Соати озод",
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp,
              color = if (isFree) Color.White else if (isDarkMode) Color.White else Navy900
            )
          }
        }

        if (!isFree) {
          // Class Selection chips
          Text(
            text = "Синфро интихоб кунед:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(classes) { cls ->
              val isSelected = cls.name == selectedClass
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) Navy800 else if (isDarkMode) Color(0xFF334155) else Color(0xFFEFF6FF))
                  .border(
                    1.dp,
                    if (isSelected) Navy800 else Color(0xFF93C5FD),
                    RoundedCornerShape(6.dp)
                  )
                  .clickable { selectedClass = cls.name }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = cls.name,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else if (isDarkMode) Color.White else Navy900
                )
              }
            }
          }

          // 3 Fixed Subject Selection Buttons: Алгебра (А), Геометрия (Г), Соати тарбиявӣ (С.Т)
          Text(
            text = "Фанро интихоб кунед:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            allowedSubjects.forEach { (fullSubj, shortCode) ->
              val isSelectedSubj = subjectText == fullSubj
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isSelectedSubj) Navy800
                    else if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)
                  )
                  .border(
                    width = 1.2.dp,
                    color = if (isSelectedSubj) Gold500 else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable {
                    subjectText = fullSubj
                    shortSubjectText = shortCode
                  }
                  .padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = fullSubj,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelectedSubj || isDarkMode) Color.White else Navy900
                  )
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (isSelectedSubj) Gold500 else Color(0xFFE2E8F0))
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = shortCode,
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Black,
                      color = Navy900
                    )
                  }
                }
              }
            }
          }

          // 2 Custom Input Fields: 1) Номи фан  2) Шакли кӯтоҳ (2-3 ҳарф)
          Text(
            text = "Ё фанни дигарро ворид кунед:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = subjectText,
              onValueChange = { newVal ->
                subjectText = newVal
                val preset = allowedSubjects.find { it.first.equals(newVal.trim(), ignoreCase = true) }
                if (preset != null) {
                  shortSubjectText = preset.second
                } else if (shortSubjectText.isBlank() || allowedSubjects.any { it.second == shortSubjectText }) {
                  shortSubjectText = newVal.trim().take(3).uppercase()
                }
              },
              label = { Text("Номи фан", fontSize = 11.sp) },
              placeholder = { Text("Физика", fontSize = 12.sp) },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1.65f)
                .testTag("teacher_subject_full_input")
            )

            OutlinedTextField(
              value = shortSubjectText,
              onValueChange = { newShort ->
                if (newShort.length <= 4) {
                  shortSubjectText = newShort.uppercase()
                }
              },
              label = { Text("Кӯтоҳ (2-3 ҳ.)", fontSize = 10.5.sp) },
              placeholder = { Text("ФИЗ", fontSize = 12.sp) },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("teacher_subject_short_input")
            )
          }
        } else {
          // Free period: Just clean icon badge
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDarkMode) Color(0xFF292524) else Color(0xFFFEF3C7))
              .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(10.dp))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "☕",
                fontSize = 32.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Соати озод",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkMode) Gold500 else Color(0xFF92400E)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalSubject = if (isFree) "Соати озод" else subjectText.trim().ifBlank { "Алгебра" }
          val finalShort = if (isFree) "" else shortSubjectText.trim().ifBlank {
            finalSubject.take(3).uppercase()
          }
          val finalClass = if (isFree) "" else selectedClass
          onSave(isFree, finalClass, finalSubject, finalShort, "")
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isFree) Color(0xFFD97706) else Navy800
        )
      ) {
        Text("Сабт кардан", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Бекор кардан")
      }
    }
  )
}
