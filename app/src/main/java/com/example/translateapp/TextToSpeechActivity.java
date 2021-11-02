package com.example.translateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.speech.tts.TextToSpeech;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Locale;

/**
 * Activity that handles the Text to speech part.
 */
public class TextToSpeechActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    // Global variables.
    private TextToSpeech textToSpeech;
    private DBHelper dBHelper;

    /**
     * onCreate method for this activity. gets the database and sets onClickListeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        dBHelper = new DBHelper(this);
        findViewById(R.id.speakButton).setOnClickListener(view -> speakOut());
        findViewById(R.id.toMain).setOnClickListener(this::back);
    }

    /**
     * onStart Method for the activity, enables text to speech.
     */
    @Override
    protected void onStart() {
        textToSpeech = new TextToSpeech(this, this);
        super.onStart();
    }

    /**
     * onStop Method for the activity, disables text to speech.
     */
    @Override
    protected void onStop() {
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onStop();
    }

    /**
     * Method that takes you back to the main activity.
     */
    private void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Method that runs when you initialize text to speech.
     */
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "This Language is not supported");
            }
        }
        else{
            Log.e("TTS", "Initialization Failed");
        }
    }

    /**
     * Method that gets a random row from the database and then uses text to speech on it.
     */
    private void speakOut(){
        // Get the text from a random row.
        DataBean databaseRow = dBHelper.getRow(dBHelper.randomRowID());
        String original = databaseRow.getOriginal();
        String translation = databaseRow.getTranslated();

        // Add text to text to speech. Changes language between original and translation.
        textToSpeech.setLanguage(Locale.getDefault());
        textToSpeech.speak(original, TextToSpeech.QUEUE_FLUSH, null, "Original");
        textToSpeech.playSilentUtterance(750, TextToSpeech.QUEUE_ADD, "pause");
        textToSpeech.setLanguage(Locale.forLanguageTag("sr"));
        textToSpeech.speak(translation, TextToSpeech.QUEUE_ADD, null, "translation");
    }
}