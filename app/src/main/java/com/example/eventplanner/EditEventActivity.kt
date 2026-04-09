package com.example.eventplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class EditEventActivity : AppCompatActivity() {

    private var eventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etCategory = findViewById<EditText>(R.id.etEditCategory)
        val etLocation = findViewById<EditText>(R.id.etEditLocation)
        val etDateTime = findViewById<EditText>(R.id.etEditDateTime)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateEvent)

        eventId = intent.getIntExtra("id", -1)
        etTitle.setText(intent.getStringExtra("title"))
        etCategory.setText(intent.getStringExtra("category"))
        etLocation.setText(intent.getStringExtra("location"))
        etDateTime.setText(intent.getStringExtra("dateTime"))

        etDateTime.setOnClickListener {
            showDateTimePicker(etDateTime)
        }

        btnUpdate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val dateTime = etDateTime.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                etTitle.requestFocus()
                return@setOnClickListener
            }

            if (dateTime.isEmpty()) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedEvent = Event(
                id = eventId,
                title = title,
                category = category,
                location = location,
                dateTime = dateTime
            )

            lifecycleScope.launch {
                val db = EventDatabase.getDatabase(this@EditEventActivity)
                db.eventDao().updateEvent(updatedEvent)

                Toast.makeText(
                    this@EditEventActivity,
                    "Event updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    private fun showDateTimePicker(etDateTime: EditText) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    this,
                    { _, selectedHour, selectedMinute ->
                        val formattedDateTime =
                            "$selectedDay/${selectedMonth + 1}/$selectedYear " +
                                String.format("%02d:%02d", selectedHour, selectedMinute)

                        etDateTime.setText(formattedDateTime)
                    },
                    currentHour,
                    currentMinute,
                    true
                ).show()
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
}
