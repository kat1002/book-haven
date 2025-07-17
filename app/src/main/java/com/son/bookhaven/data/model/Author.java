package com.son.bookhaven.data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private List<BookVariant> variants = new ArrayList<>();
}