package com.example.minipost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minipost.Config;
import com.example.minipost.MainActivity;
import com.example.minipost.R;
import com.example.minipost.http.ApiService;
import com.example.minipost.http.LoginRequest;
import com.example.minipost.http.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;

    // Retrofit 实例
    private Retrofit retrofit;
    private ApiService apiService;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // 初始化 Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL) // 使用全局变量
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginUser(username, password);
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // 登录用户
    private void loginUser(String username, String password) {
        Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null && loginResponse.getMessage().equals("Login successful")) {

                        String token = loginResponse.getToken();
                        int id = loginResponse.getId();
                        String email = loginResponse.getEmail();

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();


                        // 保存 token 并跳转到主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        intent.putExtra("username", username); // 传递用户名
                        intent.putExtra("password", password); // 传递用户名
                        intent.putExtra("email", email); // 传递用户名
                        intent.putExtra("userId", id); // 传递用户 ID

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginActivity", "Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}