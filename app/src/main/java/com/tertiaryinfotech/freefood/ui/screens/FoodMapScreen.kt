package com.tertiaryinfotech.freefood.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tertiaryinfotech.freefood.ui.components.ListingThumbnail
import com.tertiaryinfotech.freefood.ui.components.MapMarker
import com.tertiaryinfotech.freefood.ui.components.OsmMap
import com.tertiaryinfotech.freefood.ui.vm.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodMapScreen(
    viewModel: FoodViewModel,
    onOpenListing: (String) -> Unit,
) {
    val listings by viewModel.visibleListings.collectAsStateWithLifecycle()
    val location by viewModel.currentLocation.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = listings.firstOrNull { it.id == selectedId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Map") }) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            OsmMap(
                markers = listings.map { MapMarker(it.id, it.coordinate.latitude, it.coordinate.longitude, it.title) },
                centerLat = location?.latitude ?: listings.firstOrNull()?.coordinate?.latitude,
                centerLon = location?.longitude ?: listings.firstOrNull()?.coordinate?.longitude,
                zoom = 12.0,
                onMarkerClick = { selectedId = it },
                modifier = Modifier.fillMaxSize(),
            )

            if (selected != null) {
                Card(
                    onClick = { onOpenListing(selected.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ListingThumbnail(selected.photos.firstOrNull(), Modifier.size(58.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selected.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(selected.locationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
