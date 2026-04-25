<script setup>
import { computed, onMounted, ref } from 'vue'
import { logStore } from '../stores/logs.js'

const operationTypes = [
  { value: 'add_points', label: '加积分' },
  { value: 'sub_points', label: '减积分' },
  { value: 'add_wine', label: '加酒' },
  { value: 'sub_wine', label: '减酒' },
  { value: 'add_balance', label: '加储值' },
  { value: 'sub_balance', label: '减储值' }
]

const loading = ref(false)
const errorMsg = ref('')
const filters = ref({
  memberId: '',
  staffId: '',
  operationType: '',
  startTime: '',
  endTime: ''
})

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(logStore.total / logStore.pageSize))
})

const formatDateTime = (value) => {
  if (!value) {
    return ''
  }

  return value.replace('T', ' ') + ':00'
}

const resolveQuery = (pageNum = 1) => ({
  memberId: filters.value.memberId.trim(),
  staffId: filters.value.staffId.trim(),
  operationType: filters.value.operationType,
  startTime: formatDateTime(filters.value.startTime),
  endTime: formatDateTime(filters.value.endTime),
  pageNum,
  pageSize: logStore.pageSize
})

const loadLogs = async (pageNum = 1) => {
  loading.value = true
  errorMsg.value = ''

  try {
    await logStore.loadLogs(resolveQuery(pageNum))
  } catch (error) {
    errorMsg.value = error?.message || '操作日志加载失败'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadLogs(1)
}

const handleReset = () => {
  filters.value = {
    memberId: '',
    staffId: '',
    operationType: '',
    startTime: '',
    endTime: ''
  }
  loadLogs(1)
}

const changePage = (pageNum) => {
  if (pageNum < 1 || pageNum > totalPages.value || pageNum === logStore.pageNum) {
    return
  }

  loadLogs(pageNum)
}

onMounted(() => {
  loadLogs()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志审计</h2>
      <p class="page-desc">实时监控员工引发的资产加减操作及留痕</p>
    </div>

    <div class="card-panel table-panel">
      <form class="filter-bar" @submit.prevent="handleSearch">
        <input v-model="filters.memberId" type="text" placeholder="会员ID" />
        <input v-model="filters.staffId" type="text" placeholder="员工ID" />
        <select v-model="filters.operationType">
          <option value="">全部操作</option>
          <option v-for="item in operationTypes" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
        <input v-model="filters.startTime" type="datetime-local" />
        <input v-model="filters.endTime" type="datetime-local" />
        <button class="btn-solid filter-btn" type="submit" :disabled="loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
        <button class="btn-outline filter-btn" type="button" @click="handleReset">重置</button>
      </form>

      <div v-if="errorMsg" class="page-error">{{ errorMsg }}</div>

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
            <tr v-if="loading">
              <td colspan="6" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="!logStore.list.length">
              <td colspan="6" class="empty-row">暂无操作日志</td>
            </tr>
            <template v-else>
              <tr v-for="l in logStore.list" :key="l.id">
                <td data-label="操作员">
                  <span class="user-badge">{{ l.staff }}</span>
                </td>
                <td data-label="操作时间" class="text-muted">{{ l.time }}</td>
                <td data-label="关联客户">
                  <strong>{{ l.memberName }}</strong>
                  <span v-if="l.memberId" class="text-muted mobile-phone">#{{ l.memberId }}</span>
                </td>
                <td data-label="操作事项">
                  <span class="type-badge">{{ l.type }}</span>
                </td>
                <td data-label="触发变动" :class="l.value.includes('+') ? 'text-green' : 'text-red'" class="val-text">
                  {{ l.value }}
                </td>
                <td data-label="日志快照" class="diff-col">{{ l.diff }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="pager">
        <span>共 {{ logStore.total }} 条</span>
        <div class="pager-actions">
          <button type="button" class="pager-btn" :disabled="logStore.pageNum <= 1 || loading" @click="changePage(logStore.pageNum - 1)">
            上一页
          </button>
          <span>{{ logStore.pageNum }} / {{ totalPages }}</span>
          <button type="button" class="pager-btn" :disabled="logStore.pageNum >= totalPages || loading" @click="changePage(logStore.pageNum + 1)">
            下一页
          </button>
        </div>
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

.filter-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(120px, 1fr)) auto auto;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-bar input,
.filter-bar select {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  background: #f7f7f7;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  color: var(--text-main);
  font-size: 14px;
}

.filter-bar input:focus,
.filter-bar select:focus {
  outline: none;
  border-color: var(--tx-red);
  background: #fff;
}

.filter-btn {
  min-width: 84px;
  height: 42px;
  padding: 0 16px;
}

.filter-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-outline {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  color: #333;
  cursor: pointer;
  font-weight: bold;
}

.btn-outline:hover {
  border-color: var(--tx-red);
  color: var(--tx-red);
}

.page-error {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(193, 39, 45, 0.1);
  color: var(--tx-red);
  font-size: 14px;
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

.empty-row {
  text-align: center;
  color: var(--text-muted);
  padding: 32px 12px !important;
}

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

.pager {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 18px;
  color: var(--text-muted);
  font-size: 14px;
}

.pager-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pager-btn {
  height: 34px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  color: #333;
  cursor: pointer;
}

.pager-btn:disabled {
  color: #aaa;
  cursor: not-allowed;
  background: #f7f7f7;
}

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
  .filter-bar {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }
  .filter-bar input[type="datetime-local"],
  .filter-bar select {
    grid-column: span 2;
  }
  .filter-btn {
    width: 100%;
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
  .pager {
    flex-direction: column;
    align-items: stretch;
    background: #fff;
    border-radius: 12px;
    padding: 12px;
  }
  .pager-actions {
    justify-content: space-between;
  }
}
</style>
