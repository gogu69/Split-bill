package com.example

import com.example.model.OrganizerBankDetails
import com.example.model.Participant
import com.example.model.ParticipantShare
import com.example.model.ReceiptItem
import com.example.service.QrCodeFormat
import com.example.service.QrCodeService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SplitCalculationTest {

    @Test
    fun testMultiQuantityWaterAllocation() {
        // Testăm cerința: 4 sticle de apă împărțite între mai multe persoane
        val waterItem = ReceiptItem(
            name = "Apă plată 0.75L",
            quantity = 4.0,
            unitPrice = 12.0,
            totalPrice = 48.0,
            allocations = mapOf(
                "p1" to 2.0, // 2 sticle
                "p2" to 1.0, // 1 sticlă
                "p3" to 1.0  // 1 sticlă
            )
        )

        assertEquals(4.0, waterItem.allocatedQuantity, 0.001)
        assertEquals(0.0, waterItem.remainingQuantity, 0.001)
        assertTrue(waterItem.isFullyAllocated)

        // Verificăm calculul costului pentru fiecare
        val costP1 = waterItem.allocations["p1"]!! * waterItem.unitPrice
        val costP2 = waterItem.allocations["p2"]!! * waterItem.unitPrice
        val costP3 = waterItem.allocations["p3"]!! * waterItem.unitPrice

        assertEquals(24.0, costP1, 0.001)
        assertEquals(12.0, costP2, 0.001)
        assertEquals(12.0, costP3, 0.001)
        assertEquals(48.0, costP1 + costP2 + costP3, 0.001)
    }

    @Test
    fun testEpcQrPayloadGeneration() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678",
            currency = "RON",
            paymentNotePrefix = "Nota restaurant"
        )
        val participant = Participant(name = "Familia Popescu")
        val share = ParticipantShare(
            participant = participant,
            items = emptyList(),
            subtotal = 150.0,
            tipShare = 15.0,
            totalToPay = 165.0,
            isPaid = false
        )

        val payload = QrCodeService.buildQrPayload(QrCodeFormat.EPC_SEPA, bankDetails, share)
        assertTrue(payload.contains("BCD"))
        assertTrue(payload.contains("SCT"))
        assertTrue(payload.contains("Ion Popa"))
        assertTrue(payload.contains("RO49INGB0000999912345678"))
        assertTrue(payload.contains("EUR165.00"))

        val qrBitmap = QrCodeService.generateQrBitmap(payload, 256, 256)
        assertNotNull(qrBitmap)
        assertEquals(256, qrBitmap!!.width)
        assertEquals(256, qrBitmap.height)
    }

    @Test
    fun testInvoiceBarcode128Generation() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 100.0,
            tipShare = 10.0,
            totalToPay = 110.0,
            isPaid = false
        )

        val payload = QrCodeService.buildQrPayload(QrCodeFormat.INVOICE_BARCODE, bankDetails, share)
        assertEquals("RO49INGB0000999912345678*110.00", payload)

        val barcodeBitmap = QrCodeService.generateBarcode128Bitmap(payload, 400, 150)
        assertNotNull(barcodeBitmap)
        assertEquals(400, barcodeBitmap!!.width)
        assertEquals(150, barcodeBitmap.height)
    }

    @Test
    fun testInvoiceNumericBarcode40Digits() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 70.0,
            tipShare = 6.69,
            totalToPay = 76.69,
            isPaid = false
        )

        val numericBarcode = QrCodeService.buildInvoiceNumericBarcode(bankDetails, share)
        assertEquals(40, numericBarcode.length)
        assertTrue(numericBarcode.endsWith("0000007669"))
        assertTrue(numericBarcode.all { it.isDigit() })

        val bmp = QrCodeService.generateBarcode128Bitmap(numericBarcode, 720, 220)
        assertNotNull(bmp)
        assertEquals(720, bmp!!.width)
        assertEquals(220, bmp.height)

        val ibanBarcode = QrCodeService.buildInvoiceIbanBarcode(bankDetails, share)
        assertEquals("RO49INGB0000999912345678*76.69", ibanBarcode)
    }

    @Test
    fun testRomanianBankQrPayloadGeneration() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678",
            paymentNotePrefix = "Nota restaurant"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 100.0,
            tipShare = 10.0,
            totalToPay = 110.0,
            isPaid = false
        )

        val payload = QrCodeService.buildQrPayload(QrCodeFormat.ROMANIAN_BANK_QR, bankDetails, share)
        assertTrue(payload.startsWith("RO49INGB0000999912345678*110.00*"))
        assertTrue(payload.contains("Ion Popa"))

        val qrBitmap = QrCodeService.generateQrBitmap(payload, 256, 256)
        assertNotNull(qrBitmap)
        assertEquals(256, qrBitmap!!.width)
    }

    @Test
    fun testRoPayPayloadGeneration() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678",
            paymentNotePrefix = "Nota restaurant"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 100.0,
            tipShare = 10.0,
            totalToPay = 110.0,
            isPaid = false
        )

        val payload = QrCodeService.buildQrPayload(QrCodeFormat.ROPAY_QR, bankDetails, share)
        assertTrue(payload.startsWith("ropay:?iban=RO49INGB0000999912345678"))
        assertTrue(payload.contains("amount=110.00"))
        assertTrue(payload.contains("currency=RON"))

        val qrBmp = QrCodeService.generateQrBitmap(payload, 256, 256)
        assertNotNull(qrBmp)
        assertEquals(256, qrBmp!!.width)
    }

    @Test
    fun testRevolutPayloadGeneration() {
        val bankDetails = OrganizerBankDetails(
            revolutTag = "ionpopa"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 50.0,
            tipShare = 5.0,
            totalToPay = 55.0,
            isPaid = false
        )

        val revolutUrl = QrCodeService.buildQrPayload(QrCodeFormat.REVOLUT_LINK, bankDetails, share)
        assertEquals("https://revolut.me/ionpopa/55.00", revolutUrl)
    }

    @Test
    fun testOfflineTranslationToRomanian() {
        val translatedWater = com.example.service.OfflineTranslator.translateToRomanian("acqua naturale")
        assertTrue(translatedWater.contains("Apă plată"))

        val translatedBeer = com.example.service.OfflineTranslator.translateToRomanian("birra")
        assertTrue(translatedBeer.contains("Bere"))

        val translatedSalad = com.example.service.OfflineTranslator.translateToRomanian("greek salad")
        assertTrue(translatedSalad.contains("Salată grecească"))

        val translatedCoperto = com.example.service.OfflineTranslator.translateToRomanian("coperto")
        assertTrue(translatedCoperto.contains("Taxă de masă"))
    }

    @Test
    fun testBankDetailsStoragePersistence() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val storage = com.example.service.BankDetailsStorage(context)
        val testDetails = OrganizerBankDetails(
            beneficiaryName = "Mihai Ionescu",
            iban = "RO20BTRL0000123456789012",
            phone = "+40722334455",
            bankName = "Banca Transilvania",
            paymentNotePrefix = "Masa de pranz"
        )
        storage.saveBankDetails(testDetails)

        val loaded = storage.loadBankDetails()
        assertEquals("Mihai Ionescu", loaded.beneficiaryName)
        assertEquals("RO20BTRL0000123456789012", loaded.iban)
        assertEquals("+40722334455", loaded.phone)
        assertEquals("Banca Transilvania", loaded.bankName)
        assertEquals("Masa de pranz", loaded.paymentNotePrefix)
    }

    @Test
    fun testRoPayAliasMessageFormatting() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678",
            phone = "+40721234567",
            bankName = "ING Bank",
            paymentNotePrefix = "Nota restaurant"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 100.0,
            tipShare = 10.0,
            totalToPay = 110.0,
            isPaid = false
        )
        val msg = com.example.service.SharingService.formatRoPayShortMessage("Taverna", bankDetails, share)
        assertTrue(msg.contains("+40721234567"))
        assertTrue(msg.contains("110.00 RON"))
        assertTrue(msg.contains("Ion Popa"))
        assertTrue(msg.contains("RoPay"))
    }

    @Test
    fun testParticipantMessageCleanWithoutBarcodes() {
        val bankDetails = OrganizerBankDetails(
            beneficiaryName = "Ion Popa",
            iban = "RO49INGB0000999912345678",
            phone = "+40721234567",
            bankName = "ING Bank",
            paymentNotePrefix = "Nota restaurant"
        )
        val share = ParticipantShare(
            participant = Participant(name = "Andrei"),
            items = emptyList(),
            subtotal = 100.0,
            tipShare = 10.0,
            totalToPay = 110.0,
            isPaid = false
        )
        val msg = com.example.service.SharingService.formatParticipantMessage("Taverna", bankDetails, share)
        assertTrue(msg.contains("+40721234567"))
        assertTrue(msg.contains("RO49INGB0000999912345678"))
        assertTrue(msg.contains("Ion Popa"))
        assertTrue(msg.contains("110.00 RON"))
        assertTrue(!msg.contains("Cod de bare"))
        assertTrue(!msg.contains("Scanează"))
    }
}
