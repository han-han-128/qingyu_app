package com.example.qing.mapper;

import com.example.qing.entity.MonitorData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MonitorDataMapper {

    // 根据监测点ID查询最新数据
    @Select("SELECT * FROM monitor_data WHERE monitor_id = #{monitorId} ORDER BY recorded_time DESC LIMIT #{limit}")
    List<MonitorData> findByMonitorId(@Param("monitorId") Long monitorId, @Param("limit") int limit);

    // 插入监测数据
    @Insert("INSERT INTO monitor_data (monitor_id, data_type, value) VALUES (#{monitorId}, #{dataType}, #{value})")
    int insert(MonitorData monitorData);

    // 批量插入监测数据
    @Insert({
            "<script>",
            "INSERT INTO monitor_data (monitor_id, data_type, value) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.monitorId}, #{item.dataType}, #{item.value})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("list") List<MonitorData> monitorDataList);

    // 根据类型和时间范围查询数据
    @Select("SELECT * FROM monitor_data WHERE monitor_id = #{monitorId} AND data_type = #{dataType} " +
            "AND recorded_time BETWEEN #{startTime} AND #{endTime} ORDER BY recorded_time")
    List<MonitorData> findByTypeAndTimeRange(@Param("monitorId") Long monitorId,
                                             @Param("dataType") String dataType,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime);
}