package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrganizerBankDetails
import com.example.model.ParticipantShare
import com.example.service.SharingService
import java.util.Locale

@Composable
fun QrCodeDialog(
    share: ParticipantShare,
    bankDetails: OrganizerBankDetails,
    restaurantName: String,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onEditBankDetails: (() -> Unit)? = null
) {
    val context = LocalContext.current
    // selectedTab: 0 = RoPay Alias (Telefon), 1 = Date Bancare (Nume, IBAN)
    var selectedTab by remember { mutableIntStateOf(if (initialTab > 1) 0 else initialTab) }

    val totalFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
    val ibanClean = bankDetails.iban.replace(" ", "").uppercase()
    val ibanFormatted = ibanClean.chunked(4).joinToString(" ")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("qr_code_dialog"),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (selectedTab == 0) "Plată Instant RoPay Alias" else "Date Bancare Organizator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${share.participant.name} • $totalFormatted ${bankDetails.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector opțiuni de plată: RoPay Alias (pe număr de telefon) vs Date Cont Bancar (IBAN)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // TAB 0: RoPay Alias (Telefon)
                    Surface(
                        onClick = { selectedTab = 0 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f).testTag("tab_ropay_alias")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RoPay Alias",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // TAB 1: Date Cont Bancar (IBAN)
                    Surface(
                        onClick = { selectedTab = 1 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f).testTag("tab_bank_account")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cont IBAN",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // ==========================================
                    // TAB 0: ROPAY ALIAS (TRANSFER PE NUMĂR DE TELEFON)
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Transfer Instant RoPay",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (onEditBankDetails != null) {
                                    IconButton(
                                        onClick = onEditBankDetails,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Modifică telefon",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Caseta afișare completă număr de telefon și detalii
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Număr telefon (RoPay Alias)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = bankDetails.phone.ifBlank { "Nespecificat" },
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (bankDetails.phone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                if (bankDetails.phone.isNotBlank()) {
                                                    SharingService.copyToClipboard(context, bankDetails.phone, "Număr telefon RoPay")
                                                    Toast.makeText(context, "Număr telefon copiat!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Configurează mai întâi numărul de telefon!", Toast.LENGTH_SHORT).show()
                                                    onEditBankDetails?.invoke()
                                                }
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copiază număr",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Titular: ${bankDetails.beneficiaryName}  •  ${bankDetails.bankName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Suma de transferat: $totalFormatted RON",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Notă plată: ${bankDetails.paymentNotePrefix} - ${share.participant.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💡 În aplicația bancară (BT Pay, George, CEC, Libra etc.): Alege Transfer RoPay / pe număr de telefon (fără IBAN, instant, gratuit).",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Butoane acțiune compacte cu iconițe pentru RoPay Alias
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copiază număr telefon
                        FilledIconButton(
                            onClick = {
                                if (bankDetails.phone.isNotBlank()) {
                                    SharingService.copyToClipboard(context, bankDetails.phone, "Număr telefon RoPay")
                                    Toast.makeText(context, "Număr copiat: ${bankDetails.phone}!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Configurează mai întâi numărul de telefon!", Toast.LENGTH_SHORT).show()
                                    onEditBankDetails?.invoke()
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("copy_ropay_phone_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiază număr telefon",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Trimite pe WhatsApp RoPay
                        FilledIconButton(
                            onClick = {
                                SharingService.sendRoPayViaWhatsApp(
                                    context = context,
                                    restaurantName = restaurantName,
                                    bankDetails = bankDetails,
                                    share = share
                                )
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("send_ropay_whatsapp_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Trimite RoPay pe WhatsApp",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Trimite SMS RoPay
                        FilledTonalIconButton(
                            onClick = {
                                val shortMsg = SharingService.formatRoPayShortMessage(restaurantName, bankDetails, share)
                                SharingService.sendViaSms(
                                    context = context,
                                    recipientPhone = share.participant.phoneOrWhatsapp,
                                    message = shortMsg
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("send_ropay_sms_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Trimite SMS",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Copiază număr + sumă
                        FilledTonalIconButton(
                            onClick = {
                                val combinedText = "${bankDetails.phone} (${totalFormatted} RON - ${bankDetails.beneficiaryName})"
                                SharingService.copyToClipboard(context, combinedText, "Date RoPay")
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("copy_ropay_combined_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Copiază Telefon + Sumă",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // TAB 1: DATE CONT BANCAR (NUME TITULAR & IBAN)
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Titular & Bancă
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nume Titular (Beneficiar)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = bankDetails.beneficiaryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = bankDetails.bankName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            SharingService.copyToClipboard(context, bankDetails.beneficiaryName, "Nume titular")
                                            Toast.makeText(context, "Nume titular copiat!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copiază nume",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    if (onEditBankDetails != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = onEditBankDetails,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("edit_bank_details_from_qr")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifică cont bancar",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Cont IBAN cu buton de copiere dedicat
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cont IBAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = ibanFormatted,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        SharingService.copyToClipboard(context, bankDetails.iban, "IBAN")
                                        Toast.makeText(context, "Cont IBAN copiat!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiază IBAN",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sumă de plată
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sumă de plată",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$totalFormatted RON",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        SharingService.copyToClipboard(context, totalFormatted, "Sumă de plată")
                                        Toast.makeText(context, "Suma copiată!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiază Sumă",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Notă de plată
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Notă plată / Referință",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${bankDetails.paymentNotePrefix} - ${share.participant.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        val note = "${bankDetails.paymentNotePrefix} - ${share.participant.name}"
                                        SharingService.copyToClipboard(context, note, "Notă plată")
                                        Toast.makeText(context, "Notă plată copiată!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiază notă",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Butoane acțiune compacte cu iconițe pentru transfer IBAN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Trimite date complete pe WhatsApp
                        FilledIconButton(
                            onClick = {
                                SharingService.sendViaWhatsApp(
                                    context = context,
                                    restaurantName = restaurantName,
                                    bankDetails = bankDetails,
                                    share = share
                                )
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("send_whatsapp_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Trimite pe WhatsApp",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Trimite pe Email
                        FilledTonalIconButton(
                            onClick = {
                                SharingService.sendViaEmail(
                                    context = context,
                                    restaurantName = restaurantName,
                                    bankDetails = bankDetails,
                                    share = share
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("send_email_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Trimite pe Email",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Copiază toate datele într-un singur bloc de text
                        FilledTonalIconButton(
                            onClick = {
                                val allInfo = "Beneficiar: ${bankDetails.beneficiaryName}\nIBAN: ${bankDetails.iban} (${bankDetails.bankName})\nSuma: $totalFormatted RON\nNotă: ${bankDetails.paymentNotePrefix} - ${share.participant.name}"
                                SharingService.copyToClipboard(context, allInfo, "Toate datele")
                                Toast.makeText(context, "Toate datele bancare au fost copiate!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("copy_all_qr_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiază toate datele",
                                modifier = Modifier.size(19.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_qr_dialog_button")
            ) {
                Text("Închide")
            }
        }
    )
}
