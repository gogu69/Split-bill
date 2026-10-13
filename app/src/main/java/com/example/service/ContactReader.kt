package com.example.service

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class ExtractedContact(
    val name: String,
    val phone: String = "",
    val email: String = "",
    val memberCount: Int = 1
)

object ContactReader {

    /**
     * Extrage toate datele disponibile (nume, telefon, email, număr membri familie)
     * dintr-un contact selectat prin ActivityResultContracts.PickContact().
     */
    fun readContactDetails(context: Context, contactUri: Uri): ExtractedContact {
        var name = ""
        var phone = ""
        var email = ""
        var memberCount = 1

        val contentResolver = context.contentResolver

        var contactId: String? = null

        // 1. Citim numele și ID-ul din URI-ul contactului returnat
        try {
            contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (nameIdx >= 0) {
                        name = cursor.getString(nameIdx).orEmpty()
                    }
                    if (idIdx >= 0) {
                        contactId = cursor.getString(idIdx)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Metoda oficială Android pentru permisiunea acordată prin PickContact:
        // Interogarea directorului /data aparținând acestui contact specific.
        // Aceasta NU necesită permisiune globală READ_CONTACTS deoarece URI-ul a fost acordat de selector.
        try {
            val dataUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
            contentResolver.query(
                dataUri,
                arrayOf(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.Data.DATA1,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Email.ADDRESS
                ),
                null,
                null,
                null
            )?.use { dataCursor ->
                val mimeTypeIdx = dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
                val data1Idx = dataCursor.getColumnIndex(ContactsContract.Data.DATA1)
                val phoneNumIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneTypeIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val emailIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

                var foundMobilePhone: String? = null
                var foundAnyPhone: String? = null

                while (dataCursor.moveToNext()) {
                    val mimeType = if (mimeTypeIdx >= 0) dataCursor.getString(mimeTypeIdx) else null

                    when (mimeType) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                            val num = if (phoneNumIdx >= 0) {
                                dataCursor.getString(phoneNumIdx)
                            } else if (data1Idx >= 0) {
                                dataCursor.getString(data1Idx)
                            } else null

                            val type = if (phoneTypeIdx >= 0) dataCursor.getInt(phoneTypeIdx) else -1

                            if (!num.isNullOrBlank()) {
                                val cleanNum = num.trim()
                                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                                    foundMobilePhone = cleanNum
                                } else if (foundAnyPhone == null) {
                                    foundAnyPhone = cleanNum
                                }
                            }
                        }

                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                            val mail = if (emailIdx >= 0) {
                                dataCursor.getString(emailIdx)
                            } else if (data1Idx >= 0) {
                                dataCursor.getString(data1Idx)
                            } else null

                            if (!mail.isNullOrBlank() && email.isBlank()) {
                                email = mail.trim()
                            }
                        }

                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                            if (name.isBlank()) {
                                val dispName = if (data1Idx >= 0) dataCursor.getString(data1Idx) else null
                                if (!dispName.isNullOrBlank()) {
                                    name = dispName.trim()
                                }
                            }
                        }
                    }
                }

                if (phone.isBlank()) {
                    phone = (foundMobilePhone ?: foundAnyPhone).orEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback: Dacă telefonul sau emailul nu au fost găsite prin sub-directorul /data,
        // și avem contactId, încercăm tabelele globale Phone și Email (funcționează dacă READ_CONTACTS este acordat)
        if (contactId != null && (phone.isBlank() || email.isBlank())) {
            try {
                if (phone.isBlank()) {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE
                        ),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { pc ->
                        var fallbackMobile: String? = null
                        var fallbackAny: String? = null
                        val pIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val tIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                        while (pc.moveToNext()) {
                            if (pIdx >= 0) {
                                val num = pc.getString(pIdx)
                                val type = if (tIdx >= 0) pc.getInt(tIdx) else -1
                                if (!num.isNullOrBlank()) {
                                    if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                                        fallbackMobile = num.trim()
                                    } else if (fallbackAny == null) {
                                        fallbackAny = num.trim()
                                    }
                                }
                            }
                        }
                        phone = (fallbackMobile ?: fallbackAny).orEmpty()
                    }
                }

                if (email.isBlank()) {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { ec ->
                        if (ec.moveToFirst()) {
                            val eIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            if (eIdx >= 0) {
                                email = ec.getString(eIdx).orEmpty().trim()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Poate arunca SecurityException dacă permisiunea globală nu este activă, se ignoră în favoarea pasului 2
                e.printStackTrace()
            }
        }

        // 4. Detectare inteligentă număr de membri din numele contactului dacă este vorba de o familie
        // Ex: "Familia Ionescu (3)", "Popescu (4)", "Dan & Andreea (2)", "Mihai (2 pers)"
        try {
            val countRegex = Regex("""\(\s*(\d+)\s*(?:pers|persoane|membri)?\s*\)""", RegexOption.IGNORE_CASE)
            val match = countRegex.find(name)
            if (match != null) {
                val count = match.groupValues[1].toIntOrNull()
                if (count != null && count in 1..20) {
                    memberCount = count
                }
            } else if (name.contains("&") || name.contains(" și ", ignoreCase = true) || name.contains(" si ", ignoreCase = true)) {
                // Dacă numele conține "&" sau "și" (ex: "Andrei & Maria"), implicit 2 persoane
                memberCount = 2
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ExtractedContact(
            name = name,
            phone = phone,
            email = email,
            memberCount = memberCount
        )
    }
}
