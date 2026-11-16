package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PulsePayDao {

    @Query("SELECT * FROM bank_accounts ORDER BY isPrimary DESC, bankName ASC")
    fun observeBankAccounts(): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts")
    suspend fun getBankAccountsOnce(): List<BankAccountEntity>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): BankAccountEntity?

    @Query("UPDATE bank_accounts SET balance = :newBalance WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, newBalance: Double)

    @Query("UPDATE bank_accounts SET isPrimary = CASE WHEN id = :accountId THEN 1 ELSE 0 END")
    suspend fun setPrimaryAccount(accountId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccounts(accounts: List<BankAccountEntity>)

    @Query("SELECT * FROM contacts ORDER BY lastPaidAt DESC, name ASC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET lastPaidAt = :timestamp WHERE upiId = :upiId")
    suspend fun touchContactByUpi(upiId: String, timestamp: Long)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET status = :status, failureReason = :failureReason WHERE id = :id")
    suspend fun updateTransactionStatus(id: String, status: String, failureReason: String?)

    @Query("DELETE FROM bank_accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM contacts")
    suspend fun clearContacts()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Transaction
    suspend fun resetAllAndSeed(
        accounts: List<BankAccountEntity>,
        contacts: List<ContactEntity>,
        transactions: List<TransactionEntity>
    ) {
        clearTransactions()
        clearContacts()
        clearAccounts()
        insertBankAccounts(accounts)
        insertContacts(contacts)
        insertTransactions(transactions)
    }
}
