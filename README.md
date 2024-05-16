# udefj2
Java UDEF Class

------------------------------------------------------------------------------
----
Name     : Udf.base.BASE

property: VERSION, json, UDEFLOGOFF, LOOPMAX

method  : at, isnull, log, dt, hash, md5, base64, des, trim, rep, reverse, split

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

        String dbname, sql;
        dbname = "mysqlp83";
        DB db1 = new DB(dbname);
        sql = "select * from test";
        db1.execute(sql);
        log(db1.val2);

Sample   : 002 - sql from file or DB

Sample   : 003 - multiple data sources servers in db.cnf

         // multiple hosts in the db.cnf (.host=ip1,ip2...), and randomly connect to one of the available server


------------------------------------------------------------------------------
----
Name     : Udf.base.KAFKA

------------------------------------------------------------------------------
----