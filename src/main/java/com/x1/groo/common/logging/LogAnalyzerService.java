package com.x1.groo.common.logging;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LogAnalyzerService {

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).*Client Request - IP: ([^,]+), Method: ([^,]+), URI: ([^,]+), User-Agent: (.+)"
    );

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    Logger logger = LoggerFactory.getLogger(LogAnalyzerService.class);

    /**
     * 지정된 날짜의 로그 파일을 파싱하여 접속 이력 LogEntry 객체 목록 반환
     *
     * @param date 로그 파일 날짜(yyyy-MM-dd) 형식
     * @return 파싱된 로그 항목 목록
     */
    public List<LogEntry> parseLogFile(String date) {
        String logFile = String.format("logs/client-requests.%s.log", date);
        Path logPath = Paths.get(logFile);

        if (!Files.exists(logPath)) {
            return new ArrayList<>(); // 해당 날짜의 로그 파일이 없으면 빈 리스트 반환
        }

        try {
            return Files.lines(logPath)
                    .map(this::parseLine)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("로그 파일 읽기 실패", e);
            throw new CustomException(ErrorCode.LOG_FILE_READ_FAILED, e);
        }
    }

    /**
     * 로그 라인을 파싱하여 접속 client에 대한 정보를 담은 로그들만 LogEntry 객체로 반환
     * 
     * @param line 파싱할 로그 라인
     * @return 파싱된 LogEntry 객체 or null
     */
    private LogEntry parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.find()) {

            return new LogEntry(
                    LocalDateTime.parse(matcher.group(1), DATE_FORMAT),
                    matcher.group(2).trim(),
                    matcher.group(3).trim(),
                    matcher.group(4).trim(),
                    matcher.group(5).trim()
            );
        }
        return null;
    }

    /**
     * 로그 항목 목록에서 IP 별 요청 수를 계산하여 반환
     *
     * @param entries
     * @return 키: IP 주소, 값: 요청 수 반환
     */
    public Map<String, Long> getRequestsByIp(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        LogEntry::getClientIp, // 어떤 기준으로 그루핑 할지
                        Collectors.counting() // 그룹 내에서 어떤 연산을 할지
                ));
    }

    /**
     * 로그 항목 목록에서 URI별 요청 수를 계산하여 반환
     *
     * @param entries 로그 항목 목록
     * @return 키: URI, 값: 요청 수 반환
     */
    public Map<String, Long> getRequestsByUri(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        LogEntry::getUri,
                        Collectors.counting()
                ));
    }

    /**
     * 로그 항목 목록에서 HTTP 메소드별 요청 수를 계산하여 반환
     *
     * @param entries 로그 항목 목록
     * @return 키: HTTP 메소드, 값: 요청 수 반환
     */
    public Map<String, Long> getRequestsByMethod(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        LogEntry::getMethod,
                        Collectors.counting()
                ));
    }

    /**
     * 지정된 날짜의 로그 항목 목록을 Excel 파일로 내보냄
     *
     * @param entries 내보낼 로그 항목 목록
     * @param date 로그 파일 날짜(yyyy-MM-dd) 형식 (파일 명에 포함됨)
     */
    public void exportToExcel(List<LogEntry> entries, String date) {
        try (Workbook workbook = new XSSFWorkbook()) {

            // 요청 상세 시트 생성
            Sheet detailSheet = workbook.createSheet("요청 상세");

            // 헤더 생성
            Row headerRow = detailSheet.createRow(0);
            headerRow.createCell(0).setCellValue("시간");
            headerRow.createCell(1).setCellValue("IP");
            headerRow.createCell(2).setCellValue("메소드");
            headerRow.createCell(3).setCellValue("URI");
            headerRow.createCell(4).setCellValue("User-Agent");

            // 데이터 입력
            int rowNum = 1;
            for (LogEntry entry : entries) {
                Row row = detailSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.createCell(1).setCellValue(entry.getClientIp());
                row.createCell(2).setCellValue(entry.getMethod());
                row.createCell(3).setCellValue(entry.getUri());
                row.createCell(4).setCellValue(entry.getUserAgent());
            }

            // 통계 시트 생성
            Sheet statsSheet = workbook.createSheet("통계");

            // IP별 통계
            createStatisticsSection(statsSheet, 0, "IP별 요청 수", getRequestsByIp(entries));

            // URI별 통계
            createStatisticsSection(statsSheet, getRequestsByIp(entries).size() + 2,
                    "URI별 요청 수", getRequestsByUri(entries));

            // 메소드별 통계
            createStatisticsSection(statsSheet, getRequestsByIp(entries).size() +
                            getRequestsByUri(entries).size() + 4,
                    "메소드별 요청 수", getRequestsByMethod(entries));

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 5; i++) {
                detailSheet.autoSizeColumn(i);
                statsSheet.autoSizeColumn(i);
            }

            // 파일 저장
            String fileName = String.format("logs/client-requests-%s.xlsx", date);
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }
        } catch (Exception e) {
            logger.error("로그 엑셀 파일 생성 실패", e);
            throw new CustomException(ErrorCode.LOG_EXCEL_FILE_GENERATE_FAILED, e);
        }
    }

    /**
     * 통계 데이터를 excel 시트에 추가
     *
     * @param sheet 데이터를 추가할 시트
     * @param startRow 데이터 추가를 시작할 행 인덱스
     * @param title 통계 섹션의 제목
     * @param data 통계 데이터 맵 (키: 구분, 값: 요청 수)
     */
    private void createStatisticsSection(Sheet sheet, int startRow, String title,
                                         Map<String, Long> data) {
        Row titleRow = sheet.createRow(startRow);
        titleRow.createCell(0).setCellValue(title);

        Row headerRow = sheet.createRow(startRow + 1);
        headerRow.createCell(0).setCellValue("구분");
        headerRow.createCell(1).setCellValue("요청 수");

        int rowNum = startRow + 2;
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

}
