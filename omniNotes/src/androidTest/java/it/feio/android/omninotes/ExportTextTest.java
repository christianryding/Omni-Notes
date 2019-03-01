package it.feio.android.omninotes;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * This test creates a note and exports it to a text file then checks to make sure it was exported
 * as expected.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExportTextTest extends BaseAndroidTestCase {
    private File exportTempFile = null;

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        stubFilePickerIntent();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Creates a test response for the file picker intent.
     * @throws Exception
     */
    public void stubFilePickerIntent() throws Exception {
        // Create a temp file for exporting the note
        File outputDir =  BaseAndroidTestCase.testContext.getCacheDir();
        exportTempFile = File.createTempFile("text", ".txt", outputDir);
        Uri uri = Uri.fromFile(exportTempFile);

        // Intent result with temp file uri
        Intent intent = new Intent();
        intent.setData(uri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        // Setup intent response
        HashSet<String> categories = new HashSet<>();
        categories.add(Intent.CATEGORY_OPENABLE);
        intending(allOf(
                hasAction(Intent.ACTION_CREATE_DOCUMENT),
                hasCategories(categories))
        ).respondWith(result);
    }

    @Test
    public void exportTextTest() throws Exception {
        ViewInteraction viewInteraction = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        childAtPosition(
                                allOf(withId(R.id.fab),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                2)),
                                3),
                        isDisplayed()));
        viewInteraction.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_note),
                        childAtPosition(
                                allOf(withId(R.id.fab),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                2)),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.detail_title),
                        childAtPosition(
                                allOf(withId(R.id.title_wrapper),
                                        childAtPosition(
                                                withId(R.id.detail_tile_card),
                                                0)),
                                1),
                        isDisplayed()));
        editText.perform(replaceText("Test title"), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.detail_content),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0)));
        editText2.perform(scrollTo(), replaceText("Abc\n123"), closeSoftKeyboard());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Export"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.export_text), withText("Text File"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.customViewFrame),
                                        0),
                                0),
                        isDisplayed()));
        textView.perform(click());

        // Check that the intent has been launched correctly
        HashSet<String> categories = new HashSet<>();
        categories.add(Intent.CATEGORY_OPENABLE);
        intended(allOf(
                hasAction(Intent.ACTION_CREATE_DOCUMENT),
                hasCategories(categories),
                hasType("text/plain"),
                hasExtra(Intent.EXTRA_TITLE, "Test title.txt")));


        // Check that the note has been exported

        // The exporting of the note is done in a background thread, so it's a bit tricky to know
        // when it's done. So wait a bit and hope it's done after that.
        Thread.sleep(250);

        // Read file content
        byte[] data = Files.readAllBytes(exportTempFile.toPath());
        String exportedNote = new String(data);
        String[] lines = exportedNote.split("\n");
        //exportTempFile.delete();

        // Test content
        assertEquals("Test title", lines[0]);
        assertEquals("==========", lines[1]);
        assertEquals("Abc", lines[3]);
        assertEquals("123", lines[4]);

        // TODO: Test attachments

        // Last line should contain a timestamp
        Context context = BaseAndroidTestCase.testContext;
        String timeStamp = lines[lines.length-1];
        assertTrue(timeStamp.contains(context.getString(R.string.last_update)));
        assertTrue(timeStamp.contains(context.getString(R.string.creation)));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
