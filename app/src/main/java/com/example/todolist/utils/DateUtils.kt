package com.example.todolist.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat     = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return Pair(start, cal.timeInMillis)
    }

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun isOverdue(dueDate: Long?): Boolean =
        dueDate != null && dueDate < System.currentTimeMillis()

    fun isToday(timestamp: Long): Boolean {
        val (start, end) = todayRange()
        return timestamp in start until end
    }
}
