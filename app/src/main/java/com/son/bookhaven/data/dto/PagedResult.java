package com.son.bookhaven.data.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class PagedResult<T> {
    public List<T> items;
    public int totalItems;
    public int totalPages;
    public int currentPage;
    public int pageSize;
} 