package com.example.qing.controller;

import com.example.qing.entity.News;
import com.example.qing.entity.NewsImage;
import com.example.qing.entity.User;
import com.example.qing.result.JsonResult;
import com.example.qing.service.NewsService;
import com.example.qing.service.Userservice;
import com.example.qing.utils.AliOssUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 帖子控制器
 * 处理帖子相关的请求
 */
@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private Userservice userservice;

    /**
     * 发布帖子
     * @param title 帖子标题
     * @param content 帖子内容
     * @param authorId 作者ID
     * @param files 帖子图片文件列表
     * @return 发布结果
     */
    @PostMapping("/publish")
    public JsonResult publishNews(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String authorId,
            @RequestParam(required = false) MultipartFile[] files) {

        // 1. 参数校验
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || StringUtils.isBlank(authorId)) {
            return JsonResult.errorMsg("标题、内容和作者ID不能为空");
        }

        try {
            // 2. 根据用户名查询真正的用户ID
            User user = userservice.getUserByUsername(authorId);
            if (user == null) {
                return JsonResult.errorMsg("用户不存在");
            }
            
            // 3. 创建帖子对象
            News news = new News();
            news.setId(UUID.randomUUID().toString());
            news.setTitle(title);
            news.setContent(content);
            news.setAuthorId(user.getUid()); // 使用真正的用户ID (uid) 而不是用户名
            news.setStatus(1); // 设置为已发布状态
            news.setCreateTime(new Date());
            news.setUpdateTime(new Date());
            news.setViewCount(0); // 初始浏览次数为0

            // 3. 处理图片上传
            List<String> imageUrls = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // 生成唯一的文件名
                        String originalFilename = file.getOriginalFilename();
                        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String objectName = "news/" + UUID.randomUUID() + extension;

                        // 上传图片到阿里云OSS
                        String imageUrl = aliOssUtil.upload(file.getBytes(), objectName);
                        imageUrls.add(imageUrl);
                    }
                }
            }

            // 4. 保存帖子和图片信息到数据库
            boolean result = newsService.publishNews(news, imageUrls);

            if (result) {
                return JsonResult.buildData("发布成功");
            } else {
                return JsonResult.errorMsg("发布失败，请稍后重试");
            }

        } catch (IOException e) {
            // 处理文件上传异常
            e.printStackTrace();
            return JsonResult.errorMsg("图片上传失败，请稍后重试");
        } catch (Exception e) {
            // 处理其他异常
            e.printStackTrace();
            return JsonResult.errorMsg("发布失败，请稍后重试");
        }
    }

    /**
     * 获取所有已发布的帖子列表
     * @return 帖子列表（包含图片信息）
     */
    @GetMapping("/list")
    public JsonResult getAllPublishedNews() {
        try {
            // 获取所有已发布的帖子
            List<News> newsList = newsService.getAllPublishedNews();
            if (newsList == null || newsList.isEmpty()) {
                return JsonResult.buildData(new ArrayList<>());
            }

            // 为每个帖子添加图片信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (News news : newsList) {
                // 直接使用News对象作为基础
                Map<String, Object> newsMap = new HashMap<>();
                
                // 将News对象的所有属性添加到map中
                newsMap.put("id", news.getId());
                newsMap.put("title", news.getTitle());
                newsMap.put("content", news.getContent());
                newsMap.put("authorId", news.getAuthorId());
                newsMap.put("status", news.getStatus());
                newsMap.put("createTime", news.getCreateTime());
                newsMap.put("updateTime", news.getUpdateTime());
                newsMap.put("viewCount", news.getViewCount());

                // 获取帖子对应的图片列表
                List<NewsImage> images = newsService.getImagesByNewsId(news.getId());
                newsMap.put("images", images);

                resultList.add(newsMap);
            }

            return JsonResult.buildData(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取帖子列表失败，请稍后重试");
        }
    }
    
    /**
     * 根据ID获取帖子详情
     * @param id 帖子ID
     * @return 帖子详情（包含图片信息）
     */
    @GetMapping("/{id}")
    public JsonResult getNewsDetail(@PathVariable String id) {
        try {
            // 参数验证
            if (StringUtils.isBlank(id)) {
                return JsonResult.errorMsg("帖子ID不能为空");
            }

            // 获取帖子基本信息
            News news = newsService.getNewsById(id);
            if (news == null) {
                return JsonResult.errorMsg("帖子不存在");
            }

            // 获取帖子对应的图片列表
            List<NewsImage> images = newsService.getImagesByNewsId(id);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("news", news);
            result.put("images", images);

            return JsonResult.buildData(result);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取帖子详情失败，请稍后重试");
        }
    }
    
    /**
     * 根据用户ID获取已发布的帖子列表
     * @param userId 用户ID
     * @return 用户发布的帖子列表（包含图片信息）
     */
    @GetMapping("/user/{userId}")
    public JsonResult getNewsByUserId(@PathVariable String userId) {
        try {
            // 参数验证
            if (StringUtils.isBlank(userId)) {
                return JsonResult.errorMsg("用户ID不能为空");
            }

            // 先尝试直接按用户ID查询
            List<News> newsList = newsService.getNewsByAuthorId(userId);
            
            // 如果没有查询到，可能传入的是用户名，尝试查询用户ID后再查询
            if (newsList == null || newsList.isEmpty()) {
                User user = userservice.getUserByUsername(userId);
                if (user != null) {
                    newsList = newsService.getNewsByAuthorId(user.getUid());
                }
            }
            if (newsList == null || newsList.isEmpty()) {
                return JsonResult.buildData(new ArrayList<>());
            }

            // 为每个帖子添加图片信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (News news : newsList) {
                // 直接使用News对象作为基础
                Map<String, Object> newsMap = new HashMap<>();
                
                // 将News对象的所有属性添加到map中
                newsMap.put("id", news.getId());
                newsMap.put("title", news.getTitle());
                newsMap.put("content", news.getContent());
                newsMap.put("authorId", news.getAuthorId());
                newsMap.put("status", news.getStatus());
                newsMap.put("createTime", news.getCreateTime());
                newsMap.put("updateTime", news.getUpdateTime());
                newsMap.put("viewCount", news.getViewCount());

                // 获取帖子对应的图片列表
                List<NewsImage> images = newsService.getImagesByNewsId(news.getId());
                newsMap.put("images", images);

                resultList.add(newsMap);
            }

            return JsonResult.buildData(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取用户发布的帖子列表失败，请稍后重试");
        }
    }
}