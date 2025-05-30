package com.example.firstapp.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.firstapp.SampleData
import com.example.firstapp.compose.Conversation
import com.example.firstapp.ui.theme.FirstAppTheme

@Preview
@Composable
fun PreviewConversation() {
    FirstAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Conversation(
                SampleData.conversationSample, modifier = Modifier.padding(innerPadding)
            )
        }
    }
}