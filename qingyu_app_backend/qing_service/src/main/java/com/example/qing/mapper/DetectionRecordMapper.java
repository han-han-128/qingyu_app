package com.example.qing.mapper;

import com.example.qing.entity.DetectionRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检测记录Mapper接口
 */
@Mapper
public interface DetectionRecordMapper {
    /**
     * 插入检测记录
     * @param detectionRecord 检测记录对象
     * @return 影响行数
     */
    @Insert("INSERT INTO detection_records (id, user_id, image_url, detected_image_url, detection_result, detection_time, confidence) " +
            "VALUES (#{id}, #{userId}, #{imageUrl}, #{detectedImageUrl}, #{detectionResult}, #{detectionTime}, #{confidence})")
    int insertDetectionRecord(DetectionRecord detectionRecord);
}