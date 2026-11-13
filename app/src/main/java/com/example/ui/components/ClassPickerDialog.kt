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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.SchoolClass
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900

@Composable
fun ClassPickerDialog(
  classes: List<SchoolClass>,
  selectedClassId: String,
  onDismiss: () -> Unit,
  onClassSelected: (String) -> Unit,
  onManageClassesClick: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("class_picker_dialog")
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
            imageVector = Icons.Default.Class,
            contentDescription = null,
            tint = Navy800,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Интихоби синф",
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
          items(classes) { cls ->
            val isSelected = cls.id == selectedClassId
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                .clickable {
                  onClassSelected(cls.id)
                  onDismiss()
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "Синфи ${cls.name}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Navy800 else Navy900
                )
                Text(
                  text = "${cls.students.size} хонанда",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Navy800),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = {
              onDismiss()
              onManageClassesClick()
            }
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = Navy800
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Танзими синфҳо",
              color = Navy800,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          TextButton(onClick = onDismiss) {
            Text("Пӯшидан", color = Color(0xFF64748B), fontSize = 13.sp)
          }
        }
      }
    }
  }
}
