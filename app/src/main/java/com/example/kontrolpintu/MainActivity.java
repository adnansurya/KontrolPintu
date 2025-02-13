package com.example.kontrolpintu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    AppCompatImageView btn_speech;
    TextView tv_inet, tv_pintu;
    private static final int RECOGNIZER_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_speech = (AppCompatImageView) findViewById(R.id.btn_speech);
        tv_inet = (TextView) findViewById(R.id.tv_inet);
        tv_pintu = (TextView) findViewById(R.id.tv_pintu);

        if(cekInternet()){
            tv_inet.setText("Connected");
        }else{
            tv_inet.setText("Disconnect");
            tv_pintu.setText("-");
        }

        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Katakan Sesuatu");
                startActivityForResult(speechIntent,RECOGNIZER_RESULT);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("status_pintu");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(cekInternet()){
                    tv_inet.setText("Connected");
                }
                tv_pintu.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tv_pintu.setText("-");
                Toast.makeText(MainActivity.this, "Gagal terhubung keserver!"+ error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode ==  RECOGNIZER_RESULT && data != null){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = matches.get(0);
            if(result.equals("buka pintu") || result.equals("tutup pintu")) {
                if(result.equals("buka pintu")){
                    result = "Terbuka";
                }else{
                    result = "Tertutup";
                }
                if(cekInternet()){
                    tv_inet.setText("Connected");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("status_pintu");
                    myRef.setValue(result);
                    Toast.makeText(this, "Pintu "+result+"!", Toast.LENGTH_SHORT).show();
                }else{
                    tv_inet.setText("Disconnect");
                    tv_pintu.setText("-");
                    Toast.makeText(this, "Gagal terhubung keserver!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Perintah tidak dikenali!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean cekInternet(){
        ConnectivityManager koneksi = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return koneksi.getActiveNetworkInfo() != null;
    }

}