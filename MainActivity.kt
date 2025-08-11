package com.example.whatsappscheduler

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.whatsappscheduler.databinding.ActivityMainBinding

/**
 * MainActivity provides a simple interface to schedule WhatsApp
 * messages. The user can enter a phone number manually, select the
 * language, frequency, time window and message type. When saved,
 * the schedule is passed to [MessageScheduler] to create periodic
 * work requests. For brevity this example stores no persistent
 * database and only schedules one contact at a time.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Register permission launcher for reading contacts (unused if manual entry).
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up spinners with options from resources.
        ArrayAdapter.createFromResource(
            this,
            R.array.language_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.frequency_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.frequencySpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.time_range_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.timeSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.message_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.messageTypeSpinner.adapter = adapter
        }

        // Optionally request contact permission when the activity starts.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        // Save button click listener.
        binding.saveButton.setOnClickListener {
            val phone = binding.contactInput.text.toString().replace("+", "").replace(" ", "")
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val language = when (binding.languageSpinner.selectedItemPosition) {
                0 -> Language.ENGLISH
                1 -> Language.ARABIC
                else -> Language.FRENCH
            }
            val frequencyDays = when (binding.frequencySpinner.selectedItemPosition) {
                0 -> 1
                1 -> 2
                2 -> 3
                else -> 7
            }
            // Parse time range. Format: "HH:MM - HH:MM".
            val range = binding.timeSpinner.selectedItem as String
            val parts = range.split(" - ")
            val startHour = parts[0].split(":")[0].toInt()
            val endHour = parts[1].split(":")[0].toInt()
            val messageType = when (binding.messageTypeSpinner.selectedItemPosition) {
                0 -> MessageType.MORNING
                1 -> MessageType.NIGHT
                else -> MessageType.MISS_YOU
            }

            val schedule = ContactSchedule(
                phoneNumber = phone,
                displayName = null,
                language = language,
                frequencyDays = frequencyDays,
                startHour = startHour,
                endHour = endHour,
                messageType = messageType
            )
            MessageScheduler.scheduleMessage(this, schedule)
            Toast.makeText(this, "Scheduled!", Toast.LENGTH_LONG).show()
        }
    }
}