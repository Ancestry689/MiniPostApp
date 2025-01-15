package com.example.minipost;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minipost.activity.SharedViewModel;
import com.example.minipost.fragment.HomeFragment;
import com.example.minipost.fragment.PostFragment;
import com.example.minipost.fragment.ProfileFragment;
import com.example.minipost.post.Post;
import com.example.minipost.post.PostAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private SharedViewModel sharedViewModel;

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取登录页面传递的用户信息
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        String email = intent.getStringExtra("email");
        int id = intent.getIntExtra("userId",0);

        // 初始化 SharedViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // 将数据存储到 SharedViewModel
        sharedViewModel.setId(id);
        sharedViewModel.setEmail(email);
        sharedViewModel.setUsername(username);
        sharedViewModel.setPassword(password);

        // 初始化底部导航栏
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        // 默认加载帖子首页 Fragment
        loadFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                // 首页
                loadFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_post) {
                // 发布帖子
                loadFragment(new PostFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                // 个人主页
                ProfileFragment profileFragment = new ProfileFragment();

                loadFragment(profileFragment);
                return true;
            }
            return false;
        });

    }

    // 加载 Fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}