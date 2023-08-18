package com.example.time

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.time.ui.theme.TimeTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimerScreen() {
    val viewModel: TimerViewModel = viewModel()
    val remainingTime = viewModel.remainingTime
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Uri da música selecionada pelo usuário
        uri?.let {
            viewModel.setMusicUri(it)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Set Timer",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TimeInput(
            label = "Hours",
            value = viewModel.selectedHours,
            onValueChange = viewModel::onHoursChange
        )

        TimeInput(
            label = "Minutes",
            value = viewModel.selectedMinutes,
            onValueChange = viewModel::onMinutesChange
        )

        TimeInput(
            label = "Seconds",
            value = viewModel.selectedSeconds,
            onValueChange = viewModel::onSecondsChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.startTimer(context)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Start Timer")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.stopTimer()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Stop Timer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                launcher.launch("audio/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Select Music")
        }

        CountdownTimer(remainingTime)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { newValue ->
            if (newValue.isNotEmpty()) {
                onValueChange(newValue.toInt())
            } else {
                onValueChange(0)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { /* Dismiss keyboard */ }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}




@Composable
fun CountdownTimer(remainingTime: State<Long>) {
    val formattedTime = formatTime(remainingTime.value)

    Text(
        text = "Remaining Time: $formattedTime",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 8.dp)
    )
}


fun formatTime(timeInMillis: Long): String {
    val hours = timeInMillis / 3600
    val minutes = (timeInMillis % 3600) / 60
    val seconds = timeInMillis % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

class TimerViewModel : ViewModel() {
    var selectedHours by mutableStateOf(0)
    var selectedMinutes by mutableStateOf(0)
    var selectedSeconds by mutableStateOf(0)

    private var timerJob: Job? = null
    private val _remainingTime = mutableStateOf(0L)
    val remainingTime: State<Long> = _remainingTime

    private var musicUri: Uri? = null

    fun setMusicUri(uri: Uri) {
        musicUri = uri
    }

    private lateinit var musicPlayer: MusicPlayer // Declare a propriedade aqui
    fun startTimer(context: Context) {
        val validMusicUri =
            musicUri ?: Uri.parse("android.resource://" + context.packageName + "/" + R.raw.fire)
        musicPlayer = MusicPlayer(context, validMusicUri)
        val totalTime = selectedHours * 3600L + selectedMinutes * 60L + selectedSeconds
        _remainingTime.value = totalTime
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                _remainingTime.value -= 1
                delay(1000)
            }
            musicPlayer.startPlayback() // Inicia a reprodução quando o temporizador atingir zero

        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _remainingTime.value = 0
        musicPlayer.stopPlayback()
    }

    fun onHoursChange(hours: Int) {
        selectedHours = hours
    }

    fun onMinutesChange(minutes: Int) {
        selectedMinutes = minutes
    }

    fun onSecondsChange(seconds: Int) {
        selectedSeconds = seconds
    }

}




