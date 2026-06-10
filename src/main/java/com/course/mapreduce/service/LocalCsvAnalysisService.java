package com.course.mapreduce.service;

import com.course.mapreduce.job.AccessLogRecord;
import com.course.mapreduce.mapper.AccessLogMapper;
import com.course.mapreduce.mapper.ResultMapper;
import com.course.mapreduce.model.AccessLogEntry;
import com.course.mapreduce.model.SourceDistribution;
import com.course.mapreduce.model.VisitPeak;
import com.course.mapreduce.model.VisitRank;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalCsvAnalysisService {
    private final ResultMapper resultMapper;
    private final AccessLogMapper accessLogMapper;

    public LocalCsvAnalysisService(ResultMapper resultMapper, AccessLogMapper accessLogMapper) {
        this.resultMapper = resultMapper;
        this.accessLogMapper = accessLogMapper;
    }

    public void analyze(String inputPath) throws IOException {
        Map<String, Long> rank = new HashMap<>();
        Map<String, Long> peak = new HashMap<>();
        Map<String, Long> source = new HashMap<>();

        accessLogMapper.clear();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inputPath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                AccessLogRecord record = AccessLogRecord.parse(line);
                if (record == null) {
                    continue;
                }
                accessLogMapper.insert(toEntry(record));
                rank.merge(record.site() + "\t" + record.url(), 1L, Long::sum);
                peak.merge(record.site() + "\t" + record.hour(), 1L, Long::sum);
                source.merge(record.site() + "\tREGION\t" + record.region(), 1L, Long::sum);
                source.merge(record.site() + "\tIP\t" + record.ip(), 1L, Long::sum);
            }
        }

        resultMapper.clearRank();
        resultMapper.clearPeak();
        resultMapper.clearSource();
        rank.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            VisitRank item = new VisitRank();
            item.setSite(parts[0]);
            item.setUrl(parts[1]);
            item.setPv(value);
            resultMapper.insertRank(item);
        });
        peak.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            VisitPeak item = new VisitPeak();
            item.setSite(parts[0]);
            item.setHour(parts[1]);
            item.setPv(value);
            resultMapper.insertPeak(item);
        });
        source.forEach((key, value) -> {
            String[] parts = key.split("\t", -1);
            SourceDistribution item = new SourceDistribution();
            item.setSite(parts[0]);
            item.setSourceType(parts[1]);
            item.setSourceValue(parts[2]);
            item.setPv(value);
            resultMapper.insertSource(item);
        });
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
