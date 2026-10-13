package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.OrganizerBankDetails
import com.example.model.Participant
import com.example.model.ParticipantItemConsumption
import com.example.model.ParticipantShare
import com.example.model.ReceiptData
import com.example.model.ReceiptItem
import com.example.model.SplitMode
import com.example.service.BankDetailsStorage
import com.example.service.GeminiReceiptScanner
import com.example.service.SampleReceipts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SplitNotaViewModel(application: Application) : AndroidViewModel(application) {

    private val bankDetailsStorage = BankDetailsStorage(application)

    private val _receiptData = MutableStateFlow(
        ReceiptData(
            restaurantName = "Taverna Tradițională Românească",
            date = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("ro-RO")).format(Date()),
            items = emptyList(),
            tipPercentage = 10.0,
            splitMode = SplitMode.BY_CONSUMPTION
        )
    )
    val receiptData: StateFlow<ReceiptData> = _receiptData.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _bankDetails = MutableStateFlow(bankDetailsStorage.loadBankDetails())
    val bankDetails: StateFlow<OrganizerBankDetails> = _bankDetails.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    // Mod Offline & Traducere automată în Română
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _autoTranslateToRomanian = MutableStateFlow(true)
    val autoTranslateToRomanian: StateFlow<Boolean> = _autoTranslateToRomanian.asStateFlow()

    // Calculează în timp real împărțirea notei pentru fiecare participant
    val participantShares: StateFlow<List<ParticipantShare>> = combine(
        _receiptData,
        _participants
    ) { receipt, people ->
        calculateShares(receipt, people)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Încărcăm implicit participanții și nota demo cu 4 ape plate, fripturi, etc.
        val defaultPeople = SampleReceipts.getDefaultParticipants()
        _participants.value = defaultPeople
        val sampleBill = SampleReceipts.getSampleReceiptMasaFestiva()
        _receiptData.value = _receiptData.value.copy(
            restaurantName = sampleBill.first,
            items = sampleBill.second
        )
    }

    private fun calculateShares(receipt: ReceiptData, people: List<Participant>): List<ParticipantShare> {
        if (people.isEmpty()) return emptyList()

        return when (receipt.splitMode) {
            SplitMode.EQUAL_PER_PARTICIPANT -> {
                // Împărțire egală pe familie/entitate
                val totalPerGroup = receipt.grandTotal / people.size
                val subtotalPerGroup = receipt.subtotal / people.size
                val tipPerGroup = receipt.tipAmount / people.size

                people.map { p ->
                    ParticipantShare(
                        participant = p,
                        items = emptyList(),
                        subtotal = subtotalPerGroup,
                        tipShare = tipPerGroup,
                        totalToPay = totalPerGroup,
                        isPaid = p.isPaid
                    )
                }
            }

            SplitMode.EQUAL_PER_PERSON -> {
                // Împărțire egală pe cap de persoană
                val totalMembers = people.sumOf { it.memberCount.coerceAtLeast(1) }
                val costPerPerson = receipt.grandTotal / totalMembers
                val subtotalPerPerson = receipt.subtotal / totalMembers
                val tipPerPerson = receipt.tipAmount / totalMembers

                people.map { p ->
                    val memberCount = p.memberCount.coerceAtLeast(1)
                    ParticipantShare(
                        participant = p,
                        items = emptyList(),
                        subtotal = subtotalPerPerson * memberCount,
                        tipShare = tipPerPerson * memberCount,
                        totalToPay = costPerPerson * memberCount,
                        isPaid = p.isPaid
                    )
                }
            }

            SplitMode.BY_CONSUMPTION -> {
                // Împărțire detaliată după consumul exact al fiecăruia + articole comune
                val sharesMap = mutableMapOf<String, MutableList<ParticipantItemConsumption>>()
                people.forEach { sharesMap[it.id] = mutableListOf() }

                // Identificăm articolele comune sau împărțite egal
                receipt.items.forEach { item ->
                    if (item.isSharedEqually) {
                        // Articol comun: împărțit egal între toți participanții
                        val shareFraction = 1.0 / people.size
                        val qtyPerPerson = item.quantity * shareFraction
                        val costPerPerson = item.totalPrice * shareFraction

                        people.forEach { p ->
                            sharesMap[p.id]?.add(
                                ParticipantItemConsumption(
                                    item = item,
                                    consumedQuantity = qtyPerPerson,
                                    itemCost = costPerPerson,
                                    isShared = true
                                )
                            )
                        }
                    } else {
                        // Articol cu distribuire per participant (ex: 4 sticle apă)
                        item.allocations.forEach { (personId, qty) ->
                            if (qty > 0 && sharesMap.containsKey(personId)) {
                                val cost = qty * item.unitPrice
                                sharesMap[personId]?.add(
                                    ParticipantItemConsumption(
                                        item = item,
                                        consumedQuantity = qty,
                                        itemCost = cost,
                                        isShared = false
                                    )
                                )
                            }
                        }

                        // Dacă un produs a rămas parțial nealocat, diferența rămasă se poate împărți ca și comună dacă se dorește
                        val remaining = item.remainingQuantity
                        if (remaining > 0.001) {
                            val remainingCost = remaining * item.unitPrice
                            val remainingCostPerPerson = remainingCost / people.size
                            val remainingQtyPerPerson = remaining / people.size
                            people.forEach { p ->
                                sharesMap[p.id]?.add(
                                    ParticipantItemConsumption(
                                        item = item,
                                        consumedQuantity = remainingQtyPerPerson,
                                        itemCost = remainingCostPerPerson,
                                        isShared = true
                                    )
                                )
                            }
                        }
                    }
                }

                val totalSubtotal = receipt.subtotal.coerceAtLeast(0.01)

                people.map { p ->
                    val userItems = sharesMap[p.id] ?: emptyList()
                    val userSubtotal = userItems.sumOf { it.itemCost }
                    // Bacșiș proporțional cu valoarea consumată
                    val proportion = if (totalSubtotal > 0) userSubtotal / totalSubtotal else (1.0 / people.size)
                    val tipShare = receipt.tipAmount * proportion

                    ParticipantShare(
                        participant = p,
                        items = userItems,
                        subtotal = userSubtotal,
                        tipShare = tipShare,
                        totalToPay = userSubtotal + tipShare,
                        isPaid = p.isPaid
                    )
                }
            }
        }
    }

    private var lastScannedBitmap: Bitmap? = null

    fun scanReceiptImage(bitmap: Bitmap) {
        lastScannedBitmap = bitmap
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            _scanMessage.value = "Optimizare imagine și analiză Gemini AI (cu fallback automat)..."

            val result = GeminiReceiptScanner.scanReceipt(bitmap)
            result.onSuccess { scanResult ->
                _isScanning.value = false
                _scanMessage.value = "Notă scanată cu succes: ${scanResult.items.size} produse identificate și traduse!"
                _receiptData.value = _receiptData.value.copy(
                    restaurantName = scanResult.restaurantName,
                    items = scanResult.items
                )
            }.onFailure { exception ->
                _isScanning.value = false
                _scanError.value = exception.message ?: "Eroare la procesarea imaginii"
                _scanMessage.value = null
            }
        }
    }

    fun retryLastScan() {
        lastScannedBitmap?.let {
            _scanMessage.value = "Reîncercare analiză notă fiscală..."
            scanReceiptImage(it)
        } ?: run {
            _scanError.value = "Nu există o poză salvată pentru reîncercare. Efectuați o nouă scanare din cameră sau galerie."
        }
    }

    fun clearScanMessages() {
        _scanMessage.value = null
        _scanError.value = null
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        if (_isOfflineMode.value) {
            _scanMessage.value = "Modul Offline este activat. Puteți introduce sau edita produsele manual, folosi șabloane și calcula totul fără conexiune la internet."
        } else {
            _scanMessage.value = "Modul Online activat. Puteți folosi scanarea automată cu camera și Gemini AI."
        }
    }

    fun setOfflineMode(active: Boolean) {
        _isOfflineMode.value = active
    }

    fun toggleAutoTranslate() {
        _autoTranslateToRomanian.value = !_autoTranslateToRomanian.value
        if (_autoTranslateToRomanian.value) {
            translateAllItemsToRomanian()
            _scanMessage.value = "Traducerea automată în limba română este activată."
        } else {
            _scanMessage.value = "Traducerea automată a fost dezactivată."
        }
    }

    fun translateAllItemsToRomanian() {
        val updated = _receiptData.value.items.map { item ->
            val translatedName = com.example.service.OfflineTranslator.translateToRomanian(item.name)
            item.copy(name = translatedName)
        }
        _receiptData.value = _receiptData.value.copy(items = updated)
    }

    fun parseReceiptTextOffline(rawText: String) {
        if (rawText.isBlank()) return
        val lines = rawText.lines()
        val newItems = mutableListOf<ReceiptItem>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue

            // Încercăm să extragem cantitate, denumire și preț (ex: "4 apa plata 12" sau "Pizza Diavola 45" sau "2 x Cafea 8")
            val tokens = trimmed.split(Regex("\\s+"))
            if (tokens.size >= 2) {
                var qty = 1.0
                var unitPrice = 0.0
                val nameParts = mutableListOf<String>()

                // Verificăm dacă primul token e cantitate (ex: 4 sau 4x sau 4.0)
                val firstClean = tokens.first().replace("x", "").replace("X", "").replace(",", ".")
                val maybeQty = firstClean.toDoubleOrNull()
                val startIndex = if (maybeQty != null && maybeQty > 0) {
                    qty = maybeQty
                    1
                } else {
                    0
                }

                // Verificăm dacă ultimul token e preț (ex: 14.50 sau 14.50lei sau 14.50€)
                val lastClean = tokens.last().replace(Regex("[^0.9.,]"), "").replace(",", ".")
                val maybePrice = lastClean.toDoubleOrNull()
                val endIndex = if (maybePrice != null && maybePrice >= 0) {
                    unitPrice = maybePrice
                    tokens.size - 1
                } else {
                    tokens.size
                }

                for (i in startIndex until endIndex) {
                    val token = tokens[i]
                    if (token != "x" && token != "X" && token != "-") {
                        nameParts.add(token)
                    }
                }

                val rawName = if (nameParts.isNotEmpty()) nameParts.joinToString(" ") else "Produs"
                val finalName = if (_autoTranslateToRomanian.value) {
                    com.example.service.OfflineTranslator.translateToRomanian(rawName)
                } else {
                    rawName
                }

                newItems.add(
                    ReceiptItem(
                        name = finalName,
                        quantity = qty,
                        unitPrice = if (unitPrice > 0) unitPrice else 10.0,
                        totalPrice = qty * (if (unitPrice > 0) unitPrice else 10.0)
                    )
                )
            }
        }

        if (newItems.isNotEmpty()) {
            _receiptData.value = _receiptData.value.copy(
                items = _receiptData.value.items + newItems
            )
            _scanMessage.value = "S-au adăugat offline ${newItems.size} produse din text!"
        }
    }

    fun loadSample(type: Int) {
        val sample = when (type) {
            1 -> SampleReceipts.getSampleReceiptMasaFestiva()
            2 -> SampleReceipts.getSampleReceiptPizzerie()
            else -> SampleReceipts.getSampleReceiptGreciaTradusa()
        }
        _receiptData.value = _receiptData.value.copy(
            restaurantName = sample.first,
            items = sample.second
        )
        _scanMessage.value = "Nota demonstrativă '${sample.first}' a fost încărcată!"
    }

    // Gestionare Participanți / Familii
    fun addParticipant(name: String, memberCount: Int, phone: String = "", email: String = "", colorIndex: Int = 0) {
        if (name.isBlank()) return
        val newPerson = Participant(
            name = name.trim(),
            memberCount = memberCount.coerceAtLeast(1),
            phoneOrWhatsapp = phone.trim(),
            email = email.trim(),
            colorIndex = colorIndex
        )
        _participants.value = _participants.value + newPerson
    }

    fun removeParticipant(participantId: String) {
        _participants.value = _participants.value.filter { it.id != participantId }
        // Curățăm alocările pentru participantul șters
        val updatedItems = _receiptData.value.items.map { item ->
            if (item.allocations.containsKey(participantId)) {
                val newAllocations = item.allocations.toMutableMap()
                newAllocations.remove(participantId)
                item.copy(allocations = newAllocations)
            } else {
                item
            }
        }
        _receiptData.value = _receiptData.value.copy(items = updatedItems)
    }

    fun toggleParticipantPaid(participantId: String) {
        _participants.value = _participants.value.map {
            if (it.id == participantId) it.copy(isPaid = !it.isPaid) else it
        }
    }

    // Gestionare Produse Notă
    fun addItem(name: String, quantity: Double, unitPrice: Double) {
        if (name.isBlank() || unitPrice < 0) return
        val finalName = if (_autoTranslateToRomanian.value) {
            com.example.service.OfflineTranslator.translateToRomanian(name.trim())
        } else {
            name.trim()
        }
        val newItem = ReceiptItem(
            name = finalName,
            quantity = quantity.coerceAtLeast(1.0),
            unitPrice = unitPrice,
            totalPrice = quantity * unitPrice
        )
        _receiptData.value = _receiptData.value.copy(items = _receiptData.value.items + newItem)
    }

    fun updateItem(updatedItem: ReceiptItem) {
        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.map {
                if (it.id == updatedItem.id) updatedItem else it
            }
        )
    }

    fun removeItem(itemId: String) {
        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.filter { it.id != itemId }
        )
    }

    fun clearAllItems() {
        _receiptData.value = _receiptData.value.copy(
            items = emptyList()
        )
        _scanMessage.value = "Toate produsele au fost șterse din notă."
    }

    // Alocare cantități specifice (ex: 4 sticle apă: Popescu 2, Andrei 1, Ionescu 1)
    fun setParticipantAllocation(itemId: String, participantId: String, newQuantity: Double) {
        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.map { item ->
                if (item.id == itemId) {
                    val allocations = item.allocations.toMutableMap()
                    if (newQuantity <= 0.0) {
                        allocations.remove(participantId)
                    } else {
                        allocations[participantId] = newQuantity
                    }
                    item.copy(
                        allocations = allocations,
                        isSharedEqually = false // Dacă se alocă manual, dezactivăm modul comun
                    )
                } else {
                    item
                }
            }
        )
    }

    fun toggleItemSharedEqually(itemId: String) {
        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.map { item ->
                if (item.id == itemId) {
                    item.copy(isSharedEqually = !item.isSharedEqually)
                } else {
                    item
                }
            }
        )
    }

    fun splitItemEquallyAmongParticipants(itemId: String) {
        val people = _participants.value
        if (people.isEmpty()) return

        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.map { item ->
                if (item.id == itemId) {
                    val equalQty = item.quantity / people.size
                    val newAllocations = people.associate { it.id to equalQty }
                    item.copy(allocations = newAllocations, isSharedEqually = false)
                } else {
                    item
                }
            }
        )
    }

    fun clearItemAllocations(itemId: String) {
        _receiptData.value = _receiptData.value.copy(
            items = _receiptData.value.items.map { item ->
                if (item.id == itemId) {
                    item.copy(allocations = emptyMap(), isSharedEqually = false)
                } else {
                    item
                }
            }
        )
    }

    // Setări mod de împărțire și bacșiș
    fun setSplitMode(mode: SplitMode) {
        _receiptData.value = _receiptData.value.copy(splitMode = mode)
    }

    fun setTipPercentage(percentage: Double) {
        _receiptData.value = _receiptData.value.copy(
            tipPercentage = percentage,
            customTipAmount = null
        )
    }

    fun setCustomTip(customAmount: Double?) {
        _receiptData.value = _receiptData.value.copy(customTipAmount = customAmount)
    }

    fun setRestaurantName(name: String) {
        _receiptData.value = _receiptData.value.copy(restaurantName = name)
    }

    fun updateBankDetails(details: OrganizerBankDetails) {
        _bankDetails.value = details
        bankDetailsStorage.saveBankDetails(details)
    }
}
