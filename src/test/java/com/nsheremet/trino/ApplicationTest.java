package com.nsheremet.trino;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author nsheremet
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest

public class ApplicationTest {
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void deduplicates() {
    extracted("CREATE SCHEMA IF NOT EXISTS iceberg.temp");
    extracted("CREATE TABLE iceberg.temp.ns_mytable (\n"
        + "   mc varchar,\n"
        + "   ve decimal(38, 2),\n"
        + "   sd bigint,\n"
        + "   cd bigint,\n"
        + "   cgd integer,\n"
        + "   sp varchar,\n"
        + "   an varchar,\n"
        + "   ie varchar,\n"
        + "   vn varchar,\n"
        + "   br varchar,\n"
        + "   de varchar,\n"
        + "   oy varchar,\n"
        + "   wso varchar,\n"
        + "   dde date\n"
        + ")\n"
        + "WITH (\n"
        + "   format = 'PARQUET',\n"
        + "   format_version = 2,\n"
        + "   location = 's3a://mybucket/temp/ns_mytable'\n"
        + ")");

    extracted("CREATE TABLE generator.default.ns_mytable (\n"
            + " mc varchar WITH (generator = '#{Name.first_name}_#{Name.last_name}'),\n"
            + " ve decimal(38, 2),\n"
            + " sd bigint,\n"
            + " cd bigint,\n"
            + " cgd integer ,\n"
            + " sp varchar WITH (generator = '#{Currency.code}'),\n"
            + " an varchar WITH (generator = '#{Currency.code}'),\n"
            + " ie varchar WITH (generator = '#{Currency.code}'),\n"
            + " vn varchar WITH (generator = '#{Currency.code}'),\n"
            + " br varchar WITH (generator = '#{Currency.code}'),\n"
            + " de varchar WITH (generator = '#{Currency.code}'),\n"
            + " oy varchar WITH (generator = '#{Currency.code}'),\n"
            + " wso varchar WITH (generator = '#{Currency.code}'),\n"
            + " dde date )\n"
        );
    extracted("INSERT INTO iceberg.temp.ns_mytable\n"
        + "SELECT *\n"
        + "FROM generator.default.ns_mytable\n"
        + "where 1=1\n"
        + "and ve between 1.0 and 200.0\n"
        + "and sd between 1 and 9000\n"
        + "and cd between 1 and 1000000\n"
        + "and cgd between 1 and 1000000\n"
        + "and dde between date '2020-01-01' and date '2025-01-01'\n"
        + "LIMIT 19481297");
//    extracted("ALTER TABLE  iceberg.temp.ns_mytable EXECUTE optimize(file_size_threshold  => '1GB') ");

    extracted("CALL clickhouse.system.execute(query => 'CREATE DATABASE IF NOT EXISTS dev ON CLUSTER testcluster')");
    extracted("CALL clickhouse.system.execute(query => 'CREATE TABLE dev.ns_mytable_local on cluster testcluster\n"
        + "(\n"
        + "  `mc` Nullable(String),\n"
        + "  `ve` Nullable(Float64),\n"
        + "  `sd` Nullable(Int64),\n"
        + "  `cd` Nullable(Int64),\n"
        + "  `cgd` Nullable(Int32),\n"
        + "  `sp` Nullable(String),\n"
        + "  `an` Nullable(String),\n"
        + "  `ie` Nullable(String),\n"
        + "  `vn` Nullable(String),\n"
        + "  `br` Nullable(String),\n"
        + "  `de` Nullable(String),\n"
        + "  `oy` Nullable(String),\n"
        + "  `wso` Nullable(String),\n"
        + "  `dde` Nullable(Date),\n"
        + "  `updated_at` DateTime,\n"
        + "  `interval_start` DateTime\n"
        + ")\n"
        + "  ENGINE = ReplicatedReplacingMergeTree(''/clickhouse/tables/testcluster/dev/ns_mytable_local'', ''{replica}'')\n"
        + "    ORDER BY (dde, mc, cd, wso, sp, ie, vn, br, de, oy)\n"
        + "    SETTINGS allow_nullable_key = 1, index_granularity = 8192;')");

    extracted("CALL clickhouse.system.execute(query => 'CREATE TABLE dev.ns_mytable\n"
        + "(\n"
        + "  `mc` Nullable(String),\n"
        + "  `ve` Nullable(Float64),\n"
        + "  `sd` Nullable(Int64),\n"
        + "  `cd` Nullable(Int64),\n"
        + "  `cgd` Nullable(Int32),\n"
        + "  `sp` Nullable(String),\n"
        + "  `an` Nullable(String),\n"
        + "  `ie` Nullable(String),\n"
        + "  `vn` Nullable(String),\n"
        + "  `br` Nullable(String),\n"
        + "  `de` Nullable(String),\n"
        + "  `oy` Nullable(String),\n"
        + "  `wso` Nullable(String),\n"
        + "  `dde` Nullable(Date),\n"
        + "  `updated_at` DateTime,\n"
        + "  `interval_start` DateTime\n"
        + ")\n"
        + "  ENGINE = Distributed(''testcluster'', ''dev'', ''ns_mytable_local'')')");

    extracted("CALL clickhouse.system.execute(query => 'truncate table dev.ns_mytable_local ON CLUSTER ''testcluster''')");

    extracted( "INSERT INTO clickhouse.dev.ns_mytable\n"
        + "SELECT\n"
        + "  mc \n"
        + ", ve \n"
        + ", sd \n"
        + ", cd \n"
        + ", cgd \n"
        + ", sp \n"
        + ", an \n"
        + ", ie \n"
        + ", vn \n"
        + ", br \n"
        + ", de \n"
        + ", oy \n"
        + ", wso \n"
        + ", dde \n"
        + ", current_timestamp(6) AS updated_at\n"
        + ", cast(from_iso8601_timestamp('2025-01-20T00:00:00+00:00') as timestamp(6)) as interval_start\n"
        + "FROM\n"
        + "  temp.ns_mytable\n"
        + "");

  }

  private void extracted(String sql) {
    jdbcTemplate.execute(sql);
    log.info("SQL executed: {}", sql);
  }
}