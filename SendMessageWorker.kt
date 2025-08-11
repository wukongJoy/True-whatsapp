package com.example.whatsappscheduler

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.Locale

/**
 * Worker responsible for initiating a WhatsApp message to a contact.
 * This worker retrieves parameters about the contact and message
 * configuration, constructs a message with randomized wording, and
 * launches an intent to WhatsApp. The user will need to confirm
 * sending within WhatsApp; automatic sending is not supported by
 * WhatsApp's public API.
 */
class SendMessageWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val phoneNumber = inputData.getString(KEY_PHONE) ?: return@withContext Result.failure()
        val languageOrdinal = inputData.getInt(KEY_LANGUAGE, 0)
        val messageTypeOrdinal = inputData.getInt(KEY_MESSAGE_TYPE, 0)
        val language = Language.values()[languageOrdinal]
        val messageType = MessageType.values()[messageTypeOrdinal]

        val message = generateMessage(language, messageType)
        launchWhatsApp(phoneNumber, message)
        Result.success()
    }

    /**
     * Builds a WhatsApp intent with the supplied phone number and message
     * and launches it. The phone number must be in international format
     * without the '+' prefix. If WhatsApp is not installed, the intent
     * safely fails.
     */
    private fun launchWhatsApp(phoneNumber: String, message: String) {
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
    }

    /**
     * Returns a message with slight variations based on the language
     * and message type. Each call selects a message at random from
     * a set of pre‑defined templates.
     */
    private fun generateMessage(language: Language, messageType: MessageType): String {
        val templates = when (language) {
            Language.ENGLISH -> when (messageType) {
                MessageType.MORNING -> listOf(
                    "Good morning! Hope your day is as bright as your smile.",
                    "Morning! Wishing you a wonderful day ahead.",
                    "Rise and shine! Sending positive vibes your way."
                )
                MessageType.NIGHT -> listOf(
                    "Good night! Sweet dreams and rest well.",
                    "Sleep tight! May tomorrow be a great day for you.",
                    "Night! Hope you drift off to peaceful dreams."
                )
                MessageType.MISS_YOU -> listOf(
                    "Hey there! Just thinking of you and sending hugs.",
                    "Hello! I miss you, hope to see you soon.",
                    "Missing you! Hope you’re doing well."
                )
            }
            Language.ARABIC -> when (messageType) {
                MessageType.MORNING -> listOf(
                    "صباح الخير! أتمنى لك يومًا جميلًا.",
                    "صباح النور! أرسل لك أجمل الأمنيات ليومك.",
                    "صباح الورد! أتمنى أن يكون يومك مليئًا بالسعادة."
                )
                MessageType.NIGHT -> listOf(
                    "تصبح على خير! أحلام سعيدة.",
                    "ليلة سعيدة! أتمنى لك نومًا هانئًا.",
                    "تصبح على خير! أتمنى لك راحة وطمأنينة."
                )
                MessageType.MISS_YOU -> listOf(
                    "أفتقدك! أتمنى أن أراك قريبًا.",
                    "أهلاً! أشتاق إليك كثيرًا.",
                    "أفكر فيك، أتمنى أن تكون بخير." 
                )
            }
            Language.FRENCH -> when (messageType) {
                MessageType.MORNING -> listOf(
                    "Bonjour! Passe une merveilleuse journée.",
                    "Salut! Je te souhaite un bon matin.",
                    "Coucou! Que ta journée soit belle et lumineuse."
                )
                MessageType.NIGHT -> listOf(
                    "Bonne nuit! Fais de beaux rêves.",
                    "Dors bien! À demain.",
                    "Bonne nuit! Que ton sommeil soit doux." 
                )
                MessageType.MISS_YOU -> listOf(
                    "Tu me manques! J'espère te voir bientôt.",
                    "Salut! Je pense à toi et tu me manques.",
                    "Je t'envoie des pensées, tu me manques beaucoup." 
                )
            }
        }
        return templates.random()
    }

    companion object {
        const val KEY_PHONE = "KEY_PHONE"
        const val KEY_LANGUAGE = "KEY_LANGUAGE"
        const val KEY_MESSAGE_TYPE = "KEY_MESSAGE_TYPE"
    }
}