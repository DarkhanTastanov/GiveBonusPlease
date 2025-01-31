package com.example.mynavigationapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import androidx.recyclerview.widget.LinearLayoutManager

class PlaybackFragment : Fragment() {
    private var mediaPlayer: MediaPlayer? = null
    private val REQUEST_READ_AUDIO_PERMISSION = 201
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AudioAdapter
    private var audioFiles: MutableList<File> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestReadAudioPermission()

        recyclerView = view.findViewById(R.id.audio_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        audioFiles = getAudioFiles().toMutableList()

        adapter = AudioAdapter(audioFiles) { file ->
            playAudio(file)
        }
        recyclerView.adapter = adapter
    }

    private fun requestReadAudioPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                REQUEST_READ_AUDIO_PERMISSION
            )
        }
    }

    private fun getAudioFiles(): List<File> {
        val filesDir = requireContext().filesDir
        return filesDir.listFiles { _, name -> name.endsWith(".3gp") }?.toList() ?: emptyList()
    }

    private fun playAudio(file: File) {
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MediaPlayer Error", "Error playing audio: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    inner class AudioAdapter(private val audioFiles: List<File>, private val onItemClick: (File) -> Unit) :
        RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

        inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val fileNameTextView: TextView = itemView.findViewById(R.id.file_name_text_view)
            val playButton: Button = itemView.findViewById(R.id.play_audio_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
            return AudioViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
            val file = audioFiles[position]
            holder.fileNameTextView.text = file.name
            holder.playButton.setOnClickListener {
                onItemClick(file)
            }
        }

        override fun getItemCount(): Int {
            return audioFiles.size
        }
    }
}