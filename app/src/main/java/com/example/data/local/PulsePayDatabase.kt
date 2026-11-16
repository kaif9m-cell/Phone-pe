package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BankAccountEntity::class,
        ContactEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PulsePayDatabase : RoomDatabase() {
    abstract fun pulsePayDao(): PulsePayDao

    companion object {
        @Volatile
        private var INSTANCE: PulsePayDatabase? = null

        fun getInstance(context: Context): PulsePayDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): PulsePayDatabase {
            return Room.databaseBuilder(
                context,
                PulsePayDatabase::class.java,
                "phonepe_upi.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).pulsePayDao().resetAllAndSeed(
                                accounts = DemoSeedData.accounts(),
                                contacts = DemoSeedData.contacts(System.currentTimeMillis()),
                                transactions = DemoSeedData.transactions(System.currentTimeMillis())
                            )
                        }
                    }
                })
                .build()
        }
    }
}

object DemoSeedData {
    fun accounts(): List<BankAccountEntity> = listOf(
        BankAccountEntity(
            id = "acc_hdfc",
            bankName = "HDFC Bank",
            accountType = "Savings Account",
            maskedNumber = "•••• 4821",
            upiId = "arjun.mehta@okhdfc",
            ifscCode = "HDFC0001204",
            balance = 48250.00,
            isPrimary = true,
            accentHex = 0xFF1D4ED8
        ),
        BankAccountEntity(
            id = "acc_sbi",
            bankName = "State Bank of India",
            accountType = "Savings Account",
            maskedNumber = "•••• 9034",
            upiId = "arjun.mehta@oksbi",
            ifscCode = "SBIN0004312",
            balance = 19400.50,
            isPrimary = false,
            accentHex = 0xFF0284C7
        ),
        BankAccountEntity(
            id = "acc_wallet",
            bankName = "PhonePe Wallet",
            accountType = "UPI Lite / Wallet",
            maskedNumber = "Wallet ••01",
            upiId = "arjun.wallet@ybl",
            ifscCode = "PPEL0000001",
            balance = 2350.00,
            isPrimary = false,
            accentHex = 0xFF5F259F
        )
    )

    fun contacts(now: Long): List<ContactEntity> {
        val hour = 3_600_000L
        return listOf(
            ContactEntity(
                id = "c_1",
                name = "Aarav Sharma",
                phoneNumber = "9876543210",
                upiId = "aarav.sharma@okaxis",
                bankLabel = "Axis Bank",
                avatarColorHex = 0xFF5F259F,
                isVerifiedMerchant = false,
                categoryHint = TransactionCategory.TRANSFER.name,
                lastPaidAt = now - 2 * hour
            ),
            ContactEntity(
                id = "c_2",
                name = "Priya Nair",
                phoneNumber = "9820112233",
                upiId = "priya.nair@okicici",
                bankLabel = "ICICI Bank",
                avatarColorHex = 0xFF00A878,
                isVerifiedMerchant = false,
                categoryHint = TransactionCategory.TRANSFER.name,
                lastPaidAt = now - 8 * hour
            ),
            ContactEntity(
                id = "c_3",
                name = "Blue Tokai Coffee",
                phoneNumber = "9900112244",
                upiId = "bluetokai.blr@yesbank",
                bankLabel = "Merchant UPI",
                avatarColorHex = 0xFF0284C7,
                isVerifiedMerchant = true,
                categoryHint = TransactionCategory.FOOD.name,
                lastPaidAt = now - 16 * hour
            ),
            ContactEntity(
                id = "c_4",
                name = "Metro Fresh Mart",
                phoneNumber = "9811223344",
                upiId = "metrofresh@okhdfc",
                bankLabel = "Verified Store",
                avatarColorHex = 0xFFEA580C,
                isVerifiedMerchant = true,
                categoryHint = TransactionCategory.GROCERIES.name,
                lastPaidAt = now - 30 * hour
            ),
            ContactEntity(
                id = "c_5",
                name = "Rohan Kulkarni",
                phoneNumber = "9765432109",
                upiId = "rohan.k@ybl",
                bankLabel = "Kotak Bank",
                avatarColorHex = 0xFF7C3AED,
                isVerifiedMerchant = false,
                categoryHint = TransactionCategory.TRANSFER.name,
                lastPaidAt = now - 54 * hour
            ),
            ContactEntity(
                id = "c_6",
                name = "Ananya Verma",
                phoneNumber = "9845098450",
                upiId = "ananya.v@oksbi",
                bankLabel = "SBI Bank",
                avatarColorHex = 0xFFDB2777,
                isVerifiedMerchant = false,
                categoryHint = TransactionCategory.TRANSFER.name,
                lastPaidAt = now - 72 * hour
            ),
            ContactEntity(
                id = "c_7",
                name = "Urban Threads Store",
                phoneNumber = "9988776655",
                upiId = "urbanthreads@paytm",
                bankLabel = "Merchant UPI",
                avatarColorHex = 0xFF0F766E,
                isVerifiedMerchant = true,
                categoryHint = TransactionCategory.SHOPPING.name,
                lastPaidAt = now - 96 * hour
            ),
            ContactEntity(
                id = "c_8",
                name = "BESCOM Power Utility",
                phoneNumber = "1800425191",
                upiId = "bescom.bills@sbi",
                bankLabel = "BBPS Biller",
                avatarColorHex = 0xFFCA8A04,
                isVerifiedMerchant = true,
                categoryHint = TransactionCategory.BILLS.name,
                lastPaidAt = now - 120 * hour
            )
        )
    }

    fun transactions(now: Long): List<TransactionEntity> {
        val min = 60_000L
        val hour = 3_600_000L
        val day = 86_400_000L
        return listOf(
            TransactionEntity(
                id = "tx_101",
                utrNumber = "632018492011",
                upiTransactionId = "UPI632018492011PLSP",
                counterpartyName = "Blue Tokai Coffee",
                counterpartyUpiId = "bluetokai.blr@yesbank",
                counterpartyPhone = "9900112244",
                amount = 340.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.FOOD.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "HDFC Bank ••4821",
                destinationAccountLabel = "bluetokai.blr@yesbank",
                note = "Iced Pour Over & Croissant",
                paymentMethod = PaymentMethod.QR_SCAN.name,
                timestamp = now - 35 * min
            ),
            TransactionEntity(
                id = "tx_102",
                utrNumber = "632018440928",
                upiTransactionId = "UPI632018440928PLSP",
                counterpartyName = "Priya Nair",
                counterpartyUpiId = "priya.nair@okicici",
                counterpartyPhone = "9820112233",
                amount = 1850.00,
                type = TransactionType.RECEIVED.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.TRANSFER.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "priya.nair@okicici",
                destinationAccountLabel = "HDFC Bank ••4821",
                note = "Dinner split settlement",
                paymentMethod = PaymentMethod.UPI.name,
                timestamp = now - 3 * hour
            ),
            TransactionEntity(
                id = "tx_103",
                utrNumber = "632018391042",
                upiTransactionId = "UPI632018391042PLSP",
                counterpartyName = "PhonePe QR Reward",
                counterpartyUpiId = "rewards@ybl",
                counterpartyPhone = "",
                amount = 25.00,
                type = TransactionType.CASHBACK.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.REWARDS.name,
                sourceAccountId = "acc_wallet",
                sourceAccountLabel = "PhonePe Rewards",
                destinationAccountLabel = "PhonePe Wallet",
                note = "Scratch card cashback on QR scan",
                paymentMethod = PaymentMethod.UPI.name,
                timestamp = now - 4 * hour
            ),
            TransactionEntity(
                id = "tx_104",
                utrNumber = "632018210491",
                upiTransactionId = "UPI632018210491PLSP",
                counterpartyName = "Metro Fresh Mart",
                counterpartyUpiId = "metrofresh@okhdfc",
                counterpartyPhone = "9811223344",
                amount = 1420.50,
                type = TransactionType.PAID.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.GROCERIES.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "HDFC Bank ••4821",
                destinationAccountLabel = "metrofresh@okhdfc",
                note = "Weekly fresh produce",
                paymentMethod = PaymentMethod.QR_SCAN.name,
                timestamp = now - 1 * day - 2 * hour
            ),
            TransactionEntity(
                id = "tx_105",
                utrNumber = "632017992314",
                upiTransactionId = "UPI632017992314PLSP",
                counterpartyName = "Rohan Kulkarni",
                counterpartyUpiId = "rohan.k@ybl",
                counterpartyPhone = "9765432109",
                amount = 750.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.PENDING.name,
                category = TransactionCategory.TRANSFER.name,
                sourceAccountId = "acc_sbi",
                sourceAccountLabel = "SBI Bank ••9034",
                destinationAccountLabel = "rohan.k@ybl",
                note = "Awaiting beneficiary bank confirmation",
                paymentMethod = PaymentMethod.MOBILE.name,
                timestamp = now - 1 * day - 6 * hour
            ),
            TransactionEntity(
                id = "tx_106",
                utrNumber = "632017501122",
                upiTransactionId = "UPI632017501122PLSP",
                counterpartyName = "SBI Bank ••9034",
                counterpartyUpiId = "arjun.mehta@oksbi",
                counterpartyPhone = "",
                amount = 5000.00,
                type = TransactionType.SELF_TRANSFER.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.TRANSFER.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "HDFC Bank ••4821",
                destinationAccountLabel = "SBI Bank ••9034",
                note = "Monthly savings transfer",
                paymentMethod = PaymentMethod.SELF.name,
                timestamp = now - 2 * day
            ),
            TransactionEntity(
                id = "tx_107",
                utrNumber = "632016883201",
                upiTransactionId = "UPI632016883201PLSP",
                counterpartyName = "BESCOM Power Utility",
                counterpartyUpiId = "bescom.bills@sbi",
                counterpartyPhone = "1800425191",
                amount = 2190.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.BILLS.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "HDFC Bank ••4821",
                destinationAccountLabel = "bescom.bills@sbi",
                note = "Electricity bill Oct-Nov",
                paymentMethod = PaymentMethod.UPI.name,
                timestamp = now - 3 * day
            ),
            TransactionEntity(
                id = "tx_108",
                utrNumber = "632016102944",
                upiTransactionId = "UPI632016102944PLSP",
                counterpartyName = "Urban Threads Store",
                counterpartyUpiId = "urbanthreads@paytm",
                counterpartyPhone = "9988776655",
                amount = 2999.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.FAILED.name,
                category = TransactionCategory.SHOPPING.name,
                sourceAccountId = "acc_wallet",
                sourceAccountLabel = "PhonePe Wallet",
                destinationAccountLabel = "urbanthreads@paytm",
                note = "Denim jacket order",
                paymentMethod = PaymentMethod.QR_SCAN.name,
                timestamp = now - 4 * day,
                failureReason = "Insufficient wallet balance at checkout"
            ),
            TransactionEntity(
                id = "tx_109",
                utrNumber = "632015409821",
                upiTransactionId = "UPI632015409821PLSP",
                counterpartyName = "Aarav Sharma",
                counterpartyUpiId = "aarav.sharma@okaxis",
                counterpartyPhone = "9876543210",
                amount = 4500.00,
                type = TransactionType.RECEIVED.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.TRANSFER.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "aarav.sharma@okaxis",
                destinationAccountLabel = "HDFC Bank ••4821",
                note = "Freelance UI design milestone",
                paymentMethod = PaymentMethod.UPI.name,
                timestamp = now - 5 * day
            ),
            TransactionEntity(
                id = "tx_110",
                utrNumber = "632014203819",
                upiTransactionId = "UPI632014203819PLSP",
                counterpartyName = "Ananya Verma",
                counterpartyUpiId = "ananya.v@oksbi",
                counterpartyPhone = "9845098450",
                amount = 980.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.FOOD.name,
                sourceAccountId = "acc_wallet",
                sourceAccountLabel = "PhonePe Wallet",
                destinationAccountLabel = "ananya.v@oksbi",
                note = "Movie & snacks",
                paymentMethod = PaymentMethod.MOBILE.name,
                timestamp = now - 8 * day
            ),
            TransactionEntity(
                id = "tx_111",
                utrNumber = "632012980143",
                upiTransactionId = "UPI632012980143PLSP",
                counterpartyName = "Airtel Fiber Broadband",
                counterpartyUpiId = "airtel.bbps@icici",
                counterpartyPhone = "",
                amount = 1179.00,
                type = TransactionType.PAID.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.BILLS.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "HDFC Bank ••4821",
                destinationAccountLabel = "airtel.bbps@icici",
                note = "300 Mbps Unlimited Plan",
                paymentMethod = PaymentMethod.UPI.name,
                timestamp = now - 12 * day
            ),
            TransactionEntity(
                id = "tx_112",
                utrNumber = "632010551290",
                upiTransactionId = "UPI632010551290PLSP",
                counterpartyName = "Acme Tech Payroll",
                counterpartyUpiId = "payroll@acmetech",
                counterpartyPhone = "",
                amount = 24000.00,
                type = TransactionType.RECEIVED.name,
                status = TransactionStatus.SUCCESS.name,
                category = TransactionCategory.TRANSFER.name,
                sourceAccountId = "acc_hdfc",
                sourceAccountLabel = "payroll@acmetech",
                destinationAccountLabel = "HDFC Bank ••4821",
                note = "Design consulting retainer",
                paymentMethod = PaymentMethod.BANK_ACCOUNT.name,
                timestamp = now - 18 * day
            )
        )
    }

    val qrPresets: List<DemoQrPreset> = listOf(
        DemoQrPreset(
            id = "qr_cafe",
            title = "Blue Tokai Coffee",
            subtitle = "₹285 • Verified Merchant",
            category = TransactionCategory.FOOD,
            upiUri = "upi://pay?pa=bluetokai.blr@yesbank&pn=Blue%20Tokai%20Coffee&am=285&tn=Latte%20and%20Bagel&mc=5814",
            badgeText = "Preset ₹285",
            isMerchant = true
        ),
        DemoQrPreset(
            id = "qr_mart",
            title = "Metro Fresh Mart",
            subtitle = "Dynamic Store QR • Enter Amount",
            category = TransactionCategory.GROCERIES,
            upiUri = "upi://pay?pa=metrofresh@okhdfc&pn=Metro%20Fresh%20Mart&tn=Grocery%20Counter%202&mc=5411",
            badgeText = "Open Amount",
            isMerchant = true
        ),
        DemoQrPreset(
            id = "qr_peer",
            title = "Aarav Sharma",
            subtitle = "aarav.sharma@okaxis • Personal",
            category = TransactionCategory.TRANSFER,
            upiUri = "upi://pay?pa=aarav.sharma@okaxis&pn=Aarav%20Sharma&tn=Quick%20Split",
            badgeText = "Peer QR",
            isMerchant = false
        ),
        DemoQrPreset(
            id = "qr_bill",
            title = "BESCOM Utility",
            subtitle = "₹1,450 • Electricity Bill QR",
            category = TransactionCategory.BILLS,
            upiUri = "upi://pay?pa=bescom.bills@sbi&pn=BESCOM%20Power%20Utility&am=1450&tn=Bill%20Nov%202026&mc=4900",
            badgeText = "Bill ₹1,450",
            isMerchant = true
        )
    )
}
