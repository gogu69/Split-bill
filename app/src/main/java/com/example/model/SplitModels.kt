package com.example.model

import java.util.UUID

/**
 * Reprezintă o persoană sau o familie care împarte nota.
 */
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val memberCount: Int = 1, // Pentru familii cu mai mulți membri (ex: 2 adulți + 1 copil)
    val colorIndex: Int = 0,
    val phoneOrWhatsapp: String = "",
    val email: String = "",
    val isPaid: Boolean = false
)

/**
 * Reprezintă un articol de pe nota fiscală.
 * allocations: Map de participantId -> cantitate consumată (ex: "id_popescu" -> 2.0 pentru 2 sticle de apă)
 */
data class ReceiptItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double = 1.0,
    val unitPrice: Double,
    val totalPrice: Double = quantity * unitPrice,
    val allocations: Map<String, Double> = emptyMap(), // participantId -> quantity assigned
    val isSharedEqually: Boolean = false // Dacă se împarte la toți în mod egal
) {
    val allocatedQuantity: Double
        get() = if (isSharedEqually) quantity else allocations.values.sum()

    val remainingQuantity: Double
        get() = (quantity - allocatedQuantity).coerceAtLeast(0.0)

    val isFullyAllocated: Boolean
        get() = isSharedEqually || (allocatedQuantity >= quantity - 0.001)
}

/**
 * Modul de calcul pentru notă:
 * BY_CONSUMPTION: fiecare plătește strict ce a consumat + cota de produse comune
 * EQUAL_PER_PERSON: nota totală împărțită egal la numărul total de persoane (persoane fizice)
 * EQUAL_PER_PARTICIPANT: nota împărțită egal la numărul de familii/grupuri înscrise
 */
enum class SplitMode {
    BY_CONSUMPTION,
    EQUAL_PER_PERSON,
    EQUAL_PER_PARTICIPANT
}

/**
 * Informațiile bancare ale organizatorului (cel care plătește nota la ospătar)
 */
data class OrganizerBankDetails(
    val beneficiaryName: String = "Ion Popa",
    val iban: String = "RO49INGB0000999912345678",
    val bankName: String = "ING Bank",
    val revolutTag: String = "ionpopa",
    val phone: String = "+40721234567",
    val currency: String = "RON",
    val paymentNotePrefix: String = "Nota restaurant"
)

/**
 * Sumar consum pentru un participant/familie
 */
data class ParticipantItemConsumption(
    val item: ReceiptItem,
    val consumedQuantity: Double,
    val itemCost: Double,
    val isShared: Boolean
)

data class ParticipantShare(
    val participant: Participant,
    val items: List<ParticipantItemConsumption>,
    val subtotal: Double,
    val tipShare: Double,
    val totalToPay: Double,
    val isPaid: Boolean
)

/**
 * Date complete notă de plată
 */
data class ReceiptData(
    val restaurantName: String = "Restaurant Trattoria",
    val date: String = "",
    val items: List<ReceiptItem> = emptyList(),
    val tipPercentage: Double = 10.0,
    val customTipAmount: Double? = null,
    val splitMode: SplitMode = SplitMode.BY_CONSUMPTION
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val tipAmount: Double
        get() = customTipAmount ?: (subtotal * (tipPercentage / 100.0))

    val grandTotal: Double
        get() = subtotal + tipAmount
}
