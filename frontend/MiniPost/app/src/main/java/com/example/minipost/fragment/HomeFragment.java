package com.example.minipost.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minipost.R;
import com.example.minipost.activity.PostDetailActivity;
import com.example.minipost.activity.SharedViewModel;
import com.example.minipost.http.ApiService;
import com.example.minipost.http.RetrofitClient;
import com.example.minipost.post.Post;
import com.example.minipost.post.PostAdapter;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private int currentPage = 1;

    private SharedViewModel sharedViewModel;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化 RecyclerView
        postRecyclerView = view.findViewById(R.id.recyclerView);
        postRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 两栏布局

        // 初始化 Adapter
        postAdapter = new PostAdapter(postList, this);
        postRecyclerView.setAdapter(postAdapter);

        // 加载初始数据
        loadPosts(currentPage);

        // 获取 SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Log.d("MainActivity", "ViewModel HashCode: " + sharedViewModel.hashCode())
        ;
        sharedViewModel.getId().observe(getViewLifecycleOwner(), id -> {
            userId = String.valueOf(id);
        });

        postRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) { // 判断是否滑动到底部
                    currentPage++;
                    loadPosts(currentPage);
                }
            }
        });

        return view;
    }

    private void loadPosts(int page) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<List<Post>> call = apiService.getPosts(page, 20);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList.addAll(response.body()); // 将数据添加到 postList
                    postAdapter.notifyDataSetChanged(); // 通知 Adapter 数据已更新
                } else {
                    // 处理空数据情况
                    Toast.makeText(getContext(), "No posts found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                // 处理错误
                Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    // 模拟加载帖子数据
//    private void loadPosts(int page) {
//        // 这里应该从后端获取数据，模拟数据如下
//        List<Post> newPosts = fetchPostsFromServer(page);
//        postList.addAll(newPosts);
//        postAdapter.notifyDataSetChanged();
//    }

//    // 模拟从后端获取数据
//    private List<Post> fetchPostsFromServer(int page) {
//        List<Post> posts = new ArrayList<>();
//        // 模拟 20 个帖子
//        for (int i = 0; i < 2; i++) {
//            posts.add(new Post((page * 20 + i),"Post " + (page * 20 + i),"Postcontent",(page * 20 + i),null,null));
//        }
//        return posts;
//    }

    @Override
    public void onPostClick(Post post) {
        // 启动帖子详情页面
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("postId", post.getId());
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}