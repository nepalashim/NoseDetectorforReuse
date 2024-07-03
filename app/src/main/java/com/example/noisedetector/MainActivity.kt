package com.example.noisedetector

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
import java.util.Locale
import kotlin.math.log10

import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.noisedetector.ui.theme.NoiseDetectorTheme

import android.os.Handler
import android.os.Looper
import android.media.AudioFormat
import android.media.AudioRecord
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private val requestRecordAudioPermission = 200
//    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
//    private var mediaRecorder: MediaRecorder? = null
    private lateinit var noiseLevelTextView: TextView
//    private var permissionToRecordAccepted = false
//    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val sampleRate = 44100
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startRecording()
            } else {
                noiseLevelTextView.text = getString(R.string.permission_denied_message)

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)


        noiseLevelTextView = findViewById(R.id.noiseLevelTextView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, requestRecordAudioPermission)
        } else {
            startRecording()
        }
    }


//    @Deprecated
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
//    {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            requestRecordAudioPermission -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted, initialize AudioRecord
//                    startRecording()
//                } else {
//                    // Permission denied, handle accordingly
//                    // For example, show a message or disable audio functionality
//                }
//            }
//        }
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        permissionToRecordAccepted = requestCode == requestRecordAudioPermission && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
//
//        if (permissionToRecordAccepted) {
//            startRecording()
//        } else {
//            noiseLevelTextView.text = "Permission to record audio was denied"
//        }
//    }


    private fun startRecording() {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRecord?.startRecording()
        isRecording = true

        Thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (readSize > 0) {
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += (buffer[i] * buffer[i]).toDouble()
                    }
                    if (readSize > 0) {
                        val amplitude = sum / readSize
//                        val decibels = 10 * Math.log10(amplitude)
                        val decibels = 10 * log10(amplitude)

                        handler.post {
//                            noiseLevelTextView.text = String.format("Noise Level: %.2f dB", decibels)
                            noiseLevelTextView.text = String.format(Locale.getDefault(), "Noise Level: %.2f dB", decibels)

                        }
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioRecord != null) {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
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


//package com.example.noisedetector
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.media.AudioFormat
//import android.media.AudioRecord
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//class MainActivity : AppCompatActivity() {
//
//    private val requestRecordAudioPermission = 200
//    private lateinit var noiseLevelTextView: TextView
//    private var permissionToRecordAccepted = false
//    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
//
//    private val sampleRate = 44100
//    private var audioRecord: AudioRecord? = null
//    private var isRecording = false
//    private val handler = Handler(Looper.getMainLooper())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        noiseLevelTextView = findViewById(R.id.noiseLevelTextView)
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, permissions, requestRecordAudioPermission)
//        } else {
//            startRecording()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        permissionToRecordAccepted = requestCode == requestRecordAudioPermission && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
//
//        if (permissionToRecordAccepted) {
//            startRecording()
//        } else {
//            noiseLevelTextView.text = "Permission to record audio was denied"
//        }
//    }
//
//    private fun startRecording() {
//        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
//        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
//
//        audioRecord?.startRecording()
//        isRecording = true
//
//        Thread {
//            val buffer = ShortArray(bufferSize)
//            while (isRecording) {
//                val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
//                if (readSize > 0) {
//                    var sum = 0.0
//                    for (i in 0 until readSize) {
//                        sum += (buffer[i] * buffer[i]).toDouble()
//                    }
//                    if (readSize > 0) {
//                        val amplitude = sum / readSize
//                        val decibels = 10 * Math.log10(amplitude)
//
//                        handler.post {
//                            noiseLevelTextView.text = String.format("Noise Level: %.2f dB", decibels)
//                        }
//                    }
//                }
//            }
//        }.start()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (audioRecord != null) {
//            isRecording = false
//            audioRecord?.stop()
//            audioRecord?.release()
//            audioRecord = null
//        }
//    }
//}
//
