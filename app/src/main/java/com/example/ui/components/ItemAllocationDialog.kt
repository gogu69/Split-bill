package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Participant
import com.example.model.ReceiptItem
import com.example.ui.theme.ParticipantColors
import java.util.Locale

@Composable
fun ItemAllocationDialog(
    item: ReceiptItem,
    participants: List<Participant>,
    onDismiss: () -> Unit,
    onAllocateQuantity: (participantId: String, newQty: Double) -> Unit,
    onSplitEqually: () -> Unit,
    onToggleSharedEqually: () -> Unit,
    onResetAllocations: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("item_allocation_dialog"),
        title = {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format(Locale.US, "%.1f", item.quantity)
                    Text(
                        text = "Total pe notă: $qtyStr bucăți",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f RON", item.totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Starea distribuirii cantităților
                val allocated = item.allocatedQuantity
                val totalQty = item.quantity
                val remaining = item.remainingQuantity
                val isComplete = item.isFullyAllocated

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isSharedEqually) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else if (isComplete) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isSharedEqually) Icons.Default.Group else if (isComplete) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (item.isSharedEqually) MaterialTheme.colorScheme.onSecondaryContainer else if (isComplete) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            if (item.isSharedEqually) {
                                Text(
                                    text = "Produs comun (împărțit egal la toți)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Fiecare persoană suportă o cotă egală",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                val allocFormatted = if (allocated % 1.0 == 0.0) allocated.toInt().toString() else String.format(Locale.US, "%.1f", allocated)
                                val totFormatted = if (totalQty % 1.0 == 0.0) totalQty.toInt().toString() else String.format(Locale.US, "%.1f", totalQty)
                                val remFormatted = if (remaining % 1.0 == 0.0) remaining.toInt().toString() else String.format(Locale.US, "%.1f", remaining)

                                Text(
                                    text = if (isComplete) "Toate cele $totFormatted produse au fost alocate!" else "Distribuite: $allocFormatted / $totFormatted (Rămase: $remFormatted)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Alegeți mai jos câte produse a consumat fiecare familie",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Butoane de acțiune rapidă
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSplitEqually,
                        modifier = Modifier.weight(1f).testTag("split_equally_button")
                    ) {
                        Text("Împarte la toți", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onToggleSharedEqually,
                        modifier = Modifier.weight(1f).testTag("toggle_shared_button")
                    ) {
                        Text(if (item.isSharedEqually) "Distribuie manual" else "Marchează comun", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Lista participanților cu stepper cantitate (+ / -)
                if (participants.isEmpty()) {
                    Text(
                        text = "Nu există participanți adăugați. Adăugați familii din fila Participanți.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(participants) { participant ->
                            val currentQty = item.allocations[participant.id] ?: 0.0
                            val participantColor = ParticipantColors.getOrElse(participant.colorIndex) { MaterialTheme.colorScheme.primary }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentQty > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(participantColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = participant.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = participant.name,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (currentQty > 0) {
                                                val cost = currentQty * item.unitPrice
                                                Text(
                                                    text = String.format(Locale.US, "Cost: %.2f RON", cost),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    // Stepper cantitate
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val newQty = (currentQty - 1.0).coerceAtLeast(0.0)
                                                onAllocateQuantity(participant.id, newQty)
                                            },
                                            enabled = currentQty > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Scade cantitate",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        val qtyDisplay = if (currentQty % 1.0 == 0.0) {
                                            currentQty.toInt().toString()
                                        } else {
                                            String.format(Locale.US, "%.1f", currentQty)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentQty > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = qtyDisplay,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                color = if (currentQty > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val newQty = currentQty + 1.0
                                                onAllocateQuantity(participant.id, newQty)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Adaugă cantitate",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("save_allocation_button")
            ) {
                Text("Gata")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onResetAllocations,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Resetează")
            }
        }
    )
}
