package com.betomaluje.android.miband.example.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.betomaluje.android.miband.example.R;
import com.betomaluje.android.miband.example.models.App;

import java.util.List;

/**
 * Created by betomaluje on 7/6/15.
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.SimpleViewHolder> {

    private List<App> apps;
    private Context context;
    private LayoutInflater inflater;
    private final PackageManager pm;

    private View.OnClickListener clickListener;

    public ApplicationsAdapter(Context context, List<App> s, View.OnClickListener clickListener) {
        this.apps = s;
        this.context = context;
        this.clickListener = clickListener;
        pm = context.getPackageManager();

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_appName;
        public ImageView imageView_appIcon;
        public View view_appColor;

        public SimpleViewHolder(View v) {
            super(v);

            textView_appName = (TextView) v.findViewById(R.id.textView_appName);
            imageView_appIcon = (ImageView) v.findViewById(R.id.imageView_appIcon);
            view_appColor = v.findViewById(R.id.view_appColor);

            v.setOnClickListener(clickListener);
        }
    }

    @Override
    public ApplicationsAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.apps_item_row, parent, false);
        return new SimpleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ApplicationsAdapter.SimpleViewHolder holder, int position) {
        try {
            final App app = apps.get(position);

            ApplicationInfo applicationInfo = pm.getApplicationInfo(app.getSource(), PackageManager.GET_META_DATA);
            holder.textView_appName.setText(pm.getApplicationLabel(applicationInfo).toString());
            holder.imageView_appIcon.setImageDrawable(pm.getApplicationIcon(applicationInfo));

            if (app.isNotify()) {
                holder.view_appColor.setBackgroundColor(app.getColorForView());
                holder.view_appColor.setVisibility(View.VISIBLE);
            } else {
                holder.view_appColor.setVisibility(View.INVISIBLE);
            }


        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }
}
