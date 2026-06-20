package com.tertiaryinfotech.freefood.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tertiaryinfotech.freefood.model.Coordinate
import com.tertiaryinfotech.freefood.model.Recurrence
import com.tertiaryinfotech.freefood.ui.components.ListingThumbnail
import com.tertiaryinfotech.freefood.ui.components.formatLongDate
import com.tertiaryinfotech.freefood.ui.vm.FoodViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

private fun makeChallenge(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (0 until 4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}

private fun timeMillis(hour: Int, minute: Int): Long =
    LocalDate.now().atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    viewModel: FoodViewModel,
    onClose: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    val nowTime = remember { LocalTime.now() }
    var startHour by rememberSaveable { mutableStateOf(nowTime.hour) }
    var startMinute by rememberSaveable { mutableStateOf(nowTime.minute) }
    var endHour by rememberSaveable { mutableStateOf((nowTime.hour + 1) % 24) }
    var endMinute by rememberSaveable { mutableStateOf(nowTime.minute) }
    var recurrence by rememberSaveable { mutableStateOf(Recurrence.NONE) }

    var locationQuery by rememberSaveable { mutableStateOf("") }
    var selectedLocationName by rememberSaveable { mutableStateOf("") }
    var selectedCoordinate by remember { mutableStateOf<Coordinate?>(null) }
    var selectedCountry by rememberSaveable { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var challenge by rememberSaveable { mutableStateOf(makeChallenge()) }
    var challengeAnswer by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris -> photoUris = uris.take(3) }

    val humanVerified = challengeAnswer.trim().uppercase() == challenge
    val startMinutes = startHour * 60 + startMinute
    val endMinutes = endHour * 60 + endMinute
    val canSave = title.isNotBlank() && details.isNotBlank() &&
        selectedCoordinate != null && endMinutes > startMinutes && humanVerified

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share food") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Cancel") }
                },
                actions = {
                    TextButton(
                        enabled = canSave,
                        onClick = {
                            val coord = selectedCoordinate ?: return@TextButton
                            viewModel.addListing(
                                title = title,
                                details = details,
                                locationName = selectedLocationName,
                                coordinate = coord,
                                country = selectedCountry,
                                date = dateMillis,
                                startTime = timeMillis(startHour, startMinute),
                                endTime = timeMillis(endHour, endMinute),
                                recurrence = recurrence,
                                photoUris = photoUris,
                                onDone = onClose,
                            )
                        },
                    ) { Text("Save") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "  FreeFood helps reduce food waste. Please post only real, available food — don't create fictitious events.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel("Photos")
            OutlinedButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Choose up to 3 photos")
            }
            if (photoUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris) { uri ->
                        Box(Modifier.size(88.dp).clip(RoundedCornerShape(10.dp))) {
                            coil.compose.AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            SectionLabel("Event")
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Food title") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = details, onValueChange = { details = it },
                label = { Text("Event details") }, minLines = 3, maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            FieldButton("Date", formatLongDate(dateMillis)) { showDatePicker = true }
            FieldButton("Start time", String.format("%02d:%02d", startHour, startMinute)) { showStartPicker = true }
            FieldButton("End time", String.format("%02d:%02d", endHour, endMinute)) { showEndPicker = true }

            Text("Repeats", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Recurrence.entries.forEachIndexed { index, r ->
                    SegmentedButton(
                        selected = recurrence == r,
                        onClick = { recurrence = r },
                        shape = SegmentedButtonDefaults.itemShape(index, Recurrence.entries.size),
                    ) { Text(r.label) }
                }
            }
            if (recurrence != Recurrence.NONE) {
                Text(
                    "Recurring giveaways stay listed and roll forward to the next ${if (recurrence == Recurrence.DAILY) "day" else "week"}. Great for bakeries and stalls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel("Location")
            OutlinedTextField(
                value = locationQuery, onValueChange = { locationQuery = it },
                label = { Text("Search location") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = { viewModel.searchLocation(locationQuery) }) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Search Maps")
            }
            searchResults.take(6).forEach { result ->
                TextButton(
                    onClick = {
                        selectedLocationName = result.name
                        selectedCoordinate = Coordinate(result.latitude, result.longitude)
                        selectedCountry = result.country
                        locationQuery = result.name
                        viewModel.clearSearchResults()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(result.name, color = MaterialTheme.colorScheme.onSurface)
                        if (result.address.isNotEmpty()) {
                            Text(result.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (selectedLocationName.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedLocationName, color = MaterialTheme.colorScheme.primary)
                }
            }

            SectionLabel("Quick human check")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    challenge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textDecoration = TextDecoration.LineThrough,
                    letterSpacing = 6.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                IconButton(onClick = { challenge = makeChallenge(); challengeAnswer = "" }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "New code")
                }
            }
            OutlinedTextField(
                value = challengeAnswer, onValueChange = { challengeAnswer = it },
                label = { Text("Type the code above") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initialHour = startHour, initialMinute = startMinute,
            onDismiss = { showStartPicker = false },
            onConfirm = { h, m -> startHour = h; startMinute = m; showStartPicker = false },
        )
    }
    if (showEndPicker) {
        TimePickerDialog(
            initialHour = endHour, initialMinute = endMinute,
            onDismiss = { showEndPicker = false },
            onConfirm = { h, m -> endHour = h; endMinute = m; showEndPicker = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun FieldButton(label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
        Spacer(Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } },
    )
}
