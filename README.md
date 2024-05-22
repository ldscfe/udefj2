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
    2024-05-22 13:49:50,902 INFO [testbase] ==== slf4j-log4j ====
    2024-05-22 13:49:50,902 INFO [testbase] Hello, World!
    2024-05-22 13:49:50,903 WARN [testbase] Hello, World!
    2024-05-22 13:49:50,903 ERROR [testbase] Hello, World!

Sample   : 2 - dt - datetime

        log("==== datetime ====");
        logwarn(dt());
        log(dt("/:"));
        log(dt(8));
        log(dt("-:", 19));

    // -- output --
    2024-05-22 13:56:05,825 WARN [testbase] 20240522135605
    2024-05-22 13:56:05,860 INFO [testbase] 2024/05/22 13:56:05
    2024-05-22 13:56:05,861 INFO [testbase] 20240522
    2024-05-22 13:56:05,861 INFO [testbase] 2024-05-22 13:56:05

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
    2024-05-22 14:02:01,412 INFO [testbase] ==== hash ====
    2024-05-22 14:02:01,412 INFO [testbase]
    2024-05-22 14:02:01,489 INFO [testbase] fcff297b5772aa6d04967352c5f4eb96
    2024-05-22 14:02:01,490 INFO [testbase] fcff297b5772aa6d04967352c5f4eb96
    2024-05-22 14:02:01,491 INFO [testbase] dd0588c172986c32636ffdd8cc690de7b41bf253
    2024-05-22 14:02:01,727 ERROR [com.udf.BASE] java.security.NoSuchAlgorithmException: sha2 MessageDigest not available
    2024-05-22 14:02:01,729 INFO [testbase]
    2024-05-22 14:02:01,731 INFO [testbase] 4aa5d2533987c34839e8dbc8d8fcac86f0137e31c1c6ea4349ade4fcaf87ed8
    2024-05-22 14:02:01,737 INFO [testbase] c0d0df8be7405b0cdb12df4d674d64ebed62207ffe118ee5ee9d33071af4abf383d6efa2b56450e1475971e7e9105629c11ad855b08e17e9fbc6584c08403990
    2024-05-22 14:02:01,738 INFO [testbase]
    2024-05-22 14:02:01,741 INFO [testbase] aGVsbG8sIFdvcmxkIQ==
    2024-05-22 14:02:01,741 INFO [testbase] hello, World!
    2024-05-22 14:02:01,841 INFO [testbase] ZJ49zumHoYtt9oGaSeJWeQ==
    2024-05-22 14:02:01,843 INFO [testbase] hello, World!

Sample   : 4 - trim - ltrim, rtrim, trim

        log("==== trim ====");
        log(ltrim("==== isnull ====", "="));
        log(rtrim("==== isnull ====", "="));
        log(trim("==== isnull ====", "==="));

    // -- output --
    2024-05-22 14:09:02,657 INFO [testbase] ==== trim ====
    2024-05-22 14:09:02,657 INFO [testbase]  isnull ====
    2024-05-22 14:09:02,657 INFO [testbase] ==== isnull
    2024-05-22 14:09:02,657 INFO [testbase] = isnull =

Sample   : 5 - rep - replace string from map


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

Sample   : 001

        DB db1 = new DB("mysqlp83");
        db1.execute("select * from test");
        log(db1.val2);

Sample   : 002 - sql from file or DB

Sample   : 003 - multiple data sources servers in db.cnf

         // multiple hosts in the db.cnf (.host=ip1,ip2...), and randomly connect to one of the available server


------------------------------------------------------------------------------
----
Name     : Udf.base.KAFKA

------------------------------------------------------------------------------
----