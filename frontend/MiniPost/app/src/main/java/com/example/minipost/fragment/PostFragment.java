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
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.minipost.R;
import com.example.minipost.activity.SharedViewModel;
import com.example.minipost.http.ApiService;
import com.example.minipost.post.PostResponse;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostFragment extends Fragment {

    private EditText titleEditText, contentEditText;
    private Button selectImagesButton, postButton;
    private LinearLayout imageContainer;
    private List<Uri> selectedImages = new ArrayList<>();

    private SharedViewModel sharedViewModel;
    private String userId;

    // 使用 ActivityResultLauncher 替代 onActivityResult
    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // 多选图片
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedImages.add(imageUri);
                            addImageToContainer(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // 单选图片
                        Uri imageUri = data.getData();
                        selectedImages.add(imageUri);
                        addImageToContainer(imageUri);
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        // 初始化视图
        titleEditText = view.findViewById(R.id.title);
        contentEditText = view.findViewById(R.id.content);
        selectImagesButton = view.findViewById(R.id.select_images);
        postButton = view.findViewById(R.id.post_button);
        imageContainer = view.findViewById(R.id.image_container);

        // 选择图片按钮点击事件
        selectImagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(intent); // 启动图片选择
        });

        // 发布按钮点击事件
        postButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "请填写标题和正文", Toast.LENGTH_SHORT).show();
            } else {
                uploadPost(title, content, selectedImages);
            }
        });

        // 获取 SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Log.d("MainActivity", "ViewModel HashCode: " + sharedViewModel.hashCode())
        ;
        sharedViewModel.getId().observe(getViewLifecycleOwner(), id -> {
            userId = String.valueOf(id);
        });

        return view;
    }

    // 将选择的图片添加到预览区域
    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        params.setMargins(8, 0, 8, 0);
        imageView.setLayoutParams(params);
        Glide.with(this).load(imageUri).into(imageView);
        imageContainer.addView(imageView);
    }

    // 上传帖子到后端
    private void uploadPost(String title, String content, List<Uri> images) {
        // 创建 Retrofit 实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // 后端地址
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 创建 MultipartBody.Part 列表
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri imageUri : images) {
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
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("images", "image.jpg", requestFile);
                imageParts.add(filePart);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "文件读取失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 创建标题和正文的 RequestBody
        RequestBody titleBody = RequestBody.create(title,MediaType.parse("text/plain"));
        RequestBody contentBody = RequestBody.create(content,MediaType.parse("text/plain"));
        RequestBody idBody = RequestBody.create(userId,MediaType.parse("text/plain"));

        // 发送请求
        Call<PostResponse> call = apiService.createPost(titleBody, contentBody, idBody,imageParts);
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "发布失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}