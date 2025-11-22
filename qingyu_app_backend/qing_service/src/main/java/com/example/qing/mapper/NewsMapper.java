package com.example.qing.mapper;

import com.example.qing.entity.News;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子Mapper接口
 */
@Mapper
public interface NewsMapper {
    /**
     * 插入帖子
     * @param news 帖子对象
     * @return 影响行数
     */
    @Insert("INSERT INTO news (id, title, content, author_id, status, create_time, update_time, view_count) " +
            "VALUES (#{id}, #{title}, #{content}, #{authorId}, #{status}, #{createTime}, #{updateTime}, #{viewCount})")
    int insertNews(News news);
}