package com.example.minipost.fragment;

import static com.example.minipost.Config.BASE_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.example.minipost.R;
import com.example.minipost.activity.LoginActivity;
import com.example.minipost.activity.SharedViewModel;
import com.example.minipost.http.ApiService;
import com.example.minipost.http.RetrofitClient;
import com.example.minipost.http.UpdateUserInfoRequest;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private String username;
    private String password;
    private String email;
    private String userId;

    private ImageView profileAvatar;
    private EditText profileUsername, profilePassword, profileEmail;
    private TextView profileId;
    private Button btnLoadAvatar, btnSave, btnLogout;
    private Uri selectedImageUri;
    private ApiService apiService;

    private SharedViewModel sharedViewModel;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1001;


    // Activity Result API 用于选择图片
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        // 显示选择的图片
                        profileAvatar.setImageURI(selectedImage);
                        // 上传图片到后端
                        uploadAvatar(selectedImage);
                    }
                }
            }
    );


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 ActivityResultLauncher
//        pickImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                result -> {
//                    if (result != null) {
//                        try {
//                            // 将 Uri 转换为 Bitmap
//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), result);
//                            profileAvatar.setImageBitmap(bitmap);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 初始化视图
        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileUsername = view.findViewById(R.id.profile_username);
        profilePassword = view.findViewById(R.id.profile_password);
        profileEmail = view.findViewById(R.id.profile_email);
        profileId = view.findViewById(R.id.profile_id);
        btnLoadAvatar = view.findViewById(R.id.btn_load_avatar);
        btnSave = view.findViewById(R.id.btn_save);
        btnLogout = view.findViewById(R.id.btn_logout);

        // 使用 RetrofitClient 提供的 Retrofit 实例
        apiService = RetrofitClient.getInstance().create(ApiService.class);


        // 设置默认用户信息（从登录后获取）
        setDefaultUserInfo();

        // 载入头像按钮点击事件
        btnLoadAvatar.setOnClickListener(v -> openGallery());

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> saveUserInfo());

        // 退出登录按钮点击事件
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    // 设置默认用户信息
    private void setDefaultUserInfo() {
        // 获取从 MainActivity 传递的用户信息
        // 获取 SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Log.d("MainActivity", "ViewModel HashCode: " + sharedViewModel.hashCode());
        // 观察数据变化

        sharedViewModel.getId().observe(getViewLifecycleOwner(), id -> {
            userId = String.valueOf(id);
            profileId.setText("ID: " + userId);

            loadAvatar(userId);
        });

        sharedViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            profileEmail.setText(email);
        });

        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            profileUsername.setText(username);
        });

        sharedViewModel.getPassword().observe(getViewLifecycleOwner(), password -> {
            profilePassword.setText(password);
        });

    }
    // 打开相册选择图片
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
    // 上传图片到后端
    private void uploadAvatar(Uri imageUri) {
        try {
            // 使用 ContentResolver 打开 InputStream
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(getContext(), "无法读取文件", Toast.LENGTH_SHORT).show();
                return;
            }

            // 将 InputStream 转换为字节数组
            byte[] fileBytes = IOUtils.toByteArray(inputStream); // 使用 Apache Commons IO 工具类
            RequestBody requestFile = RequestBody.create(fileBytes, MediaType.parse("image/*"));

            // 创建 MultipartBody.Part
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile);

            // 创建 userId 的 RequestBody
            RequestBody userIdBody = RequestBody.create(userId, MediaType.parse("text/plain"));

            // 调用 Retrofit 上传接口
            Call<ResponseBody> call = apiService.uploadAvatar(filePart, userIdBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "头像上传成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "头像上传失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "文件读取失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 从后端加载用户头像
    private void loadAvatar(String userId) {
        Call<ResponseBody> call = apiService.downloadAvatar(userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 使用 Glide 加载图片
                    Glide.with(ProfileFragment.this)
                            .load(BASE_URL + "avatar/" + userId)
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                            .skipMemoryCache(true) // 禁用内存缓存
                            .into(profileAvatar);
                } else {
                    Toast.makeText(getContext(), "头像加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 保存用户信息
    private void saveUserInfo() {
        // 获取用户输入的信息
        String username = profileUsername.getText().toString();
        String password = profilePassword.getText().toString();
        String email = profileEmail.getText().toString();

        sharedViewModel.setUsername(username);
        sharedViewModel.setPassword(password);
        sharedViewModel.setEmail(email);

        // 创建请求体
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(userId, username, password, email);

        // 使用 Retrofit 发送请求
        Call<ResponseBody> call = apiService.updateUserInfo(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "保存失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 退出登录
    private void logout() {
        // 清除登录状态（例如清除 SharedPreferences 或 Token）
        // 跳转到登录页面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}