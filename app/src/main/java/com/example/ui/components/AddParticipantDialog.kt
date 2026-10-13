package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.ContactReader
import com.example.ui.theme.ParticipantColors

@Composable
fun AddParticipantDialog(
    initialName: String = "",
    initialPhone: String = "",
    initialEmail: String = "",
    initialMemberCount: Int = 1,
    onDismiss: () -> Unit,
    onAdd: (name: String, memberCount: Int, phone: String, email: String, colorIndex: Int) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var memberCountStr by remember { mutableStateOf(initialMemberCount.toString()) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var importedFromContacts by remember { mutableStateOf(initialName.isNotBlank() && (initialPhone.isNotBlank() || initialEmail.isNotBlank())) }

    // Sincronizare la schimbarea valorilor inițiale
    LaunchedEffect(initialName, initialPhone, initialEmail, initialMemberCount) {
        if (initialName.isNotBlank()) name = initialName
        if (initialPhone.isNotBlank()) phone = initialPhone
        if (initialEmail.isNotBlank()) email = initialEmail
        if (initialMemberCount > 1) memberCountStr = initialMemberCount.toString()
        if (initialName.isNotBlank() && (initialPhone.isNotBlank() || initialEmail.isNotBlank())) {
            importedFromContacts = true
        }
    }

    // Selector contact nativ Android
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { contactUri ->
            try {
                val extracted = ContactReader.readContactDetails(context, contactUri)
                if (extracted.name.isNotBlank()) {
                    name = extracted.name
                    errorMessage = null
                }
                phone = extracted.phone
                email = extracted.email
                memberCountStr = extracted.memberCount.toString()
                importedFromContacts = true

                val details = mutableListOf<String>()
                if (extracted.phone.isNotBlank()) details.add("telefon")
                if (extracted.email.isNotBlank()) details.add("email")
                val detailStr = if (details.isNotEmpty()) " (inclusiv ${details.joinToString(", ")})" else ""
                Toast.makeText(context, "Contact preluat cu succes${detailStr}!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Eroare la citirea datelor din agendă: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Permisiune opțională de citire contacte (asigură acces complet la numere/emailuri)
    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        try {
            contactPickerLauncher.launch(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openContactPicker() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_participant_dialog"),
        title = {
            Text(
                text = "Adaugă Familie / Persoană",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Buton rapid import direct din agenda telefonului cu preluare completă de date
                FilledTonalButton(
                    onClick = {
                        try {
                            openContactPicker()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            contactPickerLauncher.launch(null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_contact_from_agenda_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alege din Agendă / Contacte", fontWeight = FontWeight.SemiBold)
                }

                if (importedFromContacts) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Date preluate din agendă (Nume, Tel, Email)",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Nume Familie / Persoană") },
                    placeholder = { Text("ex: Familia Popescu, Andrei, Dan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("participant_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = memberCountStr,
                    onValueChange = { memberCountStr = it; errorMessage = null },
                    label = { Text("Număr de membri (pentru familii)") },
                    placeholder = { Text("ex: 2, 3") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("participant_members_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon / WhatsApp (opțional)") },
                    placeholder = { Text("ex: 0722123456") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("participant_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (opțional)") },
                    placeholder = { Text("ex: popescu@email.ro") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("participant_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Alege culoare reprezentativă:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ParticipantColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorIndex = index }
                                .then(
                                    if (selectedColorIndex == index) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                        )
                    }
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
                    val count = memberCountStr.toIntOrNull() ?: 1
                    if (cleanName.isBlank()) {
                        errorMessage = "Introduceți un nume valid sau alegeți din agendă"
                        return@Button
                    }
                    onAdd(cleanName, count, phone, email, selectedColorIndex)
                },
                modifier = Modifier.testTag("save_participant_button")
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
