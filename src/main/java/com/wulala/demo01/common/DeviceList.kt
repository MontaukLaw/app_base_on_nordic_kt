package com.wulala.demo01.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceList(
    devices: List<ScanDevice>,
    onItemClick: (Peripheral) -> Unit,
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
                onClick = { onItemClick(device.peripheral) },
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DeviceItem(
    device: ScanDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        val animatedColor by animateColorAsState(
            targetValue = when (device.isConnectable) {
                true -> Color.Green
                false -> Color.Gray //contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
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
        /*
        if (device.isConnectable) {
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
                }  */
    }
}
