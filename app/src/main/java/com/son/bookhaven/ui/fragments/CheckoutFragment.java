package com.son.bookhaven.ui.fragments; // Adjust package as necessary

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
import com.son.bookhaven.R;
import com.son.bookhaven.data.adapters.CartItemAdapter; // Assuming this is your adapter package
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.BookImage; // Ensure this is imported if used
import com.son.bookhaven.data.model.CartItem;
import com.son.bookhaven.data.model.LanguageCode;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CheckoutFragment extends Fragment {

    private RecyclerView recyclerViewCartItems;
    private CartItemAdapter cartItemAdapter;
    private List<CartItem> currentCartItems;

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
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // Or your desired locale
        //vnAddressAPI = new VNAddressAPI(); // Initialize the mock API
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
        currentCartItems = generateDummyCartItems(); // Replace with actual data fetch
        cartItemAdapter = new CartItemAdapter(currentCartItems);
        recyclerViewCartItems.setAdapter(cartItemAdapter);

        // Calculate and display summary
        updateOrderSummary();

        // Setup Address Pickers
        setupAddressPickers();

        /*
        // Set up Place Order button click listener
        buttonPlaceOrder.setOnClickListener(v -> {
            // Validate inputs
            if (validateInputs()) {
                // Collect address data
                String recipientName = textInputEditTextRecipientName.getText().toString().trim();
                String phone = textInputEditTextPhone.getText().toString().trim();
                String street = textInputEditTextStreet.getText().toString().trim();
                String note = textInputEditTextNote.getText().toString().trim();

                //String provinceName = selectedProvince != null ? selectedProvince.getName() : "";
                //String districtName = selectedDistrict != null ? selectedDistrict.getName() : "";
                //String wardName = selectedWard != null ? selectedWard.getName() : "";

                //DeliveryAddress deliveryAddress = new DeliveryAddress(
                //        recipientName, phone, provinceName, districtName, wardName, street, note
                //);

                // For demonstration, show a Snackbar with the collected address
                Snackbar.make(view, "Order Placed! Delivery to: \n" + deliveryAddress.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Dismiss", v1 -> {})
                        .show();
                // In a real app, you would create an Order object and send it to your backend
            } else {
                Snackbar.make(view, "Please fill in all required address fields.", Snackbar.LENGTH_SHORT).show();
            }
        });
        */
    }

    /**
     * Calculates and updates the order summary (subtotal, shipping, total).
     */
    private void updateOrderSummary() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : currentCartItems) {
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
    private void setupAddressPickers() {
        // Provinces
        //List<Province> provinces = vnAddressAPI.getProvinces();
        //ArrayAdapter<Province> provinceAdapter = new ArrayAdapter<>(
        //        requireContext(),
        //        android.R.layout.simple_dropdown_item_1line, // Simple layout for dropdown
        //        provinces
        //);
        //autoCompleteTextViewProvince.setAdapter(provinceAdapter);

        //autoCompleteTextViewProvince.setOnItemClickListener((parent, view, position, id) -> {
        //    selectedProvince = (Province) parent.getItemAtPosition(position);
        //    // Clear and reset dependent fields
        //    selectedDistrict = null;
        //    selectedWard = null;
        //    autoCompleteTextViewDistrict.setText("");
        //    autoCompleteTextViewWard.setText("");
        //    textInputLayoutDistrict.setEnabled(true);
        //    textInputLayoutWard.setEnabled(false); // Disable ward until district is chosen
        //    populateDistricts(selectedProvince.getCode());
        //});

        // Districts (initially disabled)
        //textInputLayoutDistrict.setEnabled(false);
        //autoCompleteTextViewDistrict.setOnItemClickListener((parent, view, position, id) -> {
        //    selectedDistrict = (District) parent.getItemAtPosition(position);
        //    // Clear and reset dependent fields
        //    selectedWard = null;
        //    autoCompleteTextViewWard.setText("");
        //    textInputLayoutWard.setEnabled(true);
        //    populateWards(selectedDistrict.getCode());
        //});

        // Wards (initially disabled)
        //textInputLayoutWard.setEnabled(false);
        //autoCompleteTextViewWard.setOnItemClickListener((parent, view, position, id) -> {
        //    selectedWard = (Ward) parent.getItemAtPosition(position);
        //});
    }

    /**
     * Populates the district AutoCompleteTextView based on the selected province.
     * @param provinceCode The code of the selected province.
     */
    //private void populateDistricts(String provinceCode) {
    //    List<District> districts = vnAddressAPI.getDistrictsByProvince(provinceCode);
    //    ArrayAdapter<District> districtAdapter = new ArrayAdapter<>(
    //            requireContext(),
    //            android.R.layout.simple_dropdown_item_1line,
    //            districts
    //    );
    //    autoCompleteTextViewDistrict.setAdapter(districtAdapter);
    //    // Show dropdown immediately after population
    //    autoCompleteTextViewDistrict.showDropDown();
    //}

    /**
     * Populates the ward AutoCompleteTextView based on the selected district.
     * @param districtCode The code of the selected district.
     */
    //private void populateWards(String districtCode) {
    //    List<Ward> wards = vnAddressAPI.getWardsByDistrict(districtCode);
    //    ArrayAdapter<Ward> wardAdapter = new ArrayAdapter<>(
    //            requireContext(),
    //            android.R.layout.simple_dropdown_item_1line,
    //            wards
    //    );
    //    autoCompleteTextViewWard.setAdapter(wardAdapter);
    //    // Show dropdown immediately after population
    //    autoCompleteTextViewWard.showDropDown();
    //}

    /**
     * Validates that all required input fields for delivery address are filled.
     * @return true if all required fields are valid, false otherwise.
     */

    /*
    private boolean validateInputs() {
        boolean isValid = true;

        if (textInputEditTextRecipientName.getText().toString().trim().isEmpty()) {
            textInputLayoutRecipientName.setError("Recipient name is required.");
            isValid = false;
        } else {
            textInputLayoutRecipientName.setError(null);
        }

        if (textInputEditTextPhone.getText().toString().trim().isEmpty()) {
            textInputLayoutPhone.setError("Phone number is required.");
            isValid = false;
        } else {
            textInputLayoutPhone.setError(null);
        }

        if (selectedProvince == null) {
            textInputLayoutProvince.setError("Province/City is required.");
            isValid = false;
        } else {
            textInputLayoutProvince.setError(null);
        }

        if (selectedDistrict == null) {
            textInputLayoutDistrict.setError("District is required.");
            isValid = false;
        } else {
            textInputLayoutDistrict.setError(null);
        }

        if (selectedWard == null) {
            textInputLayoutWard.setError("Ward is required.");
            isValid = false;
        } else {
            textInputLayoutWard.setError(null);
        }

        if (textInputEditTextStreet.getText().toString().trim().isEmpty()) {
            textInputLayoutStreet.setError("Street address is required.");
            isValid = false;
        } else {
            textInputLayoutStreet.setError(null);
        }

        return isValid;
    }
    */

    /**
     * Generates dummy cart items for demonstration purposes.
     * In a real application, this data would come from your backend or local storage.
     * @return A list of dummy CartItem objects.
     */
    private List<CartItem> generateDummyCartItems() {
        List<CartItem> dummyItems = new ArrayList<>();

        // Book 1: The Silent Echo
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setTitle("The Silent Echo");
        book1.setPublisherId(101);
        book1.setCategoryId(1);
        book1.setPublicationYear(2023);
        book1.setPrice(new BigDecimal("19.99"));
        book1.setIsbn("978-0-123456-78-9");
        book1.setLanguage(LanguageCode.English);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book1.setCreatedAt(LocalDateTime.now().minusDays(30));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book1.setUpdatedAt(LocalDateTime.now().minusDays(5));
        }

        // Add authors for book1
        Set<Author> authors1 = new HashSet<>();
        Author author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("Sarah Johnson");
        authors1.add(author1);
        book1.setAuthors(authors1);

        List<BookImage> bookImages1 = new ArrayList<>();
        BookImage bookImage1 = new BookImage();
        bookImage1.setBookImageId(1);
        bookImage1.setImageUrl("https://picsum.photos/200/300?random=1"); // Placeholder image
        bookImages1.add(bookImage1);
        book1.setBookImages(bookImages1);


        // Book 2: Digital Dreams
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setTitle("Digital Dreams");
        book2.setPublisherId(102);
        book2.setCategoryId(2);
        book2.setPublicationYear(2024);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setIsbn("978-0-234567-89-0");
        book2.setLanguage(LanguageCode.English);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book2.setCreatedAt(LocalDateTime.now().minusDays(25));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book2.setUpdatedAt(LocalDateTime.now().minusDays(3));
        }

        // Add authors for book2
        Set<Author> authors2 = new HashSet<>();
        Author author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Alex Chen");
        authors2.add(author2);
        book2.setAuthors(authors2);
        // Assuming book2 also uses bookImages1 for simplicity or define new ones
        book2.setBookImages(bookImages1);


        // Book 3: Ocean's Mystery
        Book book3 = new Book();
        book3.setBookId(3);
        book3.setTitle("Ocean's Mystery");
        book3.setPublisherId(103);
        book3.setCategoryId(3);
        book3.setPublicationYear(2023);
        book3.setPrice(new BigDecimal("21.99"));
        book3.setIsbn("978-0-345678-90-1");
        book3.setLanguage(LanguageCode.English);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book3.setCreatedAt(LocalDateTime.now().minusDays(20));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book3.setUpdatedAt(LocalDateTime.now().minusDays(2));
        }

        // Add authors for book3
        Set<Author> authors3 = new HashSet<>();
        Author author3 = new Author();
        author3.setAuthorId(3);
        author3.setAuthorName("Maria Rodriguez");
        authors3.add(author3);
        book3.setAuthors(authors3);
        book3.setBookImages(bookImages1);

        // Book 4: City Lights
        Book book4 = new Book();
        book4.setBookId(4);
        book4.setTitle("City Lights");
        book4.setPublisherId(104);
        book4.setCategoryId(4);
        book4.setPublicationYear(2024);
        book4.setPrice(new BigDecimal("18.99"));
        book4.setIsbn("978-0-456789-01-2");
        book4.setLanguage(LanguageCode.English);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book4.setCreatedAt(LocalDateTime.now().minusDays(15));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book4.setUpdatedAt(LocalDateTime.now().minusDays(1));
        }

        // Add authors for book4
        Set<Author> authors4 = new HashSet<>();
        Author author4 = new Author();
        author4.setAuthorId(4);
        author4.setAuthorName("David Kim");
        authors4.add(author4);
        book4.setAuthors(authors4);
        book4.setBookImages(bookImages1);

        // Dummy CartItems
        CartItem cartItem1 = new CartItem(book1, 2, true);
        cartItem1.setCartItemId(101);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cartItem1.setCreatedAt(LocalDateTime.now());
        }

        CartItem cartItem2 = new CartItem(book2, 1, true);
        cartItem2.setCartItemId(102);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cartItem2.setCreatedAt(LocalDateTime.now());
        }

        CartItem cartItem3 = new CartItem(book3, 3, true);
        cartItem3.setCartItemId(103);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cartItem3.setCreatedAt(LocalDateTime.now());
        }

        CartItem cartItem4 = new CartItem(book4, 1, true);
        cartItem4.setCartItemId(104);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cartItem4.setCreatedAt(LocalDateTime.now());
        }

        dummyItems.add(cartItem1);
        dummyItems.add(cartItem2);
        dummyItems.add(cartItem3);
        dummyItems.add(cartItem4);

        return dummyItems;
    }
}
