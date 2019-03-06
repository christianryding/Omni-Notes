package it.feio.android.omninotes.export;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Exports a note to a pdf file. Currently quite limited, see todo list.
 *
 * TODO:
 * - The pager needs to be quite big to not get text with irregular character spacing. But this
 *   makes the resulting PDF quite "big" when opening it in a PDF viewer. Figure out how to solve
 *   this.
 * - Only single pages are supported at the moment. Support multiple pages.
 * - Long texts are not wrapped except for the note text.
 */
public class PdfExporter extends ExporterBase {
    /**
     * Paper width.
     */
    private final int PAPER_WIDTH = 2100;
    /**
     * Paper height.
     */
    private final int PAPER_HEIGHT = 2970;
    /**
     * Left and right margin of the document.
     */
    private final int MARGIN_X = 200;
    /**
     * Top and bottom margin of the document.
     */
    private final int MARGIN_Y = 300;

    /**
     * Title header font size
     */
    private final int TITLE_SIZE = 120;
    /**
     * Attachments header font size
     */
    private final int ATTACH_TITLE_SIZE = 100;
    /**
     * Headers in attachments section.
     */
    private final int ATTACH_SUBTITLE_SIZE = 80;
    /**
     * Content text size, used for all text that is not headers.
     */
    private final int TEXT_SIZE = 40;
    /**
     * Space between lines. The spacing will be this multiplied with the text size.
     */
    private final float LINE_SPACE = 0.3f;

    /**
     * Note that is exported.
     */
    private NoteFacade facade = null;
    /**
     * Resulting PDF document.
     */
    private PdfDocument document = null;
    /**
     * Canvas of the page that is written to.
     */
    private Canvas canvas = null;

    /**
     * Title text paint
     */
    private Paint paintH1;
    /**
     * Attachments text paint
     */
    private Paint paintH2;
    /**
     * Attachments subtitle text paint
     */
    private Paint paintH3;
    /**
     * Content text paint.
     */
    private Paint paintText;

    /**
     * Constructs a PDF exporter.
     */
    public PdfExporter() {
        paintH1 = new Paint();
        paintH1.setColor(Color.BLACK);
        paintH1.setTextSize(TITLE_SIZE);
        paintH1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintH2 = new Paint();
        paintH2.setColor(Color.BLACK);
        paintH2.setTextSize(ATTACH_TITLE_SIZE);
        paintH2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintH3 = new Paint();
        paintH3.setColor(Color.BLACK);
        paintH3.setTextSize(ATTACH_SUBTITLE_SIZE);
        paintH3.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(TEXT_SIZE);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
    }

    @Override
    protected void createDocument(NoteFacade facade) throws ExporterException {
        this.facade = facade;

        // create a new document
        document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAPER_WIDTH, PAPER_HEIGHT, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);


        // draw something on the page
        canvas = page.getCanvas();
        setOriginTranslation();

        printTitle();
        printContent();
        printAttachments();
        printTimeStamp();

        // finish the page
        document.finishPage(page);

    }

    @Override
    protected void writeDocument(OutputStream os) throws ExporterException {
        try {
            document.writeTo(os);
        } catch (IOException e) {
            throw new ExporterException("IO error when writing to file.");
        }
        document.close();
    }

    /**
     * Translate the current drawing location to the top left corner.
     */
    private void setOriginTranslation() {
        canvas.translate(MARGIN_X, MARGIN_Y);
    }

    /**
     * Draw a text on the page canvas and update the canvas translation so the next text will be
     * drawn under this.
     * @param text the text to draw.
     * @param paint which text paint to use.
     */
    private void print(String text, Paint paint) {
        canvas.translate(0, paint.getTextSize());
        canvas.drawText(text, 0, 0, paint);
        canvas.translate(0, paint.getTextSize() * LINE_SPACE);
    }

    /**
     * Prints a text on two-column-form. This is used by the contacts part of the document
     * construction.
     * @param col1 text of left column
     * @param col2 text of right column
     * @param col1Width the width of left column.
     * @param paint the text paint to use.
     */
    private void printColumns(String col1, String col2, float col1Width, Paint paint) {
        float colSeparation = paintText.getTextSize();

        canvas.translate(0, paint.getTextSize());

        canvas.drawText(col1, 0, 0, paint);

        canvas.save();
        canvas.translate(col1Width + colSeparation, 0);
        canvas.drawText(col2, 0, 0, paint);
        canvas.restore();

        canvas.translate(0, paint.getTextSize() * LINE_SPACE);
    }

    /**
     * Progress the translation an empty line downwards.
     * @param paint this is used to decide how high a line is, that is the text size.
     */
    private void printEmpty(Paint paint) {
        canvas.translate(0, paint.getTextSize());
    }


    /**
     * Draws the title of the note.
     */
    private void printTitle() {
        String title;
        if (facade.hasCategory()) {
            title = facade.getTitle() + " (" + facade.getCategoryName() + ")";
        } else {
            title = facade.getTitle();
        }

        print(title, paintH1);

        printEmpty(paintText);
    }

    /**
     * Draw note content, note text or checklist depending on the note.
     */
    private void printContent() {
        if (facade.isNoteChecklist()) {
            for (NoteFacade.ChecklistItem item : facade.getChecklist()) {
                final char checkedChar = item.isChecked ? '☑' : '☐';
                String text = checkedChar + " " + item.text;
                print(text, paintText);
            }
        } else {
            // Content text could be quite long so use StaticLayout to have line wrapping.
            // TODO: Use LINE_SPACE instead of hardcoded value.
            String text = facade.getTextContent();
            TextPaint paint = new TextPaint();
            paint.set(paintText);
            StaticLayout layout = new StaticLayout(
                    text,
                    paint,
                    PAPER_WIDTH - MARGIN_X * 2,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    3.0f,
                    false);

            layout.draw(canvas);

            canvas.translate(0, layout.getHeight());
            printEmpty(paintText);
        }
    }

    /**
     * Draw note attachments
     */
    private void printAttachments() {
        if (!facade.hasContacts() && !facade.hasLocation() && !facade.hasReminder()) {
            return;
        }

        printEmpty(paintText);

        print(facade.getString(NoteFacade.STRING_ATTACHMENTS), paintH2);

        printLocation();
        printReminder();
        printContacts();
    }

    /**
     * Draw location attachment
     */
    private void printLocation() {
        if (facade.hasLocation()) {
            printEmpty(paintText);
            print(facade.getString(NoteFacade.STRING_LOCATION), paintH3);
            print(facade.getLocation(), paintText);
        }
    }

    /**
     * Draw reminder attachment
     */
    private void printReminder() {
        if (facade.hasReminder()) {
            printEmpty(paintText);
            print(facade.getString(NoteFacade.STRING_REMINDER), paintH3);
            print(facade.getReminder(), paintText);
        }
    }

    /**
     * Draw contact attachments
     */
    private void printContacts() {
        if (facade.hasContacts()) {
            printEmpty(paintText);
            print(facade.getString(NoteFacade.STRING_CONTACTS), paintH3);

            String name = facade.getString(NoteFacade.STRING_NAME);
            String phone = facade.getString(NoteFacade.STRING_PHONE);
            String email = facade.getString(NoteFacade.STRING_EMAIL);

            // Find which label is longest to be able to make the columns the right width
            float longest = getLongestText(new String[] {name, phone, email});

            // Add the contact info for all contacts
            for (NoteFacade.Contact contact : facade.getContacts()) {
                printColumns(name, contact.name, longest, paintText);
                printColumns(phone, contact.phone, longest, paintText);
                printColumns(email, contact.email, longest, paintText);

                printEmpty(paintText);
            }
        }
    }

    /**
     * Draw note time stamp on bottom of the page.
     */
    private void printTimeStamp() {
        canvas.save();

        // Set to identity matrix
        canvas.setMatrix(null);

        // Translate to the page foot
        setOriginTranslation();
        canvas.translate(0, PAPER_HEIGHT - MARGIN_Y * 2);

        print(facade.getTimestamp(), paintText);

        canvas.restore();
    }

    /**
     * Measures which text that would be the longest if it would be drawn on the canvas. Used to
     * find right column with when drawing the contact columns.
     * @param texts a list of strings.
     * @return the length of the longest text.
     */
    private float getLongestText(String[] texts) {
        float longest = 0.0f;

        for (String text : texts) {
            float length = paintText.measureText(text);
            longest = Math.max(longest, length);
        }

        return longest;
    }
}
