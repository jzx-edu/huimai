package com.huimai.module.file.service;

import com.huimai.common.BusinessException;
import com.huimai.config.HuimaiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务：保存视频、探测媒体信息、截取封面。
 * 未安装 FFmpeg 时仅原样存储，不影响主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String WEB_PREFIX = "/api/media/";
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private final HuimaiProperties properties;

    /** 缓存的 ffmpeg/ffprobe 可执行命令，null 表示不可用 */
    private String cachedFfmpeg;
    private String cachedFfprobe;
    private boolean detected;

    /**
     * 保存视频并尝试生成封面与媒体信息。
     */
    public StoredVideo storeVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }

        String month = LocalDate.now().format(MONTH_FMT);
        String original = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "video.mp4";
        String ext = parseExtension(original);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path videoDir = Paths.get(properties.getStorage().getRoot(),
                properties.getStorage().getVideoDir(), month);
        Path coverDir = Paths.get(properties.getStorage().getRoot(),
                properties.getStorage().getCoverDir(), month);
        try {
            Files.createDirectories(videoDir);
            Files.createDirectories(coverDir);

            Path target = videoDir.resolve(fileName);
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativeVideo = properties.getStorage().getVideoDir() + "/" + month + "/" + fileName;
            String videoUrl = WEB_PREFIX + relativeVideo;

            long duration = 0L;
            int width = 0;
            int height = 0;
            String coverUrl = null;

            ensureDetector();
            if (cachedFfmpeg != null) {
                duration = probeDuration(target.toString());
                int[] wh = probeSize(target.toString());
                if (wh != null) {
                    width = wh[0];
                    height = wh[1];
                }
                String coverName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
                Path coverPath = coverDir.resolve(coverName);
                if (captureCover(target.toString(), coverPath.toString())) {
                    coverUrl = WEB_PREFIX + properties.getStorage().getCoverDir()
                            + "/" + month + "/" + coverName;
                }
            }

            return StoredVideo.builder()
                    .videoUrl(videoUrl)
                    .coverUrl(coverUrl)
                    .absolutePath(target.toString())
                    .duration(duration)
                    .width(width)
                    .height(height)
                    .fileSize(file.getSize())
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("视频存储失败", e);
            throw new BusinessException("视频保存失败: " + e.getMessage());
        }
    }

    // ---------------- FFmpeg 支持 ----------------

    private void ensureDetector() {
        if (detected) {
            return;
        }
        detected = true;
        String configured = properties.getFfmpeg().getPath();
        if (StringUtils.hasText(configured) && new File(configured).exists()) {
            cachedFfmpeg = configured;
            cachedFfprobe = sibling(configured, "ffprobe");
            return;
        }
        // 尝试从 PATH 查找
        if (commandAvailable("ffmpeg")) {
            cachedFfmpeg = "ffmpeg";
            cachedFfprobe = "ffprobe";
        } else {
            log.warn("未检测到 FFmpeg，视频将原样存储，不生成封面与媒体信息");
        }
    }

    private long probeDuration(String input) {
        String out = runCommand(cachedFfprobe, "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1", input);
        try {
            return (long) Double.parseDouble(out.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private int[] probeSize(String input) {
        String out = runCommand(cachedFfprobe, "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=p=0:s=x", input);
        try {
            String[] parts = out.trim().split("x");
            return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
        } catch (Exception e) {
            return null;
        }
    }

    private boolean captureCover(String input, String output) {
        String out = runCommand(cachedFfmpeg, "-y", "-ss", "1",
                "-i", input, "-frames:v", "1", "-q:v", "2", output);
        boolean ok = new File(output).exists();
        if (!ok) {
            log.debug("封面生成失败: {}", out);
        }
        return ok;
    }

    private boolean commandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String runCommand(String... command) {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            p.waitFor();
        } catch (Exception e) {
            log.debug("命令执行失败: {}", e.getMessage());
        }
        return sb.toString();
    }

    private String sibling(String ffmpegPath, String targetName) {
        Path p = Paths.get(ffmpegPath);
        String fileName = p.getFileName().toString();
        String replaced = fileName.replace("ffmpeg", targetName);
        Path resolved = p.resolveSibling(replaced);
        return resolved.toString();
    }

    private String parseExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx >= 0 && idx < name.length() - 1) {
            return name.substring(idx).toLowerCase();
        }
        return ".mp4";
    }
}
