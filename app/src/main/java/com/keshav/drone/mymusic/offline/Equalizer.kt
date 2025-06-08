package com.keshav.drone.mymusic.offline

import android.media.audiofx.Equalizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate

@Composable
fun EqualizerUI(MV: MusicViewModel) {
    val isEnabled by MV.isEqualizerEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Equalizer",
            color = Color.Green)
        Box(

            modifier = Modifier
                .background(Color.Transparent)
                .padding(10.dp)
                .rotate(-90f),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                Text("Equalizer", color = Color.Green)

                Spacer(modifier = Modifier.height(12.dp))

               /*Band 0: Low/bass frequencies (~60 Hz)

                Band 1: Low-mid (~230 Hz)

                Band 2: Mid (~910 Hz)

                Band 3: High-mid (~3.6 kHz)

                Band 4: Treble (~14 kHz)
                */

                EqualizerSlider("Low/bass (~60 Hz)", MV.bassLevel) {
                    MV.bassLevel = it
                    MV.setBandLevel(0, it.toShort())
                }

                EqualizerSlider("Low-mid (~230 Hz)", MV.lowmidLevel) {
                    MV.lowmidLevel = it
                    MV.setBandLevel(1, it.toShort())
                }

                EqualizerSlider("Mid (~910 Hz)", MV.midLevel) {
                    MV.midLevel = it
                    MV.setBandLevel(2, it.toShort())
                }

                EqualizerSlider("High-mid (~3.6 kHz)", MV.highmidLevel) {
                    MV.highmidLevel = it
                    MV.setBandLevel(3, it.toShort())
                }

                EqualizerSlider(" Treble (~14 kHz)", MV.trebleLevel) {
                    MV.trebleLevel = it
                    MV.setBandLevel(4, it.toShort()) // assuming 5-band EQ
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        Button(onClick = { MV.toggleEqualizer() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            )) {
            Text(if (isEnabled) "Off" else "On")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val min = -1500
    val max = 1500

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
       // horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$label: $value", color = Color.White)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            //steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Green,
                inactiveTrackColor = Color.Red
            ),
            thumb = {
                // Custom slim vertical line as thumb
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.Yellow, RoundedCornerShape(1.dp))
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(Color.Transparent)
        )
    }
}
