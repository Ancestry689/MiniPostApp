package com.example.minipost.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.example.minipost.R;
import com.example.minipost.http.ApiService;
import com.example.minipost.http.RetrofitClient;
import com.example.minipost.post.Comment;
import com.example.minipost.post.CommentAdapter;
import com.example.minipost.post.CommentRequest;
import com.example.minipost.post.ImagePagerAdapter;

import com.example.minipost.post.LikeResponse;
import com.example.minipost.post.Post;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView backButton; // 返回按钮
    private TextView postTitle, postContent, postTime, likeCount, authorName;
    private ImageView authorAvatar;
    private ViewPager imagePager;
    private RecyclerView commentRecyclerView;
    private EditText commentInput;
    private Button sendCommentButton, likeButton;

    private int postId;
    private String userId;
    private int currentPage = 1;
    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 初始化视图
        // 初始化视图

        authorName = findViewById(R.id.authorName);
        authorAvatar = findViewById(R.id.authorAvatar);

        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);
        postTime = findViewById(R.id.postTime);
        likeCount = findViewById(R.id.likeCount);
        imagePager = findViewById(R.id.imagePager);
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendCommentButton = findViewById(R.id.sendCommentButton);

        likeButton = findViewById(R.id.likeButton);
        backButton = findViewById(R.id.backButton); // 返回按钮


        // 获取帖子 ID
        postId = getIntent().getIntExtra("postId", -1);
        userId = getIntent().getStringExtra("userId");
        if (postId == -1) {
            finish();
            return;
        }

        // 加载帖子详情
        loadPostDetails(postId);

        // 初始化评论列表
        commentAdapter = new CommentAdapter(commentList);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(commentAdapter);

        // 加载初始评论
        loadComments(postId, currentPage);



        // 设置滚动监听，加载更多评论
        commentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) { // 判断是否滑动到底部
                    currentPage++;
                    loadComments(postId, currentPage);
                }
            }
        });

        // 点赞按钮点击事件
        likeButton.setOnClickListener(v -> likePost(postId));

        // 发布评论按钮点击事件
        sendCommentButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString();
            if (!content.isEmpty()) {
                addComment(postId, content);
            }
        });

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> finish());
    }

    private void loadPostDetails(int postId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<Post> call = apiService.getPostById(postId);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body();
                    postTitle.setText(post.getTitle());
                    postContent.setText(post.getContent());
                    postTime.setText(post.getCreatedAt());
                    likeCount.setText(String.valueOf(post.getLikeCount()));

                    // 显示作者信息
                    authorName.setText(post.getAuthorName());
                    Glide.with(PostDetailActivity.this)
                            .load(post.getAuthorAvatar())
                            .into(authorAvatar);

                    //加载图片
                    if (post.getImages() != null && !post.getImages().isEmpty()) {
                        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(post.getImages());
                        imagePager.setAdapter(imagePagerAdapter);
                    }

                } else {
                // 打印错误信息
                Log.e("PostDetailActivity", "Request failed with code: " + response.code());
                    try {
                        String errorBody = response.errorBody().string(); // 将 errorBody 转换为字符串
                        Log.e("PostDetailActivity", "Error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void likePost(int postId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<LikeResponse> call = apiService.likePost(postId);
        call.enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> call, Response<LikeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newLikeCount = response.body().getLikeCount();
                    likeCount.setText(String.valueOf(newLikeCount));
                    Toast.makeText(PostDetailActivity.this, "Post liked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PostDetailActivity.this, "Failed to like post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LikeResponse> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments(int postId, int page) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<List<Comment>> call = apiService.getComments(postId, page, 20);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    commentList.addAll(response.body());
                    commentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addComment(int postId, String content) {

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<Comment> call = apiService.addComment(postId, new CommentRequest(Integer.valueOf(userId).intValue(), content));
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentList.add(0, response.body()); // 将新评论添加到列表顶部
                    commentAdapter.notifyDataSetChanged();
                    commentInput.setText(""); // 清空输入框
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



//public class PostDetailActivity extends AppCompatActivity {
//
//    private RecyclerView commentRecyclerView;
//    private CommentAdapter commentAdapter;
//    private List<Comment> commentList = new ArrayList<>();
//    private int page = 1;
//    private int postId;
//
//    // 视图对象
//    private ImageView backButton;
//    private ImageView authorAvatar;
//    private TextView authorName;
//    private ViewPager imagePager;
//    private TextView postTitle;
//    private TextView postContent;
//    private TextView postTime;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_post_detail);
//
//        // 获取帖子 ID
//        postId = getIntent().getIntExtra("postId", 0);
//
//        // 初始化视图
//        ImageView backButton = findViewById(R.id.backButton);
//        ImageView authorAvatar = findViewById(R.id.authorAvatar);
//        TextView authorName = findViewById(R.id.authorName);
//        ViewPager imagePager = findViewById(R.id.imagePager);
//        TextView postTitle = findViewById(R.id.postTitle);
//        TextView postContent = findViewById(R.id.postContent);
//        TextView postTime = findViewById(R.id.postTime);
//        commentRecyclerView = findViewById(R.id.commentRecyclerView);
//        EditText commentInput = findViewById(R.id.commentInput);
//        Button sendCommentButton = findViewById(R.id.sendCommentButton);
//
//        // 加载帖子详情
//        loadPostDetail(postId);
//
//
////        // 设置数据
////        backButton.setOnClickListener(v -> finish());
////        authorName.setText("Author Name");
////        postTitle.setText("Post Title");
////        postContent.setText("Post Content");
////        postTime.setText("2024-12-23 21:00:00");
////
////        // 使用 Glide 加载作者头像
////        Glide.with(this)
////                .load("https://via.placeholder.com/50")
////                .placeholder(R.drawable.placeholder)
////                .error(R.drawable.error_image)
////                .into(authorAvatar);
////
////        // 设置图片列表
////        List<String> imageUrls = Arrays.asList(
////                "https://via.placeholder.com/150",
////                "https://via.placeholder.com/200",
////                "https://via.placeholder.com/250"
////        );
////        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, imageUrls);
////        imagePager.setAdapter(imagePagerAdapter);
////
//        // 初始化评论列表
//        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        commentAdapter = new CommentAdapter(commentList);
//        commentRecyclerView.setAdapter(commentAdapter);
//
//        // 加载评论
//        loadComments(postId, page);
//
//        // 发布评论
//        sendCommentButton.setOnClickListener(v -> {
//            String content = commentInput.getText().toString().trim();
//            if (!content.isEmpty()) {
//                postComment(postId, content);
//                commentInput.setText(""); // 清空输入框
//            }
//        });
//
//        // 滑动到底部时加载更多评论
//        commentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (!recyclerView.canScrollVertically(1)) { // 判断是否滑动到底部
//                    loadComments(postId, ++page);
//                }
//            }
//        });
//    }
//
//
//    private void loadPostDetail(int postId) {
////        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
////        Call<PostDetail> call = apiService.getPostDetail(postId);
////        call.enqueue(new Callback<PostDetail>() {
////            @Override
////            public void onResponse(Call<PostDetail> call, Response<PostDetail> response) {
////                if (response.isSuccessful() && response.body() != null) {
////                    PostDetail postDetail = response.body();
////                    // 设置帖子详情
////                    Glide.with(PostDetailActivity.this).load(postDetail.getAuthorAvatarUrl()).into(authorAvatar);
////                    authorName.setText(postDetail.getAuthorName());
////                    postTitle.setText(postDetail.getTitle());
////                    postContent.setText(postDetail.getContent());
////                    postTime.setText(postDetail.getTimestamp());
////
////                    // 设置图片列表
////                    ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(PostDetailActivity.this, postDetail.getImageUrls());
////                    imagePager.setAdapter(imagePagerAdapter);
////                }
////            }
////
////            @Override
////            public void onFailure(Call<PostDetail> call, Throwable t) {
////                // 处理错误
////            }
////        });
//    }
//
//    private void loadComments(int postId, int page) {
//        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
//        Call<List<Comment>> call = apiService.getComments(postId, page, 20);
//        call.enqueue(new Callback<List<Comment>>() {
//            @Override
//            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    commentList.addAll(response.body());
//                    commentAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Comment>> call, Throwable t) {
//                // 处理错误
//            }
//        });
//    }
//
//    private void postComment(int postId, String content) {
//        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
//        CommentRequest commentRequest = new CommentRequest(content);
//        Call<Comment> call = apiService.postComment(postId, commentRequest);
//        call.enqueue(new Callback<Comment>() {
//            @Override
//            public void onResponse(Call<Comment> call, Response<Comment> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Comment newComment = response.body();
//                    commentList.add(0, newComment); // 添加到列表顶部
//                    commentAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Comment> call, Throwable t) {
//                // 处理错误
//            }
//        });
//    }
//
////    private void loadComments(int page) {
////        // 模拟从后端加载评论数据
////        List<Comment> newComments = fetchCommentsFromServer(page);
////        commentList.addAll(newComments);
////        commentAdapter.notifyDataSetChanged();
////    }
////
////    private List<Comment> fetchCommentsFromServer(int page) {
////        // 模拟从后端获取评论数据
////        List<Comment> comments = new ArrayList<>();
////        for (int i = 0; i < 20; i++) {
////            comments.add(new Comment("Comment " + (page * 20 + i), "User " + (page * 20 + i), "https://via.placeholder.com/50", "2024-12-23 21:00:00"));
////        }
////        return comments;
////    }
//}