package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.BASE
  Purpose  : string crypt & decrypt, datetime
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/02/20   Adam
  1.1        2024/02/27   Adam             merge STR
  1.2        2024/02/29   Adam             remove commons-codec, replace util
  1.21       2024/03/20   Adam             Add log, reverse2
  1.22       2024/03/28   Adam             Add isnull, log4j --> slf4j
  1.23       2024/04/09   Adam             Add at(Atomicinteger)
 format:
    object  :
    property: json, UDEFLOGOFF
    method  : isnull, log, dt, hash, md5, base64, des, ltrim, rtrim, trim, reverse, at

        <!--Json-->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.9</version>
        </dependency>
        <!--log4j-->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
        <!--slf4j-->
        <!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-api -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>

------------------------------------------------------------------------------
*/

import com.google.gson.Gson;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Int;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BASE {
    public static final String VERSION = "v1.23";
    private static AtomicInteger _UDEFAT = new AtomicInteger(0);
    static {
        PropertyConfigurator.configure("config/log4j.properties");
    }
    public static Gson json = new Gson();
    public static boolean UDEFLOGOFF = false;
    public static boolean isnull(Object obj1, boolean default1) {
        if (obj1 == null) return true;

        if      (obj1 instanceof String)     { return ((String)     obj1).isEmpty(); }
        else if (obj1 instanceof List)       { return ((List)       obj1).isEmpty(); }
        else if (obj1 instanceof Map)        { return ((Map)        obj1).isEmpty(); }
        else if (obj1 instanceof Properties) { return ((Properties) obj1).isEmpty(); }
        else                                 { return default1; }
        // Default behavior for unknown types (e.g., return false)
    }
    public static boolean isnull(Object obj1) {
        return isnull(obj1, false);
    }
    public static void log(Object log1) {
        if (UDEFLOGOFF) return;
        Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
        if (log1==null)
            logger.info("");
        else
            logger.info(log1.toString());
    }
    public static void logwarn(Object log1) {
        if (UDEFLOGOFF) return;
        Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
        if (log1==null)
            logger.warn("");
        else
            logger.warn(log1.toString());
    }
    public static void logerror(Object log1) {
        if (UDEFLOGOFF) return;
        Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
        if (log1==null)
            logger.error("");
        else
            logger.error(log1.toString());
    }
    // get sysdate, "-:" --> 2024-02-22 00:00:00.000
    public static String dt(String p, int len) {
        SimpleDateFormat df = new SimpleDateFormat();
        if (p==null||p.length() <2)
            df.applyPattern("yyyyMMddHHmmssSSS");
        else
            df.applyPattern("yyyy"+p.substring(0,1)+"MM"+p.substring(0,1)+"dd HH"+p.substring(1,2)+"mm"+p.substring(1,2)+"ss"+"."+"SSS");

        Date date = new Date();
        p = df.format(date);
        len = (len<1||len>p.length())?p.length():len;
        return p.substring(0, len);
    }
    public static String dt(String p) {
        return dt(p, 0);
    }
    public static String dt(int len) {
        return dt(null, len);
    }
    // get sysdate, 20240222000000
    public static String dt() {
        return dt(14);
    }
    public static String hash(String s1, String type) {
        if ((s1==null)||(s1.equals(""))) {
            return "";
        }
        if ((type==null)||(type.equals(""))) {
            type = "md5";
        }
        String hash1 = "";
        try {
            MessageDigest md = MessageDigest.getInstance(type);
            md.update(s1.getBytes());
            hash1 = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            logerror(e);
        }
        return hash1;
    }
    public static String md5(String s1) {
        return hash(s1, "md5");
    }
    public static String base64(String s1) {
        if ((s1==null)||(s1.equals(""))) {
            return "";
        }
        try {
            String b64s1 = Base64.getEncoder().encodeToString(s1.getBytes("utf-8"));
            return b64s1;
        } catch (Exception e){
            logerror(e);
            return null;
        }
    }
    public static String ubase64(String s1) {
        if ((s1==null)||(s1.equals(""))) {
            return "";
        }
        try {
            byte[] b1 = Base64.getDecoder().decode(s1);
            return new String(b1, "utf-8");
        } catch (Exception e){
            logerror(e);
            return null;
        }
    }
    public static String des(String s1, String k1) {
        if (s1 == null) {
            log("The parameter is NULL.");
            return null;
        }
        while (k1.length() < 8){
            log("The parameter is short.");
            k1 += "0";
        }

        String DES_ECB = "DES/ECB/PKCS5Padding";
        String Charset = "UTF-8";

        try {
            // get key
            DESKeySpec dk1 = new DESKeySpec(k1.getBytes(Charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey =  keyFactory.generateSecret(dk1);

            Cipher cipher = Cipher.getInstance(DES_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            byte[] b1 = cipher.doFinal(s1.getBytes(Charset));
            // JDK1.7 及以下可以使用 BASE64Encoder
            return Base64.getEncoder().encodeToString(b1);
        } catch (Exception e) {
            logerror(e);
            return null;
        }
    }
    public static String udes(String s1, String k1) {
        if (s1 == null) {
            log("The parameter is NULL.");
            return null;
        }
        while (k1.length() < 8) {
            log("The parameter is short.");
            k1 += "0";
        }

        try {
            String DES_ECB = "DES/ECB/PKCS5Padding";
            String Charset = "UTF-8";
            // get key
            DESKeySpec dk1 = new DESKeySpec(k1.getBytes(Charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey =  keyFactory.generateSecret(dk1);

            Cipher cipher = Cipher.getInstance(DES_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new SecureRandom());
            return new String(cipher.doFinal(Base64.getDecoder().decode(s1)), Charset);
        } catch (Exception e) {
            logerror(e);
            return null;
        }
    }
    public static String des(String s1) {
        return des(s1, "DeiPd8ltuAE3");
    }
    public static String udes(String s1) {
        return udes(s1, "DeiPd8ltuAE3");
    }
    // String(a=1 b=c...) --> map({a=1, b=c, ...})
    public static HashMap<String, String> str2map (String[] s1) {
        HashMap<String, String> para = new HashMap<>();
        String s11, key, val;
        String[] s2;
        for (int i=0; i<s1.length; i++) {
            try {
                s11 = s1[i];
                s2 = s11.split("=");
                key = s2[0];
                val = ltrim(s11, key+"=");
                if (null == key || "".equals(key)) {
                    key = "" + i;
                }
                else if (key.equals(s11)) {
                    val = key;
                    key = "" + i;
                }
                para.put(key, val);
            } catch (Exception e) {
                para.put("" + i, s1[i].substring(1));
            }
        }
        return para;
    }
    // list1, list2 --> map(list1, list2)
    // if list2 < list1, set to null.
    public static HashMap<String, String> list2map(ArrayList<String> ky1, ArrayList<String> val1) {
        HashMap<String, String> map1 = new HashMap<>();
        for (int i=0; i<ky1.size(); i++) {
            map1.put(ky1.get(i), i>=val1.size()?null:val1.get(i));
        }
        return map1;
    }
    // reverse a string, like: abc --> cba
    public static String reverse(String s1) {
        if ((s1==null)||(s1.equals(""))) {
            return "";
        }
        char[] cStr1 = s1.toCharArray();
        int len1 = cStr1.length;
        for (int i = 0; i < len1 / 2; i++) {
            char c1 = cStr1[i];
            cStr1[i] = cStr1[len1 - 1 - i];
            cStr1[len1 - 1 - i] = c1;
        }
        String rStr1 = new String(cStr1);
        return rStr1;
    }
    public static String reverse2(String s1) {
        if ((s1==null)||(s1.equals(""))) {
            return "";
        }
        StringBuffer sf1 = new StringBuffer(128);
        sf1.append(s1);
        return sf1.reverse().toString();
    }

    // left remove a substring
    public static String ltrim(String str1, String str2) {
        if (str1 == null) return str1;
        if (str1.indexOf(str2) == 0)
            return str1.substring(str2.length());
        return str1;
    }
    // left remove i substrings
    public static String ltrim(String str1, String str2, int i) {
        String s1, s2;
        if (i == 0)
            i = 1024;
        s1 = str1;
        s2 = str1;
        for (int j=0; j<i; j++) {
            s2 = ltrim(s1, str2);
            if (s1 == s2)
                break;
            s1 = s2;
        }
        return s2;
    }

    // right remove a substring
    public static String rtrim(String str1, String str2) {
        return rtrim(str1, str2, 1);
    }
    // right remove i substrings
    public static String rtrim(String str1, String str2, int i) {
        String rstr1, rstr2, s1, s2;
        rstr1 = reverse(str1);
        rstr2 = reverse(str2);

        if (i == 0)
            i = 1024;
        s1 = rstr1;
        s2 = rstr1;
        for (int j=0; j<i; j++) {
            s2 = ltrim(s1, rstr2);
            if (s1 == s2)
                break;
            s1 = s2;
        }
        return reverse(s2);
    }

    // left & right remove a substring
    public static String trim(String str1, String str2) {
        return rtrim(ltrim(str1, str2), str2);
    }
    // left & right remove i substring
    public static String trim(String str1, String str2, int i) {
        return rtrim(ltrim(str1, str2, i), str2, i);
    }
    public static int at(int flag) {
        switch(flag) {
            case 1:
                return _UDEFAT.getAndIncrement();
            case -1:
                return _UDEFAT.getAndDecrement();
            case 0:
                return _UDEFAT.get();
            default :
                _UDEFAT.set(flag);
                return flag;
        }
    }
}
