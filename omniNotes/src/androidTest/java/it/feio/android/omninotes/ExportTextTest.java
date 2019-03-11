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

/**
 * Exports a note to a text file. The resulting file is search for the note content, title and
 * time stamp.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExportTextTest extends ExportTestBase {
    @Override
    String getExtension() {
        return ".txt";
    }

    @Override
    String getMime() {
        return "text/plain";
    }

    @Override
    ViewInteraction getFormatViewInteraction() {
        return onView(
            allOf(withId(R.id.export_text), withText("Text File"),
                    childAtPosition(
                            childAtPosition(
                                    withId(R.id.customViewFrame),
                                    0),
                        0), // Position in dialog
                    isDisplayed()));
    }


    @Override
    void testFileContent(File file) throws IOException {
        // Read file content
        byte[] data = Files.readAllBytes(file.toPath());
        String exportedNote = new String(data);
        String[] lines = exportedNote.split("\n");
        String[] content_lines = NOTE_CONTENT.split("\n");

        // Test content
        assertEquals(NOTE_TITLE, lines[0]);
        assertEquals(content_lines[0], lines[3]);
        assertEquals(content_lines[1], lines[4]);

        // Last line should contain a timestamp
        Context context = BaseAndroidTestCase.testContext;
        String timeStamp = lines[lines.length-1];
        assertTrue(timeStamp.contains(context.getString(R.string.last_update)));
        assertTrue(timeStamp.contains(context.getString(R.string.creation)));
    }
}
