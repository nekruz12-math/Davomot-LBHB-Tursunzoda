package com.example.ui.screens.settings

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Girl
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.theme.Gold500
import com.example.ui.theme.Green600
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import java.io.File

@Composable
fun StudentAvatarImage(
  photoPath: String,
  gender: String,
  size: Dp,
  modifier: Modifier = Modifier
) {
  val bitmap = remember(photoPath) {
    if (photoPath.isNotBlank()) {
      try {
        val file = File(photoPath)
        if (file.exists()) {
          BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else null
      } catch (e: Exception) {
        null
      }
    } else null
  }

  val isFemale = gender == "FEMALE"
  val tintColor = if (isFemale) Color(0xFFDB2777) else Color(0xFF2563EB)
  val bgColor = if (isFemale) Color(0xFFFCE7F3) else Color(0xFFDBEAFE)

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(bgColor)
      .border(2.dp, tintColor.copy(alpha = 0.5f), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap,
        contentDescription = "Сурати хонанда",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size)
          .clip(CircleShape)
      )
    } else {
      Icon(
        imageVector = if (isFemale) Icons.Default.Girl else Icons.Default.Boy,
        contentDescription = null,
        tint = tintColor,
        modifier = Modifier.size(size * 0.68f)
      )
    }
  }
}

@Composable
fun StudentProfilesSubScreen(
  classes: List<SchoolClass>,
  currentClass: SchoolClass?,
  isDarkMode: Boolean,
  onSelectClass: (String) -> Unit,
  onSaveStudentProfile: (String, Student) -> Unit,
  onPickStudentPhoto: (String, Student, android.net.Uri, (String) -> Unit) -> Unit,
  onDialPhone: (String) -> Unit,
  onBack: () -> Unit
) {
  val selectedStudentIdState = remember { mutableStateOf<String?>(null) }
  val activeClass = currentClass ?: classes.firstOrNull()
  val selectedStudent = remember(activeClass, selectedStudentIdState.value) {
    activeClass?.students?.find { it.id == selectedStudentIdState.value }
  }

  BackHandler {
    if (selectedStudent != null) {
      selectedStudentIdState.value = null
    } else {
      onBack()
    }
  }

  val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
  val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)

  if (selectedStudent != null && activeClass != null) {
    StudentDetailProfileEditor(
      classId = activeClass.id,
      className = activeClass.name,
      student = selectedStudent,
      isDarkMode = isDarkMode,
      bgColor = bgColor,
      cardBg = cardBg,
      textPrimary = textPrimary,
      textSecondary = textSecondary,
      onSave = { updated ->
        onSaveStudentProfile(activeClass.id, updated)
      },
      onPickPhoto = { uri, onSaved ->
        onPickStudentPhoto(activeClass.id, selectedStudent, uri, onSaved)
      },
      onDialPhone = onDialPhone,
      onBackToList = { selectedStudentIdState.value = null }
    )
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
        .testTag("student_profiles_list_screen"),
      contentPadding = PaddingValues(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Submenu Header Bar
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = cardBg,
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("back_from_student_profiles")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Ба қафо",
                tint = textPrimary
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Маълумотномаи хонандагон",
                color = textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "Варақаи шахсӣ ва маълумотномаи муфассали хонандагон",
                color = textSecondary,
                fontSize = 11.5.sp
              )
            }
          }
        }
      }

      // Class selector chips
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          classes.forEach { cls ->
            val isSelected = cls.id == activeClass?.id
            Surface(
              shape = RoundedCornerShape(50),
              color = if (isSelected) Navy800 else cardBg,
              shadowElevation = 2.dp,
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onSelectClass(cls.id) }
            ) {
              Text(
                text = "Синфи ${cls.name} (${cls.students.size})",
                color = if (isSelected) Color.White else textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
              )
            }
          }
        }
      }

      val students = activeClass?.students ?: emptyList()
      if (students.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Дар ин синф ҳоло хонанда ворид нашудааст.",
              color = textSecondary,
              fontSize = 14.sp,
              modifier = Modifier.padding(16.dp)
            )
          }
        }
      } else {
        itemsIndexed(students, key = { _, st -> st.id }) { index, student ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { selectedStudentIdState.value = student.id }
              .testTag("student_profile_row_${student.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${index + 1}.",
                color = textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.width(24.dp)
              )

              StudentAvatarImage(
                photoPath = student.photoPath,
                gender = student.gender,
                size = 46.dp
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = student.name,
                  color = textPrimary,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.ExtraBold
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  val genderLabel = if (student.gender == "FEMALE") "👧 Духтар" else "👦 Писар"
                  Text(
                    text = genderLabel,
                    color = if (student.gender == "FEMALE") Color(0xFFDB2777) else Color(0xFF2563EB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                  if (student.birthDate.isNotBlank()) {
                    Text(
                      text = "• 🎂 ${student.birthDate}",
                      color = textSecondary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
                if (student.chosenProfession.isNotBlank()) {
                  Text(
                    text = "🎯 Касб: ${student.chosenProfession}",
                    color = Green600,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Кушодан",
                tint = textSecondary
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun StudentDetailProfileEditor(
  classId: String,
  className: String,
  student: Student,
  isDarkMode: Boolean,
  bgColor: Color,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  onSave: (Student) -> Unit,
  onPickPhoto: (android.net.Uri, (String) -> Unit) -> Unit,
  onDialPhone: (String) -> Unit,
  onBackToList: () -> Unit
) {
  val context = LocalContext.current
  var photoPath by remember(student.id, student.photoPath) { mutableStateOf(student.photoPath) }
  var fullName by remember(student.id) { mutableStateOf(student.name) }
  var gender by remember(student.id) { mutableStateOf(student.gender.ifBlank { "MALE" }) }
  var nationality by remember(student.id) { mutableStateOf(student.nationality.ifBlank { "Тоҷик" }) }
  var birthDate by remember(student.id) { mutableStateOf(student.birthDate) }
  var address by remember(student.id) { mutableStateOf(student.address) }
  var fatherName by remember(student.id) { mutableStateOf(student.fatherName) }
  var motherName by remember(student.id) { mutableStateOf(student.motherName) }
  var parentPhone by remember(student.id) { mutableStateOf(student.parentPhone) }
  var motherPhone by remember(student.id) { mutableStateOf(student.motherPhone) }
  var chosenProfession by remember(student.id) { mutableStateOf(student.chosenProfession) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      onPickPhoto(uri) { savedPath ->
        photoPath = savedPath
      }
    }
  }

  fun buildUpdatedStudent(): Student {
    return student.copy(
      name = fullName.trim().ifBlank { student.name },
      photoPath = photoPath,
      gender = gender,
      nationality = nationality.trim().ifBlank { "Тоҷик" },
      birthDate = birthDate.trim(),
      address = address.trim(),
      fatherName = fatherName.trim(),
      motherName = motherName.trim(),
      parentPhone = parentPhone.trim(),
      motherPhone = motherPhone.trim(),
      chosenProfession = chosenProfession.trim()
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .testTag("student_detail_profile_editor"),
    contentPadding = PaddingValues(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Top Back & Save Bar
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onBackToList,
              modifier = Modifier.testTag("back_to_student_list_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Ба рӯйхат",
                tint = textPrimary
              )
            }
            Column {
              Text(
                text = "Маълумоти хонанда",
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "Синфи $className",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Button(
            onClick = { onSave(buildUpdatedStudent()) },
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("save_student_profile_top_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Захира", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
          }
        }
      }
    }

    // Student Photo Header Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          StudentAvatarImage(
            photoPath = photoPath,
            gender = gender,
            size = 104.dp,
            modifier = Modifier.clickable {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = fullName.ifBlank { student.name },
            color = textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.testTag("pick_student_photo_button")
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Интихоби сурати хонанда",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 7 Numbered Profile Fields Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. Насаб, ном, номи падари хонанда
          ProfileSectionHeader(
            number = "1",
            title = "Насаб, ном, номи падари хонанда",
            icon = Icons.Default.Person,
            textPrimary = textPrimary
          )
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text("Масалан: Алиев Алишер Аҳмадович") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_name_input")
          )

          // 2. Ҷинс (бо иконка)
          ProfileSectionHeader(
            number = "2",
            title = "Ҷинс",
            icon = if (gender == "FEMALE") Icons.Default.Girl else Icons.Default.Boy,
            textPrimary = textPrimary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isMale = gender != "FEMALE"
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isMale) Color(0xFFDBEAFE) else (if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
              modifier = Modifier
                .weight(1f)
                .border(
                  width = if (isMale) 2.dp else 1.dp,
                  color = if (isMale) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                  shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { gender = "MALE" }
                .testTag("gender_male_option")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Boy,
                  contentDescription = "Писар",
                  tint = Color(0xFF2563EB),
                  modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "👦 Писар",
                  color = if (isMale) Color(0xFF1E3A8A) else textPrimary,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp
                )
              }
            }

            val isFemale = gender == "FEMALE"
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isFemale) Color(0xFFFCE7F3) else (if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
              modifier = Modifier
                .weight(1f)
                .border(
                  width = if (isFemale) 2.dp else 1.dp,
                  color = if (isFemale) Color(0xFFDB2777) else Color(0xFFCBD5E1),
                  shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { gender = "FEMALE" }
                .testTag("gender_female_option")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Girl,
                  contentDescription = "Духтар",
                  tint = Color(0xFFDB2777),
                  modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "👧 Духтар",
                  color = if (isFemale) Color(0xFF831843) else textPrimary,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp
                )
              }
            }
          }

          // 3. Миллат ва рӯз, моҳ, соли таваллуд
          ProfileSectionHeader(
            number = "3",
            title = "Миллат ва рӯз, моҳ, соли таваллуд",
            icon = Icons.Default.Cake,
            textPrimary = textPrimary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = nationality,
              onValueChange = { nationality = it },
              label = { Text("Миллат") },
              placeholder = { Text("Тоҷик") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(0.9f)
                .testTag("profile_nationality_input")
            )
            OutlinedTextField(
              value = birthDate,
              onValueChange = { birthDate = it },
              label = { Text("Санаи таваллуд") },
              placeholder = { Text("06.11.2010") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1.1f)
                .testTag("profile_birthdate_input")
            )
          }

          // 4. Суроға
          ProfileSectionHeader(
            number = "4",
            title = "Суроға",
            icon = Icons.Default.Home,
            textPrimary = textPrimary
          )
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = { Text("Маҳалла, кӯча, хона...") },
            singleLine = false,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_address_input")
          )

          // 5. Номи волидайн (падар, модар)
          ProfileSectionHeader(
            number = "5",
            title = "Номи волидайн (падар, модар)",
            icon = Icons.Default.FamilyRestroom,
            textPrimary = textPrimary
          )
          OutlinedTextField(
            value = fatherName,
            onValueChange = { fatherName = it },
            label = { Text("Ному насаби падар") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_father_name_input")
          )
          OutlinedTextField(
            value = motherName,
            onValueChange = { motherName = it },
            label = { Text("Ному насаби модар") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_mother_name_input")
          )

          // 6. Рақами телефони волидайн
          ProfileSectionHeader(
            number = "6",
            title = "Рақами телефони волидайн",
            icon = Icons.Default.Phone,
            textPrimary = textPrimary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = parentPhone,
              onValueChange = { parentPhone = it },
              label = { Text("Телефони падар / асосӣ") },
              placeholder = { Text("+992...") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("profile_parent_phone_input")
            )
            if (parentPhone.isNotBlank()) {
              IconButton(
                onClick = { onDialPhone(parentPhone) },
                modifier = Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Green600)
              ) {
                Icon(Icons.Default.Call, contentDescription = "Занг", tint = Color.White)
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = motherPhone,
              onValueChange = { motherPhone = it },
              label = { Text("Телефони модар (ихтиёрӣ)") },
              placeholder = { Text("+992...") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("profile_mother_phone_input")
            )
            if (motherPhone.isNotBlank()) {
              IconButton(
                onClick = { onDialPhone(motherPhone) },
                modifier = Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Green600)
              ) {
                Icon(Icons.Default.Call, contentDescription = "Занг", tint = Color.White)
              }
            }
          }

          // 7. Касби интихобнамуда
          ProfileSectionHeader(
            number = "7",
            title = "Касби интихобнамуда",
            icon = Icons.Default.Work,
            textPrimary = textPrimary
          )
          OutlinedTextField(
            value = chosenProfession,
            onValueChange = { chosenProfession = it },
            placeholder = { Text("Масалан: Духтур, Барномасоз, Муҳандис...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_profession_input")
          )

          Spacer(modifier = Modifier.height(6.dp))

          Button(
            onClick = { onSave(buildUpdatedStudent()) },
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_student_profile_bottom_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Захира кардани маълумоти хонанда",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun ProfileSectionHeader(
  number: String,
  title: String,
  icon: ImageVector,
  textPrimary: Color
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(26.dp)
        .clip(CircleShape)
        .background(Navy800),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        color = Gold500,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Navy800,
      modifier = Modifier.size(19.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      color = textPrimary,
      fontSize = 14.sp,
      fontWeight = FontWeight.ExtraBold
    )
  }
}
