package com.x1.groo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 도메인 별 분리

    // s3
    S3_PRESIGNED_URL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "S3 presigned URL 생성 실패"),
    S3_LIST_OBJECTS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "S3 객체 목록 조회 실패"),

    // email
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E002", "이메일 전송에 실패했습니다."),
    EMAIL_TEMPLATE_LOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E003", "이메일 템플릿 로드에 실패했습니다."),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "유저 정보를 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다."),
    USER_NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "U003", "이미 존재하는 닉네임입니다."),
    USER_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "U004", "이메일 인증을 먼저 진행해 주세요."),
    USER_EMAIL_AUTH_FAILED(HttpStatus.BAD_REQUEST, "U005", "인증 번호가 일치하지 않습니다."),

    // diary
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 일기입니다."),
    DIARY_SAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "존재하지 않는 임시 저장입니다."),
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "D003", "해당 일기에 접근 권한이 없습니다."),
    DIARY_ALREADY_WRITTEN(HttpStatus.BAD_REQUEST, "D004", "일기는 하루에 하나만 작성 가능합니다."),

    // item
    ITEM_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "아이템 카테고리를 찾을 수 없습니다."),
    ITEMS_NOT_FOUND_FOR_CATEGORY_EMOTION(HttpStatus.NOT_FOUND, "I002", "해당 카테고리와 감정에 맞는 아이템이 없습니다."),
    ITEM_STORAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "I003", "보관함/아이템 관련 권한이 없습니다."),
    ITEM_STORAGE_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "I004", "아이템 저장에 실패했습니다."),

    // forest
    FOREST_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "존재하지 않는 숲입니다."),
    FOREST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "F002", "해당 숲에 대한 접근 권한이 없습니다."),
    FOREST_INVITE_CODE_INVALID(HttpStatus.BAD_REQUEST, "F003", "초대코드가 유효하지 않습니다."),
    FOREST_ALREADY_ACCEPTED_INVITE(HttpStatus.BAD_REQUEST, "F004", "이미 숲에 참여한 사용자입니다."),
    FOREST_FULL(HttpStatus.BAD_REQUEST, "F005", "이 숲은 이미 정원이 가득 찼습니다."),
    FOREST_INVITE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F006", "초대 코드를 생성하지 못했습니다."),
    FOREST_INVITE_CODE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F007", "초대 코드 저장 중 오류가 발생했습니다."),

    // forest 내 placement 관련
    PLACEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "배치된 아이템을 찾을 수 없습니다."),
    PLACEMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "본인의 배치만 수정/삭제할 수 있습니다."),

    // forest 내 mailbox 관련
    MAILBOX_ACCESS_DENIED(HttpStatus.FORBIDDEN, "M001", "본인의 숲에만 방명록을 삭제할 수 있습니다."),

    // forest 내 background 관련
    BACKGROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "배경을 찾을 수 없습니다."),

    // 로그
    LOG_FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L001", "로그 파일 읽기 실패"),
    LOG_FILE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L002", "로그 파일 다운로드 실패"),
    LOG_EXCEL_FILE_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L003", "로그 파일 엑셀 생성 실패"),

    // 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
