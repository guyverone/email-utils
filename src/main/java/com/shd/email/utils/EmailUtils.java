package com.shd.email.utils;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by suhd on 2016-08-24.
 */
public class EmailUtils {
    private String mailSettingPath;
    private String msgTextPath;
    private String subjectPath;
    private String prefixSubject;
    private Properties props = new Properties();
    private Session session;
    private MimeMessage msg;

    public static void main(String[] args) throws Exception{
        EmailUtils eu = new EmailUtils();
        try {
            eu.checkPropertiesValidate(args);
            eu.assembleBasicEmailElement();
            eu.sendEmail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPropertiesValidate(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println("Usage: jar email-utils.jar <path of jdwEmail.properties> <path of jdwEmail.*> <path of subject> <prefix of subject>");
            System.exit(0);
        }
        this.mailSettingPath = args[0];
        this.msgTextPath = args[1];
        this.subjectPath = args[2];
        this.prefixSubject = args[3];
        if(this.mailSettingPath!=null && !"".equals(this.mailSettingPath)) {
            String[] separatedPathWords = this.mailSettingPath.split("/");
            if(!separatedPathWords[separatedPathWords.length-1].equals("jdwEmail.properties")) {
                throw new Exception("path of jdwEmail.properties is required");
            }
        }
        if(this.msgTextPath==null || "".equals(this.msgTextPath)) {
            throw new Exception("path of jdwEmail.* is required");
        }
        if(this.subjectPath ==null || "".equals(this.subjectPath)) {
            throw new Exception("path of subject is required");
        }
    }

    private void assembleBasicEmailElement() throws Exception {
        InputStream isr = new FileInputStream(this.mailSettingPath);
        this.props.load(isr);
        isr.close();
        this.session = Session.getInstance(this.props,null);
        this.msg = new MimeMessage(this.session);
        Address[] from = this.ConvertStringToAddress(this.props.getProperty("from"));
        this.msg.addFrom(from);
        Address[] toPublic = this.ConvertStringToAddress(this.props.getProperty("toPublic"));
        //to public
        this.msg.addRecipients(Message.RecipientType.TO, toPublic);
        Address[] toCopy = this.ConvertStringToAddress(this.props.getProperty("toCopy"));
        //to copy
        this.msg.addRecipients(Message.RecipientType.CC, toCopy);
        String[] lines = this.getText(this.subjectPath).split("\n");
        this.msg.setSubject(this.prefixSubject + (lines.length>0 ? lines[0] : ""));
        this.msg.setSentDate(new Date());
        this.msg.setText(this.getText(this.msgTextPath));
    }

    private Address[] ConvertStringToAddress(String line) throws Exception {
        String[] addressWords = line.split(",");
        Address[] address = new InternetAddress[addressWords.length];
        for(int i=0; i<addressWords.length; i++) {
            address[i] = new InternetAddress(addressWords[i]);
        }
        return address;
    }

    private String getText(String path) throws Exception {
        InputStreamReader isr =new InputStreamReader(new FileInputStream(path),"UTF-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    private void sendEmail() throws Exception {
        SMTPTransport transport = (SMTPTransport)this.session.getTransport("smtp");
        transport.connect(this.props.get("mail.smtp.username").toString(), this.props.get("mail.smtp.password").toString());
        transport.sendMessage(this.msg, this.msg.getAllRecipients());
        transport.close();
    }

}
