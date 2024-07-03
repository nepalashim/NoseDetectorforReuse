package com.example.noisedetector

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.noisedetector.ui.theme.NoiseDetectorTheme

class MainActivity : ComponentActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var noiseLevelTextView: TextView
    private var permissionToRecordAccepted = false
    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoiseDetectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            noiseLevelTextView = TextView(this@MainActivity)
            ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (permissionToRecordAccepted) {
            startRecording()
        } else {
            finish()
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            try {
                prepare()
                start()
                Thread {
                    while (true) {
                        runOnUiThread {
                            val amplitude = mediaRecorder?.maxAmplitude?.toDouble() ?: 0.0
                            val decibels = 20 * Math.log10(amplitude / 32767.0)
                            noiseLevelTextView.text = String.format("Noise Level: %.2f dB", decibels)
                        }
                        Thread.sleep(1000)
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoiseDetectorTheme {
        Greeting("NoiseMakers")
    }
}
