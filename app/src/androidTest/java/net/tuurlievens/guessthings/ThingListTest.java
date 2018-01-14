package net.tuurlievens.guessthings;

import android.support.design.widget.TextInputEditText;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;


import net.tuurlievens.guessthings.activities.ThingListActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ThingListTest {

    @Rule
    public ActivityTestRule<ThingListActivity> activityRule = new ActivityTestRule(ThingListActivity.class);

//    @Before
//    public void resetDatabase() {
//        ThingDbHelper helper = new ThingDbHelper(null);
//        SQLiteDatabase db = helper.getWritableDatabase();
//        helper.onUpgrade(db,0,0);
//        db.close();
//
//        activityRule.launchActivity(null);
//    }

    @Test
    public void addAndDeleteNewThing() {
        // test showing only one button
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.save_button)).check(matches(isDisplayed()));
        onView(withId(R.id.delete_button)).check(matches(not(isDisplayed())));

        // test save
        onView(withId(R.id.thing_name)).perform(typeText("Test item name"));
        onView(withId(R.id.thing_descr)).perform(typeText("Test item description"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.save_button)).perform(click());

        // check if saved item is displayed
        onView(withId(R.id.thing_list))
            .perform(RecyclerViewActions.actionOnItem(
                hasDescendant(withText("Test item name")),
                click()));

        RecyclerView rv = activityRule.getActivity().findViewById(R.id.thing_list);
        int countBefore = rv.getAdapter().getItemCount();

        // delete item and check
        onView(withId(R.id.thing_name)).check(matches(hasValueEqualTo("Test item name")));
        onView(withId(R.id.delete_button)).perform(click());

        // press ok on dialog
        onView(withId(android.R.id.button1)).perform(click());

        int countAfter = rv.getAdapter().getItemCount();
        assertTrue("item count has gone down", countBefore -1 == countAfter);
    }


    @Test
    public void dismissAndUndoThing() {
        RecyclerView rv = activityRule.getActivity().findViewById(R.id.thing_list);
        int countBeforeSwipe = rv.getAdapter().getItemCount();

        onView(withId(R.id.thing_list))
            .perform(RecyclerViewActions.actionOnItem(
                hasDescendant(withText("Duck")),
                swipeRight()));

        // check if item doesnt exists in recyclerview
        int countAfterDismiss = rv.getAdapter().getItemCount();
        assertTrue("item count has gone down", countBeforeSwipe -1 == countAfterDismiss);

        // press undo button
        onView(allOf(withId(android.support.design.R.id.snackbar_action))).perform(click());

        // check if item exists in recyclerview
        int countAfterUndo = rv.getAdapter().getItemCount();
        assertTrue("item count has gone down", countAfterDismiss +1 == countAfterUndo);
    }

    @Test
    public void resetAllThings() {

        // reset and press ok on dialog
        onView(withId(R.id.fab)).perform(longClick());
        onView(withId(android.R.id.button1)).perform(click());

        RecyclerView rv = activityRule.getActivity().findViewById(R.id.thing_list);
        int countBefore = rv.getAdapter().getItemCount();

        // dismiss one thing
        onView(withId(R.id.thing_list))
            .perform(RecyclerViewActions.actionOnItem(
                hasDescendant(withText("Lamp")),
                swipeRight()));

        // reset and check if dismissed is restored
        onView(withId(R.id.fab)).perform(longClick());
        int countAfterReset = rv.getAdapter().getItemCount();
        assertTrue("item count has gone down", countBefore == countAfterReset);
    }

    // Matchers

    private Matcher<View> hasValueEqualTo(final String content) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Has TextInputEditText the value:  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (view != null && view instanceof TextInputEditText) {
                    String text = ((TextInputEditText) view).getText().toString();
                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }

        };
    }

}
