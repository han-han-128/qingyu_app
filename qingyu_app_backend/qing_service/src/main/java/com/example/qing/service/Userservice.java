package com.example.qing.service;

import java.util.UUID;

import com.example.qing.entity.User;

import com.example.qing.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class Userservice {

    @Autowired
    private UserMapper userMapper;

    public boolean findUsernameIsExist(String username) {
        //User user=new User();
        User user=userMapper.findUsernameIsExist(username);

        return user == null ? false : true;
    }
    
    // 根据用户名获取用户对象
    public User getUserByUsername(String username) {
        return userMapper.findUsernameIsExist(username);
    }

    public void saveUser(User user) {
        user.setUid(UUID.randomUUID().toString());
        userMapper.saveUser(user);
    }

    public User queryUserForLogin(String username, String password) {
        return userMapper.queryUserForLogin(username,password);
    }
}