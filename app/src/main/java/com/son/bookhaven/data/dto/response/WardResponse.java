package com.son.bookhaven.data.dto.response;

public class WardResponse {
    private String name;
    private int code;
    private String codename;
    private String division_type;
    private String short_codename;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}