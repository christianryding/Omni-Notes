package it.feio.android.omninotes.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class VcardHelper {

    private String contactsName;
    private List<ContactHelper.Contact> contactsPhoneNrs = new ArrayList<>();
    private List<ContactHelper.Contact> contactsMailAddresses = new ArrayList<>();

    public VcardHelper(String contactsName,List <ContactHelper.Contact> contactsPhoneNrs, List<ContactHelper.Contact> contactsMailAddresses){
        this.contactsPhoneNrs = new ArrayList<>();
        this.contactsMailAddresses = new ArrayList<>();
        this.contactsName = contactsName;
        this.contactsPhoneNrs = contactsPhoneNrs;
        this.contactsMailAddresses = contactsMailAddresses;
    }

    public File writeVcard(Context context){
        // Write vCard to file
        File vcfFile = null;
        try {
            vcfFile = new File(context.getExternalFilesDir(null) + "/" + contactsName + ".vcf");
            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:3.0\r\n");
            fw.write("FN:" + contactsName + "\r\n");
            for (int i = 0; i < contactsPhoneNrs.size(); i++) {
                fw.write("TEL;TYPE=" + contactsPhoneNrs.get(i).getType() + ",VOICE:" + contactsPhoneNrs.get(i).getData() + "\r\n");
            }
            for (int j = 0; j < contactsMailAddresses.size(); j++) {
                fw.write("EMAIL;TYPE=" + contactsMailAddresses.get(j).getType() + ":" + contactsMailAddresses.get(j).getData() + "\r\n");
            }
            fw.write("END:VCARD\r\n");
            fw.close();

            Log.d(Constants.TAG_VCARD, "wrote Vcard");
        } catch (Exception e) {
            Log.e(Constants.TAG_VCARD, "Could not write to vCard file");
        }

        return vcfFile;
    }







}
