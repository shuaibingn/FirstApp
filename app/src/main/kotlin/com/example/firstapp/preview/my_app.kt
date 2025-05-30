package com.example.firstapp.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var clickContinue by remember { mutableStateOf(false) }

    if (clickContinue) {
        LocationMapScreen()
    } else {
        OnBoardingScreen(modifier = modifier) {
            clickContinue = true
        }
    }
}