package com.example.incomingcallrecorder

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    var isRecordStarted = false
    var recorder: MediaRecorder? = null
    var audioFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val checkPermission: CheckPermission = CheckPermission()
        checkPermission.checkPermission(this)

        IncomingServcie.startService(this)

        start.setOnClickListener {
            prepareAudioRecorder(seed = "testing", phoneNumber = "9940402323")
        }

        stop.setOnClickListener {
            stopRecord(this)
        }


    }

    private fun prepareAudioRecorder(
        context: Context = this, seed: String, phoneNumber: String?
    ): Boolean {
        try {

            Log.d(TAG, "prepareAudioRecorder: ")

            var fileName = "In"
            val dirPath = context.filesDir
            val dirName = "IncomingCall"
            val outputFormat = MediaRecorder.OutputFormat.THREE_GPP
            val audioSource = MediaRecorder.AudioSource.MIC
            val audioEncoder = MediaRecorder.AudioEncoder.AMR_NB

            val sampleDir = File("$dirPath/$dirName")

            if (!sampleDir.exists()) {
                sampleDir.mkdirs()
            }

            val fileNameBuilder = StringBuilder()
            fileNameBuilder.append(fileName)
            fileNameBuilder.append("_")

            fileNameBuilder.append(seed)
            fileNameBuilder.append("_")

            fileNameBuilder.append(phoneNumber)
            fileNameBuilder.append("_")


            fileName = fileNameBuilder.toString()

            val suffix = ".3gp"


            audioFile = File.createTempFile(fileName, suffix, sampleDir)

            recorder = MediaRecorder()
            recorder?.apply {
                setAudioSource(audioSource)
                setOutputFormat(outputFormat)
                setAudioEncoder(audioEncoder)
                setOutputFile(audioFile!!.absolutePath)
                setOnErrorListener { _, _, _ -> }
            }

            try {
                recorder?.prepare()

            } catch (e: IllegalStateException) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            }

            recorder!!.start()
            isRecordStarted = true
            Log.i(TAG, "record start")

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun releaseMediaRecorder() {
        recorder?.apply {
            reset()
            release()
        }
        recorder = null
    }


    private fun stopRecord(context: Context = this) {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder()
                isRecordStarted = false
                Log.i(TAG, "record stop")
            }
        } catch (e: Exception) {
            releaseMediaRecorder()
            e.printStackTrace()
        }
    }

}