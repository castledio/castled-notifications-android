package io.castled.android.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import io.castled.android.demoapp.databinding.FragmentFirstBinding;
import io.castled.android.notifications.CastledNotifications;
import io.castled.android.notifications.inbox.model.CastledInboxDisplayConfig;
import io.castled.android.notifications.inbox.model.CastledInboxItem;

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
        CastledNotifications.getInboxItems((List<CastledInboxItem> items) -> {
            // Handle the fetched items here
            for (CastledInboxItem item : items) {
                // Your code to handle each item

            }
            return null;
        });

        binding.buttonFirst.setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
        binding.btnInbox.setOnClickListener(btnInbox -> {
            CastledInboxDisplayConfig styleConfig = new CastledInboxDisplayConfig();
            styleConfig.setEmptyMessageViewText("There are no inbox items");
            styleConfig.setEmptyMessageViewTextColor("#195190");
            styleConfig.setInboxViewBackgroundColor("#A2A2A1");
            styleConfig.setNavigationBarBackgroundColor("#0000FF");
            styleConfig.setNavigationBarTitleColor("#FFFFFF");
            styleConfig.setNavigationBarTitle("Castled Inbox");
            styleConfig.setHideNavigationBar(false);

//            styleConfig.setHideBackButton(true);
//            int resId = getContext().getResources().getIdentifier("back_btn1", "drawable", getContext().getPackageName());
//            styleConfig.setBackButtonResourceId(resId);


            styleConfig.setShowCategoriesTab(true);
            styleConfig.setTabBarDefaultBackgroundColor("#FFFFFF");
            styleConfig.setTabBarSelectedBackgroundColor("#FFFFFF");
            styleConfig.setTabBarDefaultTextColor("#00539C");
            styleConfig.setTabBarSelectedTextColor("#00539C");
            styleConfig.setTabBarIndicatorBackgroundColor("#0000FF");
            CastledNotifications.showAppInbox(view.getContext(), styleConfig);
        });
        binding.btnLogout.setOnClickListener(btnLogout -> {
            CastledNotifications.logout();
        });

        
        /*

        binding.btnLaunchPopup.setOnClickListener(btnLaunchPopupView -> TestTriggerEvents.getInstance(requireContext()).showDialog(
                requireContext(),
                "#f8ffbd",
                new HeaderViewParams("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                new MessageViewParams("30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
                "https://img.uswitch.com/qhi9fkhtpbo3/hSSkIfF0OsQQGuiCCm0EQ/6c1a9b54de813e0a71a85edb400d58d8/rsz_1android.jpg",
//                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Google_Images_2015_logo.svg/800px-Google_Images_2015_logo.svg.png",
//                "https://www.infogrepper.com/wp-content/uploads/2022/10/image-url-for-testing.png",
                "https://www.apple.com/",
                new ButtonViewParams("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                new ButtonViewParams("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
        ));

        binding.btnLaunchFullscreenPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {


            TestTriggerEvents.getInstance(requireContext()).showFullscreenDialog(
                    requireContext(),
                    "#f8ffbd",
                    new HeaderViewParams("Summer sale is Back!", "#FFFFFF" , 18, "#E74C3C"),
                    new MessageViewParams("Full Screen \n30% offer on Electronics, Cloths, Sports and other categories.","#FFFFFF" , 12, "#039ADC"),
//                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://img.uswitch.com/qhi9fkhtpbo3/hSSkIfF0OsQQGuiCCm0EQ/6c1a9b54de813e0a71a85edb400d58d8/rsz_1android.jpg",
//                    "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Google_Images_2015_logo.svg/800px-Google_Images_2015_logo.svg.png",
                    "https://www.apple.com/",
                    new ButtonViewParams("Skip Now", "#000000", "#ffffff", "#000000", "https://www.google.com/"),
                    new ButtonViewParams("Start Shopping", "#ffe0da", "#FF6D07", "#5cdb5c","https://stackoverflow.com/")
            );

        });

        binding.btnLaunchSlideupPopup.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance(requireContext()).showSlideUpDialog(
                    requireContext(),
                    "#ff99ff",
                    new MessageViewParams("Slide Up \n30% offer on Electronics, Cloths, Sports and other categories.", "#ffffff", 12, "#039ADC"),
                    "https://cdn.castled.io/logo/castled_multi_color_logo_only.png",
                    "https://www.apple.com/"
            );

        });

        binding.btnCloudEvent.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance(requireContext()).fetchAndSaveTriggerEvents(requireContext());
//            apiTest.observeDatabaseNotification(requireContext(), getViewLifecycleOwner());
        });

        binding.btnDbEvent.setOnClickListener(btnLaunchFullscreenPopupView -> {
            TestTriggerEvents.getInstance(requireContext()).findAndLaunchTriggerEvent(requireContext());

        });
        
         */
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}