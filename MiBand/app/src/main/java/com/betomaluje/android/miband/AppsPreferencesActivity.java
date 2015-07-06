package com.betomaluje.android.miband;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.betomaluje.android.miband.adapters.ApplicationsAdapter;
import com.betomaluje.android.miband.core.colorpicker.ColorPickerDialog;
import com.betomaluje.android.miband.models.App;
import com.betomaluje.android.miband.sqlite.AppsSQLite;

import java.util.List;

/**
 * Created by betomaluje on 7/6/15.
 */
public class AppsPreferencesActivity extends ActionBarActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        final ListView listView_apps = (ListView) findViewById(R.id.listView_apps);

        if (!AppsSQLite.getInstance(AppsPreferencesActivity.this).doesTableExists()) {
            fillApps();
        }

        final ApplicationsAdapter adapter = new ApplicationsAdapter(AppsPreferencesActivity.this,
                AppsSQLite.getInstance(AppsPreferencesActivity.this).getApps());

        listView_apps.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView_apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final App app = (App) listView_apps.getItemAtPosition(position);

                new ColorPickerDialog(AppsPreferencesActivity.this, app.getColor(), new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int rgb) {

                        Log.i(TAG, "before app: " + app.getName() + " to " + app.isNotify());
                        app.setColor(rgb);
                        app.setNotify(app.isNotify() ? 0 : 1);
                        Log.i(TAG, "changed app: " + app.getName() + " to " + app.isNotify());

                        AppsSQLite.getInstance(AppsPreferencesActivity.this).updateApp(app);

                        adapter.notifyDataSetChanged();
                    }
                }).show();
            }
        });
    }

    private void fillApps() {
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        AppsSQLite appsSQLite = AppsSQLite.getInstance(AppsPreferencesActivity.this);

        for (ApplicationInfo packageInfo : packages) {
            String name = pm.getApplicationLabel(packageInfo).toString();
            appsSQLite.saveApp(name, packageInfo.packageName, -524538, false, 500);
        }
    }
}
