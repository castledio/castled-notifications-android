package io.castled.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.MessageFormat;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(NavigationActivity.class.getSimpleName());

        loadParamsToLayout();
    }

    private void loadParamsToLayout() {

        try {

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();

            TextView paramsTv = findViewById(R.id.params_tv);
            paramsTv.setText(MessageFormat.format("Bundle: {0}", bundle2string(bundle)));
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static String bundle2string(Bundle bundle) {

        if (bundle == null) {
            return null;
        }
        String string = "{";
        for (String key : bundle.keySet()) {
            string += "\t\t" + key + ": " + bundle.get(key) + ",\n";
        }
        string += "\t}";
        return string;
    }
}
