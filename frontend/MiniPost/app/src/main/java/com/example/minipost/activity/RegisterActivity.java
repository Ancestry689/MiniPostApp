package com.example.minipost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minipost.Config;
import com.example.minipost.R;
import com.example.minipost.http.ApiService;
import com.example.minipost.http.RegisterRequest;
import com.example.minipost.http.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, emailEditText;
    private Button registerButton, loginButton;

    // Retrofit 实例
    private Retrofit retrofit;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化视图
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        // 初始化 Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL) // 使用全局变量
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();
            registerUser(username, password, email);
        });

        // 返回登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 注册用户
    private void registerUser(String username, String password, String email) {
        Call<RegisterResponse> call = apiService.register(new RegisterRequest(username, password, email));
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null && registerResponse.getMessage().equals("User registered successfully")) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        // 跳转到登录界面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("RegisterActivity", "Error: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
