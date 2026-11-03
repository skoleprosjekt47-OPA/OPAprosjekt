package com.booklibrary.ui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GuideStepsTest {

    @Test
    void tourStartsAndEndsWithoutATab() {
        var steps = GuideSteps.all();
        assertEquals(GuideSteps.NO_TAB, steps.get(0).tabIndex());
        assertEquals(GuideSteps.NO_TAB, steps.get(steps.size() - 1).tabIndex());
    }

    @Test
    void everyStepHasText() {
        for (GuideSteps.Step step : GuideSteps.all()) {
            assertFalse(step.text().isBlank());
        }
    }

    @Test
    void tourCoversTheFiveTabsInOrder() {
        int expected = 0;
        for (GuideSteps.Step step : GuideSteps.all()) {
            if (step.tabIndex() != GuideSteps.NO_TAB) {
                assertEquals(expected++, step.tabIndex());
            }
        }
        assertEquals(5, expected);
    }

    @Test
    void praiseRepeatsAndNeverFails() {
        assertEquals(GuideSteps.praise(0), GuideSteps.praise(4));
        assertFalse(GuideSteps.praise(-3).isBlank());
    }
}
