package com.course.mapreduce.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                create table if not exists access_logs (
                    id bigint primary key auto_increment,
                    visit_date varchar(16) not null,
                    visit_time varchar(16) not null,
                    site varchar(128) not null,
                    url varchar(512) not null,
                    target_address varchar(640) not null,
                    ip varchar(64) not null,
                    region varchar(64) not null,
                    status varchar(16) not null,
                    bytes bigint not null,
                    index idx_access_logs_time (visit_date, visit_time),
                    index idx_access_logs_site (site),
                    index idx_access_logs_ip (ip),
                    index idx_access_logs_region (region)
                )
                """);
    }
}
