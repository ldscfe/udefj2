package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.db.DB
  Purpose  : DB Class
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/2/19   Adam
  1.1        2024/2/28   Adam             merge DB(MySQL,H2)
  1.1.6      2024/10/9   Adam             cnf --> cnfyaml, remove gson

 Usage:
    property: status, info, val, jval, col
    method  : dbconn, execSQL, execute, execStream
    import  : cnfyaml
    pom     :
        <!--mysql-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
        <!--postgresql-->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.2.18</version>
        </dependency>
        <!--h2-->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
        <!-- json -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>${fastjson.version}</version>
        </dependency>

------------------------------------------------------------------------------
*/
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import static com.udf.BASE.*;

public class DB {
    public static final String VERSION = "v1.1.6";

    public static Map<String, String> info = new LinkedHashMap<>();            // info
    public static List<String> col = new ArrayList<>();                        // data set column
    public static Map<Integer, List<String>> val = new LinkedHashMap<>();      // data set result
    public static Map<Integer, Map<String, String>> val2 = new LinkedHashMap<>();    // data set result2
    //public static String jval2 = "";                                           // data set result2(json)
    public static Connection conn;
    public static Statement curr;
    public static ResultSet rs;
    public static int cs = 0;                                                  // rows count
    public static boolean status = false;                                      // db connect status
    public static int MaxRows = 10000;                                          // return MAX of rows count
    // protected & private
    protected static Map<String, String> dbinfo = new LinkedHashMap<>();       // db connect info
    private String dboffset;                                                   // db limit & offset format(from sys.cnf)

    public DB() {
        this("default", "db.yaml");
    }
    public DB(String dbname) {
        this(dbname, "db.yaml");
    }
    public DB(String dbname, String dbcnf) {
        setAll("code,msg,status,cols,cs,secs,tb,te");
        status = false;

        CNFYAML cnf1 = new CNFYAML(dbcnf);
        dbinfo = map2map(cnf1.get(dbname));
        dbconn();
    }
    private void setAll(String str1) {
        String[] s1 = str1.split(",");
        for(int i=0; i<s1.length; i++) {
            info.put(s1[i], "");
        }
    }
    private void setok() {
        info.put("status", "1");
        info.put("code", "0000");
        info.put("msg", "success.");
    }
    private void seterr() {
        info.put("status", "0");
        info.put("code", "1000");
        info.put("msg", "SQL Error.");
    }
    private void setinit() {
        setAll("code,msg,status,cols,cs,secs,tb,te");
        col.clear();
        val.clear();
        val2.clear();
        //jval2 = "";
    }

    public void dbconn() {
        String sDrive, sJdbc, sType, sHost, sPort, sDB, sUser, sPasswd;
        sType = dbinfo.get("type");
        sHost = dbinfo.get("host");
        sPort = dbinfo.get("port");
        sDB = dbinfo.get("db");
        sUser = dbinfo.get("user");
        sPasswd = udes(dbinfo.get("passwd"));

        // get db jdbc info
        CNF cnf1 = new CNF("sys.cnf");
        dboffset = cnf1.get("db_offset." + sType);
        if (dboffset==null)
            dboffset = cnf1.get("db_offset.default");
        if (dboffset==null)
            dboffset = "%sql%";

        cnf1.put(dbinfo, "db_" + sType);

        sDrive = dbinfo.get("drive");
        sJdbc = dbinfo.get("jdbc");

        if (isnull(sJdbc)) sJdbc = "";
        if (!isnull(sHost)) sJdbc = sJdbc.replace("%host%", sHost);
        if (!isnull(sPort)) sJdbc = sJdbc.replace("%port%", sPort);
        if (!isnull(sDB)) sJdbc = sJdbc.replace("%db%", sDB);
        dbinfo.put("jdbc", sJdbc);
        info.putAll(dbinfo);

        // register JDBC Driver
        try {
            Class.forName(sDrive);
        } catch (ClassNotFoundException e) {
            //throw new RuntimeException(e);
            logerror(String.format("Class %s INIT Error: %s", sType, e.toString()));
            return;
        }

        try {
            conn = DriverManager.getConnection(sJdbc, sUser, sPasswd);
        } catch (SQLException e) {
            logerror(String.format("Class %s Connect Error: %s", sType, e.toString()));
            return;
        }
        try {
            curr = conn.createStatement();
        } catch (SQLException e) {
            logerror(String.format("Class %s Create Cursor Error: %s", sType, e.toString()));
            return;
        }

        setok();
        status = true;
    }

    // SQL: select
    public int execSQL(String sSQL) {
        setinit();
        info.put("tb", dt());

        if (!status) {
            logerror("DB Connect Error");
            seterr();
            return 0;
        }

        int i, j;
        try {
            rs = curr.executeQuery(sSQL);
            //System.out.println(rs.getMetaData());

            int ls = rs.getMetaData().getColumnCount();
            // get column name list --> col
            for (j = 1; j <= ls; j++) {
                col.add(rs.getMetaData().getColumnName(j));
            }

            List<String> lval1 = new ArrayList<>();
            Object obj1;
            i = 0;
            while (rs.next()) {
                lval1 = new ArrayList<>();
                for (j = 1; j <= ls; j++) {
                    obj1 = rs.getObject(j);
                    lval1.add(obj1==null?null:obj1.toString());
                }
                val.put(i, lval1);
                // Put col & lval1 To val2
                val2.put(i, list2map(col, lval1));
                if (++i >= MaxRows)
                    break;
            }
            rs.close();
            info.put("cs", "" + i);                  // result count
            info.put("cols", "" + (j - 1));          // column count
        } catch (Exception e) {
            logerror(String.format("Execute SQL Error: %s", e));
            seterr();
            return 0;
        }

        //jval2 = json.toJson(val2);

        setok();
        info.put("te", dt());
        return 1;
    }
    // DDL, insert, delete
    public int execute(String sSQL) {
        setinit();
        info.put("tb", dt());
        if (!status) {
            logerror("DB Connect Error");
            seterr();
            return 0;
        }

        try {
            curr.execute(sSQL);
        }catch (Exception e) {
            logerror(String.format("Execute SQL Error: %s", e));
            seterr();
            return 0;
        }

        setok();
        info.put("te", dt());
        return 1;
    }
/*
 execute SQL to stream rs.
 sample:
        DB sysdb1 = new DB("dorisp158");
        sysdb1.execStream("select sql_id, sql_app, seq from ti_s_sys_sql where 1=1 order by 1,2,3 desc", 15, 6);
        //sysdb1.execStream("select sql_id, sql_app, seq from ti_s_sys_sql where 1=1 order by 1,2,3 desc");

        ArrayList lval1 = new ArrayList();
        Object obj1;
        int i=0;
        try {
            while (sysdb1.rs.next()) {
                i++;
                lval1 = new ArrayList();
                for (int j = 1; j <= sysdb1.col.size(); j++) {
                    obj1 = sysdb1.rs.getObject(j);
                    lval1.add(obj1==null?null:obj1.toString());
                }
                System.out.println(lval1);   // row i
            }
            sysdb1.rs.close();
            System.out.println(i);           // rows

        } catch (Exception e) {
            logger.error(String.format("Execute SQL Error: %s", e));
        }
 */
    public int execStream(String sSQL, int l1, int o1) {
        setinit();
        info.put("tb", dt());
        if (!status) {
            logerror("DB Connect Error");
            seterr();
            return 0;
        }

        // get result set count
        try {
            rs = curr.executeQuery(String.format("select count(*) cs from (%s) o", sSQL));
            rs.next();
            cs = rs.getInt(1);
        } catch (Exception e) {
            logerror(String.format("Execute SQL Error: %s", e));
            seterr();
            return 0;
        }

        // set offset
        if (l1>0) {
            dboffset = dboffset.replace("%sql%", sSQL);
            dboffset = dboffset.replace("%limit%", ""+l1);
            dboffset = dboffset.replace("%offset%", ""+o1);
            sSQL = dboffset;
        }

        int i, j;
        try {
            rs = curr.executeQuery(sSQL);

            int ls = rs.getMetaData().getColumnCount();
            // get column name list --> col
            for (j = 1; j <= ls; j++) {
                col.add(rs.getMetaData().getColumnName(j));
            }
        }catch (Exception e) {
            logerror(String.format("Execute SQL Error: %s", e));
            seterr();
            return 0;
        }

        setok();
        info.put("te", dt());
        return 1;
    }
    public int execStream(String sSQL) {
        return execStream(sSQL, 0, 0);
    }
}
