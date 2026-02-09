
package com.wulala.demo01.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DeviceActions(
    isBonded: Boolean,
    onBondRequested: () -> Unit,
    onRemoveBondRequested: () -> Unit,
    onClearCacheRequested: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onBondRequested,
            enabled = !isBonded,
        ) {
            Text("Bind", maxLines = 1)
        }
        Button(
            onClick = onRemoveBondRequested,
            enabled = isBonded,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
        ) {
            Text("Unbind", maxLines = 1)
        }
        Button(
            onClick = onClearCacheRequested,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
        ) {
            Text("Refresh", maxLines = 1)
        }
    }
}