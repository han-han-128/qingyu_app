package com.example.qing.mapper;

import com.example.qing.entity.News;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 查询所有已发布的帖子，按创建时间倒序排列
     * @return 帖子列表
     */
    @Select("SELECT id, title, content, author_id, status, create_time, update_time, view_count " +
            "FROM news WHERE status = 1 ORDER BY create_time DESC")
    List<News> selectAllPublishedNews();
    
    /**
     * 根据ID查询帖子
     * @param id 帖子ID
     * @return 帖子对象
     */
    @Select("SELECT id, title, content, author_id, status, create_time, update_time, view_count " +
            "FROM news WHERE id = #{id}")
    News selectNewsById(String id);
    
    /**
     * 根据用户ID查询已发布的帖子
     * @param authorId 用户ID
     * @return 帖子列表
     */
    @Select("SELECT id, title, content, author_id, status, create_time, update_time, view_count " +
            "FROM news WHERE author_id = #{authorId} AND status = 1 ORDER BY create_time DESC")
    List<News> selectNewsByAuthorId(String authorId);
}