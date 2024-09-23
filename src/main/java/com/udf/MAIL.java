package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.MAIL
  Purpose  : send mail
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.28       2024/09/12  Adam             Create

 format:
    object  :
    property: MSG_TYPE(charset;...)
    method  : send<MAP>

        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>jakarta.mail</artifactId>
            <version>2.0.1</version>
        </dependency>

------------------------------------------------------------------------------
*/
import java.util.*;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import static com.udf.BASE.log;
import static com.udf.BASE.str2list;

public class MAIL {
    public static final String VERSION = "v1.0.0";
    public static String _TYPE = "text/html;charset=UTF-8";

    private static Properties mailSERV = new Properties();
    private static Session session;
    private static Transport ts;

    private static boolean _DEBUG = false;

    public MAIL(String sFile) {
        String host, user, password;
        try {
            CNF cnf1 = new CNF(sFile);
            mailSERV = cnf1.get();

            host = mailSERV.getProperty("host");
            user = mailSERV.getProperty("user");
            password = mailSERV.getProperty("password");

            session = Session.getInstance(mailSERV);
            if (_DEBUG) session.setDebug(true);
            ts = session.getTransport();
            ts.connect(host, user, password);
        } catch (Exception e) {
            log(e);
        }
    }
    public MAIL() {
        this("email.cnf");
    }

    public static int send(Map<String, String> _mail) {
        MimeMessage message;
        String s_from, s_to, s_subject, s_content;
        List<String> l_to;

        // default from = mail.user
        if (_mail.containsKey("from")) {
            s_from = _mail.get("from");
        } else {
            s_from = mailSERV.getProperty("user");
        }

        // to is list
        s_to = _mail.get("to").replace(";", ",");
        l_to = str2list(s_to, ',');
        s_subject = _mail.get("subject");
        s_content = _mail.get("content");
        try {
            message = new MimeMessage(session);
            message.setFrom(new InternetAddress(s_from));
            message.setSubject(s_subject);
            message.setContent(s_content, _TYPE);

            for (String to : l_to) {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                ts.sendMessage(message, message.getAllRecipients());
            }
            ts.close();
        } catch (Exception e) {
            log(e);
            return -1;
        }
        return 0;
    }
}