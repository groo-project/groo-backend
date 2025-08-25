package com.x1.groo.common.logging;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogAnalyzerController {

    private final LogAnalyzerService logAnalyzerService;
    Logger logger = LoggerFactory.getLogger(LogAnalyzerController.class);

    public LogAnalyzerController(LogAnalyzerService logAnalyzerService) {
        this.logAnalyzerService = logAnalyzerService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        logger.debug("health check");
        return "I'm OK";
    }

    @GetMapping("/analyze/{date}")
    public Map<String, Object> analyzeLog(@PathVariable String date) {
        logger.debug("analyze 핸들러 메소드 실행");
        List<LogEntry> entries = logAnalyzerService.parseLogFile(date);

        Map<String, Object> analysis = new HashMap<>();
        if (entries.isEmpty()) {
            analysis.put("message", date + " 날짜의 로그 데이터가 없습니다.");
            analysis.put("totalRequests", 0);
        } else {
            analysis.put("totalRequests", entries.size());
            analysis.put("requestsByIp", logAnalyzerService.getRequestsByIp(entries));
            analysis.put("requestsByUri", logAnalyzerService.getRequestsByUri(entries));
            analysis.put("requestsByMethod", logAnalyzerService.getRequestsByMethod(entries));
        }

        return analysis;
    }

    @GetMapping("/download/{date}")
    public ResponseEntity<Resource> downloadExcel(@PathVariable String date) {
        logger.debug("download 핸들러 메소드 실행");
        try {
            List<LogEntry> entries = logAnalyzerService.parseLogFile(date);
            logAnalyzerService.exportToExcel(entries, date);

            Path file = Paths.get("logs/client-requests-" + date + ".xlsx");

            // 파일을 Resource 객체로 포장 -> HTTP 응답 본문에 담기 위해 필요
            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    // Excel MIME 타입 지정
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    // 브라우저가 파일 다운로드 대화창을 띄우도록 설정
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"client-requests-" + date + ".xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LOG_FILE_DOWNLOAD_FAILED, e);
        }
    }
}
