package com.tertiaryinfotech.freefood.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
)

/**
 * A lightweight osmdroid map. The iOS app uses MapKit; osmdroid renders OpenStreetMap
 * tiles with no API key required.
 */
@Composable
fun OsmMap(
    markers: List<MapMarker>,
    modifier: Modifier = Modifier,
    centerLat: Double? = null,
    centerLon: Double? = null,
    zoom: Double = 13.0,
    onMarkerClick: ((String) -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoom)
            }
        },
        update = { map ->
            map.overlays.clear()
            markers.forEach { m ->
                val marker = Marker(map).apply {
                    position = GeoPoint(m.latitude, m.longitude)
                    title = m.title
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(m.id)
                        true
                    }
                }
                map.overlays.add(marker)
            }
            val lat = centerLat ?: markers.firstOrNull()?.latitude
            val lon = centerLon ?: markers.firstOrNull()?.longitude
            if (lat != null && lon != null) {
                map.controller.setCenter(GeoPoint(lat, lon))
            }
            map.invalidate()
        },
    )

    DisposableEffect(Unit) {
        onDispose { }
    }
}
