package com.son.bookhaven.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem {
    private boolean isChecked;
    private int cartItemId;
    private int cartId;
    private int bookId;
    private int quantity;
    private LocalDateTime createdAt;
    private Book book;
    private Cart cart;

    public CartItem(Book book, int quantity, boolean isChecked) {
        this.book = book;
        this.quantity = quantity;
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal bigQuantity = BigDecimal.valueOf(quantity);
        return book.getPrice().multiply(bigQuantity);
    }
}
