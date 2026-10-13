package com.example.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.OrganizerBankDetails
import com.example.model.ParticipantShare
import com.example.model.ReceiptData
import java.util.Locale

object SharingService {

    /**
     * Generează mesajul detaliat în limba română pentru un participant/familie,
     * incluzând detaliile RoPay Alias pe număr de telefon și datele bancare clasice (Nume Beneficiar & Cont IBAN).
     * Nu include coduri de bare sau coduri QR.
     */
    fun formatParticipantMessage(
        restaurantName: String,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val totalFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        val subtotalFormatted = String.format(Locale.US, "%.2f", share.subtotal)
        val tipFormatted = String.format(Locale.US, "%.2f", share.tipShare)

        val sb = StringBuilder()
        sb.append("🍽️ *SplitNota - $restaurantName*\n")
        sb.append("Salut, *${share.participant.name}*! 👋\n\n")
        sb.append("💰 *Total de plată: $totalFormatted ${bankDetails.currency}*\n\n")

        if (share.items.isNotEmpty()) {
            sb.append("🧾 *Detaliu consum:*\n")
            share.items.forEach { itemConsumption ->
                val qtyFormatted = if (itemConsumption.consumedQuantity % 1.0 == 0.0) {
                    itemConsumption.consumedQuantity.toInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", itemConsumption.consumedQuantity)
                }
                val costFormatted = String.format(Locale.US, "%.2f", itemConsumption.itemCost)
                val sharedLabel = if (itemConsumption.isShared) " _(împărțit)_" else ""
                sb.append("• $qtyFormatted x ${itemConsumption.item.name}: $costFormatted ${bankDetails.currency}$sharedLabel\n")
            }
            sb.append("\nSubtotal consum: $subtotalFormatted ${bankDetails.currency}\n")
        }

        if (share.tipShare > 0.01) {
            sb.append("Bacșiș / Tips: $tipFormatted ${bankDetails.currency}\n")
        }

        if (bankDetails.phone.isNotBlank()) {
            sb.append("\n⚡ *Plată instant RoPay Alias (pe Număr de Telefon):*\n")
            sb.append("• Telefon: *${bankDetails.phone}*\n")
            sb.append("• Beneficiar: ${bankDetails.beneficiaryName} (${bankDetails.bankName})\n")
            sb.append("• Sumă: *$totalFormatted ${bankDetails.currency}*\n")
            sb.append("• Notă plată: ${bankDetails.paymentNotePrefix} - ${share.participant.name}\n")
            sb.append("📲 _În aplicația ta bancară (BT Pay, George BCR, CEC Bank, Libra etc.), alege Transfer RoPay pe număr de telefon (instant și fără comisioane)!_\n")
        }

        sb.append("\n💳 *Transfer bancar (IBAN):*\n")
        sb.append("• Beneficiar (Nume): ${bankDetails.beneficiaryName}\n")
        sb.append("• Cont IBAN: ${bankDetails.iban}\n")
        sb.append("• Banca: ${bankDetails.bankName}\n")
        sb.append("• Sumă: $totalFormatted ${bankDetails.currency}\n")
        sb.append("• Notă plată: ${bankDetails.paymentNotePrefix} - ${share.participant.name}\n")

        return sb.toString()
    }

    /**
     * Generează mesajul scurt și direct, optimizat pentru plata instant prin RoPay Alias pe număr de telefon.
     */
    fun formatRoPayShortMessage(
        restaurantName: String,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val totalFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        return """
            ⚡ *Plată instant RoPay - $restaurantName*
            Salut, *${share.participant.name}*! 👋
            Trimite suma de *$totalFormatted ${bankDetails.currency}* prin RoPay către:
            📞 Număr telefon: *${bankDetails.phone}*
            Titular: *${bankDetails.beneficiaryName}* (${bankDetails.bankName})
            Descriere: *${bankDetails.paymentNotePrefix} - ${share.participant.name}*
            
            📲 _Deschide aplicația ta bancară și alege Transfer RoPay pe număr de telefon!_
        """.trimIndent()
    }

    /**
     * Generează mesajul complet pentru tot grupul.
     */
    fun formatGroupSummaryMessage(
        receiptData: ReceiptData,
        bankDetails: OrganizerBankDetails,
        shares: List<ParticipantShare>
    ): String {
        val totalFormatted = String.format(Locale.US, "%.2f", receiptData.grandTotal)
        val sb = StringBuilder()
        sb.append("🍽️ *Notă de plată restaurant - ${receiptData.restaurantName}*\n")
        sb.append("Total notă: *$totalFormatted ${bankDetails.currency}*\n\n")
        sb.append("👥 *Împărțire pe familii & persoane:*\n")

        shares.forEach { share ->
            val amountFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
            val paidStatus = if (share.isPaid) "✅ Achitat" else "⏳ De achitat"
            sb.append("• *${share.participant.name}*: $amountFormatted ${bankDetails.currency} ($paidStatus)\n")
        }

        if (bankDetails.phone.isNotBlank()) {
            sb.append("\n⚡ *Plată instant RoPay Alias (pe Număr Telefon):* ${bankDetails.phone}\n")
        }
        sb.append("\n💳 *Cont pentru decontare (IBAN):*\n")
        sb.append("Beneficiar: ${bankDetails.beneficiaryName}\n")
        sb.append("Cont IBAN: ${bankDetails.iban} (${bankDetails.bankName})\n")

        return sb.toString()
    }

    /**
     * Trimite mesajul cu datele bancare și RoPay pe WhatsApp ca text curat și direct.
     */
    fun sendViaWhatsApp(
        context: Context,
        restaurantName: String,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ) {
        val message = formatParticipantMessage(restaurantName, bankDetails, share)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        intent.setPackage("com.whatsapp")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b")
                context.startActivity(intent)
            } catch (e2: Exception) {
                intent.setPackage(null)
                context.startActivity(Intent.createChooser(intent, "Trimite către ${share.participant.name}"))
            }
        }
    }

    /**
     * Trimite mesajul rapid RoPay Alias direct pe WhatsApp.
     */
    fun sendRoPayViaWhatsApp(
        context: Context,
        restaurantName: String,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ) {
        val message = formatRoPayShortMessage(restaurantName, bankDetails, share)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        intent.setPackage("com.whatsapp")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b")
                context.startActivity(intent)
            } catch (e2: Exception) {
                intent.setPackage(null)
                context.startActivity(Intent.createChooser(intent, "Trimite RoPay către ${share.participant.name}"))
            }
        }
    }

    /**
     * Trimite detalii RoPay prin SMS către numărul participantului sau prin aplicația implicită de mesagerie.
     */
    fun sendViaSms(
        context: Context,
        recipientPhone: String,
        message: String
    ) {
        try {
            val cleanPhone = recipientPhone.replace(" ", "").trim()
            val uri = if (cleanPhone.isNotBlank()) Uri.parse("smsto:$cleanPhone") else Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard(context, message, "Detalii RoPay")
            Toast.makeText(context, "Textul RoPay a fost copiat în clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Trimite prin Email cu subiect și date de plată (fără atașamente de cod de bare).
     */
    fun sendViaEmail(
        context: Context,
        restaurantName: String,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ) {
        val message = formatParticipantMessage(restaurantName, bankDetails, share)
        val subject = "Nota de plată $restaurantName - ${share.participant.name} (${String.format(Locale.US, "%.2f", share.totalToPay)} ${bankDetails.currency})"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            if (share.participant.email.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(share.participant.email))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Trimite Email către ${share.participant.name}"))
        } catch (e: Exception) {
            Toast.makeText(context, "Nu s-a găsit o aplicație de email", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Partajează textul sumarului de grup în orice aplicație.
     */
    fun shareGroupSummary(
        context: Context,
        receiptData: ReceiptData,
        bankDetails: OrganizerBankDetails,
        shares: List<ParticipantShare>
    ) {
        val summary = formatGroupSummaryMessage(receiptData, bankDetails, shares)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            putExtra(Intent.EXTRA_SUBJECT, "Sumar notă ${receiptData.restaurantName}")
        }
        context.startActivity(Intent.createChooser(intent, "Trimite sumar notă de plată"))
    }

    /**
     * Copiază în clipboard datele bancare cu feedback Toast.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Date plată") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiat în clipboard: $text", Toast.LENGTH_SHORT).show()
    }
}
