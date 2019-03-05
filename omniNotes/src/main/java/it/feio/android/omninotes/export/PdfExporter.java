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
 * - Fix the character spacing. Something is wrong with the character spacing.
 * - Cleanup the code.
 * - Support multiple pages.
 */
public class PdfExporter extends ExporterBase {
    // The PDF document is the of a A4 paper in mm
    private final int WIDTH = 2100;
    private final int HEIGHT = 2970;

    private final int ORIGIN_X = 200;
    private final int ORIGIN_Y = 300;

    private final int H1_SIZE = 120;
    private final int H2_SIZE = 100;
    private final int H3_SIZE = 80;
    private final int TEXT_SIZE = 40;

    private NoteFacade facade = null;

    private PdfDocument document = null;
    private Canvas canvas = null;


    // The current print position on the canvas
    private int position = 0;

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
        position = 0;

        // create a new document
        document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);


        // draw something on the page
        canvas = page.getCanvas();
        canvas.translate(ORIGIN_X, ORIGIN_Y);

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

    private void print(String text, Paint paint) {
        position += paint.getTextSize();
        canvas.drawText(text, 0, position, paint);
        position += 3;
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
                    WIDTH - ORIGIN_X * 2,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    3.0f,
                    false);

            canvas.save();
            canvas.translate(0, position);
            layout.draw(canvas);
            canvas.restore();


            position += layout.getHeight() + 5;
        }
    }

    /**
     * Add note attachments
     */
    private void printAttachments() {
        if (!facade.hasContacts() && !facade.hasLocation() && !facade.hasReminder()) {
            return;
        }

        position += 5;

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
            print(facade.getString(NoteFacade.STRING_LOCATION), paintH3);
            print(facade.getLocation(), paintText);
        }
    }

    /**
     * Add reminder attachment
     */
    private void printReminder() {
        if (facade.hasReminder()) {
            print(facade.getString(NoteFacade.STRING_REMINDER), paintH3);
            print(facade.getReminder(), paintText);
        }
    }

    /**
     * Add contact attachments
     */
    private void printContacts() {
        if (facade.hasContacts()) {
            print(facade.getString(NoteFacade.STRING_CONTACTS), paintH3);

            String name = facade.getString(NoteFacade.STRING_NAME);
            String phone = facade.getString(NoteFacade.STRING_PHONE);
            String email = facade.getString(NoteFacade.STRING_EMAIL);

            // Find which label is longest to be able to make the columns the right width
            float longest = getLongestText(new String[] {name, phone, email});

            // Add the contact info for all contacts
            for (NoteFacade.Contact contact : facade.getContacts()) {
                printContactRow(name, contact.name, longest);
                printContactRow(phone, contact.phone, longest);
                printContactRow(email, contact.email, longest);
                position += 3;
            }

            position += 5;
        }
    }

    /**
     * Add note time stamp
     */
    private void printTimeStamp() {
        int oldPos = position;
        position = HEIGHT - ORIGIN_Y * 2;
        print(facade.getTimestamp(), paintText);
        position = oldPos;
    }

    private void printContactRow(String col1, String col2, float col1Width) {
        int oldPos = position;
        print(col1, paintText);
        position = oldPos;
        canvas.save();
        canvas.translate(col1Width+5, 0);
        print(col2, paintText);
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
