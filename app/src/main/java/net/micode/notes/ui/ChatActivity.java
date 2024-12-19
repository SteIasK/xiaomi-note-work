package net.micode.notes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import net.micode.notes.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

    private EditText userInput;
    private TextView chatResponse;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // 加载布局文件

        // 初始化视图
        userInput = findViewById(R.id.user_input);
        chatResponse = findViewById(R.id.chat_response);
        sendButton = findViewById(R.id.send_button);

        // 设置按钮点击事件
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入
                String input = userInput.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "Please enter something", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 调用 ChatGpt 的 completion 方法
                String response = ChatGpt.completion(input);

                // 解析 API 响应
                //String generatedText = parseApiResponse(response);

                chatResponse.setText(response);
            }
        });
    }
}


    /**
     * 解析 API 响应，提取生成的文本。
     *
     * @param response API 返回的完整响应（JSON 字符串）。
     * @return 生成的文本，如果解析失败则返回 null。
     */
