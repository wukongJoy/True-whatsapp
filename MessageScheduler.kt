package com.example.whatsappscheduler

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * MessageScheduler is responsible for scheduling periodic workers
 * that launch WhatsApp with a prepared message according to each
 * contact's configuration. It uses WorkManager to schedule work
 * uniquely for each contact by tag so that existing schedules are
 * replaced when the user updates them.
 */
object MessageScheduler {

    /**
     * Schedule a message for the given contact. This method builds
     * a unique WorkRequest using an initial delay computed from the
     * schedule and a repeat interval equal to the frequencyDays. A
     * tag based on the phone number is used so that existing work
     * requests for the same contact are replaced.
     */
    fun scheduleMessage(context: Context, schedule: ContactSchedule) {
        val data = Data.Builder()
            .putString(SendMessageWorker.KEY_PHONE, schedule.phoneNumber)
            .putInt(SendMessageWorker.KEY_LANGUAGE, schedule.language.ordinal)
            .putInt(SendMessageWorker.KEY_MESSAGE_TYPE, schedule.messageType.ordinal)
            .build()

        val delay = schedule.computeInitialDelay()
        val work = PeriodicWorkRequestBuilder<SendMessageWorker>(
            schedule.frequencyDays.toLong(), TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tagForPhone(schedule.phoneNumber))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            tagForPhone(schedule.phoneNumber),
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }

    /**
     * Cancel any scheduled work for the provided phone number.
     */
    fun cancelSchedule(context: Context, phoneNumber: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tagForPhone(phoneNumber))
    }

    private fun tagForPhone(phone: String) = "schedule_" + phone
}