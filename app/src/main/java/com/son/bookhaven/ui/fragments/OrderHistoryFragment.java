package com.son.bookhaven.ui.fragments; // Adjust your package name

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
import com.son.bookhaven.authService.TokenManager;
import com.son.bookhaven.data.adapters.OrderAdapter;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.data.dto.PagedResult;

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
    private List<OrderResponse> orderList;
    private OrderService orderService;
    private TokenManager tokenManager;

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

        // Initialize TokenManager
        tokenManager = new TokenManager(requireContext());
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
        orderService = ApiClient.getAuthenticatedClient(requireContext()).create(OrderService.class);
    }

    private void loadOrderHistory() {
        showLoadingState();

        // Get userId from TokenManager
        int userId = tokenManager.getUserId();
        if (userId == -1) {
            // User not logged in or userId not available
            showError("User not logged in");
            updateUIState(true);
            return;
        }

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

                        // Update UI on main thread with OrderResponse objects
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                orderAdapter.updateOrders(orderResponses);
                                updateUIState(orderResponses.isEmpty());
                                Log.d(TAG, "Order history loaded from API. Count: " + orderResponses.size());
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
    public void onOrderClick(OrderResponse order) {
        Toast.makeText(getContext(), "Clicked Order ID: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        // Navigate to OrderDetailFragment, passing the order ID or the entire order object
        navigateToOrderDetail(order.getOrderId());
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


    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }
}