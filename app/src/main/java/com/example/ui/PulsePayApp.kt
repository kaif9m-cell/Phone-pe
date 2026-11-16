package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PaymentProcessingOverlay
import com.example.ui.components.UpiPinBottomSheet
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileAndQrScreen
import com.example.ui.screens.ReceiptScreen
import com.example.ui.screens.ScanAndPayScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.screens.WalletAndBalanceScreen
import com.example.ui.theme.PulseDarkSurface
import com.example.ui.theme.PulseElectricViolet
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseViolet
import kotlinx.coroutines.launch

object PulseRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SCAN = "scan"
    const val ALERTS = "alerts"
    const val HISTORY = "history"
    const val HISTORY_WITH_ID = "history?txId={txId}"
    const val PROFILE_QR = "profile_qr"
    const val WALLET = "wallet"
    const val TRANSFER = "transfer"
    const val RECEIPT = "receipt/{txId}"
    const val TRANSACTION_DETAIL = "transaction_detail/{txId}"

    fun receipt(txId: String) = "receipt/$txId"
    fun historyWithTx(txId: String) = "history?txId=$txId"
    fun transactionDetail(txId: String) = "transaction_detail/$txId"
}

private data class BottomTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun PulsePayApp(
    viewModel: PulsePayViewModel = viewModel(factory = PulsePayViewModel.Factory)
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val historyFilters by viewModel.historyFilters.collectAsStateWithLifecycle()
    val historyAnalytics by viewModel.historyAnalytics.collectAsStateWithLifecycle()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsStateWithLifecycle()
    val transferDraft by viewModel.transferDraft.collectAsStateWithLifecycle()
    val pinPromptState by viewModel.pinPromptState.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val latestTransferResult by viewModel.latestTransferResult.collectAsStateWithLifecycle()
    val resolvingTxId by viewModel.resolvingTxId.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = { msg ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is UiEvent.NavigateToReceipt -> {
                    navController.navigate(PulseRoutes.receipt(event.transactionId)) {
                        popUpTo(PulseRoutes.HOME) { inclusive = false }
                    }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: PulseRoutes.HOME
    val showTopLevelNav = currentRoute == PulseRoutes.HOME ||
        currentRoute == PulseRoutes.SEARCH ||
        currentRoute == PulseRoutes.ALERTS ||
        currentRoute.startsWith(PulseRoutes.HISTORY)

    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Exact 4 surrounding tabs + center Scan QR button matching PhonePe Home screenshot
    val tabs = listOf(
        BottomTabItem(PulseRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        BottomTabItem(PulseRoutes.SEARCH, "Search", Icons.Filled.Search, Icons.Outlined.Search, "nav_search"),
        BottomTabItem(PulseRoutes.ALERTS, "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_alerts"),
        BottomTabItem(PulseRoutes.HISTORY, "History", Icons.Filled.History, Icons.Outlined.History, "nav_history")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && showTopLevelNav) {
                NavigationRail(
                    containerColor = PulseDarkSurface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationRailItem(
                        selected = currentRoute == PulseRoutes.SCAN,
                        onClick = { navController.navigate(PulseRoutes.SCAN) },
                        icon = {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan & Pay", tint = PulseViolet)
                        },
                        label = { Text("Scan") }
                    )
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route ||
                            (tab.route == PulseRoutes.HISTORY && currentRoute.startsWith(PulseRoutes.HISTORY))
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
                bottomBar = {
                    if (!useRail && showTopLevelNav) {
                        PulsePayBottomBar(
                            currentRoute = currentRoute,
                            tabs = tabs,
                            onSelectTab = navigateTab,
                            onScanClick = { navController.navigate(PulseRoutes.SCAN) }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = PulseRoutes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(PulseRoutes.HOME) {
                        HomeScreen(
                            userProfile = userProfile,
                            accounts = accounts,
                            contacts = contacts,
                            recentTransactions = allTransactions,
                            isBalanceVisible = isBalanceVisible,
                            onToggleBalanceVisibility = viewModel::toggleBalanceVisibility,
                            onStartTransfer = { mode, contact ->
                                viewModel.startTransferFlow(mode, contact)
                                navController.navigate(PulseRoutes.TRANSFER)
                            },
                            onStartBiller = { name, upi, amt, note, cat ->
                                viewModel.startBillerTransfer(name, upi, amt, note, cat)
                                navController.navigate(PulseRoutes.TRANSFER)
                            },
                            onOpenScan = { navController.navigate(PulseRoutes.SCAN) },
                            onOpenProfileQr = { navController.navigate(PulseRoutes.PROFILE_QR) },
                            onOpenWallet = { navController.navigate(PulseRoutes.WALLET) },
                            onOpenHistory = { navigateTab(PulseRoutes.HISTORY) },
                            onOpenTransactionDetail = { txId ->
                                navController.navigate(PulseRoutes.transactionDetail(txId))
                            },
                            onClaimReferral = viewModel::claimReferralBonus,
                            onSimulateIncoming = { viewModel.simulateIncomingTransfer() }
                        )
                    }

                    composable(PulseRoutes.SEARCH) {
                        SearchScreen(
                            contacts = contacts,
                            transactions = allTransactions,
                            onSelectContact = { contact ->
                                viewModel.startTransferFlow(TransferEntryMode.MOBILE, contact)
                                navController.navigate(PulseRoutes.TRANSFER)
                            },
                            onOpenTransaction = { txId ->
                                navController.navigate(PulseRoutes.transactionDetail(txId))
                            },
                            onPayDirectUpi = { upi ->
                                if (viewModel.startTransferFromUpiUri(upi)) {
                                    navController.navigate(PulseRoutes.TRANSFER)
                                }
                            }
                        )
                    }

                    composable(PulseRoutes.ALERTS) {
                        AlertsScreen(
                            transactions = allTransactions,
                            onOpenTransaction = { txId ->
                                navController.navigate(PulseRoutes.transactionDetail(txId))
                            }
                        )
                    }

                    composable(
                        route = PulseRoutes.HISTORY_WITH_ID,
                        arguments = listOf(
                            navArgument("txId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        HistoryScreen(
                            transactions = filteredTransactions,
                            filters = historyFilters,
                            analytics = historyAnalytics,
                            resolvingTxId = resolvingTxId,
                            onSearchChange = viewModel::updateSearchQuery,
                            onToggleType = viewModel::updateTypeFilter,
                            onToggleStatus = viewModel::updateStatusFilter,
                            onSelectDateRange = viewModel::updateDateRangeFilter,
                            onToggleCategory = viewModel::updateCategoryFilter,
                            onClearFilters = viewModel::clearAllFilters,
                            onResolvePending = viewModel::resolvePendingTransaction,
                            onOpenTransaction = { txId ->
                                navController.navigate(PulseRoutes.transactionDetail(txId))
                            },
                            onShowMessage = showSnackbar
                        )
                    }

                    composable(PulseRoutes.HISTORY) {
                        HistoryScreen(
                            transactions = filteredTransactions,
                            filters = historyFilters,
                            analytics = historyAnalytics,
                            resolvingTxId = resolvingTxId,
                            onSearchChange = viewModel::updateSearchQuery,
                            onToggleType = viewModel::updateTypeFilter,
                            onToggleStatus = viewModel::updateStatusFilter,
                            onSelectDateRange = viewModel::updateDateRangeFilter,
                            onToggleCategory = viewModel::updateCategoryFilter,
                            onClearFilters = viewModel::clearAllFilters,
                            onResolvePending = viewModel::resolvePendingTransaction,
                            onOpenTransaction = { txId ->
                                navController.navigate(PulseRoutes.transactionDetail(txId))
                            },
                            onShowMessage = showSnackbar
                        )
                    }

                    composable(
                        route = PulseRoutes.TRANSACTION_DETAIL,
                        arguments = listOf(navArgument("txId") { type = NavType.StringType })
                    ) { backStack ->
                        val txId = backStack.arguments?.getString("txId").orEmpty()
                        val tx = viewModel.getTransactionById(txId)
                        if (tx != null) {
                            TransactionDetailScreen(
                                transaction = tx,
                                isResolving = resolvingTxId == tx.id,
                                onBack = { navController.popBackStack() },
                                onCheckBalance = { navController.navigate(PulseRoutes.WALLET) },
                                onViewHistory = { counterparty ->
                                    viewModel.updateSearchQuery(counterparty)
                                    navController.popBackStack()
                                    navigateTab(PulseRoutes.HISTORY)
                                },
                                onResolvePending = viewModel::resolvePendingTransaction,
                                onShowMessage = showSnackbar
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    composable(PulseRoutes.SCAN) {
                        ScanAndPayScreen(
                            demoPresets = viewModel.demoQrPresets,
                            onBack = { navController.popBackStack() },
                            onQrPayloadDetected = { rawUri ->
                                val ok = viewModel.startTransferFromUpiUri(rawUri)
                                if (ok) {
                                    navController.navigate(PulseRoutes.TRANSFER)
                                }
                                ok
                            },
                            onOpenMyQr = {
                                navController.popBackStack()
                                navController.navigate(PulseRoutes.PROFILE_QR)
                            },
                            onShowMessage = showSnackbar
                        )
                    }

                    composable(PulseRoutes.PROFILE_QR) {
                        ProfileAndQrScreen(
                            userProfile = userProfile,
                            accounts = accounts,
                            appLockEnabled = appLockEnabled,
                            buildQrPayload = viewModel::buildMyQrPayload,
                            onBack = { navController.popBackStack() },
                            onUpdateProfile = viewModel::updateUserProfile,
                            onToggleFaceId = viewModel::toggleFaceId,
                            onToggleUpiCircle = viewModel::toggleUpiCircle,
                            onToggleAppLock = viewModel::setAppLockEnabled,
                            onLockAppNow = {
                                if (!appLockEnabled) {
                                    viewModel.setAppLockEnabled(true)
                                }
                                viewModel.lockAppNow()
                            },
                            onOpenBankAccounts = { navController.navigate(PulseRoutes.WALLET) },
                            onShowMessage = showSnackbar
                        )
                    }

                    composable(PulseRoutes.WALLET) {
                        WalletAndBalanceScreen(
                            accounts = accounts,
                            isBalanceVisible = isBalanceVisible,
                            onBack = { navController.popBackStack() },
                            onToggleBalanceVisibility = viewModel::toggleBalanceVisibility,
                            onSetPrimaryAccount = viewModel::setPrimaryAccount,
                            onAddFunds = { accId -> viewModel.addDemoMoney(accId, 5000.0) }
                        )
                    }

                    composable(PulseRoutes.TRANSFER) {
                        TransferScreen(
                            draft = transferDraft,
                            accounts = accounts,
                            contacts = contacts,
                            onBack = { navController.popBackStack() },
                            onSelectContact = viewModel::selectContactForDraft,
                            onUpdateRecipient = viewModel::updateDraftRecipient,
                            onUpdateAmount = viewModel::updateDraftAmount,
                            onAddQuickAmount = viewModel::addQuickAmount,
                            onUpdateNote = viewModel::updateDraftNote,
                            onUpdateCategory = viewModel::updateDraftCategory,
                            onSelectSourceAccount = viewModel::updateDraftSourceAccount,
                            onSelectDestinationAccount = viewModel::updateDraftDestinationAccount,
                            onProceedToPay = viewModel::requestPaymentPin
                        )
                    }

                    composable(
                        route = PulseRoutes.RECEIPT,
                        arguments = listOf(navArgument("txId") { type = NavType.StringType })
                    ) { backStack ->
                        val txId = backStack.arguments?.getString("txId").orEmpty()
                        val tx = viewModel.getTransactionById(txId)
                        if (tx != null) {
                            ReceiptScreen(
                                transaction = tx,
                                executionResult = latestTransferResult,
                                onDone = {
                                    navController.popBackStack(PulseRoutes.HOME, inclusive = false)
                                },
                                onRepeatPayment = { repeatTx ->
                                    viewModel.prepareRepeatTransaction(repeatTx)
                                    navController.navigate(PulseRoutes.TRANSFER) {
                                        popUpTo(PulseRoutes.HOME) { inclusive = false }
                                    }
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack(PulseRoutes.HOME, inclusive = false)
                            }
                        }
                    }
                }
            }
        }

        val sourceAccount = accounts.firstOrNull { it.id == transferDraft.sourceAccountId }
        UpiPinBottomSheet(
            pinState = pinPromptState,
            draft = transferDraft,
            sourceAccount = sourceAccount,
            biometricPayEnabled = userProfile.faceIdEnabled,
            onDigitClick = viewModel::onPinDigitPressed,
            onBackspace = viewModel::onPinBackspace,
            onBiometricAuthorized = viewModel::authorizeTransferWithBiometrics,
            onDismiss = viewModel::dismissPinPrompt
        )

        PaymentProcessingOverlay(state = processingState)

        if (appLockEnabled && !isAppUnlocked) {
            BiometricLockScreen(
                userName = userProfile.displayName,
                phoneNumber = userProfile.phoneNumber,
                onUnlocked = viewModel::unlockApp
            )
        }
    }
}

@Composable
private fun PulsePayBottomBar(
    currentRoute: String,
    tabs: List<BottomTabItem>,
    onSelectTab: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Surface(
        color = PulseDarkSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, PulseOutlineDark),
        shadowElevation = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(66.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                tabs.take(2).forEach { tab ->
                    BottomNavTabButton(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onSelectTab(tab.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Center Vibrant Violet Circular Scan FAB matching Screenshot 1
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .size(56.dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(listOf(PulseElectricViolet, PulseViolet))
                            )
                            .clickable(onClick = onScanClick)
                            .testTag("nav_scan_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan and Pay",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                tabs.drop(2).forEach { tab ->
                    BottomNavTabButton(
                        tab = tab,
                        selected = currentRoute == tab.route ||
                            (tab.route == PulseRoutes.HISTORY && currentRoute.startsWith(PulseRoutes.HISTORY)),
                        onClick = { onSelectTab(tab.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavTabButton(
    tab: BottomTabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color.White
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag(tab.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) activeColor else inactiveColor
        )
    }
}
