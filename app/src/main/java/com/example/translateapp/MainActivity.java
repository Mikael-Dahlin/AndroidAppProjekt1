package com.example.translateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Main Activity for the application runs when the app is started.
 */
public class MainActivity extends AppCompatActivity {
    // Global variables.
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText recordedTxt;
    private TextView translatedTxt;
    private Translate translate;
    private DBHelper dBHelper;
    private DataBean dataBean;

    /**
     * onCreate method of main activity, runs when you start the app.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set values to global variables.
        dataBean = new DataBean();
        dBHelper = new DBHelper(this);
        recordedTxt = (EditText) findViewById(R.id.recordedText);
        translatedTxt = (TextView) findViewById(R.id.translatedText);

        // Set onClickListeners to the buttons.
        findViewById(R.id.recordButton).setOnClickListener(this::Record);
        findViewById(R.id.translateButton).setOnClickListener(view -> translate());
        findViewById(R.id.saveToDatabase).setOnClickListener(view -> saveToDB());
        findViewById(R.id.toTTS).setOnClickListener(this::next);
    }

    /**
     * Method for taking you to the next activity.
     */
    private void next(View view) {
        Intent intent = new Intent(this, TextToSpeechActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Method that starts the voice recording that listens for a word/sentence to translate.
     */
    private void Record(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.record_prompt));

        try{
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }catch (ActivityNotFoundException a){
            Toast.makeText(this,getString(R.string.record_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method that runs when an registered activity sends a result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        // Check if the request code is the speech input.
        if(requestCode == REQ_CODE_SPEECH_INPUT){
            if (resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                recordedTxt.setText(result.get(0));
                translate();
            }
        }
    }

    /**
     * Method that runs the Google Translate API.
     */
    private void translate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream inputStream = getResources().openRawResource(R.raw.auth)) {

            //Get credentials:
            final GoogleCredentials authorisation = GoogleCredentials.fromStream(inputStream);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(authorisation).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }

        // Do the translation and set it to the TextView.
        Translation translation = translate.translate(recordedTxt.getText().toString(), Translate.TranslateOption.targetLanguage("sr"),Translate.TranslateOption.model("base"));
        translatedTxt.setText(translation.getTranslatedText());

        // Store the translation in a DataBean.
        dataBean.setOriginal(recordedTxt.getText().toString());
        dataBean.setTranslated(translatedTxt.getText().toString());
    }

    /**
     * Method for saving the current information to the database.
     */
    private void saveToDB(){
        // Saves data to the database.
        dBHelper.addData(dataBean.getOriginal(), dataBean.getTranslated());
    }
}