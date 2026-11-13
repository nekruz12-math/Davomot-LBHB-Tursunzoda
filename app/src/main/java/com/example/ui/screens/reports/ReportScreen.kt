package com.example.ui.screens.reports

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceStatus
import com.example.data.model.ReportPeriodMode
import com.example.data.model.SchoolClass
import com.example.data.model.SmartAnalyticsSummary
import com.example.data.model.StudentReportStat
import com.example.data.model.TajikDateUtils
import com.example.ui.components.ClassPickerDialog
import com.example.ui.components.TajikCalendarDialog
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportScreen(
  classes: List<SchoolClass> = emptyList(),
  currentClass: SchoolClass?,
  onSelectClass: (String) -> Unit = {},
  reportMode: ReportPeriodMode,
  reportYearMonth: String,
  customStartDate: String,
  customEndDate: String,
  periodSubtitle: String,
  schoolDates: List<String> = emptyList(),
  reportData: List<StudentReportStat>,
  smartAnalytics: SmartAnalyticsSummary,
  isDarkMode: Boolean,
  fontScale: Float,
  onReportModeChanged: (ReportPeriodMode) -> Unit,
  onMonthChanged: (String) -> Unit,
  onCustomRangeChanged: (start: String, end: String) -> Unit,
  onSaveExcelToDownloads: (Context) -> Unit,
  onSaveExcelToUri: (Context, Uri) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var selectedStudentStat by remember { mutableStateOf<StudentReportStat?>(null) }
  var pickingStartDate by remember { mutableStateOf(false) }
  var pickingEndDate by remember { mutableStateOf(false) }
  var showClassPicker by remember { mutableStateOf(false) }
  val tableScrollState = rememberScrollState()

  val parts = reportYearMonth.split("-")
  val currentYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
  val currentMonth = parts.getOrNull(1)?.toIntOrNull() ?: 11 // 1..12
  val monthName = TajikDateUtils.MONTHS.getOrElse(currentMonth - 1) { "" }

  val numColWidth = 28.dp
  val nameColWidth = 142.dp
  val dayColWidth = 54.dp
  val statColWidth = 60.dp
  val displayMonthLabel = remember(schoolDates, monthName) {
    val firstMonthIdx = schoolDates.firstOrNull()?.split("-")?.getOrNull(1)?.toIntOrNull()
    if (firstMonthIdx != null) {
      TajikDateUtils.MONTHS.getOrElse(firstMonthIdx - 1) { monthName.ifBlank { "Моҳ" } }
    } else {
      monthName.ifBlank { "Моҳ" }
    }
  }

  val totalSbHours = reportData.sumOf { it.sbHours }
  val totalSbDays = reportData.sumOf { it.sbFullDays }
  val totalBsbHours = reportData.sumOf { it.bsbHours }
  val totalBsbDays = reportData.sumOf { it.bsbFullDays }
  val totalMissedHours = totalSbHours + totalBsbHours
  val totalMissedDays = totalSbDays + totalBsbDays

  val screenBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF4F6F9)
  val surfaceBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
  val primaryText = if (isDarkMode) Color(0xFFF8FAFC) else Navy900
  val secondaryText = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(screenBg)
  ) {
    // 1. Header with Mode Selector & Date Range Controls
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
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFEFF6FF))
                .border(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFF93C5FD), RoundedCornerShape(8.dp))
                .clickable { showClassPicker = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("report_class_picker_btn")
            ) {
              Text(
                text = "📘 Синф: ${currentClass?.name ?: "Интихоб"}",
                fontSize = (14.5f * fontScale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkMode) Gold500 else Navy900
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Ивази синф",
                tint = if (isDarkMode) Gold500 else Navy900,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = periodSubtitle,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
          }

          if (reportMode == ReportPeriodMode.MONTHLY) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
              IconButton(
                onClick = {
                  var prevM = currentMonth - 1
                  var prevY = currentYear
                  if (prevM < 1) {
                    prevM = 12
                    prevY -= 1
                  }
                  onMonthChanged(String.format(Locale.US, "%04d-%02d", prevY, prevM))
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Моҳи пешина",
                  tint = Navy900,
                  modifier = Modifier.size(16.dp)
                )
              }

              Text(
                text = "$monthName $currentYear",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy900,
                modifier = Modifier.padding(horizontal = 6.dp)
              )

              IconButton(
                onClick = {
                  var nextM = currentMonth + 1
                  var nextY = currentYear
                  if (nextM > 12) {
                    nextM = 1
                    nextY += 1
                  }
                  onMonthChanged(String.format(Locale.US, "%04d-%02d", nextY, nextM))
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "Моҳи оянда",
                  tint = Navy900,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Period Mode Chips: Рӯзона, Ҳафтаина, Моҳона, Аз сана то сана
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(ReportPeriodMode.entries) { mode ->
            val selected = mode == reportMode
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) Navy800 else Color(0xFFF1F5F9))
                .border(
                  width = 1.dp,
                  color = if (selected) Gold500 else Color(0xFFCBD5E1),
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { onReportModeChanged(mode) }
                .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
              Text(
                text = mode.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF1E293B)
              )
            }
          }
        }

        if (reportMode == ReportPeriodMode.CUSTOM) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                .clickable { pickingStartDate = true }
                .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = Navy800,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Аз: ${TajikDateUtils.formatDateDisplay(customStartDate)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Navy900
                )
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                .clickable { pickingEndDate = true }
                .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = Navy800,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "То: ${TajikDateUtils.formatDateDisplay(customEndDate)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Navy900
                )
              }
            }
          }
        }
      }
    }

    // 2. Summary Cards (Compact row above the scroll region)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      SummaryMetricCard(
        label = "Хонандагон",
        primaryValue = "${reportData.size}",
        secondaryValue = "нафар",
        color = Navy800,
        bgColor = Color.White,
        modifier = Modifier.weight(0.95f)
      )
      SummaryMetricCard(
        label = "СБ (Сабабнок)",
        primaryValue = "$totalSbHours с.",
        secondaryValue = "$totalSbDays рӯз",
        color = StatusGreen,
        bgColor = Color(0xFFDCFCE7),
        modifier = Modifier.weight(1.05f)
      )
      SummaryMetricCard(
        label = "БСБ (Бе сабаб)",
        primaryValue = "$totalBsbHours с.",
        secondaryValue = "$totalBsbDays рӯз",
        color = StatusRed,
        bgColor = Color(0xFFFEE2E2),
        modifier = Modifier.weight(1.05f)
      )
      SummaryMetricCard(
        label = "Ҷамъи кулл",
        primaryValue = "$totalMissedHours с.",
        secondaryValue = "$totalMissedDays рӯз",
        color = Color(0xFF0F172A),
        bgColor = Color(0xFFFEF3C7),
        modifier = Modifier.weight(1.05f)
      )
    }

    // 3. Scrollable Region starting right from "Таҳлили ҳушманд" (Analytics -> Excel Button -> Sticky Table Header -> Student Rows)
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp)
        .testTag("reports_scrollable_list"),
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
      // Idea #20: Smart Analytics Card ("Кадом рӯз ё кадом фан бештар ғоиб мешаванд?")
      if (totalMissedHours > 0) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Insights,
                  contentDescription = null,
                  tint = Navy800,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Таҳлили ҳушманд (Аналитика):",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Black,
                  color = Navy900
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              val dayInsight = if (smartAnalytics.mostMissedDayName != null) {
                "• Хонандагон бештар рӯзи «${smartAnalytics.mostMissedDayName}» (${smartAnalytics.mostMissedDayHours} соат) ва дар соати ${smartAnalytics.mostMissedPeriod}-ум ғоиб мешаванд."
              } else ""
              val subjInsight = if (smartAnalytics.mostBsbSubject != null) {
                "• Дар фанни «${smartAnalytics.mostBsbSubject}» бештар БСБ (${smartAnalytics.mostBsbSubjectHours} соат) ба қайд гирифта шудааст."
              } else if (smartAnalytics.mostMissedSubject != null) {
                "• Бештари ғоибҳо дар фанни «${smartAnalytics.mostMissedSubject}» (${smartAnalytics.mostMissedSubjectHours} соат) мебошад."
              } else ""

              if (dayInsight.isNotBlank()) {
                Text(
                  text = dayInsight,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF1E293B),
                  lineHeight = 15.sp
                )
              }
              if (subjInsight.isNotBlank()) {
                Text(
                  text = subjInsight,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF7F1D1D),
                  lineHeight = 15.sp
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
        }
      }

      // Direct Report Download Button + Hint inside the scroll
      item {
        Button(
          onClick = { onSaveExcelToDownloads(context) },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("export_excel_button"),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
        ) {
          Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Боргирии ҳисобот",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }

        Text(
          text = "💡 Барои дидани сана ва дарсҳои ғоибшуда болои номи хонанда пахш кунед • Барои дидани рӯзҳо ба рост лағжонед",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = secondaryText,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
      }

      // Sticky Table Header (Left pinned № + Ному насаб, Right horizontally scrollable day-by-day + totals)
      stickyHeader {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color(0xFFCBD5E1),
          shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left fixed columns: № + Ному насаб
            Row(
              modifier = Modifier
                .width(numColWidth + nameColWidth)
                .fillMaxHeight()
                .background(Color(0xFF1E3A8A))
                .border(0.5.dp, Color(0xFF94A3B8)),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "№",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.width(numColWidth),
                textAlign = TextAlign.Center
              )
              Box(
                modifier = Modifier
                  .width(0.8.dp)
                  .fillMaxHeight()
                  .background(Color.White.copy(alpha = 0.25f))
              )
              Text(
                text = "Ному насаб",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                  .width(nameColWidth)
                  .padding(horizontal = 6.dp)
              )
            }

            // Right horizontally scrollable columns: Month + Days + Summary totals
            Column(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(tableScrollState)
            ) {
              // Top Sub-Row: Month Banner across day columns
              Row(
                modifier = Modifier.height(22.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (schoolDates.isNotEmpty()) {
                  Box(
                    modifier = Modifier
                      .width(dayColWidth * schoolDates.size)
                      .fillMaxHeight()
                      .background(Color(0xFF1E3A8A))
                      .border(0.5.dp, Color(0xFF94A3B8)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = displayMonthLabel,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Gold500
                    )
                  }
                }
                Box(
                  modifier = Modifier
                    .width(statColWidth * 6)
                    .fillMaxHeight()
                    .background(Color(0xFFE2E8F0))
                    .border(0.5.dp, Color(0xFF94A3B8)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Ҷамъбаст",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                  )
                }
              }

              // Bottom Sub-Row: Day numbers + Summary headers
              Row(
                modifier = Modifier.height(42.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                schoolDates.forEach { dStr ->
                  val dayNum = dStr.takeLast(2).toIntOrNull()?.toString() ?: dStr.takeLast(2)
                  Box(
                    modifier = Modifier
                      .width(dayColWidth)
                      .fillMaxHeight()
                      .background(Color(0xFF284B9E))
                      .border(0.5.dp, Color(0xFF94A3B8)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = dayNum,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color.White,
                      textAlign = TextAlign.Center
                    )
                  }
                }

                ReportHeaderCell("СБ\n(соат)", Color(0xFFDCFCE7), Color(0xFF14532D), statColWidth)
                ReportHeaderCell("СБ\n(рӯз)", Color(0xFFDCFCE7), Color(0xFF14532D), statColWidth)
                ReportHeaderCell("БСБ\n(соат)", Color(0xFFFEE2E2), Color(0xFF7F1D1D), statColWidth)
                ReportHeaderCell("БСБ\n(рӯз)", Color(0xFFFEE2E2), Color(0xFF7F1D1D), statColWidth)
                ReportHeaderCell("Ҷамъ\n(соат)", Color(0xFFE2E8F0), Color(0xFF0F172A), statColWidth)
                ReportHeaderCell("Ҷамъ\n(рӯз)", Color(0xFFE2E8F0), Color(0xFF0F172A), statColWidth)
              }
            }
          }
        }
      }

      // Report Table Rows (Day-by-day + totals)
      itemsIndexed(reportData) { index, item ->
        val rowBg = when {
          item.hasHighBsbWarning -> Color(0xFFFFF1F2)
          index % 2 == 0 -> Color.White
          else -> Color(0xFFF8FAFC)
        }
        val detailsByDate = remember(item.dailyDetails) {
          item.dailyDetails.associateBy { it.dateStr }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedStudentStat = item },
          color = rowBg
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left fixed: Index + Student Name
            Row(
              modifier = Modifier
                .width(numColWidth + nameColWidth)
                .fillMaxHeight()
                .background(rowBg)
                .border(0.5.dp, Color(0xFFCBD5E1)),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${index + 1}",
                fontSize = (11.5f * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                modifier = Modifier.width(numColWidth),
                textAlign = TextAlign.Center
              )
              Box(
                modifier = Modifier
                  .width(0.8.dp)
                  .fillMaxHeight()
                  .background(Color(0xFFCBD5E1))
              )
              Row(
                modifier = Modifier
                  .width(nameColWidth)
                  .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = item.studentName,
                  fontSize = (12.5f * fontScale).sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A),
                  maxLines = 2,
                  lineHeight = 14.sp,
                  modifier = Modifier.weight(1f, fill = false)
                )
                if (item.hasHighBsbWarning) {
                  Spacer(modifier = Modifier.width(2.dp))
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "БСБ зиёд",
                    tint = StatusRed,
                    modifier = Modifier.size(13.dp)
                  )
                }
              }
            }

            // Right horizontally scrollable: Day-by-Day cells + 6 totals
            Row(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(tableScrollState),
              verticalAlignment = Alignment.CenterVertically
            ) {
              schoolDates.forEach { dStr ->
                val detail = detailsByDate[dStr]
                val (cellText, cellBg, cellColor) = if (detail == null) {
                  Triple("", rowBg, Color(0xFF64748B))
                } else {
                  val sbC = detail.missedPeriods.count { it.second.second == AttendanceStatus.SB }
                  val bsbC = detail.missedPeriods.count { it.second.second == AttendanceStatus.BSB }
                  val lateC = detail.missedPeriods.count { it.second.second == AttendanceStatus.LATE }
                  when {
                    detail.isFullDaySB -> Triple("СБ\n(пурра)", Color(0xFFDCFCE7), Color(0xFF14532D))
                    detail.isFullDayBSB -> Triple("БСБ\n(пурра)", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
                    sbC > 0 && bsbC > 0 -> Triple("${sbC}С/${bsbC}Б", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
                    sbC > 0 -> Triple("${sbC}с СБ", Color(0xFFDCFCE7), Color(0xFF14532D))
                    bsbC > 0 -> Triple("${bsbC}с БСБ", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
                    lateC > 0 -> Triple("Дер", Color(0xFFFEF3C7), Color(0xFF92400E))
                    else -> Triple("", rowBg, Color(0xFF64748B))
                  }
                }
                Box(
                  modifier = Modifier
                    .width(dayColWidth)
                    .fillMaxHeight()
                    .background(cellBg)
                    .border(0.5.dp, Color(0xFFCBD5E1)),
                  contentAlignment = Alignment.Center
                ) {
                  if (cellText.isNotEmpty()) {
                    Text(
                      text = cellText,
                      fontSize = 10.sp,
                      lineHeight = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = cellColor,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              }

              ReportNumericCell(item.sbHours, if (item.sbHours > 0) StatusGreen else Color(0xFF64748B), rowBg, statColWidth)
              ReportNumericCell(item.sbFullDays, if (item.sbFullDays > 0) Color(0xFF15803D) else Color(0xFF64748B), rowBg, statColWidth)
              ReportNumericCell(item.bsbHours, if (item.bsbHours > 0) StatusRed else Color(0xFF64748B), rowBg, statColWidth)
              ReportNumericCell(item.bsbFullDays, if (item.bsbFullDays > 0) Color(0xFFB91C1C) else Color(0xFF64748B), rowBg, statColWidth)
              ReportNumericCell(item.totalMissedHours, if (item.totalMissedHours > 0) Color(0xFF0F172A) else Color(0xFF64748B), Color(0xFFF8FAFC), statColWidth)
              ReportNumericCell(item.totalFullDays, if (item.totalFullDays > 0) Navy800 else Color(0xFF64748B), Color(0xFFF8FAFC), statColWidth)
            }
          }
        }
      }

      // Totals Footer Row ("ҲАМАГӢ:") matching the Excel/CSV template
      if (reportData.isNotEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE2E8F0),
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier
                  .width(numColWidth + nameColWidth)
                  .fillMaxHeight()
                  .background(Color(0xFFE2E8F0))
                  .border(0.5.dp, Color(0xFF94A3B8)),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Spacer(modifier = Modifier.width(numColWidth))
                Text(
                  text = "ҲАМАГӢ:",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF0F172A),
                  modifier = Modifier.padding(horizontal = 6.dp)
                )
              }

              Row(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .horizontalScroll(tableScrollState),
                verticalAlignment = Alignment.CenterVertically
              ) {
                repeat(schoolDates.size) {
                  Box(
                    modifier = Modifier
                      .width(dayColWidth)
                      .fillMaxHeight()
                      .background(Color(0xFFE2E8F0))
                      .border(0.5.dp, Color(0xFF94A3B8))
                  )
                }
                ReportNumericCell(totalSbHours, Color(0xFF14532D), Color(0xFFDCFCE7), statColWidth)
                ReportNumericCell(totalSbDays, Color(0xFF14532D), Color(0xFFDCFCE7), statColWidth)
                ReportNumericCell(totalBsbHours, Color(0xFF7F1D1D), Color(0xFFFEE2E2), statColWidth)
                ReportNumericCell(totalBsbDays, Color(0xFF7F1D1D), Color(0xFFFEE2E2), statColWidth)
                ReportNumericCell(totalMissedHours, Color(0xFF0F172A), Color(0xFFCBD5E1), statColWidth)
                ReportNumericCell(totalMissedDays, Color(0xFF0F172A), Color(0xFFCBD5E1), statColWidth)
              }
            }
          }
        }
      }
    }
  }

  // Student Detailed Drill-Down Dialog
  selectedStudentStat?.let { stat ->
    StudentDetailDialog(
      stat = stat,
      periodSubtitle = periodSubtitle,
      onDismiss = { selectedStudentStat = null }
    )
  }

  if (pickingStartDate) {
    TajikCalendarDialog(
      currentDateString = customStartDate,
      onDismiss = { pickingStartDate = false },
      onDateSelected = { newStart ->
        onCustomRangeChanged(newStart, customEndDate)
      }
    )
  }

  if (pickingEndDate) {
    TajikCalendarDialog(
      currentDateString = customEndDate,
      onDismiss = { pickingEndDate = false },
      onDateSelected = { newEnd ->
        onCustomRangeChanged(customStartDate, newEnd)
      }
    )
  }

  if (showClassPicker) {
    ClassPickerDialog(
      classes = classes,
      selectedClassId = currentClass?.id ?: "",
      onDismiss = { showClassPicker = false },
      onClassSelected = {
        onSelectClass(it)
        showClassPicker = false
      },
      onManageClassesClick = { showClassPicker = false }
    )
  }
}

@Composable
private fun ReportHeaderCell(
  text: String,
  bgColor: Color,
  textColor: Color,
  width: androidx.compose.ui.unit.Dp
) {
  Box(
    modifier = Modifier
      .width(width)
      .fillMaxHeight()
      .background(bgColor)
      .border(0.5.dp, Color(0xFF94A3B8))
      .padding(horizontal = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = 10.sp,
      lineHeight = 11.5.sp,
      fontWeight = FontWeight.Black,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun ReportNumericCell(
  value: Int,
  textColor: Color,
  bgColor: Color,
  width: androidx.compose.ui.unit.Dp
) {
  Box(
    modifier = Modifier
      .width(width)
      .fillMaxHeight()
      .background(bgColor)
      .border(0.5.dp, Color(0xFFCBD5E1)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "$value",
      fontSize = 12.sp,
      fontWeight = FontWeight.ExtraBold,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun SummaryMetricCard(
  label: String,
  primaryValue: String,
  secondaryValue: String,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = bgColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 5.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155),
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(1.dp))
      Text(
        text = primaryValue,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Black,
        color = color
      )
      Text(
        text = secondaryValue,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color.copy(alpha = 0.85f)
      )
    }
  }
}

@Composable
private fun StudentDetailDialog(
  stat: StudentReportStat,
  periodSubtitle: String,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
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
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stat.studentName,
              fontSize = 17.sp,
              fontWeight = FontWeight.Black,
              color = Navy900
            )
            Text(
              text = "Тафсилоти давомот ($periodSubtitle)",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF475569)
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Пӯшидан",
              tint = Color(0xFF334155)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFDCFCE7))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "СБ: ${stat.sbHours} соат (${stat.sbFullDays} рӯз)",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF14532D)
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFEE2E2))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "БСБ: ${stat.bsbHours} соат (${stat.bsbFullDays} рӯз)",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF7F1D1D)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(8.dp))

        if (stat.dailyDetails.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Дар ин давра хонанда ғоиб надорад! 🎉",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = StatusGreen
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(stat.dailyDetails) { dayDetail ->
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "📅 ${dayDetail.formattedDate} (${dayDetail.dayName})",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Navy900
                    )
                    if (dayDetail.isFullDaySB) {
                      Text(
                        text = "Рӯзи пурра (СБ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = StatusGreen
                      )
                    } else if (dayDetail.isFullDayBSB) {
                      Text(
                        text = "Рӯзи пурра (БСБ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = StatusRed
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  dayDetail.missedPeriods.forEach { (periodNum, subjAndStatus) ->
                    val (subj, st) = subjAndStatus
                    val statusLabel = when (st) {
                      AttendanceStatus.SB -> "СБ"
                      AttendanceStatus.BSB -> "БСБ"
                      AttendanceStatus.LATE -> "Дермонӣ"
                      else -> "Ҳозир"
                    }
                    val statusColor = when (st) {
                      AttendanceStatus.SB -> Color(0xFF15803D)
                      AttendanceStatus.BSB -> Color(0xFFB91C1C)
                      AttendanceStatus.LATE -> Color(0xFFD97706)
                      else -> Color(0xFF0F766E)
                    }
                    Text(
                      text = "• Соати $periodNum ($subj): $statusLabel",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = statusColor
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
}
