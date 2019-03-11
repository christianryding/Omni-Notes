package it.feio.android.omninotes;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
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

import java.io.File;
import java.io.IOException;
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
 * Base UI test class for a exporting a note to a file. The file format is specified in the sub
 * classes: ExportTextTest, ExportHtmlTest and ExportPdfTest.
 */
public abstract class ExportTestBase extends BaseAndroidTestCase {
    protected final static String NOTE_TITLE = "Test Note";
    protected final static String NOTE_CONTENT = "Abc\n123";

    private File exportTempFile = null;

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    /**
     * @return File extension of the file to be exported.
     */
    abstract String getExtension();

    /**
     * @return MIME type of the file to be exported.
     */
    abstract String getMime();

    /**
     * To export a file the user has to choose the file format in a dialog. This is called
     * when the test needs to choose that format.
     * @return
     */
    abstract ViewInteraction getFormatViewInteraction();

    /**
     * This is called when the test needs to check the file content of the resulting file.
     * @param file The file to check
     * @throws IOException
     */
    abstract void testFileContent(File file) throws IOException;



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
        exportTempFile = File.createTempFile("export_test", getExtension(), outputDir);
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
        editText.perform(replaceText(NOTE_TITLE), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.detail_content),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0)));
        editText2.perform(scrollTo(), replaceText(NOTE_CONTENT), closeSoftKeyboard());

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

        // Choose the file format in the format dialog
        getFormatViewInteraction().perform(click());

        // Check that the intent has been launched correctly
        HashSet<String> categories = new HashSet<>();
        categories.add(Intent.CATEGORY_OPENABLE);
        intended(allOf(
                hasAction(Intent.ACTION_CREATE_DOCUMENT),
                hasCategories(categories),
                hasType(getMime()),
                hasExtra(Intent.EXTRA_TITLE, NOTE_TITLE + getExtension())));


        // The exporting of the note is done in a background thread, so it's a bit tricky to know
        // when it's done. So wait a bit and hope it's done after that.
        Thread.sleep(250);

        // Test file content
        testFileContent(exportTempFile);

        exportTempFile.delete();
    }

    protected static Matcher<View> childAtPosition(
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
