package ca.sozoservers.dev.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.internet.InternetAddress;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.RowData;

import org.apache.commons.codec.binary.Base64;


public class Config {

    public static class EmailConfig {

        private SubjectLine subjectLine;
        private Body body;
        private Attachment attachment;
        private InternetAddress to;
        private Sponsor sponsor;

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        public void setSubjectLine(SubjectLine subjectLine) {
            this.subjectLine = subjectLine;
        }

        public void setTo(InternetAddress to) {
            this.to = to;
        }

        public void setSponsor(Sponsor sponsor) {
            this.sponsor = sponsor;
        }

        public boolean isReady(){
            return !(subjectLine == null || body == null || attachment == null);
        }

        public Attachment getAttachment() {
            return attachment;
        }

        public Body getBody() {
            return body;
        }

        public Sponsor getSponsor() {
            return sponsor;
        }

        public SubjectLine getSubjectLine() {
            return subjectLine;
        }

        public InternetAddress getTo() {
            return to;
        }
        
        public String formatText(String input){
            String result = new String();
            result = input.replaceAll("%name%", sponsor.name);
            result = result.replaceAll("%email%", sponsor.email);
            result = result.replaceAll("%phone%", sponsor.phone);
            result = result.replaceAll("%website%", sponsor.website);
            return result;
        }

        public String toString(){
            String str = new String();
            str += to;
            str += sponsor;
            str += subjectLine;
            str += body;
            str += attachment;
            return str;
        }
    }

    public static class SubjectLine {
        
        private String name;
        private String content;

        public SubjectLine(String name, String content){
            this.content = content;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }
        
        @Override
        public String toString(){
            return getName();
        }
    }

    public static class Body {
        
        private String name;
        private String content;

        public Body(String name, String content){
            this.content = content;
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString(){
            return getName();
        }
    }

    public static class Attachment {
        
        private String name;
        private File content;

        public Attachment(String name, File content){
            this.content = content;
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public File getContent() {
            return content;
        }
        
        @Override
        public String toString(){
            return getName();
        }
    }

    public static class Sponsor {

        public Boolean contacted;
        public String name;
        public String email;
        public String phone;
        public String website;
        public String other;
        public String notes;

        public Boolean getContacted() {
            return contacted;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getNotes() {
            return notes;
        }

        public String getOther() {
            return other;
        }

        public String getPhone() {
            return phone;
        }

        public String getWebsite() {
            return website;
        }

        public static Sponsor createSponsor(RowData rowData) {
            Sponsor sponsor = new Sponsor(){{
                contacted = rowData.getValues().get(0).getEffectiveFormat().getBackgroundColor().getRed() == null;
                name = rowData.getValues().get(1).getFormattedValue();
                email = rowData.getValues().get(2).getFormattedValue();
                phone = rowData.getValues().get(3).getFormattedValue();
                website = rowData.getValues().get(4).getFormattedValue();
                other = rowData.getValues().get(5).getFormattedValue();
                notes = rowData.getValues().get(6).getFormattedValue();
            }};
            return sponsor;
        }

        public List<RowData> toRowData(){
            List<RowData> rowData = new ArrayList<RowData>();
            List<CellData> cellData = new ArrayList<CellData>();
            
            CellFormat contactFormat = new CellFormat();
            com.google.api.services.sheets.v4.model.Color color = new com.google.api.services.sheets.v4.model.Color();
    
            if(contacted){
                color.setGreen(1f);
            }else{
                color.setRed(1f);
            }
            contactFormat.setBackgroundColor(color);
    
            CellData sponsorContactCell = new CellData(),
                sponsorNameCell = new CellData(),
                sponsorEmailCell = new CellData(),
                sponsorPhoneCell = new CellData(),
                sponsorWebsiteCell = new CellData(),
                sponsorOtherCell = new CellData(),
                sponsorNotesCell = new CellData();

            sponsorContactCell.setUserEnteredFormat(contactFormat);
            sponsorNameCell.setUserEnteredValue(new ExtendedValue().setStringValue(name));
            sponsorEmailCell.setUserEnteredValue(new ExtendedValue().setStringValue(email));   
            sponsorPhoneCell.setUserEnteredValue(new ExtendedValue().setStringValue(phone));  
            sponsorWebsiteCell.setUserEnteredValue(new ExtendedValue().setStringValue(website));  
            sponsorOtherCell.setUserEnteredValue(new ExtendedValue().setStringValue(other));
            sponsorNotesCell.setUserEnteredValue(new ExtendedValue().setStringValue(notes));
    
            cellData.addAll(Arrays.asList(sponsorContactCell, sponsorNameCell, sponsorEmailCell, sponsorPhoneCell, sponsorWebsiteCell, sponsorOtherCell, sponsorNotesCell));
            rowData.add(new RowData().setValues(cellData));
            return rowData;
        }
    }

    public static class EmailInfo {

        public static Properties props = new Properties();
        public static final String PASSWORD = "aDNQSEEzc3R1c3sjNjM5MC8yMX0=";

        private static String host = "smtp.gmail.com";
        private static String user = "robotics.hephaestus@gmail.com";
        private static String port = "465";

        static{
            props.put("mail.smtp.user", user);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.debug", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        public static Properties getProperties(){
            return props;
        }

        public static boolean comparePassword(byte[] input){
            return Base64.encodeBase64(input).equals(PASSWORD.getBytes()); 
        }
    }
}
