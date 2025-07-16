package com.son.bookhaven.data.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Author {
    private int authorId;
    private String authorName;
    private String bio;
    private String createdAt;
    private Collection<BookVariant> variants = new ArrayList<>();
}