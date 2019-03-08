package it.feio.android.omninotes;


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
 * Exports a note to a PDF file. Because PDF is a binary format it's not possible to test that it
 * contains the note, because of that it only checks the PDF header magic number.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExportPdfTest extends ExportTestBase {
    @Override
    String getExtension() {
        return ".pdf";
    }

    @Override
    String getMime() {
        return "application/pdf";
    }

    @Override
    ViewInteraction getFormatViewInteraction() {
        return onView(
            allOf(withId(R.id.export_pdf), withText("PDF File"),
                    childAtPosition(
                            childAtPosition(
                                    withId(R.id.customViewFrame),
                                    0),
                        1), // Position in dialog
                    isDisplayed()));
    }


    @Override
    void testFileContent(File file) throws IOException {
        // Check to see if a PDF file was produced.
        // PDF file header:
        // https://en.wikipedia.org/wiki/PDF#File_structure
        byte[] data = Files.readAllBytes(file.toPath());
        assertTrue(data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46);
    }
}
