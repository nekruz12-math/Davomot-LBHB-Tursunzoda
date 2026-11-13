package com.example.ui.screens.discipline

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AcademicYearConfig
import com.example.data.model.DisciplineNote
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.AppStrings
import com.example.ui.components.TajikCalendarDialog
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.StatusRed

private fun formatDdMmYyyy(dateStr: String): String {
  val parts = dateStr.split("-")
  return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
}

@Composable
fun DisciplineScreen(
  currentClass: SchoolClass?,
  selectedDate: String,
  academicYearConfig: AcademicYearConfig,
  disciplineNotes: List<DisciplineNote>,
  defaultCategories: List<String>,
  customCategories: List<String>,
  appStrings: AppStrings,
  isDarkMode: Boolean,
  onPrevDate: () -> Unit,
  onNextDate: () -> Unit,
  onSelectDate: (String) -> Unit,
  onSaveNote: (noteId: String?, studentId: String, studentName: String, dateStr: String, category: String, comment: String) -> Unit,
  onDeleteNote: (String) -> Unit,
  onAddCustomCategory: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val students = currentClass?.students ?: emptyList()
  val currentClassId = currentClass?.id ?: "1"

  // 0 = All Quarters, 1..4 = Q1..Q4
  var selectedQuarterFilter by remember { mutableIntStateOf(0) }
  var selectedCategoryFilter by remember { mutableStateOf("") } // "" = All categories
  var showAddOrEditDialog by remember { mutableStateOf(false) }
  var editingNote by remember { mutableStateOf<DisciplineNote?>(null) }
  var showDateCalendar by remember { mutableStateOf(false) }

  val allCategories = remember(defaultCategories, customCategories) {
    (customCategories + defaultCategories).distinct()
  }

  val classNotes = remember(
    disciplineNotes,
    currentClassId,
    selectedDate,
    selectedQuarterFilter,
    selectedCategoryFilter,
    academicYearConfig
  ) {
    disciplineNotes.filter { note ->
      val matchClass = note.classId == currentClassId
      val matchDateOrQuarter = if (selectedQuarterFilter == 0) {
        note.dateStr == selectedDate
      } else {
        academicYearConfig.getQuarterForDate(note.dateStr) == selectedQuarterFilter
      }
      val matchCat = selectedCategoryFilter.isBlank() || note.category == selectedCategoryFilter
      matchClass && matchDateOrQuarter && matchCat
    }
  }

  val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6F9)
  val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
  val accentBlue = Color(0xFF1D4ED8)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(screenBg)
      .testTag("discipline_screen")
  ) {
    // 1. Top Date Selector Bar: < dd-MM-yyyy >
    Surface(
      color = cardBg,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
            .border(
              1.dp,
              if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
              RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = {
              selectedQuarterFilter = 0
              onPrevDate()
            },
            modifier = Modifier
              .size(40.dp)
              .testTag("discipline_prev_day_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
              contentDescription = "Рӯзи гузашта",
              tint = primaryText,
              modifier = Modifier.size(26.dp)
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { showDateCalendar = true }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("discipline_date_display")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = accentBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = formatDdMmYyyy(selectedDate),
              fontSize = 15.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = primaryText
            )
          }

          IconButton(
            onClick = {
              selectedQuarterFilter = 0
              onNextDate()
            },
            modifier = Modifier
              .size(40.dp)
              .testTag("discipline_next_day_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Рӯзи оянда",
              tint = primaryText,
              modifier = Modifier.size(26.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Title "Қайдҳои интизом" + "+ Илова" Button side by side
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = appStrings.disciplineTitle,
              fontSize = 19.sp,
              fontWeight = FontWeight.Black,
              color = primaryText
            )
            Text(
              text = "${classNotes.size} ${appStrings.totalIncidentsSuffix}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = secondaryText
            )
          }

          Button(
            onClick = {
              editingNote = null
              showAddOrEditDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier.testTag("discipline_add_note_btn")
          ) {
            Icon(
              imageVector = Icons.Default.PostAdd,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = appStrings.disciplineAddBtn,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Filter Pills: All Quarters (Selected Date), Ч1, Ч2, Ч3, Ч4 + Category chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          item {
            val isAllSelected = selectedQuarterFilter == 0
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(
                  when {
                    isAllSelected -> accentBlue
                    isDarkMode -> Color(0xFF334155)
                    else -> Color(0xFFF1F5F9)
                  }
                )
                .clickable { selectedQuarterFilter = 0 }
                .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
              Text(
                text = appStrings.disciplineAllQuarters,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAllSelected) Color.White else primaryText
              )
            }
          }

          items(listOf(1, 2, 3, 4)) { qNum ->
            val isQSelected = selectedQuarterFilter == qNum
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(
                  when {
                    isQSelected -> accentBlue
                    isDarkMode -> Color(0xFF334155)
                    else -> Color(0xFFF1F5F9)
                  }
                )
                .clickable {
                  selectedQuarterFilter = if (selectedQuarterFilter == qNum) 0 else qNum
                }
                .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${appStrings.quarterPrefix}$qNum",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isQSelected) Color.White else primaryText
              )
            }
          }

          item {
            val isAllCat = selectedCategoryFilter.isBlank()
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(
                  if (!isAllCat) Color(0xFFDBEAFE)
                  else if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)
                )
                .clickable {
                  if (selectedCategoryFilter.isNotBlank()) {
                    selectedCategoryFilter = ""
                  }
                }
                .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (isAllCat) appStrings.disciplineAllCategories else selectedCategoryFilter,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (!isAllCat) accentBlue else secondaryText
              )
            }
          }
        }
      }
    }

    // 4. Content Area: Empty State or Discipline Notes List
    if (classNotes.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = cardBg),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("discipline_empty_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AssignmentLate,
                contentDescription = null,
                tint = secondaryText,
                modifier = Modifier.size(30.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = appStrings.disciplineEmptyTitle,
              fontSize = 16.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = primaryText,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = appStrings.disciplineEmptySubtitle,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = secondaryText,
              textAlign = TextAlign.Center,
              lineHeight = 18.sp
            )
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("discipline_notes_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(classNotes, key = { it.id }) { note ->
          val qNum = academicYearConfig.getQuarterForDate(note.dateStr)
          DisciplineNoteCard(
            note = note,
            quarterLabel = "${appStrings.quarterPrefix}$qNum",
            cardBg = cardBg,
            primaryText = primaryText,
            secondaryText = secondaryText,
            isDarkMode = isDarkMode,
            onEdit = {
              editingNote = note
              showAddOrEditDialog = true
            },
            onDelete = { onDeleteNote(note.id) }
          )
        }
      }
    }
  }

  if (showAddOrEditDialog) {
    AddEditDisciplineNoteDialog(
      students = students,
      initialNote = editingNote,
      defaultDateStr = selectedDate,
      allCategories = allCategories,
      appStrings = appStrings,
      isDarkMode = isDarkMode,
      onAddCustomCategory = onAddCustomCategory,
      onDismiss = {
        showAddOrEditDialog = false
        editingNote = null
      },
      onSave = { noteId, stId, stName, dStr, cat, comment ->
        onSaveNote(noteId, stId, stName, dStr, cat, comment)
        showAddOrEditDialog = false
        editingNote = null
      }
    )
  }

  if (showDateCalendar) {
    TajikCalendarDialog(
      currentDateString = selectedDate,
      onDismiss = { showDateCalendar = false },
      onDateSelected = { picked ->
        onSelectDate(picked)
        showDateCalendar = false
      }
    )
  }
}

@Composable
private fun DisciplineNoteCard(
  note: DisciplineNote,
  quarterLabel: String,
  cardBg: Color,
  primaryText: Color,
  secondaryText: Color,
  isDarkMode: Boolean,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
        shape = RoundedCornerShape(16.dp)
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = note.studentName,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = primaryText
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "${formatDdMmYyyy(note.dateStr)} • Чоряки ${quarterLabel.removePrefix("Ч").removePrefix("Q")}",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = secondaryText
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isDarkMode) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFDBEAFE))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = note.category,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isDarkMode) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFEFF6FF))
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Таҳрир",
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0xFFFEE2E2))
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Нест кардан",
              tint = StatusRed,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      if (note.comment.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = note.comment,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = primaryText.copy(alpha = 0.88f),
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
private fun AddEditDisciplineNoteDialog(
  students: List<Student>,
  initialNote: DisciplineNote?,
  defaultDateStr: String,
  allCategories: List<String>,
  appStrings: AppStrings,
  isDarkMode: Boolean,
  onAddCustomCategory: (String) -> Unit,
  onDismiss: () -> Unit,
  onSave: (noteId: String?, studentId: String, studentName: String, dateStr: String, category: String, comment: String) -> Unit
) {
  var studentQuery by remember { mutableStateOf("") }
  var selectedStudent by remember {
    val initSt = students.find { it.id == initialNote?.studentId } ?: students.firstOrNull()
    mutableStateOf(initSt)
  }
  var noteDateStr by remember { mutableStateOf(initialNote?.dateStr ?: defaultDateStr) }
  var selectedCategory by remember {
    mutableStateOf(initialNote?.category?.takeIf { it.isNotBlank() } ?: allCategories.firstOrNull().orEmpty())
  }
  var commentText by remember { mutableStateOf(initialNote?.comment ?: "") }
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var newCategoryInput by remember { mutableStateOf("") }
  var showCalendarDialog by remember { mutableStateOf(false) }

  val filteredStudents = remember(students, studentQuery) {
    val q = studentQuery.trim()
    if (q.isEmpty()) students
    else students.filter { it.name.contains(q, ignoreCase = true) }
  }

  val dialogBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
  val itemBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
  val activeBlue = Color(0xFF1D4ED8)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = dialogBg,
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .testTag("discipline_add_edit_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // 1. Student Search Box
        OutlinedTextField(
          value = studentQuery,
          onValueChange = { studentQuery = it },
          placeholder = {
            Text(
              text = appStrings.disciplineSearchStudentPlaceholder,
              fontSize = 13.sp,
              color = secondaryText
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = secondaryText
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = activeBlue,
            unfocusedBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Scrollable Student List (бо лента поёну боло мешудагӣ)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(162.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF1F5F9))
            .padding(6.dp)
        ) {
          if (filteredStudents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(
                text = "Хонанда ёфт нашуд",
                fontSize = 13.sp,
                color = secondaryText
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(filteredStudents, key = { it.id }) { st ->
                val isSelected = selectedStudent?.id == st.id
                val initialChar = st.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "Х"
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) activeBlue else cardColorForItem(isDarkMode))
                    .clickable { selectedStudent = st }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                          if (isSelected) Color.White.copy(alpha = 0.22f)
                          else Color(0xFFDBEAFE)
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = initialChar,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else activeBlue
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = st.name,
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else primaryText,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Date Row
        Text(
          text = appStrings.disciplineDateLabel,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = secondaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(itemBg)
            .border(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .clickable { showCalendarDialog = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
          Text(
            text = formatDdMmYyyy(noteDateStr),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryText
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Category Header + "+ Категория" button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = appStrings.disciplineCategoryLabel,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = secondaryText
          )
          Text(
            text = appStrings.disciplineAddCategoryBtn,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = activeBlue,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable {
                newCategoryInput = ""
                showAddCategoryDialog = true
              }
              .padding(horizontal = 6.dp, vertical = 3.dp)
              .testTag("discipline_add_custom_category_btn")
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Scrollable Categories List (бо лентаи поёну боло мерафтагӣ)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF1F5F9))
            .padding(6.dp)
        ) {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(allCategories) { cat ->
              val isCatSelected = selectedCategory == cat
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isCatSelected) activeBlue else cardColorForItem(isDarkMode))
                  .border(
                    width = 1.dp,
                    color = if (isCatSelected) activeBlue else Color(0xFFE2E8F0).copy(alpha = if (isDarkMode) 0.15f else 1f),
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable { selectedCategory = cat }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = cat,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isCatSelected) Color.White else primaryText
                )
                if (isCatSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Comment / Details Input
        Text(
          text = appStrings.disciplineCommentLabel,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = secondaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = commentText,
          onValueChange = { commentText = it },
          placeholder = {
            Text(
              text = appStrings.disciplineCommentPlaceholder,
              fontSize = 12.5.sp,
              color = secondaryText
            )
          },
          maxLines = 2,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("discipline_comment_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Save & Cancel Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text(
              text = appStrings.cancelButton,
              fontWeight = FontWeight.Bold,
              color = secondaryText
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val st = selectedStudent ?: return@Button
              val cat = selectedCategory.ifBlank { allCategories.firstOrNull() ?: "Дигар" }
              onSave(initialNote?.id, st.id, st.name, noteDateStr, cat, commentText)
            },
            enabled = selectedStudent != null,
            colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("discipline_save_note_btn")
          ) {
            Text(
              text = appStrings.saveButton,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }
        }
      }
    }
  }

  if (showAddCategoryDialog) {
    AlertDialog(
      onDismissRequest = { showAddCategoryDialog = false },
      title = {
        Text(
          text = "Намуди нави интизом",
          fontSize = 16.sp,
          fontWeight = FontWeight.ExtraBold
        )
      },
      text = {
        OutlinedTextField(
          value = newCategoryInput,
          onValueChange = { newCategoryInput = it },
          label = { Text("Номи категория") },
          placeholder = { Text("Масалан: Бе китоби дарсӣ") },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val clean = newCategoryInput.trim()
            if (clean.isNotBlank()) {
              onAddCustomCategory(clean)
              selectedCategory = clean
              showAddCategoryDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = activeBlue)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Илова кардан", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCategoryDialog = false }) {
          Text(appStrings.cancelButton)
        }
      }
    )
  }

  if (showCalendarDialog) {
    TajikCalendarDialog(
      currentDateString = noteDateStr,
      onDismiss = { showCalendarDialog = false },
      onDateSelected = { picked ->
        noteDateStr = picked
        showCalendarDialog = false
      }
    )
  }
}

private fun cardColorForItem(isDarkMode: Boolean): Color {
  return if (isDarkMode) Color(0xFF1E293B) else Color.White
}
