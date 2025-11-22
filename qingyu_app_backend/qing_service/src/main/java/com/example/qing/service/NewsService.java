package com.example.qing.service;

import com.example.qing.entity.News;
import com.example.qing.entity.NewsImage;
import java.util.List;

/**
 * 帖子服务接口
 */
public interface NewsService {
    /**
     * 发布帖子
     * @param news 帖子对象
     * @param imageUrls 图片URL列表
     * @return 是否发布成功
     */
    boolean publishNews(News news, List<String> imageUrls);

    /**
     * 获取所有已发布的帖子
     * @return 帖子列表
     */
    List<News> getAllPublishedNews();

    /**
     * 根据帖子ID获取图片列表
     * @param newsId 帖子ID
     * @return 图片列表
     */
    List<NewsImage> getImagesByNewsId(String newsId);
    
    /**
     * 根据ID获取帖子详情
     * @param newsId 帖子ID
     * @return 帖子对象
     */
    News getNewsById(String newsId);
    
    /**
     * 根据用户ID获取已发布的帖子列表
     * @param authorId 用户ID
     * @return 帖子列表
     */
    List<News> getNewsByAuthorId(String authorId);
}