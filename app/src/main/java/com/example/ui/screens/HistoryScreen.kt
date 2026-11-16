package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import com.example.ui.DateRangeFilter
import com.example.ui.HistoryAnalytics
import com.example.ui.HistoryFilters
import com.example.ui.components.ContactAvatar
import com.example.ui.components.formatExactInr
import com.example.ui.components.formatInr
import com.example.ui.theme.PulseAmberPending
import com.example.ui.theme.PulseCrimsonFail
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseDarkSurface
import com.example.ui.theme.PulseDarkTile
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseOnSurfaceMutedDark
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseSoftViolet
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity>,
    filters: HistoryFilters,
    analytics: HistoryAnalytics,
    resolvingTxId: String?,
    onSearchChange: (String) -> Unit,
    onToggleType: (TransactionType) -> Unit,
    onToggleStatus: (TransactionStatus) -> Unit,
    onSelectDateRange: (DateRangeFilter) -> Unit,
    onToggleCategory: (TransactionCategory) -> Unit,
    onClearFilters: () -> Unit,
    onResolvePending: (String) -> Unit,
    onOpenTransaction: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var showStatementsSheet by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val monthGroups = remember(transactions) {
        val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        transactions.groupBy { monthFmt.format(Date(it.timestamp)) }
    }

    val hasActiveFilters = filters.type != null ||
        filters.status != null ||
        filters.dateRange != DateRangeFilter.ALL ||
        filters.category != null

    val activeFilterCount = listOfNotNull(
        filters.type,
        filters.status,
        filters.category,
        filters.dateRange.takeIf { it != DateRangeFilter.ALL }
    ).size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PulseDarkBg),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // 1. Top Help Icon Row + "History" & "My Statements" Header (matching screenshot)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier.testTag("history_help_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "History Help",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )

                        Surface(
                            onClick = { showStatementsSheet = true },
                            color = PulseDarkSurface,
                            shape = RoundedCornerShape(999.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseOutlineDark),
                            modifier = Modifier.testTag("my_statements_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowCircleDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "My Statements",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Pill Search Bar with Divider + Filter Sliders Icon
                    Surface(
                        color = PulseDarkCard,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = PulseOnSurfaceMutedDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (filters.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PulseOnSurfaceMutedDark
                                    )
                                }
                                BasicTextField(
                                    value = filters.searchQuery,
                                    onValueChange = onSearchChange,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(PulseViolet),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("history_search_input")
                                )
                            }
                            if (filters.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                        tint = PulseOnSurfaceMutedDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(PulseOutlineDark)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showFilterSheet = true }
                                    .padding(6.dp)
                                    .testTag("history_filter_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "Filter transactions",
                                    tint = if (hasActiveFilters) Color(0xFFC084FC) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    if (hasActiveFilters) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$activeFilterCount active filter${if (activeFilterCount > 1) "s" else ""} • ${transactions.size} results",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC084FC)
                            )
                            Text(
                                text = "Clear all",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PulseTealSuccess,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onClearFilters)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // 3. Empty State or Monthly Grouped PhonePe List
            if (transactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PulseDarkCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = PulseViolet.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No matching transactions",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search or clearing active filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PulseOnSurfaceMutedDark
                            )
                            if (hasActiveFilters || filters.searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = onClearFilters) {
                                    Text("Clear All Filters")
                                }
                            }
                        }
                    }
                }
            } else {
                monthGroups.forEach { (monthLabel, monthItems) ->
                    val monthSuccessful = monthItems.filter { it.txStatus == TransactionStatus.SUCCESS }
                    val monthCredits = monthSuccessful
                        .filter { it.txType == TransactionType.RECEIVED || it.txType == TransactionType.CASHBACK }
                        .sumOf { it.amount }
                    val monthDebits = monthSuccessful
                        .filter { it.txType == TransactionType.PAID }
                        .sumOf { it.amount }
                    val monthNet = monthCredits - monthDebits
                    val netText = if (monthNet >= 0) {
                        "+ ${formatInr(monthNet, showDecimals = false)}"
                    } else {
                        "- ${formatInr(abs(monthNet), showDecimals = false)}"
                    }

                    // Month Header Strip matching "November 2026    + ₹6,001 >"
                    item(key = "month_$monthLabel") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PulseDarkCard)
                                .clickable { showStatementsSheet = true }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFFD4D4DC)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = netText,
                                    style = PulseMonoStyles.amountSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    color = if (monthNet >= 0) PulseTealSuccess else Color(0xFFD4D4DC)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "View monthly statement",
                                    tint = PulseOnSurfaceMutedDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    itemsIndexed(monthItems, key = { _, tx -> tx.id }) { index, tx ->
                        PhonePeHistoryRow(
                            transaction = tx,
                            isResolving = resolvingTxId == tx.id,
                            onClick = { onOpenTransaction(tx.id) },
                            onResolvePending = { onResolvePending(tx.id) }
                        )
                        if (index < monthItems.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.7.dp,
                                color = Color(0xFF1F1F26),
                                modifier = Modifier.padding(start = 76.dp, end = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet (triggered from the Tune slider icon inside Search bar)
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = PulseDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Transactions", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    if (hasActiveFilters) {
                        TextButton(onClick = {
                            onClearFilters()
                            showFilterSheet = false
                        }) {
                            Text("Reset", color = PulseTealSuccess)
                        }
                    }
                }

                Text("Time Range", style = MaterialTheme.typography.labelLarge, color = PulseOnSurfaceMutedDark)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRangeFilter.entries.forEach { range ->
                        FilterChip(
                            selected = filters.dateRange == range,
                            onClick = { onSelectDateRange(range) },
                            label = { Text(range.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Payment Type", style = MaterialTheme.typography.labelLarge, color = PulseOnSurfaceMutedDark)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = filters.type == type,
                            onClick = { onToggleType(type) },
                            label = { Text(type.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseSoftViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_type_${type.name}")
                        )
                    }
                }

                Text("Status", style = MaterialTheme.typography.labelLarge, color = PulseOnSurfaceMutedDark)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionStatus.entries.forEach { status ->
                        FilterChip(
                            selected = filters.status == status,
                            onClick = { onToggleStatus(status) },
                            label = { Text(status.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseSoftViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_${status.name}")
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelLarge, color = PulseOnSurfaceMutedDark)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = filters.category == cat,
                            onClick = { onToggleCategory(cat) },
                            label = { Text(cat.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseSoftViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PulseViolet),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Filters", color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // My Statements & Cashflow Analytics Sheet
    if (showStatementsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatementsSheet = false },
            containerColor = PulseDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("My Statements & Summary", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    text = "${analytics.count} transactions in current view",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PulseOnSurfaceMutedDark
                )

                Surface(
                    color = PulseDarkTile,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Paid", style = MaterialTheme.typography.labelMedium, color = PulseOnSurfaceMutedDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatInr(analytics.totalPaid), style = PulseMonoStyles.amountMedium, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Received", style = MaterialTheme.typography.labelMedium, color = PulseOnSurfaceMutedDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("+${formatInr(analytics.totalReceived)}", style = PulseMonoStyles.amountMedium, color = PulseTealSuccess)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Flow", style = MaterialTheme.typography.labelMedium, color = PulseOnSurfaceMutedDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            val netSign = if (analytics.netFlow >= 0) "+" else "-"
                            Text(
                                text = "$netSign${formatInr(abs(analytics.netFlow))}",
                                style = PulseMonoStyles.amountMedium,
                                color = if (analytics.netFlow >= 0) PulseTealSuccess else PulseCrimsonFail
                            )
                        }
                    }
                }

                if (analytics.categoryBreakdown.isNotEmpty() && analytics.totalPaid > 0.0) {
                    Text("Spending Breakdown", style = MaterialTheme.typography.titleSmall, color = Color.White)
                    analytics.categoryBreakdown.take(4).forEach { (cat, amt) ->
                        val ratio = (amt / analytics.totalPaid).toFloat().coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.label, style = MaterialTheme.typography.labelMedium, color = Color.White)
                                Text(formatInr(amt), style = PulseMonoStyles.codeLabel, color = PulseOnSurfaceMutedDark)
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = PulseViolet,
                                trackColor = PulseDarkSurface
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        val summary = "PhonePe Statement Summary\n" +
                            "Transactions: ${analytics.count}\n" +
                            "Total Paid: ${formatExactInr(analytics.totalPaid)}\n" +
                            "Total Received: ${formatExactInr(analytics.totalReceived)}"
                        clipboard.setText(AnnotatedString(summary))
                        onShowMessage("Statement summary copied")
                        showStatementsSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Statement Summary")
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("Transaction History Help", color = Color.White) },
            text = {
                Text(
                    text = "Tap any transaction to inspect its UTR reference, source bank, and receipt actions. Use the search bar or filter sliders to narrow by status, type, date range, or category.",
                    color = PulseOnSurfaceMutedDark
                )
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun PhonePeHistoryRow(
    transaction: TransactionEntity,
    isResolving: Boolean,
    onClick: () -> Unit,
    onResolvePending: () -> Unit
) {
    val isCredit = transaction.txType == TransactionType.RECEIVED ||
        transaction.txType == TransactionType.CASHBACK

    val directionLabel = when (transaction.txType) {
        TransactionType.PAID -> "Paid to"
        TransactionType.RECEIVED -> "Received from"
        TransactionType.CASHBACK -> "Cashback from"
        TransactionType.SELF_TRANSFER -> "Transfer to"
    }

    val amountPrefix = when {
        transaction.txStatus == TransactionStatus.FAILED -> ""
        isCredit -> "+ "
        else -> ""
    }
    val amountColor = when {
        transaction.txStatus == TransactionStatus.FAILED -> PulseCrimsonFail
        isCredit -> PulseTealSuccess
        else -> Color.White
    }

    val shortDate = remember(transaction.timestamp) {
        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    val accountTag = if (isCredit) transaction.destinationAccountLabel else transaction.sourceAccountLabel
    val (bankShort, bankBgColor) = remember(accountTag) {
        when {
            accountTag.contains("HDFC", ignoreCase = true) -> "H" to Color(0xFF1D4ED8)
            accountTag.contains("SBI", ignoreCase = true) -> "S" to Color(0xFF0284C7)
            accountTag.contains("Wallet", ignoreCase = true) -> "पे" to Color(0xFF5F259F)
            else -> "B" to Color(0xFFEA580C)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("history_tx_${transaction.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Circle: Diagonal NorthEast arrow for debits, peer avatar for credits
            if (isCredit) {
                ContactAvatar(
                    name = transaction.counterpartyName,
                    colorHex = if (transaction.txType == TransactionType.CASHBACK) 0xFF16C784 else 0xFF38BDF8,
                    size = 44.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF282830)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NorthEast,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: "Paid to" / Counterparty Name / Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = directionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = PulseOnSurfaceMutedDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (transaction.txType == TransactionType.PAID) {
                        transaction.counterpartyName.uppercase()
                    } else {
                        transaction.counterpartyName
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = shortDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = PulseOnSurfaceMutedDark
                    )
                    if (transaction.txStatus == TransactionStatus.FAILED) {
                        Text(
                            text = "• Failed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PulseCrimsonFail
                        )
                    } else if (transaction.txStatus == TransactionStatus.PENDING) {
                        Text(
                            text = "• Pending",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PulseAmberPending
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Amount + "Debited from [bank]" / "Credited to [bank]"
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${formatInr(transaction.amount, showDecimals = false)}",
                    style = PulseMonoStyles.amountSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isCredit) "Credited to" else "Debited from",
                        style = MaterialTheme.typography.labelMedium,
                        color = PulseOnSurfaceMutedDark
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(bankBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bankShort,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (transaction.txStatus == TransactionStatus.PENDING) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Awaiting bank confirmation",
                    style = MaterialTheme.typography.labelSmall,
                    color = PulseAmberPending
                )
                Surface(
                    onClick = { if (!isResolving) onResolvePending() },
                    color = PulseAmberPending.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isResolving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = PulseAmberPending
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = PulseAmberPending,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "Check Status",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PulseAmberPending
                        )
                    }
                }
            }
        }
    }
}
