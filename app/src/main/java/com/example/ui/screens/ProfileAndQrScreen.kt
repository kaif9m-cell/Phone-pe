package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BankAccountEntity
import com.example.security.BiometricAuthHelper
import com.example.security.BiometricCapability
import com.example.security.findFragmentActivity
import com.example.ui.UserProfileState
import com.example.ui.components.UpiQrMatrixCard
import com.example.ui.components.formatExactInr
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseDarkSurface
import com.example.ui.theme.PulseDarkTile
import com.example.ui.theme.PulseFreeBadgePink
import com.example.ui.theme.PulseGoldAvatar
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseTealSoft
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndQrScreen(
    userProfile: UserProfileState,
    accounts: List<BankAccountEntity>,
    appLockEnabled: Boolean,
    buildQrPayload: (BankAccountEntity, Double?) -> String,
    onBack: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onToggleFaceId: () -> Unit,
    onToggleUpiCircle: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onLockAppNow: () -> Unit,
    onOpenBankAccounts: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val bioCapability = remember(context) { BiometricAuthHelper.checkCapability(context) }

    var selectedAccountId by remember(accounts) {
        mutableStateOf(accounts.firstOrNull { it.isPrimary }?.id ?: accounts.firstOrNull()?.id)
    }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()

    var requestAmount by remember { mutableStateOf<Double?>(null) }
    var showManageSheet by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showAmountDialog by remember { mutableStateOf(false) }
    var showFeatureDialog by remember { mutableStateOf<String?>(null) }

    var nameInput by remember(userProfile.displayName) { mutableStateOf(userProfile.displayName) }
    var phoneInput by remember(userProfile.phoneNumber) { mutableStateOf(userProfile.phoneNumber) }
    var amountInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PulseDarkBg),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // 1. Top Bar (Back arrow + ? Help)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showFeatureDialog = "help" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Profile Help",
                            tint = Color.White
                        )
                    }
                }
            }

            // 2. Profile Identity Row (Golden '+' avatar, phone, green receiving pill, purple Manage button)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PulseGoldAvatar)
                                .clickable { showManageSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1A1404)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = userProfile.phoneNumber,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "${userProfile.displayName} • Update profile details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { showManageSheet = true }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = PulseTealSoft,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = PulseTealSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Receiving money on PhonePe",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showManageSheet = true },
                        modifier = Modifier.testTag("profile_manage_button")
                    ) {
                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC084FC)
                        )
                    }
                }
            }

            // 3. Elevated Dark QR Card with White QR + Bank Selector Row
            if (selectedAccount != null) {
                val payload = buildQrPayload(selectedAccount, requestAmount)
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = PulseDarkCard,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseOutlineDark),
                            modifier = Modifier.width(282.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 22.dp, bottom = 14.dp, start = 22.dp, end = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UpiQrMatrixCard(
                                    payload = payload,
                                    modifier = Modifier.size(224.dp)
                                )

                                if (requestAmount != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = PulseTealSoft,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            text = "Requesting ${formatExactInr(requestAmount!!)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PulseTealSuccess,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Bank selector row inside bottom of QR card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showAccountPicker = true }
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                        .testTag("qr_bank_selector"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBalance,
                                            contentDescription = null,
                                            tint = Color(selectedAccount.accentHex),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${selectedAccount.bankName} - ${selectedAccount.maskedNumber.takeLast(4)}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Switch bank account",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Set Amount + Copy UPI ID Bar directly under QR Card
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                amountInput = requestAmount?.toInt()?.toString() ?: ""
                                showAmountDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_qr_amount_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (requestAmount == null) "Set Amount" else "Edit ₹")
                        }
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(selectedAccount.upiId))
                                onShowMessage("Copied ${selectedAccount.upiId}")
                            },
                            modifier = Modifier
                                .weight(1.15f)
                                .testTag("copy_qr_upi_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy UPI ID")
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = PulseDarkSurface)
            }

            // 4. "Suggested for you" 4-Tile Row matching Screenshot 2
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Suggested for you",
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
                        SuggestedTile(
                            icon = Icons.Filled.CreditCard,
                            iconTint = PulseGoldAvatar,
                            label = "PhonePe\nHDFC Card",
                            badge = "FREE",
                            onClick = { showFeatureDialog = "hdfc_card" },
                            modifier = Modifier.weight(1f)
                        )
                        SuggestedTile(
                            icon = Icons.Filled.CreditCard,
                            iconTint = Color(0xFF38BDF8),
                            label = "PhonePe\nSBI Card",
                            badge = null,
                            onClick = { showFeatureDialog = "sbi_card" },
                            modifier = Modifier.weight(1f)
                        )
                        SuggestedTile(
                            icon = Icons.Filled.Face,
                            iconTint = Color(0xFF93C5FD),
                            label = if (userProfile.faceIdEnabled) "Face ID\nActive" else "Pay with\nFace ID",
                            badge = null,
                            onClick = onToggleFaceId,
                            modifier = Modifier.weight(1f)
                        )
                        SuggestedTile(
                            icon = Icons.Filled.Group,
                            iconTint = Color(0xFFF472B6),
                            label = if (userProfile.upiCircleEnabled) "UPI Circle\nOn" else "Try UPI\nCircle",
                            badge = null,
                            onClick = onToggleUpiCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = PulseDarkSurface)
            }

            // 5. "Payment Methods" Section matching Screenshot 2
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Payment Methods",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PaymentMethodRow(
                        icon = Icons.Filled.AccountBalance,
                        iconTint = Color(0xFF60A5FA),
                        title = "Bank Accounts",
                        subtitle = "Set UPI PIN, Add Accounts & more (${accounts.size} linked)",
                        onClick = onOpenBankAccounts,
                        testTag = "profile_bank_accounts_row"
                    )

                    HorizontalDivider(color = PulseOutlineDark)

                    PaymentMethodRow(
                        icon = Icons.Filled.CreditCard,
                        iconTint = Color(0xFF38BDF8),
                        title = "RuPay Credit Card",
                        subtitle = "Link & pay merchants via UPI Credit",
                        onClick = { showFeatureDialog = "rupay" },
                        testTag = "profile_rupay_row"
                    )

                    HorizontalDivider(color = PulseOutlineDark)

                    PaymentMethodRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconTint = PulseViolet,
                        title = "PhonePe Wallet",
                        subtitle = "Instant cashback & zero-PIN micro-payments",
                        onClick = onOpenBankAccounts,
                        testTag = "profile_wallet_row"
                    )
                }
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = PulseDarkSurface)
            }

            // 6. Security & Biometric App Lock Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Security & App Lock",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )

                    Surface(
                        color = PulseDarkCard,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseOutlineDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PulseDarkTile),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        tint = PulseTealSuccess
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Biometric App Lock",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = bioCapability.statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = appLockEnabled,
                                onCheckedChange = { enable ->
                                    if (enable && activity != null &&
                                        (bioCapability == BiometricCapability.READY || bioCapability == BiometricCapability.DEVICE_CREDENTIAL_ONLY)
                                    ) {
                                        BiometricAuthHelper.showBiometricPrompt(
                                            activity = activity,
                                            title = "Enable Biometric App Lock",
                                            subtitle = "Verify fingerprint or face to turn on app lock",
                                            allowDeviceCredential = true,
                                            onSuccess = { onToggleAppLock(true) },
                                            onError = { _, msg -> onShowMessage(msg) },
                                            onFailed = { onShowMessage("Biometric verification failed") }
                                        )
                                    } else {
                                        onToggleAppLock(enable)
                                    }
                                },
                                modifier = Modifier.testTag("profile_biometric_lock_switch")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onLockAppNow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lock_app_now_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock App Now")
                        }
                        if (bioCapability == BiometricCapability.NONE_ENROLLED) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(BiometricAuthHelper.buildEnrollIntent())
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Enroll Device Lock")
                            }
                        }
                    }
                }
            }
        }
    }

    // Manage Profile & QR Sheet
    if (showManageSheet && selectedAccount != null) {
        val payload = buildQrPayload(selectedAccount, requestAmount)
        ModalBottomSheet(
            onDismissRequest = { showManageSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PulseDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Manage Profile & UPI ID", style = MaterialTheme.typography.titleLarge, color = Color.White)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Registered Mobile Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(selectedAccount.upiId))
                            onShowMessage("Copied ${selectedAccount.upiId}")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy UPI ID")
                    }
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Pay ${userProfile.displayName} via PhonePe:\n$payload")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share UPI QR"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share QR")
                    }
                }

                Button(
                    onClick = {
                        onUpdateProfile(nameInput, phoneInput)
                        showManageSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PulseViolet)
                ) {
                    Text("Save Profile", color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Bank Account Switcher Bottom Sheet
    if (showAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAccountPicker = false },
            containerColor = PulseDarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Receiving Account", style = MaterialTheme.typography.titleLarge, color = Color.White)
                accounts.forEach { acc ->
                    val selected = acc.id == selectedAccount?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) PulseViolet.copy(alpha = 0.2f) else PulseDarkTile)
                            .clickable {
                                selectedAccountId = acc.id
                                showAccountPicker = false
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${acc.bankName} ${acc.maskedNumber}", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Text(acc.upiId, style = PulseMonoStyles.codeLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PulseTealSuccess)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Request Amount Dialog
    if (showAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false },
            containerColor = PulseDarkCard,
            title = { Text("Request Specific Amount", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount in ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    requestAmount = amountInput.toDoubleOrNull()?.takeIf { it > 0.0 }
                    showAmountDialog = false
                }) {
                    Text("Update QR")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    requestAmount = null
                    showAmountDialog = false
                }) {
                    Text("Clear")
                }
            }
        )
    }

    // Suggested Feature Info Dialog
    if (showFeatureDialog != null) {
        val (title, body) = when (showFeatureDialog) {
            "hdfc_card" -> "PhonePe HDFC Card" to "Lifetime-free co-branded card with 1% instant reward points on UPI Scan & Pay."
            "sbi_card" -> "PhonePe SBI Card" to "Travel & lounge card offering 4 complimentary domestic lounge visits."
            "rupay" -> "RuPay Credit on UPI" to "Scan any merchant QR and pay using your linked RuPay Credit line with 45-day interest-free cycle."
            else -> "Receive Money Safely" to "Share your personal QR or UPI ID to receive instant UPI credits directly into your selected bank account."
        }
        AlertDialog(
            onDismissRequest = { showFeatureDialog = null },
            containerColor = PulseDarkCard,
            title = { Text(title, color = Color.White) },
            text = { Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { showFeatureDialog = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun SuggestedTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PulseDarkTile)
                    .border(1.dp, PulseOutlineDark, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label.replace("\n", " "),
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (badge != null) {
                Surface(
                    color = PulseFreeBadgePink,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (-6).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
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

@Composable
private fun PaymentMethodRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PulseDarkTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
