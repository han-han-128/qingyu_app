package com.example.qing.controller;

import com.example.qing.entity.News;
import com.example.qing.result.JsonResult;
import com.example.qing.service.NewsService;
import com.example.qing.utils.AliOssUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            // 2. 创建帖子对象
            News news = new News();
            news.setId(UUID.randomUUID().toString());
            news.setTitle(title);
            news.setContent(content);
            news.setAuthorId(authorId);
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
}