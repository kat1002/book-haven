package com.son.bookhaven.data.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidVouchersResponse {
    private List<VoucherResponse> validVouchers;
    private int totalCount;
    private String message;

    public List<VoucherResponse> getValidVouchers() {
        return validVouchers;
    }

    public void setValidVouchers(List<VoucherResponse> validVouchers) {
        this.validVouchers = validVouchers;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}