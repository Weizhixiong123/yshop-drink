<script setup>
import { memberStore } from '../stores/members.js'
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">全量会员档案</h2>
      <p class="page-desc">包含完全开放的客户联系方式与资产</p>
    </div>

    <!-- 白色实心卡片容器 -->
    <div class="card-panel table-panel">
      <div class="notice">
        <strong class="notice-tag">最高权限</strong>
        <span>查重逻辑已关闭，完整手机号强制可见。</span>
      </div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>ID</th>
              <th>姓名</th>
              <th>性别</th>
              <th class="highlight-col">手机号</th>
              <th>存酒</th>
              <th>积分</th>
              <th>余额</th>
              <th>后台备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in memberStore.list" :key="m.id">
              <td data-label="ID / 编号" class="text-muted">#{{ String(m.id).padStart(3, '0') }}</td>
              <td data-label="姓名" class="font-bold">{{ m.name }}</td>
              <td data-label="性别">{{ m.gender }}</td>
              <td data-label="手机号" class="highlight-col">{{ m.phone }}</td>
              <td data-label="存酒"><span class="value-badge">{{ m.liquor }}</span></td>
              <td data-label="积分"><span class="value-badge">{{ m.points }}</span></td>
              <td data-label="余额"><span class="value-badge">{{ m.balance }}</span></td>
              <td data-label="后台备注" class="remark-cell">{{ m.remark }}</td>
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

.notice {
  background: #fdf5f5;
  border-left: 4px solid var(--tx-red);
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #555;
  display: flex;
  align-items: center;
  gap: 12px;
}
.notice-tag {
  background: var(--tx-red);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
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
  min-width: 800px; /* 强制移动端发生横向溢出 */
}

.data-table th {
  padding: 16px 12px;
  color: #111;
  font-weight: bold;
  border-bottom: 2px solid #eee;
  font-size: 14px;
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

.highlight-col {
  color: var(--tx-red) !important;
  font-weight: bold;
}
.font-bold { font-weight: bold; }
.text-muted { color: var(--text-muted); }

.value-badge {
  display: inline-block;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  border: 1px solid #ebebeb;
}

.remark-cell {
  color: #888;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  .notice {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    font-size: 13px;
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
    display: none; /* 隐藏传统表头 */
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
  /* 注入通过 data-label 设置的属性标题 */
  .data-table td::before {
    content: attr(data-label);
    font-weight: bold;
    color: #888;
    margin-right: 15px;
    text-align: left;
  }
  
  .remark-cell {
    max-width: 60%;
    text-align: right;
  }
}
</style>
