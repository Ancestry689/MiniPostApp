package com.example.minipost.post;

// 评论实体类
public class Comment {
    private int id;
    private String content;
    private int authorId;
    private String authorName;
    private String authorAvatar;
    private String createdAt;

    // 构造函数、Getter 和 Setter 方法
    public Comment(int id, String content, int authorId, String authorName, String authorAvatar, String createdAt) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}