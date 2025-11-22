package com.example.qing.service;

import com.example.qing.entity.News;
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
}