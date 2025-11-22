package com.example.qing.service;

import com.example.qing.config.StreamProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VideoStreamService {

    @Autowired
    private StreamProperties streamProperties;

    private final Map<Long, Process> activeStreams = new ConcurrentHashMap<>();

    public String startStream(Long monitorId, String mjpgUrl) {
        if (activeStreams.containsKey(monitorId)) {
            log.info("监测点 {} 的流已经在运行中", monitorId);
            return getHlsUrl(monitorId);
        }

        try {
            String outputDir = streamProperties.getHls().getOutputDir();
            File dir = new File(outputDir, String.valueOf(monitorId));
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String playlistPath = new File(dir, "playlist.m3u8").getAbsolutePath();
            String segmentPattern = new File(dir, "segment_%03d.ts").getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    streamProperties.getFfmpeg().getPath(),
                    "-i", mjpgUrl,
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-tune", "zerolatency",
                    "-g", "25",
                    "-sc_threshold", "0",
                    "-f", "hls",
                    "-hls_time", String.valueOf(streamProperties.getHls().getSegmentDuration()),
                    "-hls_list_size", String.valueOf(streamProperties.getHls().getPlaylistSize()),
                    "-hls_flags", "delete_segments",
                    "-hls_segment_filename", segmentPattern,
                    playlistPath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("FFmpeg[{}]: {}", monitorId, line);
                    }
                } catch (Exception e) {
                    log.error("读取FFmpeg输出失败: {}", e.getMessage());
                }
            }).start();

            activeStreams.put(monitorId, process);
            log.info("成功启动监测点 {} 的视频流转换", monitorId);

            return getHlsUrl(monitorId);

        } catch (Exception e) {
            log.error("启动视频流转换失败: {}", e.getMessage(), e);
            throw new RuntimeException("启动视频流转换失败: " + e.getMessage());
        }
    }

    public void stopStream(Long monitorId) {
        Process process = activeStreams.remove(monitorId);
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("已停止监测点 {} 的视频流转换", monitorId);
        }
    }

    public boolean isStreamActive(Long monitorId) {
        Process process = activeStreams.get(monitorId);
        return process != null && process.isAlive();
    }

    public String getHlsUrl(Long monitorId) {
        return "/hls/" + monitorId + "/playlist.m3u8";
    }

    @PreDestroy
    public void cleanup() {
        log.info("正在清理所有视频流...");
        activeStreams.forEach((id, process) -> {
            if (process.isAlive()) {
                process.destroy();
            }
        });
        activeStreams.clear();
    }
}