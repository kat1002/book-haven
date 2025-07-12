package com.son.bookhaven.data.dto.response;

public class LoginResponse {
    private String token;
    private User user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public static class User {
        private int userId;
        private String fullName;
        private String role;

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
