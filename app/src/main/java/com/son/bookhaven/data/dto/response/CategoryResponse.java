package com.son.bookhaven.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private int categoryId;
    private String categoryName;
    private String description;
}
