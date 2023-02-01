package io.castled.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import io.castled.inappNotifications.trigger.APITest;
import io.castled.notifications.databinding.FragmentFirstBinding;
import io.castled.inappNotifications.trigger.PopupHeader;
import io.castled.inappNotifications.trigger.PopupMessage;
import io.castled.inappNotifications.trigger.PopupPrimaryButton;
import io.castled.inappNotifications.trigger.PopupSecondaryButton;
import io.castled.inappNotifications.trigger.TriggerPopup;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));

        binding.btnLaunchPopup.setOnClickListener(btnLaunchPopupView -> TriggerPopup.Companion.showDialog(
                requireContext(),
                "#f8ffbd",
                new PopupHeader("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                new PopupMessage("30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
//                    "http://i.imgur.com/DvpvklR.png",
//                    "https://www.pakainfo.com/wp-content/uploads/2021/09/image-url-for-testing.jpg",
                "https://www.infogrepper.com/wp-content/uploads/2022/10/image-url-for-testing.png",
                "https://www.apple.com/",
                new PopupPrimaryButton("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                new PopupSecondaryButton("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
        ));

        binding.btnLaunchFullscreenPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {
//            TriggerPopup.Companion.showFullScreenDialog(requireContext(), requireActivity());


            TriggerPopup.Companion.showFullscreenDialog(
                    requireContext(),
                    "#f8ffbd",
                    new PopupHeader("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                    new PopupMessage("Full Screen \n30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
//                    "http://i.imgur.com/DvpvklR.png",
//                    "https://www.pakainfo.com/wp-content/uploads/2021/09/image-url-for-testing.jpg",
                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://www.apple.com/",
                    new PopupPrimaryButton("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                    new PopupSecondaryButton("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
            );

        });

        binding.btnApiTest.setOnClickListener(btnLaunchFullscreenPopupView -> {
            APITest apiTest = new APITest();
            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());
            apiTest.fetchNotification(requireContext());
        });

        binding.btnDbNotification.setOnClickListener(btnLaunchFullscreenPopupView -> {
            APITest apiTest = new APITest();
            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}