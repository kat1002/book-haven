package com.son.bookhaven.data.model;

import java.time.LocalDateTime;
import java.util.List;

public class Cart {
    private int cartId;
    private int userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cartKey;

    private List<CartItem> cartItems;
    private User user;
}
