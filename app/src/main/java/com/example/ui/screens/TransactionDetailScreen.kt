package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import com.example.ui.components.formatExactInr
import com.example.ui.components.formatInr
import com.example.ui.theme.PulseAmberPending
import com.example.ui.theme.PulseCrimsonFail
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseOnSurfaceMutedDark
import com.example.ui.theme.PulseViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    isResolving: Boolean,
    onBack: () -> Unit,
    onCheckBalance: () -> Unit,
    onViewHistory: (String) -> Unit,
    onResolvePending: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var detailsExpanded by remember { mutableStateOf(true) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val isCredit = transaction.txType == TransactionType.RECEIVED ||
        transaction.txType == TransactionType.CASHBACK

    val headerColor = when (transaction.txStatus) {
        TransactionStatus.SUCCESS -> Color(0xFF0E7C3A)
        TransactionStatus.PENDING -> Color(0xFFB45309)
        TransactionStatus.FAILED -> Color(0xFFB91C1C)
    }

    val headerTitle = when (transaction.txStatus) {
        TransactionStatus.SUCCESS -> "Transaction Successful"
        TransactionStatus.PENDING -> "Transaction Pending"
        TransactionStatus.FAILED -> "Transaction Failed"
    }

    val formattedDateTime = remember(transaction.timestamp) {
        SimpleDateFormat("d MMMM yyyy 'at' h:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    val directionHeader = when (transaction.txType) {
        TransactionType.RECEIVED -> "Received from"
        TransactionType.CASHBACK -> "Cashback from"
        TransactionType.PAID -> "Paid to"
        TransactionType.SELF_TRANSFER -> "Transferred to"
    }

    val rawAccountLabel = if (isCredit) transaction.destinationAccountLabel else transaction.sourceAccountLabel
    val maskedAccountDisplay = remember(rawAccountLabel) {
        val lastDigits = rawAccountLabel.filter { it.isDigit() }.takeLast(4)
        if (lastDigits.length == 4) "XXXXXX$lastDigits" else rawAccountLabel
    }

    val bankAccent = remember(rawAccountLabel) {
        when {
            rawAccountLabel.contains("HDFC", ignoreCase = true) -> Color(0xFF1D4ED8)
            rawAccountLabel.contains("SBI", ignoreCase = true) -> Color(0xFF0284C7)
            rawAccountLabel.contains("Wallet", ignoreCase = true) -> Color(0xFF5F259F)
            else -> Color(0xFFF59E0B)
        }
    }

    val amountDisplay = formatInr(transaction.amount, showDecimals = false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PulseDarkBg)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Full-bleed Status Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 14.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("tx_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.5.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDateTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // 2. Scrollable Dark Content matching PhonePe Receipt Info
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Transaction Info Card
            Surface(
                color = PulseDarkCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Counterparty Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = directionHeader,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF9366D6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCredit) Icons.Filled.SouthWest else Icons.Filled.NorthEast,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.counterpartyName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.5.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = transaction.counterpartyUpiId,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = PulseOnSurfaceMutedDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = amountDisplay,
                                style = PulseMonoStyles.amountSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFF282832),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    // Expandable Payment Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { detailsExpanded = !detailsExpanded }
                                .padding(vertical = 4.dp)
                                .testTag("tx_payment_details_toggle"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2B213E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Payment Details",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.5.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Icon(
                                imageVector = if (detailsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (detailsExpanded) "Collapse details" else "Expand details",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        AnimatedVisibility(visible = detailsExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (transaction.txStatus == TransactionStatus.FAILED && !transaction.failureReason.isNullOrBlank()) {
                                    Surface(
                                        color = PulseCrimsonFail.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = transaction.failureReason,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFFCA5A5),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (transaction.txStatus == TransactionStatus.PENDING) {
                                    Surface(
                                        color = PulseAmberPending.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Bank confirmation pending",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = PulseAmberPending
                                            )
                                            Button(
                                                onClick = { onResolvePending(transaction.id) },
                                                enabled = !isResolving,
                                                colors = ButtonDefaults.buttonColors(containerColor = PulseAmberPending),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                if (isResolving) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color.White
                                                    )
                                                } else {
                                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Check Status", color = Color.White)
                                            }
                                        }
                                    }
                                }

                                // PhonePe Transaction ID block
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "PhonePe Transaction ID",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                        color = PulseOnSurfaceMutedDark
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = transaction.upiTransactionId,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 15.5.sp
                                            ),
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(transaction.upiTransactionId))
                                                onShowMessage("Transaction ID copied")
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("copy_tx_id_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ContentCopy,
                                                contentDescription = "Copy Transaction ID",
                                                tint = Color(0xFFB374FF),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Credited to / Debited from Account & UTR block
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = if (isCredit) "Credited to" else "Debited from",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                        color = PulseOnSurfaceMutedDark
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AccountBalance,
                                                contentDescription = null,
                                                tint = bankAccent,
                                                modifier = Modifier.size(21.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = maskedAccountDisplay,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 16.sp
                                                ),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "UTR: ${transaction.utrNumber}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                                color = PulseOnSurfaceMutedDark
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = amountDisplay,
                                                style = PulseMonoStyles.amountSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 15.5.sp
                                                ),
                                                color = Color.White
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(transaction.utrNumber))
                                                    onShowMessage("UTR ${transaction.utrNumber} copied")
                                                },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("copy_utr_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ContentCopy,
                                                    contentDescription = "Copy UTR",
                                                    tint = Color(0xFFB374FF),
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFF282832),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    // 3 Quick Action Circles Row: Check Balance | View History | Share Receipt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickReceiptActionButton(
                            icon = Icons.Filled.AccountBalance,
                            label = "Check Balance",
                            onClick = onCheckBalance,
                            testTag = "tx_detail_check_balance",
                            modifier = Modifier.weight(1f)
                        )
                        QuickReceiptActionButton(
                            icon = Icons.Filled.SwapHoriz,
                            label = "View History",
                            onClick = { onViewHistory(transaction.counterpartyName) },
                            testTag = "tx_detail_view_history",
                            modifier = Modifier.weight(1f)
                        )
                        QuickReceiptActionButton(
                            icon = Icons.Filled.Share,
                            label = "Share Receipt",
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "PhonePe Transaction Receipt\n" +
                                            "Status: ${transaction.txStatus.label}\n" +
                                            "Amount: ${formatExactInr(transaction.amount)}\n" +
                                            "$directionHeader: ${transaction.counterpartyName} (${transaction.counterpartyUpiId})\n" +
                                            "PhonePe Transaction ID: ${transaction.upiTransactionId}\n" +
                                            "UTR: ${transaction.utrNumber}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                            },
                            testTag = "tx_detail_share_receipt",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Contact PhonePe Support Card
            Surface(
                onClick = { showSupportDialog = true },
                color = PulseDarkCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_phonepe_support_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Contact PhonePe Support",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.5.sp
                            ),
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PulseOnSurfaceMutedDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Powered by UPI • AXIS BANK
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Powered by",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    color = PulseOnSurfaceMutedDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "UPI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 18.sp
                                ),
                                color = Color(0xFF8E8E98)
                            )
                            MiniUpiChevrons()
                        }
                        Text(
                            text = "UNIFIED PAYMENTS INTERFACE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 5.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF70707A)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "▲",
                            fontSize = 13.sp,
                            color = Color(0xFF97144D)
                        )
                        Text(
                            text = "AXIS BANK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF70707A)
                        )
                    }
                }
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("PhonePe Support", color = Color.White) },
            text = {
                Text(
                    text = "Need help with transaction ${transaction.upiTransactionId} (UTR: ${transaction.utrNumber})?\n\nYou can copy the UTR reference above and raise a dispute with your bank or UPI support desk.",
                    color = PulseOnSurfaceMutedDark
                )
            },
            confirmButton = {
                Button(onClick = {
                    clipboard.setText(AnnotatedString("${transaction.upiTransactionId} | UTR: ${transaction.utrNumber}"))
                    onShowMessage("Reference details copied for support")
                    showSupportDialog = false
                }) {
                    Text("Copy Reference")
                }
            }
        )
    }
}

@Composable
private fun QuickReceiptActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF4B287B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFC4B5FD),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MiniUpiChevrons() {
    Canvas(modifier = Modifier.size(width = 13.dp, height = 16.dp)) {
        val w = size.width
        val h = size.height
        val saffron = Path().apply {
            moveTo(0f, h * 0.12f)
            lineTo(w * 0.56f, h * 0.5f)
            lineTo(0f, h * 0.88f)
            lineTo(w * 0.22f, h * 0.88f)
            lineTo(w * 0.78f, h * 0.5f)
            lineTo(w * 0.22f, h * 0.12f)
            close()
        }
        val green = Path().apply {
            moveTo(w * 0.28f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.5f)
            lineTo(w * 0.28f, h * 0.88f)
            lineTo(w * 0.46f, h * 0.88f)
            lineTo(w, h * 0.5f)
            lineTo(w * 0.46f, h * 0.12f)
            close()
        }
        drawPath(saffron, color = Color(0xFFEA580C))
        drawPath(green, color = Color(0xFF16A34A))
    }
}
