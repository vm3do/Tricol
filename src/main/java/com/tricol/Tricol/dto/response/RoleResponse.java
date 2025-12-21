package com.tricol.Tricol.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private List<String> permissions;
    private LocalDateTime createdAt;
}
