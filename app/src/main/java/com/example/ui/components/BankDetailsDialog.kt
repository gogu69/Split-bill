package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.OrganizerBankDetails

@Composable
fun BankDetailsDialog(
    initialDetails: OrganizerBankDetails,
    onDismiss: () -> Unit,
    onSave: (OrganizerBankDetails) -> Unit
) {
    val context = LocalContext.current
    var beneficiaryName by remember { mutableStateOf(initialDetails.beneficiaryName) }
    var iban by remember { mutableStateOf(initialDetails.iban) }
    var bankName by remember { mutableStateOf(initialDetails.bankName) }
    var phone by remember { mutableStateOf(initialDetails.phone) }
    var notePrefix by remember { mutableStateOf(initialDetails.paymentNotePrefix) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("bank_details_dialog"),
        title = {
            Text(
                text = "Date Organizator (IBAN & RoPay)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Aceste detalii sunt utilizate pentru generarea codului QR standard EPC și pentru plățile instant RoPay Alias pe număr de telefon. Rămân salvate permanent în aplicație.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = beneficiaryName,
                    onValueChange = { beneficiaryName = it },
                    label = { Text("Nume Titular Cont (Beneficiar)") },
                    placeholder = { Text("ex: Ion Popa") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_beneficiary_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = iban,
                    onValueChange = { iban = it.uppercase() },
                    label = { Text("IBAN (pentru Cod QR EPC)") },
                    placeholder = { Text("ex: RO49INGB0000999912345678") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_iban_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Număr de telefon (pentru RoPay Alias)") },
                    placeholder = { Text("ex: 0721234567 sau +40721234567") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    supportingText = {
                        Text("Numărul asociat în banca ta pentru transferuri instant RoPay (fără IBAN)")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Nume Bancă") },
                    placeholder = { Text("ex: ING Bank, Banca Transilvania, BCR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notePrefix,
                    onValueChange = { notePrefix = it },
                    label = { Text("Prefix descriere plată") },
                    placeholder = { Text("ex: Nota restaurant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialDetails.copy(
                        beneficiaryName = beneficiaryName.trim(),
                        iban = iban.replace(" ", "").trim(),
                        bankName = bankName.trim(),
                        phone = phone.trim(),
                        revolutTag = "",
                        paymentNotePrefix = notePrefix.trim()
                    )
                    onSave(updated)
                    Toast.makeText(context, "Datele organizatorului au fost salvate!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("save_bank_details_button")
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
