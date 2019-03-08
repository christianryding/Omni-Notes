package it.feio.android.omninotes;


import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExportHtmlTest extends ExportTestBase {
    @Override
    String getExtension() {
        return ".html";
    }

    @Override
    String getMime() {
        return "text/html";
    }

    @Override
    ViewInteraction getFormatViewInteraction() {
        return onView(
            allOf(withId(R.id.export_html), withText("HTML File"),
                    childAtPosition(
                            childAtPosition(
                                    withId(R.id.customViewFrame),
                                    0),
                        2), // Position in dialog
                    isDisplayed()));
    }


    @Override
    void testFileContent(File file) throws IOException {
        // Read file content
        byte[] data = Files.readAllBytes(file.toPath());
        String exportedNote = new String(data);

        assertTrue(exportedNote.startsWith("<!DOCTYPE html>"));
        assertTrue(exportedNote.contains(NOTE_TITLE));
    }
}
