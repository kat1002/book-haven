package com.son.bookhaven.data.dto.response;

import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.Category;
import com.son.bookhaven.data.model.Publisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookVariantResponse {
    private int variantId;
    private int bookId;
    private int stock;
    private int publicationYear;
    private String title;
    private BigDecimal price;
    private int stockQuantity;
    private String format;
    private String description;
    private String isbn;
    private String languageCode;
    private String createdAt;
    private String updatedAt;
    private BookResponse book;
    private String language;
    private List<BookImage> images = new ArrayList<>();
    private Category category;
    private Publisher publisher;
    private Set<Author> authors = new HashSet<>();
}
