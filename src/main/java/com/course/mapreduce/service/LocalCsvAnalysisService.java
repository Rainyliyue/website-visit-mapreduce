package com.course.mapreduce.service;

import com.course.mapreduce.job.AccessLogRecord;
import com.course.mapreduce.mapper.AccessLogMapper;
import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.model.AccessLogEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class LocalCsvAnalysisService {
    private static final int INSERT_BATCH_SIZE = 500;

    private final ResultMapper resultMapper;
    private final AccessLogMapper accessLogMapper;

    public LocalCsvAnalysisService(ResultMapper resultMapper, AccessLogMapper accessLogMapper) {
        this.resultMapper = resultMapper;
        this.accessLogMapper = accessLogMapper;
    }

    @Transactional
    public void analyze(String inputPath, String analysisType) throws IOException {
        String type = normalizeAnalysisType(analysisType);
        boolean analyzeRank = shouldAnalyze(type, "RANK");
        boolean analyzePeak = shouldAnalyze(type, "PEAK");
        boolean analyzeSource = shouldAnalyze(type, "SOURCE");

        Map<String, Long> rank = analyzeRank ? new HashMap<>() : null;
        Map<String, Long> peak = analyzePeak ? new HashMap<>() : null;
        Map<String, Long> source = analyzeSource ? new HashMap<>() : null;
        List<AccessLogEntry> logBatch = new ArrayList<>(INSERT_BATCH_SIZE);

        accessLogMapper.clear();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inputPath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                AccessLogRecord record = AccessLogRecord.parse(line);
                if (record == null) {
                    continue;
                }
                logBatch.add(toEntry(record));
                if (logBatch.size() >= INSERT_BATCH_SIZE) {
                    flushAccessLogs(logBatch);
                }
                if (analyzeRank) {
                    rank.merge(record.site() + "\t" + record.url(), 1L, Long::sum);
                }
                if (analyzePeak) {
                    peak.merge(record.site() + "\t" + record.hour(), 1L, Long::sum);
                }
                if (analyzeSource) {
                    source.merge(record.site() + "\tREGION\t" + record.region(), 1L, Long::sum);
                    source.merge(record.site() + "\tIP\t" + record.ip(), 1L, Long::sum);
                }
            }
        }
        flushAccessLogs(logBatch);

        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();

        if (analyzeRank) {
            insertRankResults(rank);
        }
        if (analyzePeak) {
            insertPeakResults(peak);
        }
        if (analyzeSource) {
            insertSourceResults(source);
        }
    }

    private void insertRankResults(Map<String, Long> rank) {
        List<VisitRank> items = new ArrayList<>(rank.size());
        rank.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            VisitRank item = new VisitRank();
            item.setSite(parts[0]);
            item.setUrl(parts[1]);
            item.setPv(value);
            items.add(item);
        });
        insertInBatches(items, resultMapper::insertRankBatch);
    }

    private void insertPeakResults(Map<String, Long> peak) {
        List<VisitPeak> items = new ArrayList<>(peak.size());
        peak.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            VisitPeak item = new VisitPeak();
            item.setSite(parts[0]);
            item.setHour(parts[1]);
            item.setPv(value);
            items.add(item);
        });
        insertInBatches(items, resultMapper::insertPeakBatch);
    }

    private void insertSourceResults(Map<String, Long> source) {
        List<SourceDistribution> items = new ArrayList<>(source.size());
        source.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            SourceDistribution item = new SourceDistribution();
            item.setSite(parts[0]);
            item.setSourceType(parts[1]);
            item.setSourceValue(parts[2]);
            item.setPv(value);
            items.add(item);
        });
        insertInBatches(items, resultMapper::insertSourceBatch);
    }

    private void flushAccessLogs(List<AccessLogEntry> batch) {
        if (batch.isEmpty()) {
            return;
        }
        accessLogMapper.insertBatch(batch);
        batch.clear();
    }

    private <T> void insertInBatches(List<T> items, Consumer<List<T>> inserter) {
        for (int start = 0; start < items.size(); start += INSERT_BATCH_SIZE) {
            int end = Math.min(start + INSERT_BATCH_SIZE, items.size());
            inserter.accept(items.subList(start, end));
        }
    }

    private boolean shouldAnalyze(String analysisType, String target) {
        return "ALL".equals(analysisType) || target.equals(analysisType);
    }

    private String normalizeAnalysisType(String analysisType) {
        if (analysisType == null || analysisType.isBlank()) {
            return "ALL";
        }
        return analysisType.trim().toUpperCase(Locale.ROOT);
    }

    private AccessLogEntry toEntry(AccessLogRecord record) {
        AccessLogEntry entry = new AccessLogEntry();
        entry.setVisitDate(record.date());
        entry.setVisitTime(record.time());
        entry.setSite(record.site());
        entry.setUrl(record.url());
        entry.setTargetAddress(record.targetAddress());
        entry.setIp(record.ip());
        entry.setRegion(record.region());
        entry.setStatus(record.status());
        entry.setBytes(record.bytes());
        return entry;
    }
}
