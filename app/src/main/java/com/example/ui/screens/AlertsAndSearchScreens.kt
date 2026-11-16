package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ContactEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import com.example.ui.components.CompactTransactionRow
import com.example.ui.components.ContactAvatar
import com.example.ui.components.formatExactInr
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseDarkSurface
import com.example.ui.theme.PulseMonoStyles
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@Composable
fun SearchScreen(
    contacts: List<ContactEntity>,
    transactions: List<TransactionEntity>,
    onSelectContact: (ContactEntity) -> Unit,
    onOpenTransaction: (String) -> Unit,
    onPayDirectUpi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val matchingContacts = remember(contacts, q) {
        if (q.isEmpty()) contacts else contacts.filter {
            it.name.lowercase().contains(q) || it.upiId.lowercase().contains(q) || it.phoneNumber.contains(q)
        }
    }
    val matchingTx = remember(transactions, q) {
        if (q.isEmpty()) transactions.take(5) else transactions.filter {
            it.counterpartyName.lowercase().contains(q) ||
                it.utrNumber.lowercase().contains(q) ||
                it.note.lowercase().contains(q)
        }
    }

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search people, UPI IDs, UTR, or notes") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("global_search_input")
                    )
                }
            }

            if (q.contains("@") && !q.contains(" ")) {
                item {
                    Surface(
                        onClick = { onPayDirectUpi(query.trim()) },
                        color = PulseViolet.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pay UPI ID: ${query.trim()}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            if (matchingContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "People & Verified Merchants",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(matchingContacts, key = { it.id }) { contact ->
                    Surface(
                        color = PulseDarkCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectContact(contact) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ContactAvatar(
                                name = contact.name,
                                colorHex = contact.avatarColorHex,
                                isVerifiedMerchant = contact.isVerifiedMerchant
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                                Text(contact.upiId, style = PulseMonoStyles.codeLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Pay", style = MaterialTheme.typography.labelLarge, color = Color(0xFFC084FC))
                        }
                    }
                }
            }

            if (matchingTx.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Matching Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(matchingTx, key = { it.id }) { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PulseDarkCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            CompactTransactionRow(
                                transaction = tx,
                                onClick = { onOpenTransaction(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsScreen(
    transactions: List<TransactionEntity>,
    onOpenTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val alertItems = remember(transactions) {
        transactions.filter {
            it.txType == TransactionType.RECEIVED ||
                it.txType == TransactionType.CASHBACK ||
                it.txStatus != TransactionStatus.SUCCESS
        }
    }

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alerts & Activity",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            if (alertItems.isEmpty()) {
                item {
                    Surface(
                        color = PulseDarkSurface,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = PulseViolet,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No alerts yet", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }
            } else {
                items(alertItems, key = { it.id }) { tx ->
                    val isCredit = tx.txType == TransactionType.RECEIVED || tx.txType == TransactionType.CASHBACK
                    Surface(
                        color = PulseDarkCard,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTransaction(tx.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCredit) PulseTealSuccess.copy(alpha = 0.16f)
                                        else PulseViolet.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCredit) Icons.Filled.SouthWest else Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isCredit) PulseTealSuccess else Color(0xFFC084FC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isCredit) {
                                        "${formatExactInr(tx.amount)} credited from ${tx.counterpartyName}"
                                    } else {
                                        "${tx.txStatus.label}: ${tx.counterpartyName} (${formatExactInr(tx.amount)})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "${formatTimestamp(tx.timestamp)} • ${tx.note}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
