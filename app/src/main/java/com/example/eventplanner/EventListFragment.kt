package com.example.eventplanner

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EventListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_list, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadEventsFromDatabase()
    }

    private fun loadEventsFromDatabase() {
        val rootView = view ?: return
        val eventContainer = rootView.findViewById<LinearLayout>(R.id.eventContainer)
        val emptyMessage = rootView.findViewById<TextView>(R.id.tvEmptyMessage)

        while (eventContainer.childCount > 2) {
            eventContainer.removeViewAt(2)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = EventDatabase.getDatabase(requireContext())
            val events = db.eventDao().getAllEvents()

            if (events.isEmpty()) {
                emptyMessage.visibility = View.VISIBLE
            } else {
                emptyMessage.visibility = View.GONE

                for (event in events) {
                    val textView = TextView(requireContext())
                    textView.text =
                        "${event.title}\nCategory: ${event.category}\nLocation: ${event.location}\nDate: ${event.dateTime}"
                    textView.textSize = 16f
                    textView.setPadding(16, 16, 16, 16)
                    textView.setBackgroundColor(Color.parseColor("#EDE7F6"))

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = 20
                    textView.layoutParams = params

                    textView.setOnClickListener {
                        showActionDialog(event)
                    }

                    eventContainer.addView(textView)
                }
            }
        }
    }

    private fun showActionDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("Event Options")
            .setMessage("What do you want to do with this event?")
            .setPositiveButton("Update") { _, _ ->
                openEditScreen(event)
            }
            .setNeutralButton("Delete") { _, _ ->
                showDeleteDialog(event)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditScreen(event: Event) {
        val intent = Intent(requireContext(), EditEventActivity::class.java)
        intent.putExtra("id", event.id)
        intent.putExtra("title", event.title)
        intent.putExtra("category", event.category)
        intent.putExtra("location", event.location)
        intent.putExtra("dateTime", event.dateTime)
        startActivity(intent)
    }

    private fun showDeleteDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Do you want to delete this event?")
            .setPositiveButton("Yes") { _, _ ->
                deleteEvent(event)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteEvent(event: Event) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = EventDatabase.getDatabase(requireContext())
            db.eventDao().deleteEvent(event)

            Toast.makeText(
                requireContext(),
                "Event deleted successfully",
                Toast.LENGTH_SHORT
            ).show()

            loadEventsFromDatabase()
        }
    }
}
