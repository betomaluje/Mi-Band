package com.betomaluje.android.miband.example.wizard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.betomaluje.android.miband.example.R;
import com.betomaluje.android.miband.example.activities.MainActivity;
import com.betomaluje.miband.model.UserInfo;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Created by betomaluje on 7/1/15.
 */
public class TutorialSummary extends WizardStep {

    @ContextVariable
    private boolean male;

    @ContextVariable
    private boolean female;

    @ContextVariable
    private int age;

    @ContextVariable
    private int height;

    @ContextVariable
    private int weight;

    //int gender, int age, int height, int weight, String alias
    private TextView textview_gender, textview_age, textview_height, textview_weight;

    //Wire the layout to the step
    public TutorialSummary() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_summary, container, false);

        textview_gender = (TextView) v.findViewById(R.id.textview_gender);
        textview_age = (TextView) v.findViewById(R.id.textview_age);
        textview_height = (TextView) v.findViewById(R.id.textview_height);
        textview_weight = (TextView) v.findViewById(R.id.textview_weight);

        String gender = male ? getString(R.string.gender_male) : getString(R.string.gender_female);

        textview_gender.setText("Gender: " + gender);
        textview_age.setText("Age: " + String.valueOf(age) + " " + getString(R.string.age));
        textview_height.setText("Height: " + String.valueOf(height) + " " + getString(R.string.height));
        textview_weight.setText("Weight: " + String.valueOf(weight) + " " + getString(R.string.weight));

        return v;
    }

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                bindDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                break;
        }
    }

    private void bindDataFields() {
        //finally save data to local storage

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(UserInfo.KEY_PREFERENCES, Context.MODE_PRIVATE);

        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(UserInfo.KEY_GENDER, male ? 0 : 1);
        editor.putInt(UserInfo.KEY_AGE, age);
        editor.putInt(UserInfo.KEY_HEIGHT, height);
        editor.putInt(UserInfo.KEY_WEIGHT, weight);
        editor.putInt(UserInfo.KEY_TYPE, 0);
        editor.putString(UserInfo.KEY_ALIAS, UserInfo.generateAlias());

        //we update the main shared preferences
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("firstrun", false).commit();

        if (editor.commit()) {
            getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

    }
}
