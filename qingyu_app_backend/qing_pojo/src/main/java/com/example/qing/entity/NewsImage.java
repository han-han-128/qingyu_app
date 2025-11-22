package com.example.qing.entity;

import lombok.Data;

/**
 * 帖子图片关联实体类
 * 对应数据库中的news_image表
 */
@Data
public class NewsImage {
    /**
     * 关联ID
     */
    private String id;

    /**
     * 帖子ID，关联news表
     */
    private String newsId;

    /**
     * 图片URL
     */
    private String imageUrl;
}