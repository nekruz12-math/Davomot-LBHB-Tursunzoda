package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BellCountdownState
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import java.io.File

@Composable
fun SchoolLogoImage(
  schoolLogoPath: String,
  size: Dp,
  borderWidth: Dp = 2.dp,
  borderColor: Color = Gold500,
  modifier: Modifier = Modifier
) {
  val customBitmap = remember(schoolLogoPath) {
    if (schoolLogoPath.isNotBlank()) {
      try {
        val file = File(schoolLogoPath)
        if (file.exists()) {
          BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else null
      } catch (e: Exception) {
        null
      }
    } else null
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .border(borderWidth, borderColor, CircleShape)
      .background(Color.White),
    contentAlignment = Alignment.Center
  ) {
    if (customBitmap != null) {
      Image(
        bitmap = customBitmap,
        contentDescription = "Логотипи муассиса",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size)
          .clip(CircleShape)
      )
    } else {
      Image(
        painter = painterResource(id = R.drawable.school_logo),
        contentDescription = "Логотипи ЛБХБ Турсунзода",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size)
          .clip(CircleShape)
      )
    }
  }
}

@Composable
fun SchoolHeader(
  schoolName: String,
  schoolLogoPath: String,
  className: String,
  formattedDate: String,
  periodNumber: Int,
  bellCountdown: BellCountdownState,
  isDarkMode: Boolean,
  showSelectors: Boolean = true,
  showSearchIcon: Boolean = false,
  onSearchClick: () -> Unit = {},
  onClassClick: () -> Unit,
  onDateClick: () -> Unit,
  onPeriodClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        brush = Brush.verticalGradient(
          colors = if (isDarkMode) {
            listOf(Color(0xFF090F1D), Color(0xFF111C35), Color(0xFF1B2B4B))
          } else {
            listOf(Navy900, Navy800, Navy700)
          }
        )
      )
      .statusBarsPadding()
      .padding(bottom = 10.dp)
  ) {
    // Top Row: School Emblem & Custom/Default Title
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      SchoolLogoImage(
        schoolLogoPath = schoolLogoPath,
        size = 48.dp,
        borderWidth = 2.dp,
        borderColor = Gold500
      )

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = schoolName.ifBlank { "ЛБХБ ТУРСУНЗОДА" },
          color = Gold500,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.6.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Box(
          modifier = Modifier
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
              if (bellCountdown.isInLesson) Color(0xFF15803D).copy(alpha = 0.9f)
              else Color.White.copy(alpha = 0.16f)
            )
            .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
          Text(
            text = bellCountdown.displayText,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      if (showSearchIcon) {
        IconButton(
          onClick = onSearchClick,
          modifier = Modifier
            .size(48.dp)
            .testTag("header_search_icon_button")
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.18f))
              .border(1.dp, Gold500.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Ҷустуҷӯи хонанда",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    if (showSelectors) {
      Spacer(modifier = Modifier.height(1.dp))

      // Interactive Selector Buttons: Class, Date, Period
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HeaderButton(
          label = "Синф",
          value = className,
          icon = Icons.Default.Class,
          isDarkMode = isDarkMode,
          testTag = "header_class_button",
          onClick = onClassClick,
          modifier = Modifier.weight(1f)
        )

        HeaderButton(
          label = "Сана",
          value = formattedDate,
          icon = Icons.Default.CalendarMonth,
          isDarkMode = isDarkMode,
          testTag = "header_date_button",
          onClick = onDateClick,
          modifier = Modifier.weight(1.35f)
        )

        HeaderButton(
          label = "Соат",
          value = "$periodNumber-дарс",
          icon = Icons.Default.Schedule,
          isDarkMode = isDarkMode,
          testTag = "header_period_button",
          onClick = onPeriodClick,
          modifier = Modifier.weight(1.1f)
        )
      }
    }
  }
}

@Composable
private fun HeaderButton(
  label: String,
  value: String,
  icon: ImageVector,
  isDarkMode: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .testTag(testTag)
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(10.dp),
    color = if (isDarkMode) Color(0xFF1E293B) else Color.White,
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = if (isDarkMode) Gold500 else Navy800
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = label,
          fontSize = 12.sp,
          lineHeight = 13.sp,
          color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF334155),
          fontWeight = FontWeight.ExtraBold
        )
      }

      Text(
        text = value,
        fontSize = 14.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Black,
        color = if (isDarkMode) Color.White else Navy900,
        maxLines = 1
      )
    }
  }
}
