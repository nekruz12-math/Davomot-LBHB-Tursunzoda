package com.example.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PicnicData
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.SettingsSubScreen
import com.example.ui.components.SchoolLogoImage
import com.example.ui.screens.classes.ClassesScreen
import com.example.ui.screens.home.PicnicPlannerWindowCard
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.theme.Gold500
import com.example.ui.theme.Green600
import com.example.ui.theme.Navy800

@Composable
fun SettingsScreen(
  subScreen: SettingsSubScreen,
  schoolName: String,
  schoolLogoPath: String,
  classes: List<SchoolClass>,
  currentClass: SchoolClass?,
  picnicData: PicnicData,
  selectedDate: String,
  isDarkMode: Boolean,
  onSelectSubScreen: (SettingsSubScreen) -> Unit,
  onToggleDarkMode: () -> Unit,
  onUpdateSchoolName: (String) -> Unit,
  onPickSchoolLogo: (Context, Uri) -> Unit,
  onResetSchoolBranding: () -> Unit,
  onSelectClass: (String) -> Unit,
  onAddClass: (String, Boolean) -> Unit,
  onUpdateClass: (classId: String, newName: String, isHomeroom: Boolean) -> Unit = { _, _, _ -> },
  onDeleteClass: (String) -> Unit,
  onAddStudent: (String, String, String) -> Unit,
  onUpdateStudent: (String, String, String, String) -> Unit,
  onDeleteStudent: (String) -> Unit,
  onSortAlphabetically: () -> Unit,
  onImportExcel: (String) -> Unit,
  onSaveBackup: (Context, Uri) -> Unit,
  onRestoreBackup: (Context, Uri) -> Unit,
  onSaveStudentProfile: (String, Student) -> Unit,
  onPickStudentPhoto: (String, Student, Uri, (String) -> Unit) -> Unit,
  onDialPhone: (String) -> Unit,
  onUpdatePicnicDate: (String) -> Unit,
  onUpdatePicnicNotes: (String) -> Unit,
  onAddPicnicItem: (String) -> Unit,
  onTogglePicnicItem: (String) -> Unit,
  onDeletePicnicItem: (String) -> Unit,
  schedules: Map<String, Map<Int, List<String>>> = emptyMap(),
  teacherSchedule: List<com.example.data.model.TeacherLesson> = emptyList(),
  allKnownSubjects: List<String> = emptyList(),
  onUpdateSubject: (Int, Int, String) -> Unit = { _, _, _ -> },
  onAddPeriod: (Int, String) -> Unit = { _, _ -> },
  onDeletePeriod: (Int, Int) -> Unit = { _, _ -> },
  onAdjustPeriods: (Int, Int) -> Unit = { _, _ -> },
  onCopyFromClass: (String, String) -> Unit = { _, _ -> },
  onCopyDayToDay: (Int, Int) -> Unit = { _, _ -> },
  onSaveTeacherLesson: (dayOfWeek: Int, period: Int, isFree: Boolean, className: String, subject: String, note: String) -> Unit = { _, _, _, _, _, _ -> },
  onDeleteTeacherLesson: (lessonId: String) -> Unit = {},
  onSetTeacherPeriodFree: (dayOfWeek: Int, period: Int) -> Unit = { _, _ -> }
) {
  val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
  val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)

  when (subScreen) {
    SettingsSubScreen.CLASSES -> {
      BackHandler { onSelectSubScreen(SettingsSubScreen.MAIN) }
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(bgColor)
      ) {
        SubScreenBackHeader(
          title = "Танзими синфҳо ва хонандагон",
          subtitle = "",
          cardBg = cardBg,
          textPrimary = textPrimary,
          textSecondary = textSecondary,
          onBack = { onSelectSubScreen(SettingsSubScreen.MAIN) }
        )
        Box(modifier = Modifier.weight(1f)) {
          ClassesScreen(
            classes = classes,
            currentClass = currentClass,
            isDarkMode = isDarkMode,
            fontScale = 1.0f,
            onSelectClass = onSelectClass,
            onAddClass = onAddClass,
            onUpdateClass = onUpdateClass,
            onDeleteClass = onDeleteClass,
            onAddStudent = onAddStudent,
            onUpdateStudent = onUpdateStudent,
            onDeleteStudent = onDeleteStudent,
            onSortAlphabetically = onSortAlphabetically,
            onImportExcel = onImportExcel,
            onSaveBackup = onSaveBackup,
            onRestoreBackup = onRestoreBackup,
            onSaveStudentProfile = onSaveStudentProfile,
            onPickStudentPhoto = onPickStudentPhoto,
            onDialPhone = onDialPhone
          )
        }
      }
    }

    SettingsSubScreen.SCHEDULE -> {
      BackHandler { onSelectSubScreen(SettingsSubScreen.MAIN) }
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(bgColor)
      ) {
        SubScreenBackHeader(
          title = "Танзими ҷадвали дарсҳо",
          subtitle = "",
          cardBg = cardBg,
          textPrimary = textPrimary,
          textSecondary = textSecondary,
          onBack = { onSelectSubScreen(SettingsSubScreen.MAIN) }
        )
        Box(modifier = Modifier.weight(1f)) {
          ScheduleScreen(
            classes = classes,
            currentClass = currentClass,
            schedules = schedules,
            teacherSchedule = teacherSchedule,
            allKnownSubjects = allKnownSubjects,
            isDarkMode = isDarkMode,
            fontScale = 1.0f,
            onUpdateSubject = onUpdateSubject,
            onAddPeriod = onAddPeriod,
            onDeletePeriod = onDeletePeriod,
            onAdjustPeriods = onAdjustPeriods,
            onCopyFromClass = onCopyFromClass,
            onCopyDayToDay = onCopyDayToDay,
            onSaveTeacherLesson = onSaveTeacherLesson,
            onDeleteTeacherLesson = onDeleteTeacherLesson,
            onSetTeacherPeriodFree = onSetTeacherPeriodFree
          )
        }
      }
    }

    SettingsSubScreen.SCHOOL_BRANDING -> {
      BackHandler { onSelectSubScreen(SettingsSubScreen.MAIN) }
      SchoolBrandingSubScreen(
        schoolName = schoolName,
        schoolLogoPath = schoolLogoPath,
        bgColor = bgColor,
        cardBg = cardBg,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        onUpdateSchoolName = onUpdateSchoolName,
        onPickSchoolLogo = onPickSchoolLogo,
        onResetSchoolBranding = onResetSchoolBranding,
        onBack = { onSelectSubScreen(SettingsSubScreen.MAIN) }
      )
    }

    SettingsSubScreen.STUDENT_PROFILES -> {
      StudentProfilesSubScreen(
        classes = classes,
        currentClass = currentClass,
        isDarkMode = isDarkMode,
        onSelectClass = onSelectClass,
        onSaveStudentProfile = onSaveStudentProfile,
        onPickStudentPhoto = onPickStudentPhoto,
        onDialPhone = onDialPhone,
        onBack = { onSelectSubScreen(SettingsSubScreen.MAIN) }
      )
    }

    SettingsSubScreen.PICNIC -> {
      BackHandler { onSelectSubScreen(SettingsSubScreen.MAIN) }
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .background(bgColor)
          .testTag("settings_picnic_subscreen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          SubScreenBackHeader(
            title = "Пикник ва Сайругашт",
            subtitle = "Интихоби рӯзи баргузории пикник ва нақшаҳо",
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            onBack = { onSelectSubScreen(SettingsSubScreen.MAIN) }
          )
        }
        item {
          PicnicPlannerWindowCard(
            picnicData = picnicData,
            defaultCalendarDate = selectedDate,
            isDarkMode = isDarkMode,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            onUpdatePicnicDate = onUpdatePicnicDate,
            onUpdatePicnicNotes = onUpdatePicnicNotes,
            onAddPicnicItem = onAddPicnicItem,
            onTogglePicnicItem = onTogglePicnicItem,
            onDeletePicnicItem = onDeletePicnicItem
          )
        }
      }
    }

    SettingsSubScreen.MAIN -> {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .background(bgColor)
          .testTag("settings_main_screen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Text(
            text = "Танзимот",
            color = textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }

        // 1. Submenu: Синфҳо (Classes & Students)
        item {
          SettingsMenuItemCard(
            title = "Синфҳо",
            subtitle = "Илова ва таҳрири синфҳо, рӯйхати хонандагон ва захираи база",
            icon = Icons.Default.Group,
            iconBgColor = Color(0xFF2563EB),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            testTag = "settings_item_classes",
            onClick = { onSelectSubScreen(SettingsSubScreen.CLASSES) }
          )
        }

        // 2. Submenu: Маълумотномаи хонандагон (Student Profiles)
        item {
          SettingsMenuItemCard(
            title = "Маълумотномаи хонандагон",
            subtitle = "Варақаи шахсӣ ва маълумотномаи муфассали хонандагон",
            icon = Icons.Default.AccountBox,
            iconBgColor = Color(0xFF7C3AED),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            testTag = "settings_item_student_profiles",
            onClick = { onSelectSubScreen(SettingsSubScreen.STUDENT_PROFILES) }
          )
        }

        // 3. Submenu: Ҷадвали дарсҳо (Танҳо барои синфи роҳбари синф)
        item {
          SettingsMenuItemCard(
            title = "Ҷадвали дарсҳо",
            subtitle = "Ҷадвали дарсҳои ҳафтаина барои синфи роҳбари синф",
            icon = Icons.Default.DateRange,
            iconBgColor = Color(0xFF0284C7),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            testTag = "settings_item_schedule",
            onClick = { onSelectSubScreen(SettingsSubScreen.SCHEDULE) }
          )
        }

        // 4. Submenu: Ном ва логотипи муассиса
        item {
          SettingsMenuItemCard(
            title = "Ном ва логотипи муассиса",
            subtitle = "Иваз кардани навиштаҷоти «$schoolName» ва гузоштани дилхоҳ сурат",
            icon = Icons.Default.School,
            iconBgColor = Color(0xFFD97706),
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            testTag = "settings_item_school_branding",
            onClick = { onSelectSubScreen(SettingsSubScreen.SCHOOL_BRANDING) }
          )
        }

        // 4. Submenu: Пикник
        item {
          SettingsMenuItemCard(
            title = "Пикник",
            subtitle = "Интихоби рӯзи баргузории пикник ва нақшаи чорабиниҳо",
            icon = Icons.Default.Park,
            iconBgColor = Green600,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            testTag = "settings_item_picnic",
            onClick = { onSelectSubScreen(SettingsSubScreen.PICNIC) }
          )
        }

        // 5. Намуди барнома (Рӯзона / Шабона)
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onToggleDarkMode() }
              .testTag("settings_theme_toggle_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      if (isDarkMode) Color(0xFF312E81) else Color(0xFFFEF3C7)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = if (isDarkMode) Gold500 else Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = "Намуди барнома (Рӯзона / Шабона)",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                  )
                  Text(
                    text = if (isDarkMode) "Ҳозир: Реҷаи шабона (Торик) 🌙" else "Ҳозир: Реҷаи рӯзона (Равшан) ☀️",
                    color = textSecondary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Gold500,
                  checkedTrackColor = Navy800
                ),
                modifier = Modifier.testTag("settings_dark_mode_switch")
              )
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun SettingsMenuItemCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconBgColor: Color,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(iconBgColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconBgColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = textPrimary,
          fontSize = 15.5.sp,
          fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          color = textSecondary,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = textSecondary
      )
    }
  }
}

@Composable
private fun SubScreenBackHeader(
  title: String,
  subtitle: String,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  onBack: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = cardBg,
    shadowElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("subscreen_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Ба қафо",
          tint = textPrimary
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = textPrimary,
          fontSize = 16.5.sp,
          fontWeight = FontWeight.Black
        )
        if (subtitle.isNotBlank()) {
          Text(
            text = subtitle,
            color = textSecondary,
            fontSize = 11.5.sp
          )
        }
      }
    }
  }
}

@Composable
private fun SchoolBrandingSubScreen(
  schoolName: String,
  schoolLogoPath: String,
  bgColor: Color,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  onUpdateSchoolName: (String) -> Unit,
  onPickSchoolLogo: (Context, Uri) -> Unit,
  onResetSchoolBranding: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var editedName by remember(schoolName) { mutableStateOf(schoolName) }

  val logoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      onPickSchoolLogo(context, uri)
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
      .testTag("school_branding_subscreen"),
    contentPadding = PaddingValues(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      SubScreenBackHeader(
        title = "Ном ва логотипи муассиса",
        subtitle = "Тағйир додани ном ва сурати болоии барнома",
        cardBg = cardBg,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        onBack = onBack
      )
    }

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
          Text(
            text = "Логотипи кунунӣ (дар боло ва дар загрузка):",
            color = textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.height(12.dp))

          SchoolLogoImage(
            schoolLogoPath = schoolLogoPath,
            size = 104.dp,
            borderWidth = 3.dp,
            borderColor = Gold500,
            modifier = Modifier.clickable {
              logoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              logoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            shape = RoundedCornerShape(50),
            modifier = Modifier.testTag("pick_custom_school_logo_button")
          ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Интихоби сурат аз телефон",
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            label = { Text("Номи муассиса дар болои барнома") },
            placeholder = { Text("ЛБХБ ТУРСУНЗОДА") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("school_name_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { onUpdateSchoolName(editedName) },
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 13.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_school_name_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Захира кардани номи муассиса",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.5.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = {
              editedName = "ЛБХБ ТУРСУНЗОДА"
              onResetSchoolBranding()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_school_branding_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Барқарор кардани ном ва логотипи аслӣ",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}
