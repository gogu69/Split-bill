package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Participant
import com.example.model.ParticipantShare
import com.example.service.ContactReader
import com.example.service.SampleReceipts
import com.example.ui.components.AddParticipantDialog
import com.example.ui.theme.ParticipantColors
import java.util.Locale

@Composable
fun ParticipantsScreen(
    participants: List<Participant>,
    shares: List<ParticipantShare>,
    onAddParticipant: (name: String, memberCount: Int, phone: String, email: String, colorIndex: Int) -> Unit,
    onRemoveParticipant: (String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var prefilledName by remember { mutableStateOf("") }
    var prefilledPhone by remember { mutableStateOf("") }
    var prefilledEmail by remember { mutableStateOf("") }
    var prefilledMemberCount by remember { mutableIntStateOf(1) }

    // Selector contacte nativ Android
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { contactUri ->
            try {
                val extracted = ContactReader.readContactDetails(context, contactUri)
                prefilledName = extracted.name
                prefilledPhone = extracted.phone
                prefilledEmail = extracted.email
                prefilledMemberCount = extracted.memberCount
                showAddDialog = true

                val detailList = mutableListOf<String>()
                if (extracted.phone.isNotBlank()) detailList.add("tel: ${extracted.phone}")
                if (extracted.email.isNotBlank()) detailList.add("email: ${extracted.email}")
                if (extracted.memberCount > 1) detailList.add("${extracted.memberCount} membri")
                val detailMsg = if (detailList.isNotEmpty()) " (${detailList.joinToString(", ")})" else ""
                Toast.makeText(context, "Contact preluat: ${extracted.name}$detailMsg", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Eroare la citirea contactului: ${e.message}", Toast.LENGTH_SHORT).show()
                showAddDialog = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
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
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val totalMembers = participants.sumOf { it.memberCount.coerceAtLeast(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("participants_screen")
    ) {
        // Card statistici
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${participants.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Familii / Grupuri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalMembers",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Total Persoane",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Bara de titlu cu butoane de adăugare din agendă sau manual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Familii și Prieteni",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        try {
                            openContactPicker()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showAddDialog = true
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("pick_contact_header_button")
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agendă", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledIconButton(
                    onClick = {
                        prefilledName = ""
                        prefilledPhone = ""
                        prefilledEmail = ""
                        prefilledMemberCount = 1
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("add_participant_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adaugă persoană manual",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (participants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nu există participanți adăugați",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Alege direct din agendă sau adaugă manual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = {
                            val defaultList = SampleReceipts.getDefaultParticipants()
                            defaultList.forEach {
                                onAddParticipant(it.name, it.memberCount, it.phoneOrWhatsapp, it.email, it.colorIndex)
                            }
                        }
                    ) {
                        Text("Încarcă grup demonstrativ (4 familii)")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(participants, key = { it.id }) { participant ->
                    val color = ParticipantColors.getOrElse(participant.colorIndex) { Color.Gray }
                    val share = shares.find { it.participant.id == participant.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("participant_item_${participant.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = color
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = participant.name.take(2).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = participant.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (participant.memberCount > 1) "${participant.memberCount} membri în familie" else "1 persoană",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (participant.phoneOrWhatsapp.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = participant.phoneOrWhatsapp,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (participant.email.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = participant.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                share?.let { s ->
                                    Text(
                                        text = String.format(Locale.US, "%.2f RON", s.totalToPay),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveParticipant(participant.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Șterge",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddParticipantDialog(
            initialName = prefilledName,
            initialPhone = prefilledPhone,
            initialEmail = prefilledEmail,
            initialMemberCount = prefilledMemberCount,
            onDismiss = {
                showAddDialog = false
                prefilledName = ""
                prefilledPhone = ""
                prefilledEmail = ""
                prefilledMemberCount = 1
            },
            onAdd = { name, count, phone, email, colorIdx ->
                onAddParticipant(name, count, phone, email, colorIdx)
                showAddDialog = false
                prefilledName = ""
                prefilledPhone = ""
                prefilledEmail = ""
                prefilledMemberCount = 1
            }
        )
    }
}
