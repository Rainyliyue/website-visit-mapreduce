package com.course.mapreduce.controller;

import com.course.mapreduce.dto.CreateTaskRequest;
import com.course.mapreduce.model.AnalysisTask;
import com.course.mapreduce.service.AnalysisTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final AnalysisTaskService taskService;

    public TaskController(AnalysisTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public AnalysisTask submit(@RequestBody(required = false) CreateTaskRequest request) {
        return taskService.submit(request);
    }

    @GetMapping("/{id}")
    public AnalysisTask findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @GetMapping
    public List<AnalysisTask> findAll() {
        return taskService.findAll();
    }
}
