package io.castled.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import io.castled.inappNotifications.trigger.NotificationTrigger;
import io.castled.inappNotifications.trigger.TestTriggerNotification;
import io.castled.notifications.databinding.FragmentFirstBinding;
import io.castled.inappNotifications.trigger.PopupHeader;
import io.castled.inappNotifications.trigger.PopupMessage;
import io.castled.inappNotifications.trigger.PopupPrimaryButton;
import io.castled.inappNotifications.trigger.PopupSecondaryButton;

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

        binding.btnLaunchPopup.setOnClickListener(btnLaunchPopupView -> TestTriggerNotification.getInstance().showDialog(
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


            TestTriggerNotification.getInstance().showFullscreenDialog(
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

        binding.btnLaunchSlideupPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerNotification.getInstance().showSlideUpDialog(
                    requireContext(),
                    "#ff99ff",
                    new PopupMessage("Slide Up \n30% offer on Electronics, Cloths, Sports and other categories.", "#ffffff", 12, "#039ADC"),
                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://www.apple.com/"
            );

        });

        binding.btnApiTest.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerNotification.getInstance().fetchAndSaveTriggerNotification(requireContext());
//            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());
        });

        binding.btnDbNotification.setOnClickListener(btnLaunchFullscreenPopupView -> {
//            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());

            NotificationTrigger.getInstance().startObservingTriggerNotification(requireContext());

        });

    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationTrigger.getInstance().startObservingTriggerNotification(requireContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}