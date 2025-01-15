package com.example.minipost.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.minipost.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private OnPostClickListener listener;

    public PostAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (postList == null || postList.isEmpty()) {
            return; // 如果 postList 为空，直接返回
        }
        Post post = postList.get(position);
        holder.postTitle.setText(post.getTitle());

        // 使用 Glide 加载第一张图片
        String firstImage = null;

        if (post.getImages() != null && !post.getImages().isEmpty()) {
            firstImage = post.getImages().get(0);
        }

            Glide.with(holder.itemView.getContext())
                    .load(firstImage) // 第一张图片 URL
                    .placeholder(R.drawable.placeholder) // 占位图
                    .error(R.drawable.error_image) // 加载失败时的图片 R.drawable.error_image
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                    .skipMemoryCache(true) // 禁用内存缓存
                    .into(holder.postImage); // 目标 ImageView


        // 绑定点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView postImage;
        private TextView postTitle;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            postTitle = itemView.findViewById(R.id.postTitle);
        }
    }
    // 定义点击事件接口
    public interface OnPostClickListener {
        void onPostClick(Post post);
    }
}