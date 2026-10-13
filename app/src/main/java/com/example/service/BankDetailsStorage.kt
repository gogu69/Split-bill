package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.model.OrganizerBankDetails

/**
 * Persistență locală pentru datele bancare ale organizatorului.
 * Datele sunt salvate în SharedPreferences pentru a rămâne memorate permanent în aplicație
 * între sesiuni și reporniri.
 */
class BankDetailsStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadBankDetails(): OrganizerBankDetails {
        val hasSaved = prefs.getBoolean(KEY_HAS_SAVED, false)
        if (!hasSaved) {
            return OrganizerBankDetails()
        }
        return OrganizerBankDetails(
            beneficiaryName = prefs.getString(KEY_BENEFICIARY, "Ion Popa") ?: "Ion Popa",
            iban = prefs.getString(KEY_IBAN, "RO49INGB0000999912345678") ?: "RO49INGB0000999912345678",
            bankName = prefs.getString(KEY_BANK, "ING Bank") ?: "ING Bank",
            revolutTag = "",
            phone = prefs.getString(KEY_PHONE, "+40721234567") ?: "+40721234567",
            currency = prefs.getString(KEY_CURRENCY, "RON") ?: "RON",
            paymentNotePrefix = prefs.getString(KEY_PREFIX, "Nota restaurant") ?: "Nota restaurant"
        )
    }

    fun saveBankDetails(details: OrganizerBankDetails) {
        prefs.edit()
            .putBoolean(KEY_HAS_SAVED, true)
            .putString(KEY_BENEFICIARY, details.beneficiaryName)
            .putString(KEY_IBAN, details.iban)
            .putString(KEY_BANK, details.bankName)
            .putString(KEY_PHONE, details.phone)
            .putString(KEY_CURRENCY, details.currency)
            .putString(KEY_PREFIX, details.paymentNotePrefix)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "organizer_bank_details_prefs"
        private const val KEY_HAS_SAVED = "has_saved_organizer_data"
        private const val KEY_BENEFICIARY = "beneficiary_name"
        private const val KEY_IBAN = "iban"
        private const val KEY_BANK = "bank_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_PREFIX = "payment_note_prefix"
    }
}
