package com.example.qing.mapper;

import com.example.qing.entity.ReportImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 紧急情况上报图片Mapper接口
 */
@Mapper
public interface ReportImageMapper {
    /**
     * 插入上报图片
     * @param reportImage 上报图片对象
     * @return 影响行数
     */
    @Insert("INSERT INTO report_image (report_id, image_url) VALUES (#{reportId}, #{imageUrl})")
    int insertReportImage(ReportImage reportImage);

    /**
     * 根据上报记录ID查询图片
     * @param reportId 上报记录ID
     * @return 图片列表
     */
    @Select("SELECT * FROM report_image WHERE report_id = #{reportId}")
    List<ReportImage> selectReportImagesByReportId(Long reportId);
}