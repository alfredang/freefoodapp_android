package com.tertiaryinfotech.freefood.ui.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val zone: ZoneId get() = ZoneId.systemDefault()

private val dateTimeFmt: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
private val longDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
private val mediumDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val timeFmt: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private fun ldt(millis: Long) = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()

fun formatDateTime(millis: Long): String = dateTimeFmt.format(ldt(millis))
fun formatLongDate(millis: Long): String = longDateFmt.format(ldt(millis))
fun formatMediumDateTime(millis: Long): String = dateTimeFmt.format(ldt(millis))
fun formatTime(millis: Long): String = timeFmt.format(ldt(millis))

fun distanceText(meters: Double): String =
    if (meters >= 1_000) String.format(Locale.getDefault(), "%.1f km", meters / 1_000)
    else "${meters.toInt()} m"
