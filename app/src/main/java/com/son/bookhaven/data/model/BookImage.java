package com.son.bookhaven.data.model;

import java.time.LocalDateTime;

public class BookImage {

    private int bookImageId;
    private int bookId;
    private String imageUrl;
    private int displayIndex;

    private LocalDateTime createdAt;

    private Book book;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getBookImageId() {
        return bookImageId;
    }

    public void setBookImageId(int bookImageId) {
        this.bookImageId = bookImageId;
    }
}