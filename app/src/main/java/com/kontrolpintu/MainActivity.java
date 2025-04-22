package com.kontrolpintu;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewResult;
    private Button btnSpeech;
    private SpeechRecognizer speechRecognizer;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        textViewResult = findViewById(R.id.textViewResult);
        btnSpeech = findViewById(R.id.btnSpeech);

        // Inisialisasi Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("speech_result");

        // Inisialisasi Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(MainActivity.this, "Mulai berbicara...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(MainActivity.this, "Mendengarkan selesai.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                Toast.makeText(MainActivity.this, "Gagal mengenali suara.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    textViewResult.setText(spokenText);

                    // Simpan teks ke Firebase Realtime Database
                    databaseReference.setValue(spokenText)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Data tersimpan!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Gagal menyimpan!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        btnSpeech.setOnClickListener(view -> startListening());
    }



    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID"); // Paksa bahasa Indonesia
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan berbicara...");

        speechRecognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}