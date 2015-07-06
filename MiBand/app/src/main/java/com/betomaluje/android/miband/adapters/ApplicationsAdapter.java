package com.betomaluje.android.miband.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betomaluje.android.miband.R;
import com.betomaluje.android.miband.models.App;

import java.util.List;

/**
 * Created by betomaluje on 7/6/15.
 */
public class ApplicationsAdapter extends BaseAdapter {

    private List<App> apps;
    private Context context;
    private LayoutInflater inflater;
    private final PackageManager pm;

    public ApplicationsAdapter(Context context, List<App> s) {
        this.apps = s;
        this.context = context;
        pm = context.getPackageManager();

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return apps.size();
    }

    @Override
    public App getItem(int position) {
        // TODO Auto-generated method stub
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        try {
            View rowView = convertView;

            final App app = getItem(position);

            // reuse views
            if (rowView == null) {

                rowView = inflater.inflate(R.layout.apps_item_row, parent, false);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.textView_appName = (TextView) rowView.findViewById(R.id.textView_appName);
                viewHolder.imageView_appIcon = (ImageView) rowView.findViewById(R.id.imageView_appIcon);
                viewHolder.view_appColor = rowView.findViewById(R.id.view_appColor);

                rowView.setTag(viewHolder);
            } else {
                rowView = convertView;
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            ApplicationInfo applicationInfo = pm.getApplicationInfo(app.getSource(), PackageManager.GET_META_DATA);

            holder.textView_appName.setText(pm.getApplicationLabel(applicationInfo).toString());
            holder.imageView_appIcon.setImageDrawable(pm.getApplicationIcon(applicationInfo));
            holder.view_appColor.setBackgroundColor(app.getColorForView());

            return rowView;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return null;
    }

    public static class ViewHolder {
        public TextView textView_appName;
        public ImageView imageView_appIcon;
        public View view_appColor;
    }

}
