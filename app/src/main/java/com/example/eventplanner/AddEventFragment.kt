package com.example.eventplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEventFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etDateTime = view.findViewById<EditText>(R.id.etDateTime)
        val btnSaveEvent = view.findViewById<Button>(R.id.btnSaveEvent)

        etDateTime.setOnClickListener {
            showDateTimePicker(etDateTime)
        }

        btnSaveEvent.setOnClickListener {
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
                Toast.makeText(
                    requireContext(),
                    "Please select date and time",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val event = Event(
                title = title,
                category = category,
                location = location,
                dateTime = dateTime
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val db = EventDatabase.getDatabase(requireContext())
                db.eventDao().insertEvent(event)

                Toast.makeText(
                    requireContext(),
                    "Event saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                etTitle.text.clear()
                etCategory.text.clear()
                etLocation.text.clear()
                etDateTime.text.clear()

                requireActivity()
                    .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                        R.id.bottomNavigationView
                    ).selectedItemId = R.id.eventListFragment
            }
        }
    }

    private fun showDateTimePicker(etDateTime: EditText) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    requireContext(),
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
