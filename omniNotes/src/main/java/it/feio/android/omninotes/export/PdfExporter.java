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
 * Exports a note to a pdf file.
 *
 * TODO:
 * - The pager needs to be quite big to not get text with irregular character spacing. But this
 *   makes the resulting PDF quite "big" when opening it in a PDF viewer. Figure out how to solve
 *   this.
 * - Only single pages are supported at the moment. Support multiple pages.
 */
public class PdfExporter extends ExporterBase {
    // Paper dimension and margins
    private final int PAPER_WIDTH = 2100;
    private final int PAPER_HEIGHT = 2970;
    private final int MARGIN_X = 200;
    private final int MARGIN_Y = 300;

    // Header text sizes
    private final int H1_SIZE = 120;
    private final int H2_SIZE = 100;
    private final int H3_SIZE = 80;
    // Content text size
    private final int TEXT_SIZE = 40;
    // Space between lines. The spacing will be this multiplied with the text size.
    private final float LINE_SPACE = 0.3f;

    private NoteFacade facade = null;
    private PdfDocument document = null;
    private Canvas canvas = null;

    private Paint paintH1;
    private Paint paintH2;
    private Paint paintH3;
    private Paint paintText;

    public PdfExporter() {
        paintH1 = new Paint();
        paintH1.setColor(Color.BLACK);
        paintH1.setTextSize(H1_SIZE);
        paintH1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintH2 = new Paint();
        paintH2.setColor(Color.BLACK);
        paintH2.setTextSize(H2_SIZE);
        paintH2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paintH3 = new Paint();
        paintH3.setColor(Color.BLACK);
        paintH3.setTextSize(H3_SIZE);
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

    private void setOriginTranslation() {
        canvas.translate(MARGIN_X, MARGIN_Y);
    }

    private void print(String text, Paint paint) {
        canvas.translate(0, paint.getTextSize());
        canvas.drawText(text, 0, 0, paint);
        canvas.translate(0, paint.getTextSize() * LINE_SPACE);
    }

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

    private void printEmpty(Paint paint) {
        canvas.translate(0, paint.getTextSize());
    }


    /**
     * Add note title
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
     * Add note content, note text or checklist
     */
    private void printContent() {
        if (facade.isNoteChecklist()) {
            for (NoteFacade.ChecklistItem item : facade.getChecklist()) {
                final char checkedChar = item.isChecked ? '☑' : '☐';
                String text = checkedChar + " " + item.text;
                print(text, paintText);
            }
        } else {
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
     * Add note attachments
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
     * Add location attachment
     */
    private void printLocation() {
        if (facade.hasLocation()) {
            printEmpty(paintText);
            print(facade.getString(NoteFacade.STRING_LOCATION), paintH3);
            print(facade.getLocation(), paintText);
        }
    }

    /**
     * Add reminder attachment
     */
    private void printReminder() {
        if (facade.hasReminder()) {
            printEmpty(paintText);
            print(facade.getString(NoteFacade.STRING_REMINDER), paintH3);
            print(facade.getReminder(), paintText);
        }
    }

    /**
     * Add contact attachments
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
     * Add note time stamp
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

    private float getLongestText(String[] texts) {
        float longest = 0.0f;

        for (String text : texts) {
            float length = paintText.measureText(text);
            longest = Math.max(longest, length);
        }

        return longest;
    }
}
