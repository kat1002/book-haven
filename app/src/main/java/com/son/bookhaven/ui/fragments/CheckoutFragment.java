package com.son.bookhaven.ui.fragments; // Adjust package as necessary

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.MainActivity;
import com.son.bookhaven.PaymentActivity;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.AddressApiClient;
import com.son.bookhaven.apiHelper.AddressApiService;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.CheckOutService;
import com.son.bookhaven.data.adapters.CartItemAdapter; // Assuming this is your adapter package
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.CheckOutRequest;
import com.son.bookhaven.data.dto.response.CartItemResponse;
import com.son.bookhaven.data.dto.response.CheckOutResponse;
import com.son.bookhaven.data.dto.response.DistrictResponse;
import com.son.bookhaven.data.dto.response.ProvinceResponse;
import com.son.bookhaven.data.dto.response.WardResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private RecyclerView recyclerViewCartItems;
    private CartItemAdapter cartItemAdapter;


    private List<CartItemResponse> cartItems = new ArrayList<>();
    private MaterialTextView textViewSubtotalValue;
    private MaterialTextView textViewShippingValue;
    private MaterialTextView textViewTotalValue;
    private MaterialButton buttonPlaceOrder;
    private MaterialToolbar toolbar;

    // Address selection UI elements
    private AutoCompleteTextView autoCompleteTextViewProvince;
    private AutoCompleteTextView autoCompleteTextViewDistrict;
    private AutoCompleteTextView autoCompleteTextViewWard;
    private TextInputEditText textInputEditTextStreet;
    private TextInputEditText textInputEditTextRecipientName;
    private TextInputEditText textInputEditTextPhone;
    private TextInputEditText textInputEditTextNote;
    private TextInputEditText textInputEditTextEmail;
    private TextInputLayout textInputLayoutProvince;
    private TextInputLayout textInputLayoutDistrict;
    private TextInputLayout textInputLayoutWard;
    private TextInputLayout textInputLayoutStreet;
    private TextInputLayout textInputLayoutRecipientName;
    private TextInputLayout textInputLayoutPhone;
    private TextInputLayout textInputLayoutNote;

    private NumberFormat currencyFormatter;
    private final BigDecimal SHIPPING_COST = new BigDecimal("5.00"); // Example fixed shipping cost

    public CheckoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize formatter and API here
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));// Or your desired locale
        //vnAddressAPI = new VNAddressAPI(); // Initialize the mock API
        if (getArguments() != null && getArguments().containsKey("cart_items")) {
            cartItems = (List<CartItemResponse>) getArguments().getSerializable("cart_items");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        recyclerViewCartItems = view.findViewById(R.id.recyclerViewCartItems);
        textViewSubtotalValue = view.findViewById(R.id.textViewSubtotalValue);
        textViewShippingValue = view.findViewById(R.id.textViewShippingValue);
        textViewTotalValue = view.findViewById(R.id.textViewTotalValue);
        buttonPlaceOrder = view.findViewById(R.id.buttonPlaceOrder);

        // Initialize address input fields
        textInputLayoutRecipientName = view.findViewById(R.id.textInputLayoutRecipientName);
        textInputEditTextRecipientName = view.findViewById(R.id.textInputEditTextRecipientName);
        textInputLayoutPhone = view.findViewById(R.id.textInputLayoutPhone);
        textInputEditTextEmail = view.findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPhone = view.findViewById(R.id.textInputEditTextPhone);
        textInputLayoutProvince = view.findViewById(R.id.textInputLayoutProvince);
        autoCompleteTextViewProvince = view.findViewById(R.id.autoCompleteTextViewProvince);
        textInputLayoutDistrict = view.findViewById(R.id.textInputLayoutDistrict);
        autoCompleteTextViewDistrict = view.findViewById(R.id.autoCompleteTextViewDistrict);
        textInputLayoutWard = view.findViewById(R.id.textInputLayoutWard);
        autoCompleteTextViewWard = view.findViewById(R.id.autoCompleteTextViewWard);
        textInputLayoutStreet = view.findViewById(R.id.textInputLayoutStreet);
        textInputEditTextStreet = view.findViewById(R.id.textInputEditTextStreet);
        textInputLayoutNote = view.findViewById(R.id.textInputLayoutNote);
        textInputEditTextNote = view.findViewById(R.id.textInputEditTextNote);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up toolbar navigation (e.g., back button)
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // Go back to previous fragment/activity
            }
        });

        // Setup RecyclerView
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        // Use the cart items from arguments instead of generating dummy data


        cartItemAdapter = new CartItemAdapter(cartItems);
        recyclerViewCartItems.setAdapter(cartItemAdapter);

        updateOrderSummary();
        setupAddressSelection();


        buttonPlaceOrder.setOnClickListener(v -> {
            if (validateInputs()) {
                showOrderConfirmationDialog();
            } else {
                Snackbar.make(view, "Please fill in all required fields", Snackbar.LENGTH_SHORT).show();
            }
        });

    }


    private void setupAddressSelection() {
        // Show loading for provinces
        showLoading(true);

        // Get provinces from API
        AddressApiService addressApiService = AddressApiClient.getAddressService();
        addressApiService.getProvinces().enqueue(new Callback<List<ProvinceResponse>>() {
            @Override
            public void onResponse(Call<List<ProvinceResponse>> call, Response<List<ProvinceResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<ProvinceResponse> provinces = response.body();

                    // Create adapter for provinces
                    ArrayAdapter<ProvinceResponse> provinceAdapter = new ArrayAdapter<>(
                            requireContext(),
                            R.layout.dropdown_item,
                            provinces);

                    autoCompleteTextViewProvince.setAdapter(provinceAdapter);

                    // Set up district selection based on province
                    autoCompleteTextViewProvince.setOnItemClickListener((parent, view, position, id) -> {
                        ProvinceResponse selectedProvince = (ProvinceResponse) parent.getItemAtPosition(position);

                        // Clear district and ward selections
                        autoCompleteTextViewDistrict.setText("");
                        autoCompleteTextViewWard.setText("");

                        // Show loading for districts
                        showLoading(true);

                        // Get districts for selected province
                        addressApiService.getDistricts(selectedProvince.getCode())
                                .enqueue(new Callback<ProvinceResponse>() {
                                    @Override
                                    public void onResponse(Call<ProvinceResponse> call, Response<ProvinceResponse> response) {
                                        showLoading(false);

                                        if (response.isSuccessful() && response.body() != null) {
                                            List<DistrictResponse> districts = response.body().getDistricts();

                                            // Create adapter for districts
                                            ArrayAdapter<DistrictResponse> districtAdapter = new ArrayAdapter<>(
                                                    requireContext(),
                                                    R.layout.dropdown_item,
                                                    districts);

                                            autoCompleteTextViewDistrict.setAdapter(districtAdapter);
                                        } else {
                                            Snackbar.make(requireView(), "Failed to load districts", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ProvinceResponse> call, Throwable t) {
                                        showLoading(false);
                                        Snackbar.make(requireView(), "Network error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                    });

                    // Set up ward selection based on district
                    autoCompleteTextViewDistrict.setOnItemClickListener((parent, view, position, id) -> {
                        DistrictResponse selectedDistrict = (DistrictResponse) parent.getItemAtPosition(position);

                        // Clear ward selection
                        autoCompleteTextViewWard.setText("");

                        // Show loading for wards
                        showLoading(true);

                        // Get wards for selected district
                        addressApiService.getWards(selectedDistrict.getCode())
                                .enqueue(new Callback<DistrictResponse>() {
                                    @Override
                                    public void onResponse(Call<DistrictResponse> call, Response<DistrictResponse> response) {
                                        showLoading(false);

                                        if (response.isSuccessful() && response.body() != null) {
                                            List<WardResponse> wards = response.body().getWards();

                                            // Create adapter for wards
                                            ArrayAdapter<WardResponse> wardAdapter = new ArrayAdapter<>(
                                                    requireContext(),
                                                    R.layout.dropdown_item,
                                                    wards);

                                            autoCompleteTextViewWard.setAdapter(wardAdapter);
                                        } else {
                                            Snackbar.make(requireView(), "Failed to load wards", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<DistrictResponse> call, Throwable t) {
                                        showLoading(false);
                                        Snackbar.make(requireView(), "Network error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                    });
                } else {
                    Snackbar.make(requireView(), "Failed to load provinces", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProvinceResponse>> call, Throwable t) {
                showLoading(false);
                Snackbar.make(requireView(), "Network error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        View loadingOverlay = getView().findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }


    /**
     * Calculates and updates the order summary (subtotal, shipping, total).
     */
    private void updateOrderSummary() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemResponse item : cartItems) {
            subtotal = subtotal.add(item.getTotalPrice());
        }

        BigDecimal total = subtotal.add(SHIPPING_COST);

        textViewSubtotalValue.setText(currencyFormatter.format(subtotal));
        textViewShippingValue.setText(currencyFormatter.format(SHIPPING_COST));
        textViewTotalValue.setText(currencyFormatter.format(total));
    }

    /**
     * Sets up the AutoCompleteTextViews for Province, District, and Ward selection.
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate recipient name
        if (textInputEditTextRecipientName.getText().toString().trim().isEmpty()) {
            textInputLayoutRecipientName.setError("Recipient name is required");
            isValid = false;
        } else {
            textInputLayoutRecipientName.setError(null);
        }

        // Validate phone number
        String phoneNumber = textInputEditTextPhone.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            textInputLayoutPhone.setError("Phone number is required");
            isValid = false;
        } else if (phoneNumber.length() < 10 || phoneNumber.length() > 15) {
            textInputLayoutPhone.setError("Phone number must be between 10 and 15 digits");
            isValid = false;
        } else {
            textInputLayoutPhone.setError(null);
        }

        // Validate email
        String email = textInputEditTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            TextInputLayout textInputLayoutEmail = (TextInputLayout) textInputEditTextEmail.getParent().getParent();
            textInputLayoutEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            TextInputLayout textInputLayoutEmail = (TextInputLayout) textInputEditTextEmail.getParent().getParent();
            textInputLayoutEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            TextInputLayout textInputLayoutEmail = (TextInputLayout) textInputEditTextEmail.getParent().getParent();
            textInputLayoutEmail.setError(null);
        }

        // Validate province/city
        if (autoCompleteTextViewProvince.getText().toString().trim().isEmpty()) {
            textInputLayoutProvince.setError("City is required");
            isValid = false;
        } else {
            textInputLayoutProvince.setError(null);
        }

        // Validate district
        if (autoCompleteTextViewDistrict.getText().toString().trim().isEmpty()) {
            textInputLayoutDistrict.setError("District is required");
            isValid = false;
        } else {
            textInputLayoutDistrict.setError(null);
        }

        // Validate ward
        if (autoCompleteTextViewWard.getText().toString().trim().isEmpty()) {
            textInputLayoutWard.setError("Ward is required");
            isValid = false;
        } else {
            textInputLayoutWard.setError(null);
        }

        // Validate street address
        String street = textInputEditTextStreet.getText().toString().trim();
        if (street.isEmpty()) {
            textInputLayoutStreet.setError("Street address is required");
            isValid = false;
        } else if (street.length() < 5) {
            textInputLayoutStreet.setError("Street address must be at least 5 characters");
            isValid = false;
        } else {
            textInputLayoutStreet.setError(null);
        }

        return isValid;
    }

    private void showOrderConfirmationDialog() {
        // Calculate the total for confirmation message
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemResponse item : cartItems) {
            subtotal = subtotal.add(item.getTotalPrice());
        }
        BigDecimal total = subtotal.add(SHIPPING_COST);
        String formattedTotal = currencyFormatter.format(total);

        // Create an AlertDialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Order")
                .setMessage("Are you sure you want to place this order for " + formattedTotal + "?")
                .setPositiveButton("Yes, Place Order", (dialog, which) -> {
                    // User confirmed, proceed with order processing
                    processOrder();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled the dialog
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void processOrder() {
        // Get values from input fields
        String recipientName = textInputEditTextRecipientName.getText().toString().trim();
        String phoneNumber = textInputEditTextPhone.getText().toString().trim();
        String street = textInputEditTextStreet.getText().toString().trim();
        String note = textInputEditTextNote.getText().toString().trim();
        String city = autoCompleteTextViewProvince.getText().toString().trim();
        String district = autoCompleteTextViewDistrict.getText().toString().trim();
        String ward = autoCompleteTextViewWard.getText().toString().trim();
        String email = textInputEditTextEmail.getText().toString().trim();

        RadioGroup radioGroupPaymentMethod = getView().findViewById(R.id.radioGroupPaymentMethod);
        int selectedPaymentMethodId = radioGroupPaymentMethod.getCheckedRadioButtonId();
        byte paymentMethod = (byte) (selectedPaymentMethodId == R.id.radioButtonPayOS ? 0 : 1);
        // Create checkout request
        CheckOutRequest checkoutRequest = new CheckOutRequest();
        checkoutRequest.setRecipientName(recipientName);
        checkoutRequest.setPhoneNumber(phoneNumber);
        checkoutRequest.setNote(note);
        checkoutRequest.setPaymentMethod(paymentMethod); // Use 2 for PayOS payment method
        checkoutRequest.setCity(city);
        checkoutRequest.setWard(ward);
        checkoutRequest.setStreet(street);
        checkoutRequest.setDistrict(district);
        checkoutRequest.setEmail(email);

        // Extract cart item IDs
        List<Integer> cartItemIds = new ArrayList<>();
        for (CartItemResponse item : cartItems) {
            cartItemIds.add(item.getCartItemId());
        }
        checkoutRequest.setCartItemIds(cartItemIds);

        // Show loading indicator
        View loadingOverlay = getView().findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }

        // Log request data to help debug
        Log.d("checkout", "Request: " + checkoutRequest.toString());

        CheckOutService checkOutService = ApiClient.getAuthenticatedClient(requireContext()).create(CheckOutService.class);

        // Make API call to place order
        checkOutService.placeOrder(checkoutRequest)
                .enqueue(new Callback<ApiResponse<CheckOutResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CheckOutResponse>> call, Response<ApiResponse<CheckOutResponse>> response) {
                        // Hide loading indicator
                        if (loadingOverlay != null) {
                            loadingOverlay.setVisibility(View.GONE);
                        }

                        Log.d("checkout", "Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CheckOutResponse> apiResponse = response.body();

                            Log.d("checkout", "Success: " + apiResponse.isSuccess());
                            Log.d("checkout", "Message: " + apiResponse.getMessage());

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                // Check if payment method is COD (value 1)
                                if (paymentMethod == 1) {
                                    // Create and navigate to OrderConfirmationFragment directly for COD
                                    OrderConfirmationFragment confirmationFragment = new OrderConfirmationFragment();

                                    // Pass all necessary data to the fragment
                                    Bundle args = new Bundle();
                                    args.putInt("order_id", apiResponse.getData().getOrderId());
                                    args.putInt("payment_method", OrderConfirmationFragment.PAYMENT_COD);
                                    args.putBoolean("is_payment_completed", true); // For COD, mark as completed
                                    args.putString("order_date", new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));

                                    // Add address info
                                    args.putString("recipient_name", recipientName);
                                    args.putString("phone_number", phoneNumber);
                                    args.putString("city", city);
                                    args.putString("district", district);
                                    args.putString("ward", ward);
                                    args.putString("street", street);

                                    // Calculate total amount
                                    BigDecimal subtotal = BigDecimal.ZERO;
                                    for (CartItemResponse item : cartItems) {
                                        subtotal = subtotal.add(item.getTotalPrice());
                                    }
                                    BigDecimal total = subtotal.add(SHIPPING_COST);
                                    args.putDouble("total_amount", total.doubleValue());

                                    confirmationFragment.setArguments(args);

                                    // Replace fragment
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).replaceFragment(confirmationFragment);
                                    }
                                } else {
                                    // For online payment (PayOS), proceed with WebView
                                    String paymentUrl = apiResponse.getData().getRedirectURl();
                                    Log.d("checkout", "Payment URL: " + paymentUrl);

                                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                        // Save payment in progress flag
                                        saveOrderInProgress();

                                        // Open payment in WebView
                                        openPaymentWebView(paymentUrl);
                                    } else {
                                        Snackbar.make(requireView(),
                                                "Payment URL not found", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                Snackbar.make(requireView(),
                                        "Error: " + apiResponse.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ?
                                        response.errorBody().string() : "Unknown error";

                                Log.e("checkout", "Error body: " + errorBody);

                                Snackbar.make(requireView(),
                                        "Failed to place order: " + errorBody, Snackbar.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.e("checkout", "Error parsing error body", e);

                                Snackbar.make(requireView(),
                                        "Failed to place order", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CheckOutResponse>> call, Throwable t) {
                        // Hide loading indicator
                        if (loadingOverlay != null) {
                            loadingOverlay.setVisibility(View.GONE);
                        }

                        Log.e("checkout", "API call failed", t);

                        // Show error message
                        Snackbar.make(requireView(),
                                "Network error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
    private void saveOrderInProgress() {
        // Save information that user has a payment in progress
        SharedPreferences prefs = requireContext().getSharedPreferences("payment_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("payment_in_progress", true);
        editor.apply();
    }

    private void openPaymentWebView(String paymentUrl) {
        // Create Intent for the PaymentActivity
        Intent paymentIntent = new Intent(requireContext(), PaymentActivity.class);
        paymentIntent.putExtra("payment_url", paymentUrl);
        startActivity(paymentIntent);
    }
}