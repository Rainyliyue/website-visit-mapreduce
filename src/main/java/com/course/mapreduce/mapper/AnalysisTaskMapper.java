package com.course.mapreduce.mapper;

import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.model.TaskStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisTaskMapper {

    @Insert("""
            insert into analysis_tasks(input_path, analysis_type, status, created_at, message)
            values(#{inputPath}, #{analysisType}, #{status}, #{createdAt}, #{message})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AnalysisTask task);

    @Select("select * from analysis_tasks where id = #{id}")
    AnalysisTask findById(Long id);

    @Select("select * from analysis_tasks order by id desc")
    List<AnalysisTask> findAll();

    @Update("""
            update analysis_tasks
            set status = #{status}, finished_at = #{finishedAt}, message = #{message}
            where id = #{id}
            """)
    void updateStatus(@Param("id") Long id,
                      @Param("status") TaskStatus status,
                      @Param("finishedAt") LocalDateTime finishedAt,
                      @Param("message") String message);
}
