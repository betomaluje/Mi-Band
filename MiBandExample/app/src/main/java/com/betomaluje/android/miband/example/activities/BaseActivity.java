package com.betomaluje.android.miband.example.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.betomaluje.android.miband.example.R;

/**
 * Created by betomaluje on 7/10/15.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private int toolbarBaseColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(getDisplayHomeAsUpEnabled());
            } catch (NullPointerException e) {

            }

            toolbarBaseColor = toolbar.getSolidColor();
        }
    }

    protected abstract int getLayoutResource();

    protected abstract boolean getDisplayHomeAsUpEnabled();

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    protected void resetToolbar() {
        toolbar.setBackgroundColor(toolbarBaseColor);
    }
}
