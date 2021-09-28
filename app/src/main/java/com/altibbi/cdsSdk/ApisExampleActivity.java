package com.altibbi.cdsSdk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import java.io.IOException;

public class ApisExampleActivity extends AppCompatActivity {
    AltibbiCDS altibbiCDS;

    private void logVal(String log) {
        System.out.println("ALTIBBI_SDK => " + log);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apis);
        altibbiCDS = AltibbiCDS.getInstance();
        altibbiCDS.init(this);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs",MODE_PRIVATE);
        altibbiCDS.loginUser(sharedPreferences.getString("TOKEN", ""), sharedPreferences.getString("API_LINK", ""));
    }

    public void btn1click(View view) {

        altibbiCDS.phrList(new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);
            }
        });
    }

    public void btn2click(View view) {

        altibbiCDS.getUserInfo("193", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn3click(View view) throws IOException {
        altibbiCDS.getConsultationsList(new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn4click(View view) throws IOException, JSONException {
        altibbiCDS.UpdatePhrInfo("193","rula","female", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn5click(View view)  {
        altibbiCDS.getConsultationDetails("161", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn7click(View view) {
        altibbiCDS.cancelConsultation("161", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn8click(View view) {
        altibbiCDS.deleteConsultation("161", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });
    }

    public void btn6click(View view) {
        altibbiCDS.createConsultation("193","headache","gsm", new NetResult() {
            @Override
            public void onSuccess(String response) {
                logVal(response);
            }

            @Override
            public void onFailure(String error) {
                logVal(error);

            }
        });

    }

}
