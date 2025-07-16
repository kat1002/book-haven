package com.son.bookhaven.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book implements Serializable {
    private int bookId;
    private String bookTitle;
    private List<BookVariant> variants = new ArrayList<>();
}