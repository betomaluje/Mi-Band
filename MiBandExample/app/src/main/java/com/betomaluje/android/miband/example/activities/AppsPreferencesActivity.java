package com.betomaluje.android.miband.example.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.betomaluje.android.miband.example.R;
import com.betomaluje.android.miband.example.adapters.ApplicationsAdapter;
import com.betomaluje.android.miband.example.models.App;
import com.betomaluje.android.miband.example.sqlite.AppsSQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by betomaluje on 7/6/15.
 */
public class AppsPreferencesActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private final int APP_DETAIL_CODE = 5211;

    private RecyclerView recycler;
    private LinearLayoutManager lManager;

    private ArrayList<App> apps;
    private ApplicationsAdapter adapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_apps;
    }

    @Override
    protected void setActionBarIcon(int iconRes) {
        super.setActionBarIcon(iconRes);
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppsSQLite.getInstance(AppsPreferencesActivity.this).doesTableExists()) {
            fillApps();
        }

        apps = AppsSQLite.getInstance(AppsPreferencesActivity.this).getApps();

        adapter = new ApplicationsAdapter(AppsPreferencesActivity.this, apps, itemClickListener);

        recycler = (RecyclerView) findViewById(R.id.recyclerView);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        // Crear un nuevo adaptador
        recycler.setAdapter(adapter);
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = recycler.getChildAdapterPosition(v);

            thumbNailScaleAnimation(v, apps.get(position), position);
        }
    };

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

    private void thumbNailScaleAnimation(View view, App app, int position) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        AppsPreferencesActivity.this, view, AppDetailActivity.extra);
        Intent intent = new Intent(AppsPreferencesActivity.this, AppDetailActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(AppDetailActivity.extra, app);
        b.putInt(AppDetailActivity.extra_position, position);

        intent.putExtras(b);
        ActivityCompat.startActivityForResult(AppsPreferencesActivity.this, intent, APP_DETAIL_CODE, options.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_DETAIL_CODE && resultCode == RESULT_OK) {

            Bundle b = data.getExtras();

            if (b != null) {
                App returned = b.getParcelable(AppDetailActivity.extra_returned);

                if (returned != null) {
                    App previous = apps.get(b.getInt(AppDetailActivity.extra_position, 0));
                    previous.setNotify(returned.isNotify());
                    previous.setColor(returned.getColor());
                    previous.setStartTime(returned.getStartTime());
                    previous.setEndTime(returned.getEndTime());

                    adapter.notifyDataSetChanged();
                }
            }

            Snackbar.make(findViewById(R.id.coordinator), "Changes saved!", Snackbar.LENGTH_LONG).show();
        }
    }
}
