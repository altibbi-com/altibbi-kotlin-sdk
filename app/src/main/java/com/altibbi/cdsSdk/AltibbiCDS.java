package com.altibbi.cdsSdk;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AltibbiCDS {

    private static AltibbiCDS altibbiCDS;
    public static AltibbiCDS getInstance() {
        if (altibbiCDS == null) {
            altibbiCDS = new AltibbiCDS();
        }
        return altibbiCDS;

    }

    Context context;
    String authToken;
    String apiServer;

    public void init(Context context) {
        this.context = context;
        altibbiCDS = this;

        SharedPreferences sharedPreferences = this.context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        if (!sharedPreferences.getString("TOKEN", "").isEmpty() && !sharedPreferences.getString("API_LINK", "").isEmpty()) {
            authToken = sharedPreferences.getString("TOKEN", "");
            apiServer = sharedPreferences.getString("API_LINK", "");
        }
    }

    public void loginUser(String authToken, String apiServer) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TOKEN", authToken);
        editor.putString("API_LINK", apiServer);
        editor.apply();
        this.authToken = sharedPreferences.getString("TOKEN", "");
        this.apiServer = sharedPreferences.getString("API_LINK", "");
    }

    public void triggerNetRequest(String api,String method, NetResult netResult) {
        try {
            new Thread(() -> {
                try {
                    URL urlConn = new URL(apiServer + api);
                    HttpURLConnection con = (HttpURLConnection) urlConn.openConnection();
                    con.setRequestMethod(method);
                    con.setRequestProperty("Authorization", "Bearer " + authToken);
                    int responseCode = con.getResponseCode();
                    String responseMessage = con.getResponseMessage();
                    if (responseCode == HttpURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        netResult.onSuccess(response.toString());
                    } else {
                        netResult.onFailure("error" + responseMessage);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();


        } catch (Exception ex) {
            netResult.onFailure(ex.getMessage());
        }
    }

    public void triggerNetRequestWithBody(String api,String method,JSONObject data, NetResult netResult) {
        try {
            new Thread(() -> {
                try {
                    URL urlConn = new URL(apiServer + api);
                    HttpURLConnection con = (HttpURLConnection) urlConn.openConnection();
                    con.setRequestMethod(method);
                    con.setDoOutput(true);
                    con.setRequestProperty("Authorization", "Bearer " + authToken);
                    con.setRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                    osw.write(String.valueOf(data));
                    osw.flush();
                    osw.close();
                    int responseCode = con.getResponseCode();
                    String responseMessage = con.getResponseMessage();
                    if (responseCode == HttpURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        netResult.onSuccess(response.toString());
                    } else {
                        netResult.onFailure("error" + responseMessage);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            netResult.onFailure(ex.getMessage());
        }
    }



    public void getConsultationsList(NetResult netResult) {
        triggerNetRequest("/v1/consultations", "GET", netResult);
    }

    public void phrList(NetResult netResult) {
        triggerNetRequest("/v1/users", "GET", netResult);
    }

    public void getUserInfo(String userID, NetResult netResult) {
        triggerNetRequest("/v1/users/"+userID, "GET", netResult);
    }

    public void getConsultationDetails(String consultationID, NetResult netResult)  {
        triggerNetRequest("/v1/consultations/"+consultationID, "GET", netResult);
    }

    public void getMedia(NetResult netResult)  {
        triggerNetRequest("/v1/media/", "GET", netResult);
    }

    public void UpdatePhrInfo(String userID,String name, String gender, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();

            json.put("id",userID);
            json.put("name",name);
            json.put("gender",gender);
            triggerNetRequestWithBody("/v1/users/" + userID, "PUT", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

    public void cancelConsultation(String consultationId, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();

            json.put("consultation_id",consultationId);
            triggerNetRequestWithBody("/v1/consultations/" + consultationId+"/cancel", "POST", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

    public void deleteConsultation(String consultationId, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();

            json.put("consultation_id",consultationId);
            triggerNetRequestWithBody("/v1/consultations/" + consultationId, "DELETE", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

    public void createConsultation(String userID,String question,String medium, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id",userID);
            json.put("question",question);
            json.put("medium",medium);
            triggerNetRequestWithBody("/v1/consultations", "POST", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

    public void createConsultation(String userID, String question, String medium, ArrayList<String> mediaIds, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id",userID);
            json.put("question",question);
            json.put("medium",medium);
            json.put("media_ids",mediaIds);
            triggerNetRequestWithBody("/v1/consultations", "POST", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

    public void createConsultation(String userID, String question, String medium, ArrayList<String> mediaIds,String parentId, NetResult netResult) {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id",userID);
            json.put("question",question);
            json.put("medium",medium);
            json.put("media_ids",mediaIds);
            json.put("parent_consultation_id",parentId);
            triggerNetRequestWithBody("/v1/consultations", "POST", json, netResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            netResult.onFailure(ex.getMessage());
        }
    }

}
