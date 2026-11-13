package com.example.ui.screens.classes

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.UploadFile
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
import com.example.data.model.Student
import com.example.ui.screens.settings.StudentAvatarImage
import com.example.ui.screens.settings.StudentDetailProfileEditor
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.StatusRed

@Composable
fun ClassesScreen(
  classes: List<SchoolClass>,
  currentClass: SchoolClass?,
  isDarkMode: Boolean,
  fontScale: Float,
  onSelectClass: (String) -> Unit,
  onAddClass: (String, Boolean) -> Unit,
  onUpdateClass: (classId: String, newName: String, isHomeroom: Boolean) -> Unit = { _, _, _ -> },
  onDeleteClass: (String) -> Unit,
  onAddStudent: (name: String, parentPhone: String, birthDate: String) -> Unit,
  onUpdateStudent: (studentId: String, newName: String, newPhone: String, newBirthDate: String) -> Unit,
  onDeleteStudent: (studentId: String) -> Unit,
  onSortAlphabetically: () -> Unit,
  onImportExcel: (String) -> Unit,
  onSaveBackup: (Context, Uri) -> Unit,
  onRestoreBackup: (Context, Uri) -> Unit,
  onSaveStudentProfile: (String, Student) -> Unit = { _, _ -> },
  onPickStudentPhoto: (String, Student, Uri, (String) -> Unit) -> Unit = { _, _, _, _ -> },
  onDialPhone: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var newClassName by remember { mutableStateOf("") }
  var isNewClassHomeroom by remember { mutableStateOf(false) }
  var singleStudentName by remember { mutableStateOf("") }
  var singleStudentPhone by remember { mutableStateOf("") }
  var singleStudentBirthDate by remember { mutableStateOf("") }
  var excelInputText by remember { mutableStateOf("") }
  var isBulkImportOpen by remember { mutableStateOf(false) }

  // Student full profile being edited (for Homeroom classes)
  var editingFullProfileStudent by remember { mutableStateOf<Student?>(null) }

  // Quick edit student name/phone
  var editingStudent by remember { mutableStateOf<Student?>(null) }
  var editingStudentName by remember { mutableStateOf("") }
  var editingStudentPhone by remember { mutableStateOf("") }
  var editingStudentBirthDate by remember { mutableStateOf("") }

  // Class to edit (name and homeroom status)
  var editingClassTarget by remember { mutableStateOf<SchoolClass?>(null) }
  var editingClassNameText by remember { mutableStateOf("") }
  var editingClassIsHomeroom by remember { mutableStateOf(false) }

  // Class to delete confirmation
  var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }

  val backupSaveLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri: Uri? ->
    if (uri != null) {
      onSaveBackup(context, uri)
    }
  }

  val backupRestoreLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      onRestoreBackup(context, uri)
    }
  }

  val students = currentClass?.students ?: emptyList()
  val isHomeroom = currentClass?.isHomeroom == true

  val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6F9)
  val surfaceBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Navy900

  if (editingFullProfileStudent != null && currentClass != null) {
    BackHandler { editingFullProfileStudent = null }
    StudentDetailProfileEditor(
      classId = currentClass.id,
      className = currentClass.name,
      student = editingFullProfileStudent!!,
      isDarkMode = isDarkMode,
      bgColor = screenBg,
      cardBg = surfaceBg,
      textPrimary = primaryText,
      textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
      onSave = { updated ->
        onSaveStudentProfile(currentClass.id, updated)
        editingFullProfileStudent = null
      },
      onPickPhoto = { uri, onSaved ->
        onPickStudentPhoto(currentClass.id, editingFullProfileStudent!!, uri, onSaved)
      },
      onDialPhone = onDialPhone,
      onBackToList = { editingFullProfileStudent = null }
    )
  } else {
    // Entire Classes screen scrolls as one unified LazyColumn so short screens can see the full student list
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .background(screenBg)
        .testTag("classes_full_scroll_list"),
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
    // 1. Classes Chips Row & Backup Controls
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceBg,
        shadowElevation = 1.5.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Интихоби синф:",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = primaryText
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFEFF6FF))
                  .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(6.dp))
                  .clickable {
                    backupSaveLauncher.launch("Baza_LBHB_Tursunzoda.json")
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Navy800,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Захираи база",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFF0FDF4))
                  .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                  .clickable {
                    backupRestoreLauncher.launch(arrayOf("application/json", "*/*"))
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Барқароркунӣ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14532D)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
          ) {
            items(classes) { cls ->
              val isSelected = cls.id == currentClass?.id
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(
                    when {
                      isSelected && cls.isHomeroom -> Color(0xFFB45309)
                      isSelected -> Navy800
                      cls.isHomeroom -> Color(0xFFFEF3C7)
                      else -> Color(0xFFF1F5F9)
                    }
                  )
                  .border(
                    width = if (cls.isHomeroom) 1.5.dp else 1.dp,
                    color = if (cls.isHomeroom) Gold500 else if (isSelected) Navy800 else Color(0xFF94A3B8),
                    shape = RoundedCornerShape(20.dp)
                  )
                  .clickable { onSelectClass(cls.id) }
                  .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                  .testTag("class_chip_${cls.id}")
              ) {
                Text(
                  text = if (cls.isHomeroom) "👑 ${cls.name}" else cls.name,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isSelected) Color.White else if (cls.isHomeroom) Color(0xFF92400E) else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Edit class name/homeroom status
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable {
                      editingClassTarget = cls
                      editingClassNameText = cls.name
                      editingClassIsHomeroom = cls.isHomeroom
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Таҳрири синф",
                    modifier = Modifier.size(13.dp),
                    tint = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF2563EB)
                  )
                }
                Spacer(modifier = Modifier.width(3.dp))
                // Delete class
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { classToDelete = cls },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Нест кардани синф",
                    modifier = Modifier.size(13.dp),
                    tint = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFFDC2626)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 2 options when adding new class: Regular class vs Homeroom class
          Text(
            text = "Иловаи синфи нав (намудро интихоб кунед):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isNewClassHomeroom) Navy800 else Color(0xFFE2E8F0))
                .clickable { isNewClassHomeroom = false }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📘 Синфи муқаррарӣ",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (!isNewClassHomeroom) Color.White else Color(0xFF334155)
              )
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isNewClassHomeroom) Color(0xFFD97706) else Color(0xFFE2E8F0))
                .clickable { isNewClassHomeroom = true }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "👑 Роҳбари синф",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNewClassHomeroom) Color.White else Color(0xFF334155)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Add Class input
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = newClassName,
              onValueChange = { newClassName = it },
              textStyle = TextStyle(
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              ),
              placeholder = {
                Text(
                  text = "Масалан 10Б",
                  fontSize = 13.sp,
                  color = Color(0xFF475569),
                  fontWeight = FontWeight.Medium
                )
              },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("new_class_input"),
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A),
                focusedPlaceholderColor = Color(0xFF475569),
                unfocusedPlaceholderColor = Color(0xFF475569),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = if (isNewClassHomeroom) Color(0xFFD97706) else Navy800,
                unfocusedBorderColor = Color(0xFF94A3B8),
                cursorColor = Navy800
              )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newClassName.isNotBlank()) {
                  onAddClass(newClassName, isNewClassHomeroom)
                  newClassName = ""
                  isNewClassHomeroom = false
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isNewClassHomeroom) Color(0xFFD97706) else Navy800
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .height(50.dp)
                .testTag("add_class_button")
            ) {
              Text(
                text = if (isNewClassHomeroom) "+ 👑 Синф" else "+ 📘 Синф",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // 2. Add Student Section
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (isHomeroom) "👑 Синфи ${currentClass?.name ?: ""}" else "📘 Синфи ${currentClass?.name ?: ""}",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isHomeroom) Color(0xFFD97706) else Navy800
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Student Name Input + Add Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = singleStudentName,
              onValueChange = { singleStudentName = it },
              textStyle = TextStyle(
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              ),
              placeholder = {
                Text(
                  text = "Ному насаби хонанда...",
                  fontSize = 13.5.sp,
                  color = Color(0xFF475569),
                  fontWeight = FontWeight.Medium
                )
              },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("single_student_input"),
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
                if (singleStudentName.isNotBlank()) {
                  onAddStudent(singleStudentName, singleStudentPhone, singleStudentBirthDate)
                  singleStudentName = ""
                  singleStudentPhone = ""
                  singleStudentBirthDate = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = if (isHomeroom) Color(0xFFD97706) else Navy800),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .height(50.dp)
                .testTag("add_student_button")
            ) {
              Text("Илова", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
            }
          }

          // In Homeroom classes, show additional phone + birth date inputs + bulk import!
          if (isHomeroom) {
            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              OutlinedTextField(
                value = singleStudentPhone,
                onValueChange = { singleStudentPhone = it },
                textStyle = TextStyle(
                  color = Color(0xFF0F172A),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                  Text(
                    text = "📞 Тел. волидон",
                    fontSize = 11.5.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                  )
                },
                singleLine = true,
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color(0xFF0F172A),
                  unfocusedTextColor = Color(0xFF0F172A),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = Navy800,
                  unfocusedBorderColor = Color(0xFF94A3B8)
                )
              )

              OutlinedTextField(
                value = singleStudentBirthDate,
                onValueChange = { singleStudentBirthDate = it },
                textStyle = TextStyle(
                  color = Color(0xFF0F172A),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                  Text(
                    text = "🎂 Зодрӯз (рр.мм)",
                    fontSize = 11.5.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                  )
                },
                singleLine = true,
                modifier = Modifier.weight(0.9f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color(0xFF0F172A),
                  unfocusedTextColor = Color(0xFF0F172A),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = Navy800,
                  unfocusedBorderColor = Color(0xFF94A3B8)
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Toggle Bulk Import Section (available for ALL classes!)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFF1F5F9))
              .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
              .clickable { isBulkImportOpen = !isBulkImportOpen }
              .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Navy800
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isBulkImportOpen) "Пӯшидани воридкунии Excel" else "Воридкунӣ аз Excel",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy900
              )
            }
            Text(
              text = if (isBulkImportOpen) "▲" else "▼",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF334155)
            )
          }

          AnimatedVisibility(visible = isBulkImportOpen) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
            ) {
              Text(
                text = "Рӯйхати хонандагонро (аз Excel/Word) ба ин ҷо нусхабардорӣ кунед:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = excelInputText,
                onValueChange = { excelInputText = it },
                textStyle = TextStyle(
                  color = Color(0xFF0F172A),
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                placeholder = {
                  Text(
                    "1. Раҳимов Алишер\n2. Каримова Зарина\n3. Собиров Рустам",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                  )
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(100.dp)
                  .testTag("excel_import_textarea"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color(0xFF0F172A),
                  unfocusedTextColor = Color(0xFF0F172A),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = Navy800,
                  unfocusedBorderColor = Color(0xFF94A3B8)
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Button(
                onClick = {
                  if (excelInputText.isNotBlank()) {
                    onImportExcel(excelInputText)
                    excelInputText = ""
                    isBulkImportOpen = false
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
                  .testTag("execute_excel_import_btn")
              ) {
                Text("Ҳамаро ворид кун 📥", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
              }
            }
          }
        }
      }
    }

    // 3. Current Students Roster Title + Alphabetical Sort Button (А–Я)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Рӯйхати хонандагон (${students.size})",
          fontSize = (14.5f * fontScale).sp,
          fontWeight = FontWeight.ExtraBold,
          color = primaryText
        )

        if (students.size > 1) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Navy800)
              .clickable { onSortAlphabetically() }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("sort_alphabetically_btn")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Аз рӯи алифбо (А–Я)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // 4. Students List items
    itemsIndexed(students, key = { _, item -> item.id }) { index, student ->
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 3.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${index + 1}.",
              fontSize = (13.5f * fontScale).sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF334155),
              modifier = Modifier.width(28.dp)
            )

            if (isHomeroom) {
              StudentAvatarImage(
                photoPath = student.photoPath,
                gender = student.gender,
                size = 38.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = student.name,
                fontSize = (14.5f * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                maxLines = 1
              )
              // Only in Homeroom class show phone, birthDate, profession
              if (isHomeroom) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (student.parentPhone.isNotBlank()) {
                    Text(
                      text = "📞 ${student.parentPhone}",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = if (isDarkMode) Color(0xFF93C5FD) else Navy800
                    )
                  }
                  if (student.birthDate.isNotBlank()) {
                    Text(
                      text = "🎂 ${student.birthDate}",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color(0xFFD97706)
                    )
                  }
                }
                if (student.chosenProfession.isNotBlank()) {
                  Text(
                    text = "🎯 Касб: ${student.chosenProfession}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F766E)
                  )
                }
              }
            }
          }

          // Action buttons
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isHomeroom) {
              // Full profile dossier editor button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFEFF6FF))
                  .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(6.dp))
                  .clickable { editingFullProfileStudent = student }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Text(
                  text = "Варақа 📝",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Navy900
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
            } else {
              // Simple quick rename
              IconButton(
                onClick = {
                  editingStudent = student
                  editingStudentName = student.name
                  editingStudentPhone = student.parentPhone
                  editingStudentBirthDate = student.birthDate
                },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Таҳрир",
                  tint = if (isDarkMode) Color(0xFF93C5FD) else Navy800,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            IconButton(
              onClick = { onDeleteStudent(student.id) },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Нест кардан",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }

  // Edit Student Dialog (Name + Parent Phone + Birth Date)
  editingStudent?.let { student ->
    AlertDialog(
      onDismissRequest = { editingStudent = null },
      containerColor = Color.White,
      title = {
        Text(
          text = "Таҳрири маълумоти хонанда",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 17.sp,
          color = Navy900
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editingStudentName,
            onValueChange = { editingStudentName = it },
            textStyle = TextStyle(
              color = Color(0xFF0F172A),
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            ),
            label = { Text("Ному насаб", color = Color(0xFF334155)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )

          OutlinedTextField(
            value = editingStudentPhone,
            onValueChange = { editingStudentPhone = it },
            textStyle = TextStyle(
              color = Color(0xFF0F172A),
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            ),
            label = { Text("📞 Телефони волидон (мас. +992900112233)", color = Color(0xFF334155)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )

          OutlinedTextField(
            value = editingStudentBirthDate,
            onValueChange = { editingStudentBirthDate = it },
            textStyle = TextStyle(
              color = Color(0xFF0F172A),
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            ),
            label = { Text("🎂 Санаи таваллуд (мас. 15.03.2010)", color = Color(0xFF334155)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editingStudentName.isNotBlank()) {
              onUpdateStudent(
                student.id,
                editingStudentName,
                editingStudentPhone,
                editingStudentBirthDate
              )
            }
            editingStudent = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Navy800)
        ) {
          Text("Сабт", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { editingStudent = null }) {
          Text("Бекор кардан", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Delete Class Confirmation Dialog
  classToDelete?.let { cls ->
    AlertDialog(
      onDismissRequest = { classToDelete = null },
      containerColor = Color.White,
      title = {
        Text(
          text = "Нест кардани синф",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 17.sp,
          color = Navy900
        )
      },
      text = {
        Text(
          text = "Шумо мехоҳед синфи «${cls.name}»-ро бо тамоми маълумотҳояш нест кунед?",
          color = Color(0xFF1E293B),
          fontSize = 14.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteClass(cls.id)
            classToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("Нест кардан", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { classToDelete = null }) {
          Text("Бекор кардан", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Edit Class (Name and Homeroom Type) Dialog
  editingClassTarget?.let { cls ->
    AlertDialog(
      onDismissRequest = { editingClassTarget = null },
      containerColor = Color.White,
      title = {
        Text(
          text = "Таҳрири маълумоти синф ✏️",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 17.sp,
          color = Navy900
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Номи синфро таҳрир кунед ва намуди онро интихоб намоед:",
            fontSize = 12.5.sp,
            color = Color(0xFF334155)
          )
          OutlinedTextField(
            value = editingClassNameText,
            onValueChange = { editingClassNameText = it },
            label = { Text("Номи синф") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )

          Text(
            text = "Намуди синф:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!editingClassIsHomeroom) Navy800 else Color(0xFFE2E8F0))
                .clickable { editingClassIsHomeroom = false }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "📘 Муқаррарӣ",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (!editingClassIsHomeroom) Color.White else Color(0xFF334155)
              )
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (editingClassIsHomeroom) Color(0xFFD97706) else Color(0xFFE2E8F0))
                .clickable { editingClassIsHomeroom = true }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "👑 Роҳбари синф",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (editingClassIsHomeroom) Color.White else Color(0xFF334155)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editingClassNameText.isNotBlank()) {
              onUpdateClass(cls.id, editingClassNameText.trim(), editingClassIsHomeroom)
            }
            editingClassTarget = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Navy800)
        ) {
          Text("Захира кардан", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { editingClassTarget = null }) {
          Text("Бекор кардан", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
}
