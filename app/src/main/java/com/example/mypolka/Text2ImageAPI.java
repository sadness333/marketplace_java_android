package com.example.mypolka;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Text2ImageAPI {

    private final String URL;
    private final String API_KEY;
    private final String SECRET_KEY;
    private final OkHttpClient client = new OkHttpClient();

    public Text2ImageAPI(String url, String apiKey, String secretKey) {
        URL = url;
        API_KEY = apiKey;
        SECRET_KEY = secretKey;
    }

    public void generateImage(String prompt, TextView text, ImageView resultImageView) {
        new GenerateImageTask(text, resultImageView).execute(prompt);
    }

    private class GenerateImageTask extends AsyncTask<String, Void, String> {
        private ImageView resultImageView;

        private TextView text;

        GenerateImageTask(TextView textview, ImageView imageView) {
            resultImageView = imageView;
            text = textview;
        }

        @Override
        protected String doInBackground(String... strings) {
            String prompt = strings[0];
            String modelId = getModel();

            if (modelId != null) {
                String uuid = generate(prompt, modelId);
                if (uuid != null) {
                    return checkGeneration(uuid);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                text.setVisibility(View.INVISIBLE);
                byte[] imageBytes = android.util.Base64.decode(result, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                resultImageView.setImageBitmap(bitmap);
            }
        }
    }

    private String getModel() {
        String modelId = null;
        try {
            Request request = new Request.Builder()
                    .url(URL + "key/api/v1/models")
                    .addHeader("X-Key", "Key " + API_KEY)
                    .addHeader("X-Secret", "Secret " + SECRET_KEY)
                    .build();

            Response response = client.newCall(request).execute();
            String responseData = response.body().string();

            JSONArray jsonArray = new JSONArray(responseData);
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.has("id")) {
                    modelId = jsonObject.getString("id");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return modelId;
    }



    private String generate(String prompt, String modelId) {
        try {
            JSONObject generateParamsJson = new JSONObject()
                    .put("type", "GENERATE")
                    .put("numImages", 1)
                    .put("width", 1024)
                    .put("height", 1024)
                    .put("generateParams", new JSONObject().put("query", prompt));

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model_id", modelId)
                    .addFormDataPart("params", null, RequestBody.create(MediaType.parse("application/json"), generateParamsJson.toString()));

            RequestBody requestBody = multipartBuilder.build();

            Request request = new Request.Builder()
                    .url(URL + "key/api/v1/text2image/run")
                    .addHeader("X-Key", "Key " + API_KEY)
                    .addHeader("X-Secret", "Secret " + SECRET_KEY)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            System.err.println("Error occurred. JSON content: " + responseData);
            return jsonObject.getString("uuid");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    private String checkGeneration(String requestId) {
        String result = null;
        try {
            int attempts = 10;
            int delay = 10;
            while (attempts > 0) {
                Request request = new Request.Builder()
                        .url(URL + "key/api/v1/text2image/status/" + requestId)
                        .addHeader("X-Key", "Key " + API_KEY)
                        .addHeader("X-Secret", "Secret " + SECRET_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                String status = jsonObject.getString("status");
                if (status.equals("DONE")) {
                    result = jsonObject.getJSONArray("images").toString();
                    break;
                }
                attempts--;
                Thread.sleep(delay * 1000);
            }
        } catch (IOException | JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}