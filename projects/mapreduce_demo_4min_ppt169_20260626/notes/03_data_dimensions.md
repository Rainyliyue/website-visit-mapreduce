说什么：每条日志都有 date、time、site、url、ip、region、status、bytes 八个字段。任务书中特别提到的用户 IP、用户所在地区、访问发生时间、访问目标地址都在一条原始记录里。不同分析方向只是取用不同字段：访问排行看 site 和 url，时段峰值看 site 和 time，来源分布看 site、region 和 ip。

打开/点击什么：可以在 IDEA 中打开 `data/sample_access_log_medium.csv` 或 `data/sample_access_log_large.csv`，展示每行数据确实包含这些字段；如果现场时间紧，可以只在 PPT 上讲。