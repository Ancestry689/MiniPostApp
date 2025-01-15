package com.example.minipost.post;

import java.util.Arrays;
import java.util.List;

public class Post {
    private int postId; // 帖子 ID
    private String title; // 帖子标题
    private String content; // 帖子内容
    private int authorId; // 作者Id
    private String author_name;
    private String author_avatar;
    private String createdAt; // 发布时间
    private List<String> image_url;
    private int like_count;


    public Post(int postId, String title, String content, int authorId, int like_count,String author_name, String author_avatar, String createdAt, String[] image_url) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.author_name = author_name;
        this.author_avatar = author_avatar;
        this.createdAt = createdAt;
        this.like_count = like_count;
        this.image_url = Arrays.asList(image_url);
    }

    // Getter 和 Setter
    public int getId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getImages() {
        return image_url;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return author_name;
    }

    public String getAuthorAvatar() {
        return author_avatar;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getLikeCount() {
        return like_count;
    }

}
