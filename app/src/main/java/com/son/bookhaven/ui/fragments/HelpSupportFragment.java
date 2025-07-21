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

    }


}