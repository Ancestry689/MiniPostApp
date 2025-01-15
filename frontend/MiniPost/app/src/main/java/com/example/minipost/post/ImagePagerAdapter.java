package com.example.minipost.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.example.minipost.R;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {
    private List<String> imageUrls;

    public ImagePagerAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // 加载布局
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_image, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        // 使用 Glide 加载图片
        Glide.with(container.getContext())
                .load(imageUrls.get(position))
                .into(imageView);

        // 添加到容器
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}