package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.PulsePayRepository
import com.example.data.TransferExecutionResult
import com.example.data.local.BankAccountEntity
import com.example.data.local.ContactEntity
import com.example.data.local.DemoQrPreset
import com.example.data.local.DemoSeedData
import com.example.data.local.ParsedUpiPayload
import com.example.data.local.PaymentMethod
import com.example.data.local.PulsePayDatabase
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TransferEntryMode(val title: String) {
    MOBILE("To Mobile / Contact"),
    UPI_OR_BANK("To Bank / UPI ID"),
    SELF("To Self Account"),
    QR_CHECKOUT("QR / Merchant Pay")
}

enum class DateRangeFilter(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days")
}

data class HistoryFilters(
    val searchQuery: String = "",
    val type: TransactionType? = null,
    val status: TransactionStatus? = null,
    val dateRange: DateRangeFilter = DateRangeFilter.ALL,
    val category: TransactionCategory? = null,
    val accountId: String? = null
)

data class HistoryAnalytics(
    val totalPaid: Double = 0.0,
    val totalReceived: Double = 0.0,
    val count: Int = 0,
    val categoryBreakdown: List<Pair<TransactionCategory, Double>> = emptyList()
) {
    val netFlow: Double get() = totalReceived - totalPaid
}

data class TransferDraft(
    val mode: TransferEntryMode = TransferEntryMode.MOBILE,
    val recipientName: String = "",
    val recipientUpiId: String = "",
    val recipientPhone: String = "",
    val isVerifiedMerchant: Boolean = false,
    val amountInput: String = "",
    val isAmountLocked: Boolean = false,
    val note: String = "",
    val category: TransactionCategory = TransactionCategory.TRANSFER,
    val paymentMethod: PaymentMethod = PaymentMethod.MOBILE,
    val sourceAccountId: String = "acc_hdfc",
    val destinationAccountId: String? = null
)

data class PinPromptState(
    val isVisible: Boolean = false,
    val enteredPin: String = "",
    val errorText: String? = null,
    val simulateFailure: Boolean = false
)

data class ProcessingOverlayState(
    val isProcessing: Boolean = false,
    val statusMessage: String = "Connecting to UPI Switch...",
    val stepIndex: Int = 0
)

data class UserProfileState(
    val displayName: String = "Arjun Mehta",
    val phoneNumber: String = "+91 9236460858",
    val faceIdEnabled: Boolean = true,
    val upiCircleEnabled: Boolean = false,
    val ruPayLinked: Boolean = true
)

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class NavigateToReceipt(val transactionId: String) : UiEvent
}

class PulsePayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PulsePayRepository(
        PulsePayDatabase.getInstance(application).pulsePayDao()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val accounts: StateFlow<List<BankAccountEntity>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val demoQrPresets: List<DemoQrPreset> = DemoSeedData.qrPresets

    private val securityPrefs = application.getSharedPreferences("pulsepay_security", Context.MODE_PRIVATE)
    private val _appLockEnabled = MutableStateFlow(securityPrefs.getBoolean("app_lock_enabled", true))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(!_appLockEnabled.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfileState())
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    private val _historyFilters = MutableStateFlow(HistoryFilters())
    val historyFilters: StateFlow<HistoryFilters> = _historyFilters.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, _historyFilters) { list, filters ->
            val now = System.currentTimeMillis()
            val minTimestamp = when (filters.dateRange) {
                DateRangeFilter.ALL -> 0L
                DateRangeFilter.TODAY -> now - 86_400_000L
                DateRangeFilter.LAST_7_DAYS -> now - 7L * 86_400_000L
                DateRangeFilter.LAST_30_DAYS -> now - 30L * 86_400_000L
            }
            val q = filters.searchQuery.trim().lowercase()
            list.filter { tx ->
                val matchesSearch = q.isEmpty() ||
                    tx.counterpartyName.lowercase().contains(q) ||
                    tx.counterpartyUpiId.lowercase().contains(q) ||
                    tx.utrNumber.lowercase().contains(q) ||
                    tx.note.lowercase().contains(q) ||
                    tx.amount.toInt().toString().contains(q)
                val matchesType = filters.type == null || tx.txType == filters.type
                val matchesStatus = filters.status == null || tx.txStatus == filters.status
                val matchesCategory = filters.category == null || tx.txCategory == filters.category
                val matchesAccount = filters.accountId == null || tx.sourceAccountId == filters.accountId
                val matchesDate = tx.timestamp >= minTimestamp
                matchesSearch && matchesType && matchesStatus && matchesCategory && matchesAccount && matchesDate
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyAnalytics: StateFlow<HistoryAnalytics> = filteredTransactions
        .combine(MutableStateFlow(Unit)) { list, _ ->
            val successful = list.filter { it.txStatus == TransactionStatus.SUCCESS }
            val paid = successful.filter { it.txType == TransactionType.PAID }.sumOf { it.amount }
            val received = successful.filter {
                it.txType == TransactionType.RECEIVED || it.txType == TransactionType.CASHBACK
            }.sumOf { it.amount }

            val breakdown = successful
                .filter { it.txType == TransactionType.PAID }
                .groupBy { it.txCategory }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            HistoryAnalytics(
                totalPaid = paid,
                totalReceived = received,
                count = list.size,
                categoryBreakdown = breakdown
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryAnalytics())

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    private val _transferDraft = MutableStateFlow(TransferDraft())
    val transferDraft: StateFlow<TransferDraft> = _transferDraft.asStateFlow()

    private val _pinPromptState = MutableStateFlow(PinPromptState())
    val pinPromptState: StateFlow<PinPromptState> = _pinPromptState.asStateFlow()

    private val _processingState = MutableStateFlow(ProcessingOverlayState())
    val processingState: StateFlow<ProcessingOverlayState> = _processingState.asStateFlow()

    private val _latestTransferResult = MutableStateFlow<TransferExecutionResult?>(null)
    val latestTransferResult: StateFlow<TransferExecutionResult?> = _latestTransferResult.asStateFlow()

    private val _resolvingTxId = MutableStateFlow<String?>(null)
    val resolvingTxId: StateFlow<String?> = _resolvingTxId.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun toggleBalanceVisibility() {
        _isBalanceVisible.update { !it }
    }

    fun startTransferFlow(mode: TransferEntryMode, contact: ContactEntity? = null) {
        val currentAccounts = accounts.value
        val primaryId = currentAccounts.firstOrNull { it.isPrimary }?.id
            ?: currentAccounts.firstOrNull()?.id
            ?: "acc_hdfc"
        val secondaryId = currentAccounts.firstOrNull { it.id != primaryId }?.id

        if (contact != null) {
            _transferDraft.value = TransferDraft(
                mode = mode,
                recipientName = contact.name,
                recipientUpiId = contact.upiId,
                recipientPhone = contact.phoneNumber,
                isVerifiedMerchant = contact.isVerifiedMerchant,
                category = runCatching { TransactionCategory.valueOf(contact.categoryHint) }
                    .getOrDefault(TransactionCategory.TRANSFER),
                paymentMethod = if (mode == TransferEntryMode.MOBILE) PaymentMethod.MOBILE else PaymentMethod.UPI,
                sourceAccountId = primaryId
            )
        } else if (mode == TransferEntryMode.SELF) {
            val dest = currentAccounts.firstOrNull { it.id == secondaryId }
            _transferDraft.value = TransferDraft(
                mode = TransferEntryMode.SELF,
                recipientName = dest?.let { "${it.bankName} ${it.maskedNumber}" } ?: "Self Account",
                recipientUpiId = dest?.upiId ?: "",
                category = TransactionCategory.TRANSFER,
                paymentMethod = PaymentMethod.SELF,
                sourceAccountId = primaryId,
                destinationAccountId = secondaryId
            )
        } else {
            _transferDraft.value = TransferDraft(
                mode = mode,
                paymentMethod = when (mode) {
                    TransferEntryMode.MOBILE -> PaymentMethod.MOBILE
                    TransferEntryMode.UPI_OR_BANK -> PaymentMethod.UPI
                    TransferEntryMode.SELF -> PaymentMethod.SELF
                    TransferEntryMode.QR_CHECKOUT -> PaymentMethod.QR_SCAN
                },
                sourceAccountId = primaryId
            )
        }
    }

    fun startTransferFromUpiUri(rawUri: String): Boolean {
        val parsed: ParsedUpiPayload = repository.parseUpiUri(rawUri) ?: return false
        val primaryId = accounts.value.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"
        _transferDraft.value = TransferDraft(
            mode = TransferEntryMode.QR_CHECKOUT,
            recipientName = parsed.payeeName,
            recipientUpiId = parsed.payeeUpiId,
            isVerifiedMerchant = parsed.isMerchant,
            amountInput = parsed.presetAmount?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)
            } ?: "",
            isAmountLocked = parsed.presetAmount != null,
            note = parsed.note,
            category = parsed.category,
            paymentMethod = PaymentMethod.QR_SCAN,
            sourceAccountId = primaryId
        )
        return true
    }

    fun startBillerTransfer(
        billerName: String,
        billerUpi: String,
        defaultAmount: String,
        note: String,
        category: TransactionCategory
    ) {
        val primaryId = accounts.value.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"
        _transferDraft.value = TransferDraft(
            mode = TransferEntryMode.QR_CHECKOUT,
            recipientName = billerName,
            recipientUpiId = billerUpi,
            isVerifiedMerchant = true,
            amountInput = defaultAmount,
            isAmountLocked = false,
            note = note,
            category = category,
            paymentMethod = PaymentMethod.UPI,
            sourceAccountId = primaryId
        )
    }

    fun selectContactForDraft(contact: ContactEntity) {
        _transferDraft.update {
            it.copy(
                recipientName = contact.name,
                recipientUpiId = contact.upiId,
                recipientPhone = contact.phoneNumber,
                isVerifiedMerchant = contact.isVerifiedMerchant,
                category = runCatching { TransactionCategory.valueOf(contact.categoryHint) }
                    .getOrDefault(TransactionCategory.TRANSFER),
                paymentMethod = PaymentMethod.MOBILE
            )
        }
    }

    fun updateDraftRecipient(
        name: String? = null,
        upiId: String? = null,
        phone: String? = null,
        paymentMethod: PaymentMethod? = null
    ) {
        _transferDraft.update { draft ->
            draft.copy(
                recipientName = name ?: draft.recipientName,
                recipientUpiId = upiId ?: draft.recipientUpiId,
                recipientPhone = phone ?: draft.recipientPhone,
                paymentMethod = paymentMethod ?: draft.paymentMethod
            )
        }
    }

    fun updateDraftAmount(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        val dotCount = sanitized.count { it == '.' }
        if (dotCount <= 1) {
            _transferDraft.update { it.copy(amountInput = sanitized) }
        }
    }

    fun addQuickAmount(delta: Int) {
        val current = _transferDraft.value.amountInput.toDoubleOrNull() ?: 0.0
        val updated = (current + delta).coerceAtMost(100_000.0)
        _transferDraft.update {
            it.copy(
                amountInput = if (updated % 1.0 == 0.0) updated.toInt().toString() else String.format("%.2f", updated)
            )
        }
    }

    fun updateDraftNote(note: String) {
        _transferDraft.update { it.copy(note = note.take(60)) }
    }

    fun updateDraftCategory(category: TransactionCategory) {
        _transferDraft.update { it.copy(category = category) }
    }

    fun updateDraftSourceAccount(accountId: String) {
        _transferDraft.update { draft ->
            val newDest = if (draft.mode == TransferEntryMode.SELF && draft.destinationAccountId == accountId) {
                accounts.value.firstOrNull { it.id != accountId }?.id
            } else draft.destinationAccountId
            val destAccount = accounts.value.firstOrNull { it.id == newDest }
            draft.copy(
                sourceAccountId = accountId,
                destinationAccountId = newDest,
                recipientName = if (draft.mode == TransferEntryMode.SELF && destAccount != null) {
                    "${destAccount.bankName} ${destAccount.maskedNumber}"
                } else draft.recipientName,
                recipientUpiId = if (draft.mode == TransferEntryMode.SELF && destAccount != null) {
                    destAccount.upiId
                } else draft.recipientUpiId
            )
        }
    }

    fun updateDraftDestinationAccount(accountId: String) {
        val destAccount = accounts.value.firstOrNull { it.id == accountId } ?: return
        _transferDraft.update { draft ->
            val newSource = if (draft.sourceAccountId == accountId) {
                accounts.value.firstOrNull { it.id != accountId }?.id ?: draft.sourceAccountId
            } else draft.sourceAccountId
            draft.copy(
                sourceAccountId = newSource,
                destinationAccountId = accountId,
                recipientName = "${destAccount.bankName} ${destAccount.maskedNumber}",
                recipientUpiId = destAccount.upiId
            )
        }
    }

    fun requestPaymentPin() {
        val draft = _transferDraft.value
        val amount = draft.amountInput.toDoubleOrNull() ?: 0.0
        if (amount < 1.0 || amount > 100_000.0) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Enter an amount between ₹1 and ₹1,00,000"))
            }
            return
        }
        if (draft.recipientName.isBlank() && draft.recipientUpiId.isBlank() && draft.recipientPhone.isBlank()) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Select or enter a recipient first"))
            }
            return
        }
        _pinPromptState.value = PinPromptState(isVisible = true)
    }

    fun dismissPinPrompt() {
        _pinPromptState.value = PinPromptState(isVisible = false)
    }

    fun toggleSimulateFailure(enabled: Boolean) {
        _pinPromptState.update { it.copy(simulateFailure = enabled) }
    }

    fun onPinDigitPressed(digit: String) {
        val current = _pinPromptState.value
        if (current.enteredPin.length >= 4) return
        val next = current.enteredPin + digit
        _pinPromptState.update { it.copy(enteredPin = next, errorText = null) }
        if (next.length == 4) {
            submitPin(next)
        }
    }

    fun onPinBackspace() {
        _pinPromptState.update {
            if (it.enteredPin.isNotEmpty()) it.copy(enteredPin = it.enteredPin.dropLast(1), errorText = null)
            else it
        }
    }

    private fun submitPin(pin: String) {
        if (pin != "1234") {
            _pinPromptState.update {
                it.copy(
                    enteredPin = "",
                    errorText = "Incorrect UPI PIN. Default PIN is 1234"
                )
            }
            return
        }

        val simulateFailure = _pinPromptState.value.simulateFailure
        _pinPromptState.value = PinPromptState(isVisible = false)
        executeRealTimeTransfer(simulateFailure)
    }

    fun authorizeTransferWithBiometrics() {
        val simulateFailure = _pinPromptState.value.simulateFailure
        _pinPromptState.value = PinPromptState(isVisible = false)
        executeRealTimeTransfer(simulateFailure)
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockAppNow() {
        _isAppUnlocked.value = false
    }

    fun setAppLockEnabled(enabled: Boolean) {
        securityPrefs.edit().putBoolean("app_lock_enabled", enabled).apply()
        _appLockEnabled.value = enabled
        if (!enabled) {
            _isAppUnlocked.value = true
        }
        viewModelScope.launch {
            _events.emit(
                UiEvent.ShowMessage(
                    if (enabled) "Biometric App Lock enabled" else "Biometric App Lock disabled"
                )
            )
        }
    }

    private fun executeRealTimeTransfer(simulateFailure: Boolean) {
        val draft = _transferDraft.value
        val amount = draft.amountInput.toDoubleOrNull() ?: return
        val resolvedName = draft.recipientName.ifBlank {
            if (draft.recipientPhone.isNotBlank()) "+91 ${draft.recipientPhone}" else draft.recipientUpiId
        }
        val resolvedUpi = draft.recipientUpiId.ifBlank {
            "${draft.recipientPhone.ifBlank { "recipient" }}@ybl"
        }

        viewModelScope.launch {
            _processingState.value = ProcessingOverlayState(
                isProcessing = true,
                statusMessage = "Connecting to NPCI UPI Switch...",
                stepIndex = 1
            )
            delay(550)
            _processingState.value = ProcessingOverlayState(
                isProcessing = true,
                statusMessage = "Verifying PIN & Debiting Account...",
                stepIndex = 2
            )
            delay(550)
            _processingState.value = ProcessingOverlayState(
                isProcessing = true,
                statusMessage = "Settling Instant Transfer...",
                stepIndex = 3
            )
            delay(450)

            val result = repository.executeTransfer(
                sourceAccountId = draft.sourceAccountId,
                destinationAccountId = draft.destinationAccountId,
                recipientName = resolvedName,
                recipientUpiId = resolvedUpi,
                recipientPhone = draft.recipientPhone,
                amount = amount,
                category = draft.category,
                paymentMethod = draft.paymentMethod,
                note = draft.note,
                isSelfTransfer = draft.mode == TransferEntryMode.SELF,
                simulateFailure = simulateFailure
            )

            _latestTransferResult.value = result
            _processingState.value = ProcessingOverlayState(isProcessing = false)
            _events.emit(UiEvent.NavigateToReceipt(result.primaryTransaction.id))
        }
    }

    fun prepareRepeatTransaction(tx: TransactionEntity) {
        val primaryId = accounts.value.firstOrNull { it.isPrimary }?.id ?: tx.sourceAccountId
        _transferDraft.value = TransferDraft(
            mode = TransferEntryMode.QR_CHECKOUT,
            recipientName = tx.counterpartyName,
            recipientUpiId = tx.counterpartyUpiId,
            recipientPhone = tx.counterpartyPhone,
            isVerifiedMerchant = true,
            amountInput = if (tx.amount % 1.0 == 0.0) tx.amount.toInt().toString() else String.format("%.2f", tx.amount),
            isAmountLocked = false,
            note = tx.note,
            category = tx.txCategory,
            paymentMethod = tx.txMethod,
            sourceAccountId = primaryId
        )
    }

    fun setPrimaryAccount(accountId: String) {
        viewModelScope.launch {
            repository.setPrimaryAccount(accountId)
            _events.emit(UiEvent.ShowMessage("Primary UPI bank account updated"))
        }
    }

    fun addDemoMoney(accountId: String, amount: Double = 5000.0) {
        viewModelScope.launch {
            repository.addDemoFunds(accountId, amount)
            _events.emit(UiEvent.ShowMessage("Added ₹${amount.toInt()} to your account"))
        }
    }

    fun simulateIncomingTransfer(
        targetAccountId: String = accounts.value.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"
    ) {
        val senders = listOf(
            Triple("Vikramaditya Rao", "vikram.rao@okicici", 1250.0),
            Triple("Sneha Kapoor", "sneha.k@okhdfc", 640.0),
            Triple("Kavya Menon", "kavya.m@ybl", 2200.0),
            Triple("Devansh Joshi", "devansh.j@oksbi", 890.0)
        )
        val pick = senders.random()
        viewModelScope.launch {
            val tx = repository.simulateIncomingPayment(
                targetAccountId = targetAccountId,
                senderName = pick.first,
                senderUpiId = pick.second,
                amount = pick.third,
                note = "Instant peer UPI credit"
            )
            if (tx != null) {
                _events.emit(
                    UiEvent.ShowMessage("Received ₹${pick.third.toInt()} from ${pick.first} in real time!")
                )
            }
        }
    }

    fun resolvePendingTransaction(transactionId: String) {
        viewModelScope.launch {
            _resolvingTxId.value = transactionId
            repository.resolvePendingTransaction(transactionId)
            _resolvingTxId.value = null
            _events.emit(UiEvent.ShowMessage("Transaction status refreshed: Completed"))
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _events.emit(UiEvent.ShowMessage("Account balances and history restored"))
        }
    }

    fun updateUserProfile(displayName: String, phoneNumber: String) {
        _userProfile.update {
            it.copy(
                displayName = displayName.trim().ifBlank { it.displayName },
                phoneNumber = phoneNumber.trim().ifBlank { it.phoneNumber }
            )
        }
        viewModelScope.launch {
            _events.emit(UiEvent.ShowMessage("Profile details updated"))
        }
    }

    fun toggleFaceId() {
        _userProfile.update { it.copy(faceIdEnabled = !it.faceIdEnabled) }
        viewModelScope.launch {
            val enabled = _userProfile.value.faceIdEnabled
            _events.emit(UiEvent.ShowMessage(if (enabled) "Biometric Quick Pay enabled" else "Biometric Quick Pay paused"))
        }
    }

    fun toggleUpiCircle() {
        _userProfile.update { it.copy(upiCircleEnabled = !it.upiCircleEnabled) }
        viewModelScope.launch {
            val enabled = _userProfile.value.upiCircleEnabled
            _events.emit(UiEvent.ShowMessage(if (enabled) "UPI Circle family delegate active" else "UPI Circle paused"))
        }
    }

    fun claimReferralBonus() {
        val primaryId = accounts.value.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"
        viewModelScope.launch {
            repository.simulateIncomingPayment(
                targetAccountId = primaryId,
                senderName = "PhonePe Referral Bonus",
                senderUpiId = "referrals@ybl",
                amount = 200.0,
                note = "Referral reward credited"
            )
            _events.emit(UiEvent.ShowMessage("₹200 referral bonus credited to your primary account!"))
        }
    }

    fun buildMyQrPayload(account: BankAccountEntity, requestAmount: Double?): String {
        return repository.buildUpiUri(
            upiId = account.upiId,
            name = _userProfile.value.displayName,
            amount = requestAmount,
            note = "PhonePe Receive"
        )
    }

    fun updateSearchQuery(query: String) {
        _historyFilters.update { it.copy(searchQuery = query) }
    }

    fun updateTypeFilter(type: TransactionType?) {
        _historyFilters.update { it.copy(type = if (it.type == type) null else type) }
    }

    fun updateStatusFilter(status: TransactionStatus?) {
        _historyFilters.update { it.copy(status = if (it.status == status) null else status) }
    }

    fun updateDateRangeFilter(range: DateRangeFilter) {
        _historyFilters.update { it.copy(dateRange = range) }
    }

    fun updateCategoryFilter(category: TransactionCategory?) {
        _historyFilters.update { it.copy(category = if (it.category == category) null else category) }
    }

    fun clearAllFilters() {
        _historyFilters.value = HistoryFilters()
    }

    fun getTransactionById(id: String): TransactionEntity? {
        return allTransactions.value.firstOrNull { it.id == id }
            ?: _latestTransferResult.value?.primaryTransaction?.takeIf { it.id == id }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                PulsePayViewModel(app)
            }
        }
    }
}
