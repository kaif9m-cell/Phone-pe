package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import com.example.ui.theme.PulseAmberPending
import com.example.ui.theme.PulseAmberSoft
import com.example.ui.theme.PulseCrimsonFail
import com.example.ui.theme.PulseCrimsonSoft
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseTealSoft
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val indianLocale = Locale("en", "IN")

fun formatInr(amount: Double, showDecimals: Boolean = true): String {
    val formatter = NumberFormat.getCurrencyInstance(indianLocale).apply {
        maximumFractionDigits = if (showDecimals && amount % 1.0 != 0.0) 2 else 0
        minimumFractionDigits = if (showDecimals && amount % 1.0 != 0.0) 2 else 0
    }
    return formatter.format(amount)
}

fun formatExactInr(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(indianLocale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    return formatter.format(amount)
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDayHeader(millis: Long): String {
    val now = System.currentTimeMillis()
    val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val targetDay = dayFmt.format(Date(millis))
    val today = dayFmt.format(Date(now))
    val yesterday = dayFmt.format(Date(now - 86_400_000L))
    return when (targetDay) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }
}

@Composable
fun ContactAvatar(
    name: String,
    colorHex: Long,
    isVerifiedMerchant: Boolean = false,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val initials = name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "UP" }

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(colorHex).copy(alpha = 0.16f))
                .border(1.5.dp, Color(colorHex).copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(colorHex)
            )
        }
        if (isVerifiedMerchant) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified Merchant",
                tint = PulseTealSuccess,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.dp)
            )
        }
    }
}

@Composable
fun TransactionStatusChip(
    status: TransactionStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = when (status) {
        TransactionStatus.SUCCESS -> Triple(PulseTealSoft, PulseTealSuccess, Icons.Filled.CheckCircle)
        TransactionStatus.PENDING -> Triple(PulseAmberSoft, PulseAmberPending, Icons.Filled.Schedule)
        TransactionStatus.FAILED -> Triple(PulseCrimsonSoft, PulseCrimsonFail, Icons.Filled.Error)
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.label,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = fg
            )
        }
    }
}

@Composable
fun CompactTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCredit = transaction.txType == TransactionType.RECEIVED ||
        transaction.txType == TransactionType.CASHBACK
    val amountColor = when {
        transaction.txStatus == TransactionStatus.FAILED -> PulseCrimsonFail
        isCredit -> PulseTealSuccess
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when {
        transaction.txStatus == TransactionStatus.FAILED -> ""
        isCredit -> "+"
        transaction.txType == TransactionType.SELF_TRANSFER -> ""
        else -> "-"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCredit) PulseTealSuccess.copy(alpha = 0.14f)
                        else PulseViolet.copy(alpha = 0.16f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.txType == TransactionType.CASHBACK) Icons.Filled.Bolt
                    else if (isCredit) Icons.Filled.SouthWest
                    else if (transaction.txType == TransactionType.SELF_TRANSFER) Icons.Filled.SwapHoriz
                    else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isCredit) PulseTealSuccess else Color(0xFFC084FC),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.counterpartyName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTimestamp(transaction.timestamp)} • ${transaction.txMethod.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$prefix${formatInr(transaction.amount)}",
                style = PulseMonoStyles.amountSmall.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (transaction.txStatus != TransactionStatus.SUCCESS) {
                TransactionStatusChip(status = transaction.txStatus)
            } else {
                Text(
                    text = transaction.txType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
