const rankChart = echarts.init(document.getElementById("rankChart"));
const peakChart = echarts.init(document.getElementById("peakChart"));
const sourceChart = echarts.init(document.getElementById("sourceChart"));
const chartColors = ["#0f766e", "#4f46e5", "#f97316", "#e11d48", "#0891b2", "#65a30d", "#7c3aed", "#ca8a04"];
const chartTextColor = "#667085";
const axisLineStyle = {lineStyle: {color: "#d9e2ec"}};
const splitLineStyle = {lineStyle: {color: "#e7edf5"}};
const tooltipStyle = {
    backgroundColor: "rgba(15, 23, 42, 0.92)",
    borderColor: "rgba(15, 23, 42, 0)",
    textStyle: {color: "#ffffff"}
};

loadCurrentUser();

document.getElementById("logoutBtn").addEventListener("click", logout);

document.getElementById("sampleSelect").addEventListener("change", event => {
    document.getElementById("inputPath").value = event.target.value;
    setNotice(`已选择样例：${event.target.options[event.target.selectedIndex].text}`, "info");
});

document.getElementById("runBtn").addEventListener("click", async () => {
    const button = document.getElementById("runBtn");
    button.disabled = true;
    setNotice("任务提交中，请稍候...", "info");
    try {
        const sampleSelect = document.getElementById("sampleSelect");
        const inputPath = sampleSelect.value;
        document.getElementById("inputPath").value = inputPath;
        const analysisType = document.getElementById("analysisType").value;
        const task = await fetchJson("/api/tasks", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({inputPath, analysisType})
        });
        setNotice(`任务 #${task.id} 已提交，正在后台分析...`, "info");
        await refreshAll();
        const finishedTask = await waitForTask(task.id);
        await refreshAll();
        if (finishedTask.status === "SUCCESS") {
            setNotice(`任务 #${finishedTask.id} 执行完成，结果已刷新。`, "success");
        } else {
            setNotice(`任务 #${finishedTask.id} 执行失败：${finishedTask.message || "请查看后台日志"}`, "error");
        }
    } catch (error) {
        setNotice(error.message, "error");
    } finally {
        button.disabled = false;
    }
});

async function waitForTask(taskId) {
    for (let index = 0; index < 300; index++) {
        await sleep(1500);
        const task = await fetchJson(`/api/tasks/${taskId}`);
        const tasks = await fetchJson("/api/tasks");
        renderTasks(tasks);
        if (task.status === "SUCCESS" || task.status === "FAILED") {
            return task;
        }
        setNotice(`任务 #${taskId} 当前状态：${task.status}，后台仍在分析...`, "info");
    }
    throw new Error(`任务 #${taskId} 仍在运行，请稍后在任务列表查看状态。`);
}

function sleep(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}

async function refreshAll() {
    try {
        const [tasks, rank, peak, source] = await Promise.all([
            fetchJson("/api/tasks"),
            fetchJson("/api/results/rank"),
            fetchJson("/api/results/peak"),
            Promise.all([
                fetchJson("/api/results/source/regions"),
                fetchJson("/api/results/source/ips")
            ]).then(([regions, ips]) => ({regions, ips}))
        ]);
        renderTasks(tasks);
        renderRank(rank);
        renderPeak(peak);
        renderSource(source);
        renderRows(rank, peak, source);
        renderDashboardStats(tasks, rank, peak, source);
    } catch (error) {
        setNotice(error.message, "error");
    }
}

function renderDashboardStats(tasks, rank, peak, source) {
    const latest = tasks[0];
    const stats = [
        ["最新任务", latest ? `#${latest.id}` : "-", latest ? latest.status : "暂无任务", "accent-teal"],
        ["热度排行", rank.length, "页面维度", "accent-amber"],
        ["地区来源", source.regions.length, "展示维度", "accent-indigo"],
        ["用户IP", source.ips.length, "展示维度", "accent-rose"]
    ];
    const box = document.getElementById("dashboardStats");
    box.innerHTML = stats.map(item => `
        <article class="metric-card ${item[3]}">
            <span>${item[0]}</span>
            <strong>${item[1]}</strong>
            <em>${item[2]}</em>
        </article>
    `).join("");
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let detail = "";
        try {
            const body = await response.json();
            detail = body.message || body.error || JSON.stringify(body);
        } catch (ignore) {
            detail = await response.text();
        }
        throw new Error(`${url} 请求失败：${response.status} ${detail}`);
    }
    return response.json();
}

function setNotice(message, type) {
    const notice = document.getElementById("notice");
    notice.textContent = message;
    notice.className = `notice ${type === "error" || type === "success" ? type : ""}`;
}

function renderTasks(tasks) {
    const box = document.getElementById("taskList");
    box.innerHTML = "";
    tasks.slice(0, 6).forEach(task => {
        const item = document.createElement("div");
        item.className = "task-item";
        item.innerHTML = `
            <strong>#${task.id}</strong>
            <span>${task.inputPath}</span>
            <span class="status ${task.status}">${task.status}</span>
        `;
        box.appendChild(item);
    });
}

function renderRank(rows) {
    const topRows = rows.slice(0, 15).reverse();
    rankChart.setOption({
        color: [chartColors[0]],
        tooltip: {
            trigger: "axis",
            ...tooltipStyle,
            axisPointer: {type: "shadow"},
            formatter: params => {
                const item = params[0];
                const row = topRows[item.dataIndex];
                return `${row.site}${row.url}<br/>PV：${row.pv}`;
            }
        },
        grid: {left: 156, right: 28, bottom: 28, top: 12},
        xAxis: {
            type: "value",
            axisLine: axisLineStyle,
            axisTick: {show: false},
            axisLabel: {color: chartTextColor},
            splitLine: splitLineStyle
        },
        yAxis: {
            type: "category",
            data: topRows.map(row => `${row.site} ${row.url}`),
            axisLine: axisLineStyle,
            axisTick: {show: false},
            axisLabel: {
                color: chartTextColor,
                width: 136,
                overflow: "truncate"
            }
        },
        series: [{
            type: "bar",
            data: topRows.map(row => row.pv),
            barWidth: 12,
            itemStyle: {
                color: "#0f766e",
                borderRadius: [0, 5, 5, 0]
            },
            label: {
                show: true,
                position: "right",
                color: "#334155",
                fontWeight: 700
            },
            animationDuration: 650
        }]
    });
}

function renderPeak(rows) {
    const hours = Array.from({length: 24}, (_, index) => `${String(index).padStart(2, "0")}`);
    const sites = [...new Set(rows.map(row => row.site))];
    const valueMap = new Map(rows.map(row => [`${row.site}-${row.hour}`, row.pv]));
    const series = sites.map(site => ({
        name: site,
        type: "line",
        smooth: true,
        showSymbol: false,
        lineStyle: {width: 3},
        emphasis: {focus: "series"},
        data: hours.map(hour => valueMap.get(`${site}-${hour}`) || 0)
    }));
    peakChart.setOption({
        color: chartColors,
        tooltip: {trigger: "axis", ...tooltipStyle},
        legend: {
            type: "scroll",
            top: 0,
            left: 0,
            right: 0,
            textStyle: {color: "#334155"},
            pageIconColor: "#334155",
            pageIconInactiveColor: "#cbd5e1"
        },
        grid: {left: 48, right: 28, bottom: 46, top: 62},
        xAxis: {
            type: "category",
            data: hours.map(hour => `${hour}:00`),
            boundaryGap: false,
            axisLine: axisLineStyle,
            axisTick: {show: false},
            axisLabel: {color: chartTextColor}
        },
        yAxis: {
            type: "value",
            axisLine: {show: false},
            axisTick: {show: false},
            axisLabel: {color: chartTextColor},
            splitLine: splitLineStyle
        },
        series
    });
}

function renderSource(rows) {
    const regionRows = rows.regions.slice(0, 10);
    const legend = document.getElementById("sourceLegend");
    legend.innerHTML = regionRows.map((row, index) => `
        <div class="source-legend-item">
            <span class="legend-dot dot-${index % 10}"></span>
            <span class="legend-name" title="${row.site} · ${row.sourceValue}">${row.site} · ${row.sourceValue}</span>
            <strong>${row.pv}</strong>
        </div>
    `).join("");
    sourceChart.setOption({
        color: chartColors,
        tooltip: {trigger: "item", ...tooltipStyle},
        series: [{
            type: "pie",
            radius: ["48%", "76%"],
            center: ["50%", "50%"],
            label: {show: false},
            labelLine: {show: false},
            itemStyle: {
                borderColor: "#ffffff",
                borderWidth: 3,
                borderRadius: 5
            },
            data: regionRows.map(row => ({name: `${row.site}-${row.sourceValue}`, value: row.pv}))
        }]
    });
}

function renderRows(rank, peak, source) {
    const tbody = document.getElementById("resultRows");
    const rows = [
        ...rank.map(row => ["访问排行", row.site, row.url, row.pv]),
        ...peak.slice(0, 10).map(row => ["峰值时段", row.site, `${row.hour}:00`, row.pv]),
        ...source.regions.slice(0, 12).map(row => ["用户所在地区", row.site, row.sourceValue, row.pv]),
        ...source.ips.slice(0, 12).map(row => ["用户IP", row.site, row.sourceValue, row.pv])
    ];
    tbody.innerHTML = "";
    rows.forEach(row => {
        const tr = document.createElement("tr");
        tr.innerHTML = row.map(cell => `<td>${cell}</td>`).join("");
        tbody.appendChild(tr);
    });
}

window.addEventListener("resize", () => {
    rankChart.resize();
    peakChart.resize();
    sourceChart.resize();
});

refreshAll();

async function loadCurrentUser() {
    try {
        const user = await fetchJson("/api/auth/me");
        document.getElementById("currentUser").textContent = `${user.username} (${user.role})`;
    } catch (error) {
        window.location.href = "/login.html";
    }
}

async function logout() {
    await fetch("/api/auth/logout", {method: "POST"});
    window.location.href = "/login.html";
}
