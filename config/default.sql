tidat_task_identiry.1=select sql_id, sql_app, seq, sql_content, mm from ti_s_sys_sql_new where sql_id = 'ti_w_busi_identity_query' and sql_app = 'A00' and seq > 0 order by sql_id, sql_app, seq
tidat_task_identiry.2=select * from ti_s_sys_sql_new where sql_id = '%sqlid%' and sql_app = '%sqlapp%' and seq > 0 order by sql_id, sql_app, seq

# h2_test
h2_test.1=Create table test (ky int primary key, val varchar(20))
h2_test.2=Insert into test (ky, val) values (1, 'Hello, World!')
h2_test.21=Insert into test (ky, val) values (2, 'Hello, DB.')
h2_test.3=select * from test
h2_test.4=select count(*) from test
