package com.example.nambukhwangdan.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthOnlyDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, monthNumber: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }

    val currentYear = LocalDate.now().year
    val yearRange = (currentYear - 5)..(currentYear + 5)

    val monthNames = remember {
        Month.values().map {
            it.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("월 선택", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("년도 선택", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                YearSelector(
                    currentYear = selectedYear,
                    yearRange = yearRange,
                    onYearSelected = { selectedYear = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("월 선택", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                MonthSelector(
                    monthNames = monthNames,
                    currentMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(selectedYear, selectedMonth)
                    onDismissRequest()
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun YearSelector(
    currentYear: Int,
    yearRange: IntRange,
    onYearSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = yearRange.indexOf(currentYear).coerceAtLeast(0)
    )

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(yearRange.toList()) { year ->
            val isSelected = year == currentYear
            Text(
                text = year.toString(),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onYearSelected(year) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun MonthSelector(
    monthNames: List<String>,
    currentMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentMonth - 1).coerceAtLeast(0)
    )

    val monthNumbers = (1..12).toList()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(monthNumbers) { monthNumber ->
            val isSelected = monthNumber == currentMonth
            Text(
                text = monthNames[monthNumber - 1],
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onMonthSelected(monthNumber) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}