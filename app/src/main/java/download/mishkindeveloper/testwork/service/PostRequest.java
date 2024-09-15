package download.mishkindeveloper.testwork.service;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class PostRequest {

    private static final String GET_URL = "http://46.219.50.186:8181/tPay/task3";
    private static final String POST_URL = "http://46.219.50.186:8181/tPay/task3";
    private static final OkHttpClient client = new OkHttpClient();
    private OnRequestCompleteListener listener;

    public void execute() {
        Request getRequest = new Request.Builder()
                .url(GET_URL)
                .build();

        client.newCall(getRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PostRequest", "GET request failed: " + e.getMessage(), e);
                if (listener != null) {
                    listener.onRequestComplete("GET request failed: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("PostRequest", "GET request successful: " + responseData);
                    try {
                        executePostRequest();
                    } catch (JSONException e) {
                        throw new RuntimeException("Error in executePostRequest()",e);
                    }
                } else {
                    Log.e("PostRequest", "GET request failed with code: " + response.code());
                    if (listener != null) {
                        listener.onRequestComplete("GET request failed with code: " + response.code());
                    }
                }
            }
        });
    }

    public void executePostRequest() throws JSONException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", "SkyService");
        jsonObject.put("password", "Sky1234");

        String json = jsonObject.toString();

        RequestBody body = RequestBody.create(json, JSON);
        Request postRequest = new Request.Builder()
                .url(POST_URL)
                .post(body)
                .build();

        client.newCall(postRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PostRequest", "POST request failed: " + e.getMessage(), e);
                if (listener != null) {
                    listener.onRequestComplete("POST request failed: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("PostRequest", "POST request successful: " + responseData);
                    if (listener != null) {
                        listener.onRequestComplete(responseData);
                    }
                } else {
                    Log.e("PostRequest", "POST request failed: " + response.message());
                    if (listener != null) {
                        listener.onRequestComplete("POST request failed: " + response.message());
                    }
                }
            }
        });
    }

    public void setOnRequestCompleteListener(OnRequestCompleteListener listener) {
        this.listener = listener;
    }
}
