package com.son.bookhaven.data.dto.request;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private int roleID;
    private String returnUrl;

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, int roleID, String returnUrl) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.roleID = roleID;
        this.returnUrl = returnUrl;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}

