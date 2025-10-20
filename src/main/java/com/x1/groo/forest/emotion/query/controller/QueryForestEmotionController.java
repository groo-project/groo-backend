package com.x1.groo.forest.emotion.query.controller;

import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionDetailDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionMailboxListDTO;
import com.x1.groo.forest.emotion.query.dto.QueryForestEmotionForestItemDTO;
import com.x1.groo.forest.emotion.query.service.QueryForestEmotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "감정숲")
@RestController
@RequestMapping("/api")
@Slf4j
public class QueryForestEmotionController {

    private final QueryForestEmotionService queryForestEmotionService;

    @Autowired
    public QueryForestEmotionController(QueryForestEmotionService queryForestEmotionService) {
        this.queryForestEmotionService = queryForestEmotionService;
    }

    @Operation(summary = "기록의 조각 조회")
    @GetMapping("/items/{categoryId}/{forestId}")
    public ResponseEntity<?> getItems(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int categoryId,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        List<QueryForestEmotionForestItemDTO> items = queryForestEmotionService.getPieceOfMemory(userId, categoryId, forestId);

        return ResponseEntity.ok(items);
    }

    @Operation(summary = "작성된 방명록 리스트 조회")
    @GetMapping("/mailbox-lists/{forestId}")
    public ResponseEntity<List<QueryForestEmotionMailboxListDTO>> getMailboxList(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        List<QueryForestEmotionMailboxListDTO> result = queryForestEmotionService.getMailboxList(userId, forestId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "감정의 숲에 작성된 방명록 상세 조회")
    @GetMapping("/mailbox-detail/{id}")
    public ResponseEntity<List<QueryForestEmotionMailboxDTO>> getMailboxDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int id) {

        int userId = user.getUserId();

        List<QueryForestEmotionMailboxDTO> result = queryForestEmotionService.getMailboxDetail(userId, id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "감정의 숲 상세 조회")
    @GetMapping("/detail/{forestId}")
    public ResponseEntity<List<QueryForestEmotionDetailDTO>> getForestDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int forestId) {

        int userId = user.getUserId();

        List<QueryForestEmotionDetailDTO> result = queryForestEmotionService.getForestDetail(userId, forestId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "소유한 숲 조회")
    @GetMapping("/myforest")
    public ResponseEntity<List<QueryForestEmotionListDTO>> getMyForest(
            @AuthenticationPrincipal CustomUserDetails user) {

        int userId = user.getUserId();

        List<QueryForestEmotionListDTO> result = queryForestEmotionService.getForestList(userId);
        return ResponseEntity.ok(result);
    }
}
