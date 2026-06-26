package com.course.mapreduce.mapper;

import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ResultMapper {

    @Delete("delete from visit_rank")
    void clearRank();

    @Delete("delete from visit_peak")
    void clearPeak();

    @Delete("delete from source_distribution")
    void clearSource();

    @Delete("delete from source_distribution where source_type = 'REGION'")
    void clearRegionSource();

    @Delete("delete from source_distribution where source_type = 'IP'")
    void clearIpSource();

    @Select("select count(*) from visit_rank")
    long countRank();

    @Select("select count(*) from visit_peak")
    long countPeak();

    @Select("select count(*) from source_distribution where source_type = 'REGION'")
    long countRegionSource();

    @Select("select count(*) from source_distribution where source_type = 'IP'")
    long countIpSource();

    @Insert("insert into visit_rank(site, url, pv) values(#{site}, #{url}, #{pv})")
    void insertRank(VisitRank rank);

    @Insert("""
            <script>
            insert into visit_rank(site, url, pv)
            values
            <foreach collection="items" item="item" separator=",">
                (#{item.site}, #{item.url}, #{item.pv})
            </foreach>
            </script>
            """)
    void insertRankBatch(@Param("items") List<VisitRank> items);

    @Insert("insert into visit_peak(site, hour, pv) values(#{site}, #{hour}, #{pv})")
    void insertPeak(VisitPeak peak);

    @Insert("""
            <script>
            insert into visit_peak(site, hour, pv)
            values
            <foreach collection="items" item="item" separator=",">
                (#{item.site}, #{item.hour}, #{item.pv})
            </foreach>
            </script>
            """)
    void insertPeakBatch(@Param("items") List<VisitPeak> items);

    @Insert("insert into source_distribution(site, source_type, source_value, pv) values(#{site}, #{sourceType}, #{sourceValue}, #{pv})")
    void insertSource(SourceDistribution source);

    @Insert("""
            <script>
            insert into source_distribution(site, source_type, source_value, pv)
            values
            <foreach collection="items" item="item" separator=",">
                (#{item.site}, #{item.sourceType}, #{item.sourceValue}, #{item.pv})
            </foreach>
            </script>
            """)
    void insertSourceBatch(@Param("items") List<SourceDistribution> items);

    @Select("select site, url, pv from visit_rank order by pv desc, site asc, url asc limit 20")
    List<VisitRank> findTopRank();

    @Select("select site, hour, pv from visit_peak order by site asc, hour asc")
    List<VisitPeak> findPeaks();

    @Select("""
            select site, source_type, source_value, pv
            from source_distribution
            order by field(source_type, 'REGION', 'IP'), pv desc, site asc, source_value asc
            limit 50
            """)
    List<SourceDistribution> findSources();

    @Select("""
            select site, source_type, source_value, pv
            from source_distribution
            where source_type = 'REGION'
            order by pv desc, site asc, source_value asc
            limit 50
            """)
    List<SourceDistribution> findRegionSources();

    @Select("""
            select site, source_type, source_value, pv
            from source_distribution
            where source_type = 'IP'
            order by pv desc, site asc, source_value asc
            limit 50
            """)
    List<SourceDistribution> findIpSources();
}
