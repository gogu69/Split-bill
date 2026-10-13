package com.example.service

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.ReceiptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ScanResult(
    val restaurantName: String,
    val items: List<ReceiptItem>,
    val detectedTotal: Double?
)

object GeminiReceiptScanner {

    private const val TAG = "GeminiReceiptScanner"

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Comprimare și scalare optimizată pentru OCR rapid:
     * 960px este rezoluția optimă pentru recunoaștere caractere pe bon fiscal,
     * reducând dimensiunea payload-ului de la ~1MB la sub 90KB.
     */
    private fun Bitmap.toBase64(maxDimension: Int = 960): String {
        val width = this.width
        val height = this.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
        } else {
            this
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun buildRequestBody(base64Image: String, includeThinkingConfig: Boolean): String {
        val prompt = """
            Ești un asistent rapid pentru digitalizarea bonurilor fiscale de restaurant.
            Analizează această poză a notei de plată.
            
            REGULĂ MANDATORIE DE TRADUCERE:
            Tradu AUTOMAT în limba ROMÂNĂ denumirea fiecărui produs (ex: 'Acqua' -> 'Apă plată/minerală', 'Caffè' -> 'Cafea espresso', 'Coperto' -> 'Taxă masă (Coperto)').
            
            Extrage:
            1. Numele restaurantului.
            2. Fiecare produs:
               - name: denumire tradusă în română
               - quantity: număr zecimal (ex: dacă scrie 4 sticle, quantity e 4.0)
               - unitPrice: preț unitar
               - totalPrice: preț total
            3. Totalul general al notei.
            
            Răspunde STRICT în format JSON valid, fără markdown sau text adițional:
            {
              "restaurantName": "Nume restaurant sau Notă Restaurant",
              "items": [
                {
                  "name": "Nume produs în română",
                  "quantity": 1.0,
                  "unitPrice": 25.0,
                  "totalPrice": 25.0
                }
              ],
              "total": 150.0
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            val inlineData = JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            }
                            put("inlineData", inlineData)
                        })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
                if (includeThinkingConfig) {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingBudget", 0)
                    })
                }
            }
            put("generationConfig", generationConfig)
        }

        return requestJson.toString()
    }

    suspend fun scanReceipt(bitmap: Bitmap): Result<ScanResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Cheia GEMINI_API_KEY nu este configurată în Secrets. Puteți încărca o notă demonstrativă sau introduce produsele manual.")
            )
        }

        try {
            val base64Image = bitmap.toBase64()

            // Modele rapide candidate în ordinea priorității
            // gemini-3.5-flash (implicit stabil), gemini-flash-latest (alias flash), gemini-3.1-flash-lite-preview, gemini-3.8-flash
            val candidateModels = listOf(
                "gemini-3.5-flash",
                "gemini-flash-latest",
                "gemini-3.1-flash-lite-preview",
                "gemini-3.8-flash"
            )

            var lastHttpCode: Int? = null
            var had503 = false
            var hadTimeout = false
            var lastErrorMessage: String? = null

            for (model in candidateModels) {
                Log.d(TAG, "Încercare analiză notă cu modelul: $model")

                // Încercăm până la 2 iterații pe model în caz de erori tranzitorii (503, 429, timeout)
                for (attempt in 1..2) {
                    try {
                        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                        // La a doua încercare, omitem thinkingConfig pentru compatibilitate maximă
                        val payloadString = buildRequestBody(base64Image, includeThinkingConfig = (attempt == 1))
                        val requestBody = payloadString.toRequestBody("application/json".toMediaType())

                        val request = Request.Builder()
                            .url(requestUrl)
                            .post(requestBody)
                            .build()

                        val response = client.newCall(request).execute()
                        val responseCode = response.code

                        if (response.isSuccessful) {
                            val responseBody = response.body?.string().orEmpty()
                            val parsedResult = parseGeminiResponse(responseBody)
                            Log.d(TAG, "Analiză reușită cu $model (produse: ${parsedResult.items.size})")
                            return@withContext Result.success(parsedResult)
                        } else {
                            lastHttpCode = responseCode
                            val errorBody = response.body?.string().orEmpty()
                            Log.w(TAG, "Eroare $responseCode de la $model la încercarea $attempt: $errorBody")

                            if (responseCode == 503) {
                                had503 = true
                                // Server temporar supraîncărcat: așteptăm 1.5s înainte de următoarea încercare
                                if (attempt < 2) {
                                    delay(1500)
                                    continue
                                }
                            } else if (responseCode == 429) {
                                // Rate limit: așteptăm 2s
                                if (attempt < 2) {
                                    delay(2000)
                                    continue
                                }
                            } else if (responseCode == 404) {
                                // Model negăsit, trecem direct la următorul model
                                break
                            } else {
                                // Alt cod de eroare HTTP (ex. 400, 500)
                                lastErrorMessage = extractErrorMessage(errorBody, responseCode)
                                if (attempt < 2 && (responseCode in 500..599)) {
                                    delay(1000)
                                    continue
                                }
                            }
                        }
                    } catch (timeoutEx: SocketTimeoutException) {
                        hadTimeout = true
                        Log.w(TAG, "Timeout cu $model la încercarea $attempt")
                        if (attempt < 2) {
                            delay(1000)
                            continue
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepție la apelul cu $model: ${e.message}")
                        lastErrorMessage = e.message
                    }
                }
            }

            // Dacă s-a ajuns aici, toate modelele candidate au eșuat. Oferim un mesaj clar în limba română:
            val userFriendlyMessage = when {
                had503 -> "Serverul Gemini AI este temporar supraîncărcat (Eroare 503: Serviciu indisponibil). Am încercat modele alternative, dar serverul este ocupat. Apăsați «Reîncearcă» peste câteva momente sau folosiți adăugarea manuală / nota demonstrativă."
                lastHttpCode == 429 -> "Limita temporară de cereri AI a fost atinsă (Eroare 429). Așteptați câteva secunde și apăsați «Reîncearcă»."
                lastHttpCode in listOf(401, 403) -> "Cheia API Gemini nu are permisiuni valide (Eroare $lastHttpCode). Verificați configurarea în Secrets."
                hadTimeout -> "Timpul de conectare a expirat la analiza notei. Verificați conexiunea la internet și apăsați «Reîncearcă» sau comutați pe modul Offline."
                !lastErrorMessage.isNullOrBlank() -> lastErrorMessage
                else -> "Nu s-a putut analiza nota fiscală (Cod HTTP: ${lastHttpCode ?: "necunoscut"}). Vă rugăm să reîncercați."
            }

            Result.failure(Exception(userFriendlyMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Eroare generală scanReceipt: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String, responseCode: Int): String {
        return try {
            val root = JSONObject(errorBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message")
            if (!message.isNullOrBlank()) {
                "Eroare AI ($responseCode): $message"
            } else {
                "Eroare server AI ($responseCode)"
            }
        } catch (_: Exception) {
            "Eroare server AI ($responseCode)"
        }
    }

    private fun parseGeminiResponse(jsonString: String): ScanResult {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

        // Curățare eventuale markdowns ```json ... ```
        val cleanedText = text
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val parsedJson = JSONObject(cleanedText)
        val restaurantName = parsedJson.optString("restaurantName", "Notă Restaurant")
        val detectedTotal = if (parsedJson.has("total")) parsedJson.optDouble("total") else null

        val itemsArray = parsedJson.optJSONArray("items") ?: JSONArray()
        val itemsList = mutableListOf<ReceiptItem>()

        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            val name = itemObj.optString("name", "Produs $i")
            val quantity = itemObj.optDouble("quantity", 1.0)
            val totalPrice = itemObj.optDouble("totalPrice", 0.0)
            val unitPrice = itemObj.optDouble("unitPrice", if (quantity > 0) totalPrice / quantity else totalPrice)

            itemsList.add(
                ReceiptItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    quantity = if (quantity <= 0) 1.0 else quantity,
                    unitPrice = unitPrice,
                    totalPrice = if (totalPrice > 0) totalPrice else quantity * unitPrice
                )
            )
        }

        return ScanResult(
            restaurantName = restaurantName,
            items = itemsList,
            detectedTotal = detectedTotal
        )
    }
}
