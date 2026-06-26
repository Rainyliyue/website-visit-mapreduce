# Slide 1｜项目目标与任务要求

说什么：这次演示的是课程设计“统计网站访问量系统”。任务要求是使用 Hadoop MapReduce 对网站访问日志做统计分析，并把结果通过 Web 页面可视化出来。我的实现把任务拆成日志数据、MapReduce 计算、MySQL 存储、Spring Boot 后端和 ECharts 页面展示五个部分，普通用户和管理员都有对应功能。

打开/点击什么：先打开 PPT 第 1 页即可，不需要切换系统。

# Slide 2｜系统整体架构

说什么：系统的主流程是从 CSV 访问日志开始。用户在页面提交分析任务后，Spring Boot 创建任务记录并调用分析服务；核心统计由三个 MapReduce 作业完成，结果写入 MySQL；页面再通过接口读取结果，用 ECharts 展示排行、峰值和来源分布。这个结构能说明项目不是单纯画图，而是从日志到计算再到展示的一条完整链路。

打开/点击什么：仍然讲 PPT。可以顺着架构箭头从左到右指一下：CSV 日志、Spring Boot、MapReduce、MySQL、ECharts。

# Slide 3｜数据字段与分析维度

说什么：每条日志都有 date、time、site、url、ip、region、status、bytes 八个字段。任务书中特别提到的用户 IP、用户所在地区、访问发生时间、访问目标地址都在一条原始记录里。不同分析方向只是取用不同字段：访问排行看 site 和 url，时段峰值看 site 和 time，来源分布看 site、region 和 ip。

打开/点击什么：可以在 IDEA 中打开 `data/sample_access_log_medium.csv` 或 `data/sample_access_log_large.csv`，展示每行数据确实包含这些字段；如果现场时间紧，可以只在 PPT 上讲。

# Slide 4｜MapReduce 核心算法

说什么：MapReduce 的核心是把每行日志解析成 AccessLogRecord，然后 Mapper 按不同任务输出不同 key，Reducer 做求和聚合。访问量排行输出“站点加路径”，小时峰值输出“站点加小时”，来源分布同时输出“站点加地区”和“站点加 IP”。三个作业共享解析和 SumReducer，所以代码结构比较清楚，也方便验收时单独运行 Hadoop 命令。

打开/点击什么：可在 IDEA 中打开 `src/main/java/com/course/mapreduce/job/WebsiteRankJob.java`、`HourPeakJob.java`、`SourceDistributionJob.java`，分别指出 Mapper 的 key 是怎么设计的。

# Slide 5｜用户功能演示流程

说什么：下面演示普通用户功能。普通用户可以登录系统，选择样例数据和分析类型，提交任务，然后查看任务状态。任务成功后，页面下方会刷新网站访问量热度排行、不同时间段访问峰值、用户所在地区分布和明细数据，这对应任务书里的提交任务、查看状态、查看结果和可视化视图。

打开/点击什么：打开浏览器进入 `http://localhost:8080`。如果出现登录页，用 `user / user123` 登录。选择一份样例数据，例如中等样例或大规模样例，点击“提交分析任务”。等待任务列表显示 SUCCESS，然后说明三个图表和结果明细已经从数据库读取并刷新。

# Slide 6｜管理员功能与性能优化

说什么：管理员侧对应任务书要求的用户管理、任务管理、数据管理和分析缓存数据管理。管理员可以查看用户和任务，可以删除完成或异常任务，也可以删除已经统计完成的数据和缓存。为了让较大样例运行更稳，后端使用异步任务执行，写入数据库时使用批量插入，并且可以按选择的分析类型只执行必要任务。

打开/点击什么：在页面退出，使用 `admin / admin123` 登录，或者直接进入管理员入口。展示用户管理、任务管理、数据管理和缓存管理区域。可以选择一条已完成任务点击删除，或说明数据管理中的删除按钮对应不同统计表。

# Slide 7｜验收要点与总结

说什么：最后总结一下验收材料。源码工程可以用 IDEA 打开，数据库脚本在 sql 目录，样例数据在 data 目录，Hadoop Docker 脚本可以证明 MapReduce 作业能独立运行，报告和设计说明书在 docs 目录。整体上，项目覆盖了任务书要求的数据字段、三类统计分析、用户功能、管理员功能和 Web 可视化。

打开/点击什么：如老师要看 Hadoop，可以打开 PowerShell，展示之前运行脚本后的 `Job ... completed successfully` 输出，或运行 `scripts/run-hadoop-docker.ps1`。如老师要看文档，打开 `docs/课程设计报告.docx`。

