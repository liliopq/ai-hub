package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectResponse {
    private Integer collectCount;
    private Boolean isCollected;
}
