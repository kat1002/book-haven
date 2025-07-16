package com.son.bookhaven.data.model;

import java.io.Serializable;
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
public class BookVariant implements Serializable {
    private int variantId;
    private String title;
    private String description;
    private int bookId;
    private String isbn;
    private BigDecimal price;
    private int stock;
    private int categoryId;
    private int publisherId;
    private int publicationYear;
    private LanguageCode language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int reviewCount;
    private int reviewTotal;

    // Navigation properties
    private Book book;
    private Category category;
    private Publisher publisher;
    private Set<Author> authors = new HashSet<>();
    private List<BookImage> bookImages = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    private List<OrderDetail> orderDetails = new ArrayList<>();

    /**
     * Calculate the average rating based on review count and total.
     *
     * @return Average rating as a decimal or 0 if no reviews
     */
    public BigDecimal getAverageRating() {
        if (reviewCount == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(reviewTotal).divide(new BigDecimal(reviewCount), 1, BigDecimal.ROUND_HALF_UP);
    }
}
