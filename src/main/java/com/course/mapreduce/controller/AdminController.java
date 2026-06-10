package com.course.mapreduce.controller;

import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.mapper.UserMapper;
import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.model.UserAccount;
import com.course.mapreduce.service.AnalysisTaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserMapper userMapper;
    private final ResultMapper resultMapper;
    private final AnalysisTaskService taskService;

    public AdminController(UserMapper userMapper, ResultMapper resultMapper, AnalysisTaskService taskService) {
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

    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        return Map.of("message", "Analysis cache cleared.");
    }
}
