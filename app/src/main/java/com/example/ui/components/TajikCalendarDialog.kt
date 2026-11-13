package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TajikDateUtils
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TajikCalendarDialog(
  currentDateString: String,
  onDismiss: () -> Unit,
  onDateSelected: (String) -> Unit
) {
  val cal = remember {
    Calendar.getInstance().apply {
      try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(currentDateString)
        if (parsed != null) time = parsed
      } catch (e: Exception) {
        time = Date()
      }
    }
  }

  var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) } // 0..11
  val selectedDay = cal.get(Calendar.DAY_OF_MONTH)

  // Compute calendar days
  val calHelper = Calendar.getInstance().apply {
    set(Calendar.YEAR, selectedYear)
    set(Calendar.MONTH, selectedMonth)
    set(Calendar.DAY_OF_MONTH, 1)
  }
  val daysInMonth = calHelper.getActualMaximum(Calendar.DAY_OF_MONTH)
  // 1=Sunday, 2=Monday...
  val firstDayOfWeek = calHelper.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon...

  val todayCal = Calendar.getInstance()
  val todayYear = todayCal.get(Calendar.YEAR)
  val todayMonth = todayCal.get(Calendar.MONTH)
  val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tajik_calendar_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Dialog Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Navy800)
            .padding(18.dp)
        ) {
          Column {
            Text(
              text = "$selectedYear",
              color = Color(0xFFE2E8F0),
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            val mName = TajikDateUtils.MONTHS.getOrElse(selectedMonth) { "" }
            Text(
              text = "$mName, $selectedYear",
              color = Color.White,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Month Navigation
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = {
              if (selectedMonth == 0) {
                selectedMonth = 11
                selectedYear -= 1
              } else {
                selectedMonth -= 1
              }
            }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Моҳи пешина",
              tint = Navy900
            )
          }

          Text(
            text = "${TajikDateUtils.MONTHS.getOrElse(selectedMonth) { "" }} $selectedYear",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
          )

          IconButton(
            onClick = {
              if (selectedMonth == 11) {
                selectedMonth = 0
                selectedYear += 1
              } else {
                selectedMonth += 1
              }
            }
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Моҳи оянда",
              tint = Navy900
            )
          }
        }

        // Weekday Headers: Я, Д, С, Ч, П, Ҷ, Ш
        val weekdayHeaders = listOf("Я", "Д", "С", "Ч", "П", "Ҷ", "Ш")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          weekdayHeaders.forEach { header ->
            Text(
              text = header,
              modifier = Modifier.width(36.dp),
              textAlign = TextAlign.Center,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days Grid: 7 columns
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
        ) {
          for (r in 0 until rows) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              for (c in 0 until 7) {
                val cellIndex = r * 7 + c
                val dayNum = cellIndex - firstDayOfWeek + 1
                if (dayNum in 1..daysInMonth) {
                  val isSelected = (selectedYear == cal.get(Calendar.YEAR) &&
                      selectedMonth == cal.get(Calendar.MONTH) &&
                      dayNum == selectedDay)
                  val isToday = (selectedYear == todayYear &&
                      selectedMonth == todayMonth &&
                      dayNum == todayDay)

                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(
                        when {
                          isSelected -> Navy800
                          isToday -> Color(0xFFE2E8F0)
                          else -> Color.Transparent
                        }
                      )
                      .clickable {
                        val mm = String.format(Locale.US, "%02d", selectedMonth + 1)
                        val dd = String.format(Locale.US, "%02d", dayNum)
                        onDateSelected("$selectedYear-$mm-$dd")
                        onDismiss()
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$dayNum",
                      fontSize = 13.sp,
                      fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                      color = when {
                        isSelected -> Color.White
                        isToday -> Navy800
                        else -> Navy900
                      }
                    )
                  }
                } else {
                  Spacer(modifier = Modifier.size(36.dp))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Actions
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = {
              val mm = String.format(Locale.US, "%02d", todayMonth + 1)
              val dd = String.format(Locale.US, "%02d", todayDay)
              onDateSelected("$todayYear-$mm-$dd")
              onDismiss()
            }
          ) {
            Text(
              text = "Имрӯз",
              color = Navy800,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          TextButton(onClick = onDismiss) {
            Text(
              text = "Бекор кардан",
              color = Color(0xFF64748B),
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }
}
