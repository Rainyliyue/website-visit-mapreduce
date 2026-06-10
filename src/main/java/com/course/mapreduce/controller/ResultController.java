package com.course.mapreduce.controller;

import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {
    private final ResultMapper resultMapper;

    public ResultController(ResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    @GetMapping("/rank")
    public List<VisitRank> rank() {
        return resultMapper.findTopRank();
    }

    @GetMapping("/peak")
    public List<VisitPeak> peak() {
        return resultMapper.findPeaks();
    }

    @GetMapping("/source")
    public List<SourceDistribution> source() {
        return resultMapper.findSources();
    }

    @GetMapping("/source/regions")
    public List<SourceDistribution> regionSource() {
        return resultMapper.findRegionSources();
    }

    @GetMapping("/source/ips")
    public List<SourceDistribution> ipSource() {
        return resultMapper.findIpSources();
    }
}
