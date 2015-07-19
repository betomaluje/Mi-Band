package com.betomaluje.android.miband.example.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import com.betomaluje.android.miband.example.R;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Created by betomaluje on 6/30/15.
 */
public class TutorialStep1 extends WizardStep {

    @ContextVariable
    private boolean male = true;

    @ContextVariable
    private boolean female;

    @ContextVariable
    private int age;

    private RadioButton radioButton_male, radioButton_female;
    private EditText editText_age;

    //Wire the layout to the step
    public TutorialStep1() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_1, container, false);

        radioButton_male = (RadioButton) v.findViewById(R.id.male);
        radioButton_female = (RadioButton) v.findViewById(R.id.female);

        editText_age = (EditText) v.findViewById(R.id.editText_age);

        radioButton_male.setChecked(male);
        radioButton_female.setChecked(female);

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
        //The values of these fields will be automatically stored in the wizard context
        //and will be populated in the next steps only if the same field names are used.
        male = radioButton_male.isChecked();
        female = radioButton_female.isChecked();

        if (!male && !female) {
            //please put a gender
            //Toast.makeText(getActivity(), "Please enter your gender", Toast.LENGTH_SHORT).show();
            male = true;
        }

        if (editText_age.getText().toString().isEmpty()) {
            //please put age
            //Toast.makeText(getActivity(), "Please enter your age", Toast.LENGTH_SHORT).show();
            age = 25;
        } else {
            age = Integer.parseInt(editText_age.getText().toString());
        }

    }
}
