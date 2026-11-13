package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900

@Composable
fun PeriodPickerDialog(
  todaySchedule: List<String>,
  selectedPeriod: Int,
  onDismiss: () -> Unit,
  onPeriodSelected: (Int) -> Unit
) {
  val periodsCount = if (todaySchedule.isNotEmpty()) todaySchedule.size else 6

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("period_picker_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = Navy800,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Интихоби соати дарсӣ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(periodsCount) { idx ->
            val pNum = idx + 1
            val isSelected = pNum == selectedPeriod
            val subject = todaySchedule.getOrNull(idx) ?: "Дарс муайян нест"

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                .clickable {
                  onPeriodSelected(pNum)
                  onDismiss()
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Navy800 else Color(0xFFCBD5E1)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$pNum",
                    color = if (isSelected) Color.White else Navy900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = "$pNum-юм дарс",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Navy800 else Navy900
                  )
                  Text(
                    text = subject,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                  )
                }
              }

              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Navy800,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Пӯшидан", color = Color(0xFF64748B), fontSize = 13.sp)
          }
        }
      }
    }
  }
}
