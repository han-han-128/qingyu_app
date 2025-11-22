package com.example.qing.mapper;

import com.example.qing.entity.ReportRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 紧急情况上报记录Mapper接口
 */
@Mapper
public interface ReportRecordMapper {
    /**
     * 插入上报记录
     * @param reportRecord 上报记录对象
     * @return 影响行数
     */
    @Insert("INSERT INTO report_record (report_no, user_id, latitude, longitude, address, description, emergency_level, status, report_time) " +
            "VALUES (#{reportNo}, #{userId}, #{latitude}, #{longitude}, #{address}, #{description}, #{emergencyLevel}, #{status}, #{reportTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertReportRecord(ReportRecord reportRecord);

    /**
     * 根据ID查询上报记录
     * @param id 上报记录ID
     * @return 上报记录对象
     */
    @Select("SELECT * FROM report_record WHERE id = #{id}")
    ReportRecord selectReportRecordById(Long id);

    /**
     * 根据用户ID查询上报记录
     * @param userId 用户ID
     * @return 上报记录列表
     */
    @Select("SELECT * FROM report_record WHERE user_id = #{userId}")
    List<ReportRecord> selectReportRecordsByUserId(String userId);

    /**
     * 更新上报记录状态
     * @param id 上报记录ID
     * @param status 新状态
     * @return 影响行数
     */
    @Update("UPDATE report_record SET status = #{status} WHERE id = #{id}")
    int updateReportRecordStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询所有上报记录
     * @return 上报记录列表
     */
    @Select("SELECT * FROM report_record")
    List<ReportRecord> selectAllReportRecords();
}