package com.wulala.demo01.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceList(
    devices: List<Peripheral>,
    onItemClick: (Peripheral) -> Unit,
    onBondRequested: (Peripheral) -> Unit,
    onRemoveBondRequested: (Peripheral) -> Unit,
    onClearCacheRequested: (Peripheral) -> Unit,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding,
    ) {
        items(devices) { device ->
            DeviceItem(
                device = device,
                onClick = { onItemClick(device) },
                onBondRequested = { onBondRequested(device) },
                onRemoveBondRequested = { onRemoveBondRequested(device) },
                onClearCacheRequested = { onClearCacheRequested(device) },
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DeviceItem(
    device: Peripheral,
    onClick: () -> Unit,
    onBondRequested: () -> Unit,
    onRemoveBondRequested: () -> Unit,
    onClearCacheRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        val state by device.state.collectAsStateWithLifecycle()
        val animatedColor by animateColorAsState(
            targetValue = when (state) {
                is ConnectionState.Connected -> Color.Green
                is ConnectionState.Connecting -> Color.Gray
                else -> contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
            },
            label = "background color animation"
        )
        ElevatedButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(text = device.name ?: "Unknown device")
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Icon",
                tint = animatedColor
            )
        }
        if (state.isConnected) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    val services by device.services().collectAsStateWithLifecycle()
                    val bondState by device.bondState.collectAsStateWithLifecycle()
                    DeviceServices(services = services)
                    /*
                    Spacer(modifier = Modifier.height(8.dp))
                    DeviceActions(
                        isBonded = bondState == BondState.BONDED,
                        onBondRequested = onBondRequested,
                        onRemoveBondRequested = onRemoveBondRequested,
                        onClearCacheRequested = onClearCacheRequested,
                    )
                    */
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val scope = rememberCoroutineScope()
    DeviceList(
        modifier = Modifier.fillMaxWidth(),
        devices = listOf(
            PreviewPeripheral(
                scope = scope,
                address = "AA:BB:CC:DD:EE:FF",
                name = "Mock device 1",
                state = ConnectionState.Connected
            ),
            PreviewPeripheral(
                scope = scope,
                address = "00:11:22:33:44:55",
                name = "Mock device 2",
                state = ConnectionState.Connecting
            ),
            PreviewPeripheral(
                scope = scope,
                address = "AA:BB:CC:DD:EE:00",
                name = "Mock device 3"
            ),
        ),
        onItemClick = {},
        onBondRequested = {},
        onRemoveBondRequested = {},
        onClearCacheRequested = {},
        contentPadding = PaddingValues(16.dp),
    )
}