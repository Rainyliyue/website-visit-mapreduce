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

function setAdminNotice(message, type = "") {
    const notice = document.getElementById("adminNotice");
    notice.textContent = message;
    notice.className = `notice ${type}`;
}

function renderUsers(users) {
    const tbody = document.getElementById("userRows");
    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.role}</td>
            <td>${user.createdAt}</td>
        </tr>
    `).join("");
}

function renderTasks(tasks) {
    const tbody = document.getElementById("taskRows");
    tbody.innerHTML = tasks.map(task => {
        const deletable = task.status === "SUCCESS" || task.status === "FAILED";
        const action = deletable
            ? `<button class="table-action danger" data-delete-task="${task.id}">删除</button>`
            : `<span class="muted">运行中不可删</span>`;
        return `
            <tr>
                <td>${task.id}</td>
                <td>${task.inputPath}</td>
                <td><span class="status ${task.status}">${task.status}</span></td>
                <td>${task.finishedAt ?? ""}</td>
                <td>${action}</td>
            </tr>
        `;
    }).join("");
}

function renderDataSummary(data) {
    const summary = document.getElementById("dataSummary");
    const counts = data.counts || {
        rank: data.rank.length,
        peak: data.peak.length,
        regions: data.regions.length,
        ips: data.ips.length,
        logs: data.logs?.length || 0
    };
    const items = [
        ["访问量排行", counts.rank],
        ["时段峰值", counts.peak],
        ["来源地区", counts.regions],
        ["来源IP", counts.ips],
        ["访问明细", counts.logs]
    ];
    summary.innerHTML = items.map(item => `
        <div class="summary-item">
            <strong>${item[1]}</strong>
            <span>${item[0]}</span>
        </div>
    `).join("");

    const cacheRows = document.getElementById("cacheRows");
    cacheRows.innerHTML = [
        ["visit_rank", counts.rank, "网站访问量热度排行结果，可在数据管理中单独删除"],
        ["visit_peak", counts.peak, "各网站按小时统计的访问峰值数据，可在数据管理中单独删除"],
        ["source_distribution:REGION", counts.regions, "用户所在地区统计结果，可单独删除"],
        ["source_distribution:IP", counts.ips, "用户 IP 统计结果，可单独删除"],
        ["access_logs", counts.logs, "原始访问明细，包含用户IP、地区、访问时间、目标地址"]
    ].map(row => `<tr>${row.map(cell => `<td>${cell}</td>`).join("")}</tr>`).join("");
}

function renderLogs(logs) {
    const tbody = document.getElementById("logRows");
    tbody.innerHTML = (logs || []).map(log => `
        <tr>
            <td>${log.visitDate}</td>
            <td>${log.visitTime}</td>
            <td>${log.site}</td>
            <td>${log.targetAddress}</td>
            <td>${log.ip}</td>
            <td>${log.region}</td>
            <td>${log.status}</td>
            <td>${log.bytes}</td>
        </tr>
    `).join("");
}

async function refreshAdmin() {
    const [me, users, tasks, data] = await Promise.all([
        fetchJson("/api/auth/me"),
        fetchJson("/api/admin/users"),
        fetchJson("/api/admin/tasks"),
        fetchJson("/api/admin/data")
    ]);
    document.getElementById("currentUser").textContent = `${me.username} (${me.role})`;
    renderUsers(users);
    renderTasks(tasks);
    renderDataSummary(data);
    renderLogs(data.logs);
}

document.getElementById("logoutBtn").addEventListener("click", async () => {
    await fetch("/api/auth/logout", {method: "POST"});
    window.location.href = "/login.html";
});

document.getElementById("clearBtn").addEventListener("click", async () => {
    if (!confirm("确认清理三张分析结果表吗？任务记录会保留。")) {
        return;
    }
    await fetchJson("/api/admin/cache", {method: "DELETE"});
    setAdminNotice("分析缓存已清理，结果表记录数已归零。", "success");
    await refreshAdmin();
});

document.getElementById("taskRows").addEventListener("click", async event => {
    const button = event.target.closest("[data-delete-task]");
    if (!button) {
        return;
    }
    const id = button.dataset.deleteTask;
    if (!confirm(`确认删除任务 #${id} 吗？仅 SUCCESS/FAILED 任务允许删除。`)) {
        return;
    }
    await fetchJson(`/api/admin/tasks/${id}`, {method: "DELETE"});
    setAdminNotice(`任务 #${id} 已删除。`, "success");
    await refreshAdmin();
});

document.querySelectorAll("[data-clear-data]").forEach(button => {
    button.addEventListener("click", async () => {
        const type = button.dataset.clearData;
        const labels = {
            rank: "访问量排行",
            peak: "时段峰值",
            regions: "来源地区",
            ips: "来源IP",
            logs: "访问明细"
        };
        if (!confirm(`确认删除 ${labels[type]} 统计数据吗？`)) {
            return;
        }
        await fetchJson(`/api/admin/data/${type}`, {method: "DELETE"});
        setAdminNotice(`${labels[type]} 统计数据已删除。`, "success");
        await refreshAdmin();
    });
});

document.getElementById("clearCompletedCacheBtn").addEventListener("click", async () => {
    if (!confirm("确认删除已完成任务缓存吗？这会清空三类统计结果，并删除 SUCCESS/FAILED 任务记录。")) {
        return;
    }
    const result = await fetchJson("/api/admin/cache/completed", {method: "DELETE"});
    setAdminNotice(`已完成任务缓存已删除，同时删除任务记录 ${result.deletedTasks ?? 0} 条。`, "success");
    await refreshAdmin();
});

document.getElementById("deleteCompletedTasksBtn").addEventListener("click", async () => {
    if (!confirm("确认批量删除 SUCCESS/FAILED 状态任务吗？统计结果数据会保留。")) {
        return;
    }
    const result = await fetchJson("/api/admin/tasks/completed", {method: "DELETE"});
    setAdminNotice(`已删除完成/异常任务 ${result.deleted ?? 0} 条。`, "success");
    await refreshAdmin();
});

refreshAdmin().catch(error => setAdminNotice(error.message, "error"));
