package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.AppScreen
import com.example.ui.AppStrings
import com.example.ui.AppStringsCatalog
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class TelegramNavItem(
  val screen: AppScreen,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

@Composable
fun TelegramBottomBar(
  currentScreen: AppScreen,
  isDarkMode: Boolean,
  appStrings: AppStrings = AppStringsCatalog.forLanguage(AppLanguage.TJ),
  onSelectScreen: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val items = remember(appStrings) {
    listOf(
      TelegramNavItem(
        screen = AppScreen.HOME,
        label = appStrings.navHome,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_item_home"
      ),
      TelegramNavItem(
        screen = AppScreen.ATTENDANCE,
        label = appStrings.navJournal,
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
        testTag = "nav_item_attendance"
      ),
      TelegramNavItem(
        screen = AppScreen.DISCIPLINE,
        label = appStrings.navDiscipline,
        selectedIcon = Icons.Filled.ReportProblem,
        unselectedIcon = Icons.Outlined.ReportProblem,
        testTag = "nav_item_discipline"
      ),
      TelegramNavItem(
        screen = AppScreen.REPORTS,
        label = appStrings.navReports,
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
        testTag = "nav_item_reports"
      ),
      TelegramNavItem(
        screen = AppScreen.SETTINGS,
        label = appStrings.navSettings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_item_settings"
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 10.dp, vertical = 8.dp)
      .testTag("bottom_nav_bar"),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(50),
      color = if (isDarkMode) Color(0xFF1E293B) else Color.White,
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
          shape = RoundedCornerShape(50)
        )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val activeBlue = if (isDarkMode) Color(0xFF58B9F7) else Color(0xFF1A8CD8)
        val activePillBg = if (isDarkMode) Color(0xFF16324F) else Color(0xFFEAF4FE)
        val inactiveColor = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1C1C1E)

        items.forEach { item ->
          val isSelected = currentScreen == item.screen
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(50))
              .background(if (isSelected) activePillBg else Color.Transparent)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSelectScreen(item.screen) }
              )
              .padding(vertical = 7.dp, horizontal = 2.dp)
              .testTag(item.testTag),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) activeBlue else inactiveColor,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = item.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) activeBlue else inactiveColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun WindowsSplashOverlay(
  schoolName: String,
  schoolLogoPath: String
) {
  val infiniteTransition = rememberInfiniteTransition(label = "windows_loader")
  val baseAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "orbit_angle"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF091224), Navy900, Navy800)
        )
      )
      .testTag("windows_splash_overlay"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      // Orbiting Windows-style dots around the chosen school logo
      Box(
        modifier = Modifier.size(184.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerOffset = Offset(size.width / 2f, size.height / 2f)
          val orbitRadius = (size.minDimension / 2f) - 10.dp.toPx()
          val dotCount = 6

          for (i in 0 until dotCount) {
            // Classic Windows staggered harmonic acceleration curve
            val lagDegrees = i * 24f
            val rawDeg = (baseAngle - lagDegrees + 360f) % 360f
            val rad = rawDeg * (PI / 180.0)
            val harmonicBoost = sin(rad) * 28.0
            val finalDeg = (rawDeg + harmonicBoost) - 90.0
            val finalRad = finalDeg * (PI / 180.0)

            val dotX = centerOffset.x + (orbitRadius * cos(finalRad)).toFloat()
            val dotY = centerOffset.y + (orbitRadius * sin(finalRad)).toFloat()

            val dotRadius = (5.8f - (i * 0.45f)).coerceAtLeast(3.2f).dp.toPx()
            val alpha = (1f - (i * 0.11f)).coerceIn(0.35f, 1f)

            drawCircle(
              color = Gold500.copy(alpha = alpha * 0.35f),
              radius = dotRadius * 1.6f,
              center = Offset(dotX, dotY)
            )
            drawCircle(
              color = Color.White.copy(alpha = alpha),
              radius = dotRadius,
              center = Offset(dotX, dotY)
            )
          }
        }

        SchoolLogoImage(
          schoolLogoPath = schoolLogoPath,
          size = 122.dp,
          borderWidth = 3.dp,
          borderColor = Gold500
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = schoolName.ifBlank { "ЛБХБ ТУРСУНЗОДА" },
        color = Gold500,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        letterSpacing = 0.8.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Журнали Ман",
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Боршавии барнома...",
        color = Color(0xFF94A3B8),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
