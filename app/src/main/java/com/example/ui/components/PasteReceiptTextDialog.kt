package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasteReceiptTextDialog(
    onDismiss: () -> Unit,
    onProcessText: (String) -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            """4 Acqua naturale 12
2 Pizza Diavola 45
1 Platou cald 140
2 Birra 18
3 Tiramisu 28"""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("paste_receipt_text_dialog"),
        title = {
            Column {
                Text(
                    text = "Introducere Rapidă Notă (Offline)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lipiți sau tastați produsele (format: cantitate denumire preț). Se vor traduce automat în limba română!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Linii notă de plată") },
                    placeholder = { Text("4 apa plata 12\n2 pizza 44\n1 vin 80") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offline_receipt_text_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💡 Exemplu: '4 Acqua naturale 12' va crea 4 bucăți de 'Apă plată' la prețul de 12 RON/bucată.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onProcessText(rawText)
                    onDismiss()
                },
                modifier = Modifier.testTag("process_offline_text_button")
            ) {
                Text("Adaugă & Tradu în Română")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
}
