<script setup>
import { ref } from 'vue'
import { staffStore } from '../stores/staff.js'

const showModal = ref(false)
const formData = ref({ name: '', phone: '', role: '店员' })

const handleSubmit = () => {
  if (formData.value.name && formData.value.phone) {
    staffStore.addStaff({ ...formData.value })
    showModal.value = false
    formData.value = { name: '', phone: '', role: '店员' }
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header header-flex">
      <div>
        <h2 class="page-title">营运团队管理</h2>
        <p class="page-desc">添加及管理所有门店店员信息</p>
      </div>
      <button class="btn-solid add-btn" @click="showModal = true">+ 录入新店员</button>
    </div>

    <div class="card-panel table-panel">
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th class="highlight-col">手机号 (登录账号)</th>
              <th>角色权限</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in staffStore.list" :key="s.id">
              <td data-label="ID / 编号" class="text-muted">#{{ String(s.id).padStart(3, '0') }}</td>
              <td data-label="姓名" class="font-bold">{{ s.name }}</td>
              <td data-label="手机号" class="highlight-col">{{ s.phone }}</td>
              <td data-label="角色权限"><span class="value-badge">{{ s.role }}</span></td>
              <td data-label="状态"><span class="text-green">{{ s.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 弹窗 / Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content card-panel">
        <h3 class="modal-title">添加后台操作店员</h3>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-item">
            <label>员工姓名</label>
            <input v-model="formData.name" type="text" required placeholder="如: 张三" />
          </div>
          <div class="form-item">
            <label>手机号 (自动作为登录账号)</label>
            <input v-model="formData.phone" type="tel" required placeholder="输入11位手机号码" />
          </div>
          <div class="form-item">
            <label>角色权限</label>
            <select v-model="formData.role">
              <option value="店长">店长 (Manager级别)</option>
              <option value="店员">店员 (操作员级别)</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="showModal = false">取消</button>
            <button type="submit" class="btn-solid submit-btn">确认新增</button>
          </div>
        </form>
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
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.add-btn {
  background: var(--tx-red); 
  border-radius: 8px;
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
  min-width: 600px;
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
.text-green { color: var(--tx-green); font-weight: bold; }

.value-badge {
  display: inline-block;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  border: 1px solid #ebebeb;
}

/* --- Modal Styles --- */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-content {
  width: 90%;
  max-width: 400px;
  padding: 30px;
}
.modal-title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 20px;
}
.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-item label {
  font-size: 13px;
  color: #555;
  font-weight: bold;
  margin-bottom: 6px;
  display: block;
}
.form-item input, .form-item select {
  width: 100%;
  padding: 12px;
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 15px;
}
.form-item input:focus, .form-item select:focus {
  outline: none;
  border-color: var(--tx-red);
  background: #fff;
}
.form-actions {
  display: flex;
  margin-top: 10px;
  gap: 12px;
}
.btn-outline {
  flex: 1;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-weight: bold;
  color: #333;
  cursor: pointer;
}
.submit-btn {
  flex: 2;
  background: var(--text-main);
  border-radius: 6px;
}

/* --- Mobile Card Layout Override --- */
@media (max-width: 768px) {
  .page-title { font-size: 20px; }
  .page-desc { font-size: 12px; }
  .add-btn { padding: 8px 12px; font-size: 13px; }
  .table-panel {
    padding: 15px;
    background: transparent;
    box-shadow: none;
  }
  .table-responsive { overflow: hidden; }
  .data-table { min-width: 100%; border: none; }
  .data-table thead { display: none; }
  .data-table tbody, .data-table tr, .data-table td {
    display: block; width: 100%;
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
  .data-table td:last-child { border-bottom: none; }
  .data-table td::before {
    content: attr(data-label);
    font-weight: bold;
    color: #888;
    margin-right: 15px;
    text-align: left;
  }
}
</style>
