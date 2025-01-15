package com.example.minipost.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.minipost.R;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorAvatar;
        private TextView authorName;
        private TextView commentContent;
        private TextView commentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorAvatar = itemView.findViewById(R.id.commentAuthorAvatar);
            authorName = itemView.findViewById(R.id.commentAuthorName);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentTime = itemView.findViewById(R.id.commentTimestamp);
        }

        public void bind(Comment comment) {

            authorName.setText(comment.getAuthorName());
            commentContent.setText(comment.getContent());
            commentTime.setText(comment.getCreatedAt());

//             使用 Glide 加载作者头像
            Glide.with(itemView.getContext())
                    .load(comment.getAuthorAvatar())
                    .placeholder(R.drawable.placeholder) // 占位图
                    .error(R.drawable.error_image) // 错误图
                    .into(authorAvatar);
        }
    }
}