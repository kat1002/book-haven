package com.son.bookhaven.data.model;

import java.time.LocalDateTime;
import java.util.List;

public class Publisher {
    private int publisherId;
    private String publisherName;
    private String contactInfo;
    private LocalDateTime createdAt;
    private List<Book> books;
}
