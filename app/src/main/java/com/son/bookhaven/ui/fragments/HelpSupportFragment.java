package com.son.bookhaven.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.son.bookhaven.R;

public class HelpSupportFragment extends Fragment {
    private static final String PHONE_NUMBER = "1900123456";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button handling
        Button buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null && isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new ProfileFragment())
                        .commitAllowingStateLoss();
            }
        });

        // Call now button
        MaterialButton buttonCallNow = view.findViewById(R.id.buttonCallNow);
        buttonCallNow.setOnClickListener(v -> makePhoneCall());

        // Direct call button in contact section
        MaterialButton buttonCallDirect = view.findViewById(R.id.buttonCallDirect);
        buttonCallDirect.setOnClickListener(v -> makePhoneCall());

        // Copy email button
        MaterialButton buttonCopyEmail = view.findViewById(R.id.buttonCopyEmail);
        buttonCopyEmail.setOnClickListener(v -> copyEmailToClipboard());
    }

    private void makePhoneCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyEmailToClipboard() {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    requireActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Email", "support@bookhaven.vn");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Đã sao chép email vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể sao chép email", Toast.LENGTH_SHORT).show();
        }
    }
}