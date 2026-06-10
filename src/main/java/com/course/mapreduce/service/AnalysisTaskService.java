package com.course.mapreduce.service;

import com.course.mapreduce.dto.CreateTaskRequest;
import com.course.mapreduce.mapper.AnalysisTaskMapper;
import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.model.TaskStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisTaskService {
    private final AnalysisTaskMapper taskMapper;
    private final LocalCsvAnalysisService localCsvAnalysisService;
    private final MapReduceAnalysisRunner mapReduceAnalysisRunner;
    private final MapReduceResultImportService resultImportService;

    @Value("${app.hadoop.enabled:false}")
    private boolean hadoopEnabled;

    @Value("${app.sample.input-path:data/sample_access_log.csv}")
    private String defaultInputPath;

    public AnalysisTaskService(AnalysisTaskMapper taskMapper,
                               LocalCsvAnalysisService localCsvAnalysisService,
                               MapReduceAnalysisRunner mapReduceAnalysisRunner,
                               MapReduceResultImportService resultImportService) {
        this.taskMapper = taskMapper;
        this.localCsvAnalysisService = localCsvAnalysisService;
        this.mapReduceAnalysisRunner = mapReduceAnalysisRunner;
        this.resultImportService = resultImportService;
    }

    public AnalysisTask submit(CreateTaskRequest request) {
        AnalysisTask task = new AnalysisTask();
        task.setInputPath(resolveInputPath(request));
        task.setAnalysisType(request != null && StringUtils.hasText(request.getAnalysisType()) ? request.getAnalysisType() : "ALL");
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setMessage("Task created.");
        taskMapper.insert(task);

        taskMapper.updateStatus(task.getId(), TaskStatus.RUNNING, null, "Task is running.");
        try {
            if (hadoopEnabled) {
                MapReduceAnalysisRunner.MapReduceOutputPaths outputs = mapReduceAnalysisRunner.runAll(task.getInputPath());
                resultImportService.importAll(outputs);
                taskMapper.updateStatus(task.getId(), TaskStatus.SUCCESS, LocalDateTime.now(), "MapReduce analysis completed.");
            } else {
                localCsvAnalysisService.analyze(task.getInputPath());
                taskMapper.updateStatus(task.getId(), TaskStatus.SUCCESS, LocalDateTime.now(), "Local CSV analysis completed. Enable app.hadoop.enabled for Hadoop execution.");
            }
        } catch (Exception ex) {
            taskMapper.updateStatus(task.getId(), TaskStatus.FAILED, LocalDateTime.now(), ex.getMessage());
        }
        return taskMapper.findById(task.getId());
    }

    public AnalysisTask findById(Long id) {
        return taskMapper.findById(id);
    }

    public List<AnalysisTask> findAll() {
        return taskMapper.findAll();
    }

    private String resolveInputPath(CreateTaskRequest request) {
        if (request != null && StringUtils.hasText(request.getInputPath())) {
            return request.getInputPath();
        }
        return defaultInputPath;
    }
}
