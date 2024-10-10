package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.CNFYAML
  Purpose  : get config from yaml
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/10/7   Adam             Create.
  1.0.1      2024/10/7   Adam             new Yaml() --> new Yaml(new SafeConstructor()), resolve CVE-2022-1471.
  1.0.2      2024/10/9   Adam             Resolve the key is int using gson

  Usage:
    property:
    method  : get
    pom     :
        <!-- yaml -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>1.25</version>
        </dependency>
        <!-- Json -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.9</version>
        </dependency>

------------------------------------------------------------------------------
*/
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class CNFYAML {
    public static final String VERSION = "v1.0.2";
    public static boolean status = false;
    public static String msg = "";

    private static Map<String, Object> val;

    public CNFYAML() {
        this("default.yaml");
    }
    public CNFYAML(String sFile) {
        // get config file name, if not path, default ./config/
        String sPath;
        if (sFile.indexOf('/') >= 0)
            sPath = System.getProperty("user.dir") + "/" + sFile;
        else
            sPath = System.getProperty("user.dir") + "/config/" + sFile;

        try {
            Yaml yaml = new Yaml(new SafeConstructor());
            FileInputStream inputStream = new FileInputStream(sPath);
            val = yaml.load(inputStream);
            status = true;
        } catch (Exception e) {
            msg = e.toString();
        }
    }

    public Map<String, Object> get() {
        if (val == null) return new HashMap<>();
        return val;
    }
    public Map<String, Object> get(String key) {
        Gson json = new Gson();
        if (val == null) return new HashMap<>();
        if (key == "") return get();
        if (val.get(key) == null) return new HashMap<>();

        try {
            //return (Map<String, Object>)val.get(key);
            return json.fromJson(json.toJson(val.get(key)), new TypeToken<Map<String, Object>>(){}.getType());
        } catch (Exception e) {
            return Map.of(key, (Object)val.get(key));
        }
    }
}
