package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.OrderService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.PagedResult;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.data.dto.OrderDetailResponse;
import com.son.bookhaven.data.adapters.OrderAdapter;
import com.son.bookhaven.data.model.Order;
import com.son.bookhaven.data.model.OrderDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrderHistoryFragment";

    private MaterialToolbar toolbar;
    private RecyclerView rvOrderHistory;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private OrderService orderService;

    public OrderHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        loadOrderHistory(); // Simulate loading order data

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_order_history);
        rvOrderHistory = view.findViewById(R.id.rv_order_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), orderList, this); // 'this' implements OnOrderClickListener
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setAdapter(orderAdapter);
        
        // Initialize API service
        orderService = ApiClient.getClient().create(OrderService.class);
    }

    private void loadOrderHistory() {
        showLoadingState();

        // Mock userId = 3 for testing
        int userId = 3;
        int page = 1;
        int pageSize = 10;

        Call<ApiResponse<PagedResult<OrderResponse>>> call = orderService.getUserOrders(userId, page, pageSize);
        call.enqueue(new Callback<ApiResponse<PagedResult<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResult<OrderResponse>>> call, Response<ApiResponse<PagedResult<OrderResponse>>> response) {
                Log.d(TAG, "Response body: " + response.body());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<PagedResult<OrderResponse>> apiResponse = response.body();
                    PagedResult<OrderResponse> pagedResult = apiResponse.getData();
                    
                    if (pagedResult != null && pagedResult.getItems() != null) {
                        List<OrderResponse> orderResponses = pagedResult.getItems();
                        Log.d(TAG, "OrderResponses: " + orderResponses.size() + " items");

                        // Convert API response to UI models
                        List<Order> orders = convertToOrderList(orderResponses);
                        
                        // Update UI on main thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                orderAdapter.updateOrders(orders);
                                updateUIState(orders.isEmpty());
                                Log.d(TAG, "Order history loaded from API. Count: " + orders.size());
                            });
                        }
                    } else {
                        // Handle empty data
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                orderAdapter.updateOrders(new ArrayList<>());
                                updateUIState(true);
                                Log.d(TAG, "No orders found");
                            });
                        }
                    }
                } else {
                    // Handle API error response
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateUIState(true);
                            String errorMsg = response.body() != null ? response.body().getMessage() : response.message();
                            showError("Failed to load orders: " + errorMsg);
                            Log.e(TAG, "API Error: " + response.code() + " - " + errorMsg);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResult<OrderResponse>>> call, Throwable t) {
                // Handle network error
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateUIState(true);
                        showError("Network error: " + t.getMessage());
                        Log.e(TAG, "Network Error: ", t);
                    });
                }
            }
        });
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        rvOrderHistory.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void updateUIState(boolean isEmpty) {
        progressBar.setVisibility(View.GONE);
        if (isEmpty) {
            rvOrderHistory.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvOrderHistory.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // --- OrderAdapter.OnOrderClickListener Implementation ---
    @Override
    public void onOrderClick(Order order) {
        Toast.makeText(getContext(), "Clicked Order ID: " + order.getOderId(), Toast.LENGTH_SHORT).show();
        // Navigate to OrderDetailFragment, passing the order ID or the entire order object
        navigateToOrderDetail(order.getOderId());
    }

    private void navigateToOrderDetail(int orderId) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Assuming you have an OrderDetailFragment that can take an order ID
        OrderDetailFragment orderDetailFragment = OrderDetailFragment.newInstance(orderId);

        fragmentTransaction.replace(R.id.frame_layout, orderDetailFragment); // Use your main fragment container ID
        fragmentTransaction.addToBackStack(null); // Add to back stack to allow return
        fragmentTransaction.commit();
    }

    private List<Order> convertToOrderList(List<OrderResponse> orderResponses) {
        if (orderResponses == null || orderResponses.isEmpty()) {
            return new ArrayList<>(); // Return empty list if no orders
        }
        List<Order> orders = new ArrayList<>();
        
        for (OrderResponse orderResponse : orderResponses) {
            Order order = new Order();
            
            // Map basic fields
            order.setOderId(orderResponse.getOrderId());
            order.setUserId(orderResponse.getUserId() != null ? orderResponse.getUserId() : 0);
            order.setTotalAmount(orderResponse.getTotalAmount());
            order.setStatus(orderResponse.getStatus());
            order.setDistrict(orderResponse.getDistrict());
            order.setCity(orderResponse.getCity());
            order.setWard(orderResponse.getWard());
            order.setStreet(orderResponse.getStreet());
            order.setRecipientName(orderResponse.getRecipientName());
            order.setPhone(orderResponse.getPhoneNumber());
            order.setNote(orderResponse.getNote());
            order.setDiscountedPrice(orderResponse.getDiscountedPrice());
            order.setPaymentMethod(orderResponse.getPaymentMethod());
            order.setVoucherCode(orderResponse.getVoucherCode());
            order.setVoucherId(orderResponse.getVoucherId() != null ? orderResponse.getVoucherId() : 0);
            order.setCartKey(orderResponse.getCartKey());
            
            // Parse date strings to LocalDateTime
            try {
                if (orderResponse.getOrderDate() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Handle different date formats from API
                    String dateStr = orderResponse.getOrderDate().replace("Z", "");
                    if (dateStr.contains("+")) {
                        dateStr = dateStr.substring(0, dateStr.indexOf("+"));
                    }
                    order.setOrderDate(LocalDateTime.parse(dateStr));
                }
                if (orderResponse.getCreatedAt() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String dateStr = orderResponse.getCreatedAt().replace("Z", "");
                    if (dateStr.contains("+")) {
                        dateStr = dateStr.substring(0, dateStr.indexOf("+"));
                    }
                    order.setCreatedAt(LocalDateTime.parse(dateStr));
                }
                if (orderResponse.getUpdatedAt() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String dateStr = orderResponse.getUpdatedAt().replace("Z", "");
                    if (dateStr.contains("+")) {
                        dateStr = dateStr.substring(0, dateStr.indexOf("+"));
                    }
                    order.setUpdatedAt(LocalDateTime.parse(dateStr));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
                // Set current date as fallback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    order.setOrderDate(LocalDateTime.now());
                }
            }
            
            // Convert OrderDetailResponse to OrderDetail for counting items
            List<OrderDetail> orderDetails = new ArrayList<>();
            if (orderResponse.getOrderDetails() != null) {
                for (OrderDetailResponse detailResponse : orderResponse.getOrderDetails()) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderId(detailResponse.getOrderId());
                    orderDetail.setBookId(detailResponse.getVariantId()); // Map variantId to bookId
                    orderDetail.setQuantity(detailResponse.getQuantity());
                    orderDetail.setUnitPrice(detailResponse.getUnitPrice());
                    orderDetail.setPricePerUnit(detailResponse.getUnitPrice());
                    orderDetail.setSubTotal(detailResponse.getSubtotal() != null ? detailResponse.getSubtotal() : 0.0);
                    orderDetail.setBookName("Book Item"); // Placeholder - will be filled later
                    orderDetails.add(orderDetail);
                }
            }
            order.setOrderDetails(orderDetails);
            
            orders.add(order);
        }
        
        return orders;
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }
}