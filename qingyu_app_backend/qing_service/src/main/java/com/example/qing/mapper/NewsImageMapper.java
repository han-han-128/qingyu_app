package com.example.qing.mapper;

import com.example.qing.entity.NewsImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据帖子ID获取图片列表
     * @param newsId 帖子ID
     * @return 图片列表
     */
    @Select("SELECT id, news_id, image_url FROM news_image WHERE news_id = #{newsId}")
    List<NewsImage> getImagesByNewsId(String newsId);
    
    /**
     * 批量插入帖子图片
     * @param images 图片列表
     */
    @Insert("<script>INSERT INTO news_image (id, news_id, image_url) VALUES " +
            "<foreach collection='list' item='item' separator=', '>(#{item.id}, #{item.newsId}, #{item.imageUrl})</foreach></script>")
    void batchInsertImages(List<NewsImage> images);
}