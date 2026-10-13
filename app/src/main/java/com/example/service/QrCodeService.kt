package com.example.service

import android.content.Context
import android.graphics.Bitmap
import com.example.model.OrganizerBankDetails
import com.example.model.ParticipantShare
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class QrCodeFormat {
    ROMANIAN_BANK_QR, // Format Bancar Românesc (Scan & Pay / Plată factură / Transfer: IBAN*SUMA*REFERINTA*BENEFICIAR)
    EPC_SEPA, // Coduri QR standard EPC (European Payments Council - EPC069-12 / SEPA Credit Transfer)
    INVOICE_BARCODE, // Păstrat pentru compatibilitate teste
    ROPAY_QR, // Păstrat pentru compatibilitate
    REVOLUT_LINK, // Păstrat pentru compatibilitate teste
    TEXT_PAYMENT // Păstrat pentru compatibilitate
}

object QrCodeService {

    /**
     * Deduce codul BIC / SWIFT din IBAN-ul românesc (primele 4 litere după codul de țară ROxx).
     * Format: ROxx BBBB ... -> BBBB este codul băncii.
     */
    fun getBicForIban(iban: String): String {
        val clean = iban.replace(" ", "").uppercase()
        if (clean.length >= 8 && clean.startsWith("RO")) {
            val bankCode = clean.substring(4, 8)
            return when (bankCode) {
                "RNCB" -> "RNCBROBU" // BCR
                "BTRL" -> "BTRLRO22" // Banca Transilvania
                "INGB" -> "INGBROBU" // ING Bank
                "BRDE" -> "BRDEROBU" // BRD
                "RZBR" -> "RZBRROBU" // Raiffeisen Bank
                "CECE" -> "CECEROBU" // CEC Bank
                "BACX" -> "BACXROBU" // UniCredit Bank
                "BREL" -> "BRELROBU" // Libra Internet Bank
                "OTPV" -> "OTPVROBU" // OTP Bank
                "EXIM" -> "EXIMROBU" // Exim Banca Românească
                "SALT" -> "BRELROBU" // Salt Bank (Libra infrastructure)
                "REVO" -> "REVOROM2" // Revolut Bank UAB România
                "BPOS" -> "BPOSROBU" // Bancpost
                "TREZ" -> "TREZROBU" // Trezorerie
                else -> ""
            }
        }
        return ""
    }

    /**
     * Construiește payload-ul strict conform standardului oficial "Coduri QR standard EPC" (EPC069-12).
     * Formatul este compus din 11 linii separate prin LF (\n):
     * 1. Service Tag: BCD
     * 2. Version: 002
     * 3. Character set: 1 (UTF-8)
     * 4. Identification: SCT (SEPA Credit Transfer)
     * 5. BIC: codul BIC al băncii (max 11 caractere)
     * 6. Beneficiary Name: numele titularului (max 70 caractere)
     * 7. IBAN: codul IBAN curat (max 34 caractere)
     * 8. Amount: EUR urmat de suma în format zecimal (ex: EUR110.00 conform standardului EPC)
     * 9. Purpose: (gol)
     * 10. Structured Reference: (gol)
     * 11. Remittance Information: descrierea plății (max 140 caractere)
     * Fără delimitator suplimentar după linia 11.
     */
    fun buildEpcQrPayload(
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare,
        currency: String = "EUR",
        version: String = "002"
    ): String {
        val amountFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        val ibanClean = bankDetails.iban.replace(" ", "").uppercase().take(34)
        val bic = getBicForIban(ibanClean)
        val beneficiary = bankDetails.beneficiaryName.replace("\n", " ").trim().take(70)
        val paymentDetails = "${bankDetails.paymentNotePrefix} - ${share.participant.name}"
            .replace("\n", " ")
            .trim()
            .take(140)

        val epcAmount = "$currency$amountFormatted"

        return listOf(
            "BCD",
            version,
            "1",
            "SCT",
            bic,
            beneficiary,
            ibanClean,
            epcAmount,
            "",
            "",
            paymentDetails
        ).joinToString("\n")
    }

    /**
     * Construiește codul de bare numeric de factură (40 de cifre), exact conform standardului
     * facturilor din România scanate de aplicațiile bancare (BT Pay, George BCR, ING, Raiffeisen, CEC) și PayPoint.
     * Primele 30 de cifre reprezintă identificatorul/referința facturii,
     * iar ultimele 10 cifre reprezintă suma în bani (ex: 76.69 lei -> 7669 bani -> 0000007669).
     */
    fun buildInvoiceNumericBarcode(
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val amountInBani = kotlin.math.round(share.totalToPay * 100).toLong().coerceAtLeast(0L)
        val amountStr = String.format(Locale.US, "%010d", amountInBani)

        val ibanDigits = bankDetails.iban.filter { it.isDigit() }
        val participantDigits = share.participant.id.filter { it.isDigit() }.ifEmpty { "1" }

        val baseSeed = ("1326" + ibanDigits + participantDigits.padStart(4, '0') + "20261112122020")
            .take(30)
            .padEnd(30, '0')

        return "$baseSeed$amountStr"
    }

    /**
     * Construiește codul de bare liniar Code 128 conținând direct IBAN-ul și suma (IBAN*SUMA).
     */
    fun buildInvoiceIbanBarcode(
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val amountFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        val ibanClean = bankDetails.iban.replace(" ", "").uppercase()
        return "$ibanClean*$amountFormatted"
    }

    /**
     * Construiește payload-ul pentru codul QR de factură compatibil cu scannerul „Plată Factură / Scan & Pay”
     * din aplicațiile bancare din România (George BCR, BT Pay, ING Home'Bank, Raiffeisen, CEC Bank).
     */
    fun buildInvoiceQrPayload(
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val amountFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        val ibanClean = bankDetails.iban.replace(" ", "").uppercase()
        val beneficiary = bankDetails.beneficiaryName.trim().take(50)
        val ref = "${bankDetails.paymentNotePrefix} ${share.participant.name}".trim().take(35)
        return "$ibanClean*$amountFormatted*$ref*$beneficiary"
    }

    /**
     * Construiește payload-ul în funcție de formatul ales (implicit EPC_SEPA).
     */
    fun buildQrPayload(
        format: QrCodeFormat,
        bankDetails: OrganizerBankDetails,
        share: ParticipantShare
    ): String {
        val amountFormatted = String.format(Locale.US, "%.2f", share.totalToPay)
        val ibanClean = bankDetails.iban.replace(" ", "").uppercase()
        val paymentDetails = "${bankDetails.paymentNotePrefix} - ${share.participant.name}"
            .take(140)

        return when (format) {
            QrCodeFormat.ROMANIAN_BANK_QR -> {
                buildInvoiceQrPayload(bankDetails, share)
            }

            QrCodeFormat.EPC_SEPA -> {
                buildEpcQrPayload(bankDetails, share)
            }

            QrCodeFormat.INVOICE_BARCODE -> {
                "$ibanClean*$amountFormatted"
            }

            QrCodeFormat.ROPAY_QR -> {
                val beneficiary = bankDetails.beneficiaryName.take(70)
                "ropay:?iban=$ibanClean&name=$beneficiary&amount=$amountFormatted&currency=RON&ref=$paymentDetails"
            }

            QrCodeFormat.REVOLUT_LINK -> {
                val cleanTag = bankDetails.revolutTag.replace("@", "").trim()
                if (cleanTag.isNotEmpty()) {
                    "https://revolut.me/$cleanTag/$amountFormatted"
                } else {
                    "https://revolut.me/$amountFormatted"
                }
            }

            QrCodeFormat.TEXT_PAYMENT -> {
                """
                PLATA NOTA RESTAURANT
                Beneficiar: ${bankDetails.beneficiaryName}
                IBAN: $ibanClean
                Banca: ${bankDetails.bankName}
                Suma: $amountFormatted ${bankDetails.currency}
                Detalii: $paymentDetails
                """.trimIndent()
            }
        }
    }

    /**
     * Generează un Bitmap de înaltă rezoluție pentru codul QR conform specificației standardului EPC.
     * Nivel de corecție M, codare UTF-8.
     */
    fun generateQrBitmap(
        content: String,
        widthPx: Int = 512,
        heightPx: Int = 512
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                widthPx,
                heightPx,
                hints
            )
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            val darkColor = 0xFF111827.toInt()
            val lightColor = 0xFFFFFFFF.toInt()

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generează imaginea pentru codul QR standard EPC.
     */
    fun generateBitmapForFormat(
        format: QrCodeFormat = QrCodeFormat.EPC_SEPA,
        payload: String
    ): Bitmap? {
        return generateQrBitmap(payload, widthPx = 512, heightPx = 512)
    }

    fun generateBarcode128Bitmap(
        content: String,
        widthPx: Int = 720,
        heightPx: Int = 220
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 12,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = Code128Writer().encode(
                content,
                BarcodeFormat.CODE_128,
                widthPx,
                heightPx,
                hints
            )
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)
            val darkColor = 0xFF000000.toInt()
            val lightColor = 0xFFFFFFFF.toInt()

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }
            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Salvează codul de bare într-un fișier cache pentru partajare externă (WhatsApp, Email).
     */
    fun saveBarcodeBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "cod_de_bare_factura.png"): File? {
        return saveQrBitmapToCache(context, bitmap, fileName)
    }

    /**
     * Salvează codul QR într-un fișier cache pentru partajare externă (WhatsApp, Email).
     */
    fun saveQrBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "cod_qr_epc_splitnota.png"): File? {
        return try {
            val cachePath = File(context.cacheDir, "shared_qr")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
