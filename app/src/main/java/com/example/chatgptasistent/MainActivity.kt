package com.example.chatgptasistent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatgptasistent.ui.theme.ChatgptAsistentTheme
import android.content.Intent
import android.graphics.Color
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.chatgptasistent.databinding.MainactivityBinding
import com.example.chatgptasistent.viewModel.CompletionViewModel
import java.util.*
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val SPEECH_RECOGNITION = 1
    }

    private var tts: TextToSpeech? = null
    private lateinit var mCompletionViewModel: CompletionViewModel
    private lateinit var mBinding: MainactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = MainactivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.btnVoice.setOnClickListener {
            askSpeechInput()
        }
        tts = TextToSpeech(this, this)
        setupViewModel()
    }

    private fun setupViewModel() {
        mCompletionViewModel = ViewModelProvider(this)[CompletionViewModel::class.java]
        mCompletionViewModel.observeCompletionLiveData().observe(this) {
            mBinding.pbWaiting.visibility = View.GONE
            mBinding.ltRobot.playAnimation()
            mBinding.cvChatgpt.visibility = View.VISIBLE
            mBinding.tvResponse.visibility = View.VISIBLE
            speak(it.choices[0].message.content)
        }
    }

    private fun askSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pregunta lo que quieras")
        @Suppress("DEPRECATION")
        startActivityForResult(intent, SPEECH_RECOGNITION)
    }


    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_RECOGNITION && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = result?.get(0)
            mCompletionViewModel.postCompletionLiveData(text.toString())
            mBinding.pbWaiting.visibility = View.VISIBLE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Manejar el caso de que el idioma no esté disponible o no sea compatible
            }
        } else {
            // Manejar el caso de que la inicialización del TextToSpeech haya fallado
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun speak(response: String) {
        val listener = object : UtteranceProgressListener() {
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                val spannableString = SpannableString(response)
                spannableString.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(this@MainActivity, R.color.purple_700)
                    ), start, end, 0
                )
                runOnUiThread {
                    mBinding.tvResponse.text = spannableString
                }
            }

            override fun onStart(p0: String?) {}

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    mBinding.ltRobot.pauseAnimation()
                }
            }

            @Deprecated("This method is deprecated. Use a different approach.")
            override fun onError(utteranceId: String?) {
                runOnUiThread {
                    // Manejar el error en TextToSpeech
                    mBinding.tvWelcome.text = ""
                }
            }
        }
        tts?.setOnUtteranceProgressListener(listener)
        tts?.speak(response, TextToSpeech.QUEUE_FLUSH, null, "id")
    }
}