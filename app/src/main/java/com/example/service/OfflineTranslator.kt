package com.example.service

import java.util.Locale

object OfflineTranslator {

    // Dicționar restaurant multilingv (Italiană, Greacă, Engleză, Franceză, Germană, Spaniolă -> Română)
    private val dictionary: Map<String, String> = mapOf(
        // Băuturi & Apă
        "acqua naturale" to "Apă plată",
        "acqua liscia" to "Apă plată",
        "acqua frizzante" to "Apă minerală carbogazoasă",
        "acqua minerale" to "Apă minerală",
        "acqua" to "Apă",
        "still water" to "Apă plată",
        "sparkling water" to "Apă carbogazoasă",
        "mineral water" to "Apă minerală",
        "water" to "Apă",
        "eau plate" to "Apă plată",
        "eau gazeuse" to "Apă minerală carbogazoasă",
        "eau" to "Apă",
        "stilles wasser" to "Apă plată",
        "mineralwasser" to "Apă minerală",
        "wasser" to "Apă",
        "agua con gas" to "Apă minerală carbogazoasă",
        "agua sin gas" to "Apă plată",
        "agua" to "Apă",
        "νερό" to "Apă",

        // Cafea & Ceai
        "caffè" to "Cafea",
        "caffe" to "Cafea",
        "espresso" to "Espresso",
        "cappuccino" to "Cappuccino",
        "coffee" to "Cafea",
        "tea" to "Ceai",
        "the" to "Ceai",
        "thé" to "Ceai",
        "tee" to "Ceai",
        "kaffee" to "Cafea",
        "καφές" to "Cafea",

        // Alcool & Băuturi
        "birra" to "Bere",
        "beer" to "Bere",
        "bière" to "Bere",
        "bier" to "Bere",
        "cerveza" to "Bere",
        "μπύρα" to "Bere",
        "vino rosso" to "Vin roșu",
        "vino bianco" to "Vin alb",
        "vino" to "Vin",
        "red wine" to "Vin roșu",
        "white wine" to "Vin alb",
        "wine" to "Vin",
        "vin rouge" to "Vin roșu",
        "vin blanc" to "Vin alb",
        "rotwein" to "Vin roșu",
        "weißwein" to "Vin alb",
        "κρασί" to "Vin",

        // Pâine & Serviciu masă
        "coperto" to "Taxă de masă / Serviciu (Coperto)",
        "servizio" to "Serviciu",
        "service charge" to "Taxă de serviciu",
        "couvert" to "Taxă de masă (Couvert)",
        "bread" to "Pâine",
        "pane" to "Pâine",
        "pain" to "Pâine",
        "brot" to "Pâine",
        "pan" to "Pâine",
        "psomi" to "Pâine",
        "ψωμί" to "Pâine",

        // Mâncare & Feluri
        "pizza margherita" to "Pizza Margherita (mozzarella, sos roșii)",
        "pizza quattro formaggi" to "Pizza 4 Brânzeturi",
        "pizza diavola" to "Pizza Diavola (picantă)",
        "pasta carbonara" to "Paste Carbonara",
        "spaghetti carbonara" to "Spaghete Carbonara",
        "spaghetti alle vongole" to "Spaghete cu scoici",
        "patatine fritte" to "Cartofi prăjiți",
        "french fries" to "Cartofi prăjiți",
        "fries" to "Cartofi prăjiți",
        "pommes frites" to "Cartofi prăjiți",
        "pommes" to "Cartofi prăjiți",
        "insalata mista" to "Salată mixtă",
        "insalata verde" to "Salată verde",
        "insalata" to "Salată",
        "salad" to "Salată",
        "salat" to "Salată",
        "greek salad" to "Salată grecească (Choriatiki)",
        "choriatiki" to "Salată grecească",
        "χωριάτικη" to "Salată grecească",
        "tzatziki" to "Sos Tzatziki cu castraveți și usturoi",
        "τζατζίκι" to "Tzatziki",
        "souvlaki" to "Frigărui Souvlaki",
        "σουβλάκι" to "Frigărui Souvlaki",
        "gyros" to "Gyros",
        "γύρος" to "Gyros",
        "calamari" to "Calamari la grătar / prăjiți",
        "kalamari" to "Calamari",
        "καλαμάρι" to "Calamari",
        "polpo" to "Caracatiță",
        "octopus" to "Caracatiță",
        "pulpo" to "Caracatiță",
        "pesce spada" to "Pește spadă",
        "pesce" to "Pește",
        "fish" to "Pește",
        "fisch" to "Pește",
        "poisson" to "Pește",
        "ψάρι" to "Pește",
        "carne" to "Carne",
        "meat" to "Carne",
        "fleisch" to "Carne",
        "viande" to "Carne",
        "bistecca" to "Friptură de vită / Mușchi",
        "steak" to "Friptură / Steak",
        "pollo" to "Pui",
        "chicken" to "Pui",
        "hähnchen" to "Pui",
        "poulet" to "Pui",
        "κοτόπουλο" to "Pui",
        "maiale" to "Porc",
        "pork" to "Porc",
        "schweinefleisch" to "Porc",
        "manzo" to "Vită",
        "beef" to "Vită",
        "rindfleisch" to "Vită",
        "agnello" to "Miel",
        "lamb" to "Miel",
        "lamm" to "Miel",
        "dolce" to "Desert",
        "dessert" to "Desert",
        "nachtisch" to "Desert",
        "tiramisu" to "Tiramisu",
        "gelato" to "Înghețată",
        "ice cream" to "Înghețată",
        "glace" to "Înghețată",
        "eis" to "Înghețată",
        "panna cotta" to "Panna Cotta"
    )

    /**
     * Traduce automat în limba română denumirea unui produs, dacă se regăsește în dicționar sau conține termeni cheie.
     */
    fun translateToRomanian(originalName: String): String {
        val clean = originalName.trim().lowercase(Locale.ROOT)

        // Verificare potrivire exactă
        dictionary[clean]?.let { return "$it ($originalName)" }

        // Verificare dacă începe sau conține un termen cunoscut
        for ((foreign, translation) in dictionary) {
            if (clean == foreign) {
                return "$translation ($originalName)"
            }
        }

        // Traducere parțială a cuvintelor cheie
        var translated = originalName
        var hasMatch = false

        for ((foreign, romanian) in dictionary) {
            val regex = Regex("\\b${Regex.escape(foreign)}\\b", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(translated)) {
                translated = regex.replace(translated, romanian)
                hasMatch = true
            }
        }

        return if (hasMatch && translated != originalName) {
            "$translated ($originalName)"
        } else {
            originalName
        }
    }
}
