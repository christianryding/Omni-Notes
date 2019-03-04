package it.feio.android.omninotes;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Locale;

import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.omninotes.export.NoteFacade;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;


/**
 * Test the exporter to make sure NoteFacade gives the expected information.
 *
 * TODO: Add test for contact attachment
 */
@RunWith(AndroidJUnit4.class)
public class ExporterNoteFacadeTest extends AndroidTestCase {
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

    @Before
    public void initTest() {
        testContext = InstrumentationRegistry.getTargetContext();

        // The test uses english string translations
        LanguageHelper.updateLanguage(testContext, Locale.ENGLISH.toString());
    }

    /**
     * Test basic note stuff
     */
    @Test
    public void testTextNote() {
        Note note = new Note();
        note.setTitle(TITLE);
        note.setContent(CONTENT);
        note.setCreation(TEST_TIME_MILLIS);
        note.setLastModification(TEST_TIME_MILLIS + 500);
        NoteFacade facade = new NoteFacade(note);

        assertEquals(TITLE, facade.getTitle());
        assertFalse(facade.isNoteChecklist());
        assertEquals(CONTENT, facade.getTextContent());

        String expectedTimeStamp = testContext.getString(R.string.last_update)
                + " "
                + DateHelper.getFormattedDate(TEST_TIME_MILLIS + 500, false)
                + " ("
                + testContext.getString(R.string.creation)
                + " "
                + DateHelper.getFormattedDate(TEST_TIME_MILLIS, false)
                + ")";
        assertEquals(expectedTimeStamp, facade.getTimestamp());
    }

    /**
     * Note with a checklist
     */
    @Test
    public void testChecklistNote() {
        Note note = new Note();
        note.setTitle(TITLE);
        note.setChecklist(true);
        note.setContent(Constants.UNCHECKED_SYM + UNCHECKED_ITEM + "\n" + Constants.CHECKED_SYM + CHECKED_ITEM);
        NoteFacade facade = new NoteFacade(note);

        assertTrue(facade.isNoteChecklist());

        List<NoteFacade.ChecklistItem> items = facade.getChecklist();
        assertEquals(items.size(), 2);

        assertFalse(items.get(0).isChecked);
        assertEquals(UNCHECKED_ITEM, items.get(0).text);

        assertTrue(items.get(1).isChecked);
        assertEquals(CHECKED_ITEM, items.get(1).text);
    }

    /**
     * Test a note without a category
     */
    @Test
    public void testNoCategory() {
        Note note = new Note();
        NoteFacade facade = new NoteFacade(note);

        assertFalse(facade.hasCategory());
    }

    /**
     * Test a note with a category
     */
    @Test
    public void testCategory() {
        Note note = new Note();
        Category category = new Category(1L, CATEGORY, "", CAT_COLOR);
        note.setCategory(category);

        NoteFacade facade = new NoteFacade(note);

        assertTrue(facade.hasCategory());
        assertEquals(CATEGORY, facade.getCategoryName());
        assertEquals(CAT_COLOR, facade.getCategoryColor());
    }

    /**
     * Test a note without any attachments
     */
    @Test
    public void testNoAttachments() {
        Note note = new Note();
        NoteFacade facade = new NoteFacade(note);

        assertFalse(facade.hasLocation());
        assertFalse(facade.hasReminder());
        assertFalse(facade.hasContacts());
    }

    /**
     * Test note attachments
     */
    @Test
    public void testAttachments() {
        // TODO: Test contacts attachment

        Note note = new Note();
        note.setRecurrenceRule(RECURRENCE_RULE);
        note.setAlarm(TEST_TIME_MILLIS);
        note.setAddress(LOCATION);
        NoteFacade facade = new NoteFacade(note);


        assertTrue(facade.hasLocation());
        assertEquals(LOCATION, facade.getLocation());

        assertTrue(facade.hasReminder());
        assertEquals(REMINDER, facade.getReminder());

        //assertTrue(facade.hasContacts());
    }
}
