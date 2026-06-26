说什么：系统的主流程是从 CSV 访问日志开始。用户在页面提交分析任务后，Spring Boot 创建任务记录并调用分析服务；核心统计由三个 MapReduce 作业完成，结果写入 MySQL；页面再通过接口读取结果，用 ECharts 展示排行、峰值和来源分布。这个结构能说明项目不是单纯画图，而是从日志到计算再到展示的一条完整链路。

打开/点击什么：仍然讲 PPT。可以顺着架构箭头从左到右指一下：CSV 日志、Spring Boot、MapReduce、MySQL、ECharts。