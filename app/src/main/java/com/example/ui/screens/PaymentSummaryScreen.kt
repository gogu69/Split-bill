package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrganizerBankDetails
import com.example.model.ParticipantShare
import com.example.model.ReceiptData
import com.example.service.SharingService
import com.example.ui.components.BankDetailsDialog
import com.example.ui.components.QrCodeDialog
import com.example.ui.theme.ParticipantColors
import java.util.Locale

@Composable
fun PaymentSummaryScreen(
    receiptData: ReceiptData,
    bankDetails: OrganizerBankDetails,
    shares: List<ParticipantShare>,
    onTogglePaid: (participantId: String) -> Unit,
    onUpdateBankDetails: (OrganizerBankDetails) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedShareForPayment by remember { mutableStateOf<ParticipantShare?>(null) }
    var paymentDialogInitialTab by remember { mutableIntStateOf(0) }
    var showBankDetailsDialog by remember { mutableStateOf(false) }

    val totalCollected = shares.filter { it.isPaid }.sumOf { it.totalToPay }
    val totalBill = receiptData.grandTotal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("payment_summary_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card rezumat general
        item(key = "payment_summary_header") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = receiptData.restaurantName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Total de plată notă:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f RON", totalBill),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Metrici detaliate dispuse clar pe rânduri dedicate
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtotal produse:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", receiptData.subtotal)} RON",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (receiptData.tipAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bacșiș / Serviciu:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "+${String.format(Locale.US, "%.2f", receiptData.tipAmount)} RON",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Achitat până acum:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", totalCollected)} RON / ${String.format(Locale.US, "%.2f", totalBill)} RON",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Card Contul tău pentru încasare (IBAN)
        item(key = "organizer_bank_card") {
            val ibanFormatted = bankDetails.iban.replace(" ", "").chunked(4).joinToString(" ")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .testTag("organizer_bank_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cont bancar pentru încasare",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showBankDetailsDialog = true },
                            modifier = Modifier.size(30.dp).testTag("edit_bank_details_summary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifică cont bancar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${bankDetails.beneficiaryName}  •  ${bankDetails.bankName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ibanFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    SharingService.copyToClipboard(context, bankDetails.iban, "IBAN")
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiază IBAN",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (bankDetails.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RoPay: ${bankDetails.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        SharingService.copyToClipboard(context, bankDetails.phone, "Telefon RoPay")
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiază Telefon RoPay",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
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
        }

        // Buton Trimite Sumar WhatsApp Grup dedicat și spațios
        item(key = "whatsapp_group_button_item") {
            Button(
                onClick = {
                    SharingService.shareGroupSummary(
                        context = context,
                        receiptData = receiptData,
                        bankDetails = bankDetails,
                        shares = shares
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .testTag("share_group_summary_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trimite sumar grup pe WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        item(key = "section_header_participants") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Decontare pe Familii & Persoane",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lista fiecărui participant
        if (shares.isEmpty()) {
            item(key = "empty_shares_message") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu există participanți sau consum alocat.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(shares, key = { it.participant.id }) { share ->
                ParticipantPaymentCard(
                    share = share,
                    bankDetails = bankDetails,
                    restaurantName = receiptData.restaurantName,
                    onTogglePaid = { onTogglePaid(share.participant.id) },
                    onShowBankDetails = {
                        selectedShareForPayment = share
                        paymentDialogInitialTab = 1
                    },
                    onShowRoPay = {
                        selectedShareForPayment = share
                        paymentDialogInitialTab = 0
                    },
                    onShareWhatsApp = {
                        SharingService.sendViaWhatsApp(
                            context = context,
                            restaurantName = receiptData.restaurantName,
                            bankDetails = bankDetails,
                            share = share
                        )
                    },
                    onShareEmail = {
                        SharingService.sendViaEmail(
                            context = context,
                            restaurantName = receiptData.restaurantName,
                            bankDetails = bankDetails,
                            share = share
                        )
                    }
                )
            }
        }
    }

    // Modal RoPay / Date Bancare pentru plată
    selectedShareForPayment?.let { share ->
        QrCodeDialog(
            share = share,
            bankDetails = bankDetails,
            restaurantName = receiptData.restaurantName,
            initialTab = paymentDialogInitialTab,
            onDismiss = { selectedShareForPayment = null },
            onEditBankDetails = { showBankDetailsDialog = true }
        )
    }

    // Modal editare date bancare organizator
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
fun ParticipantPaymentCard(
    share: ParticipantShare,
    bankDetails: OrganizerBankDetails,
    restaurantName: String,
    onTogglePaid: () -> Unit,
    onShowBankDetails: () -> Unit,
    onShowRoPay: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareEmail: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val color = ParticipantColors.getOrElse(share.participant.colorIndex) { MaterialTheme.colorScheme.primary }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_card_${share.participant.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (share.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Antet card participant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = share.participant.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = share.participant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (share.participant.memberCount > 1) "${share.participant.memberCount} pers" else "1 pers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (share.isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (share.isPaid) "Achitat" else "De plată",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (share.isPaid) Color(0xFF166534) else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Suma totală de plată
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.2f RON", share.totalToPay),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onTogglePaid, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (share.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (share.isPaid) "Marchează ca neachitat" else "Marchează ca achitat",
                            tint = if (share.isPaid) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Buton expandare detalii articole consumate
            if (share.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Ascunde detalii produse" else "Vezi ${share.items.size} produse consumate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        share.items.forEach { itemConsumption ->
                            val qtyStr = if (itemConsumption.consumedQuantity % 1.0 == 0.0) {
                                itemConsumption.consumedQuantity.toInt().toString()
                            } else {
                                String.format(Locale.US, "%.1f", itemConsumption.consumedQuantity)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$qtyStr x ${itemConsumption.item.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f RON", itemConsumption.itemCost),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (share.tipShare > 0.01) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bacșiș alocat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "+%.2f RON", share.tipShare),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Butoane acțiune plată & partajare (doar iconițe pentru afișare compactă și vizibilitate maximă a datelor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Buton pictogramă Date Bancare (Nume & Cont IBAN)
                FilledTonalIconButton(
                    onClick = onShowBankDetails,
                    modifier = Modifier.size(40.dp).testTag("open_bank_button_${share.participant.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Date bancare (IBAN)",
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Buton pictogramă RoPay Alias pe număr de telefon
                FilledTonalIconButton(
                    onClick = onShowRoPay,
                    modifier = Modifier.size(40.dp).testTag("open_ropay_button_${share.participant.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "RoPay Alias",
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Buton pictogramă WhatsApp
                FilledIconButton(
                    onClick = onShareWhatsApp,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(40.dp).testTag("whatsapp_button_${share.participant.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Trimite pe WhatsApp",
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Buton pictogramă Email
                FilledTonalIconButton(
                    onClick = onShareEmail,
                    modifier = Modifier.size(40.dp).testTag("email_button_${share.participant.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Trimite pe Email",
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
