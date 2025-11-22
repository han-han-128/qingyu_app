package com.example.qing.mapper;

import com.example.qing.entity.NewsImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子图片Mapper接口
 */
@Mapper
public interface NewsImageMapper {
    /**
     * 插入帖子图片
     * @param newsImage 帖子图片对象
     * @return 影响行数
     */
    @Insert("INSERT INTO news_image (id, news_id, image_url) VALUES (#{id}, #{newsId}, #{imageUrl})")
    int insertNewsImage(NewsImage newsImage);
}