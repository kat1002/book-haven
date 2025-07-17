package com.son.bookhaven.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private boolean isChecked;
    private int cartItemId;
    private int cartId;
    private int bookId;
    private int quantity;
    private LocalDateTime createdAt;
    private BookVariant variant;
    private Cart cart;

    public CartItem(BookVariant variant, int quantity, boolean isChecked) {
        this.variant = variant;
        this.quantity = quantity;
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal bigQuantity = BigDecimal.valueOf(quantity);
        return variant.getPrice().multiply(bigQuantity);
    }
}
