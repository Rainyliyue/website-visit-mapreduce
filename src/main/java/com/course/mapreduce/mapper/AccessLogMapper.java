package com.course.mapreduce.mapper;

import com.course.mapreduce.model.AccessLogEntry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AccessLogMapper {

    @Delete("delete from access_logs")
    void clear();

    @Insert("""
            insert into access_logs(visit_date, visit_time, site, url, target_address, ip, region, status, bytes)
            values(#{visitDate}, #{visitTime}, #{site}, #{url}, #{targetAddress}, #{ip}, #{region}, #{status}, #{bytes})
            """)
    void insert(AccessLogEntry entry);

    @Select("select count(*) from access_logs")
    long count();

    @Select("""
            select id, visit_date, visit_time, site, url, target_address, ip, region, status, bytes
            from access_logs
            order by id desc
            limit 100
            """)
    List<AccessLogEntry> findRecent();
}
