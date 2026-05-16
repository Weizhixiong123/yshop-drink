<script setup>
import { onMounted, ref } from 'vue'
import { staffStore } from '../stores/staff.js'

const showModal = ref(false)
const editingStaffId = ref('')
const loading = ref(false)
const submitting = ref(false)
const deletingStaffId = ref('')
const errorMsg = ref('')
const formError = ref('')
const formData = ref({ name: '', phone: '', role: 'staff' })

const phonePattern = /^1[3-9]\d{9}$/

const resetForm = () => {
  formData.value = { name: '', phone: '', role: 'staff' }
  editingStaffId.value = ''
  formError.value = ''
}

const openAddModal = () => {
  resetForm()
  showModal.value = true
}

const openEditModal = (staff) => {
  editingStaffId.value = staff.id
  formData.value = {
    name: staff.name,
    phone: staff.phone,
    role: staff.rawRole || 'staff'
  }
  formError.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const formatStaffId = (id) => {
  if (!id) {
    return '---'
  }
  return String(id).slice(0, 8).toUpperCase()
}

const loadStaff = async () => {
  loading.value = true
  errorMsg.value = ''

  try {
    await staffStore.loadStaff()
  } catch (error) {
    errorMsg.value = error?.message || '店员列表加载失败'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const payload = {
    id: editingStaffId.value,
    name: formData.value.name.trim(),
    phone: formData.value.phone.trim(),
    role: formData.value.role
  }

  if (!payload.name) {
    formError.value = '姓名不能为空'
    return
  }

  if (payload.name.length > 50) {
    formError.value = '姓名不能超过 50 字'
    return
  }

  if (!payload.phone) {
    formError.value = '手机号不能为空'
    return
  }

  if (!phonePattern.test(payload.phone)) {
    formError.value = '手机号格式不正确'
    return
  }

  if (!['staff', 'manager'].includes(payload.role)) {
    formError.value = '请选择正确的角色权限'
    return
  }

  submitting.value = true
  formError.value = ''

  try {
    if (editingStaffId.value) {
      await staffStore.updateStaff(payload)
    } else {
      await staffStore.addStaff(payload)
    }
    closeModal()
  } catch (error) {
    formError.value = error?.message || (editingStaffId.value ? '修改员工信息失败' : '录入店员失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (staff) => {
  const confirmed = window.confirm(`确认删除店员「${staff.name}」的后台权限吗？`)

  if (!confirmed) {
    return
  }

  deletingStaffId.value = staff.id
  errorMsg.value = ''

  try {
    await staffStore.deleteStaff(staff.id)
  } catch (error) {
    errorMsg.value = error?.message || '删除员工失败'
  } finally {
    deletingStaffId.value = ''
  }
}

onMounted(() => {
  loadStaff()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header header-flex">
      <div>
        <h2 class="page-title">营运团队管理</h2>
        <p class="page-desc">添加及管理所有门店店员信息</p>
      </div>
      <button class="btn-solid add-btn" @click="openAddModal">+ 录入新店员</button>
    </div>

    <div class="card-panel table-panel">
      <div v-if="errorMsg" class="page-error">{{ errorMsg }}</div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th class="highlight-col">手机号 (登录账号)</th>
              <th>角色权限</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="!staffStore.list.length">
              <td colspan="6" class="empty-row">暂无店员数据</td>
            </tr>
            <template v-else>
              <tr v-for="s in staffStore.list" :key="s.id">
                <td data-label="ID / 编号" class="text-muted">#{{ formatStaffId(s.id) }}</td>
                <td data-label="姓名" class="font-bold">{{ s.name }}</td>
                <td data-label="手机号" class="highlight-col">{{ s.phone }}</td>
                <td data-label="角色权限"><span class="value-badge">{{ s.role }}</span></td>
                <td data-label="状态">
                  <span :class="s.rawStatus === 0 ? 'text-red' : 'text-green'">{{ s.status }}</span>
                </td>
                <td data-label="操作">
                  <div class="table-actions">
                    <button type="button" class="table-action" @click="openEditModal(s)">修改</button>
                    <button
                      type="button"
                      class="table-action danger"
                      :disabled="deletingStaffId === s.id"
                      @click="handleDelete(s)"
                    >
                      {{ deletingStaffId === s.id ? '删除中' : '删除' }}
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 弹窗 / Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content card-panel">
        <h3 class="modal-title">{{ editingStaffId ? '修改店员信息' : '添加后台操作店员' }}</h3>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-item">
            <label>员工姓名</label>
            <input v-model="formData.name" type="text" maxlength="50" required placeholder="如: 张三" />
          </div>
          <div class="form-item">
            <label>手机号 (自动作为登录账号)</label>
            <input v-model="formData.phone" type="tel" maxlength="11" required placeholder="输入11位手机号码" />
          </div>
          <div class="form-item">
            <label>角色权限</label>
            <select v-model="formData.role" required>
              <option value="staff">店员</option>
              <option value="manager">店长</option>
            </select>
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-solid submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : (editingStaffId ? '确认修改' : '确认新增') }}
            </button>
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

.page-error {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(193, 39, 45, 0.1);
  color: var(--tx-red);
  font-size: 14px;
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
  min-width: 720px;
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
.text-red { color: var(--tx-red); font-weight: bold; }

.empty-row {
  text-align: center;
  color: var(--text-muted);
  padding: 32px 12px !important;
}

.value-badge {
  display: inline-block;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  border: 1px solid #ebebeb;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.table-action {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-weight: bold;
  cursor: pointer;
}

.table-action:hover {
  border-color: var(--tx-red);
  color: var(--tx-red);
}

.table-action.danger {
  border-color: rgba(193, 39, 45, 0.25);
  color: var(--tx-red);
}

.table-action.danger:hover {
  background: rgba(193, 39, 45, 0.08);
}

.table-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
.form-item input,
.form-item select {
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

.form-item input:disabled {
  color: #666;
  cursor: not-allowed;
}

.form-error {
  color: var(--tx-red);
  font-size: 14px;
  background: rgba(193, 39, 45, 0.08);
  padding: 10px 12px;
  border-radius: 6px;
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

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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
  .table-actions {
    justify-content: flex-end;
  }
}
</style>
