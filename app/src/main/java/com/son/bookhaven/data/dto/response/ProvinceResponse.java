package com.son.bookhaven.data.dto.response;

import java.util.List;

public class ProvinceResponse {
    private String name;
    private int code;
    private String codename;
    private String division_type;
    private int phone_code;
    private List<DistrictResponse> districts;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public List<DistrictResponse> getDistricts() {
        return districts;
    }

    @Override
    public String toString() {
        return name;
    }
}