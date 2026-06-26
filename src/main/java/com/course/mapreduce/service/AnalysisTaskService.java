package com.course.mapreduce.service;

import com.course.mapreduce.dto.CreateTaskRequest;
import com.course.mapreduce.mapper.AnalysisTaskMapper;
import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.model.TaskStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class AnalysisTaskService {
    private final AnalysisTaskMapper taskMapper;
    private final LocalCsvAnalysisService localCsvAnalysisService;
    private final MapReduceAnalysisRunner mapReduceAnalysisRunner;
    private final MapReduceResultImportService resultImportService;
    private final Executor analysisTaskExecutor;

    @Value("${app.hadoop.enabled:false}")
    private boolean hadoopEnabled;

    @Value("${app.sample.input-path:data/sample_access_log.csv}")
    private String defaultInputPath;

    public AnalysisTaskService(AnalysisTaskMapper taskMapper,
                               LocalCsvAnalysisService localCsvAnalysisService,
                               MapReduceAnalysisRunner mapReduceAnalysisRunner,
                               MapReduceResultImportService resultImportService,
                               @Qualifier("analysisTaskExecutor") Executor analysisTaskExecutor) {
        this.taskMapper = taskMapper;
        this.localCsvAnalysisService = localCsvAnalysisService;
        this.mapReduceAnalysisRunner = mapReduceAnalysisRunner;
        this.resultImportService = resultImportService;
        this.analysisTaskExecutor = analysisTaskExecutor;
    }

    public AnalysisTask submit(CreateTaskRequest request) {
        AnalysisTask task = new AnalysisTask();
        task.setInputPath(resolveInputPath(request));
        task.setAnalysisType(resolveAnalysisType(request));
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setMessage("Task created.");
        taskMapper.insert(task);

        try {
            analysisTaskExecutor.execute(() -> executeTask(task.getId(), task.getInputPath(), task.getAnalysisType()));
        } catch (RuntimeException ex) {
            taskMapper.updateStatus(task.getId(), TaskStatus.FAILED, LocalDateTime.now(), "Task queue is full: " + ex.getMessage());
        }
        return taskMapper.findById(task.getId());
    }

    private void executeTask(Long taskId, String inputPath, String analysisType) {
        taskMapper.updateStatus(taskId, TaskStatus.RUNNING, null, "Task is running.");
        try {
            if (hadoopEnabled) {
                MapReduceAnalysisRunner.MapReduceOutputPaths outputs = mapReduceAnalysisRunner.run(inputPath, analysisType);
                resultImportService.importAll(outputs);
                taskMapper.updateStatus(taskId, TaskStatus.SUCCESS, LocalDateTime.now(), "MapReduce analysis completed.");
            } else {
                localCsvAnalysisService.analyze(inputPath, analysisType);
                taskMapper.updateStatus(taskId, TaskStatus.SUCCESS, LocalDateTime.now(), "Local CSV analysis completed. Enable app.hadoop.enabled for Hadoop execution.");
            }
        } catch (Exception ex) {
            taskMapper.updateStatus(taskId, TaskStatus.FAILED, LocalDateTime.now(), ex.getMessage());
        }
    }

    public AnalysisTask findById(Long id) {
        return taskMapper.findById(id);
    }

    public List<AnalysisTask> findAll() {
        return taskMapper.findAll();
    }

    public boolean deleteFinishedOrFailed(Long id) {
        return taskMapper.deleteFinishedOrFailed(id) > 0;
    }

    public int deleteAllFinishedOrFailed() {
        return taskMapper.deleteAllFinishedOrFailed();
    }

    private String resolveInputPath(CreateTaskRequest request) {
        if (request != null && StringUtils.hasText(request.getInputPath())) {
            return request.getInputPath();
        }
        return defaultInputPath;
    }

    private String resolveAnalysisType(CreateTaskRequest request) {
        if (request != null && StringUtils.hasText(request.getAnalysisType())) {
            return request.getAnalysisType().trim().toUpperCase();
        }
        return "ALL";
    }
}
