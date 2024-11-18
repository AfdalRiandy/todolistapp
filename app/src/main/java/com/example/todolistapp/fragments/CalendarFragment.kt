package com.example.todolistapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolistapp.R
import android.widget.CalendarView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.database.DatabaseHelper
import com.example.todolistapp.database.TodoAdapter
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class CalendarFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var todoAdapter: TodoAdapter
    private var userId: Int = -1
    private lateinit var selectedDateText: TextView
    private var selectedDate: Long = Calendar.getInstance().timeInMillis

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = requireActivity().getSharedPreferences("TodoApp", 0)
            .getInt("USER_ID", -1)

        dbHelper = DatabaseHelper(requireContext())
        selectedDateText = view.findViewById(R.id.selectedDateText)

        setupCalendarView()
        setupRecyclerView()
        updateSelectedDateText()
        loadTodosForDate(selectedDate)
    }

    private fun setupCalendarView() {
        view?.findViewById<CalendarView>(R.id.calendarView)?.setOnDateChangeListener { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis

            updateSelectedDateText()
            loadTodosForDate(selectedDate)
        }
    }

    private fun updateSelectedDateText() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        selectedDateText.text = dateFormat.format(Date(selectedDate))
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            mutableListOf(),
            onTodoChecked = { todo ->
                dbHelper.updateTodo(todo)
                loadTodosForDate(selectedDate)
            },
            onEditClick = { todo ->
                // Implementasi edit akan ditangani oleh HomeFragment
            },
            onDeleteClick = { todo ->
                dbHelper.deleteTodo(todo.id, userId)
                loadTodosForDate(selectedDate)
            }
        )

        view?.findViewById<RecyclerView>(R.id.calendarTodoList)?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    private fun loadTodosForDate(date: Long) {
        // Mendapatkan awal hari
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Mendapatkan akhir hari
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val todos = dbHelper.getTodosForDateRange(
            userId,
            startCalendar.timeInMillis,
            endCalendar.timeInMillis
        )
        todoAdapter.updateTodos(todos)

        // Update empty state visibility
        view?.findViewById<TextView>(R.id.emptyStateText)?.visibility =
            if (todos.isEmpty()) View.VISIBLE else View.GONE
    }
}