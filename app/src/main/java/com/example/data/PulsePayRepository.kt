package com.example.data

import android.net.Uri
import com.example.data.local.BankAccountEntity
import com.example.data.local.ContactEntity
import com.example.data.local.DemoSeedData
import com.example.data.local.PaymentMethod
import com.example.data.local.PulsePayDao
import com.example.data.local.TransactionCategory
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import com.example.data.local.ParsedUpiPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

data class TransferExecutionResult(
    val primaryTransaction: TransactionEntity,
    val cashbackTransaction: TransactionEntity?,
    val updatedSourceBalance: Double
)

class PulsePayRepository(private val dao: PulsePayDao) {

    val accounts: Flow<List<BankAccountEntity>> = dao.observeBankAccounts()
    val contacts: Flow<List<ContactEntity>> = dao.observeContacts()
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()

    suspend fun ensureSeeded() {
        if (dao.getBankAccountsOnce().isEmpty()) {
            val now = System.currentTimeMillis()
            dao.resetAllAndSeed(
                accounts = DemoSeedData.accounts(),
                contacts = DemoSeedData.contacts(now),
                transactions = DemoSeedData.transactions(now)
            )
        }
    }

    suspend fun setPrimaryAccount(accountId: String) {
        dao.setPrimaryAccount(accountId)
    }

    suspend fun addDemoFunds(accountId: String, amount: Double): TransactionEntity? {
        val account = dao.getAccountById(accountId) ?: return null
        val updatedBalance = account.balance + amount
        dao.updateAccountBalance(accountId, updatedBalance)
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            utrNumber = generateUtr(),
            upiTransactionId = "UPI${generateUtr()}PPEL",
            counterpartyName = "Wallet Top-up",
            counterpartyUpiId = "topup@ybl",
            counterpartyPhone = "",
            amount = amount,
            type = TransactionType.RECEIVED.name,
            status = TransactionStatus.SUCCESS.name,
            category = TransactionCategory.TRANSFER.name,
            sourceAccountId = accountId,
            sourceAccountLabel = "PhonePe Wallet Top-up",
            destinationAccountLabel = "${account.bankName} ${account.maskedNumber}",
            note = "Instant wallet balance reload",
            paymentMethod = PaymentMethod.UPI.name,
            timestamp = now
        )
        dao.insertTransaction(tx)
        return tx
    }

    suspend fun executeTransfer(
        sourceAccountId: String,
        destinationAccountId: String?,
        recipientName: String,
        recipientUpiId: String,
        recipientPhone: String,
        amount: Double,
        category: TransactionCategory,
        paymentMethod: PaymentMethod,
        note: String,
        isSelfTransfer: Boolean,
        simulateFailure: Boolean
    ): TransferExecutionResult {
        val sourceAccount = dao.getAccountById(sourceAccountId)
            ?: throw IllegalStateException("Selected source account was not found.")
        val now = System.currentTimeMillis()
        val utr = generateUtr()

        if (simulateFailure) {
            val failedTx = TransactionEntity(
                id = "tx_${UUID.randomUUID().toString().take(8)}",
                utrNumber = utr,
                upiTransactionId = "UPI${utr}FAIL",
                counterpartyName = recipientName,
                counterpartyUpiId = recipientUpiId,
                counterpartyPhone = recipientPhone,
                amount = amount,
                type = if (isSelfTransfer) TransactionType.SELF_TRANSFER.name else TransactionType.PAID.name,
                status = TransactionStatus.FAILED.name,
                category = category.name,
                sourceAccountId = sourceAccount.id,
                sourceAccountLabel = "${sourceAccount.bankName} ${sourceAccount.maskedNumber}",
                destinationAccountLabel = recipientUpiId,
                note = note.ifBlank { "UPI Payment" },
                paymentMethod = paymentMethod.name,
                timestamp = now,
                failureReason = "Beneficiary bank server timed out"
            )
            dao.insertTransaction(failedTx)
            return TransferExecutionResult(failedTx, null, sourceAccount.balance)
        }

        if (sourceAccount.balance < amount) {
            val failedTx = TransactionEntity(
                id = "tx_${UUID.randomUUID().toString().take(8)}",
                utrNumber = utr,
                upiTransactionId = "UPI${utr}NSF",
                counterpartyName = recipientName,
                counterpartyUpiId = recipientUpiId,
                counterpartyPhone = recipientPhone,
                amount = amount,
                type = if (isSelfTransfer) TransactionType.SELF_TRANSFER.name else TransactionType.PAID.name,
                status = TransactionStatus.FAILED.name,
                category = category.name,
                sourceAccountId = sourceAccount.id,
                sourceAccountLabel = "${sourceAccount.bankName} ${sourceAccount.maskedNumber}",
                destinationAccountLabel = recipientUpiId,
                note = note.ifBlank { "UPI Payment" },
                paymentMethod = paymentMethod.name,
                timestamp = now,
                failureReason = "Insufficient balance in ${sourceAccount.bankName}"
            )
            dao.insertTransaction(failedTx)
            return TransferExecutionResult(failedTx, null, sourceAccount.balance)
        }

        val newSourceBalance = sourceAccount.balance - amount
        dao.updateAccountBalance(sourceAccount.id, newSourceBalance)

        var destLabel = recipientUpiId
        if (isSelfTransfer && destinationAccountId != null) {
            val destAccount = dao.getAccountById(destinationAccountId)
            if (destAccount != null) {
                dao.updateAccountBalance(destAccount.id, destAccount.balance + amount)
                destLabel = "${destAccount.bankName} ${destAccount.maskedNumber}"
            }
        }

        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            utrNumber = utr,
            upiTransactionId = "UPI${utr}PLSP",
            counterpartyName = recipientName,
            counterpartyUpiId = recipientUpiId,
            counterpartyPhone = recipientPhone,
            amount = amount,
            type = if (isSelfTransfer) TransactionType.SELF_TRANSFER.name else TransactionType.PAID.name,
            status = TransactionStatus.SUCCESS.name,
            category = category.name,
            sourceAccountId = sourceAccount.id,
            sourceAccountLabel = "${sourceAccount.bankName} ${sourceAccount.maskedNumber}",
            destinationAccountLabel = destLabel,
            note = note.ifBlank { if (isSelfTransfer) "Self Account Transfer" else "UPI Payment" },
            paymentMethod = paymentMethod.name,
            timestamp = now
        )
        dao.insertTransaction(tx)
        dao.touchContactByUpi(recipientUpiId, now)

        // Unlock instant cashback on QR scans >= ₹100
        val cashbackTx = if (!isSelfTransfer && paymentMethod == PaymentMethod.QR_SCAN && amount >= 100.0) {
            val wallet = dao.getAccountById("acc_wallet")
            val rewardAmount = listOf(10.0, 15.0, 20.0, 25.0, 50.0).random()
            if (wallet != null) {
                dao.updateAccountBalance(wallet.id, wallet.balance + rewardAmount)
                val cbUtr = generateUtr()
                val cb = TransactionEntity(
                    id = "tx_${UUID.randomUUID().toString().take(8)}",
                    utrNumber = cbUtr,
                    upiTransactionId = "UPI${cbUtr}RWD",
                    counterpartyName = "PhonePe QR Cashback",
                    counterpartyUpiId = "rewards@ybl",
                    counterpartyPhone = "",
                    amount = rewardAmount,
                    type = TransactionType.CASHBACK.name,
                    status = TransactionStatus.SUCCESS.name,
                    category = TransactionCategory.REWARDS.name,
                    sourceAccountId = wallet.id,
                    sourceAccountLabel = "PhonePe Rewards",
                    destinationAccountLabel = "PhonePe Wallet",
                    note = "Instant cashback on $recipientName QR scan",
                    paymentMethod = PaymentMethod.UPI.name,
                    timestamp = now + 500L
                )
                dao.insertTransaction(cb)
                cb
            } else null
        } else null

        return TransferExecutionResult(tx, cashbackTx, newSourceBalance)
    }

    suspend fun simulateIncomingPayment(
        targetAccountId: String,
        senderName: String,
        senderUpiId: String,
        amount: Double,
        note: String
    ): TransactionEntity? {
        val account = dao.getAccountById(targetAccountId) ?: return null
        val now = System.currentTimeMillis()
        dao.updateAccountBalance(account.id, account.balance + amount)
        val utr = generateUtr()
        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            utrNumber = utr,
            upiTransactionId = "UPI${utr}CR",
            counterpartyName = senderName,
            counterpartyUpiId = senderUpiId,
            counterpartyPhone = "",
            amount = amount,
            type = TransactionType.RECEIVED.name,
            status = TransactionStatus.SUCCESS.name,
            category = TransactionCategory.TRANSFER.name,
            sourceAccountId = account.id,
            sourceAccountLabel = senderUpiId,
            destinationAccountLabel = "${account.bankName} ${account.maskedNumber}",
            note = note.ifBlank { "Instant UPI Receive" },
            paymentMethod = PaymentMethod.QR_SCAN.name,
            timestamp = now
        )
        dao.insertTransaction(tx)
        return tx
    }

    suspend fun resolvePendingTransaction(transactionId: String): TransactionEntity? {
        delay(900)
        dao.updateTransactionStatus(
            id = transactionId,
            status = TransactionStatus.SUCCESS.name,
            failureReason = null
        )
        return dao.getTransactionById(transactionId)
    }

    suspend fun resetDemoData() {
        val now = System.currentTimeMillis()
        dao.resetAllAndSeed(
            accounts = DemoSeedData.accounts(),
            contacts = DemoSeedData.contacts(now),
            transactions = DemoSeedData.transactions(now)
        )
    }

    fun parseUpiUri(rawInput: String): ParsedUpiPayload? {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return null

        // Allow direct UPI ID shorthand like name@bank
        if (!trimmed.startsWith("upi://", ignoreCase = true)) {
            if (trimmed.contains("@") && !trimmed.contains(" ")) {
                val inferredName = trimmed.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { it.uppercase() }
                    }
                    .ifBlank { "UPI Recipient" }
                return ParsedUpiPayload(
                    payeeUpiId = trimmed,
                    payeeName = inferredName,
                    presetAmount = null,
                    note = "",
                    category = TransactionCategory.TRANSFER,
                    isMerchant = false,
                    rawUri = buildUpiUri(trimmed, inferredName, null, "")
                )
            }
            return null
        }

        return runCatching {
            val uri = Uri.parse(trimmed)
            val pa = uri.getQueryParameter("pa")?.trim().orEmpty()
            if (pa.isBlank() || !pa.contains("@")) return null
            val pn = uri.getQueryParameter("pn")?.trim()?.takeIf { it.isNotBlank() }
                ?: pa.substringBefore("@").replaceFirstChar { it.uppercase() }
            val am = uri.getQueryParameter("am")?.toDoubleOrNull()?.takeIf { it > 0.0 }
            val tn = uri.getQueryParameter("tn")?.trim().orEmpty()
            val mc = uri.getQueryParameter("mc")?.trim().orEmpty()
            val category = when (mc) {
                "5814", "5812" -> TransactionCategory.FOOD
                "5411" -> TransactionCategory.GROCERIES
                "4900" -> TransactionCategory.BILLS
                "5651", "5999" -> TransactionCategory.SHOPPING
                else -> TransactionCategory.TRANSFER
            }
            ParsedUpiPayload(
                payeeUpiId = pa,
                payeeName = pn,
                presetAmount = am,
                note = tn,
                category = category,
                isMerchant = mc.isNotBlank(),
                rawUri = trimmed
            )
        }.getOrNull()
    }

    fun buildUpiUri(
        upiId: String,
        name: String,
        amount: Double?,
        note: String
    ): String {
        val builder = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("cu", "INR")
        if (amount != null && amount > 0.0) {
            builder.appendQueryParameter("am", String.format("%.2f", amount))
        }
        if (note.isNotBlank()) {
            builder.appendQueryParameter("tn", note)
        }
        return builder.build().toString()
    }

    private fun generateUtr(): String {
        val prefix = Random.nextInt(6000, 6999)
        val suffix = Random.nextLong(10000000L, 99999999L)
        return "$prefix$suffix"
    }
}
