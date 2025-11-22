package com.example.qing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 帖子实体类
 * 对应数据库中的news表
 */
@Data
public class News {
    /**
     * 帖子ID
     */
    private String id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 作者ID，关联user表
     */
    private String authorId;

    /**
     * 帖子状态（0：未发布，1：已发布，2：已删除等）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 浏览次数
     */
    private Integer viewCount;
}