package com.tertiaryinfotech.freefood.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tertiaryinfotech.freefood.model.FoodListing
import com.tertiaryinfotech.freefood.model.Recurrence
import com.tertiaryinfotech.freefood.ui.components.ListingThumbnail
import com.tertiaryinfotech.freefood.ui.components.MapMarker
import com.tertiaryinfotech.freefood.ui.components.OsmMap
import com.tertiaryinfotech.freefood.ui.components.formatLongDate
import com.tertiaryinfotech.freefood.ui.components.formatMediumDateTime
import com.tertiaryinfotech.freefood.ui.components.formatTime
import com.tertiaryinfotech.freefood.ui.vm.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    viewModel: FoodViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    // Re-read so likes/edits reflect; fall back to a snapshot if it was just deleted.
    val listing = viewModel.listingById(listingId)
    if (listing == null) {
        onBack()
        return
    }

    val hasLiked = settings.hasLiked(listing.id)
    val isSubscribed = settings.isSubscribed(listing.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.delete(listing)
                        onBack()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete listing", tint = MaterialTheme.colorScheme.error)
                    }
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PhotoStrip(listing)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(listing.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (listing.recurrence != Recurrence.NONE) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Repeats ${listing.recurrence.label.lowercase()}") },
                        leadingIcon = { Icon(Icons.Filled.Repeat, contentDescription = null, Modifier.size(16.dp)) },
                    )
                }
                Text(listing.details, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            ActionBar(
                listing = listing,
                hasLiked = hasLiked,
                isSubscribed = isSubscribed,
                onLike = { if (!hasLiked) viewModel.like(listing) },
                onShare = { shareListing(context, listing) },
                onSubscribe = { viewModel.toggleSubscribe(listing.id) },
            )

            InfoGrid(listing)

            OsmMap(
                markers = listOf(MapMarker(listing.id, listing.coordinate.latitude, listing.coordinate.longitude, listing.title)),
                centerLat = listing.coordinate.latitude,
                centerLon = listing.coordinate.longitude,
                zoom = 15.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )

            Button(
                onClick = { openInMaps(context, listing) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Map, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open in Maps")
            }
        }
    }
}

@Composable
private fun PhotoStrip(listing: FoodListing) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (listing.photos.isEmpty()) {
            item { ListingThumbnail(null, Modifier.width(220.dp).height(160.dp)) }
        } else {
            items(listing.photos) { path ->
                ListingThumbnail(path, Modifier.width(220.dp).height(160.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionBar(
    listing: FoodListing,
    hasLiked: Boolean,
    isSubscribed: Boolean,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onSubscribe: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip(
            selected = hasLiked,
            onClick = onLike,
            enabled = !hasLiked,
            label = { Text("${listing.likes + if (hasLiked) 1 else 0}") },
            leadingIcon = {
                Icon(
                    if (hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    Modifier.size(18.dp),
                )
            },
        )
        FilterChip(
            selected = false,
            onClick = onShare,
            label = { Text("Share") },
            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null, Modifier.size(18.dp)) },
        )
        if (listing.recurrence != Recurrence.NONE) {
            FilterChip(
                selected = isSubscribed,
                onClick = onSubscribe,
                label = { Text(if (isSubscribed) "Subscribed" else "Subscribe") },
                leadingIcon = {
                    Icon(
                        if (isSubscribed) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                        contentDescription = null,
                        Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun InfoGrid(listing: FoodListing) {
    androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(Icons.Filled.LocationOn, "Location", listing.locationName)
            InfoRow(Icons.Filled.CalendarMonth, "Date", formatLongDate(listing.date))
            InfoRow(Icons.Filled.Schedule, "Time", "${formatTime(listing.startTime)} - ${formatTime(listing.endTime)}")
            InfoRow(Icons.Filled.Timer, "Deletes", formatMediumDateTime(listing.expiresAt))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(96.dp))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun shareText(listing: FoodListing): String {
    var s = "Free food: ${listing.title} at ${listing.locationName} — "
    s += "${formatMediumDateTime(listing.combinedStartDate())}."
    if (listing.recurrence != Recurrence.NONE) s += " Repeats ${listing.recurrence.label.lowercase()}."
    s += " Shared via FreeFood to reduce food waste."
    return s
}

private fun shareListing(context: android.content.Context, listing: FoodListing) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText(listing))
    }
    context.startActivity(Intent.createChooser(intent, "Share free food"))
}

private fun openInMaps(context: android.content.Context, listing: FoodListing) {
    val lat = listing.coordinate.latitude
    val lon = listing.coordinate.longitude
    val label = Uri.encode(listing.locationName)
    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($label)")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { context.startActivity(intent) }
}
