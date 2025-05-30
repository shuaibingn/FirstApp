import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.firstapp.Message
import com.example.firstapp.compose.MessageCard
import com.example.firstapp.ui.theme.FirstAppTheme

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    FirstAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MessageCard(
                Message("Android", "Jetpack Compose"), modifier = Modifier.padding(innerPadding)
            )
        }
    }
}