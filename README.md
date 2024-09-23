# udefj2
Java UDEF Class

------------------------------------------------------------------------------
----
Name     : Udf.base.BASE

property: VERSION, json, UDEFLOGOFF, LOOPMAX

method  : log, dt, hash, md5, base64, des, trim, reverse, rep, split, at, isnull

Sample   : 1 - log - Output information

        //UDEFLOGOFF = true;
        log("==== slf4j-log4j ====");
        log("Hello, World!");
        logwarn("Hello, World!");
        logerror("Hello, World!");

    // -- output --
    ==== slf4j-log4j ====
    Hello, World!
    Hello, World!
    Hello, World!

Sample   : 2 - dt - datetime

        log("==== datetime ====");
        logwarn(dt());
        log(dt("/:"));
        log(dt(8));
        log(dt("-:", 19));

    // -- output --
    20240522135605
    2024/05/22 13:56:05
    20240522
    2024-05-22 13:56:05

Sample   : 3 - hash - md5, sha1, sha256, base64, des...

        log("==== hash ====");
        log(md5(null));
        log(md5("hello, World!"));
        log(hash("hello, World!", null));
        log(hash("hello, World!", "sha1"));
        log(hash("hello, World!", "sha2"));   // "", error: sha2 MessageDigest not available
        log(hash("hello, World!", "sha256"));
        log(hash("hello, World!", "sha512"));
        log(base64(null));
        log(base64("hello, World!"));
        log(ubase64("aGVsbG8sIFdvcmxkIQ=="));
        log(des("hello, World!"));
        log(udes("ZJ49zumHoYtt9oGaSeJWeQ"));

    // -- output --
    ==== hash ====
    
    fcff297b5772aa6d04967352c5f4eb96
    fcff297b5772aa6d04967352c5f4eb96
    dd0588c172986c32636ffdd8cc690de7b41bf253
    java.security.NoSuchAlgorithmException: sha2 MessageDigest not available
    
    4aa5d2533987c34839e8dbc8d8fcac86f0137e31c1c6ea4349ade4fcaf87ed8
    c0d0df8be7405b0cdb12df4d674d64ebed62207ffe118ee5ee9d33071af4abf383d6efa2b56450e1475971e7e9105629c11ad855b08e17e9fbc6584c08403990    

    aGVsbG8sIFdvcmxkIQ==
    hello, World!
    ZJ49zumHoYtt9oGaSeJWeQ==
    hello, World!

Sample   : 4 - trim - ltrim, rtrim, trim

        log("==== trim ====");
        log(ltrim("==== isnull ====", "="));
        log(rtrim("==== isnull ====", "="));
        log(trim("==== isnull ====", "==="));

    // -- output --
    ==== trim ====
     isnull ====
    ==== isnull
    = isnull =

Sample   : 5 - rep - replace string from map

Sample   : 6 - arr2map - arguments array to map

    String[] a1 = {"--host", "1", "-p", "3306", "$ip", "127.0.0.1", "$$cmd", "'split from tab'", "--", "test null key"};
    log(arr2map(a1, "- -- $$ $"));
    log(arr2map(a1, null));

    // -- output --
    {host=1, p=3306, ip=127.0.0.1, cmd='split from tab', 8=test null key}
    {host=1, p=3306, 4=$ip, 5=127.0.0.1, 6=$$cmd, 7='split from tab', 8=test null key}

------------------------------------------------------------------------------
----
Name     : Udf.base.CNF

property:

method  : get, put

------------------------------------------------------------------------------
----
Name     : Udf.db.DB

property: status, info, col/val, val2, jval2

method  : execSQL, execute, execStream

DB       : MySQL(Doris), postgreSQL(Greenplum), H2

Sample   : 001

        DB db1 = new DB("postgrep182");
        db1.execute("select * from test");
        log(db1.jval2);

    // -- output --
    {"0":{"ky":"1","val":"Hello, World!"},"1":{"ky":"2","val":"Hello, DB."}}

Sample   : 002 - sql from file or DB

Sample   : 003 - multiple data sources servers in db.cnf

         // multiple hosts in the db.cnf (.host=ip1,ip2...), and randomly connect to one of the available server


------------------------------------------------------------------------------
----
Name     : Udf.base.KAFKA

------------------------------------------------------------------------------
----