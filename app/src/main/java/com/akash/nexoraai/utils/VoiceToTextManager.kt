package com.akash.nexoraai.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceToTextManager(private val context: Context) : RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextState())
    val state = _state.asStateFlow()

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

    fun startListening() {
        _state.value = VoiceToTextState(isListening = true)

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = _state.value.copy(error = "Recognition not available")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
    }

    fun stopListening() {
        _state.value = _state.value.copy(isListening = false)
        recognizer.stopListening()
    }

    fun resetSpokenText() {
        _state.value = _state.value.copy(spokenText = "")
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.value = _state.value.copy(error = null)
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        _state.value = _state.value.copy(isListening = false)
    }

    override fun onError(error: Int) {
        _state.value = _state.value.copy(
            error = "Error code: $error",
            isListening = false
        )
    }

    override fun onResults(results: Bundle?) {
        val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = data?.get(0) ?: ""
        if (text.isNotBlank()) {
            _state.value = _state.value.copy(
                spokenText = text,
                isListening = false
            )
        } else {
            _state.value = _state.value.copy(isListening = false)
        }
    }

    override fun onPartialResults(results: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}

data class VoiceToTextState(
    val spokenText: String = "",
    val isListening: Boolean = false,
    val error: String? = null
)
