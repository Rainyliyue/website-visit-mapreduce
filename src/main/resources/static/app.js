const rankChart = echarts.init(document.getElementById("rankChart"));
const peakChart = echarts.init(document.getElementById("peakChart"));
const sourceChart = echarts.init(document.getElementById("sourceChart"));

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
        await fetchJson("/api/tasks", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({inputPath, analysisType})
        });
        await refreshAll();
        setNotice("任务执行完成，结果已刷新。", "success");
    } catch (error) {
        setNotice(error.message, "error");
    } finally {
        button.disabled = false;
    }
});

async function refreshAll() {
    try {
        const [tasks, rank, peak, source] = await Promise.all([
            fetchJson("/api/tasks"),
            fetchJson("/api/results/rank"),
            fetchJson("/api/results/peak"),
            fetchJson("/api/results/source")
        ]);
        renderTasks(tasks);
        renderRank(rank);
        renderPeak(peak);
        renderSource(source);
        renderRows(rank, peak, source);
    } catch (error) {
        setNotice(error.message, "error");
    }
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
    const topRows = rows.slice(0, 20);
    rankChart.setOption({
        tooltip: {},
        grid: {left: 48, right: 20, bottom: 100, top: 20},
        xAxis: {type: "category", data: topRows.map(row => `${row.site}\n${row.url}`), axisLabel: {rotate: 35}},
        yAxis: {type: "value"},
        series: [{type: "bar", data: topRows.map(row => row.pv), itemStyle: {color: "#0f766e"}}]
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
        data: hours.map(hour => valueMap.get(`${site}-${hour}`) || 0)
    }));
    peakChart.setOption({
        tooltip: {trigger: "axis"},
        legend: {type: "scroll", top: 0},
        grid: {left: 42, right: 20, bottom: 42, top: 54},
        xAxis: {type: "category", data: hours.map(hour => `${hour}:00`)},
        yAxis: {type: "value"},
        series
    });
}

function renderSource(rows) {
    const regionRows = rows.filter(row => row.sourceType === "REGION").slice(0, 10);
    sourceChart.setOption({
        tooltip: {trigger: "item"},
        series: [{
            type: "pie",
            radius: ["42%", "70%"],
            data: regionRows.map(row => ({name: `${row.site}-${row.sourceValue}`, value: row.pv}))
        }]
    });
}

function renderRows(rank, peak, source) {
    const tbody = document.getElementById("resultRows");
    const rows = [
        ...rank.map(row => ["访问排行", row.site, row.url, row.pv]),
        ...peak.slice(0, 10).map(row => ["峰值时段", row.site, `${row.hour}:00`, row.pv]),
        ...source.slice(0, 10).map(row => ["来源分布", row.site, `${row.sourceType}:${row.sourceValue}`, row.pv])
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
