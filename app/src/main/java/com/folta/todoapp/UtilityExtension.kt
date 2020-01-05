package com.folta.todoapp

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

fun LocalDate.toStringSlashyyyyMMdd(): String {
    return DateTimeFormatter.ofPattern("yyyy/MM/dd").format(this)
}