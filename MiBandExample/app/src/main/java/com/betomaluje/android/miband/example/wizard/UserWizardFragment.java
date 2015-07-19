package com.betomaluje.android.miband.example.wizard;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.layouts.BasicWizardLayout;

/**
 * Created by betomaluje on 6/30/15.
 */
public class UserWizardFragment extends BasicWizardLayout {

    public UserWizardFragment() {
        super();
    }

    @Override
    public WizardFlow onSetup() {
        setNextButtonText("Next");
        setBackButtonText("Back");
        setFinishButtonText("I'm ready");

        //int gender, int age, int height, int weight, String alias

        return new WizardFlow.Builder()
                .addStep(TutorialStep1.class)
                .addStep(TutorialStep2.class)
                .addStep(TutorialSummary.class)
                .create();
    }
}
