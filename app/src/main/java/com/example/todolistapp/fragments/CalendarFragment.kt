package com.example.todolistapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.R
import com.example.todolistapp.database.DatabaseHelper
import com.example.todolistapp.database.TodoAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CalendarFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var todoAdapter: TodoAdapter
    private var userId: Int = -1
    private lateinit var selectedDateText: TextView
    private lateinit var calendarView: CalendarView
    private var selectedDate: Long = Calendar.getInstance().timeInMillis
    private var datesWithTodos: Set<Long> = setOf()

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
        calendarView = view.findViewById(R.id.calendarView)

        setupCalendarView()
        setupRecyclerView()
        updateSelectedDateText()
        loadTodosForDate(selectedDate)
        loadAllTodoDates()
    }

    private fun setupCalendarView() {
        calendarView.setOnDateChangeListener { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis

            updateSelectedDateText()
            loadTodosForDate(selectedDate)
        }
    }

    private fun loadAllTodoDates() {
        // Mendapatkan tanggal awal bulan
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis

        // Mendapatkan tanggal akhir bulan
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        // Mendapatkan semua tanggal dengan todo
        val dates = dbHelper.getDistinctTodoDates(userId, startOfMonth, endOfMonth)
        datesWithTodos = dates.toSet()

        // Perbarui UI Kalender
        updateCalendarUI()
    }

    private fun updateCalendarUI() {
        // Menampilkan toast sebagai fallback jika tidak ada perubahan warna
        if (datesWithTodos.isNotEmpty()) {
            Toast.makeText(
                context,
                "Tanggal dengan tugas berhasil dimuat!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "Tidak ada tanggal dengan tugas untuk bulan ini.",
                Toast.LENGTH_SHORT
            ).show()
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
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

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

        view?.findViewById<TextView>(R.id.emptyStateText)?.visibility =
            if (todos.isEmpty()) View.VISIBLE else View.GONE

        todoAdapter.updateTodos(todos)
    }
}
