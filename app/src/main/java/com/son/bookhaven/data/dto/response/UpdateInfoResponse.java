package com.son.bookhaven.data.dto.response;

public class UpdateInfoResponse {
    private String message;

    public UpdateInfoResponse() {
    }

    public UpdateInfoResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 