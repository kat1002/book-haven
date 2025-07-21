package com.son.bookhaven.data.dto.response;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class VoucherResponse {


    private int voucherId;

    private int issuerId;

    private String code;
    private BigDecimal minimumOrderPrice;
    private BigDecimal discountValue;
    private boolean isPercentageDiscount;
    private String expiryDate;
    private boolean isDisabled;



    @Override
    public String toString() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormatter.setMaximumFractionDigits(0); // No decimal places for VND

        NumberFormat percentFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        percentFormatter.setMaximumFractionDigits(0); // No decimal places for percentages

        return code + " - " + (isPercentageDiscount ?
                percentFormatter.format(discountValue) + "%" :
                currencyFormatter.format(discountValue));
    }
}