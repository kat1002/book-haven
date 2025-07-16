package com.son.bookhaven.data.dto.response;

import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.Category;
import com.son.bookhaven.data.model.Publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponse {
    private int bookId;
    private String title;
}
