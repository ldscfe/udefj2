package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.CNF
  Purpose  : get config
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/2/7    Adam

 format:
    property:
    method  : get, put
------------------------------------------------------------------------------
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class CNF extends BASE {
    public static final String VERSION = "v1.0.1";
    private static final Properties dCnf = new Properties();
    public CNF(String sFile) {
        // get config, if not path, default ./config/
        String sPath;
        if (sFile.indexOf('/') >= 0)
            sPath = sFile;
        else
            sPath = System.getProperty("user.dir") + "/config/" + sFile;

        // config file contents --> dCnf(Properties)
        try {
            File fFile = new File(sPath);
            if (!fFile.isFile()) {
                //throw new RuntimeException("File Read Error." + sFile);
                logerror(String.format("File %s Read Error.", sFile));
            }else {
                InputStream is = new FileInputStream(fFile);
                dCnf.load(is);
                is.close();
            }
        } catch (Exception e) {
            logerror(String.valueOf(e));
        }
        //System.out.println(dCnf);
    }

    public Properties get() {
        return dCnf;
    }
    public String get(String key) {
        return dCnf.getProperty(key);
    }
    // put {sKey}{sKeyp}* Properties --> map
    public void put(Map<String, String> mKV, String sKey, String sKeyp) {
        String str1;
        for (String key : dCnf.stringPropertyNames()) {
            if (key.indexOf(sKey+sKeyp) == 0) {
                str1 = ltrim(key, sKey);
                str1 = ltrim(str1, sKeyp);
                mKV.put(str1, dCnf.getProperty(key));
            }
        }
    }
    // default: item --> item.
    public void put(Map<String, String> mKV, String sKey) {
        put(mKV, sKey, ".");
    }
}
