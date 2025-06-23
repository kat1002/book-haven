package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.son.bookhaven.data.adapters.OrderAdapter;
import com.son.bookhaven.data.model.Order;
import com.son.bookhaven.data.model.OrderDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrderHistoryFragment";

    private MaterialToolbar toolbar;
    private RecyclerView rvOrderHistory;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private OrderAdapter orderAdapter;
    private List<Order> orderList;

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
    }

    private void loadOrderHistory() {
        showLoadingState();

        // Simulate a network/database call
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // --- Dummy Data for Demonstration ---
            List<Order> dummyOrders = new ArrayList<>();

            // Example Order 1
            Order order1 = new Order();
            order1.setOderId(123456);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order1.setOrderDate(LocalDateTime.now().minusDays(10));
            }
            order1.setTotalAmount(150.00);
            order1.setStatus("Delivered");
            order1.setWard("Phường Trúc Bạch");
            order1.setDistrict("Quận Ba Đình");
            order1.setCity("Thành phố Hà Nội");
            order1.setOrderDetails(Arrays.asList(new OrderDetail(), new OrderDetail())); // Dummy details for item count
            dummyOrders.add(order1);

            // Example Order 2
            Order order2 = new Order();
            order2.setOderId(123457);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order2.setOrderDate(LocalDateTime.now().minusDays(5));
            }
            order2.setTotalAmount(75.50);
            order2.setStatus("Shipped");
            order2.setWard("Phường Trúc Bạch");
            order2.setDistrict("Quận Ba Đình");
            order2.setCity("Thành phố Hà Nội");
            order2.setOrderDetails(Arrays.asList(new OrderDetail()));
            dummyOrders.add(order2);

            // Example Order 3
            Order order3 = new Order();
            order3.setOderId(123458);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order3.setOrderDate(LocalDateTime.now().minusDays(1));
            }
            order3.setTotalAmount(200.00);
            order3.setStatus("Pending");
            order3.setWard("Phường Trúc Bạch");
            order3.setDistrict("Quận Ba Đình");
            order3.setCity("Thành phố Hà Nội");
            order3.setOrderDetails(Arrays.asList(new OrderDetail(), new OrderDetail(), new OrderDetail()));
            dummyOrders.add(order3);

            // Uncomment the next line to test the empty state
            // dummyOrders.clear();

            orderAdapter.updateOrders(dummyOrders);
            updateUIState(dummyOrders.isEmpty());

            Log.d(TAG, "Order history loaded. Count: " + dummyOrders.size());

        }, 1000); // Simulate 1 second loading time
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
}