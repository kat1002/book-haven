package com.son.bookhaven.data.model;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private int roleId;
    private LocalDateTime createdAt;
    private String passwordHash;
    private boolean emailVerified;
    private List<Cart> cart;
    private List<Order> orders;
}
