# 网站访问量统计分析系统

本项目是《MapReduce 应用项目实战》课程设计任务二的源码工程，实现基于 Hadoop MapReduce 的网站访问量统计分析系统。系统对网站访问日志进行统计，输出网站访问量热度排行、不同时间段访问峰值、访问来源 IP/地区分布，并通过 Spring Boot + MySQL + ECharts 展示分析结果。

## 1. 本机环境

建议环境如下：

- JDK：Java 17
- Maven：3.8.1
- Hadoop：3.5.x
- 数据库：MySQL 8.x
- 开发工具：IntelliJ IDEA、DBeaver

当前工程默认使用 MySQL 数据库 `mapreduce_course`，默认账号为 `mapreduce_user`，默认密码为 `mapreduce_pwd`。JDBC 地址使用 `127.0.0.1`，这样可以兼容 MySQL 开启 `--skip-name-resolve` 的情况。如果需要修改，请编辑 `src/main/resources/application.yml`。

## 2. 初始化 MySQL

在 DBeaver 或 MySQL 命令行中执行：

```sql
source sql/init.sql;
```

如果 DBeaver 不支持 `source`，请直接打开 `sql/init.sql`，全选后执行。脚本会完成建库、建用户、建表和测试账号初始化。

## 3. 安装 Hadoop 3.5.x

推荐优先使用 Docker 完成 Hadoop 验收，避免 Windows 原生 Hadoop 的 `winutils.exe` 配置问题：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\run-hadoop-docker.ps1
```

该脚本会使用 `apache/hadoop:3.5.0` 镜像运行 `hadoop version` 和三个 MapReduce 作业，输出目录在 `target/docker-mr-*`。详细步骤见 `docs/Hadoop-Docker验收步骤.md`。

如果需要 Windows 原生 Hadoop，再按以下方式安装。Windows 本机建议采用伪分布式或本地模式。关键配置如下：

1. 下载 Hadoop 3.5.x 并解压，例如 `D:\hadoop\hadoop-3.5.0`。
2. 配置环境变量：
   - `HADOOP_HOME=D:\hadoop\hadoop-3.5.0`
   - `PATH=%HADOOP_HOME%\bin;%HADOOP_HOME%\sbin;%PATH%`
3. Windows 需要准备与 Hadoop 版本匹配的 `winutils.exe` 和 `hadoop.dll`，放入 `%HADOOP_HOME%\bin`。
4. 验证命令：

```powershell
hadoop version
```

如果课程验收不要求真实 HDFS，可先使用 Spring Boot 的本地 CSV 分析模式完成系统演示；后续配置 `app.hadoop.enabled=true` 再切换为真实 Hadoop MapReduce。

## 4. 构建项目

```powershell
mvn clean package -DskipTests
```

构建成功后会生成：

- `target/website-visit-mapreduce-1.0.0.jar`：普通 Jar，可用于 Hadoop job。
- `target/website-visit-mapreduce-1.0.0-boot.jar`：Spring Boot 可运行 Jar。

## 5. 运行 MapReduce 作业

访问量排行：

```powershell
scripts\run-mapreduce-rank.bat data\sample_access_log.csv target\mr-rank
```

时段峰值：

```powershell
scripts\run-mapreduce-peak.bat data\sample_access_log.csv target\mr-peak
```

来源分布：

```powershell
scripts\run-mapreduce-source.bat data\sample_access_log.csv target\mr-source
```

输出目录下的 `part-r-00000` 即为统计结果。

项目内置多份样例日志：

- `data/sample_access_log.csv`：基础样例，60 条。
- `data/sample_access_log_medium.csv`：中等样例，1000 条。
- `data/sample_access_log_holiday.csv`：节假日样例，3000 条。
- `data/sample_access_log_large.csv`：大规模样例，10000 条。

如需重新生成大样例数据，可执行：

```powershell
& "C:\Users\87152\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" scripts\generate_sample_data.py
```

## 6. 启动 Web 系统

默认配置为本地 CSV 分析模式，不强制依赖 Hadoop 命令：

```powershell
mvn spring-boot:run
```

浏览器访问：

```text
http://localhost:8080
```

系统会先进入登录页。演示账号如下：

| 角色 | 用户名 | 密码 | 可访问页面 |
| --- | --- | --- | --- |
| 普通用户 | user | user123 | 首页、提交任务、查看结果 |
| 管理员 | admin | admin123 | 首页、管理员控制台 |

登录后点击“提交分析任务”，系统会读取所选样例日志，生成统计结果并写入 MySQL。管理员控制台地址为：

```text
http://localhost:8080/admin.html
```

如果 Hadoop 环境已经安装好，可在 `application.yml` 中设置：

```yaml
app:
  hadoop:
    enabled: true
```

此时提交任务会依次运行三个 MapReduce job，并将输出结果导入 MySQL。

## 7. 接口说明

任务接口：

- `POST /api/tasks`：提交分析任务。
- `GET /api/tasks`：查看任务列表。
- `GET /api/tasks/{id}`：查看单个任务状态。

结果接口：

- `GET /api/results/rank`：网站访问量热度排行。
- `GET /api/results/peak`：不同时间段访问峰值。
- `GET /api/results/source`：来源 IP/地区分布。

管理员接口：

- `GET /api/admin/users`：用户列表。
- `GET /api/admin/tasks`：任务列表。
- `DELETE /api/admin/tasks/{id}`：删除 `SUCCESS/FAILED` 状态任务。
- `DELETE /api/admin/tasks/completed`：批量删除完成或异常任务。
- `GET /api/admin/data`：查看访问排行、时段峰值、来源地区、来源 IP 数据概览。
- `DELETE /api/admin/data/rank`：删除访问量排行数据。
- `DELETE /api/admin/data/peak`：删除时段峰值数据。
- `DELETE /api/admin/data/regions`：删除用户所在地区统计数据。
- `DELETE /api/admin/data/ips`：删除用户 IP 统计数据。
- `DELETE /api/admin/cache`：清理统计结果缓存。
- `DELETE /api/admin/cache/completed`：删除已完成任务缓存，并清理完成/异常任务记录。

## 8. 目录结构

```text
.
├── data                     样例访问日志
├── docs                     需求分析、设计说明、课程报告
├── scripts                  MapReduce 运行脚本
├── sql                      MySQL 初始化脚本
└── src
    └── main
        ├── java             Java 源码
        └── resources
            └── static       ECharts 前端页面
```

## 9. 验收演示建议

1. 使用 DBeaver 执行 `sql/init.sql`。
2. IDEA 打开项目，确认 Maven 依赖加载完成。
3. 启动 `WebsiteVisitMapReduceApplication`。
4. 打开 `http://localhost:8080`。
5. 点击“提交分析任务”。
6. 展示任务状态、排行图、峰值图、来源分布图和结果明细表。
7. 在 DBeaver 中查看 `analysis_tasks`、`visit_rank`、`visit_peak`、`source_distribution` 表。

## 10. 说明

任务书中提到“大数据计算实时数据分析”，本项目按课程项目常见做法解释为“用户提交任务后立即触发分析”。核心计算仍由 MapReduce 作业实现，同时提供本地 CSV 分析模式，便于未安装 Hadoop 时先完成系统联调和演示截图。
