package com.example.minipost.post;

// 发布评论响应
public class CommentResponse {
    private String message;
    private Comment comment;

    public String getMessage() {
        return message;
    }

    public Comment getComment() {
        return comment;
    }
}