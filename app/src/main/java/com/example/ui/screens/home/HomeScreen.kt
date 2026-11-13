package com.example.ui.screens.home

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyInspiration
import com.example.data.model.PicnicData
import com.example.data.model.TajikDateUtils
import com.example.data.model.TeacherLesson
import com.example.data.model.TodoItem
import com.example.ui.components.TajikCalendarDialog
import com.example.ui.theme.Gold500
import com.example.ui.theme.Green600
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Red600

@Composable
fun HomeScreen(
  dailyInspiration: DailyInspiration,
  todoItems: List<TodoItem>,
  picnicData: PicnicData,
  selectedDate: String,
  dayOfWeekIndex: Int,
  selectedPeriod: Int,
  isCurrentPeriodConfirmed: Boolean,
  hasStudents: Boolean,
  isDarkMode: Boolean,
  teacherSchedule: List<TeacherLesson> = emptyList(),
  onOpenScheduleSettings: () -> Unit = {},
  onConfirmPeriodAttendance: () -> Unit,
  onAddTodo: (String) -> Unit,
  onToggleTodo: (String) -> Unit,
  onDeleteTodo: (String) -> Unit,
  onUpdatePicnicDate: (String) -> Unit,
  onUpdatePicnicNotes: (String) -> Unit,
  onAddPicnicItem: (String) -> Unit,
  onTogglePicnicItem: (String) -> Unit,
  onDeletePicnicItem: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)
  val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
  val isSunday = dayOfWeekIndex == 0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .testTag("home_screen_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 0. Огоҳкунии тасдиқи давомот (ё Рӯзи истироҳатии Якшанбе) дар саҳифаи Асосӣ
    if (isSunday) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF064E3B) else Color(0xFFDCFCE7))
            .border(1.dp, Green600, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("sunday_rest_banner"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = if (isDarkMode) Gold500 else Green600,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "🌴 Имрӯз рӯзи Якшанбе — рӯзи истироҳатӣ аст!",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDarkMode) Color.White else Color(0xFF14532D)
          )
        }
      }
    } else if (!isCurrentPeriodConfirmed && hasStudents) {
      item {
        AnimatedVisibility(visible = !isCurrentPeriodConfirmed) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFFEF3C7))
              .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
              .clickable(onClick = onConfirmPeriodAttendance)
              .padding(horizontal = 12.dp, vertical = 9.dp)
              .testTag("confirm_period_banner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Давомоти ин соатро тасдиқ кунед",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF78350F)
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFD97706))
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = "✅ Тасдиқ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // 0.5. ҶАДВАЛИ ДАРСИИ ИМРӮЗАИ МАН (ба монанди EduPage)
    item {
      TeacherTodayScheduleCard(
        teacherSchedule = teacherSchedule,
        selectedDate = selectedDate,
        dayOfWeekIndex = dayOfWeekIndex,
        selectedPeriod = selectedPeriod,
        isDarkMode = isDarkMode,
        onOpenScheduleSettings = onOpenScheduleSettings
      )
    }

    // 1. СУХАНИ РӮЗ (Quote of the Day - Clean without "AI Автоматӣ" badge)
    item {
      QuoteOfTheDayCard(
        quoteText = dailyInspiration.quoteText,
        quoteAuthor = dailyInspiration.quoteAuthor,
        isDarkMode = isDarkMode
      )
    }

    // 2. ШЕЪРИ РӮЗ (Poem of the Day - Clean without "Адабиёти форсу тоҷик" badge)
    item {
      PoemOfTheDayCard(
        poemLines = dailyInspiration.poemLines,
        poemAuthor = dailyInspiration.poemAuthor,
        isDarkMode = isDarkMode
      )
    }

    // 3. TO-DO ЛИСТ (Рӯйхати корҳо)
    item {
      TodoListWindowCard(
        todoItems = todoItems,
        isDarkMode = isDarkMode,
        cardBg = cardBg,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        onAddTodo = onAddTodo,
        onToggleTodo = onToggleTodo,
        onDeleteTodo = onDeleteTodo
      )
    }

    // 4. РАВЗАНАИ ПИКНИК (Picnic Planner Window)
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

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun QuoteOfTheDayCard(
  quoteText: String,
  quoteAuthor: String,
  isDarkMode: Boolean
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("quote_of_the_day_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.linearGradient(
            colors = if (isDarkMode) {
              listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
            } else {
              listOf(Navy900, Color(0xFF1E40AF))
            }
          )
        )
        .padding(16.dp)
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Gold500.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FormatQuote,
              contentDescription = null,
              tint = Gold500,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Сухани рӯз",
            color = Gold500,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "«$quoteText»",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "— $quoteAuthor",
          color = Gold500,
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}

@Composable
fun PoemOfTheDayCard(
  poemLines: String,
  poemAuthor: String,
  isDarkMode: Boolean
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFFFFBEB)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = if (isDarkMode) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFFDE68A),
        shape = RoundedCornerShape(18.dp)
      )
      .testTag("poem_of_the_day_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFFD97706).copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = if (isDarkMode) Gold500 else Color(0xFFB45309),
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Шеъри рӯз",
          color = if (isDarkMode) Gold500 else Color(0xFF92400E),
          fontSize = 15.sp,
          fontWeight = FontWeight.Black
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = poemLines,
        color = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF451A03),
        fontSize = 15.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        lineHeight = 23.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "✍️ $poemAuthor",
        color = if (isDarkMode) Gold500 else Color(0xFFB45309),
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
}

@Composable
fun TodoListWindowCard(
  todoItems: List<TodoItem>,
  isDarkMode: Boolean,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  onAddTodo: (String) -> Unit,
  onToggleTodo: (String) -> Unit,
  onDeleteTodo: (String) -> Unit
) {
  var newTodoText by remember { mutableStateOf("") }
  val completedCount = todoItems.count { it.isDone }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("todo_list_window_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF2563EB).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Checklist,
              contentDescription = null,
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "To-Do Лист (Рӯйхати корҳо)",
              color = textPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "Иҷро шуд: $completedCount аз ${todoItems.size}",
              color = textSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Add new task input
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = newTodoText,
          onValueChange = { newTodoText = it },
          placeholder = { Text("Кори навро нависед...", fontSize = 13.sp) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("todo_input_field")
        )

        Button(
          onClick = {
            if (newTodoText.isNotBlank()) {
              onAddTodo(newTodoText)
              newTodoText = ""
            }
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Navy800),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
          modifier = Modifier.testTag("todo_add_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Илова", tint = Color.White)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Илова", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (todoItems.isEmpty()) {
        Text(
          text = "Рӯйхати корҳо холист. Кори навро дар боло илова кунед!",
          color = textSecondary,
          fontSize = 13.sp,
          modifier = Modifier.padding(vertical = 8.dp)
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          todoItems.forEach { item ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (item.isDone) {
                if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.28f) else Color(0xFFF0FDF4)
              } else {
                if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
              },
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleTodo(item.id) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                  contentDescription = null,
                  tint = if (item.isDone) Green600 else textSecondary,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = item.title,
                  color = if (item.isDone) textSecondary else textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = { onDeleteTodo(item.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Нест кардан",
                    tint = Red600.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PicnicPlannerWindowCard(
  picnicData: PicnicData,
  defaultCalendarDate: String,
  isDarkMode: Boolean,
  cardBg: Color,
  textPrimary: Color,
  textSecondary: Color,
  onUpdatePicnicDate: (String) -> Unit,
  onUpdatePicnicNotes: (String) -> Unit,
  onAddPicnicItem: (String) -> Unit,
  onTogglePicnicItem: (String) -> Unit,
  onDeletePicnicItem: (String) -> Unit
) {
  var showPicnicDatePicker by remember { mutableStateOf(false) }
  var newPlanItemText by remember { mutableStateOf("") }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("picnic_window_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Green600.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Park,
              contentDescription = null,
              tint = Green600,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "🏕️ Пикник (Сайругашт)",
              color = textPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "Интихоби рӯзи пикник ва нақшаи чорабиниҳо",
              color = textSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Picnic Date Selector Button
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF0FDF4),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Green600.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          .clip(RoundedCornerShape(12.dp))
          .clickable { showPicnicDatePicker = true }
          .testTag("picnic_date_selector")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = Green600,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Рӯзи баргузории пикник:",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (picnicData.dateStr.isNotBlank()) {
                  TajikDateUtils.formatDateDisplay(picnicData.dateStr)
                } else {
                  "Интихоб нашудааст (Пахш кунед)"
                },
                color = if (picnicData.dateStr.isNotBlank()) Green600 else textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
              )
            }
          }

          Text(
            text = "Тағйир",
            color = Green600,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // What we will do / location notes
      OutlinedTextField(
        value = picnicData.locationAndNotes,
        onValueChange = onUpdatePicnicNotes,
        label = { Text("Дар пикник чӣ мекунем ва ба куҷо меравем?") },
        placeholder = { Text("Макон, бозиҳо ва барномаи рӯзи пикник...") },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("picnic_notes_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Чӣ ба нақша мегирем (Рӯйхат):",
        color = textPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Add Picnic checklist item
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = newPlanItemText,
          onValueChange = { newPlanItemText = it },
          placeholder = { Text("Нақша ё лавозимоти пикник...", fontSize = 13.sp) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("picnic_item_input")
        )

        Button(
          onClick = {
            if (newPlanItemText.isNotBlank()) {
              onAddPicnicItem(newPlanItemText)
              newPlanItemText = ""
            }
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Green600),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
          modifier = Modifier.testTag("picnic_add_item_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Илова", tint = Color.White)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Илова", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (picnicData.items.isEmpty()) {
        Text(
          text = "Нақшаҳои пикник ҳоло ворид нашудаанд.",
          color = textSecondary,
          fontSize = 13.sp,
          modifier = Modifier.padding(vertical = 4.dp)
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          picnicData.items.forEach { item ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (item.isDone) {
                if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.28f) else Color(0xFFF0FDF4)
              } else {
                if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
              },
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTogglePicnicItem(item.id) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                  contentDescription = null,
                  tint = if (item.isDone) Green600 else textSecondary,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = item.title,
                  color = if (item.isDone) textSecondary else textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = { onDeletePicnicItem(item.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Нест кардан",
                    tint = Red600.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showPicnicDatePicker) {
    TajikCalendarDialog(
      currentDateString = picnicData.dateStr.ifBlank { defaultCalendarDate },
      onDismiss = { showPicnicDatePicker = false },
      onDateSelected = { pickedDate ->
        onUpdatePicnicDate(pickedDate)
        showPicnicDatePicker = false
      }
    )
  }
}

@Composable
fun TeacherTodayScheduleCard(
  teacherSchedule: List<TeacherLesson>,
  selectedDate: String,
  dayOfWeekIndex: Int,
  selectedPeriod: Int,
  isDarkMode: Boolean,
  onOpenScheduleSettings: () -> Unit
) {
  // Live ticker every second for EduPage 45:00 lesson / 10:00 break countdown and after-school status
  var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) {
    while (true) {
      currentTimeMillis = System.currentTimeMillis()
      delay(1000L)
    }
  }

  val todayDayMonthStr = remember(selectedDate) {
    val parts = selectedDate.split("-")
    if (parts.size == 3) "${parts[2]}.${parts[1]}." else selectedDate
  }

  val tomorrowInfo = remember(selectedDate, dayOfWeekIndex) {
    val parts = selectedDate.split("-")
    val cal = Calendar.getInstance()
    if (parts.size == 3) {
      val y = parts[0].toIntOrNull()
      val m = parts[1].toIntOrNull()
      val d = parts[2].toIntOrNull()
      if (y != null && m != null && d != null) {
        cal.set(y, m - 1, d)
      }
    }
    cal.add(Calendar.DAY_OF_YEAR, 1)
    // If tomorrow is Sunday, show tomorrow's date or if Sunday has no lessons, check dayOfWeek
    val dd = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val mm = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val nextDayIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> 1
      Calendar.TUESDAY -> 2
      Calendar.WEDNESDAY -> 3
      Calendar.THURSDAY -> 4
      Calendar.FRIDAY -> 5
      Calendar.SATURDAY -> 6
      else -> 1 // If Sunday, show Monday's lessons so the teacher sees their next teaching day
    }
    Pair("$dd.$mm.", nextDayIdx)
  }

  // Real-time schedule state synced with exact lesson, break & lunch intervals (08:00 – 15:10)
  val scheduleState = remember(currentTimeMillis, dayOfWeekIndex, todayDayMonthStr, tomorrowInfo) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)
    val totalSec = hour * 3600 + minute * 60 + second

    val morningGreetingSec = 6 * 3600 // 06:00 (2 hours before 08:00)
    val startOfSchoolSec = 8 * 3600   // 08:00:00
    val endOfSchoolSec = (15 * 60 + 10) * 60 // 15:10:00 (after 7th lesson)
    val isSundayToday = dayOfWeekIndex == 0

    if (!isSundayToday && totalSec in startOfSchoolSec until endOfSchoolSec) {
      val (topLabel, endBoundarySec, highlightPeriod) = when {
        totalSec < (8 * 60 + 45) * 60 -> Triple("Дарси 1", (8 * 60 + 45) * 60, 1)
        totalSec < (8 * 60 + 55) * 60 -> Triple("Танаффус", (8 * 60 + 55) * 60, 2)
        totalSec < (9 * 60 + 40) * 60 -> Triple("Дарси 2", (9 * 60 + 40) * 60, 2)
        totalSec < (10 * 60 + 0) * 60 -> Triple("Танаффус", (10 * 60 + 0) * 60, 3)
        totalSec < (10 * 60 + 45) * 60 -> Triple("Дарси 3", (10 * 60 + 45) * 60, 3)
        totalSec < (10 * 60 + 55) * 60 -> Triple("Танаффус", (10 * 60 + 55) * 60, 4)
        totalSec < (11 * 60 + 40) * 60 -> Triple("Дарси 4", (11 * 60 + 40) * 60, 4)
        totalSec < (11 * 60 + 50) * 60 -> Triple("Танаффус", (11 * 60 + 50) * 60, 5)
        totalSec < (12 * 60 + 35) * 60 -> Triple("Дарси 5", (12 * 60 + 35) * 60, 5)
        totalSec < (13 * 60 + 30) * 60 -> Triple("Хӯроки нисфирӯзӣ", (13 * 60 + 30) * 60, 6)
        totalSec < (14 * 60 + 15) * 60 -> Triple("Дарси 6", (14 * 60 + 15) * 60, 6)
        totalSec < (14 * 60 + 25) * 60 -> Triple("Танаффус", (14 * 60 + 25) * 60, 7)
        else -> Triple("Дарси 7", endOfSchoolSec, 7)
      }

      val remSeconds = (endBoundarySec - totalSec).coerceAtLeast(0)
      val mm = (remSeconds / 60).toString().padStart(2, '0')
      val ss = (remSeconds % 60).toString().padStart(2, '0')

      EduPageWidgetState(
        isDuringLessons = true,
        topTitle = topLabel,
        topValue = "$mm:$ss",
        timetableHeader = "Ҷадвали дарсҳо имрӯз $todayDayMonthStr",
        targetDayOfWeek = dayOfWeekIndex,
        highlightedPeriod = highlightPeriod
      )
    } else if (!isSundayToday && totalSec < startOfSchoolSec) {
      val remSec = startOfSchoolSec - totalSec
      val remMin = ((remSec + 59) / 60).coerceAtLeast(1)
      val timeUntilStr = if (remMin >= 60) {
        val hrs = remMin / 60
        val mins = remMin % 60
        if (mins == 0) "$hrs соат" else "$hrs соат $mins дақ."
      } else {
        "$remMin дақиқа"
      }
      val morningTitle = if (totalSec >= morningGreetingSec) {
        "Субҳ ба хайр. Занг баъди:"
      } else {
        "Акнун ба хона. Занг баъди:"
      }
      EduPageWidgetState(
        isDuringLessons = false,
        topTitle = morningTitle,
        topValue = timeUntilStr,
        timetableHeader = "Ҷадвали дарсҳо имрӯз $todayDayMonthStr",
        targetDayOfWeek = dayOfWeekIndex,
        highlightedPeriod = 1
      )
    } else {
      // AFTER 7th LESSON (>= 15:10) OR ON SUNDAY:
      val extraSundaySec = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) 24 * 3600 else 0
      val remSecToday = (24 * 3600 - totalSec).coerceAtLeast(0)
      val remSecToNext8am = remSecToday + startOfSchoolSec + extraSundaySec
      val hoursUntilBell = ((remSecToNext8am + 1800) / 3600).coerceAtLeast(1)

      EduPageWidgetState(
        isDuringLessons = false,
        topTitle = "Акнун ба хона. Занг баъди:",
        topValue = "$hoursUntilBell соат",
        timetableHeader = "Ҷадвали дарсҳо пагоҳ ${tomorrowInfo.first}",
        targetDayOfWeek = tomorrowInfo.second,
        highlightedPeriod = 1
      )
    }
  }

  val displayedLessons = remember(teacherSchedule, scheduleState.targetDayOfWeek) {
    teacherSchedule.filter { it.dayOfWeek == scheduleState.targetDayOfWeek }.sortedBy { it.period }
  }

  val eduPageBg = Color(0xFF152232)
  val cellDefaultBg = Color(0xFF213043)
  val cellActiveBg = Color(0xFF6B7861)
  val cellBorderColor = Color(0xFF2D3F56)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onOpenScheduleSettings() }
      .testTag("teacher_today_schedule_card"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = eduPageBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
      // 1. Top Row:
      // - During school hours: Clock Icon on left + "Дарси X" / "Танаффус" + live MM:SS countdown (45:00 / 10:00)
      // - After 7th lesson: No Clock Icon; Right-aligned "Акнун ба хона. Занг баъди:" and "15 соат"
      if (scheduleState.isDuringLessons) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = scheduleState.topTitle,
              fontSize = 11.5.sp,
              lineHeight = 12.sp,
              color = Color(0xFFCBD5E1),
              fontWeight = FontWeight.Medium
            )
            Text(
              text = scheduleState.topValue,
              fontSize = 19.sp,
              lineHeight = 20.sp,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = scheduleState.topTitle,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End
          )
          Text(
            text = scheduleState.topValue,
            fontSize = 15.5.sp,
            lineHeight = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      // 2. Middle Row: "Ҷадвали дарсҳо имрӯз DD.MM." or "Ҷадвали дарсҳо пагоҳ DD.MM."
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        if (scheduleState.isDuringLessons) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(19.dp)
          )
          Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
          text = scheduleState.timetableHeader,
          fontSize = 14.sp,
          color = Color.White,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Bottom Row: All 7 lessons side-by-side on screen (EduPage style)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(4.dp))
          .border(0.8.dp, cellBorderColor, RoundedCornerShape(4.dp))
      ) {
        for (periodNum in 1..7) {
          val lesson = displayedLessons.find { it.period == periodNum }
          val isFree = lesson?.isFreePeriod == true
          val hasLesson = lesson != null && !isFree
          val isActivePeriod = periodNum == scheduleState.highlightedPeriod

          val shortSubject = remember(lesson?.subject, lesson?.shortSubject) {
            val explicitShort = lesson?.shortSubject?.trim().orEmpty()
            if (explicitShort.isNotEmpty()) {
              explicitShort
            } else {
              val s = lesson?.subject?.trim() ?: ""
              when {
                s.equals("Алгебра", ignoreCase = true) || s.equals("А", ignoreCase = true) -> "А"
                s.equals("Геометрия", ignoreCase = true) || s.equals("Г", ignoreCase = true) -> "Г"
                s.contains("тарбияв", ignoreCase = true) || s.equals("С.Т", ignoreCase = true) -> "С.Т"
                s.isNotEmpty() -> s.take(3).uppercase()
                else -> ""
              }
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(58.dp)
              .background(if (isActivePeriod) cellActiveBg else cellDefaultBg)
              .border(0.4.dp, cellBorderColor)
              .clickable { onOpenScheduleSettings() }
              .padding(vertical = 3.dp, horizontal = 2.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              // Top: Period number (1. .. 7.)
              Text(
                text = "$periodNum.",
                fontSize = 10.sp,
                lineHeight = 11.sp,
                color = Color(0xFFCBD5E1),
                fontWeight = FontWeight.Medium
              )

              // Middle: Class name (e.g., 10Б) or Free period icon (☕)
              when {
                isFree -> {
                  Text(
                    text = "☕",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                  )
                }
                hasLesson -> {
                  Text(
                    text = lesson?.className ?: "",
                    fontSize = 13.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                  )
                }
                else -> {
                  Text(
                    text = "",
                    fontSize = 12.sp
                  )
                }
              }

              // Bottom: Shortened Subject (А, Г, С.Т)
              Text(
                text = if (hasLesson) shortSubject else "",
                fontSize = 10.5.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE2E8F0),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

private data class EduPageWidgetState(
  val isDuringLessons: Boolean,
  val topTitle: String,
  val topValue: String,
  val timetableHeader: String,
  val targetDayOfWeek: Int,
  val highlightedPeriod: Int
)
