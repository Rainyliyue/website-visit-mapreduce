create database if not exists mapreduce_course
    default character set utf8mb4
    collate utf8mb4_unicode_ci;

-- MySQL may be started with --skip-name-resolve on local machines.
-- In that mode, grants for 'localhost' do not work reliably, so this project
-- uses 127.0.0.1 explicitly in both the user host and JDBC URL.
create user if not exists 'mapreduce_user'@'127.0.0.1' identified by 'mapreduce_pwd';
grant all privileges on mapreduce_course.* to 'mapreduce_user'@'127.0.0.1';

-- Some local Windows/Docker/WSL MySQL installations see the Spring Boot client
-- as a bridge/NAT address such as 172.20.0.1 instead of 127.0.0.1.
-- The wildcard host is for this local course project account only.
create user if not exists 'mapreduce_user'@'%' identified by 'mapreduce_pwd';
grant all privileges on mapreduce_course.* to 'mapreduce_user'@'%';
flush privileges;

use mapreduce_course;

drop table if exists source_distribution;
drop table if exists visit_peak;
drop table if exists visit_rank;
drop table if exists analysis_tasks;
drop table if exists users;

create table users (
    id bigint primary key auto_increment,
    username varchar(64) not null unique,
    role varchar(32) not null,
    created_at datetime not null default current_timestamp
);

create table analysis_tasks (
    id bigint primary key auto_increment,
    input_path varchar(512) not null,
    analysis_type varchar(32) not null,
    status varchar(32) not null,
    created_at datetime not null,
    finished_at datetime null,
    message varchar(1000) null
);

create table visit_rank (
    id bigint primary key auto_increment,
    site varchar(128) not null,
    url varchar(512) not null,
    pv bigint not null,
    index idx_rank_pv (pv),
    index idx_rank_site (site)
);

create table visit_peak (
    id bigint primary key auto_increment,
    site varchar(128) not null,
    hour varchar(8) not null,
    pv bigint not null,
    index idx_peak_site_hour (site, hour)
);

create table source_distribution (
    id bigint primary key auto_increment,
    site varchar(128) not null,
    source_type varchar(32) not null,
    source_value varchar(128) not null,
    pv bigint not null,
    index idx_source_site_type (site, source_type)
);

insert into users(username, role) values
('student', 'USER'),
('admin', 'ADMIN');
