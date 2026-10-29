package com.munireport.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.munireport.app.core.designsystem.colors.MuniBlue
import com.munireport.app.core.designsystem.colors.MuniRed
import com.munireport.app.core.designsystem.colors.MuniYellow

/**
 * The blue-red-yellow Muntinlupa city stripe used as a small brand anchor on
 * the sign-in and administrator surfaces.
 */
@Composable
fun MuniCityStripe(
    modifier: Modifier = Modifier,
    height: Int = 4,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp)),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height.dp)
                .background(MuniBlue),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height.dp)
                .background(MuniRed),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height.dp)
                .background(MuniYellow),
        )
    }
}
