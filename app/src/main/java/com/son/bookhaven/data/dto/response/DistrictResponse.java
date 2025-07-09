package com.son.bookhaven.data.dto.response;

import java.util.List;

public class DistrictResponse {
    private String name;
    private int code;
    private String codename;
    private String division_type;
    private String short_codename;
    private List<WardResponse> wards;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public List<WardResponse> getWards() {
        return wards;
    }

    @Override
    public String toString() {
        return name;
    }
}