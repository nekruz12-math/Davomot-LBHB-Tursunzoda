package com.example.ui.screens.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.TajikDateUtils
import com.example.data.model.parseAttendanceCode
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

private data class ParentMessageModalTarget(
  val student: Student,
  val targetPhone: String,
  val isWhatsApp: Boolean,
  val initialText: String
)

@Composable
fun AttendanceScreen(
  currentClass: SchoolClass?,
  selectedDate: String,
  selectedPeriod: Int,
  dayOfWeekIndex: Int,
  todaySchedule: List<String>,
  currentPeriodAttendanceMap: Map<String, String>,
  isCurrentPeriodConfirmed: Boolean,
  smsTemplate: String,
  academicYearConfig: com.example.data.model.AcademicYearConfig = com.example.data.model.AcademicYearConfig(),
  appStrings: com.example.ui.AppStrings = com.example.ui.AppStringsCatalog.forLanguage(com.example.data.model.AppLanguage.TJ),
  isDarkMode: Boolean,
  fontScale: Float,
  searchQuery: String,
  isSearchVisible: Boolean = false,
  onPeriodSelected: (Int) -> Unit,
  onQuarterSelected: (Int) -> Unit = {},
  onSearchQueryChanged: (String) -> Unit,
  onCloseSearch: () -> Unit = {},
  onConfirmPeriodAttendance: () -> Unit,
  onToggleAttendance: (studentId: String, status: AttendanceStatus) -> Unit,
  onDialParent: (phone: String) -> Unit,
  onSendSms: (phone: String, message: String) -> Unit,
  onSendWhatsApp: (phone: String, message: String) -> Unit,
  onSavePhone: (studentId: String, phone: String) -> Unit = { _, _ -> },
  onRecordLate: (studentId: String, minutes: Int) -> Unit = { _, _ -> },
  onSaveSmsTemplate: (String) -> Unit,
  onNavigateToClasses: () -> Unit,
  modifier: Modifier = Modifier
) {
  val students = currentClass?.students ?: emptyList()
  val isHomeroom = currentClass?.isHomeroom == true
  val filteredStudents = if (searchQuery.isBlank()) {
    students
  } else {
    students.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
  }

  var messageModalTarget by remember { mutableStateOf<ParentMessageModalTarget?>(null) }
  var editableMessageText by remember { mutableStateOf("") }
  var editableTemplateText by remember { mutableStateOf(smsTemplate) }
  var showTemplateEditor by remember { mutableStateOf(false) }
  var studentForPhoneEntry by remember { mutableStateOf<Student?>(null) }
  var enteredPhoneText by remember { mutableStateOf("") }
  var studentForLateDialog by remember { mutableStateOf<Student?>(null) }
  var lateMinutesInput by remember { mutableStateOf("15") }

  // Count attendance stats reactively
  var sbCount = 0
  var bsbCount = 0
  students.forEach { st ->
    when (currentPeriodAttendanceMap[st.id]) {
      "SB" -> sbCount++
      "BSB" -> bsbCount++
    }
  }
  val presentCount = (students.size - sbCount - bsbCount).coerceAtLeast(0)

  val isSunday = dayOfWeekIndex == 0
  val currentSubject = if (isSunday) {
    "Рӯзи истироҳатӣ"
  } else {
    todaySchedule.getOrNull(selectedPeriod - 1) ?: "Дарс муайян нест"
  }
  val dayName = TajikDateUtils.DAYS_OF_WEEK.getOrElse(dayOfWeekIndex) { "" }

  val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6F9)
  val surfaceBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val secondaryText = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)

  fun buildDefaultMessageForStudent(student: Student): String {
    val formattedDate = TajikDateUtils.formatDateDisplay(selectedDate)
    val statusCode = currentPeriodAttendanceMap[student.id]
    val stText = when (statusCode) {
      "SB" -> "СБ - бо сабаб"
      "BSB" -> "БСБ - бе сабаб"
      else -> "ғоиб"
    }
    return smsTemplate
      .replace("[Номи хонанда]", student.name)
      .replace("[Сана]", formattedDate)
      .replace("[Номи фан]", currentSubject)
      .replace("[Ҳолат]", stText)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(screenBg)
  ) {
    val activeQuarter = remember(selectedDate, academicYearConfig) {
      academicYearConfig.getQuarterForDate(selectedDate)
    }
    val activeSemester = remember(activeQuarter, academicYearConfig) {
      academicYearConfig.getSemesterForQuarter(activeQuarter)
    }

    // 1. Quarter & Semester Bar + Schedule Box (without Ҷамъ/Ҳозир/СБ/БСБ)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = surfaceBg,
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        // Quarter (Ч1, Ч2, Ч3, Ч4) + Semester (Нимсолаи 1 / 2) row matching photo 2
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isHomeroom) 6.dp else 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            for (qNum in 1..4) {
              val isQActive = qNum == activeQuarter
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(9.dp))
                  .background(
                    when {
                      isQActive -> Color(0xFF1D4ED8)
                      isDarkMode -> Color(0xFF334155)
                      else -> Color(0xFFF1F5F9)
                    }
                  )
                  .clickable { onQuarterSelected(qNum) }
                  .padding(horizontal = 11.dp, vertical = 6.dp)
                  .testTag("quarter_badge_$qNum"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${appStrings.quarterPrefix}$qNum",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = when {
                    isQActive -> Color.White
                    isDarkMode -> Color(0xFFE2E8F0)
                    else -> Color(0xFF1E293B)
                  }
                )
              }
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isDarkMode) Color(0xFF1E3A8A).copy(alpha = 0.45f) else Color(0xFFEFF6FF)
              )
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("semester_badge")
          ) {
            Text(
              text = if (activeSemester == 1) appStrings.semester1 else appStrings.semester2,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isDarkMode) Color(0xFF93C5FD) else Color(0xFF2563EB)
            )
          }
        }

        if (isHomeroom) {
          if (isSunday) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFDCFCE7))
                .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "🌴 Имрӯз рӯзи Якшанбе — рӯзи истироҳатӣ аст (дарс нест)",
                fontSize = (13 * fontScale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF14532D)
              )
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "$dayName • Соати $selectedPeriod:",
                fontSize = (13.5f * fontScale).sp,
                color = secondaryText,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = currentSubject,
                fontSize = (14f * fontScale).sp,
                color = if (isDarkMode) Gold500 else Navy800,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Horizontal Subject Badges
            if (todaySchedule.isNotEmpty()) {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 1.dp)
              ) {
                itemsIndexed(todaySchedule) { idx, subj ->
                  val periodNum = idx + 1
                  val isSelected = periodNum == selectedPeriod
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        when {
                          isSelected -> Navy800
                          isDarkMode -> Color(0xFF334155)
                          else -> Color(0xFFF1F5F9)
                        }
                      )
                      .border(
                        width = 1.dp,
                        color = if (isSelected) Gold500 else Color(0xFF94A3B8),
                        shape = RoundedCornerShape(8.dp)
                      )
                      .clickable { onPeriodSelected(periodNum) }
                      .padding(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = "$periodNum. $subj",
                      fontSize = (12f * fontScale).sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                      color = if (isSelected || isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                  }
                }
              }
            } else {
              Text(
                text = "Ҷадвали дарсҳо барои рӯзи $dayName ворид нашудааст",
                fontSize = (12f * fontScale).sp,
                color = secondaryText,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    // 2. Collapsible Search Bar (revealed via Search Icon in SchoolHeader)
    AnimatedVisibility(visible = isSearchVisible && students.isNotEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChanged,
          textStyle = TextStyle(
            color = Color(0xFF0F172A),
            fontSize = (13 * fontScale).sp,
            fontWeight = FontWeight.SemiBold
          ),
          placeholder = {
            Text(
              text = "Ҷустуҷӯи хонанда...",
              fontSize = (13 * fontScale).sp,
              color = Color(0xFF475569),
              fontWeight = FontWeight.Medium
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = Navy800
            )
          },
          trailingIcon = {
            IconButton(
              onClick = {
                if (searchQuery.isNotEmpty()) {
                  onSearchQueryChanged("")
                } else {
                  onCloseSearch()
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Тоза кардан",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF334155)
              )
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("student_search_field"),
          shape = RoundedCornerShape(10.dp),
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
      }
    }

    // 3. Students Attendance List
    if (students.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = secondaryText
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Дар ин синф хонандагон нестанд.",
            color = primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Ба бахши синфҳо гузаред ва хонандагонро илова кунед.",
            color = secondaryText,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onNavigateToClasses,
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Илова кардани хонандагон", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        itemsIndexed(filteredStudents, key = { _, item -> item.id }) { index, student ->
          val rawCode = currentPeriodAttendanceMap[student.id]
          val (parsedStatus, lateMins, statusTitle) = parseAttendanceCode(rawCode)
          val isSB = parsedStatus == AttendanceStatus.SB
          val isBSB = parsedStatus == AttendanceStatus.BSB
          val isBirthdayToday = student.isBirthdayOn(selectedDate)
          val contactPhone = student.parentPhone

          StudentAttendanceCard(
            index = index + 1,
            student = student,
            isSB = isSB,
            isBSB = isBSB,
            lateMinutes = lateMins,
            statusTitle = statusTitle,
            isBirthdayToday = isBirthdayToday,
            showContactActions = isHomeroom,
            isDarkMode = isDarkMode,
            fontScale = fontScale,
            onToggleSB = { onToggleAttendance(student.id, AttendanceStatus.SB) },
            onToggleBSB = { onToggleAttendance(student.id, AttendanceStatus.BSB) },
            onLateClick = {
              studentForLateDialog = student
              lateMinutesInput = if (lateMins > 0) lateMins.toString() else "15"
            },
            onCallClick = {
              if (contactPhone.isBlank()) {
                studentForPhoneEntry = student
                enteredPhoneText = "+992"
              } else {
                onDialParent(contactPhone)
              }
            },
            onSmsClick = {
              if (contactPhone.isBlank()) {
                studentForPhoneEntry = student
                enteredPhoneText = "+992"
              } else {
                val msg = buildDefaultMessageForStudent(student)
                editableMessageText = msg
                editableTemplateText = smsTemplate
                showTemplateEditor = false
                messageModalTarget = ParentMessageModalTarget(
                  student = student,
                  targetPhone = contactPhone,
                  isWhatsApp = false,
                  initialText = msg
                )
              }
            },
            onWhatsAppClick = {
              if (contactPhone.isBlank()) {
                studentForPhoneEntry = student
                enteredPhoneText = "+992"
              } else {
                val msg = buildDefaultMessageForStudent(student)
                editableMessageText = msg
                editableTemplateText = smsTemplate
                showTemplateEditor = false
                messageModalTarget = ParentMessageModalTarget(
                  student = student,
                  targetPhone = contactPhone,
                  isWhatsApp = true,
                  initialText = msg
                )
              }
            }
          )
        }
      }
    }
  }

  // Idea #1: Editable SMS / WhatsApp Message Dialog before sending
  messageModalTarget?.let { target ->
    AlertDialog(
      onDismissRequest = { messageModalTarget = null },
      containerColor = Color.White,
      title = {
        Column {
          Text(
            text = if (target.isWhatsApp) "Ирсол ба WhatsApp 🟢" else "Ирсоли Паём (SMS) 💬",
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = Navy900
          )
          Text(
            text = "${target.student.name} (${target.targetPhone})",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Матни паём барои волидон (метавонед таҳрир кунед):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = editableMessageText,
            onValueChange = { editableMessageText = it },
            textStyle = TextStyle(
              color = Color(0xFF0F172A),
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedContainerColor = Color(0xFFF8FAFC),
              unfocusedContainerColor = Color(0xFFF8FAFC),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Option to edit the default template for all students
          Text(
            text = if (showTemplateEditor) "▲ Пӯшидани қолаби умумӣ" else "⚙️ Иваз кардани қолаби доимии SMS",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Navy800,
            modifier = Modifier
              .clickable { showTemplateEditor = !showTemplateEditor }
              .padding(vertical = 4.dp)
          )

          AnimatedVisibility(visible = showTemplateEditor) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              Text(
                text = "Калидвожаҳо: [Номи хонанда], [Сана], [Номи фан], [Ҳолат]",
                fontSize = 11.sp,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = editableTemplateText,
                onValueChange = { editableTemplateText = it },
                textStyle = TextStyle(
                  color = Color(0xFF0F172A),
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color(0xFF0F172A),
                  unfocusedTextColor = Color(0xFF0F172A),
                  focusedContainerColor = Color(0xFFF8FAFC),
                  unfocusedContainerColor = Color(0xFFF8FAFC),
                  focusedBorderColor = Navy800,
                  unfocusedBorderColor = Color(0xFF94A3B8)
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = {
                  onSaveSmsTemplate(editableTemplateText)
                  showTemplateEditor = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Захираи қолаби нав", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (target.isWhatsApp) {
              onSendWhatsApp(target.targetPhone, editableMessageText)
            } else {
              onSendSms(target.targetPhone, editableMessageText)
            }
            messageModalTarget = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (target.isWhatsApp) Color(0xFF16A34A) else Navy800
          )
        ) {
          Text(
            text = if (target.isWhatsApp) "Ирсол ба WhatsApp" else "Ирсоли SMS",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { messageModalTarget = null }) {
          Text("Бекор кардан", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Dialog when tapping Call/SMS/WhatsApp for student with no phone number yet
  studentForPhoneEntry?.let { student ->
    AlertDialog(
      onDismissRequest = { studentForPhoneEntry = null },
      containerColor = Color.White,
      title = {
        Text(
          text = "Аввал рақами телефонро илова намоед 📞",
          fontWeight = FontWeight.Black,
          fontSize = 16.5.sp,
          color = Navy900
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Барои «${student.name}» рақами телефони волидонро ворид кунед:",
            fontSize = 13.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = enteredPhoneText,
            onValueChange = { enteredPhoneText = it },
            placeholder = { Text("+992...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedContainerColor = Color(0xFFF8FAFC),
              unfocusedContainerColor = Color(0xFFF8FAFC),
              focusedBorderColor = Navy800,
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (enteredPhoneText.isNotBlank()) {
              onSavePhone(student.id, enteredPhoneText.trim())
              studentForPhoneEntry = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Navy800),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Захира кардан 💾", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { studentForPhoneEntry = null }) {
          Text("Бекор кардан", color = Color(0xFF64748B))
        }
      }
    )
  }

  // Late Arrival (Дермонӣ) Dialog
  studentForLateDialog?.let { st ->
    val currentLate = parseAttendanceCode(currentPeriodAttendanceMap[st.id]).second
    AlertDialog(
      onDismissRequest = { studentForLateDialog = null },
      containerColor = Color.White,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "⏰", fontSize = 22.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Дермонии хонанда",
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              color = Navy900
            )
            Text(
              text = st.name,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF475569)
            )
          }
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Чанд дақиқа ба дарс дер монд?",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155)
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Quick selection chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(5, 10, 15, 20, 30).forEach { mins ->
              val isSel = lateMinutesInput == mins.toString()
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) Color(0xFFF59E0B) else Color(0xFFF1F5F9))
                  .border(
                    width = 1.dp,
                    color = if (isSel) Color(0xFFD97706) else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { lateMinutesInput = mins.toString() }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "$mins",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isSel) Color.White else Navy900
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = lateMinutesInput,
            onValueChange = { lateMinutesInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Дақиқаи дермонӣ") },
            trailingIcon = {
              Text(
                "дақиқа",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 12.sp,
                color = Color.Gray
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0F172A),
              unfocusedTextColor = Color(0xFF0F172A),
              focusedContainerColor = Color(0xFFF8FAFC),
              unfocusedContainerColor = Color(0xFFF8FAFC),
              focusedBorderColor = Color(0xFFF59E0B),
              unfocusedBorderColor = Color(0xFF94A3B8)
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val mins = lateMinutesInput.toIntOrNull() ?: 15
            onRecordLate(st.id, mins)
            studentForLateDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Сабт кардан ⏰", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Row {
          if (currentLate > 0) {
            TextButton(
              onClick = {
                onRecordLate(st.id, 0)
                studentForLateDialog = null
              }
            ) {
              Text("Тоза (Ҳозир)", color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
            }
          }
          TextButton(onClick = { studentForLateDialog = null }) {
            Text("Бекор", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
          }
        }
      }
    )
  }
}

@Composable
private fun StatBadge(label: String, color: Color, bgColor: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.ExtraBold,
      color = color
    )
  }
}

@Composable
private fun StudentAttendanceCard(
  index: Int,
  student: Student,
  isSB: Boolean,
  isBSB: Boolean,
  lateMinutes: Int = 0,
  statusTitle: String = "Ҳозир",
  isBirthdayToday: Boolean,
  showContactActions: Boolean = true,
  isDarkMode: Boolean,
  fontScale: Float,
  onToggleSB: () -> Unit,
  onToggleBSB: () -> Unit,
  onLateClick: () -> Unit,
  onCallClick: () -> Unit,
  onSmsClick: () -> Unit,
  onWhatsAppClick: () -> Unit
) {
  val cardBg by animateColorAsState(
    targetValue = when {
      isSB -> Color(0xFFDCFCE7)
      isBSB -> Color(0xFFFEE2E2)
      lateMinutes > 0 -> if (isDarkMode) Color(0xFF2E2415) else Color(0xFFFEF3C7)
      isDarkMode -> Color(0xFF1E293B)
      else -> Color.White
    },
    animationSpec = tween(durationMillis = 90),
    label = "cardBg"
  )

  val nameColor = when {
    isSB -> Color(0xFF14532D)
    isBSB -> Color(0xFF7F1D1D)
    lateMinutes > 0 -> Color(0xFFB45309)
    isDarkMode -> Color(0xFFF8FAFC)
    else -> Color(0xFF0F172A)
  }

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("student_card_${student.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Index & Name + Contact Icons tucked right underneath
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(
                when {
                  isSB -> StatusGreen
                  isBSB -> StatusRed
                  lateMinutes > 0 -> Color(0xFFF59E0B)
                  else -> Color(0xFFE2E8F0)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$index",
              fontSize = (12.5f * fontScale).sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isSB || isBSB || lateMinutes > 0) Color.White else Color(0xFF1E293B)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = student.name,
              fontSize = (14.5f * fontScale).sp,
              fontWeight = FontWeight.Bold,
              color = nameColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // Only show status text when SB, BSB, Late, or Birthday ("Ҳозир" removed as requested)
            if (isSB || isBSB || lateMinutes > 0 || isBirthdayToday) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val statusText = when {
                  isSB -> "СБ • Сабабнок"
                  isBSB -> "БСБ • Бе сабаб"
                  lateMinutes > 0 -> "⏰ Дермонӣ: $lateMinutes дақ"
                  else -> ""
                }
                val statusColor = when {
                  isSB -> Color(0xFF15803D)
                  isBSB -> Color(0xFFB91C1C)
                  else -> Color(0xFFD97706)
                }
                if (statusText.isNotEmpty()) {
                  Text(
                    text = statusText,
                    fontSize = (11f * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                  )
                }

                if (isBirthdayToday) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFFFEF3C7))
                      .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = "🎂 Зодрӯз муборак!",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFFB45309)
                    )
                  }
                }
              }
            }

            // Contact icons moved up directly below the student's name so the card stays compact
            if (showContactActions) {
              Spacer(modifier = Modifier.height(3.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                // 1. Phone Call Icon Button
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .shadow(1.5.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                      Brush.linearGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                      )
                    )
                    .clickable(onClick = onCallClick)
                    .testTag("call_btn_${student.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Занг ба волидон",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                  )
                }

                // 2. SMS Icon Button
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .shadow(1.5.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                      Brush.linearGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                      )
                    )
                    .clickable(onClick = onSmsClick)
                    .testTag("sms_btn_${student.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = "Паём (SMS) ба волидон",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                  )
                }

                // 3. WhatsApp Icon Button
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .shadow(1.5.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                      Brush.linearGradient(
                        colors = listOf(Color(0xFF25D366), Color(0xFF128C7E))
                      )
                    )
                    .clickable(onClick = onWhatsAppClick)
                    .testTag("whatsapp_btn_${student.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  WhatsAppAuthenticIcon(modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Column containing SB/BSB row and elongated Late button underneath
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp),
          modifier = Modifier.width(IntrinsicSize.Min)
        ) {
          // Buttons SB and BSB
          Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // SB Button
            Box(
              modifier = Modifier
                .width(46.dp)
                .height(31.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (isSB) StatusGreen else Color.White)
                .border(
                  width = 1.6.dp,
                  color = StatusGreen,
                  shape = RoundedCornerShape(7.dp)
                )
                .clickable(onClick = onToggleSB)
                .testTag("btn_sb_${student.id}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "СБ",
                color = if (isSB) Color.White else StatusGreen,
                fontSize = (12.5f * fontScale).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false
              )
            }

            // BSB Button
            Box(
              modifier = Modifier
                .width(46.dp)
                .height(31.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (isBSB) StatusRed else Color.White)
                .border(
                  width = 1.6.dp,
                  color = StatusRed,
                  shape = RoundedCornerShape(7.dp)
                )
                .clickable(onClick = onToggleBSB)
                .testTag("btn_bsb_${student.id}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "БСБ",
                color = if (isBSB) Color.White else StatusRed,
                fontSize = (12.5f * fontScale).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          // Elongated Late button underneath SB & BSB
          val isLate = lateMinutes > 0
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(
                if (isLate) Color(0xFFF59E0B)
                else (if (isDarkMode) Color(0xFF334155) else Color(0xFFFEF3C7))
              )
              .border(
                width = 1.1.dp,
                color = if (isLate) Color(0xFFD97706) else Color(0xFFF59E0B),
                shape = RoundedCornerShape(6.dp)
              )
              .clickable(onClick = onLateClick)
              .padding(vertical = 3.dp, horizontal = 4.dp)
              .testTag("btn_late_${student.id}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isLate) "⏰ $lateMinutes дақ" else "⏰ Дермонӣ",
              color = if (isLate) Color.White else Color(0xFFB45309),
              fontSize = (10.5f * fontScale).sp,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WhatsAppAuthenticIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val center = Offset(w * 0.52f, h * 0.48f)
    val radius = w * 0.38f

    // Speech bubble ring
    drawCircle(
      color = Color.White,
      radius = radius,
      center = center,
      style = Stroke(width = w * 0.095f)
    )

    // Bottom-left speech tail
    val tailPath = Path().apply {
      moveTo(w * 0.22f, h * 0.68f)
      lineTo(w * 0.12f, h * 0.88f)
      lineTo(w * 0.36f, h * 0.78f)
      close()
    }
    drawPath(path = tailPath, color = Color.White)

    // Classic curved telephone handset inside the bubble
    val phonePath = Path().apply {
      moveTo(w * 0.40f, h * 0.34f)
      cubicTo(
        w * 0.35f, h * 0.42f,
        w * 0.42f, h * 0.56f,
        w * 0.54f, h * 0.61f
      )
      cubicTo(
        w * 0.60f, h * 0.63f,
        w * 0.66f, h * 0.60f,
        w * 0.68f, h * 0.55f
      )
    }
    drawPath(
      path = phonePath,
      color = Color.White,
      style = Stroke(
        width = w * 0.11f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )
  }
}

