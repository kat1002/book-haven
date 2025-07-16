package com.son.bookhaven.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookImage {
    private int bookImageId;
    private int bookId;
    private String imageUrl;
    private int displayIndex;
    private String createdAt;
    private Book book;
}