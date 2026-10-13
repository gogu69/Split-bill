package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.ReceiptItem
import java.util.Locale

@Composable
fun AddEditItemDialog(
    initialItem: ReceiptItem? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: Double, unitPrice: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var quantityStr by remember {
        mutableStateOf(
            initialItem?.let {
                if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else String.format(Locale.US, "%.1f", it.quantity)
            } ?: "1"
        )
    }
    var unitPriceStr by remember {
        mutableStateOf(
            initialItem?.let { String.format(Locale.US, "%.2f", it.unitPrice) } ?: ""
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_edit_item_dialog"),
        title = {
            Text(
                text = if (initialItem == null) "Adaugă produs pe notă" else "Editează produs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Denumire produs") },
                    placeholder = { Text("ex: Apă plată 0.75L, Pizza Diavola") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("item_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it; errorMessage = null },
                        label = { Text("Cantitate") },
                        placeholder = { Text("ex: 4") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("item_quantity_input")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it; errorMessage = null },
                        label = { Text("Preț unitar (RON)") },
                        placeholder = { Text("ex: 14.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("item_unit_price_input")
                    )
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val qty = quantityStr.replace(",", ".").toDoubleOrNull()
                    val price = unitPriceStr.replace(",", ".").toDoubleOrNull()

                    if (cleanName.isBlank()) {
                        errorMessage = "Introduceți numele produsului"
                        return@Button
                    }
                    if (qty == null || qty <= 0) {
                        errorMessage = "Introduceți o cantitate validă (ex: 1, 2, 4)"
                        return@Button
                    }
                    if (price == null || price < 0) {
                        errorMessage = "Introduceți un preț unitar valid (ex: 14.50)"
                        return@Button
                    }

                    onSave(cleanName, qty, price)
                },
                modifier = Modifier.testTag("save_item_button")
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
}
