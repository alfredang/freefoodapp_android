package com.tertiaryinfotech.freefood.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tertiaryinfotech.freefood.ui.vm.FoodViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FoodViewModel) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val countryName = remember(viewModel.myCountry) {
        if (viewModel.myCountry.isEmpty()) "Unknown"
        else Locale("", viewModel.myCountry).displayCountry.ifEmpty { viewModel.myCountry }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(title = "Where to show food", footer = "Distance filtering uses your current location. Subscribed recurring giveaways are always shown.") {
                RowToggle(
                    title = "Only my country",
                    subtitle = countryName,
                    checked = settings.onlyMyCountry,
                    onCheckedChange = viewModel::setOnlyMyCountry,
                )
                RowToggle(
                    title = "Limit by distance",
                    checked = settings.limitDistance,
                    onCheckedChange = viewModel::setLimitDistance,
                )
                if (settings.limitDistance) {
                    Text("Within ${settings.maxDistanceKm.toInt()} km", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = settings.maxDistanceKm.toFloat(),
                        onValueChange = { viewModel.setMaxDistanceKm(it.toDouble()) },
                        valueRange = 1f..50f,
                        steps = 48,
                    )
                }
            }

            SettingsSection(title = "Subscriptions") {
                if (settings.subscribedIds.isEmpty()) {
                    Text(
                        "No subscriptions yet. Open a recurring giveaway and tap Subscribe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val n = settings.subscribedIds.size
                    Text("$n recurring giveaway${if (n == 1) "" else "s"} subscribed")
                    Button(
                        onClick = viewModel::clearSubscriptions,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) { Text("Clear all subscriptions") }
                }
            }

            SettingsSection(title = "About FreeFood") {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "  FreeFood helps communities reduce food waste by sharing real, available free food. Please don't post fictitious events.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Version")
                    Text(appVersion(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, footer: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
        if (footer != null) {
            Text(footer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowToggle(title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun appVersion(): String {
    val context = LocalContext.current
    return remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (android.os.Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
            "${info.versionName} ($code)"
        }.getOrDefault("1.0 (1)")
    }
}
