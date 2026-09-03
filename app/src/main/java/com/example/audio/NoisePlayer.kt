package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import java.util.Random
import kotlin.concurrent.thread

enum class NoiseType(val label: String) {
    OFF("Off"),
    WHITE("White Noise"),
    PINK("Pink Noise"),
    BROWN("Brown Noise"),
    RAIN("Gentle Rain")
}

class NoisePlayer(private val context: Context) {
    private val lock = Any()
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioTrack: AudioTrack? = null
    private var playThread: Thread? = null
    
    @Volatile
    private var isPlaying = false
    @Volatile
    private var isPausedDueToTransientFocus = false
    @Volatile
    private var isDucked = false

    private var currentType = NoiseType.OFF
    private var currentVolume = 0.5f

    // Audio Focus Request for Android O+
    private var audioFocusRequest: AudioFocusRequest? = null

    // Audio Focus Change Listener
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d("NoisePlayer", "Audio focus gained")
                synchronized(lock) {
                    if (isDucked) {
                        isDucked = false
                        applyVolume(currentVolume)
                    }
                    if (isPausedDueToTransientFocus) {
                        isPausedDueToTransientFocus = false
                        if (currentType != NoiseType.OFF && !isPlaying) {
                            startPlayLoop(currentType)
                        }
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d("NoisePlayer", "Audio focus lost permanently")
                stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d("NoisePlayer", "Audio focus lost transiently")
                synchronized(lock) {
                    if (isPlaying) {
                        isPausedDueToTransientFocus = true
                        stopPlaybackEngineOnly()
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d("NoisePlayer", "Audio focus lost transiently (can duck)")
                synchronized(lock) {
                    isDucked = true
                    applyVolume(currentVolume * 0.25f)
                }
            }
        }
    }

    // Becoming Noisy Receiver (Headphones / Bluetooth unplugged)
    private var isNoisyReceiverRegistered = false
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.d("NoisePlayer", "Audio becoming noisy (headphones disconnected), pausing playback")
                stop()
            }
        }
    }

    fun play(type: NoiseType) {
        synchronized(lock) {
            if (currentType == type && isPlaying) return
            stop()
            if (type == NoiseType.OFF) {
                currentType = type
                return
            }

            if (!requestAudioFocus()) {
                Log.w("NoisePlayer", "Failed to acquire audio focus, aborting noise playback")
                return
            }

            registerNoisyReceiver()
            currentType = type
            isPlaying = true
            isPausedDueToTransientFocus = false

            startPlayLoop(type)
        }
    }

    private fun startPlayLoop(type: NoiseType) {
        isPlaying = true
        playThread = thread(start = true, name = "NoisePlayerThread") {
            runPlayLoop(type)
        }
    }

    fun stop() {
        synchronized(lock) {
            currentType = NoiseType.OFF
            isPausedDueToTransientFocus = false
            isDucked = false
            stopPlaybackEngineOnly()
            abandonAudioFocus()
            unregisterNoisyReceiver()
        }
    }

    private fun stopPlaybackEngineOnly() {
        isPlaying = false
        try {
            playThread?.interrupt()
            playThread?.join(300)
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error joining thread", e)
        }
        playThread = null

        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error releasing AudioTrack", e)
        }
        audioTrack = null
    }

    fun setVolume(volume: Float) {
        synchronized(lock) {
            currentVolume = volume.coerceIn(0f, 1f)
            val effectiveVolume = if (isDucked) currentVolume * 0.25f else currentVolume
            applyVolume(effectiveVolume)
        }
    }

    private fun applyVolume(volume: Float) {
        try {
            audioTrack?.setVolume(volume.coerceIn(0f, 1f))
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error setting volume", e)
        }
    }

    fun getVolume(): Float = currentVolume
    fun getCurrentType(): NoiseType = currentType
    fun isPlaying(): Boolean = isPlaying

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()

                audioFocusRequest = request
                val result = audioManager.requestAudioFocus(request)
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error requesting audio focus", e)
            true
        }
    }

    private fun abandonAudioFocus() {
        if (audioManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error abandoning audio focus", e)
        }
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            try {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                context.applicationContext.registerReceiver(becomingNoisyReceiver, filter)
                isNoisyReceiverRegistered = true
            } catch (e: Exception) {
                Log.e("NoisePlayer", "Error registering noisy receiver", e)
            }
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                context.applicationContext.unregisterReceiver(becomingNoisyReceiver)
            } catch (e: Exception) {
                Log.e("NoisePlayer", "Error unregistering noisy receiver", e)
            }
            isNoisyReceiverRegistered = false
        }
    }

    private fun runPlayLoop(type: NoiseType) {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setChannelMask(channelConfig)
                .setEncoding(audioFormat)
                .setSampleRate(sampleRate)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            val effectiveVolume = if (isDucked) currentVolume * 0.25f else currentVolume
            track.setVolume(effectiveVolume)
            track.play()

            val buffer = ShortArray(bufferSize / 2)
            val random = Random()

            // State variables for pink and brown noise filters
            var b0 = 0f; var b1 = 0f; var b2 = 0f; var b3 = 0f; var b4 = 0f; var b5 = 0f; var b6 = 0f // Pink filter
            var brownAccumulator = 0f // Brown filter

            // Rain state variables for patter drops
            var dropTimer = 0
            var dropFreq = 0f
            var dropAmp = 0f

            while (isPlaying && !Thread.currentThread().isInterrupted) {
                for (i in buffer.indices) {
                    val white = random.nextFloat() * 2f - 1f

                    val sampleValue = when (type) {
                        NoiseType.OFF -> 0f
                        NoiseType.WHITE -> {
                            white * 0.35f // keep comfortable volume
                        }
                        NoiseType.PINK -> {
                            // Voss-McCartney algorithm approximation
                            b0 = 0.99886f * b0 + white * 0.0555179f
                            b1 = 0.99332f * b1 + white * 0.0750759f
                            b2 = 0.96900f * b2 + white * 0.1538520f
                            b3 = 0.86650f * b3 + white * 0.3104856f
                            b4 = 0.55000f * b4 + white * 0.5329522f
                            b5 = -0.7616f * b5 - white * 0.0168980f
                            val pinkResult = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f
                            b6 = white * 0.115926f
                            pinkResult * 0.045f // scale to prevent clipping
                        }
                        NoiseType.BROWN -> {
                            // Integrate and leak
                            brownAccumulator = (brownAccumulator + (0.02f * white)) / 1.02f
                            brownAccumulator * 2.2f
                        }
                        NoiseType.RAIN -> {
                            brownAccumulator = (brownAccumulator + (0.02f * white)) / 1.02f
                            val rainBase = brownAccumulator * 1.3f

                            // Patter drops
                            if (dropTimer <= 0) {
                                if (random.nextFloat() < 0.0018f) {
                                    dropTimer = (random.nextFloat() * 1200 + 200).toInt()
                                    dropFreq = random.nextFloat() * 700f + 500f
                                    dropAmp = random.nextFloat() * 0.22f + 0.04f
                                }
                            }

                            var dropContribution = 0f
                            if (dropTimer > 0) {
                                val t = dropTimer.toFloat() / 44100f
                                dropContribution = Math.sin(2.0 * Math.PI * dropFreq * t).toFloat() * dropAmp * (dropTimer.toFloat() / 1500f)
                                dropTimer--
                            }

                            (rainBase * 0.65f) + (dropContribution * 0.35f)
                        }
                    }

                    val clamped = (sampleValue * 32767f).coerceIn(-32768f, 32767f)
                    buffer[i] = clamped.toInt().toShort()
                }

                if (isPlaying) {
                    track.write(buffer, 0, buffer.size)
                }
            }
        } catch (e: Exception) {
            Log.e("NoisePlayer", "Error in play loop", e)
        }
    }
}
