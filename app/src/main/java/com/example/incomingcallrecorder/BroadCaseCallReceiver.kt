package com.example.incomingcallrecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.*

class BroadCaseCallReceiver : PhoneCallReceiver() {

    private val TAG = "IncomingCallService"
    private var isRecordStarted = false
    var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onIncomingCallReceived(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onIncomingCallReceived: ")


    }

    override fun onIncomingCallAnswered(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onIncomingCallAnswered: ")
        startRecord(context, "Incoming", number, start.toString())
    }

    override fun onIncomingCallEnded(
        context: Context,
        number: String?,
        start: Date,
        end: Date
    ) {

        Log.d(TAG, "onIncomingCallEnded: ")
        stopRecord(context)
    }

    override fun onOutgoingCallStarted(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onOutgoingCallStarted: ")
        startRecord(context, "outgoing", number, start.toString())
    }

    override fun onOutgoingCallEnded(
        context: Context,
        number: String?,
        start: Date,
        end: Date
    ) {
        Log.d(TAG, "onOutgoingCallEnded: ")
        stopRecord(context)
    }

    override fun onMissedCall(context: Context, number: String?, start: Date) {
        Log.d(TAG, "onMissedCall: ")

    }


    private fun startRecord(context: Context, seed: String, phoneNumber: String?, time: String) {
        try {


            if (isRecordStarted) {
                try {
                    recorder?.stop()  // stop the recording
                } catch (e: RuntimeException) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Log.d(TAG, "RuntimeException: stop() is called immediately after start()")
                    audioFile?.delete()
                }

                releaseMediaRecorder()
                isRecordStarted = false
            } else {
                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder!!.start()
                    isRecordStarted = true
                    Log.i(TAG, "record start")

                } else {
                    releaseMediaRecorder()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
            releaseMediaRecorder()
        }
    }

    private fun stopRecord(context: Context) {
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

    private fun prepareAudioRecorder(
        context: Context, seed: String, phoneNumber: String?
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

}