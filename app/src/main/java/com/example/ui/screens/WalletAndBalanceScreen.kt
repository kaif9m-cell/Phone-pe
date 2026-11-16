package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BankAccountEntity
import com.example.security.BiometricAuthHelper
import com.example.security.BiometricCapability
import com.example.security.findFragmentActivity
import com.example.ui.components.formatExactInr
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseDarkTile
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseOnSurfaceMutedDark
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletAndBalanceScreen(
    accounts: List<BankAccountEntity>,
    isBalanceVisible: Boolean,
    onBack: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    onSetPrimaryAccount: (String) -> Unit,
    onAddFunds: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val bioCapability = remember(context) { BiometricAuthHelper.checkCapability(context) }

    val bankAccounts = remember(accounts) { accounts.filter { it.id != "acc_wallet" } }
    val walletAccount = remember(accounts) { accounts.firstOrNull { it.id == "acc_wallet" } }

    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var checkedAccountIds by remember { mutableStateOf(setOf<String>()) }
    var walletActivated by remember { mutableStateOf(false) }
    var rupayLinked by remember { mutableStateOf(false) }
    var showAddMethodSheet by remember { mutableStateOf(false) }
    var showSipDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PulseDarkBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Top Bar: Back Arrow + "Check Balance" + Help (?)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("wallet_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Check Balance",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                        }

                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "Check Balance Help",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // 2. Daily Mutual Fund SIP Promo Banner Card matching screenshot
                item {
                    Surface(
                        onClick = { showSipDialog = true },
                        color = PulseDarkCard,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("sip_promo_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Daily Mutual Fund SIP",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = PulseDarkCard,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Invest with ₹10",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PulseOnSurfaceMutedDark
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            TenRupeeNoteIllustration()
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 3. Linked Bank Account Rows
                items(bankAccounts, key = { it.id }) { acc ->
                    val isChecked = checkedAccountIds.contains(acc.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAccountId = acc.id }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("bank_card_${acc.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(acc.accentHex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${acc.bankName} - ${acc.maskedNumber.takeLast(4)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = if (isChecked && isBalanceVisible) {
                                        "Available Balance: ${formatExactInr(acc.balance)}"
                                    } else {
                                        "Bank Account"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isChecked) PulseTealSuccess else PulseOnSurfaceMutedDark
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Check balance",
                            tint = PulseOnSurfaceMutedDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 4. PhonePe Wallet Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (walletAccount != null) {
                                    selectedAccountId = walletAccount.id
                                } else {
                                    walletActivated = true
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("check_balance_wallet_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(top = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFFE67E22),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "PhonePe Wallet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = if (walletActivated && walletAccount != null) {
                                        "Balance: ${formatExactInr(walletAccount.balance)}"
                                    } else {
                                        "Pay on any QR up to ₹2 lakh"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (walletActivated) PulseTealSuccess else PulseOnSurfaceMutedDark
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = PulseOnSurfaceMutedDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Upgrade now to pay on any QR",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PulseOnSurfaceMutedDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (walletActivated) "Check" else "Activate",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFFC084FC),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    walletActivated = true
                                    if (walletAccount != null) {
                                        selectedAccountId = walletAccount.id
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // 5. RuPay on UPI Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rupayLinked = !rupayLinked }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("check_balance_rupay_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "RuPay on UPI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                if (rupayLinked) {
                                    Text(
                                        text = "RuPay Credit ••7712 Linked",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PulseTealSuccess
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (rupayLinked) "Linked" else "Link Now",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = if (rupayLinked) PulseTealSuccess else Color(0xFFC084FC),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { rupayLinked = !rupayLinked }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // 6. Add new payment method Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddMethodSheet = true }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("add_payment_method_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircleOutline,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Add new payment method",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Bank Account, UPI Lite & more",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PulseOnSurfaceMutedDark
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = PulseOnSurfaceMutedDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 7. Bottom "POWERED BY UPI" Footer Badge matching screenshot
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "POWERED BY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = PulseOnSurfaceMutedDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "UPI",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 26.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    UpiTriColorChevrons()
                }
                Text(
                    text = "UNIFIED PAYMENTS INTERFACE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.5.sp,
                        letterSpacing = 0.9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }

    // Account Balance Check & Management Bottom Sheet
    if (selectedAccount != null) {
        val isChecked = checkedAccountIds.contains(selectedAccount.id)
        ModalBottomSheet(
            onDismissRequest = { selectedAccountId = null },
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
                    Column {
                        Text(
                            text = "${selectedAccount.bankName} ${selectedAccount.maskedNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "${selectedAccount.accountType} • ${selectedAccount.upiId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PulseOnSurfaceMutedDark
                        )
                    }
                    if (selectedAccount.isPrimary) {
                        Surface(
                            color = PulseTealSuccess.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PulseTealSuccess, modifier = Modifier.size(14.dp))
                                Text("Primary", style = MaterialTheme.typography.labelSmall, color = PulseTealSuccess)
                            }
                        }
                    }
                }

                Surface(
                    color = PulseDarkTile,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Available Balance", style = MaterialTheme.typography.labelMedium, color = PulseOnSurfaceMutedDark)
                        if (isChecked) {
                            Text(
                                text = if (isBalanceVisible) formatExactInr(selectedAccount.balance) else "₹ ••••••",
                                style = PulseMonoStyles.amountLarge,
                                color = PulseTealSuccess
                            )
                            Text(
                                text = "IFSC: ${selectedAccount.ifscCode}",
                                style = PulseMonoStyles.codeLabel,
                                color = PulseOnSurfaceMutedDark
                            )
                        } else {
                            Text(
                                text = "₹ ••••••",
                                style = PulseMonoStyles.amountLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (activity != null &&
                                        (bioCapability == BiometricCapability.READY || bioCapability == BiometricCapability.DEVICE_CREDENTIAL_ONLY)
                                    ) {
                                        BiometricAuthHelper.showBiometricPrompt(
                                            activity = activity,
                                            title = "Verify to Check Balance",
                                            subtitle = "${selectedAccount.bankName} ${selectedAccount.maskedNumber}",
                                            allowDeviceCredential = true,
                                            onSuccess = {
                                                checkedAccountIds = checkedAccountIds + selectedAccount.id
                                            },
                                            onError = { _, _ ->
                                                checkedAccountIds = checkedAccountIds + selectedAccount.id
                                            },
                                            onFailed = {}
                                        )
                                    } else {
                                        checkedAccountIds = checkedAccountIds + selectedAccount.id
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PulseViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fetch_balance_button")
                            ) {
                                Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetch Live Balance", color = Color.White)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onAddFunds(selectedAccount.id)
                            checkedAccountIds = checkedAccountIds + selectedAccount.id
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_funds_${selectedAccount.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add ₹5,000")
                    }
                    if (!selectedAccount.isPrimary && selectedAccount.id != "acc_wallet") {
                        Button(
                            onClick = { onSetPrimaryAccount(selectedAccount.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseViolet)
                        ) {
                            Text("Set Primary", color = Color.White)
                        }
                    }
                }

                if (isChecked) {
                    TextButton(
                        onClick = onToggleBalanceVisibility,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isBalanceVisible) "Mask Balance" else "Reveal Balance",
                            color = Color(0xFFC084FC)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add Payment Method Sheet
    if (showAddMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMethodSheet = false },
            containerColor = PulseDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Payment Methods", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    text = "Manage your linked bank accounts, UPI Lite wallet, and RuPay Credit line.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PulseOnSurfaceMutedDark
                )
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PulseDarkTile)
                            .clickable {
                                showAddMethodSheet = false
                                selectedAccountId = acc.id
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${acc.bankName} ${acc.maskedNumber}", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Text(acc.upiId, style = PulseMonoStyles.codeLabel, color = PulseOnSurfaceMutedDark)
                        }
                        if (acc.isPrimary) {
                            Text("Primary", style = MaterialTheme.typography.labelMedium, color = PulseTealSuccess)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PulseOnSurfaceMutedDark)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showSipDialog) {
        AlertDialog(
            onDismissRequest = { showSipDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("Daily Mutual Fund SIP", color = Color.White) },
            text = {
                Text(
                    text = "Start a daily micro-SIP starting at ₹10/day directly via UPI AutoPay from your primary bank account.",
                    color = PulseOnSurfaceMutedDark
                )
            },
            confirmButton = {
                Button(onClick = { showSipDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("Check Balance Help", color = Color.White) },
            text = {
                Text(
                    text = "Tap any linked bank account to verify via biometrics or view its live balance, set your primary UPI account, or add funds.",
                    color = PulseOnSurfaceMutedDark
                )
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun TenRupeeNoteIllustration() {
    Box(
        modifier = Modifier.size(width = 72.dp, height = 46.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back pink banknote
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 30.dp)
                .offset(x = 2.dp, y = 6.dp)
                .rotate(-8f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFBE185D))
                .border(1.dp, Color(0xFFF472B6), RoundedCornerShape(6.dp))
        )
        // Front bright pink ₹10 banknote
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 32.dp)
                .rotate(-10f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEC4899))
                .border(1.5.dp, Color(0xFFFBCFE8), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹10",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                ),
                color = Color.White
            )
        }
        // Sparkle star top-center
        Text(
            text = "✦",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = Color(0xFFFBBF24),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 6.dp, y = (-6).dp)
        )
    }
}

@Composable
private fun UpiTriColorChevrons() {
    Canvas(modifier = Modifier.size(width = 18.dp, height = 22.dp)) {
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
