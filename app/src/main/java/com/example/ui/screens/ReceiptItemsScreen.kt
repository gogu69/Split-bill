package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.Participant
import com.example.model.ReceiptData
import com.example.model.ReceiptItem
import com.example.ui.components.AddEditItemDialog
import com.example.ui.components.ItemAllocationDialog
import com.example.ui.components.PasteReceiptTextDialog
import com.example.ui.theme.ParticipantColors
import java.util.Locale

@Composable
fun ReceiptItemsScreen(
    receiptData: ReceiptData,
    participants: List<Participant>,
    isScanning: Boolean,
    isOfflineMode: Boolean,
    autoTranslateToRomanian: Boolean,
    scanMessage: String?,
    scanError: String?,
    onScanReceipt: (Bitmap) -> Unit,
    onRetryScan: () -> Unit = {},
    onClearScanMessages: () -> Unit,
    onLoadSample: (Int) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onToggleAutoTranslate: () -> Unit,
    onTranslateAllItems: () -> Unit,
    onProcessOfflineText: (String) -> Unit,
    onAddItem: (name: String, quantity: Double, unitPrice: Double) -> Unit,
    onUpdateItem: (ReceiptItem) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearAllItems: () -> Unit = {},
    onAllocateQuantity: (itemId: String, participantId: String, newQty: Double) -> Unit,
    onSplitEqually: (itemId: String) -> Unit,
    onToggleSharedEqually: (itemId: String) -> Unit,
    onResetAllocations: (itemId: String) -> Unit,
    onSetRestaurantName: (String) -> Unit
) {
    val context = LocalContext.current

    // Dialog states
    var selectedItemForAllocation by remember { mutableStateOf<ReceiptItem?>(null) }
    var itemToEdit by remember { mutableStateOf<ReceiptItem?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showPasteTextDialog by remember { mutableStateOf(false) }
    var showDemoMenu by remember { mutableStateOf(false) }
    var showScanOptionsDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { onScanReceipt(it) }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                onScanReceipt(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Eroare la deschiderea imaginii: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Runtime camera permission launcher cu fallback automat la Galerie
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Camera fizică nu este disponibilă pe acest dispozitiv. Se deschide Galeria...", Toast.LENGTH_LONG).show()
                try { galleryLauncher.launch("image/*") } catch (e2: Exception) {}
            }
        } else {
            Toast.makeText(context, "Permisiunea pentru cameră nu a fost acordată. Puteți alege poza notei din Galerie.", Toast.LENGTH_LONG).show()
            try { galleryLauncher.launch("image/*") } catch (e2: Exception) {}
        }
    }

    fun launchCameraSafely() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Camera nu a putut fi pornită pe acest dispozitiv. Se deschide Galeria...", Toast.LENGTH_SHORT).show()
                try { galleryLauncher.launch("image/*") } catch (e2: Exception) {}
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("receipt_items_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Banner Informații Notă
        item(key = "receipt_banner") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${receiptData.items.size} produse pe notă",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f RON", receiptData.subtotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bară comutare Mod Offline, Traducere & Pictogramă Tradu tot
        item(key = "mode_toggles_row") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isOfflineMode,
                        onClick = onToggleOfflineMode,
                        leadingIcon = {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isOfflineMode) "Offline" else "Online",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("toggle_offline_chip")
                    )

                    FilterChip(
                        selected = autoTranslateToRomanian,
                        onClick = onToggleAutoTranslate,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (autoTranslateToRomanian) "RO" else "Orig",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("toggle_translate_chip")
                    )
                }

                if (receiptData.items.isNotEmpty()) {
                    FilledTonalIconButton(
                        onClick = onTranslateAllItems,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("translate_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Tradu tot în română",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Banner dacă Modul Offline este activat
        if (isOfflineMode) {
            item(key = "offline_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mod Offline activ: Puteți adăuga/lipi produse manual, împărți cantitățile, calcula bacșișul și genera coduri QR bancare fără internet.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Butoane acțiune: Pictograme moderne fără text lung care se întinde pe verticală
        item(key = "action_buttons_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isOfflineMode) {
                        // Pictogramă Scanează Notă (Cameră Foto)
                        FilledIconButton(
                            onClick = { showScanOptionsDialog = true },
                            enabled = !isScanning,
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("camera_scan_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scanează notă",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Pictogramă Galerie Foto
                        FilledTonalIconButton(
                            onClick = {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            enabled = !isScanning,
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Alege din galerie",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Mod Offline: Pictograme Adăugare manuală + Lipire text
                        FilledIconButton(
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("offline_add_item_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adaugă produs manual",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = { showPasteTextDialog = true },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("offline_paste_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Lipește text notă",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Pictogramă Adăugare manuală produs (accesibilă mereu)
                    FilledTonalIconButton(
                        onClick = { showAddItemDialog = true },
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("add_item_manually_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adaugă produs manual",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Pictogramă Exemple / Notă demonstrativă
                    Box {
                        FilledTonalIconButton(
                            onClick = { showDemoMenu = true },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("demo_receipt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Exemple note demonstrative",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showDemoMenu,
                            onDismissRequest = { showDemoMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Masa Festivă Românească (4 Ape, Fripturi)") },
                                onClick = {
                                    showDemoMenu = false
                                    onLoadSample(1)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pizzerie (3 Prieteni, Pizza, Bere)") },
                                onClick = {
                                    showDemoMenu = false
                                    onLoadSample(2)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Taverna Grecia Tradusă (Nero, Choriatiki, Souvlaki)") },
                                onClick = {
                                    showDemoMenu = false
                                    onLoadSample(3)
                                }
                            )
                        }
                    }

                    // Pictogramă Șterge toate produsele (dacă există produse pe notă)
                    if (receiptData.items.isNotEmpty()) {
                        FilledTonalIconButton(
                            onClick = { showClearAllConfirmDialog = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("clear_all_items_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Șterge toate produsele din notă",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Indicator stare scanare AI
        if (isScanning) {
            item(key = "scanning_indicator") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini AI analizează nota fiscală și extrage produsele...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Mesaje notificare / eroare
        scanError?.let { error ->
            item(key = "scan_error_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            IconButton(onClick = onClearScanMessages, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Închide", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onRetryScan,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("🔄 Reîncearcă", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onLoadSample(0) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("⚡ Notă Exemplu", fontSize = 12.sp)
                            }

                            if (!isOfflineMode) {
                                OutlinedButton(
                                    onClick = onToggleOfflineMode,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("📴 Mod Offline", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        scanMessage?.let { msg ->
            item(key = "scan_message_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = onClearScanMessages) {
                            Icon(Icons.Default.Close, contentDescription = "Închide")
                        }
                    }
                }
            }
        }

        // Bara subtitlu produse
        item(key = "products_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produse de pe notă",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${receiptData.items.size} produse",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Lista de produse (sau starea goală) integrată în scroll-ul complet al ecranului
        if (receiptData.items.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nu există produse pe notă.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Scanați nota sau folosiți pictogramele de acțiune de mai sus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(receiptData.items, key = { it.id }) { item ->
                ReceiptItemRow(
                    item = item,
                    participants = participants,
                    onOpenAllocation = { selectedItemForAllocation = item },
                    onEdit = { itemToEdit = item },
                    onDelete = { onRemoveItem(item.id) }
                )
            }
        }
    }

    // Modal de distribuire cantitate (multi-quantity allocation)
    selectedItemForAllocation?.let { currentItem ->
        // Actualizăm item-ul curent din state
        val latestItem = receiptData.items.find { it.id == currentItem.id } ?: currentItem
        ItemAllocationDialog(
            item = latestItem,
            participants = participants,
            onDismiss = { selectedItemForAllocation = null },
            onAllocateQuantity = { pId, newQty ->
                onAllocateQuantity(latestItem.id, pId, newQty)
            },
            onSplitEqually = {
                onSplitEqually(latestItem.id)
            },
            onToggleSharedEqually = {
                onToggleSharedEqually(latestItem.id)
            },
            onResetAllocations = {
                onResetAllocations(latestItem.id)
            }
        )
    }

    // Dialog adăugare produs manual
    if (showAddItemDialog) {
        AddEditItemDialog(
            initialItem = null,
            onDismiss = { showAddItemDialog = false },
            onSave = { name, qty, price ->
                onAddItem(name, qty, price)
                showAddItemDialog = false
            }
        )
    }

    // Dialog lipire text notă offline
    if (showPasteTextDialog) {
        PasteReceiptTextDialog(
            onDismiss = { showPasteTextDialog = false },
            onProcessText = { text ->
                onProcessOfflineText(text)
                showPasteTextDialog = false
            }
        )
    }

    // Dialog confirmare ștergere toate produsele
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            modifier = Modifier.testTag("clear_all_items_dialog"),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ștergi toate produsele?",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text("Sigur dorești să ștergi toate cele ${receiptData.items.size} produse din notă? Toate datele scanate și alocările pe familii vor fi eliminate.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllItems()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Șterge tot")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showClearAllConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_clear_all_button")
                ) {
                    Text("Anulează")
                }
            }
        )
    }

    // Dialog editare produs
    itemToEdit?.let { item ->
        AddEditItemDialog(
            initialItem = item,
            onDismiss = { itemToEdit = null },
            onSave = { name, qty, price ->
                onUpdateItem(
                    item.copy(
                        name = name,
                        quantity = qty,
                        unitPrice = price,
                        totalPrice = qty * price
                    )
                )
                itemToEdit = null
            }
        )
    }

    // Modal opțiuni scanare notă (fără crash pe orice dispozitiv)
    if (showScanOptionsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showScanOptionsDialog = false },
            title = {
                Text(
                    text = "Opțiuni Scanare Notă",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Alegeți modalitatea dorită pentru introducerea sau scanarea notei:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Opțiunea 1: Galerie Foto (100% sigur și recomandat)
                    Button(
                        onClick = {
                            showScanOptionsDialog = false
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_option_gallery")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Alege din Galerie (Recomandat)", fontWeight = FontWeight.Bold)
                    }

                    // Opțiunea 2: Cameră Foto (cu verificare permisiune și try-catch)
                    FilledTonalButton(
                        onClick = {
                            showScanOptionsDialog = false
                            launchCameraSafely()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_option_camera")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fă o poză cu Camera", fontWeight = FontWeight.SemiBold)
                    }

                    // Opțiunea 3: Notă Demonstrativă
                    OutlinedButton(
                        onClick = {
                            showScanOptionsDialog = false
                            onLoadSample(1)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_option_demo")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Încarcă Notă Demonstrativă")
                    }

                    // Opțiunea 4: Lipire Text
                    OutlinedButton(
                        onClick = {
                            showScanOptionsDialog = false
                            showPasteTextDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_option_paste")
                    ) {
                        Text("📝 Lipire / Tastare Text Notă (Offline)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showScanOptionsDialog = false }) {
                    Text("Închide")
                }
            }
        )
    }
}

@Composable
fun ReceiptItemRow(
    item: ReceiptItem,
    participants: List<Participant>,
    onOpenAllocation: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val qtyFormatted = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format(Locale.US, "%.1f", item.quantity)
    val isComplete = item.isFullyAllocated

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenAllocation() }
            .testTag("item_row_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$qtyFormatted bucăți x ${String.format(Locale.US, "%.2f", item.unitPrice)} RON",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.2f RON", item.totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editează", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Șterge", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Secțiune status alocare și buton acțiune - puse unele sub altele pe verticală
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Etichetă status
                if (item.isSharedEqually) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Comun la toți", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (isComplete) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alocat complet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    val remFormatted = if (item.remainingQuantity % 1.0 == 0.0) item.remainingQuantity.toInt().toString() else String.format(Locale.US, "%.1f", item.remainingQuantity)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = if (item.allocatedQuantity > 0) "Rămase: $remFormatted buc" else "Nealocat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Buton deschidere alocare (pe toată lățimea sau aliniat, sub status)
                FilledTonalButton(
                    onClick = onOpenAllocation,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Alege cine a consumat", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Lista cu cei care au consumat - afișați unii sub alții (vertical) pentru a evita orice suprapunere pe ecrane înguste
            if (!item.isSharedEqually && item.allocations.isNotEmpty()) {
                val activeAllocations = item.allocations.toList().filter { (personId, qty) ->
                    val participant = participants.find { it.id == personId }
                    participant != null && qty > 0
                }

                if (activeAllocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Consumat de:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        activeAllocations.forEach { (personId, qty) ->
                            val participant = participants.first { it.id == personId }
                            val color = ParticipantColors.getOrElse(participant.colorIndex) { MaterialTheme.colorScheme.primary }
                            val qtyText = if (qty % 1.0 == 0.0) qty.toInt().toString() else String.format(Locale.US, "%.1f", qty)
                            val itemCost = qty * item.unitPrice

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = participant.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = color.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "${qtyText}x",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.2f RON", itemCost),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
