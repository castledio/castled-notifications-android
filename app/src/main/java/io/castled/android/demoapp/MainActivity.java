package io.castled.android.demoapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import io.castled.android.demoapp.databinding.ActivityMainBinding;
import io.castled.android.demoapp.databinding.ContentMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ContentMainBinding bindingContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        // bindingContent = ContentMainBinding.inflate(getLayoutInflater());


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {

            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
        createNotificationChannels();
      /*  CastledNotifications.getInboxUnreadCount(count -> {
            // Handle the result in Java
            System.out.println("unread Inbox Count: " + count);
            return null;
        });

        CastledNotifications.getInboxItems((List<CastledInboxItem> itemsList) -> {

            for (CastledInboxItem item : itemsList) {
                System.out.println("Item ID: " + item);
                System.out.println("Item Name: " + item.getTitle());
            }
            CastledNotifications.logInboxItemsRead(itemsList);
            CastledInboxItem itemToDelete = itemsList.get(itemsList.size() - 1);
            CastledNotifications.logInboxItemClicked(itemToDelete, "");
            CastledNotifications.deleteInboxItem(itemToDelete, new kotlin.jvm.functions.Function2<Boolean, String, Unit>() {
                @Override
                public Unit invoke(Boolean success, String message) {
                    // Handle the success and message here
                    if (success) {
                        System.out.println("Item deleted: " + message);
                    } else {
                        System.out.println("Error: " + message);
                    }
                    return null;
                }
            });


            return null;
        });*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId1 = "castled_demo_id";
            String channelName1 = "Castled Demo App Channel 1";
            String channelDescription1 = "This is Castled Demo App Channel ";
            createNotificationChannel(channelId1, channelName1, channelDescription1);


            // Add more channels as needed
        }
    }

    private void createNotificationChannel(String channelId, String channelName, String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelDescription);

            // Optionally configure additional channel settings here

            notificationManager.createNotificationChannel(channel);
        }
    }
}