<script setup>
import { logStore } from '../stores/logs.js'
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志审计</h2>
      <p class="page-desc">实时监控员工引发的资产加减操作及留痕</p>
    </div>

    <div class="card-panel table-panel">
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>操作员</th>
              <th>操作时间</th>
              <th>关联客户</th>
              <th>操作事项</th>
              <th>触发变动</th>
              <th>日志快照</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in logStore.list" :key="l.id">
              <td data-label="操作员">
                <span class="user-badge">{{ l.staff }}</span>
              </td>
              <td data-label="操作时间" class="text-muted">{{ l.time }}</td>
              <td data-label="关联客户">
                <strong>{{ l.target.split(' ')[0] }}</strong>
                <span class="text-muted mobile-phone">{{ l.target.split(' ')[1] }}</span>
              </td>
              <td data-label="操作事项">
                <span class="type-badge">{{ l.type }}</span>
              </td>
              <td data-label="触发变动" :class="l.value.includes('+') ? 'text-green' : 'text-red'" class="val-text">
                {{ l.value }}
              </td>
              <td data-label="日志快照" class="diff-col">{{ l.diff }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-main);
}
.page-desc {
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 5px;
}

.table-panel {
  padding: 30px;
  flex-grow: 1;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  min-width: 650px;
}

.data-table th {
  padding: 16px 12px;
  color: #111;
  font-weight: bold;
  border-bottom: 2px solid #eee;
  font-size: 14px;
  white-space: nowrap;
}

.data-table td {
  padding: 16px 12px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
  font-size: 14px;
  white-space: nowrap;
}

.data-table tr:hover td {
  background: #fafafa;
}

.text-muted { color: var(--text-muted); }
.text-green { color: var(--tx-green); }
.text-red { color: var(--tx-red); }

.val-text {
  font-weight: bold; 
  font-size: 16px;
}

.user-badge {
  display: inline-block;
  background: #111;
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.type-badge {
  display: inline-block;
  background: #f5f2eb;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid #e0ddd6;
  color: #111;
}

.diff-col {
  color: #777;
  font-family: Menlo, Monaco, monospace;
  background: #fafafa;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  border: 1px solid #eee;
}

.mobile-break { display: none; }
.mobile-phone { margin-left: 6px; }

/* ====================
   H5 (Mobile) 响应式适配
   ==================== */
@media (max-width: 768px) {
  .page-header {
    margin-bottom: 15px;
  }
  .page-title {
    font-size: 22px;
  }
  .page-desc {
    font-size: 12px;
  }
  .table-panel {
    padding: 15px;
    background: transparent;
    box-shadow: none;
  }

  /* 将表格转换为卡片列表展示 (Card Layout for H5) */
  .table-responsive {
    overflow: hidden;
  }
  .data-table {
    min-width: 100%;
    border: none;
  }
  .data-table thead {
    display: none;
  }
  .data-table tbody, .data-table tr, .data-table td {
    display: block;
    width: 100%;
  }
  .data-table tr {
    margin-bottom: 20px;
    background: #fff;
    border-radius: 12px;
    padding: 15px 20px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.06);
    border: 1px solid #f0f0f0;
  }
  .data-table td {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px dashed #eee;
    text-align: right;
    font-size: 14px;
    white-space: normal;
  }
  .data-table td:last-child {
    border-bottom: none;
  }
  .data-table td::before {
    content: attr(data-label);
    font-weight: bold;
    color: #888;
    margin-right: 15px;
    text-align: left;
  }
  .val-text {
    font-size: 16px;
  }
  .mobile-break { display: none; }
  .mobile-phone { margin-left: 6px; font-size: 12px;}
}
</style>
