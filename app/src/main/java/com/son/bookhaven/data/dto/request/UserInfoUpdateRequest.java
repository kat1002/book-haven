package com.son.bookhaven.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class UserInfoUpdateRequest {
    private String fullName;
    private String phone;
} 