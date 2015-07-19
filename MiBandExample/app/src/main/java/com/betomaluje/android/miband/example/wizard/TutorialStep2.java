package com.betomaluje.android.miband.example.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.betomaluje.android.miband.example.R;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Created by betomaluje on 7/1/15.
 */
public class TutorialStep2 extends WizardStep {

    @ContextVariable
    private int height;

    @ContextVariable
    private int weight;

    private EditText editText_height, editText_weight;

    //Wire the layout to the step
    public TutorialStep2() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_2, container, false);

        editText_height = (EditText) v.findViewById(R.id.editText_height);
        editText_weight = (EditText) v.findViewById(R.id.editText_weight);

        //editText_height.setText(String.valueOf(height));
        //editText_weight.setText(String.valueOf(weight));

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
        if (editText_height.getText().toString().isEmpty()) {
            //please put height
            //Toast.makeText(getActivity(), "Please enter your height", Toast.LENGTH_SHORT).show();
            height = 175;
        } else {
            height = Integer.parseInt(editText_height.getText().toString());
        }

        if (editText_weight.getText().toString().isEmpty()) {
            //please put weight
            //Toast.makeText(getActivity(), "Please enter your weight", Toast.LENGTH_SHORT).show();
            weight = 75;
        } else {
            weight = Integer.parseInt(editText_weight.getText().toString());
        }
    }

}
