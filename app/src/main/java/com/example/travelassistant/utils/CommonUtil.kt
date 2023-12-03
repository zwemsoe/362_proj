package com.example.travelassistant.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommonUtil {
    fun formatDate(date: Long, pattern: String = "dd/MM/yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(date))
    }

    fun extractDisplayName(username: String): String {
        return username.split(' ')[0]
    }
}