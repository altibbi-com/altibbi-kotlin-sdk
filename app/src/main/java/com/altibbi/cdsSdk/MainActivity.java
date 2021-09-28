package com.altibbi.cdsSdk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    Button submitButton  ;
    EditText tokenText;
    EditText apiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs",MODE_PRIVATE);
        if (!sharedPreferences.getString("TOKEN", "").isEmpty() && !sharedPreferences.getString("API_LINK", "").isEmpty()) {
            Intent i = new Intent(MainActivity.this, ApisExampleActivity.class);
            startActivity(i);
        }

        submitButton = findViewById(R.id.sdk_button);
        tokenText   = findViewById(R.id.sdk_token_text);
        apiText   = findViewById(R.id.sdk_api_text);
        submitButton.setOnClickListener(
                v -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("TOKEN", tokenText.getText().toString());
                    editor.putString("API_LINK", apiText.getText().toString());
                    editor.apply();
                    System.out.println("ALTIBBI_SDK TOKEN ==> " + sharedPreferences.getString("TOKEN", ""));
                    System.out.println("ALTIBBI_SDK LINK ==> " + sharedPreferences.getString("API_LINK", ""));
                    Intent i = new Intent(MainActivity.this, ApisExampleActivity.class);
                    startActivity(i);
                }
        );

    }
}
