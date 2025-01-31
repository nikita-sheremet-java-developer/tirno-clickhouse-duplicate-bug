# Run
1) strat docker-compose
   1) ClickHouse containers should be run again after docker-compose manually (Can not understand this behavior) 
2) Run `com.nsheremet.trino.ApplicationTest.deduplicates` it will create and populate tables
3) Run sql
```
   select count(*) as total, 'iceberg' as catalog from iceberg.temp.ns_mytable
   union all
   select count(*) as total, 'clickhouse' as catalog from clickhouse.dev.ns_mytable
```
It should return `19481297` for both tables but it returns:

| total | catalog |
| :--- | :--- |
| 38962594 | clickhouse |
| 19481297 | iceberg |

So `ClickHouse` has twice more data then `Trino`