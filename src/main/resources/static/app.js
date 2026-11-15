// Application Controller

let monitors = [];
let dashboardStats = null;
let currentFilter = 'ALL';
let searchQuery = '';
let isPublicView = false;
let countdown = 10;
let refreshTimer = null;
let latencyChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
  lucide.createIcons();
  loadDashboard();
  startCountdown();
});

function startCountdown() {
  if (refreshTimer) clearInterval(refreshTimer);
  refreshTimer = setInterval(() => {
    countdown--;
    const el = document.getElementById('countdownText');
    if (el) el.textContent = `${countdown}s`;

    if (countdown <= 0) {
      countdown = 10;
      loadDashboard(true);
    }
  }, 1000);
}

async function loadDashboard(isSilent = false) {
  try {
    const [statsRes, monitorsRes] = await Promise.all([
      fetch('/api/dashboard/stats'),
      fetch('/api/monitors')
    ]);

    if (statsRes.ok) {
      dashboardStats = await statsRes.json();
      updateMetrics(dashboardStats);
    }

    if (monitorsRes.ok) {
      monitors = await monitorsRes.json();
      renderMonitors();
      if (isPublicView) {
        renderPublicView();
      }
    }
  } catch (err) {
    if (!isSilent) {
      showToast('Unable to synchronize data with server', 'error');
    }
  }
}

function updateMetrics(stats) {
  document.getElementById('metricUptime').textContent = `${stats.overallUptimePercentage}%`;
  document.getElementById('metricActive').textContent = stats.totalMonitors;
  document.getElementById('metricDown').textContent = stats.downCount;
  document.getElementById('metricLatency').textContent = `${stats.globalAvgLatencyMs} ms`;
  document.getElementById('metricIncidents').textContent = stats.activeIncidents;

  const tier = document.getElementById('latencyTier');
  if (stats.globalAvgLatencyMs < 200) {
    tier.textContent = 'Fast';
    tier.style.color = 'var(--color-green)';
  } else if (stats.globalAvgLatencyMs < 800) {
    tier.textContent = 'Nominal';
    tier.style.color = 'var(--text-muted)';
  } else {
    tier.textContent = 'Degraded';
    tier.style.color = 'var(--color-amber)';
  }

  const bannerTitle = document.getElementById('bannerTitle');
  const bannerSub = document.getElementById('bannerSub');
  const bannerDot = document.getElementById('bannerDot');

  if (stats.downCount > 0) {
    bannerTitle.textContent = `${stats.downCount} outage detected`;
    bannerSub.textContent = 'One or more critical services are unreachable';
    bannerDot.className = 'status-dot red';
  } else if (stats.degradedCount > 0) {
    bannerTitle.textContent = 'Degraded latency';
    bannerSub.textContent = 'Responses exceeding nominal response threshold (>1500ms)';
    bannerDot.className = 'status-dot amber';
  } else {
    bannerTitle.textContent = 'All systems operational';
    bannerSub.textContent = 'All scheduled health probes responding within thresholds';
    bannerDot.className = 'status-dot green';
  }
}

function handleSearch(val) {
  searchQuery = val.toLowerCase().trim();
  renderMonitors();
}

function renderMonitors() {
  const tbody = document.getElementById('monitorsTableBody');
  if (!tbody) return;

  let filtered = monitors;
  if (currentFilter !== 'ALL') {
    filtered = monitors.filter(m => m.status === currentFilter);
  }

  if (searchQuery) {
    filtered = filtered.filter(m =>
      m.name.toLowerCase().includes(searchQuery) ||
      m.url.toLowerCase().includes(searchQuery)
    );
  }

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align: center; padding: 36px; color: var(--text-dim);">
          No monitor records match the filter criteria.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(m => {
    let dotClass = 'green';
    let statusText = 'Operational';

    if (m.status === 'DEGRADED') {
      dotClass = 'amber';
      statusText = 'Degraded';
    } else if (m.status === 'DOWN') {
      dotClass = 'red';
      statusText = 'Down';
    } else if (m.status === 'PAUSED') {
      dotClass = 'gray';
      statusText = 'Paused';
    }

    const latency = m.lastLatencyMs ? `${m.lastLatencyMs} ms` : '—';
    const uptime = m.uptimePercentage ? `${m.uptimePercentage}%` : '100.0%';
    const lastChecked = formatRelativeTime(m.lastCheckedAt);
    const sparkHtml = renderSparkline(m.recentLogs || []);
    const isPaused = m.status === 'PAUSED';

    return `
      <tr>
        <td>
          <span class="status-badge ${dotClass}">
            <span class="status-dot ${dotClass}"></span>
            ${statusText}
          </span>
        </td>
        <td>
          <div class="monitor-name-cell">
            <span class="monitor-name" onclick="openDetailsModal(${m.id})">${escapeHtml(m.name)}</span>
            <a href="${escapeHtml(m.url)}" target="_blank" class="monitor-url" title="${escapeHtml(m.url)}">${escapeHtml(m.url)}</a>
          </div>
        </td>
        <td>
          <span class="method-tag">${m.httpMethod}</span>
        </td>
        <td>
          <span class="mono-cell">${latency}</span>
        </td>
        <td>
          <div class="sparkline-row" title="Last 20 probes">
            ${sparkHtml}
          </div>
        </td>
        <td>
          <span class="mono-cell">${uptime}</span>
        </td>
        <td>
          <span style="font-size: 11px; color: var(--text-muted);">${lastChecked}</span>
        </td>
        <td style="text-align: right;">
          <div class="action-row">
            <button onclick="triggerPing(${m.id})" class="btn-icon" title="Run probe now">
              <i data-lucide="play" style="width: 12px; height: 12px;"></i>
            </button>
            <button onclick="openDetailsModal(${m.id})" class="btn-icon" title="View metrics chart">
              <i data-lucide="bar-chart-2" style="width: 12px; height: 12px;"></i>
            </button>
            <button onclick="togglePause(${m.id})" class="btn-icon" title="${isPaused ? 'Resume' : 'Pause'}">
              <i data-lucide="${isPaused ? 'play-circle' : 'pause'}" style="width: 12px; height: 12px;"></i>
            </button>
            <button onclick="deleteMonitor(${m.id})" class="btn-icon danger" title="Delete monitor">
              <i data-lucide="trash-2" style="width: 12px; height: 12px;"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  lucide.createIcons();
}

function renderSparkline(logs) {
  if (!logs || logs.length === 0) {
    return `<span style="font-size: 10px; color: var(--text-dim); font-family: var(--font-mono);">no data</span>`;
  }

  const recent = logs.slice(0, 20).reverse();
  return recent.map(log => {
    let cls = 'green';
    if (!log.successful) cls = 'red';
    else if (log.responseTimeMs > 1500) cls = 'amber';
    return `<span class="spark-bar ${cls}" title="${log.responseTimeMs}ms (${log.statusCode})"></span>`;
  }).join('');
}

async function triggerPing(id) {
  showToast('Dispatching health probe...', 'info');
  try {
    const res = await fetch(`/api/monitors/${id}/ping`, { method: 'POST' });
    if (res.ok) {
      const log = await res.json();
      const status = log.successful ? 'Healthy' : 'Failure';
      showToast(`Probe result: HTTP ${log.statusCode} in ${log.responseTimeMs}ms (${status})`, log.successful ? 'success' : 'error');
      countdown = 10;
      await loadDashboard(true);
    } else {
      showToast('Probe execution failed', 'error');
    }
  } catch (e) {
    showToast('Network error during probe', 'error');
  }
}

async function togglePause(id) {
  try {
    const res = await fetch(`/api/monitors/${id}/toggle`, { method: 'POST' });
    if (res.ok) {
      showToast('Monitor state updated', 'success');
      await loadDashboard(true);
    }
  } catch (e) {
    showToast('Failed to toggle monitor state', 'error');
  }
}

async function deleteMonitor(id) {
  if (!confirm('Permanently delete this monitor and its logs?')) return;

  try {
    const res = await fetch(`/api/monitors/${id}`, { method: 'DELETE' });
    if (res.ok) {
      showToast('Monitor deleted', 'success');
      await loadDashboard(true);
    }
  } catch (e) {
    showToast('Delete request failed', 'error');
  }
}

async function handleSaveMonitor(e) {
  e.preventDefault();
  const saveBtn = document.getElementById('saveMonitorBtn');
  saveBtn.disabled = true;
  saveBtn.textContent = 'Saving...';

  const payload = {
    name: document.getElementById('inputName').value.trim(),
    url: document.getElementById('inputUrl').value.trim(),
    httpMethod: document.getElementById('inputMethod').value,
    expectedStatusCode: parseInt(document.getElementById('inputExpectedStatus').value, 10),
    intervalSeconds: parseInt(document.getElementById('inputInterval').value, 10)
  };

  try {
    const res = await fetch('/api/monitors', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      showToast('Target registered successfully', 'success');
      closeModal();
      countdown = 10;
      await loadDashboard(true);
    } else {
      const err = await res.json();
      showToast(err.message || 'Validation error saving target', 'error');
    }
  } catch (err) {
    showToast('Network error while saving target', 'error');
  } finally {
    saveBtn.disabled = false;
    saveBtn.textContent = 'Save Target';
  }
}

function openModal() {
  document.getElementById('monitorForm').reset();
  document.getElementById('createModal').style.display = 'flex';
}

function closeModal() {
  document.getElementById('createModal').style.display = 'none';
}

async function openDetailsModal(id) {
  const monitor = monitors.find(m => m.id === id);
  if (!monitor) return;

  document.getElementById('modalMonitorName').textContent = monitor.name;
  document.getElementById('modalMonitorUrl').textContent = `${monitor.httpMethod} ${monitor.url}`;
  document.getElementById('detailsModal').style.display = 'flex';

  const logs = monitor.recentLogs || [];
  document.getElementById('chartStats').textContent = `Avg: ${monitor.avgResponseTimeMs || 0} ms | Uptime: ${monitor.uptimePercentage || 100}%`;

  const tbody = document.getElementById('logsTableBody');
  if (logs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; padding: 20px; color: var(--text-dim);">No probes logged yet</td></tr>`;
  } else {
    tbody.innerHTML = logs.map(l => {
      const timeStr = new Date(l.timestamp).toLocaleTimeString();
      const badge = l.successful
        ? `<span style="color: var(--color-green);">PASS</span>`
        : `<span style="color: var(--color-red);">FAIL</span>`;
      return `
        <tr>
          <td style="padding: 6px 12px; color: var(--text-muted);">${timeStr}</td>
          <td style="padding: 6px 12px;">${l.statusCode || 0}</td>
          <td style="padding: 6px 12px;">${l.responseTimeMs} ms</td>
          <td style="padding: 6px 12px;">${badge} ${l.errorMessage ? `<span style="color: var(--text-dim);">(${escapeHtml(l.errorMessage)})</span>` : ''}</td>
        </tr>
      `;
    }).join('');
  }

  renderLatencyChart(logs);
}

function closeDetailsModal() {
  document.getElementById('detailsModal').style.display = 'none';
}

function renderLatencyChart(logs) {
  const ctx = document.getElementById('latencyChart').getContext('2d');
  if (latencyChartInstance) {
    latencyChartInstance.destroy();
  }

  const sorted = [...logs].reverse();
  const labels = sorted.map(l => new Date(l.timestamp).toLocaleTimeString());
  const data = sorted.map(l => l.responseTimeMs);

  latencyChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Duration (ms)',
        data: data,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.08)',
        borderWidth: 1.5,
        pointRadius: 2,
        pointBackgroundColor: '#10b981',
        tension: 0.2,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: {
          grid: { color: 'rgba(34, 38, 48, 0.6)' },
          ticks: { color: '#8b949e', font: { size: 10, family: 'monospace' } }
        },
        y: {
          grid: { color: 'rgba(34, 38, 48, 0.6)' },
          ticks: { color: '#8b949e', font: { size: 10, family: 'monospace' } },
          beginAtZero: true
        }
      }
    }
  });
}

function toggleStatusView() {
  isPublicView = !isPublicView;
  const dashSec = document.getElementById('dashboardSection');
  const pubSec = document.getElementById('publicStatusSection');
  const toggleText = document.getElementById('viewToggleText');

  if (isPublicView) {
    dashSec.style.display = 'none';
    pubSec.style.display = 'flex';
    toggleText.textContent = 'Dashboard';
    renderPublicView();
  } else {
    pubSec.style.display = 'none';
    dashSec.style.display = 'block';
    toggleText.textContent = 'Public Status';
  }
}

function showDashboardView() {
  if (isPublicView) toggleStatusView();
}

async function renderPublicView() {
  const list = document.getElementById('publicServicesList');
  if (!list) return;

  list.innerHTML = monitors.map(m => {
    let dotClass = 'green';
    let statusLabel = 'Operational';

    if (m.status === 'DEGRADED') {
      dotClass = 'amber';
      statusLabel = 'Degraded';
    } else if (m.status === 'DOWN') {
      dotClass = 'red';
      statusLabel = 'Outage';
    }

    return `
      <div style="padding: 12px 0; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-subtle);">
        <div>
          <span style="font-weight: 600; font-size: 13px;">${escapeHtml(m.name)}</span>
          <div style="font-family: var(--font-mono); font-size: 11px; color: var(--text-dim); margin-top: 2px;">${escapeHtml(m.url)}</div>
        </div>
        <div style="display: flex; align-items: center; gap: 12px;">
          <span style="font-family: var(--font-mono); font-size: 12px; color: var(--text-muted);">${m.uptimePercentage || 100.0}%</span>
          <span class="status-badge ${dotClass}">
            <span class="status-dot ${dotClass}"></span>
            ${statusLabel}
          </span>
        </div>
      </div>
    `;
  }).join('');

  try {
    const res = await fetch('/api/dashboard/incidents');
    if (res.ok) {
      const incidents = await res.json();
      const container = document.getElementById('incidentsList');
      if (incidents.length > 0) {
        container.innerHTML = incidents.map(i => `
          <div style="padding: 8px 12px; background: var(--bg-surface); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); margin-bottom: 6px; display: flex; justify-content: space-between; align-items: center;">
            <div>
              <span style="font-weight: 600;">${escapeHtml(i.monitor.name)}</span>:
              <span style="color: var(--text-muted);">${escapeHtml(i.rootCause || 'Unreachable')}</span>
              <div style="font-size: 10px; color: var(--text-dim); margin-top: 2px;">Started: ${new Date(i.startedAt).toLocaleString()}</div>
            </div>
            <span class="status-badge ${i.status === 'RESOLVED' ? 'green' : 'red'}">${i.status}</span>
          </div>
        `).join('');
      }
    }
  } catch (e) {}

  lucide.createIcons();
}

function setFilter(filter) {
  currentFilter = filter;
  document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
  const btn = document.getElementById(`filter${filter}`);
  if (btn) btn.classList.add('active');
  renderMonitors();
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = 'toast';
  if (type === 'error') toast.style.borderColor = 'var(--color-red-border)';
  if (type === 'success') toast.style.borderColor = 'var(--color-green-border)';
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(4px)';
    setTimeout(() => toast.remove(), 250);
  }, 3000);
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function formatRelativeTime(isoStr) {
  if (!isoStr) return 'Pending';
  const seconds = Math.floor((new Date() - new Date(isoStr)) / 1000);
  if (seconds < 15) return 'Just now';
  if (seconds < 60) return `${seconds}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  return `${hours}h ago`;
}
