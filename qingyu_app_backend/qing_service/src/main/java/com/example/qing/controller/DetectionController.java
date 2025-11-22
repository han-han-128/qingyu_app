package com.example.qing.controller;

import com.example.qing.entity.DetectionRecord;
import com.example.qing.entity.User;
import com.example.qing.result.JsonResult;
import com.example.qing.service.DetectionRecordService;
import com.example.qing.service.Userservice;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 检测控制器
 * 处理图片检测相关请求
 */
@RestController
@RequestMapping("/detection")
public class DetectionController {

    @Autowired
    private DetectionRecordService detectionRecordService;
    
    @Autowired
    private Userservice userservice;

    /**
     * 图片检测接口
     * @param userId 用户ID
     * @param image 待检测图片
     * @return 检测结果
     */
    @PostMapping("/detect")
    public JsonResult detectImage(@RequestParam String userId, @RequestParam MultipartFile image) {
        // 参数校验
        if (StringUtils.isBlank(userId)) {
            return JsonResult.errorMsg("用户ID不能为空");
        }

        if (image.isEmpty()) {
            return JsonResult.errorMsg("请选择要检测的图片");
        }

        try {
            // 添加日志记录，便于调试
            System.out.println("接收到检测请求，用户标识: " + userId);
            
            // 尝试根据用户名查询用户
            User user = userservice.getUserByUsername(userId);
            
            if (user != null) {
                System.out.println("成功找到用户，用户ID: " + user.getUid());
                // 使用真正的用户ID (uid) 而不是用户名
                DetectionRecord detectionRecord = detectionRecordService.saveDetectionRecord(user.getUid(), image);
                return JsonResult.buildData(detectionRecord);
            } else {
                // 如果通过用户名未找到用户，记录日志信息
                System.out.println("未找到对应用户名的用户: " + userId);
                
                // 注意：这里不再直接返回错误，而是尝试继续处理
                // 这样可以避免因为用户查找失败而影响核心功能
                // 但需要确保DetectionRecordService能够处理这种情况
                try {
                    // 尝试使用原始ID保存检测记录
                    DetectionRecord detectionRecord = detectionRecordService.saveDetectionRecord(userId, image);
                    return JsonResult.buildData(detectionRecord);
                } catch (Exception innerEx) {
                    System.err.println("使用原始ID保存检测记录失败: " + innerEx.getMessage());
                    return JsonResult.errorMsg("图片检测失败，请稍后重试");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("图片检测失败，请稍后重试");
        }
    }
}