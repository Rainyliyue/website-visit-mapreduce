package com.course.mapreduce.service;

import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MapReduceResultImportService {
    private static final int INSERT_BATCH_SIZE = 500;

    private final ResultMapper resultMapper;

    public MapReduceResultImportService(ResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    @Transactional
    public void importAll(MapReduceAnalysisRunner.MapReduceOutputPaths paths) throws IOException {
        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        if (paths.rankOutput() != null) {
            importRank(paths.rankOutput());
        }
        if (paths.peakOutput() != null) {
            importPeak(paths.peakOutput());
        }
        if (paths.sourceOutput() != null) {
            importSource(paths.sourceOutput());
        }
    }

    private void importRank(String outputDir) throws IOException {
        List<VisitRank> items = new ArrayList<>();
        forEachPartLine(outputDir, line -> {
            String[] parts = line.split("\t", -1);
            if (parts.length < 3) {
                return;
            }
            VisitRank item = new VisitRank();
            item.setSite(parts[0]);
            item.setUrl(parts[1]);
            item.setPv(Long.parseLong(parts[2]));
            items.add(item);
            flushRank(items, false);
        });
        flushRank(items, true);
    }

    private void importPeak(String outputDir) throws IOException {
        List<VisitPeak> items = new ArrayList<>();
        forEachPartLine(outputDir, line -> {
            String[] parts = line.split("\t", -1);
            if (parts.length < 3) {
                return;
            }
            VisitPeak item = new VisitPeak();
            item.setSite(parts[0]);
            item.setHour(parts[1]);
            item.setPv(Long.parseLong(parts[2]));
            items.add(item);
            flushPeak(items, false);
        });
        flushPeak(items, true);
    }

    private void importSource(String outputDir) throws IOException {
        List<SourceDistribution> items = new ArrayList<>();
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
            items.add(item);
            flushSource(items, false);
        });
        flushSource(items, true);
    }

    private void flushRank(List<VisitRank> items, boolean force) {
        if (items.isEmpty() || (!force && items.size() < INSERT_BATCH_SIZE)) {
            return;
        }
        resultMapper.insertRankBatch(items);
        items.clear();
    }

    private void flushPeak(List<VisitPeak> items, boolean force) {
        if (items.isEmpty() || (!force && items.size() < INSERT_BATCH_SIZE)) {
            return;
        }
        resultMapper.insertPeakBatch(items);
        items.clear();
    }

    private void flushSource(List<SourceDistribution> items, boolean force) {
        if (items.isEmpty() || (!force && items.size() < INSERT_BATCH_SIZE)) {
            return;
        }
        resultMapper.insertSourceBatch(items);
        items.clear();
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
