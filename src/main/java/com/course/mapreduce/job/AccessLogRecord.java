package com.course.mapreduce.job;

public class AccessLogRecord {
    private final String date;
    private final String time;
    private final String site;
    private final String url;
    private final String ip;
    private final String region;

    private AccessLogRecord(String date, String time, String site, String url, String ip, String region) {
        this.date = date;
        this.time = time;
        this.site = site;
        this.url = url;
        this.ip = ip;
        this.region = region;
    }

    public static AccessLogRecord parse(String line) {
        if (line == null || line.isBlank() || line.startsWith("date,")) {
            return null;
        }
        String[] parts = line.split(",", -1);
        if (parts.length < 8) {
            return null;
        }
        String date = parts[0].trim();
        String time = parts[1].trim();
        String site = parts[2].trim();
        String url = parts[3].trim();
        String ip = parts[4].trim();
        String region = parts[5].trim();
        if (date.isEmpty() || time.length() < 2 || site.isEmpty() || url.isEmpty()) {
            return null;
        }
        return new AccessLogRecord(date, time, site, url, ip, region);
    }

    public String rankKey() {
        return site + "\t" + url;
    }

    public String hourKey() {
        return site + "\t" + time.substring(0, 2);
    }

    public String regionKey() {
        return site + "\tREGION\t" + region;
    }

    public String ipKey() {
        return site + "\tIP\t" + ip;
    }

    public String site() {
        return site;
    }

    public String url() {
        return url;
    }

    public String hour() {
        return time.substring(0, 2);
    }

    public String ip() {
        return ip;
    }

    public String region() {
        return region;
    }
}
