package com.x1.groo.common.sse.payload;

public record ForestUpdatedPayload(int userId, int forestId, String newName) {
}
