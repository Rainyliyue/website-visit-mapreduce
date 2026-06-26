package com.course.mapreduce.service;

import com.course.mapreduce.job.HourPeakJob;
import com.course.mapreduce.job.SourceDistributionJob;
import com.course.mapreduce.job.WebsiteRankJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class MapReduceAnalysisRunner {
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${app.sample.output-dir:target/mapreduce-output}")
    private String outputDir;

    public MapReduceOutputPaths run(String inputPath, String analysisType) throws Exception {
        String type = normalizeAnalysisType(analysisType);
        String suffix = LocalDateTime.now().format(OUTPUT_FORMAT);
        String rankOutput = shouldRun(type, "RANK") ? outputDir + "/rank-" + suffix : null;
        String peakOutput = shouldRun(type, "PEAK") ? outputDir + "/peak-" + suffix : null;
        String sourceOutput = shouldRun(type, "SOURCE") ? outputDir + "/source-" + suffix : null;

        Configuration configuration = new Configuration();
        if (rankOutput != null) {
            runJob(configuration, WebsiteRankJob.createJob(configuration, inputPath, rankOutput), rankOutput);
        }
        if (peakOutput != null) {
            runJob(configuration, HourPeakJob.createJob(configuration, inputPath, peakOutput), peakOutput);
        }
        if (sourceOutput != null) {
            runJob(configuration, SourceDistributionJob.createJob(configuration, inputPath, sourceOutput), sourceOutput);
        }
        return new MapReduceOutputPaths(rankOutput, peakOutput, sourceOutput);
    }

    private void runJob(Configuration configuration, Job job, String outputPath) throws IOException, InterruptedException, ClassNotFoundException {
        FileSystem fileSystem = FileSystem.get(configuration);
        Path path = new Path(outputPath);
        if (fileSystem.exists(path)) {
            fileSystem.delete(path, true);
        }
        if (!job.waitForCompletion(true)) {
            throw new IllegalStateException("MapReduce job failed: " + job.getJobName());
        }
    }

    public record MapReduceOutputPaths(String rankOutput, String peakOutput, String sourceOutput) {
    }

    private boolean shouldRun(String analysisType, String target) {
        return "ALL".equals(analysisType) || target.equals(analysisType);
    }

    private String normalizeAnalysisType(String analysisType) {
        if (analysisType == null || analysisType.isBlank()) {
            return "ALL";
        }
        return analysisType.trim().toUpperCase(Locale.ROOT);
    }
}
