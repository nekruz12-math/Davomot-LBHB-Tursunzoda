package com.example.data.export

import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentReportStat
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxReportGenerator {

  fun generateAttendanceReportXlsx(
    className: String,
    schoolName: String,
    periodTitle: String,
    monthName: String,
    schoolDates: List<String>,
    reportData: List<StudentReportStat>
  ): ByteArray {
    val sheetXml = buildSheetXml(
      className = className,
      schoolName = schoolName,
      periodTitle = periodTitle,
      monthName = monthName,
      schoolDates = schoolDates,
      reportData = reportData
    )

    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zos ->
      writeZipEntry(zos, "[Content_Types].xml", CONTENT_TYPES_XML)
      writeZipEntry(zos, "_rels/.rels", ROOT_RELS_XML)
      writeZipEntry(zos, "xl/workbook.xml", WORKBOOK_XML)
      writeZipEntry(zos, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
      writeZipEntry(zos, "xl/styles.xml", STYLES_XML)
      writeZipEntry(zos, "xl/worksheets/sheet1.xml", sheetXml)
    }
    return baos.toByteArray()
  }

  private fun writeZipEntry(zos: ZipOutputStream, name: String, content: String) {
    val entry = ZipEntry(name)
    zos.putNextEntry(entry)
    zos.write(content.toByteArray(StandardCharsets.UTF_8))
    zos.closeEntry()
  }

  private fun colLetter(zeroBasedCol: Int): String {
    var index = zeroBasedCol
    val sb = StringBuilder()
    while (index >= 0) {
      sb.append(('A' + (index % 26)))
      index = index / 26 - 1
    }
    return sb.reverse().toString()
  }

  private fun cellRef(zeroBasedCol: Int, oneBasedRow: Int): String {
    return "${colLetter(zeroBasedCol)}$oneBasedRow"
  }

  private fun escapeXml(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      when (ch) {
        '&' -> sb.append("&amp;")
        '<' -> sb.append("&lt;")
        '>' -> sb.append("&gt;")
        '"' -> sb.append("&quot;")
        '\'' -> sb.append("&apos;")
        else -> {
          if (ch.code >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
            sb.append(ch)
          }
        }
      }
    }
    return sb.toString()
  }

  private fun strCell(ref: String, value: String, styleId: Int): String {
    if (value.isEmpty()) {
      return """<c r="$ref" s="$styleId"/>"""
    }
    return """<c r="$ref" t="inlineStr" s="$styleId"><is><t>${escapeXml(value)}</t></is></c>"""
  }

  private fun numCell(ref: String, value: Int, styleId: Int): String {
    return """<c r="$ref" s="$styleId"><v>$value</v></c>"""
  }

  private fun buildSheetXml(
    className: String,
    schoolName: String,
    periodTitle: String,
    monthName: String,
    schoolDates: List<String>,
    reportData: List<StudentReportStat>
  ): String {
    val dayCount = schoolDates.size
    val totalColumns = 2 + dayCount + 6
    val lastColIdx = (totalColumns - 1).coerceAtLeast(2)

    val sb = StringBuilder(16384)
    sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
    sb.append("""<sheetViews><sheetView workbookViewId="0"/></sheetViews>""")
    sb.append("""<sheetFormatPr defaultRowHeight="20"/>""")

    // Column widths
    sb.append("<cols>")
    sb.append("""<col min="1" max="1" width="5" customWidth="1"/>""")
    sb.append("""<col min="2" max="2" width="32" customWidth="1"/>""")
    if (dayCount > 0) {
      val dayStartCol = 3
      val dayEndCol = 2 + dayCount
      sb.append("""<col min="$dayStartCol" max="$dayEndCol" width="11" customWidth="1"/>""")
    }
    val summaryStartCol = 3 + dayCount
    val summaryEndCol = totalColumns
    sb.append("""<col min="$summaryStartCol" max="$summaryEndCol" width="14" customWidth="1"/>""")
    sb.append("</cols>")

    sb.append("<sheetData>")

    val mergeCells = mutableListOf<String>()

    // Row 1: Title
    sb.append("""<row r="1" ht="26" customHeight="1">""")
    sb.append(strCell("B1", "ҲИСОБОТИ ДАВОМОТ: Синфи $className", STYLE_TITLE))
    for (c in 2 until totalColumns) {
      sb.append(strCell(cellRef(c, 1), "", STYLE_TITLE))
    }
    sb.append("</row>")
    if (lastColIdx >= 1) {
      mergeCells.add("B1:${colLetter(lastColIdx)}1")
    }

    // Row 2: School Name
    sb.append("""<row r="2" ht="20" customHeight="1">""")
    sb.append(strCell("B2", "Мактаб: $schoolName", STYLE_SUBTITLE))
    for (c in 2 until totalColumns) {
      sb.append(strCell(cellRef(c, 2), "", STYLE_SUBTITLE))
    }
    sb.append("</row>")
    if (lastColIdx >= 1) {
      mergeCells.add("B2:${colLetter(lastColIdx)}2")
    }

    // Row 3: Period
    sb.append("""<row r="3" ht="20" customHeight="1">""")
    sb.append(strCell("B3", "Давра: $periodTitle", STYLE_SUBTITLE))
    for (c in 2 until totalColumns) {
      sb.append(strCell(cellRef(c, 3), "", STYLE_SUBTITLE))
    }
    sb.append("</row>")
    if (lastColIdx >= 1) {
      mergeCells.add("B3:${colLetter(lastColIdx)}3")
    }

    // Row 4: Month Banner over day columns
    sb.append("""<row r="4" ht="22" customHeight="1">""")
    sb.append(strCell("A4", "", STYLE_HEADER_GRAY))
    sb.append(strCell("B4", "", STYLE_HEADER_GRAY))
    if (dayCount > 0) {
      sb.append(strCell(cellRef(2, 4), monthName, STYLE_HEADER_NAVY))
      for (c in 3 until (2 + dayCount)) {
        sb.append(strCell(cellRef(c, 4), "", STYLE_HEADER_NAVY))
      }
      if (dayCount > 1) {
        mergeCells.add("${colLetter(2)}4:${colLetter(1 + dayCount)}4")
      }
    }
    for (c in (2 + dayCount) until totalColumns) {
      sb.append(strCell(cellRef(c, 4), "", STYLE_HEADER_GRAY))
    }
    if (lastColIdx > 2 + dayCount) {
      mergeCells.add("${colLetter(2 + dayCount)}4:${colLetter(lastColIdx)}4")
    }
    sb.append("</row>")

    // Row 5: Table Headers
    sb.append("""<row r="5" ht="28" customHeight="1">""")
    sb.append(strCell("A5", "№", STYLE_HEADER_NAVY))
    sb.append(strCell("B5", "Ному насаб", STYLE_HEADER_NAVY))
    schoolDates.forEachIndexed { idx, dStr ->
      val dayNum = dStr.takeLast(2).toIntOrNull()?.toString() ?: dStr.takeLast(2)
      sb.append(strCell(cellRef(2 + idx, 5), dayNum, STYLE_HEADER_NAVY))
    }
    val summaryHeaders = listOf(
      "СБ (соат)" to STYLE_HEADER_SB,
      "СБ (рӯзи пурра)" to STYLE_HEADER_SB,
      "БСБ (соат)" to STYLE_HEADER_BSB,
      "БСБ (рӯзи пурра)" to STYLE_HEADER_BSB,
      "Ҷамъ (соат)" to STYLE_HEADER_NAVY,
      "Ҷамъ (рӯзи пурра)" to STYLE_HEADER_NAVY
    )
    summaryHeaders.forEachIndexed { idx, (label, styleId) ->
      sb.append(strCell(cellRef(2 + dayCount + idx, 5), label, styleId))
    }
    sb.append("</row>")

    // Student Rows
    var totalSbHours = 0
    var totalSbDays = 0
    var totalBsbHours = 0
    var totalBsbDays = 0

    reportData.forEachIndexed { index, item ->
      val rowNum = 6 + index
      totalSbHours += item.sbHours
      totalSbDays += item.sbFullDays
      totalBsbHours += item.bsbHours
      totalBsbDays += item.bsbFullDays

      val detailsByDate = item.dailyDetails.associateBy { it.dateStr }

      sb.append("""<row r="$rowNum" ht="20" customHeight="1">""")
      sb.append(numCell(cellRef(0, rowNum), index + 1, STYLE_CELL_CENTER))
      sb.append(strCell(cellRef(1, rowNum), item.studentName, STYLE_CELL_LEFT))

      schoolDates.forEachIndexed { dIdx, dStr ->
        val detail = detailsByDate[dStr]
        val (cellVal, cellStyle) = if (detail == null) {
          "" to STYLE_CELL_CENTER
        } else {
          val sbC = detail.missedPeriods.count { it.second.second == AttendanceStatus.SB }
          val bsbC = detail.missedPeriods.count { it.second.second == AttendanceStatus.BSB }
          val lateC = detail.missedPeriods.count { it.second.second == AttendanceStatus.LATE }
          when {
            detail.isFullDaySB -> "СБ (пурра)" to STYLE_CELL_SB
            detail.isFullDayBSB -> "БСБ (пурра)" to STYLE_CELL_BSB
            sbC > 0 && bsbC > 0 -> "${sbC}с СБ / ${bsbC}с БСБ" to STYLE_CELL_BSB
            sbC > 0 -> "${sbC}с СБ" to STYLE_CELL_SB
            bsbC > 0 -> "${bsbC}с БСБ" to STYLE_CELL_BSB
            lateC > 0 -> "Дер" to STYLE_CELL_LATE
            else -> "" to STYLE_CELL_CENTER
          }
        }
        sb.append(strCell(cellRef(2 + dIdx, rowNum), cellVal, cellStyle))
      }

      val colOffset = 2 + dayCount
      sb.append(numCell(cellRef(colOffset, rowNum), item.sbHours, if (item.sbHours > 0) STYLE_CELL_SB else STYLE_CELL_CENTER))
      sb.append(numCell(cellRef(colOffset + 1, rowNum), item.sbFullDays, if (item.sbFullDays > 0) STYLE_CELL_SB else STYLE_CELL_CENTER))
      sb.append(numCell(cellRef(colOffset + 2, rowNum), item.bsbHours, if (item.bsbHours > 0) STYLE_CELL_BSB else STYLE_CELL_CENTER))
      sb.append(numCell(cellRef(colOffset + 3, rowNum), item.bsbFullDays, if (item.bsbFullDays > 0) STYLE_CELL_BSB else STYLE_CELL_CENTER))
      sb.append(numCell(cellRef(colOffset + 4, rowNum), item.totalMissedHours, STYLE_CELL_TOTAL_COL))
      sb.append(numCell(cellRef(colOffset + 5, rowNum), item.totalFullDays, STYLE_CELL_TOTAL_COL))
      sb.append("</row>")
    }

    // Footer Total Row
    val footerRow = 6 + reportData.size
    sb.append("""<row r="$footerRow" ht="24" customHeight="1">""")
    sb.append(strCell(cellRef(0, footerRow), "", STYLE_FOOTER_BOLD))
    sb.append(strCell(cellRef(1, footerRow), "ҲАМАГӢ:", STYLE_FOOTER_BOLD))
    for (dIdx in 0 until dayCount) {
      sb.append(strCell(cellRef(2 + dIdx, footerRow), "", STYLE_FOOTER_BOLD))
    }
    val colOffset = 2 + dayCount
    sb.append(numCell(cellRef(colOffset, footerRow), totalSbHours, STYLE_FOOTER_BOLD))
    sb.append(numCell(cellRef(colOffset + 1, footerRow), totalSbDays, STYLE_FOOTER_BOLD))
    sb.append(numCell(cellRef(colOffset + 2, footerRow), totalBsbHours, STYLE_FOOTER_BOLD))
    sb.append(numCell(cellRef(colOffset + 3, footerRow), totalBsbDays, STYLE_FOOTER_BOLD))
    sb.append(numCell(cellRef(colOffset + 4, footerRow), totalSbHours + totalBsbHours, STYLE_FOOTER_BOLD))
    sb.append(numCell(cellRef(colOffset + 5, footerRow), totalSbDays + totalBsbDays, STYLE_FOOTER_BOLD))
    sb.append("</row>")

    sb.append("</sheetData>")

    if (mergeCells.isNotEmpty()) {
      sb.append("""<mergeCells count="${mergeCells.size}">""")
      mergeCells.forEach { range ->
        sb.append("""<mergeCell ref="$range"/>""")
      }
      sb.append("</mergeCells>")
    }

    sb.append("</worksheet>")
    return sb.toString()
  }

  private const val STYLE_CELL_LEFT = 0
  private const val STYLE_TITLE = 1
  private const val STYLE_SUBTITLE = 2
  private const val STYLE_HEADER_NAVY = 3
  private const val STYLE_HEADER_SB = 4
  private const val STYLE_HEADER_BSB = 5
  private const val STYLE_HEADER_GRAY = 6
  private const val STYLE_CELL_CENTER = 7
  private const val STYLE_CELL_SB = 8
  private const val STYLE_CELL_BSB = 9
  private const val STYLE_CELL_LATE = 10
  private const val STYLE_CELL_TOTAL_COL = 11
  private const val STYLE_FOOTER_BOLD = 12

  private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

  private const val ROOT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

  private const val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Ҳисобот" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

  private const val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

  private const val STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="7">
    <font><sz val="11"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
    <font><b/><sz val="14"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FF334155"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FF14532D"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FF7F1D1D"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FF92400E"/><name val="Calibri"/></font>
  </fonts>
  <fills count="8">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF1E3A8A"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFDCFCE7"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEF3C7"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border><left/><right/><top/><bottom/><diagonal/></border>
    <border>
      <left style="thin"><color rgb="FF94A3B8"/></left>
      <right style="thin"><color rgb="FF94A3B8"/></right>
      <top style="thin"><color rgb="FF94A3B8"/></top>
      <bottom style="thin"><color rgb="FF94A3B8"/></bottom>
      <diagonal/>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="13">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="left"/></xf>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" applyFont="1" applyAlignment="1"><alignment vertical="center" horizontal="left"/></xf>
    <xf numFmtId="0" fontId="2" fillId="0" borderId="0" applyFont="1" applyAlignment="1"><alignment vertical="center" horizontal="left"/></xf>
    <xf numFmtId="0" fontId="3" fillId="2" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="4" fillId="3" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="5" fillId="4" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="4" fillId="3" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="5" fillId="4" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="6" fillId="6" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="7" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center" horizontal="center"/></xf>
  </cellXfs>
</styleSheet>"""
}
