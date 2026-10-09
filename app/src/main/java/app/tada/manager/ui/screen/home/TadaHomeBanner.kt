/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tada.manager.R
import app.tada.manager.ui.screen.shared.rememberAccessibilityEnabled
import kotlinx.coroutines.delay

/**
 * The TADa mascot, animated: it waves by alternating between two frames and bobs
 * gently. Animation is disabled when the system requests reduced motion.
 */
@Composable
fun TadaAnimatedMascot(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val reduceMotion = rememberAccessibilityEnabled()

    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        while (true) {
            delay(450)
            frame = (frame + 1) % 2
        }
    }

    // Gentle vertical bob synced with the wave frames.
    val bobY by animateFloatAsState(
        targetValue = if (reduceMotion) 0f else if (frame == 0) -4f else 3f,
        animationSpec = tween(450),
        label = "mascot_bob"
    )

    Image(
        painter = painterResource(
            if (frame == 0) R.drawable.tada_mascot_wave else R.drawable.tada_mascot_wave2
        ),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .offset(y = bobY.dp)
    )
}

/**
 * TADa home hero banner: animated waving mascot next to the headline and the live
 * number of patchable apps.
 */
@Composable
fun TadaHomeBanner(
    appCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TadaAnimatedMascot(modifier = Modifier.size(80.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.tada_home_banner_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.tada_home_banner_subtitle, appCount),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * "Your apps" section title used below the banner.
 */
@Composable
fun TadaHomeSectionTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.tada_home_section_title),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}
