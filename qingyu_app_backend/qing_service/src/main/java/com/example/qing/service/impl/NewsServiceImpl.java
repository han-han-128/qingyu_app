package com.example.qing.service.impl;

import com.example.qing.entity.News;
import com.example.qing.entity.NewsImage;
import com.example.qing.mapper.NewsImageMapper;
import com.example.qing.mapper.NewsMapper;
import com.example.qing.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 帖子服务实现类
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private NewsImageMapper newsImageMapper;

    /**
     * 发布帖子
     * 使用事务确保帖子和图片信息要么都保存成功，要么都失败
     */
    @Override
    @Transactional
    public boolean publishNews(News news, List<String> imageUrls) {
        try {
            // 1. 保存帖子信息
            int newsResult = newsMapper.insertNews(news);
            if (newsResult <= 0) {
                return false;
            }

            // 2. 保存图片信息（如果有图片）
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    NewsImage newsImage = new NewsImage();
                    newsImage.setId(UUID.randomUUID().toString());
                    newsImage.setNewsId(news.getId());
                    newsImage.setImageUrl(imageUrl);

                    int imageResult = newsImageMapper.insertNewsImage(newsImage);
                    if (imageResult <= 0) {
                        // 如果保存图片失败，事务会回滚
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            // 发生异常时，事务会回滚
            e.printStackTrace();
            return false;
        }
    }
}