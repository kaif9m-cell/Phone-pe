package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BankAccountEntity
import com.example.data.local.ContactEntity
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.ui.TransferEntryMode
import com.example.ui.UserProfileState
import com.example.ui.components.CompactTransactionRow
import com.example.ui.components.ContactAvatar
import com.example.ui.components.formatExactInr
import com.example.ui.theme.PulseCashbackOrange
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseDarkSurface
import com.example.ui.theme.PulseDarkTile
import com.example.ui.theme.PulseElectricViolet
import com.example.ui.theme.PulseGoldAvatar
import com.example.ui.theme.PulseHeroBottom
import com.example.ui.theme.PulseHeroTop
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseReferBg
import com.example.ui.theme.PulseReferText
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@Composable
fun HomeScreen(
    userProfile: UserProfileState,
    accounts: List<BankAccountEntity>,
    contacts: List<ContactEntity>,
    recentTransactions: List<TransactionEntity>,
    isBalanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    onStartTransfer: (TransferEntryMode, ContactEntity?) -> Unit,
    onStartBiller: (String, String, String, String, TransactionCategory) -> Unit,
    onOpenScan: () -> Unit,
    onOpenProfileQr: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTransactionDetail: (String) -> Unit,
    onClaimReferral: () -> Unit,
    onSimulateIncoming: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreBillers by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val totalBalance = accounts.sumOf { it.balance }
    val primaryAccount = accounts.firstOrNull { it.isPrimary } ?: accounts.firstOrNull()

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
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            // 1. PhonePe-inspired Deep Violet Gradient Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PulseHeroTop, PulseHeroBottom)
                            )
                        )
                ) {
                    // Decorative floating coins & banknotes artwork at bottom of hero
                    HeroPaymentsBackdrop(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .align(Alignment.BottomCenter)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        // Top bar: Golden Avatar + Overlapping QR mini badge | Demo Balance pill | ? Help button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onOpenProfileQr() }
                                    .testTag("home_profile_avatar")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PulseGoldAvatar),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "*",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF1A1404)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-2).dp, y = (-2).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF141418))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCode2,
                                        contentDescription = "Open Profile & QR",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showHelpDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("home_help_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "Help & Support",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Center Hero Headline + Pay Now -> Pill
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Send money to any UPI app",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Make your 1st payment",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 27.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { onStartTransfer(TransferEntryMode.MOBILE, null) }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .testTag("hero_pay_now_button")
                            ) {
                                Text(
                                    text = "Pay Now",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Pay Now",
                                        tint = PulseHeroBottom,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(42.dp))
                    }
                }
            }

            // 2. Money Transfers Section (Dark Obsidian Surface + Vibrant Violet Circles)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseDarkSurface)
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Money Transfers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )

                        Surface(
                            onClick = onClaimReferral,
                            color = PulseReferBg,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("refer_bonus_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(PulseCashbackOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Refer → ₹200",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = PulseReferText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VioletTransferCircleTile(
                            icon = Icons.Filled.PhoneAndroid,
                            label = "To Mobile\nNumber",
                            showOnlineDot = true,
                            badgeText = null,
                            testTag = "action_to_mobile",
                            onClick = { onStartTransfer(TransferEntryMode.MOBILE, null) },
                            modifier = Modifier.weight(1f)
                        )
                        VioletTransferCircleTile(
                            icon = Icons.Filled.AccountBalance,
                            label = "To Bank &\nSelf A/c",
                            showOnlineDot = false,
                            badgeText = null,
                            testTag = "action_to_upi",
                            onClick = { onStartTransfer(TransferEntryMode.UPI_OR_BANK, null) },
                            modifier = Modifier.weight(1f)
                        )
                        VioletTransferCircleTile(
                            icon = Icons.Filled.AccountBalanceWallet,
                            label = "PhonePe\nWallet",
                            showOnlineDot = false,
                            badgeText = "Cashback",
                            testTag = "action_to_self",
                            onClick = { onStartTransfer(TransferEntryMode.SELF, null) },
                            modifier = Modifier.weight(1f)
                        )
                        VioletTransferCircleTile(
                            icon = Icons.Filled.CurrencyRupee,
                            label = "Check\nBalance",
                            showOnlineDot = false,
                            badgeText = null,
                            testTag = "action_check_balance",
                            onClick = onOpenWallet,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Horizontal Promo Pills Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            DarkPromoChip(
                                icon = Icons.Filled.CreditCard,
                                iconTint = Color(0xFF38BDF8),
                                text = "Get 4 free Lounge Access",
                                onClick = onOpenProfileQr
                            )
                        }
                        item {
                            DarkPromoChip(
                                icon = Icons.Filled.CreditCard,
                                iconTint = PulseGoldAvatar,
                                text = "Lifetime-Free HDFC Card",
                                onClick = onOpenProfileQr
                            )
                        }
                        item {
                            DarkPromoChip(
                                icon = Icons.Filled.Bolt,
                                iconTint = PulseTealSuccess,
                                text = "Scan ₹100+ for Cashback",
                                onClick = onOpenScan
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = PulseDarkBg)
            }

            // 3. Recharge & Bills Dark Grid Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseDarkSurface)
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Recharge & Bills",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DarkBillerTile(
                            icon = Icons.Filled.PhoneIphone,
                            iconTint = Color(0xFF60A5FA),
                            label = "Mobile\nRecharge",
                            testTag = "biller_mobile",
                            onClick = {
                                onStartBiller(
                                    "Jio Prepaid Recharge",
                                    "jio.recharge@icici",
                                    "299",
                                    "28 Days Unlimited 5G Plan",
                                    TransactionCategory.BILLS
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DarkBillerTile(
                            icon = Icons.Filled.Home,
                            iconTint = Color(0xFFF87171),
                            label = "Rent via\nCredit Card",
                            testTag = "biller_rent",
                            onClick = {
                                onStartBiller(
                                    "Urban Nest Housing Rent",
                                    "urbannest.rent@okhdfc",
                                    "14500",
                                    "Monthly Apartment Rent",
                                    TransactionCategory.BILLS
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DarkBillerTile(
                            icon = Icons.Filled.Lightbulb,
                            iconTint = PulseGoldAvatar,
                            label = "Electricity\nBill",
                            testTag = "biller_electricity",
                            onClick = {
                                onStartBiller(
                                    "BESCOM Power Utility",
                                    "bescom.bills@sbi",
                                    "1450",
                                    "Monthly Electricity Bill",
                                    TransactionCategory.BILLS
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DarkBillerTile(
                            icon = Icons.Filled.ReceiptLong,
                            iconTint = Color(0xFF34D399),
                            label = "Loan\nRepayment",
                            testTag = "biller_loan",
                            onClick = {
                                onStartBiller(
                                    "HDFC EMI AutoPay",
                                    "hdfc.emi@okhdfc",
                                    "3200",
                                    "Monthly EMI Payment",
                                    TransactionCategory.BILLS
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = showMoreBillers) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DarkBillerTile(
                                    icon = Icons.Filled.Wifi,
                                    iconTint = Color(0xFFA78BFA),
                                    label = "Broadband\n& Wi-Fi",
                                    testTag = "biller_broadband",
                                    onClick = {
                                        onStartBiller(
                                            "Airtel Fiber Broadband",
                                            "airtel.bbps@icici",
                                            "999",
                                            "Fiber Wi-Fi Monthly Plan",
                                            TransactionCategory.BILLS
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                DarkBillerTile(
                                    icon = Icons.Filled.DirectionsCar,
                                    iconTint = Color(0xFFFB923C),
                                    label = "FASTag\nRecharge",
                                    testTag = "biller_fastag",
                                    onClick = {
                                        onStartBiller(
                                            "NHAI FASTag Toll",
                                            "fastag.nhai@hdfc",
                                            "500",
                                            "Highway Toll Wallet Top-up",
                                            TransactionCategory.BILLS
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                DarkBillerTile(
                                    icon = Icons.Filled.ElectricBolt,
                                    iconTint = PulseTealSuccess,
                                    label = "DTH &\nCable",
                                    testTag = "biller_dth",
                                    onClick = {
                                        onStartBiller(
                                            "Tata Play DTH",
                                            "tataplay@icici",
                                            "350",
                                            "Monthly HD Pack",
                                            TransactionCategory.BILLS
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            onClick = { showMoreBillers = !showMoreBillers },
                            color = PulseDarkTile,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("home_more_billers_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (showMoreBillers) "Less" else "More",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFC084FC)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = PulseDarkBg)
            }

            // 4. Quick Pay Contacts + Recent Activity Preview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseDarkSurface)
                        .padding(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Pay People",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        TextButton(onClick = { onStartTransfer(TransferEntryMode.MOBILE, null) }) {
                            Text("See All", color = Color(0xFFC084FC))
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(contacts.take(8), key = { it.id }) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onStartTransfer(TransferEntryMode.MOBILE, contact)
                                    }
                                    .padding(vertical = 4.dp)
                                    .testTag("quick_contact_${contact.id}")
                            ) {
                                ContactAvatar(
                                    name = contact.name,
                                    colorHex = contact.avatarColorHex,
                                    isVerifiedMerchant = contact.isVerifiedMerchant,
                                    size = 50.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = contact.name.split(" ").first(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = PulseOutlineDark,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        TextButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.testTag("home_view_all_history")
                        ) {
                            Text("View All", color = Color(0xFFC084FC))
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        recentTransactions.take(4).forEach { tx ->
                            CompactTransactionRow(
                                transaction = tx,
                                onClick = { onOpenTransactionDetail(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("PhonePe Help & Support", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Signed in as ${userProfile.displayName} (${userProfile.phoneNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Primary UPI: ${primaryAccount?.upiId ?: "arjun.mehta@okhdfc"}",
                        style = PulseMonoStyles.codeLabel,
                        color = Color(0xFFC084FC)
                    )
                    Text(
                        text = "• Default UPI PIN: 1234\n• Tap your avatar (top-left) for Profile & Receive QR\n• Tap Scan at the bottom center to pay any UPI QR",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHelpDialog = false
                        onOpenWallet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseViolet)
                ) {
                    Text("View Accounts", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun HeroPaymentsBackdrop(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Soft floating pink-violet banknotes
            rotate(degrees = -14f, pivot = Offset(w * 0.22f, h * 0.65f)) {
                drawRoundRect(
                    color = Color(0xFFDB7093).copy(alpha = 0.55f),
                    topLeft = Offset(w * 0.14f, h * 0.50f),
                    size = Size(58.dp.toPx(), 30.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
            rotate(degrees = 12f, pivot = Offset(w * 0.48f, h * 0.68f)) {
                drawRoundRect(
                    color = Color(0xFFEC4899).copy(alpha = 0.48f),
                    topLeft = Offset(w * 0.42f, h * 0.54f),
                    size = Size(54.dp.toPx(), 28.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
            // Golden coins
            drawCircle(
                color = PulseGoldAvatar.copy(alpha = 0.85f),
                radius = 12.dp.toPx(),
                center = Offset(w * 0.08f, h * 0.58f)
            )
            drawCircle(
                color = PulseGoldAvatar.copy(alpha = 0.80f),
                radius = 10.dp.toPx(),
                center = Offset(w * 0.34f, h * 0.68f)
            )
            drawCircle(
                color = PulseGoldAvatar.copy(alpha = 0.85f),
                radius = 11.dp.toPx(),
                center = Offset(w * 0.61f, h * 0.64f)
            )
        }

        // Right-side stylized phone with green check badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE9D5FF))
                    .border(2.dp, Color(0xFFA855F7), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PulseTealSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VioletTransferCircleTile(
    icon: ImageVector,
    label: String,
    showOnlineDot: Boolean,
    badgeText: String?,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PulseElectricViolet, PulseViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label.replace("\n", " "),
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }

            if (showOnlineDot) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(PulseTealSuccess)
                        .border(2.dp, PulseDarkSurface, CircleShape)
                )
            }

            if (badgeText != null) {
                Surface(
                    color = PulseCashbackOrange,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-2).dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun DarkPromoChip(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = PulseDarkCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseOutlineDark)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PulseDarkTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun DarkBillerTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PulseDarkTile)
                .border(1.dp, PulseOutlineDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label.replace("\n", " "),
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
