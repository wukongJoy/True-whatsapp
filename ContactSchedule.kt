package com.example.whatsappscheduler

import java.util.concurrent.TimeUnit

/**
 * Data class representing a schedule for sending a WhatsApp message to a contact.
 *
 * @property phoneNumber The E.164 phone number of the contact (without plus sign).
 * @property displayName The display name of the contact (optional, used for UI).
 * @property language The language in which to send the message.
 * @property frequencyDays How often to send the message, in days (1, 2, 3, or 7).
 * @property startHour The start hour of the delivery window (24‑hour clock).
 * @property endHour The end hour of the delivery window (exclusive). Must be startHour+1.
 * @property messageType The type of message: morning greeting, good night, or miss you.
 */
data class ContactSchedule(
    val phoneNumber: String,
    val displayName: String?,
    val language: Language,
    val frequencyDays: Int,
    val startHour: Int,
    val endHour: Int,
    val messageType: MessageType,
)

/** Enum representing supported languages. */
enum class Language { ENGLISH, ARABIC, FRENCH }

/** Enum representing the types of messages that can be sent. */
enum class MessageType { MORNING, NIGHT, MISS_YOU }

/**
 * Returns the initial delay in milliseconds until the next scheduled message
 * should run. This is calculated based on the current time and the
 * selected delivery window.
 */
fun ContactSchedule.computeInitialDelay(): Long {
    val now = java.util.Calendar.getInstance()
    val start = now.clone() as java.util.Calendar
    start.set(java.util.Calendar.HOUR_OF_DAY, startHour)
    start.set(java.util.Calendar.MINUTE, 0)
    start.set(java.util.Calendar.SECOND, 0)
    start.set(java.util.Calendar.MILLISECOND, 0)
    if (now.after(start)) {
        // If we are past the start window, schedule for tomorrow or next interval.
        start.add(java.util.Calendar.DATE, frequencyDays)
    }
    // Add a random minute offset within the window (0–59) to vary send times.
    val randomMinute = (0..59).random()
    start.add(java.util.Calendar.MINUTE, randomMinute)
    return start.timeInMillis - now.timeInMillis
}