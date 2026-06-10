package com.course.mapreduce.service;

import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class MapReduceResultImportService {
    private final ResultMapper resultMapper;

    public MapReduceResultImportService(ResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    public void importAll(MapReduceAnalysisRunner.MapReduceOutputPaths paths) throws IOException {
        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        importRank(paths.rankOutput());
        importPeak(paths.peakOutput());
        importSource(paths.sourceOutput());
    }

    private void importRank(String outputDir) throws IOException {
        forEachPartLine(outputDir, line -> {
            String[] parts = line.split("\t", -1);
            if (parts.length < 3) {
                return;
            }
            VisitRank item = new VisitRank();
            item.setSite(parts[0]);
            item.setUrl(parts[1]);
            item.setPv(Long.parseLong(parts[2]));
            resultMapper.insertRank(item);
        });
    }

    private void importPeak(String outputDir) throws IOException {
        forEachPartLine(outputDir, line -> {
            String[] parts = line.split("\t", -1);
            if (parts.length < 3) {
                return;
            }
            VisitPeak item = new VisitPeak();
            item.setSite(parts[0]);
            item.setHour(parts[1]);
            item.setPv(Long.parseLong(parts[2]));
            resultMapper.insertPeak(item);
        });
    }

    private void importSource(String outputDir) throws IOException {
        forEachPartLine(outputDir, line -> {
            String[] parts = line.split("\t", -1);
            if (parts.length < 4) {
                return;
            }
            SourceDistribution item = new SourceDistribution();
            item.setSite(parts[0]);
            item.setSourceType(parts[1]);
            item.setSourceValue(parts[2]);
            item.setPv(Long.parseLong(parts[3]));
            resultMapper.insertSource(item);
        });
    }

    private void forEachPartLine(String outputDir, LineConsumer consumer) throws IOException {
        try (Stream<Path> paths = Files.list(Path.of(outputDir))) {
            for (Path path : paths.filter(p -> p.getFileName().toString().startsWith("part-")).toList()) {
                try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) {
                            consumer.accept(line);
                        }
                    }
                }
            }
        }
    }

    private interface LineConsumer {
        void accept(String line) throws IOException;
    }
}
