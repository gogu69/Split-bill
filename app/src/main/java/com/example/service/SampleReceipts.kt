package com.example.service

import com.example.model.Participant
import com.example.model.ReceiptItem
import java.util.UUID

object SampleReceipts {

    fun getDefaultParticipants(): List<Participant> = listOf(
        Participant(
            id = "fam_popescu",
            name = "Familia Popescu",
            memberCount = 3,
            colorIndex = 0,
            phoneOrWhatsapp = "+40722111222",
            email = "popescu@email.ro"
        ),
        Participant(
            id = "fam_ionescu",
            name = "Familia Ionescu",
            memberCount = 2,
            colorIndex = 1,
            phoneOrWhatsapp = "+40733444555",
            email = "ionescu@email.ro"
        ),
        Participant(
            id = "andrei",
            name = "Andrei & Elena",
            memberCount = 2,
            colorIndex = 2,
            phoneOrWhatsapp = "+40744777888",
            email = "andrei@email.ro"
        ),
        Participant(
            id = "mihai",
            name = "Mihai",
            memberCount = 1,
            colorIndex = 3,
            phoneOrWhatsapp = "+40755999000",
            email = "mihai@email.ro"
        )
    )

    fun getSampleReceiptMasaFestiva(): Pair<String, List<ReceiptItem>> {
        val restaurant = "Taverna Tradițională Românească"
        val items = listOf(
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Apă plată Bucovina 0.75L",
                quantity = 4.0,
                unitPrice = 14.0,
                totalPrice = 56.0,
                allocations = mapOf("fam_popescu" to 2.0, "fam_ionescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Apă minerală Borsec 0.75L",
                quantity = 2.0,
                unitPrice = 14.0,
                totalPrice = 28.0,
                allocations = mapOf("mihai" to 1.0, "fam_ionescu" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Platou cald tradițional boieresc (4 pers)",
                quantity = 1.0,
                unitPrice = 145.0,
                totalPrice = 145.0,
                isSharedEqually = true // Împărțit la toți în mod egal!
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Coaste de porc BBQ cu cartofi la cuptor",
                quantity = 2.0,
                unitPrice = 64.0,
                totalPrice = 128.0,
                allocations = mapOf("fam_popescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Mușchi de vită la grătar cu sos piper verde",
                quantity = 1.0,
                unitPrice = 89.0,
                totalPrice = 89.0,
                allocations = mapOf("mihai" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Păstrăv la grătar cu mămăliguță și mujdei",
                quantity = 2.0,
                unitPrice = 52.0,
                totalPrice = 104.0,
                allocations = mapOf("fam_ionescu" to 2.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Ciorbă de burtă cu smântână și ardei",
                quantity = 2.0,
                unitPrice = 26.0,
                totalPrice = 52.0,
                allocations = mapOf("fam_popescu" to 1.0, "mihai" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Pâine de casă cu maia și unt",
                quantity = 3.0,
                unitPrice = 8.0,
                totalPrice = 24.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Papanași cu smântână și dulceață afine",
                quantity = 3.0,
                unitPrice = 32.0,
                totalPrice = 96.0,
                allocations = mapOf("fam_popescu" to 1.0, "fam_ionescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Vin roșu Fetească Neagră Purcari 0.75L",
                quantity = 1.0,
                unitPrice = 110.0,
                totalPrice = 110.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Espresso Illy dublu",
                quantity = 3.0,
                unitPrice = 12.0,
                totalPrice = 36.0,
                allocations = mapOf("fam_popescu" to 1.0, "fam_ionescu" to 1.0, "mihai" to 1.0)
            )
        )
        return Pair(restaurant, items)
    }

    fun getSampleReceiptPizzerie(): Pair<String, List<ReceiptItem>> {
        val restaurant = "Trattoria & Pizzeria Bella Italia"
        val items = listOf(
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Apă plată Aqua Carpatica 0.75L",
                quantity = 3.0,
                unitPrice = 13.0,
                totalPrice = 39.0,
                allocations = mapOf("fam_popescu" to 1.0, "fam_ionescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Pizza Diavola (sos picant, salam)",
                quantity = 2.0,
                unitPrice = 44.0,
                totalPrice = 88.0,
                allocations = mapOf("fam_popescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Pizza Quattro Formaggi",
                quantity = 1.0,
                unitPrice = 48.0,
                totalPrice = 48.0,
                allocations = mapOf("fam_ionescu" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Paste Penne Quattro Formaggi",
                quantity = 1.0,
                unitPrice = 46.0,
                totalPrice = 46.0,
                allocations = mapOf("fam_ionescu" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Focaccia cu rozmarin și usturoi",
                quantity = 2.0,
                unitPrice = 18.0,
                totalPrice = 36.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Bere draught Peroni Nastro Azzurro 0.4L",
                quantity = 4.0,
                unitPrice = 16.0,
                totalPrice = 64.0,
                allocations = mapOf("fam_popescu" to 2.0, "andrei" to 2.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Tiramisu della Casa",
                quantity = 2.0,
                unitPrice = 28.0,
                totalPrice = 56.0,
                allocations = mapOf("fam_ionescu" to 1.0, "fam_popescu" to 1.0)
            )
        )
        return Pair(restaurant, items)
    }

    fun getSampleReceiptGreciaTradusa(): Pair<String, List<ReceiptItem>> {
        val restaurant = "Taverna Akrotiri (Grecia - Tradusă)"
        val items = listOf(
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Apă plată 1L (Nero)",
                quantity = 4.0,
                unitPrice = 12.0,
                totalPrice = 48.0,
                allocations = mapOf("fam_popescu" to 2.0, "fam_ionescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Salată grecească cu brânză feta (Choriatiki)",
                quantity = 2.0,
                unitPrice = 42.0,
                totalPrice = 84.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Sos Tzatziki cu usturoi și castraveți",
                quantity = 2.0,
                unitPrice = 24.0,
                totalPrice = 48.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Pâine proaspătă de casă (Psomi / Couvert)",
                quantity = 4.0,
                unitPrice = 8.0,
                totalPrice = 32.0,
                isSharedEqually = true
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Frigărui Souvlaki de pui cu cartofi",
                quantity = 2.0,
                unitPrice = 58.0,
                totalPrice = 116.0,
                allocations = mapOf("fam_popescu" to 1.0, "andrei" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Calamari proaspeți la grătar",
                quantity = 2.0,
                unitPrice = 68.0,
                totalPrice = 136.0,
                allocations = mapOf("fam_ionescu" to 1.0, "mihai" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Pește dorada la grătar",
                quantity = 1.0,
                unitPrice = 85.0,
                totalPrice = 85.0,
                allocations = mapOf("fam_ionescu" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Bere grecească Mythos 0.5L",
                quantity = 4.0,
                unitPrice = 18.0,
                totalPrice = 72.0,
                allocations = mapOf("fam_popescu" to 2.0, "andrei" to 1.0, "mihai" to 1.0)
            ),
            ReceiptItem(
                id = UUID.randomUUID().toString(),
                name = "Iaurt grecesc cu miere și nuci",
                quantity = 2.0,
                unitPrice = 26.0,
                totalPrice = 52.0,
                allocations = mapOf("fam_popescu" to 1.0, "fam_ionescu" to 1.0)
            )
        )
        return Pair(restaurant, items)
    }
}
