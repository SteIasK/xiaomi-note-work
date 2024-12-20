package net.micode.notes.ui;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import com.google.gson.Gson;

public final class ChatGpt {
    //千帆平台用v1获取两个key
    public static final String apiKey = "YOUR_APIKEY";

    public static final String apiSecret = "YOUR_SECRETKEY";

    private static class GptRequest {
        public static class RequestBody {
            public String model;
            public List<Map<String, String>> messages;

            public RequestBody(String model, List<Map<String, String>> messages) {
                this.model = model;
                this.messages = messages;
            }
        }

        private final String model = "ERNIE Speed-AppBuilder";
        private String content;
        private String prompt;

        public GptRequest(String content, String prompt) {
            this.content = content;
            this.prompt = prompt;
        }

        public String toString() {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(new TreeMap<String, String>() {
                {
                    put("role", "user");
                    put("content", prompt + content);
                }
            });
            RequestBody body = new RequestBody(model, messages);
            return new Gson().toJson(body);
        }
    }

    public static String getAccessToken(String apiKey, String apiSecret) {
        String accessToken = null;
        try {
            // 构建 URL
            //https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=Lllk0jevxjvQSGRcVIErifDr&client_secret=9X9b8tSSMS5bpBni5AywhvZDlj2Kljz9
            String urlString = "https://aip.baidubce.com/oauth/2.0/token" +
                    "?grant_type=client_credentials" +
                    "&client_id=" + apiKey +
                    "&client_secret=" + apiSecret;
            URL url = new URL(urlString);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 获取响应
            int responseCode = connection.getResponseCode();
            Log.i("TokenFetcher", "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析 JSON 响应
                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.getString("access_token");
            } else {
                Log.e("TokenFetcher", "Failed to fetch access_token. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            Log.e("TokenFetcher", "Error: " + e.getMessage());
        }
        return accessToken;
    }
    /**
     * 使用指定的便签内容和提示词进行任务。
     *
     * @param content 便签内容的字符串（请调用者限制长度在 1000 字符以内）。
     * @param prompt  生成结果。
     * @return 生成的文本。
     */
    public static String task(String content, String prompt) {
        final String[] retval = {""};
        Log.d("GPT", "task: Start with: " + retval[0]);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取access_token
                    //String accessToken = getAccessToken(apiKey,apiSecret);
                    GptRequest gptRequest = new GptRequest(content, prompt);
                    Log.d("send", String.valueOf(gptRequest));
                    //免费api
                    //https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/image2text/fuyu_8b
                    //https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat
                    //https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ai_apaas
                    URL url = new URL("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ai_apaas?access_token=24.b308228f31f03a98a7dd0fa5749a7491.2592000.1737254200.282335-116761603");
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);

                    // 发送请求
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    /*wr.writeBytes(gptRequest.toString());
                    Log.d("trans", gptRequest.toString());*/
                    //这里必须强制utf-8,不然发送中文会乱码
                    byte[] requestBodyBytes = gptRequest.toString().getBytes("UTF-8");
                    wr.write(requestBodyBytes);
                    wr.flush();
                    wr.close();

                    // 获取响应
                    int responseCode = connection.getResponseCode();
                    Log.i("GPT", "task: Response Code : " + responseCode);

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String result = jsonResponse.getString("result");
                        // 过滤内容
                        retval[0] = result;

                        // 打印完整响应内容
                        Log.i("GPT", "API Response: " + retval[0]);
                    } else {
                        retval[0] = "Error: API request failed with response code " + responseCode;
                    }
                } catch (Exception e) {
                    retval[0] = "Error: " + e.getMessage();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("GPT", "finish with: " + retval[0]);
        return retval[0];
    }

    /**
     * 使用指定的便签内容进行补全。
     *
     * @param content 便签内容的字符串（请调用者限制长度在 1000 字符以内）。
     * @return 生成的文本。
     */
    public static String completion(String content) {
        return task(content, ""/*"Continue writing without repeating existing content."*/);
    }
}