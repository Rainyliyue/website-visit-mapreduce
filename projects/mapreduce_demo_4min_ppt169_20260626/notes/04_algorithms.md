说什么：MapReduce 的核心是把每行日志解析成 AccessLogRecord，然后 Mapper 按不同任务输出不同 key，Reducer 做求和聚合。访问量排行输出“站点加路径”，小时峰值输出“站点加小时”，来源分布同时输出“站点加地区”和“站点加 IP”。三个作业共享解析和 SumReducer，所以代码结构比较清楚，也方便验收时单独运行 Hadoop 命令。

打开/点击什么：可在 IDEA 中打开 `src/main/java/com/course/mapreduce/job/WebsiteRankJob.java`、`HourPeakJob.java`、`SourceDistributionJob.java`，分别指出 Mapper 的 key 是怎么设计的。