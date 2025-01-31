package com.example.mynavigationapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.IOException

class RecordFragment : Fragment() {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }
    private var recordingCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recordButton = view.findViewById<Button>(R.id.btn_record)
        outputFile = "${requireContext().filesDir.absolutePath}/recorded_audio.3gp"
        Log.d("File Path", outputFile) // Log the file path

        requestRecordAudioPermission()

        recordButton.setOnClickListener {
            if (mediaRecorder == null) {
                startRecording()
                activity?.runOnUiThread {
                    recordButton.text = "Stop Recording"
                }
            } else {
                stopRecording()
                activity?.runOnUiThread {
                    recordButton.text = "Start Recording"
                }
            }
        }
    }

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }


    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MediaRecorder Error", "Error starting recording: ${e.message}")
                stopRecording()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val newFileName = "recorded_audio_$timeStamp.3gp"
                val newOutputFile = "${requireContext().filesDir.absolutePath}/$newFileName"

                val oldFile = File(outputFile)
                val newFile = File(newOutputFile)
                oldFile.renameTo(newFile)

                outputFile = newOutputFile

                recordingCount++

            } catch (e: IllegalStateException) {
                // ... (Handle exceptions as before)
            } catch (e: Exception) {
                // ... (Handle exceptions as before)
            }
        }
        mediaRecorder = null
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }
}