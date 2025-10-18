package com.x1.groo.forest.mate.query.controller;

import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import com.x1.groo.forest.mate.query.dto.MateItemDTO;
import com.x1.groo.security.CustomUserDetails;
import com.x1.groo.forest.mate.query.dto.MateForestDetailDTO;
import com.x1.groo.forest.mate.query.dto.MateForestResponseDTO;
import com.x1.groo.forest.mate.query.service.MateServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "우정의 숲")
@RestController
@RequestMapping("/api/mate")
@RequiredArgsConstructor
public class MateController {

    private final MateServiceImpl mateService;

    @Operation(summary = "우정의 숲 목록 조회")
    @GetMapping("/forests")
    public List<MateForestResponseDTO> getMyForests(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        int userId = user.getUserId();

        // 유저 ID로 우정의 숲 리스트 조회
        return mateService.getForestsByUserId(userId);
    }

    @Operation(summary = "우정의 숲 상세 조회")
    @GetMapping("/detail/{forestId}")
    public MateForestDetailDTO getForestDetail(@PathVariable int forestId) {

        return mateService.getForestDetail(forestId);
    }

    @Operation(summary = "기록의 조각 조회")
    @GetMapping("/items/{categoryId}/{forestId}")
    public ResponseEntity<List<MateItemDTO>> getItems(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable int categoryId,
            @PathVariable int forestId
    ) {
        List<MateItemDTO> items = mateService.getPieceOfMemory(user.getUserId(), categoryId, forestId);

        return ResponseEntity.ok(items);
    }

}
