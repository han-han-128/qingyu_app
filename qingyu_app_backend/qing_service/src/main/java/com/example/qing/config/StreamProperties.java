package com.example.qing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stream")
@Data
public class StreamProperties {

    private Ffmpeg ffmpeg = new Ffmpeg();
    private Hls hls = new Hls();

    @Data
    public static class Ffmpeg {
        private String path = "ffmpeg";
    }

    @Data
    public static class Hls {
        private String outputDir = "./hls_output";
        private Integer segmentDuration = 2;
        private Integer playlistSize = 5;
    }
}