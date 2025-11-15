package com.example.qing.mapper;

import com.example.qing.entity.User;

public interface UserMapper {

    public User findUsernameIsExist(String username);

    public void saveUser(User user);

    public User queryUserForLogin(String username, String password);



}
