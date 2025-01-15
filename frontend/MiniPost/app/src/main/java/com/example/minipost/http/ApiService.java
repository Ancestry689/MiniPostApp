package com.example.minipost.http;

import com.example.minipost.post.Comment;
import com.example.minipost.post.CommentRequest;
import com.example.minipost.post.LikeResponse;
import com.example.minipost.post.Post;
import com.example.minipost.post.PostResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 登录接口
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // 注册接口
    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);


    // 上传头像
    @Multipart
    @POST("upload-avatar")
    Call<ResponseBody> uploadAvatar(
            @Part MultipartBody.Part avatar,
            @Part("userId") RequestBody userId
    );

    // 下载头像
    @GET("avatar/{userId}")
    Call<ResponseBody> downloadAvatar(@Path("userId") String userId);


    // 保存用户信息
    @POST("update-user-info")
    Call<ResponseBody> updateUserInfo(@Body UpdateUserInfoRequest request);

    // 发布帖子
    @Multipart
    @POST("posts")
    Call<PostResponse> createPost(
            @Part("title") RequestBody title,
            @Part("content") RequestBody content,
            @Part("author_id") RequestBody authorId,
            @Part List<MultipartBody.Part> images
    );

    // 获取帖子列表
    @GET("posts")
    Call<List<Post>> getPosts(
            @Query("page") int page, // 当前页码
            @Query("limit") int limit // 每页显示的帖子数量
    );

    // 获取帖子详情
    @GET("posts/{id}")
    Call<Post> getPostById(
            @Path("id") int postId // 帖子 ID
    );

    // 点赞帖子
    @POST("posts/{postId}/like")
    Call<LikeResponse> likePost(
            @Path("postId") int postId//,
//            @Body LikeRequest request
    );

    // 获取评论列表
    @GET("posts/{postId}/comments")
    Call<List<Comment>> getComments(
            @Path("postId") int postId, // 帖子 ID
            @Query("page") int page,    // 当前页码
            @Query("limit") int limit   // 每页显示的评论数量
    );

    // 发布评论
    @POST("posts/{postId}/comments")
    Call<Comment> addComment(
            @Path("postId") int postId, // 帖子 ID
            @Body CommentRequest commentRequest // 评论请求体
    );

//    // 获取用户发布的帖子
//    @GET("user/posts")
//    Call<List<Post>> getUserPosts(@Query("userId") int userId);

}