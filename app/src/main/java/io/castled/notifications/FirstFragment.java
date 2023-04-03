package io.castled.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import io.castled.CastledNotifications;
import io.castled.inAppTriggerEvents.event.TestTriggerEvents;
import io.castled.inAppTriggerEvents.trigger.PopupHeader;
import io.castled.inAppTriggerEvents.trigger.PopupMessage;
import io.castled.inAppTriggerEvents.trigger.PopupPrimaryButton;
import io.castled.inAppTriggerEvents.trigger.PopupSecondaryButton;
import io.castled.notifications.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private static final String TAG = "FirstFragment";
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

        binding.btnLaunchPopup.setOnClickListener(btnLaunchPopupView -> TestTriggerEvents.getInstance().showDialog(
                requireContext(),
                "#f8ffbd",
                new PopupHeader("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                new PopupMessage("30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
                "https://img.uswitch.com/qhi9fkhtpbo3/hSSkIfF0OsQQGuiCCm0EQ/6c1a9b54de813e0a71a85edb400d58d8/rsz_1android.jpg",
//                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Google_Images_2015_logo.svg/800px-Google_Images_2015_logo.svg.png",
//                "https://www.infogrepper.com/wp-content/uploads/2022/10/image-url-for-testing.png",
                "https://www.apple.com/",
                new PopupPrimaryButton("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                new PopupSecondaryButton("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
        ));

        binding.btnLaunchFullscreenPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {


            TestTriggerEvents.getInstance().showFullscreenDialog(
                    requireContext(),
                    "#f8ffbd",
                    new PopupHeader("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                    new PopupMessage("Full Screen \n30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
//                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://img.uswitch.com/qhi9fkhtpbo3/hSSkIfF0OsQQGuiCCm0EQ/6c1a9b54de813e0a71a85edb400d58d8/rsz_1android.jpg",
//                    "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Google_Images_2015_logo.svg/800px-Google_Images_2015_logo.svg.png",
                    "https://www.apple.com/",
                    new PopupPrimaryButton("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                    new PopupSecondaryButton("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
            );

        });

        binding.btnLaunchSlideupPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance().showSlideUpDialog(
                    requireContext(),
                    "#ff99ff",
                    new PopupMessage("Slide Up \n30% offer on Electronics, Cloths, Sports and other categories.", "#ffffff", 12, "#039ADC"),
                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://www.apple.com/"
            );

        });

        binding.btnCloudEvent.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance().fetchAndSaveTriggerEvents(requireContext());
//            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());
        });

        binding.btnDbEvent.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance().findAndLaunchTriggerEvent(requireContext());

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        CastledNotifications.logAppPageViewEvent(requireContext(), "FirstFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}