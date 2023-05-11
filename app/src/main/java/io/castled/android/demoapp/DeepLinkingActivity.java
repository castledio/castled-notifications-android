package io.castled.android.demoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.MessageFormat;

public class DeepLinkingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_linking);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(DeepLinkingActivity.class.getSimpleName());

        loadParamsToLayout();
    }

    private void loadParamsToLayout() {

        try {

            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            TextView paramsTv = findViewById(R.id.params_tv);
            paramsTv.setText(MessageFormat.format("Action: {0}\n\nHost: {1}\n\nData: {2}",
                    action, (data.getScheme() + "://" +  data.getHost()), data.getEncodedQuery()));
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
}
