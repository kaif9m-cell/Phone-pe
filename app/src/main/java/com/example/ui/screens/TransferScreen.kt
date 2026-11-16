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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.BankAccountEntity
import com.example.data.local.ContactEntity
import com.example.data.local.PaymentMethod
import com.example.data.local.TransactionCategory
import com.example.ui.TransferDraft
import com.example.ui.TransferEntryMode
import com.example.ui.components.ContactAvatar
import com.example.ui.components.formatExactInr
import com.example.ui.theme.PulseCrimsonFail
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseSoftViolet
import com.example.ui.theme.PulseTealSoft
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferScreen(
    draft: TransferDraft,
    accounts: List<BankAccountEntity>,
    contacts: List<ContactEntity>,
    onBack: () -> Unit,
    onSelectContact: (ContactEntity) -> Unit,
    onUpdateRecipient: (String?, String?, String?, PaymentMethod?) -> Unit,
    onUpdateAmount: (String) -> Unit,
    onAddQuickAmount: (Int) -> Unit,
    onUpdateNote: (String) -> Unit,
    onUpdateCategory: (TransactionCategory) -> Unit,
    onSelectSourceAccount: (String) -> Unit,
    onSelectDestinationAccount: (String) -> Unit,
    onProceedToPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contactQuery by remember { mutableStateOf("") }
    var upiOrBankTab by remember { mutableIntStateOf(0) } // 0 = UPI ID, 1 = Bank + IFSC
    var upiVerified by remember { mutableStateOf(false) }

    var bankAccountNumber by remember { mutableStateOf("") }
    var bankIfsc by remember { mutableStateOf("") }
    var bankHolderName by remember { mutableStateOf("") }

    val selectedSource = accounts.firstOrNull { it.id == draft.sourceAccountId } ?: accounts.firstOrNull()
    val enteredAmount = draft.amountInput.toDoubleOrNull() ?: 0.0
    val insufficientBalance = selectedSource != null && enteredAmount > selectedSource.balance

    val hasRecipient = draft.mode == TransferEntryMode.SELF ||
        draft.recipientName.isNotBlank() ||
        draft.recipientUpiId.isNotBlank() ||
        draft.recipientPhone.length >= 10

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = draft.mode.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("transfer_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (hasRecipient) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        if (insufficientBalance) {
                            Text(
                                text = "Amount exceeds available ${selectedSource?.bankName} balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = PulseCrimsonFail,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Button(
                            onClick = onProceedToPay,
                            enabled = enteredAmount >= 1.0 && !insufficientBalance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("proceed_to_pay_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseViolet)
                        ) {
                            Text(
                                text = if (enteredAmount >= 1.0) "Pay ${formatExactInr(enteredAmount)}" else "Enter Amount to Pay",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 620.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode-specific Recipient Card
                when (draft.mode) {
                    TransferEntryMode.MOBILE -> {
                        if (draft.recipientName.isBlank() && draft.recipientUpiId.isBlank()) {
                            item {
                                OutlinedTextField(
                                    value = contactQuery,
                                    onValueChange = { input ->
                                        contactQuery = input
                                        val digits = input.filter { it.isDigit() }
                                        if (digits.length == 10 && input.all { it.isDigit() }) {
                                            onUpdateRecipient(
                                                "Mobile +91 $digits",
                                                "$digits@ybl",
                                                digits,
                                                PaymentMethod.MOBILE
                                            )
                                        }
                                    },
                                    label = { Text("Search name or enter 10-digit mobile") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("mobile_search_input")
                                )
                            }

                            val digitsOnly = contactQuery.filter { it.isDigit() }
                            if (digitsOnly.length >= 10) {
                                item {
                                    Surface(
                                        color = PulseSoftViolet,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val num = digitsOnly.take(10)
                                                onUpdateRecipient(
                                                    "Mobile +91 $num",
                                                    "$num@ybl",
                                                    num,
                                                    PaymentMethod.MOBILE
                                                )
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Pay new number +91 ${digitsOnly.take(10)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = PulseViolet
                                            )
                                            Text(
                                                text = "Select",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = PulseViolet
                                            )
                                        }
                                    }
                                }
                            }

                            val filtered = contacts.filter {
                                val q = contactQuery.trim().lowercase()
                                q.isEmpty() ||
                                    it.name.lowercase().contains(q) ||
                                    it.phoneNumber.contains(q) ||
                                    it.upiId.lowercase().contains(q)
                            }
                            items(filtered, key = { it.id }) { contact ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectContact(contact) }
                                        .testTag("select_contact_${contact.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        ContactAvatar(
                                            name = contact.name,
                                            colorHex = contact.avatarColorHex,
                                            isVerifiedMerchant = contact.isVerifiedMerchant
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(contact.name, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                text = "+91 ${contact.phoneNumber} • ${contact.upiId}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                SelectedRecipientBanner(
                                    name = draft.recipientName,
                                    subtitle = draft.recipientUpiId,
                                    isVerified = draft.isVerifiedMerchant,
                                    onChangeRecipient = {
                                        onUpdateRecipient("", "", "", PaymentMethod.MOBILE)
                                    }
                                )
                            }
                        }
                    }

                    TransferEntryMode.UPI_OR_BANK -> {
                        if (draft.recipientUpiId.isBlank()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        TabRow(selectedTabIndex = upiOrBankTab) {
                                            Tab(
                                                selected = upiOrBankTab == 0,
                                                onClick = { upiOrBankTab = 0 },
                                                text = { Text("UPI ID") }
                                            )
                                            Tab(
                                                selected = upiOrBankTab == 1,
                                                onClick = { upiOrBankTab = 1 },
                                                text = { Text("Bank + IFSC") }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (upiOrBankTab == 0) {
                                            var upiInput by remember { mutableStateOf("") }
                                            OutlinedTextField(
                                                value = upiInput,
                                                onValueChange = {
                                                    upiInput = it
                                                    upiVerified = false
                                                },
                                                label = { Text("Enter UPI ID (e.g. aarav@okaxis)") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("upi_id_input")
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        if (upiInput.contains("@") && upiInput.length >= 5) {
                                                            upiVerified = true
                                                        }
                                                    },
                                                    enabled = upiInput.contains("@")
                                                ) {
                                                    Text(if (upiVerified) "Verified" else "Verify UPI")
                                                }
                                                Button(
                                                    onClick = {
                                                        val clean = upiInput.trim()
                                                        val inferredName = clean.substringBefore("@")
                                                            .replace(".", " ")
                                                            .split(" ")
                                                            .filter { it.isNotBlank() }
                                                            .joinToString(" ") { p -> p.replaceFirstChar { it.uppercase() } }
                                                            .ifBlank { "UPI Payee" }
                                                        onUpdateRecipient(inferredName, clean, "", PaymentMethod.UPI)
                                                    },
                                                    enabled = upiInput.contains("@") && upiInput.length >= 5,
                                                    modifier = Modifier.testTag("confirm_upi_id_button")
                                                ) {
                                                    Text("Continue")
                                                }
                                            }
                                            if (upiVerified) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = PulseTealSuccess,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "Verified NPCI Handle",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = PulseTealSuccess
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = bankAccountNumber,
                                                    onValueChange = { bankAccountNumber = it.filter { ch -> ch.isDigit() }.take(18) },
                                                    label = { Text("Account Number") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = bankIfsc,
                                                    onValueChange = { bankIfsc = it.uppercase().take(11) },
                                                    label = { Text("IFSC Code (e.g. HDFC0001204)") },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = bankHolderName,
                                                    onValueChange = { bankHolderName = it },
                                                    label = { Text("Account Holder Name") },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Button(
                                                    onClick = {
                                                        val masked = "A/C ••${bankAccountNumber.takeLast(4)}"
                                                        onUpdateRecipient(
                                                            bankHolderName.trim(),
                                                            "$masked ($bankIfsc)",
                                                            "",
                                                            PaymentMethod.BANK_ACCOUNT
                                                        )
                                                    },
                                                    enabled = bankAccountNumber.length >= 6 && bankIfsc.length >= 8 && bankHolderName.isNotBlank(),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Proceed to Amount")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                SelectedRecipientBanner(
                                    name = draft.recipientName,
                                    subtitle = draft.recipientUpiId,
                                    isVerified = true,
                                    onChangeRecipient = {
                                        onUpdateRecipient("", "", "", PaymentMethod.UPI)
                                    }
                                )
                            }
                        }
                    }

                    TransferEntryMode.SELF -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Transfer From", style = MaterialTheme.typography.labelLarge)
                                    AccountChipRow(
                                        accounts = accounts,
                                        selectedId = draft.sourceAccountId,
                                        onSelect = onSelectSourceAccount
                                    )
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SwapVert,
                                            contentDescription = "Self transfer direction",
                                            tint = PulseViolet
                                        )
                                    }
                                    Text("Transfer To", style = MaterialTheme.typography.labelLarge)
                                    AccountChipRow(
                                        accounts = accounts.filter { it.id != draft.sourceAccountId },
                                        selectedId = draft.destinationAccountId,
                                        onSelect = onSelectDestinationAccount
                                    )
                                }
                            }
                        }
                    }

                    TransferEntryMode.QR_CHECKOUT -> {
                        item {
                            SelectedRecipientBanner(
                                name = draft.recipientName,
                                subtitle = draft.recipientUpiId,
                                isVerified = draft.isVerifiedMerchant,
                                onChangeRecipient = null
                            )
                        }
                    }
                }

                // Amount & Payment Details Card (visible once recipient is ready)
                if (hasRecipient) {
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = if (draft.isAmountLocked) "Fixed QR Bill Amount" else "Enter Amount",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = draft.amountInput,
                                    onValueChange = { if (!draft.isAmountLocked) onUpdateAmount(it) },
                                    readOnly = draft.isAmountLocked,
                                    prefix = {
                                        Text(
                                            text = "₹ ",
                                            style = PulseMonoStyles.amountLarge,
                                            color = PulseViolet
                                        )
                                    },
                                    textStyle = PulseMonoStyles.amountLarge,
                                    placeholder = {
                                        Text(
                                            text = "0",
                                            style = PulseMonoStyles.amountLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("transfer_amount_input")
                                )

                                if (!draft.isAmountLocked) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(100, 500, 1000, 2000).forEach { chip ->
                                            Surface(
                                                onClick = { onAddQuickAmount(chip) },
                                                color = PulseSoftViolet,
                                                shape = RoundedCornerShape(999.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "+₹$chip",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PulseViolet,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = draft.note,
                                    onValueChange = onUpdateNote,
                                    label = { Text("Add a note (optional)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("transfer_note_input")
                                )

                                if (draft.mode != TransferEntryMode.SELF) {
                                    Text(
                                        text = "Category",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        TransactionCategory.entries
                                            .filter { it != TransactionCategory.REWARDS }
                                            .forEach { cat ->
                                                FilterChip(
                                                    selected = draft.category == cat,
                                                    onClick = { onUpdateCategory(cat) },
                                                    label = { Text(cat.label) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = PulseSoftViolet,
                                                        selectedLabelColor = PulseViolet
                                                    )
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }

                    // Debit From Account Selector
                    if (draft.mode != TransferEntryMode.SELF) {
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Debit From",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    accounts.forEach { acc ->
                                        val isSelected = acc.id == draft.sourceAccountId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) PulseViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .background(
                                                    if (isSelected) PulseSoftViolet.copy(alpha = 0.45f)
                                                    else Color.Transparent
                                                )
                                                .clickable { onSelectSourceAccount(acc.id) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AccountBalance,
                                                    contentDescription = null,
                                                    tint = Color(acc.accentHex)
                                                )
                                                Column {
                                                    Text(
                                                        text = "${acc.bankName} ${acc.maskedNumber}",
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                    Text(
                                                        text = "Balance: ${formatExactInr(acc.balance)}",
                                                        style = PulseMonoStyles.codeLabel,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Selected account",
                                                    tint = PulseViolet
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
    }
}

@Composable
private fun SelectedRecipientBanner(
    name: String,
    subtitle: String,
    isVerified: Boolean,
    onChangeRecipient: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                ContactAvatar(
                    name = name,
                    colorHex = 0xFF5F259F,
                    isVerifiedMerchant = isVerified
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        if (isVerified) {
                            Surface(
                                color = PulseTealSoft,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = PulseTealSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PulseTealSuccess
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = PulseMonoStyles.codeLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onChangeRecipient != null) {
                OutlinedButton(onClick = onChangeRecipient) {
                    Text("Change")
                }
            }
        }
    }
}

@Composable
private fun AccountChipRow(
    accounts: List<BankAccountEntity>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        accounts.forEach { acc ->
            val selected = acc.id == selectedId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) PulseViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(if (selected) PulseSoftViolet.copy(alpha = 0.45f) else Color.Transparent)
                    .clickable { onSelect(acc.id) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${acc.bankName} ${acc.maskedNumber}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = formatExactInr(acc.balance),
                        style = PulseMonoStyles.codeLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selected) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PulseViolet)
                }
            }
        }
    }
}
