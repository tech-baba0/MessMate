package com.messmate.android.ui.screens.admin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.DatePickerDialog
import com.messmate.android.data.meal.MealReportEntry
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminReportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var startDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1).toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Report", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (state is ReportState.Success) {
                        IconButton(onClick = {
                            val entries = (state as ReportState.Success).entries
                            val saved = generatePdf(context, entries, startDate, endDate)
                            Toast.makeText(context, if (saved != null) "PDF saved: $saved" else "Failed to save PDF", Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF", tint = Color(0xFF00FFB2))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF111119))
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Date Range Selector Card ──────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Date Range", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Start Date
                        DatePickerField(
                            label = "From",
                            value = startDate,
                            modifier = Modifier.weight(1f),
                            context = context
                        ) { startDate = it }

                        // End Date
                        DatePickerField(
                            label = "To",
                            value = endDate,
                            modifier = Modifier.weight(1f),
                            context = context
                        ) { endDate = it }
                    }

                    Button(
                        onClick = { viewModel.fetchReport(startDate, endDate) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Generate Report", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Report Content ────────────────────────────────────────────────
            when (val s = state) {
                is ReportState.Idle -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Select a date range and tap Generate Report", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                is ReportState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00FFB2))
                    }
                }
                is ReportState.Error -> {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A)), shape = RoundedCornerShape(12.dp)) {
                        Text(s.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
                is ReportState.Success -> {
                    val entries = s.entries
                    if (entries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No meal entries found for this period.", color = Color.Gray)
                        }
                    } else {
                        // Summary stats
                        val totalLunch  = entries.count { it.lunch == true }
                        val totalDinner = entries.count { it.dinner == true }
                        val uniqueDays  = entries.map { it.date }.distinct().size
                        val uniqueMembers = entries.map { it.userName }.distinct().size

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2A1A)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                SummaryStatColumn("Members", uniqueMembers.toString(), Color(0xFF00FFB2))
                                SummaryStatColumn("Days", uniqueDays.toString(), Color(0xFF60A5FA))
                                SummaryStatColumn("🍽 Lunch", totalLunch.toString(), Color(0xFFFBBF24))
                                SummaryStatColumn("🌙 Dinner", totalDinner.toString(), Color(0xFFA78BFA))
                            }
                        }

                        // Report Table Header
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Table header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2D2D42))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text("Date",   color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f), fontSize = 13.sp)
                                    Text("Name",   color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f),   fontSize = 13.sp)
                                    Text("Lunch",  color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),   fontSize = 13.sp)
                                    Text("Dinner", color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),   fontSize = 13.sp)
                                    Text("Units",  color = Color(0xFF00FFB2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), fontSize = 13.sp)
                                }
                                Divider(color = Color(0xFF2D2D42))

                                // Group entries by date for visual separation
                                val grouped = entries.groupBy { it.date }
                                LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) {
                                    grouped.forEach { (date, dayEntries) ->
                                        item {
                                            // Date separator row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF252538))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    formatDate(date),
                                                    color = Color(0xFF93C5FD),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        items(dayEntries) { entry ->
                                            ReportRow(entry = entry)
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
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    context: Context,
    onDateSelected: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label, fontSize = 12.sp) },
        readOnly = true,
        modifier = modifier,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    onDateSelected("$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}")
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = Color(0xFF00FFB2))
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor    = Color.White,
            unfocusedTextColor  = Color.White,
            focusedContainerColor   = Color(0xFF2A2A3C),
            unfocusedContainerColor = Color(0xFF2A2A3C),
            focusedLabelColor   = Color(0xFF00FFB2),
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Composable
private fun SummaryStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun ReportRow(entry: MealReportEntry) {
    val lunchColor  = when (entry.lunch)  { true -> Color(0xFF4ADE80); false -> Color(0xFFEF4444); else -> Color.Gray }
    val dinnerColor = when (entry.dinner) { true -> Color(0xFF4ADE80); false -> Color(0xFFEF4444); else -> Color.Gray }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("",              color = Color.Transparent, modifier = Modifier.weight(1.8f), fontSize = 13.sp) // date placeholder (shown in header row)
        Text(entry.userName, color = Color.White,       modifier = Modifier.weight(2f),   fontSize = 13.sp)
        Text(if (entry.lunch  == true) "✓" else "✗", color = lunchColor,  modifier = Modifier.weight(1f),   fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(if (entry.dinner == true) "✓" else "✗", color = dinnerColor, modifier = Modifier.weight(1f),   fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("%.1f".format(entry.mealUnits ?: 0.0), color = Color.LightGray, modifier = Modifier.weight(0.8f), fontSize = 13.sp)
    }
    Divider(color = Color(0xFF2D2D42).copy(alpha = 0.5f))
}

private fun formatDate(isoDate: String): String {
    return try {
        val d = LocalDate.parse(isoDate)
        "${d.dayOfWeek.name.take(3)}, ${d.dayOfMonth} ${d.month.name.take(3)} ${d.year}"
    } catch (e: Exception) { isoDate }
}

// ─── PDF Generation ──────────────────────────────────────────────────────────
fun generatePdf(
    context: Context,
    entries: List<MealReportEntry>,
    startDate: String,
    endDate: String
): String? {
    return try {
        val pdf = PdfDocument()
        val pageWidth  = 595   // A4 pt
        val pageHeight = 842

        val paintTitle  = Paint().apply { color = AColor.parseColor("#1E1B4B"); textSize = 22f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val paintSub    = Paint().apply { color = AColor.parseColor("#6B7280"); textSize = 13f; isAntiAlias = true }
        val paintHeader = Paint().apply { color = AColor.WHITE; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val paintCell   = Paint().apply { color = AColor.parseColor("#1F2937"); textSize = 11f; isAntiAlias = true }
        val paintGreen  = Paint().apply { color = AColor.parseColor("#16A34A"); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val paintRed    = Paint().apply { color = AColor.parseColor("#DC2626"); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val paintDate   = Paint().apply { color = AColor.parseColor("#1D4ED8"); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val rectBg      = Paint().apply { color = AColor.parseColor("#3B82F6"); style = Paint.Style.FILL }
        val rectLight   = Paint().apply { color = AColor.parseColor("#EEF2FF"); style = Paint.Style.FILL }
        val rectDate    = Paint().apply { color = AColor.parseColor("#DBEAFE"); style = Paint.Style.FILL }
        val linePaint   = Paint().apply { color = AColor.parseColor("#E5E7EB"); strokeWidth = 1f }

        val colX   = floatArrayOf(30f, 120f, 280f, 360f, 440f, 510f)
        val colHdr = arrayOf("Date", "Member Name", "Lunch", "Dinner", "Units", "Notes")
        val rowH   = 24f
        var pageNum = 1
        var y       = 0f

        fun newPage(): Canvas {
            val pi = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
            val page = pdf.startPage(pi)
            val c    = page.canvas
            y = 30f

            // Header band
            c.drawRect(0f, 0f, pageWidth.toFloat(), 80f, rectBg)
            c.drawText("MessMate – Meal Report", 30f, 42f, paintTitle.apply { color = AColor.WHITE })
            c.drawText("Period: $startDate  →  $endDate", 30f, 65f, paintSub.apply { color = AColor.parseColor("#BFDBFE") })

            y = 100f

            // Column headers
            c.drawRect(30f, y, (pageWidth - 30).toFloat(), y + rowH, rectBg)
            colHdr.forEachIndexed { i, h -> c.drawText(h, colX[i] + 4f, y + 16f, paintHeader) }
            y += rowH

            return c
        }

        val pages = mutableListOf<PdfDocument.Page>()
        var canvas = newPage().also {  }
        val startedPage = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 0).create())
        // Restart clean
        pdf.finishPage(startedPage)

        // Build fresh
        val freshPdf = PdfDocument()
        var freshPageNum = 1
        var freshY = 0f

        fun newFreshPage(): Canvas {
            val pi   = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, freshPageNum++).create()
            val page = freshPdf.startPage(pi)
            pages.add(page)
            val c = page.canvas
            freshY = 30f

            c.drawRect(0f, 0f, pageWidth.toFloat(), 80f, rectBg)
            val wPaint = Paint().apply { color = AColor.WHITE; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
            c.drawText("MessMate  ·  Meal Report", 30f, 40f, wPaint)
            val gPaint = Paint().apply { color = AColor.parseColor("#BAE6FD"); textSize = 12f; isAntiAlias = true }
            c.drawText("Period: $startDate  to  $endDate     Generated: ${LocalDate.now()}", 30f, 62f, gPaint)

            freshY = 95f
            // Column header bar
            c.drawRect(30f, freshY, (pageWidth - 30).toFloat(), freshY + rowH, rectBg)
            colHdr.forEachIndexed { i, h -> c.drawText(h, colX[i] + 4f, freshY + 16f, paintHeader) }
            freshY += rowH
            return c
        }

        var c = newFreshPage()

        var rowIndex = 0
        val grouped  = entries.groupBy { it.date }
        grouped.forEach { (date, dayEntries) ->
            // Date separator
            if (freshY + rowH > pageHeight - 40f) {
                freshPdf.finishPage(pages.last())
                c = newFreshPage()
            }
            c.drawRect(30f, freshY, (pageWidth - 30).toFloat(), freshY + rowH, rectDate)
            c.drawText(formatDate(date), colX[0] + 4f, freshY + 16f, paintDate)
            freshY += rowH

            dayEntries.forEach { entry ->
                if (freshY + rowH > pageHeight - 40f) {
                    freshPdf.finishPage(pages.last())
                    c = newFreshPage()
                }
                val bg = if (rowIndex % 2 == 0) AColor.WHITE else AColor.parseColor("#F9FAFB")
                c.drawRect(30f, freshY, (pageWidth - 30).toFloat(), freshY + rowH, Paint().apply { color = bg; style = Paint.Style.FILL })
                c.drawLine(30f, freshY + rowH, (pageWidth - 30).toFloat(), freshY + rowH, linePaint)

                c.drawText("",                      colX[0] + 4f, freshY + 16f, paintCell) // date already shown
                c.drawText(entry.userName,           colX[1] + 4f, freshY + 16f, paintCell)
                val lPaint = if (entry.lunch  == true) paintGreen else paintRed
                val dPaint = if (entry.dinner == true) paintGreen else paintRed
                c.drawText(if (entry.lunch  == true) "Yes" else "No",  colX[2] + 4f, freshY + 16f, lPaint)
                c.drawText(if (entry.dinner == true) "Yes" else "No",  colX[3] + 4f, freshY + 16f, dPaint)
                c.drawText("%.1f".format(entry.mealUnits ?: 0.0),       colX[4] + 4f, freshY + 16f, paintCell)
                freshY += rowH
                rowIndex++
            }
        }

        // Footer total
        if (freshY + 40f > pageHeight - 40f) {
            freshPdf.finishPage(pages.last())
            c = newFreshPage()
        }
        freshY += 10f
        val totPaint = Paint().apply { color = AColor.parseColor("#1D4ED8"); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        c.drawText("Total Entries: ${entries.size}   |   Lunch: ${entries.count { it.lunch == true }}   |   Dinner: ${entries.count { it.dinner == true }}", 30f, freshY + 16f, totPaint)
        freshPdf.finishPage(pages.last())

        // Save
        val dir  = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "MealReport_${startDate}_${endDate}.pdf")
        freshPdf.writeTo(FileOutputStream(file))
        freshPdf.close()

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
