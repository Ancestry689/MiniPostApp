package com.example.minipost.post;

// 发布评论请求
public class CommentRequest {
    private int authorId; // 作者 ID
    private String content; // 评论内容

    public CommentRequest(int authorId, String content) {
        this.authorId = authorId;
        this.content = content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }
}