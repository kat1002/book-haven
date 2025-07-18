package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.son.bookhaven.MainActivity;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.CartApiService;
import com.son.bookhaven.data.adapters.CartAdapter;
import com.son.bookhaven.data.dto.request.CartItemUpdateRequest;
import com.son.bookhaven.data.dto.request.RemoveCartItemsRequest;
import com.son.bookhaven.data.dto.response.CartItemResponse;
import com.son.bookhaven.databinding.FragmentCartBinding;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding; // For ViewBinding
    private CartAdapter cartAdapter;
    private final List<CartItemResponse> cartItemsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartAdapter = new CartAdapter(
                (item, newQuantity) -> {

                    item.setQuantity(newQuantity);

                    updateCartTotals();

                    int index = cartItemsList.indexOf(item);
                    if (index != -1) {
                        binding.rvCartItems.getAdapter().notifyItemChanged(index);
                    }
                    updateCartItemQuantity(item, newQuantity);
                },
                (item, isChecked) -> {

                    item.setIsSelected(isChecked);
                    updateCartTotals();

                }
        );

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setAdapter(cartAdapter);

        // --- Load Data ---
        loadCartItems();

        // --- Button Listeners ---
        binding.btnUncheckAll.setOnClickListener(v -> {
            for (CartItemResponse item : cartItemsList) {
                item.setIsSelected(false);
            }
            cartAdapter.notifyDataSetChanged(); // Refresh all items
            updateCartTotals();
        });

        binding.btnSelectAll.setOnClickListener(v -> {
            for (CartItemResponse item : cartItemsList) {
                item.setIsSelected(true);
            }
            cartAdapter.notifyDataSetChanged(); // Refresh all items
            updateCartTotals();
        });

        binding.btnCheckout.setOnClickListener(v -> {
            List<CartItemResponse> selectedItems = cartItemsList.stream()
                    .filter(CartItemResponse::getIsSelected)
                    .collect(Collectors.toList());

            if (selectedItems.isEmpty()) {
                Snackbar.make(view, "Please select items to checkout.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Create a new instance of CheckoutFragment
            CheckoutFragment checkoutFragment = new CheckoutFragment();

            // Create a Bundle to pass data
            Bundle bundle = new Bundle();
            bundle.putSerializable("cart_items", (Serializable) selectedItems); // Pass the list as Serializable
            checkoutFragment.setArguments(bundle);

            // Navigate to CheckoutFragment
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(getParentFragmentManager().findFragmentById(this.getId()).getId(), checkoutFragment)
                        .addToBackStack(null) // Allows going back to CartFragment
                        .commit();
            } else {
                Log.e("CartFragment", "ParentFragmentManager is null, cannot navigate.");
            }
        });
        binding.btnCartActionDelete.setOnClickListener(v -> {
            // Get all selected items
            List<CartItemResponse> selectedItems = cartItemsList.stream()
                    .filter(CartItemResponse::getIsSelected)
                    .collect(Collectors.toList());

            if (selectedItems.isEmpty()) {
                Snackbar.make(view, "Please select items to delete", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Ask for confirmation before deletion
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Remove Items")
                    .setMessage("Are you sure you want to remove the selected items?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        // Delete selected items
                        removeSelectedItems(selectedItems);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Initial calculation
        updateCartTotals();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release the binding when the view is destroyed
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart data when the fragment becomes visible
        loadCartItems();
        // Update the badge in MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateCartBadge(0);
        }
    }

    private void removeSelectedItems(List<CartItemResponse> itemsToRemove) {


        CartApiService apiService = ApiClient.getAuthenticatedClient(requireContext()).create(CartApiService.class);

        List<Integer> cartItemIds = new ArrayList<>();
        for (CartItemResponse item : itemsToRemove) {
            cartItemIds.add(item.getCartItemId());
        }

        RemoveCartItemsRequest request = new RemoveCartItemsRequest();
        request.setCartItemIds(cartItemIds);
        // Call API to remove items
        apiService.removeCartItems(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {


                if (response.isSuccessful()) {

                    loadCartItems();
                    updateCartTotals();

                    Snackbar.make(binding.getRoot(),
                            "Removed " + itemsToRemove.size() + " item(s)",
                            Snackbar.LENGTH_SHORT).show();
                    Log.e("CartFragment", "Failed to remove items. Response code: " + response.body());
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge(cartItemsList.size());
                    }
                } else {
                    Log.e("CartFragment", "Failed to remove items. Response code: " + response.code());
                    Snackbar.make(binding.getRoot(), "Failed to remove items. Please try again.",
                            Snackbar.LENGTH_SHORT).show();

                    // Reload cart to ensure UI matches server state
                    loadCartItems();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {


                Log.e("CartFragment", "Error removing items: " + t.getMessage());
                Snackbar.make(binding.getRoot(), "Network error. Please try again.",
                        Snackbar.LENGTH_SHORT).show();

                // Reload cart to ensure UI matches server state
                loadCartItems();
            }
        });
    }

    private void loadCartItems() {

        Log.d("LoadCartItems", "Loading cart items from API...");

        CartApiService apiService = ApiClient.getAuthenticatedClient(requireContext()).create(CartApiService.class);
        // int userId = 11; // Thay bằng userId thực tế, có thể lấy từ SharedPreferences hoặc login
        Call<List<CartItemResponse>> call = apiService.getCart();

        call.enqueue(new Callback<List<CartItemResponse>>() {
            @Override
            public void onResponse(Call<List<CartItemResponse>> call, Response<List<CartItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartItemsList.clear(); // Xóa dữ liệu cũ
                    cartItemsList.addAll(response.body()); // Thêm dữ liệu từ API
                    Log.d("LoadCartItems", "Loaded " + cartItemsList.size() + " items from API.");
                    cartAdapter.updateData(cartItemsList); // Cập nhật RecyclerView
                    updateCartTotals(); // Cập nhật tổng
                    // Update the badge in MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge(cartItemsList.size());
                    }
                } else {
                    Log.e("LoadCartItems", "Failed to load cart. Response code: " + response.code());
                    Snackbar.make(binding.getRoot(), "Failed to load cart items.", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CartItemResponse>> call, Throwable t) {
                Log.e("LoadCartItems", "Error: " + t.getMessage());
                Snackbar.make(binding.getRoot(), "Network error. Please try again.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartTotals() {
        BigDecimal subtotal = BigDecimal.ZERO; // Initialize with BigDecimal.ZERO
        for (CartItemResponse item : cartItemsList) {
            if (Boolean.TRUE.equals(item.getIsSelected())) { // Kiểm tra null-safe
                subtotal = subtotal.add(item.getTotalPrice());
            }
        }
        binding.subtotalText.setText(String.format(new Locale("vi", "VN"), "%,.0f₫", subtotal));
        binding.totalText.setText(String.format(new Locale("vi", "VN"), "%,.0f₫", subtotal));

        // You can add logic for discount, shipping, etc., here if needed
        // binding.discountLayout.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
        // binding.discountText.setText(String.format(Locale.US, "-$%.2f", discount));
    }

    private void updateCartItemQuantity(CartItemResponse item, int newQuantity) {

        CartApiService apiService = ApiClient.getAuthenticatedClient(requireContext()).create(CartApiService.class);

        CartItemUpdateRequest request = new CartItemUpdateRequest(
                item.getBookId(),
                newQuantity
        );

        apiService.updateCartItemQuantity(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("CartFragment", "Cart item quantity updated successfully");
                    loadCartItems();
                } else {
                    Log.e("CartFragment", "Failed to update cart item quantity. Response code: " + response.code());
                    Snackbar.make(binding.getRoot(), "Failed to update cart. Please try again.", Snackbar.LENGTH_SHORT).show();

                    // Optionally reload cart to ensure UI matches server state
                    loadCartItems();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CartFragment", "Error updating cart item: " + t.getMessage());
                Snackbar.make(binding.getRoot(), "Network error. Please try again.", Snackbar.LENGTH_SHORT).show();

                // Optionally reload cart to ensure UI matches server state
                loadCartItems();
            }
        });
    }
}