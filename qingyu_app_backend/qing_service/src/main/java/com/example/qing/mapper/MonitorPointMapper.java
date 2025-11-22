package com.example.qing.mapper;

import com.example.qing.entity.MonitorPoint;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MonitorPointMapper {

    // 根据地图类型查询监测点
    @Select("SELECT * FROM monitor_point WHERE map_type = #{mapType} AND status = 'ONLINE'")
    List<MonitorPoint> findByMapType(@Param("mapType") String mapType);

    // 根据ID查询监测点
    @Select("SELECT * FROM monitor_point WHERE id = #{id}")
    MonitorPoint findById(@Param("id") Long id);

    // 查询所有监测点
    @Select("SELECT * FROM monitor_point")
    List<MonitorPoint> findAll();

    // 插入监测点
    @Insert("INSERT INTO monitor_point (name, map_type, latitude, longitude, status, video_stream_url, rtsp_url, description) " +
            "VALUES (#{name}, #{mapType}, #{latitude}, #{longitude}, #{status}, #{videoStreamUrl}, #{rtspUrl}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MonitorPoint monitorPoint);

    // 更新监测点
    @Update("UPDATE monitor_point SET name=#{name}, map_type=#{mapType}, latitude=#{latitude}, longitude=#{longitude}, " +
            "status=#{status}, video_stream_url=#{videoStreamUrl}, rtsp_url=#{rtspUrl}, description=#{description}, " +
            "updated_time=NOW() WHERE id=#{id}")
    int update(MonitorPoint monitorPoint);

    // 删除监测点
    @Delete("DELETE FROM monitor_point WHERE id = #{id}")
    int delete(@Param("id") Long id);

    // 更新状态
    @Update("UPDATE monitor_point SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}