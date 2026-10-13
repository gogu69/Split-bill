package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrganizerBankDetails
import com.example.model.ReceiptData
import com.example.model.SplitMode
import com.example.ui.components.BankDetailsDialog
import java.util.Locale

@Composable
fun SplitSettingsScreen(
    receiptData: ReceiptData,
    bankDetails: OrganizerBankDetails,
    onSetSplitMode: (SplitMode) -> Unit,
    onSetTipPercentage: (Double) -> Unit,
    onSetCustomTip: (Double?) -> Unit,
    onUpdateBankDetails: (OrganizerBankDetails) -> Unit
) {
    var showBankDetailsDialog by remember { mutableStateOf(false) }
    var customTipInput by remember { mutableStateOf("") }
    var isCustomTipActive by remember { mutableStateOf(receiptData.customTipAmount != null) }

    val tipPercentages = listOf(0.0, 5.0, 10.0, 12.0, 15.0, 20.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("split_settings_screen")
    ) {
        // Secțiunea 1: Modul de împărțire
        Text(
            text = "Cum doriți să împărțiți nota?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Alegeți între împărțire exactă după consum sau împărțire egală",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Opțiunea 1: După consum individual
        SplitModeCard(
            title = "După consum individual",
            subtitle = "Fiecare persoană/familie plătește produsele alocate + cota din produsele comune",
            icon = Icons.Default.Restaurant,
            isSelected = receiptData.splitMode == SplitMode.BY_CONSUMPTION,
            onClick = { onSetSplitMode(SplitMode.BY_CONSUMPTION) },
            testTag = "mode_by_consumption"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opțiunea 2: Împărțire egală pe persoană
        SplitModeCard(
            title = "Împărțire egală pe cap de persoană",
            subtitle = "Suma totală împărțită la numărul de membri (familiile plătesc proporțional cu numărul de membri)",
            icon = Icons.Default.Person,
            isSelected = receiptData.splitMode == SplitMode.EQUAL_PER_PERSON,
            onClick = { onSetSplitMode(SplitMode.EQUAL_PER_PERSON) },
            testTag = "mode_equal_per_person"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opțiunea 3: Împărțire egală pe familie/grup
        SplitModeCard(
            title = "Împărțire egală pe familie / grup",
            subtitle = "Suma totală împărțită 50/50 exact în mod egal la numărul de familii sau grupuri participante",
            icon = Icons.Default.Group,
            isSelected = receiptData.splitMode == SplitMode.EQUAL_PER_PARTICIPANT,
            onClick = { onSetSplitMode(SplitMode.EQUAL_PER_PARTICIPANT) },
            testTag = "mode_equal_per_group"
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Secțiunea 2: Bacșiș / Tips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bacșiș ospătar (Tips)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Se distribuie automat în calcul",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = String.format(Locale.US, "+%.2f RON", receiptData.tipAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chips procente bacșiș cu încadrare automată pe rânduri în funcție de rezoluție
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tipPercentages.forEach { pct ->
                val isSelected = !isCustomTipActive && receiptData.tipPercentage == pct
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        isCustomTipActive = false
                        onSetTipPercentage(pct)
                    },
                    label = {
                        Text(
                            text = "${pct.toInt()}%",
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tip_${pct.toInt()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bacșiș sumă fixă opțional
        OutlinedTextField(
            value = customTipInput,
            onValueChange = { input ->
                customTipInput = input
                val amount = input.replace(",", ".").toDoubleOrNull()
                if (amount != null && amount >= 0) {
                    isCustomTipActive = true
                    onSetCustomTip(amount)
                } else if (input.isBlank()) {
                    isCustomTipActive = false
                    onSetTipPercentage(10.0)
                }
            },
            label = { Text("Sau sumă fixă bacșiș (RON)") },
            placeholder = { Text("ex: 20 sau 50 RON") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Secțiunea 3: Date bancare organizator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date Bancare Organizator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Folosite pentru RoPay Alias și transferuri bancare (IBAN)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showBankDetailsDialog = true },
                modifier = Modifier.testTag("edit_bank_details_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Modifică date bancare")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${bankDetails.beneficiaryName} • ${bankDetails.bankName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "IBAN: ${bankDetails.iban}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                if (bankDetails.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RoPay Alias: ${bankDetails.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ Salvat permanent în aplicație",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showBankDetailsDialog) {
        BankDetailsDialog(
            initialDetails = bankDetails,
            onDismiss = { showBankDetailsDialog = false },
            onSave = { updated ->
                onUpdateBankDetails(updated)
                showBankDetailsDialog = false
            }
        )
    }
}

@Composable
private fun SplitModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
