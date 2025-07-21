package com.son.bookhaven.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.son.bookhaven.R;
import com.son.bookhaven.data.dto.response.VoucherResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends ArrayAdapter<VoucherResponse> {

    private final LayoutInflater inflater;
    private final NumberFormat currencyFormatter;

    public VoucherAdapter(Context context, List<VoucherResponse> vouchers) {
        super(context, R.layout.item_voucher, vouchers);
        this.inflater = LayoutInflater.from(context);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_voucher, parent, false);
        }

        VoucherResponse voucher = getItem(position);
        if (voucher != null) {
            TextView textViewVoucherCode = view.findViewById(R.id.textViewVoucherCode);
            TextView textViewVoucherDiscount = view.findViewById(R.id.textViewVoucherDiscount);
            TextView textViewMinimumOrder = view.findViewById(R.id.textViewMinimumOrder);

            // Handle "No voucher" special case
            if ("No voucher".equals(voucher.getCode())) {
                textViewVoucherCode.setText("No voucher");
                textViewVoucherDiscount.setVisibility(View.GONE);
                textViewMinimumOrder.setVisibility(View.GONE);
            } else {
                textViewVoucherCode.setText(voucher.getCode());

                // Format discount value
                String discountText = "Discount: " +
                        (voucher.isPercentageDiscount() ?
                                voucher.getDiscountValue() + "%" :
                                currencyFormatter.format(voucher.getDiscountValue()));
                textViewVoucherDiscount.setText(discountText);
                textViewVoucherDiscount.setVisibility(View.VISIBLE);

                // Format minimum order
                if (voucher.getMinimumOrderPrice() != null &&
                        voucher.getMinimumOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
                    textViewMinimumOrder.setText("Min order: " +
                            currencyFormatter.format(voucher.getMinimumOrderPrice()));
                    textViewMinimumOrder.setVisibility(View.VISIBLE);
                } else {
                    textViewMinimumOrder.setVisibility(View.GONE);
                }
            }
        }

        return view;
    }
}