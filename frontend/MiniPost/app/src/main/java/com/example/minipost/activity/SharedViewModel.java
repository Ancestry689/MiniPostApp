package com.example.minipost.activity;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> token = new MutableLiveData<>();
    private final MutableLiveData<Integer> id = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();

    public void setToken(String token) {
        this.token.setValue(token);
    }

    public MutableLiveData<String> getToken() {
        return token;
    }

    public void setId(int id) {
        this.id.setValue(id);
    }

    public MutableLiveData<Integer> getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }
}