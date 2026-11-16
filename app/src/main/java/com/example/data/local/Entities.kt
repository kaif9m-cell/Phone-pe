package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    PAID("Paid"),
    RECEIVED("Received"),
    SELF_TRANSFER("Self Transfer"),
    CASHBACK("Cashback")
}

enum class TransactionStatus(val label: String) {
    SUCCESS("Completed"),
    PENDING("Pending"),
    FAILED("Failed")
}

enum class TransactionCategory(val label: String) {
    TRANSFER("Transfer"),
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    BILLS("Bills & Utilities"),
    GROCERIES("Groceries"),
    REWARDS("Rewards")
}

enum class PaymentMethod(val label: String) {
    UPI("UPI ID"),
    QR_SCAN("QR Scan"),
    MOBILE("Mobile Number"),
    BANK_ACCOUNT("Bank Transfer"),
    SELF("Self Account")
}

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey val id: String,
    val bankName: String,
    val accountType: String,
    val maskedNumber: String,
    val upiId: String,
    val ifscCode: String,
    val balance: Double,
    val isPrimary: Boolean,
    val accentHex: Long
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val upiId: String,
    val bankLabel: String,
    val avatarColorHex: Long,
    val isVerifiedMerchant: Boolean,
    val categoryHint: String,
    val lastPaidAt: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val utrNumber: String,
    val upiTransactionId: String,
    val counterpartyName: String,
    val counterpartyUpiId: String,
    val counterpartyPhone: String,
    val amount: Double,
    val type: String,
    val status: String,
    val category: String,
    val sourceAccountId: String,
    val sourceAccountLabel: String,
    val destinationAccountLabel: String,
    val note: String,
    val paymentMethod: String,
    val timestamp: Long,
    val failureReason: String? = null
) {
    val txType: TransactionType
        get() = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.PAID)

    val txStatus: TransactionStatus
        get() = runCatching { TransactionStatus.valueOf(status) }.getOrDefault(TransactionStatus.SUCCESS)

    val txCategory: TransactionCategory
        get() = runCatching { TransactionCategory.valueOf(category) }.getOrDefault(TransactionCategory.TRANSFER)

    val txMethod: PaymentMethod
        get() = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.UPI)
}

data class ParsedUpiPayload(
    val payeeUpiId: String,
    val payeeName: String,
    val presetAmount: Double?,
    val note: String,
    val category: TransactionCategory,
    val isMerchant: Boolean,
    val rawUri: String
)

data class DemoQrPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: TransactionCategory,
    val upiUri: String,
    val badgeText: String,
    val isMerchant: Boolean
)
