package com.folta.todoapp

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class Utility {
    companion object {
        fun LocalDate.toStringSlash_yyyyMMdd(): String {
            return DateTimeFormatter.ofPattern("yyyy/MM/dd").format(this)
        }
    }
}