package it.feio.android.omninotes;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.omninotes.export.Document;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;


/**
 * Test the exporter to make sure the exported information is the same as in the note.
 *
 * TODO: Add test for contact attachment
 */
@RunWith(AndroidJUnit4.class)
public class ExporterTest extends AndroidTestCase {
    private static final long TEST_TIME_MILLIS = 1551464308000L;
    private static final String TITLE = "Note title";
    private static final String CONTENT = "Note Content\nSecond row";
    private static final String UNCHECKED_ITEM = "Unchecked item";
    private static final String CHECKED_ITEM = "Checked item";
    private static final String CATEGORY = "Category";
    private static final String CAT_COLOR = "#F44336";
    private static final String LOCATION = "test address";
    private static final String REMINDER = "Weekly on Tuesday starting from Fri, Mar 1 7:18 PM";
    private static final String RECURRENCE_RULE = "FREQ=WEEKLY;WKST=SU;BYDAY=TU";
    private static final String CONTACT_FIRST_NAME = "";
    private static final String CONTACT_LAST_NAME = "";
    private static final String CONTACT_PHONE = "";
    private static final String CONTACT_EMAIL = "";

    private Context testContext;
    private final OutputStream testOutputStream = new OutputStream() {
        @Override
        public void write(int i) throws IOException {

        }
    };

    /**
     * Test implementation of an exporter. This will compare the information it gets from the
     * exporter with the information in the exported note.
     */
    class TestDoc extends Document {
        int checklistCounter = 0;

        @Override
        protected void begin() {
        }

        @Override
        protected void write(OutputStream os) {
            assertEquals(testOutputStream, os);
        }

        @Override
        protected void content(String noteTitle, String category, String color) {
            assertEquals(TITLE, noteTitle);
            assertEquals(CATEGORY, category);
            assertEquals(CAT_COLOR, color);
            super.content(noteTitle, category, color);
        }

        @Override
        protected void contacts(String contactsLabel) {
            assertEquals(testContext.getString(R.string.contacts_label), contactsLabel);
            super.contacts(contactsLabel);
        }


        @Override
        protected void attachments(String title) {
            assertEquals(testContext.getString(R.string.attachments_title), title);
            super.attachments(title);
        }

        @Override
        protected void textContent(String text) {
            assertEquals(CONTENT, text);
        }

        @Override
        protected void checklistItem(String text, boolean isChecked) {
            if (checklistCounter == 0) {
                assertEquals(UNCHECKED_ITEM, text);
                assertFalse(isChecked);
            } else if (checklistCounter == 1) {
                assertEquals(CHECKED_ITEM, text);
                assertTrue(isChecked);
            } else {
                assertTrue(checklistCounter <= 1);
            }
            checklistCounter++;
        }

        @Override
        protected void location(String locationLabel, String location) {
            assertEquals(testContext.getString(R.string.location), locationLabel);
            assertEquals(LOCATION, location);
        }

        @Override
        protected void reminder(String reminderLabel, String reminder) {
            assertEquals(testContext.getString(R.string.reminder_label), reminderLabel);
            assertEquals(REMINDER, reminder);
        }

        @Override
        protected void contactName(String nameKey, String first, String last) {
            assertEquals(testContext.getString(R.string.name_label), nameKey);
           // assertEquals(CONTACT_FIRST_NAME, first);
           // assertEquals(CONTACT_LAST_NAME, last);
        }

        @Override
        protected void contactPhone(String phoneKey, String phone) {
            assertEquals(testContext.getString(R.string.phonenumber_label), phoneKey);
         //   assertEquals(CONTACT_PHONE, phone);
        }

        @Override
        protected void contactEmail(String emailKey, String email) {
            assertEquals(testContext.getString(R.string.email_label), emailKey);
           // assertEquals(CONTACT_EMAIL, email);
        }

        @Override
        protected void timestamp(String timestamp) {
            String expectedTimeStamp = testContext.getString(R.string.last_update)
                    + " "
                    + DateHelper.getFormattedDate(TEST_TIME_MILLIS + 500, false)
                    + " ("
                    + testContext.getString(R.string.creation)
                    + " "
                    + DateHelper.getFormattedDate(TEST_TIME_MILLIS, false)
                    + ")";

            assertEquals(expectedTimeStamp, timestamp);
        }
    }

    @Before
    public void initTest() {
        testContext = InstrumentationRegistry.getTargetContext();
        LanguageHelper.updateLanguage(testContext, Locale.ENGLISH.toString());
    }

    @Test
    public void exportTextNote() {
        Note note = createNote();
        TestDoc exporter = new TestDoc();
        exporter.export(note, testOutputStream);
    }

    @Test
    public void exportChecklistNote() {
        Note note = createNote();
        note.setChecklist(true);
        note.setContent(Constants.UNCHECKED_SYM + UNCHECKED_ITEM + "\n" + Constants.CHECKED_SYM + CHECKED_ITEM);
        TestDoc exporter = new TestDoc();
        exporter.export(note, testOutputStream);
    }

    private Note createNote() {
        Category category = new Category(1L, CATEGORY, "", CAT_COLOR);

        Note note = new Note();
        note.set_id(1L);
        note.setTitle(TITLE);
        note.setContent(CONTENT);
        note.setCategory(category);
        note.setCreation(TEST_TIME_MILLIS);
        note.setLastModification(TEST_TIME_MILLIS + 500);
        note.setRecurrenceRule(RECURRENCE_RULE);
        note.setAlarm(TEST_TIME_MILLIS);
        note.setAddress(LOCATION);
        return note;
    }

}
