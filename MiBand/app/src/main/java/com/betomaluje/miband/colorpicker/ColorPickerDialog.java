package com.betomaluje.miband.colorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class ColorPickerDialog extends AlertDialog {

    private final ColorPicker colorPickerView;

    public ColorPickerDialog(Context context, int initialColor, final OnColorSelectedListener onColorSelectedListener) {
        super(context);

        RelativeLayout relativeLayout = new RelativeLayout(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        colorPickerView = new ColorPicker(context);
        colorPickerView.setColor(initialColor);

        relativeLayout.addView(colorPickerView, layoutParams);

        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BUTTON_POSITIVE:
                        int selectedColor = colorPickerView.getColor();
                        onColorSelectedListener.onColorSelected(selectedColor);
                        break;
                    case BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }

        };
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), onClickListener);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), onClickListener);

        setView(relativeLayout);

    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }
}
