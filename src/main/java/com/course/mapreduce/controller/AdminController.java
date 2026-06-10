package com.course.mapreduce.controller;

import com.course.mapreduce.mapper.AccessLogMapper;
import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.mapper.UserMapper;
import com.course.mapreduce.model.AccessLogEntry;
import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.UserAccount;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import com.course.mapreduce.service.AnalysisTaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AccessLogMapper accessLogMapper;
    private final UserMapper userMapper;
    private final ResultMapper resultMapper;
    private final AnalysisTaskService taskService;

    public AdminController(AccessLogMapper accessLogMapper, UserMapper userMapper, ResultMapper resultMapper, AnalysisTaskService taskService) {
        this.accessLogMapper = accessLogMapper;
        this.userMapper = userMapper;
        this.resultMapper = resultMapper;
        this.taskService = taskService;
    }

    @GetMapping("/users")
    public List<UserAccount> users() {
        return userMapper.findAll();
    }

    @GetMapping("/tasks")
    public List<AnalysisTask> tasks() {
        return taskService.findAll();
    }

    @GetMapping("/data")
    public Map<String, Object> data() {
        List<VisitRank> rank = resultMapper.findTopRank();
        List<VisitPeak> peak = resultMapper.findPeaks();
        List<SourceDistribution> regions = resultMapper.findRegionSources();
        List<SourceDistribution> ips = resultMapper.findIpSources();
        return Map.of(
                "rank", rank,
                "peak", peak,
                "regions", regions,
                "ips", ips,
                "logs", accessLogMapper.findRecent(),
                "counts", Map.of(
                        "rank", resultMapper.countRank(),
                        "peak", resultMapper.countPeak(),
                        "regions", resultMapper.countRegionSource(),
                        "ips", resultMapper.countIpSource(),
                        "logs", accessLogMapper.count()
                )
        );
    }

    @GetMapping("/logs")
    public List<AccessLogEntry> logs() {
        return accessLogMapper.findRecent();
    }

    @DeleteMapping("/tasks/{id}")
    public Map<String, String> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteFinishedOrFailed(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "只能删除 SUCCESS 或 FAILED 状态的任务");
        }
        return Map.of("message", "Task deleted.");
    }

    @DeleteMapping("/tasks/completed")
    public Map<String, Object> deleteCompletedTasks() {
        int deleted = taskService.deleteAllFinishedOrFailed();
        return Map.of("message", "Completed or failed tasks deleted.", "deleted", deleted);
    }

    @DeleteMapping("/data/rank")
    public Map<String, String> clearRankData() {
        resultMapper.clearRank();
        return Map.of("message", "Visit rank data cleared.");
    }

    @DeleteMapping("/data/peak")
    public Map<String, String> clearPeakData() {
        resultMapper.clearPeak();
        return Map.of("message", "Visit peak data cleared.");
    }

    @DeleteMapping("/data/regions")
    public Map<String, String> clearRegionData() {
        resultMapper.clearRegionSource();
        return Map.of("message", "Region source data cleared.");
    }

    @DeleteMapping("/data/ips")
    public Map<String, String> clearIpData() {
        resultMapper.clearIpSource();
        return Map.of("message", "IP source data cleared.");
    }

    @DeleteMapping("/data/logs")
    public Map<String, String> clearLogs() {
        accessLogMapper.clear();
        return Map.of("message", "Access log detail data cleared.");
    }

    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        accessLogMapper.clear();
        return Map.of("message", "Analysis cache cleared.");
    }

    @DeleteMapping("/cache/completed")
    public Map<String, Object> clearCompletedCache() {
        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        accessLogMapper.clear();
        int deletedTasks = taskService.deleteAllFinishedOrFailed();
        return Map.of(
                "message", "Completed task cache cleared.",
                "deletedTasks", deletedTasks
        );
    }
}
