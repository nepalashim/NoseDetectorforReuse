package com.example.noisedetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.height
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
import androidx.core.content.ContextCompat
import com.example.noisedetector.ui.theme.NoiseDetectorTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import java.util.Locale




class MainActivity : ComponentActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val REQUEST_SEND_SMS_PERMISSION = 201
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var noiseLevelTextView: TextView
    private var permissionToRecordAccepted = false
    private var permissionToSendSmsAccepted = true

    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS)

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
            ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_SEND_SMS_PERMISSION)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecation")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionToRecordAccepted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.contains(PackageManager.PERMISSION_GRANTED)
        permissionToSendSmsAccepted = requestCode == REQUEST_SEND_SMS_PERMISSION && grantResults.contains(PackageManager.PERMISSION_GRANTED)

        if (permissionToRecordAccepted && permissionToSendSmsAccepted) {
            startRecording()
        } else {
            setContent {
                NoiseDetectorTheme {
                    PermissionDeniedUI(onOpenSettings = { openAppSettings() })
                }
            }
        }
    }

    @Suppress("DEPRECATION")
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
                            noiseLevelTextView.text = String.format(Locale.getDefault(), "Noise Level: %.2f dB", decibels)

                            if (permissionToSendSmsAccepted && decibels > 80) {
                                sendSmsAlert()
                            }
                        }
                        Thread.sleep(1000)
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun sendSmsAlert() {
        val phoneNumber = "+9779844653192" // Your Nepali phone number
        val message = "The environment is getting too noisy"

        // Check if SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Use SmsManager directly
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_LONG).show()
            }
        } else {
            finish()
//            showSmsPermissionDeniedMessage()

        }
    }

//    private fun sendSmsAlert() {
//        val phoneNumber = "+9779844653192" // Your Nepali phone number
//        val message = "The environment is getting too noisy"
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
//            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
//        } else {
//            showSmsPermissionDeniedMessage()
//        }
//    }

//    private fun showSmsPermissionDeniedMessage() {
//        // Show a message to the user
//        Toast.makeText(this, "SMS permission not granted. Please enable it in settings.", Toast.LENGTH_LONG).show()
//        // Open app settings
//        openAppSettings()
//    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
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

    @Composable
    fun PermissionDeniedUI(onOpenSettings: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("SMS permission not granted. Please enable it in settings.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoiseDetectorTheme {
        Greeting("NoiseMakers")
    }
}
