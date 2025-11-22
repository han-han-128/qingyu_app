package com.example.qing.controller;

import com.example.qing.entity.ReportRecord;
import com.example.qing.result.JsonResult;
import com.example.qing.entity.User;
import com.example.qing.service.ReportRecordService;
import com.example.qing.service.Userservice;
import com.example.qing.utils.AliOssUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 紧急情况上报控制器
 * 处理紧急情况上报相关的请求
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportRecordService reportRecordService;

    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private Userservice userservice;

    /**
     * 紧急情况上报
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param address 详细地址
     * @param description 上报描述
     * @param emergencyLevel 紧急程度：1-一般，2-紧急，3-危急
     * @param files 上报图片文件列表
     * @return 上报结果
     */
    @PostMapping("/emergency")
    public JsonResult reportEmergency(
            @RequestParam String userId,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String address,
            @RequestParam String description,
            @RequestParam int emergencyLevel,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) MultipartFile[] files) {
        
        // 日志记录接收到的参数
        System.out.println("接收到上报请求，用户ID: " + userId);
        System.out.println("接收到单个文件: " + (file != null && !file.isEmpty()));
        System.out.println("接收到文件数组: " + (files != null && files.length > 0));

        // 1. 参数校验
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(address) || StringUtils.isBlank(description)) {
            return JsonResult.errorMsg("必填参数不能为空");
        }
        if (emergencyLevel < 1 || emergencyLevel > 3) {
            return JsonResult.errorMsg("紧急程度必须为1-3之间的整数");
        }

        try {
            // 2. 创建上报记录对象
            ReportRecord reportRecord = new ReportRecord();
            
            // 根据用户名查询真正的用户ID
            User user = userservice.getUserByUsername(userId);
            if (user != null) {
                // 使用真正的用户ID (uid) 而不是用户名
                reportRecord.setUserId(user.getUid());
            } else {
                return JsonResult.errorMsg("用户不存在");
            }
            
            reportRecord.setLatitude(latitude);
            reportRecord.setLongitude(longitude);
            reportRecord.setAddress(address);
            reportRecord.setDescription(description);
            reportRecord.setEmergencyLevel(emergencyLevel);

            // 3. 处理图片上传到阿里云OSS
            List<String> imageUrls = new ArrayList<>();
            
            // 处理单个文件（微信小程序wx.uploadFile默认传递的文件）
            if (file != null && !file.isEmpty()) {
                try {
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String objectName = "report/" + UUID.randomUUID() + extension;
                    String imageUrl = aliOssUtil.upload(file.getBytes(), objectName);
                    imageUrls.add(imageUrl);
                    System.out.println("成功上传单个文件: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("上传单个文件失败: " + e.getMessage());
                }
            }
            
            // 处理文件数组
            if (files != null && files.length > 0) {
                for (MultipartFile f : files) {
                    if (!f.isEmpty()) {
                        try {
                            String originalFilename = f.getOriginalFilename();
                            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            String objectName = "report/" + UUID.randomUUID() + extension;
                            String imageUrl = aliOssUtil.upload(f.getBytes(), objectName);
                            imageUrls.add(imageUrl);
                            System.out.println("成功上传文件数组中的文件: " + imageUrl);
                        } catch (Exception e) {
                            System.err.println("上传文件数组中的文件失败: " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("总共上传了" + imageUrls.size() + "个文件");

            // 4. 保存上报记录和图片信息到数据库
            Long reportId = reportRecordService.reportEmergency(reportRecord, imageUrls);

            if (reportId != null && reportId > 0) {
                return JsonResult.buildData(reportId);
            } else {
                return JsonResult.errorMsg("上报失败，请稍后重试");
            }

        } catch (IOException e) {
            // 处理文件上传异常
            e.printStackTrace();
            return JsonResult.errorMsg("图片上传失败，请稍后重试");
        } catch (Exception e) {
            // 处理其他异常
            e.printStackTrace();
            return JsonResult.errorMsg("上报失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取上报详情
     * @param reportId 上报记录ID
     * @return 上报记录详情
     */
    @GetMapping("/{reportId}")
    public JsonResult getReportDetail(@PathVariable Long reportId) {
        if (reportId == null || reportId <= 0) {
            return JsonResult.errorMsg("上报ID无效");
        }

        try {
            ReportRecord reportRecord = reportRecordService.getReportRecordById(reportId);
            if (reportRecord != null) {
                return JsonResult.buildData(reportRecord);
            } else {
                return JsonResult.errorMsg("上报记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取上报详情失败");
        }
    }

    /**
     * 根据用户ID获取上报历史
     * @param userId 用户ID
     * @return 上报历史记录列表
     */
    @GetMapping("/user/{userId}")
    public JsonResult getUserReportHistory(@PathVariable String userId) {
        if (StringUtils.isBlank(userId)) {
            return JsonResult.errorMsg("用户ID无效");
        }

        try {
            // 先尝试直接按用户ID查询
            List<ReportRecord> reportRecords = reportRecordService.getReportRecordsByUserId(userId);
            
            // 如果没有查询到，可能传入的是用户名，尝试查询用户ID后再查询
            if (reportRecords == null || reportRecords.isEmpty()) {
                User user = userservice.getUserByUsername(userId);
                if (user != null) {
                    reportRecords = reportRecordService.getReportRecordsByUserId(user.getUid());
                }
            }
            
            return JsonResult.buildData(reportRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取上报历史失败");
        }
    }

    /**
     * 获取所有上报记录（管理员）
     * @return 所有上报记录列表
     */
    @GetMapping("/list")
    public JsonResult getAllReportRecords() {
        try {
            List<ReportRecord> reportRecords = reportRecordService.getAllReportRecords();
            return JsonResult.buildData(reportRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("获取所有上报记录失败");
        }
    }

    /**
     * 更新上报记录状态
     * @param reportId 上报记录ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/{reportId}/status")
    public JsonResult updateReportStatus(@PathVariable Long reportId, @RequestParam Integer status) {
        if (reportId == null || reportId <= 0 || status == null) {
            return JsonResult.errorMsg("参数无效");
        }

        try {
            boolean success = reportRecordService.updateReportStatus(reportId, status);
            if (success) {
                return JsonResult.build(200, "OK", null);
            } else {
                return JsonResult.errorMsg("更新上报状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.errorMsg("更新上报状态失败");
        }
    }
}