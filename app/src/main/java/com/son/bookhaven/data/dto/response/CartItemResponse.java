package com.son.bookhaven.data.dto.response;

import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.BookVariant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CartItemResponse implements Serializable {
    private int cartItemId;
    private int quantity;
    private int bookId;
    private String title;
    private BigDecimal price;
    private List<String> bookImages;
    private Boolean isSelected; // Có thể null theo API, nên dùng Boolean thay vì boolean

    private List<String> authorName;
    // Constructors
    public CartItemResponse() {}

    public CartItemResponse(BookVariant variant, int quantity, boolean isChecked) {
        this.bookId = variant.getBookId();
        this.title = variant.getTitle();
        this.price = variant.getPrice();
        this.quantity = quantity;
        this.isSelected = isChecked;
        this.bookImages = variant.getBookImages().stream()
                .map(BookImage::getImageUrl)
                .collect(Collectors.toList());
    }

    // Getter và Setter


    public List<String> getAuthorName() {
        return authorName;
    }

    public void setAuthorName(List<String> authorName) {
        this.authorName = authorName;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getBookImages() {
        return bookImages;
    }

    public void setBookImages(List<String> bookImages) {
        this.bookImages = bookImages;
    }

    public Boolean getIsSelected() {
        return isSelected != null ? isSelected : false; // Default to false if null
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(quantity));
    }
}
