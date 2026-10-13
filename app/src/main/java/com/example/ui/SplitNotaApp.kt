package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ParticipantsScreen
import com.example.ui.screens.PaymentSummaryScreen
import com.example.ui.screens.ReceiptItemsScreen
import com.example.ui.screens.SplitSettingsScreen
import com.example.viewmodel.SplitNotaViewModel

enum class NavigationStep(val title: String, val icon: ImageVector) {
    ITEMS("Notă & Produse", Icons.Default.Receipt),
    PARTICIPANTS("Familii", Icons.Default.Group),
    SETTINGS("Calcul & Tips", Icons.Default.Calculate),
    PAYMENTS("Plăți & RoPay", Icons.Default.AccountBalance)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitNotaApp(
    viewModel: SplitNotaViewModel = viewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val receiptData by viewModel.receiptData.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val bankDetails by viewModel.bankDetails.collectAsStateWithLifecycle()
    val shares by viewModel.participantShares.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val autoTranslateToRomanian by viewModel.autoTranslateToRomanian.collectAsStateWithLifecycle()
    val scanMessage by viewModel.scanMessage.collectAsStateWithLifecycle()
    val scanError by viewModel.scanError.collectAsStateWithLifecycle()

    // Gestionare BackHandler: dacă nu este pe primul ecran, revine la pasul anterior
    BackHandler(enabled = currentStep > 0) {
        currentStep -= 1
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SplitNota",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationStep.values().forEachIndexed { index, step ->
                    NavigationBarItem(
                        selected = currentStep == index,
                        onClick = { currentStep = index },
                        icon = {
                            if (index == 0 && receiptData.items.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${receiptData.items.size}") } }) {
                                    Icon(step.icon, contentDescription = step.title)
                                }
                            } else if (index == 1 && participants.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${participants.size}") } }) {
                                    Icon(step.icon, contentDescription = step.title)
                                }
                            } else {
                                Icon(step.icon, contentDescription = step.title)
                            }
                        },
                        alwaysShowLabel = false,
                        modifier = Modifier.testTag("nav_step_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStep) {
                0 -> ReceiptItemsScreen(
                    receiptData = receiptData,
                    participants = participants,
                    isScanning = isScanning,
                    isOfflineMode = isOfflineMode,
                    autoTranslateToRomanian = autoTranslateToRomanian,
                    scanMessage = scanMessage,
                    scanError = scanError,
                    onScanReceipt = { viewModel.scanReceiptImage(it) },
                    onRetryScan = { viewModel.retryLastScan() },
                    onClearScanMessages = { viewModel.clearScanMessages() },
                    onLoadSample = { viewModel.loadSample(it) },
                    onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                    onToggleAutoTranslate = { viewModel.toggleAutoTranslate() },
                    onTranslateAllItems = { viewModel.translateAllItemsToRomanian() },
                    onProcessOfflineText = { viewModel.parseReceiptTextOffline(it) },
                    onAddItem = { name, qty, price -> viewModel.addItem(name, qty, price) },
                    onUpdateItem = { viewModel.updateItem(it) },
                    onRemoveItem = { viewModel.removeItem(it) },
                    onClearAllItems = { viewModel.clearAllItems() },
                    onAllocateQuantity = { itemId, pId, qty -> viewModel.setParticipantAllocation(itemId, pId, qty) },
                    onSplitEqually = { viewModel.splitItemEquallyAmongParticipants(it) },
                    onToggleSharedEqually = { viewModel.toggleItemSharedEqually(it) },
                    onResetAllocations = { viewModel.clearItemAllocations(it) },
                    onSetRestaurantName = { viewModel.setRestaurantName(it) }
                )

                1 -> ParticipantsScreen(
                    participants = participants,
                    shares = shares,
                    onAddParticipant = { name, count, phone, email, colorIdx ->
                        viewModel.addParticipant(name, count, phone, email, colorIdx)
                    },
                    onRemoveParticipant = { viewModel.removeParticipant(it) }
                )

                2 -> SplitSettingsScreen(
                    receiptData = receiptData,
                    bankDetails = bankDetails,
                    onSetSplitMode = { viewModel.setSplitMode(it) },
                    onSetTipPercentage = { viewModel.setTipPercentage(it) },
                    onSetCustomTip = { viewModel.setCustomTip(it) },
                    onUpdateBankDetails = { viewModel.updateBankDetails(it) }
                )

                3 -> PaymentSummaryScreen(
                    receiptData = receiptData,
                    bankDetails = bankDetails,
                    shares = shares,
                    onTogglePaid = { viewModel.toggleParticipantPaid(it) },
                    onUpdateBankDetails = { viewModel.updateBankDetails(it) }
                )
            }
        }
    }
}
