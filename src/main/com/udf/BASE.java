package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.BASE
  Purpose  : string crypt, hash, datetime, isnull, trim, etc...
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/02/20   Adam
  1.1        2024/02/27   Adam             merge STR
  1.2        2024/02/29   Adam             remove commons-codec, replace util
  1.21       2024/03/20   Adam             Add log, reverse2
  1.22       2024/03/28   Adam             Add isnull, log4j -> slf4j
  1.23       2024/04/09   Adam             Add at(Atomicinteger)
  1.25       2024/04/22   Adam             Add split, rep
  1.26       2024/05/15   Adam             split -> str2list, str2map(Specify separator)

 format:
    object  :
    property: VERSION, json, UDEFLOGOFF, LOOPMAX
    method  : at, isnull, log, dt, hash, md5, base64, des, trim, rep, reverse, str2list, str2map

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
    // Property
    public static final String VERSION = "v1.26.2";
    public static boolean UDEFLOGOFF = false;
    public static String CHARSET = "UTF-8";
    public static int LOOPMAX = 1024;
    public static final Gson json = new Gson();
    // Private
    private static final AtomicInteger _UDEFAT = new AtomicInteger(0);
    static {
        PropertyConfigurator.configure("config/log4j.properties");
    }
    // Method
    public static int at() {
        return _UDEFAT.get();
    }
    public static int at(int flag) {
        switch(flag) {
            case 1:
                return _UDEFAT.getAndIncrement();
            case -1:
                return _UDEFAT.getAndDecrement();
            default :
                _UDEFAT.set(flag);
                return flag;
        }
    }
    // Default "" for unknown types
    public static String type(Object obj1) {
        if (obj1 == null) return "";

        if      (obj1 instanceof String)     return "String";
        else if (obj1 instanceof List)       return "List";
        else if (obj1 instanceof Map)        return "Map";
        else if (obj1 instanceof Properties) return "Properties";
        else                                 return "";
    }
    // Default behavior for unknown types (e.g., return default1[false])
    public static boolean isnull(Object obj1, boolean default1) {
        if (obj1 == null) return true;

        switch (type(obj1)) {
            case "String":
                return ((String)obj1).isEmpty();
            case "List":
                return ((List)obj1).isEmpty();
            case "Map":
                return ((Map)obj1).isEmpty();
            case "Properties":
                return ((Properties)obj1).isEmpty();
            default:
                return default1;
        }
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
        Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
        if (log1==null)
            logger.error("");
        else
            logger.error(log1.toString());
    }
    // get datetime string
    // if l=0, get sysdate, "-:" --> 2024-02-22 00:00:00.000
    // if l>0, get sysdate from 19700101.(l=millisecond)
    public static String dt(String p, int len, long l) {
        if (isnull(p)) p = "";

        SimpleDateFormat df = new SimpleDateFormat();
        if (p.length() == 2)
            df.applyPattern("yyyy"+p.substring(0,1)+"MM"+p.substring(0,1)+"dd HH"+p.substring(1,2)+"mm"+p.substring(1,2)+"ss"+"."+"SSS");
        else
            df.applyPattern("yyyyMMddHHmmssSSS");

        Date date1 = new Date();
        if (l > 0) {
            date1.setTime(l);
        }
        p = df.format(date1);
        len = (len<1 || len>p.length()) ? p.length() : len;

        return p.substring(0, len);
    }
    // get sysdate, "-:" --> 2024-02-22 00:00:00.000
    public static String dt(String p, int len) {
        return dt(p, len, 0);
    }
    public static String dt(String p) {
        return dt(p, 19);
    }
    // if len=0, get secs from 19700101, else dt("", len)
    // if len<100, get sysdate, like dt("", len)
    // if len>=100, get datetime from 19700101 + len(secs), like dt("", 19, len)
    public static String dt(long len) {
        if (len == 0) {
            Date t1 = new Date();
            return String.valueOf(t1.getTime());
        }
        if (len < 100) {
            return dt("", (int)len);
        }

        return dt("", 19, len);
    }
    // get sysdate, 20240222000000
    public static String dt() {
        return dt("", 14);
    }
    // ???? te - tb, msecs
    public static long msecs(String tb, String te) {
        return 0L;
    }
    public static String hash(String s1, String type) {
        if (isnull(s1)) return s1;

        if (isnull(type)) type = "md5";

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
        if (isnull(s1)) return s1;

        try {
            String b64s1 = Base64.getEncoder().encodeToString(s1.getBytes(CHARSET));
            return b64s1;
        } catch (Exception e){
            logerror(e);
            return null;
        }
    }
    public static String ubase64(String s1) {
        if (isnull(s1)) return s1;

        try {
            byte[] b1 = Base64.getDecoder().decode(s1);
            return new String(b1, CHARSET);
        } catch (Exception e){
            logerror(e);
            return null;
        }
    }
    public static String des(String s1, String k1) {
        if (isnull(s1) || isnull(k1))  return s1;

        while (k1.length() < 8){
            logwarn("The parameter is short.");
            k1 += "0";
        }

        String DES_ECB = "DES/ECB/PKCS5Padding";

        try {
            // get key
            DESKeySpec dk1 = new DESKeySpec(k1.getBytes(CHARSET));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey =  keyFactory.generateSecret(dk1);

            Cipher cipher = Cipher.getInstance(DES_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            byte[] b1 = cipher.doFinal(s1.getBytes(CHARSET));
            // JDK1.7 及以下可以使用 BASE64Encoder
            return Base64.getEncoder().encodeToString(b1);
        } catch (Exception e) {
            logerror(e);
            return "";
        }
    }
    public static String udes(String s1, String k1) {
        if (isnull(s1) || isnull(k1))  return s1;

        while (k1.length() < 8) {
            logwarn("The parameter is short.");
            k1 += "0";
        }

        try {
            String DES_ECB = "DES/ECB/PKCS5Padding";
            // get key
            DESKeySpec dk1 = new DESKeySpec(k1.getBytes(CHARSET));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key secretKey =  keyFactory.generateSecret(dk1);

            Cipher cipher = Cipher.getInstance(DES_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new SecureRandom());
            return new String(cipher.doFinal(Base64.getDecoder().decode(s1)), CHARSET);
        } catch (Exception e) {
            logerror(e);
            return "";
        }
    }
    public static String des(String s1) {
        return des(s1, "f0nnlS54jB0");
    }
    public static String udes(String s1) {
        return udes(s1, "f0nnlS54jB0");
    }
    // string(a=1 b=c...) --> map({a=1, b=c, ...})
    // string(a=1 c d=2...) --> map({a=1, 1=c, d=2...})
    // Warn: Not supported key'=' or key"="
    // if ltrim&rtrim is true, trim char from char[] of trim1
    public static Map<String, String> str2map(String src, String sep) {
        if (isnull(src)) return new HashMap<>();

        char sep1, sep2;
        if (isnull(sep)) {
            sep1 = ' ';
            sep2 = '=';
        } else if (sep.length() == 1) {
            sep1 = sep.charAt(0);
            sep2 = '=';
        } else {
            sep1 = sep.charAt(0);
            sep2 = sep.charAt(1);
        }

        Map<String, String> para = new LinkedHashMap<>();
        String s11, key, val;
        String[] s1, s2;

        List<String> ltmp = str2list(src, sep1);
        s1 = ltmp.toArray(new String[ltmp.size()]);

        for (int i=0; i<s1.length; i++) {
            try {
                s11 = s1[i];
                s2 = s11.split(String.valueOf(sep2));
                key = s2[0];
                val = ltrim(s11, key+sep2);
                if (isnull(key)) {
                    key = String.valueOf(i);
                }
                else if (key.equals(s11)) {
                    val = key;
                    key = String.valueOf(i);
                }
                para.put(key, val);
            } catch (Exception e) {
                para.put(String.valueOf(i), s1[i].substring(1));
            }
        }

        return para;
    }
    public static Map<String, String> str2map(String src) {
        return str2map(src, " =");
    }
    // main(String[] args)
    public static Map<String, String> str2map2(String[] src) {
        Map<String, String> para = new LinkedHashMap<>();
        for (int i=0; i<src.length; i++) {
            para.putAll(str2map(src[i]));
        }
        return para;
    }
    // list1, list2 --> map(list1, list2)
    // if list2 < list1, set to null.
    public static Map<String, String> list2map(List<String> ky1, List<String> val1) {
        Map<String, String> map1 = new LinkedHashMap<>();
        for (int i=0; i<ky1.size(); i++) {
            map1.put(ky1.get(i), i>=val1.size()?"":val1.get(i));
        }

        return map1;
    }
    // Splits strings -> List by the specified delimiter
    public static List<String> str2list(String src, final char PC) {
        if (isnull(src)) return new ArrayList<>();

        List<String> res = new ArrayList<>();
        StringBuilder buf1 = new StringBuilder();

        boolean flagd = true, flags = true;
        char c;

        for (int i=0; i<src.length(); i++) {
            c = src.charAt(i);
            if (c =='"' && PC !='"')  flags = !flags;
            if (c =='\'' && PC !='\'')  flagd = !flagd;

            // separator
            if (c == PC) {
                // not ' & ", add list
                if (flagd && flags) {
                    res.add(buf1.toString());
                    buf1.setLength(0);
                }
                // It is not considered a separator
                else {
                    buf1.append(c);
                }
            }
            // not separator
            else {
                buf1.append(c);
            }
        }
        if (!isnull(buf1)) res.add(buf1.toString());

        return res;
    }
    public static List<String> str2list(String src) {
        return str2list(src, ' ');
    }
    //replace string from map
    public static String rep(String src, Map<String, String> m1) {
        if (isnull(src)||isnull(m1)) return src;

        for (String key : m1.keySet()) {
            src = src.replaceAll(String.format("%c%s%c", '%', key, '%'), m1.get(key));
        }

        return src;
    }
    // reverse a string, like: abc --> cba
    public static String reverse(String s1) {
        if (isnull(s1)) return s1;

        char[] cStr1 = s1.toCharArray();
        int len1 = cStr1.length;
        for (int i = 0; i < len1 / 2; i++) {
            char c1 = cStr1[i];
            cStr1[i] = cStr1[len1 - 1 - i];
            cStr1[len1 - 1 - i] = c1;
        }

        return String.valueOf(cStr1);
    }
    public static String reverse2(String s1) {
        if (isnull(s1)) return s1;

        StringBuffer sf1 = new StringBuffer(128);
        sf1.append(s1);

        return sf1.reverse().toString();
    }

    // left remove all substring
    public static String ltrim(String src, String rep) {
        return ltrim(src, rep, LOOPMAX);
    }
    // left remove i substrings
    public static String ltrim(String src, String rep, int i) {
        if (i <= 0 || isnull(src) || isnull(rep)) return src;

        for (int j=0; j<i; j++) {
            if (src.indexOf(rep) == 0) {
                src = src.substring(rep.length());
            } else {
                break;
            }
        }

        return src;
    }
    // right remove all substring
    public static String rtrim(String src, String rep) {
        return rtrim(src, rep, LOOPMAX);
    }
    // right remove i substrings
    public static String rtrim(String src, String rep, int i) {
        if (i <= 0 || isnull(src) || isnull(rep)) return src;

        String rsrc, rrep;
        rsrc = reverse(src);
        rrep = reverse(rep);

        return reverse(ltrim(rsrc, rrep, i));
    }

    // left & right remove all substring
    public static String trim(String src, String rep) {
        return rtrim(ltrim(src, rep), rep);
    }
    // left & right remove i substring
    public static String trim(String src, String rep, int i) {
        return rtrim(ltrim(src, rep, i), rep, i);
    }
}