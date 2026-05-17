<script setup>
import { computed, onMounted, ref } from 'vue'
import { memberStore } from '../stores/members.js'

const loading = ref(false)
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const formError = ref('')
const modalType = ref('')
const selectedMember = ref(null)
const filters = ref({
  id: '',
  name: '',
  gender: '',
  phone: ''
})
const infoForm = ref({ name: '', gender: '', phone: '' })
const remarkForm = ref({ remark: '' })
const operateForm = ref({ type: 'ADD_POINTS', value: '' })
const levelForm = ref({ level: 'normal' })

const phonePattern = /^1[3-9]\d{9}$/
const operationTypes = [
  { value: 'ADD_POINTS', label: '加积分' },
  { value: 'SUB_POINTS', label: '减积分' },
  { value: 'ADD_WINE', label: '加酒' },
  { value: 'SUB_WINE', label: '减酒' },
  { value: 'SUB_BALANCE', label: '减储值' }
]

const levelTypes = [
  { value: 'normal', label: '普通会员' },
  { value: 'gold', label: '黄金会员' },
  { value: 'platinum', label: '白金会员' },
  { value: 'black_gold', label: '黑金会员' },
  { value: 'black_diamond', label: '黑钻会员' }
]

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(memberStore.total / memberStore.pageSize))
})

const formatMemberId = (id) => {
  if (!id) {
    return '---'
  }

  return `#${String(id).padStart(3, '0')}`
}

const resolveQuery = (pageNum = 1) => ({
  pageNum,
  pageSize: memberStore.pageSize,
  id: filters.value.id.trim(),
  name: filters.value.name.trim(),
  gender: filters.value.gender,
  phone: filters.value.phone.trim()
})

const closeModal = () => {
  modalType.value = ''
  selectedMember.value = null
  formError.value = ''
  submitting.value = false
}

const openInfoModal = (member) => {
  selectedMember.value = member
  infoForm.value = {
    name: member.name === '--' ? '' : member.name,
    gender: member.rawGender || (member.gender === '男' ? 'M' : member.gender === '女' ? 'F' : ''),
    phone: member.phone === '--' ? '' : member.phone
  }
  formError.value = ''
  modalType.value = 'info'
}

const openRemarkModal = (member) => {
  selectedMember.value = member
  remarkForm.value = {
    remark: member.remark === '--' ? '' : member.remark
  }
  formError.value = ''
  modalType.value = 'remark'
}

const openOperateModal = (member) => {
  selectedMember.value = member
  operateForm.value = { type: 'ADD_POINTS', value: '' }
  formError.value = ''
  modalType.value = 'operate'
}

const openLevelModal = (member) => {
  selectedMember.value = member
  levelForm.value = { level: member.rawLevel || 'normal' }
  formError.value = ''
  modalType.value = 'level'
}

const loadMembers = async (pageNum = 1) => {
  loading.value = true
  errorMsg.value = ''

  try {
    await memberStore.loadMembers(resolveQuery(pageNum))
  } catch (error) {
    errorMsg.value = error?.message || '会员档案加载失败'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadMembers(1)
}

const handleReset = () => {
  filters.value = {
    id: '',
    name: '',
    gender: '',
    phone: ''
  }
  loadMembers(1)
}

const changePage = (pageNum) => {
  if (pageNum < 1 || pageNum > totalPages.value || pageNum === memberStore.pageNum) {
    return
  }

  loadMembers(pageNum)
}

const submitInfo = async () => {
  const payload = {
    id: selectedMember.value?.id,
    name: infoForm.value.name.trim(),
    gender: infoForm.value.gender,
    phone: infoForm.value.phone.trim()
  }

  if (!payload.name) {
    formError.value = '姓名不能为空'
    return
  }

  if (payload.phone && !phonePattern.test(payload.phone)) {
    formError.value = '手机号格式不正确'
    return
  }

  submitting.value = true
  formError.value = ''
  successMsg.value = ''
  try {
    await memberStore.updateInfo(payload)
    successMsg.value = '会员信息修改成功'
    closeModal()
  } catch (error) {
    formError.value = error?.message || '修改会员信息失败'
  } finally {
    submitting.value = false
  }
}

const submitRemark = async () => {
  const payload = {
    id: selectedMember.value?.id,
    remark: remarkForm.value.remark.trim()
  }

  if (payload.remark.length > 500) {
    formError.value = '备注不能超过 500 字'
    return
  }

  submitting.value = true
  formError.value = ''
  successMsg.value = ''
  try {
    await memberStore.updateRemark(payload)
    successMsg.value = '会员备注修改成功'
    closeModal()
  } catch (error) {
    formError.value = error?.message || '修改会员备注失败'
  } finally {
    submitting.value = false
  }
}

const submitOperate = async () => {
  const value = Number(operateForm.value.value)
  if (!Number.isFinite(value) || value <= 0) {
    formError.value = '操作值必须大于 0'
    return
  }

  submitting.value = true
  formError.value = ''
  successMsg.value = ''
  try {
    await memberStore.operate({
      memberId: selectedMember.value?.id,
      type: operateForm.value.type,
      value
    })
    successMsg.value = '会员资产操作成功'
    closeModal()
  } catch (error) {
    formError.value = error?.message || '会员资产操作失败'
  } finally {
    submitting.value = false
  }
}

const submitLevel = async () => {
  submitting.value = true
  formError.value = ''
  successMsg.value = ''
  try {
    await memberStore.updateLevel({
      memberId: selectedMember.value?.id,
      level: levelForm.value.level
    })
    successMsg.value = '会员等级调整成功'
    closeModal()
  } catch (error) {
    formError.value = error?.message || '调整会员等级失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMembers()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">全量会员档案</h2>
      <p class="page-desc">包含完全开放的客户联系方式与资产</p>
    </div>

    <!-- 白色实心卡片容器 -->
    <div class="card-panel table-panel">
      <form class="filter-bar" @submit.prevent="handleSearch">
        <input v-model="filters.id" type="text" placeholder="会员编号" />
        <input v-model="filters.name" type="text" placeholder="姓名" />
        <select v-model="filters.gender">
          <option value="">全部性别</option>
          <option value="M">男</option>
          <option value="F">女</option>
        </select>
        <input v-model="filters.phone" type="tel" maxlength="11" placeholder="手机号" />
        <button class="btn-solid filter-btn" type="submit" :disabled="loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
        <button class="btn-outline filter-btn" type="button" @click="handleReset">重置</button>
      </form>

      <div v-if="successMsg" class="page-success">{{ successMsg }}</div>
      <div v-if="errorMsg" class="page-error">{{ errorMsg }}</div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>会员编号</th>
              <th>姓名</th>
              <th>性别</th>
              <th class="highlight-col">手机号</th>
              <th>存酒</th>
              <th>积分</th>
              <th>余额</th>
              <th>本金/赠送</th>
              <th>等级</th>
              <th>后台备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="11" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="!memberStore.list.length">
              <td colspan="11" class="empty-row">暂无会员数据</td>
            </tr>
            <template v-else>
              <tr v-for="m in memberStore.list" :key="m.id">
                <td data-label="会员编号" class="text-muted">{{ formatMemberId(m.id) }}</td>
                <td data-label="姓名" class="font-bold">{{ m.name }}</td>
                <td data-label="性别">{{ m.gender }}</td>
                <td data-label="手机号" class="highlight-col">{{ m.phone }}</td>
                <td data-label="存酒"><span class="value-badge">{{ m.liquor }}</span></td>
                <td data-label="积分"><span class="value-badge">{{ m.points }}</span></td>
                <td data-label="余额"><span class="value-badge">{{ m.balance }}</span></td>
                <td data-label="本金/赠送"><span class="value-badge">{{ m.principalBalance }} / {{ m.bonusBalance }}</span></td>
                <td data-label="等级"><span class="value-badge">{{ m.level }}</span></td>
                <td data-label="后台备注" class="remark-cell">{{ m.remark }}</td>
                <td data-label="操作">
                  <div class="table-actions">
                    <button type="button" class="table-action" @click="openInfoModal(m)">资料</button>
                    <button type="button" class="table-action" @click="openLevelModal(m)">等级</button>
                    <button type="button" class="table-action" @click="openRemarkModal(m)">备注</button>
                    <button type="button" class="table-action danger" @click="openOperateModal(m)">资产</button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div class="pager">
        <span>共 {{ memberStore.total }} 条</span>
        <div class="pager-actions">
          <button type="button" class="pager-btn" :disabled="memberStore.pageNum <= 1 || loading" @click="changePage(memberStore.pageNum - 1)">
            上一页
          </button>
          <span>{{ memberStore.pageNum }} / {{ totalPages }}</span>
          <button type="button" class="pager-btn" :disabled="memberStore.pageNum >= totalPages || loading" @click="changePage(memberStore.pageNum + 1)">
            下一页
          </button>
        </div>
      </div>
    </div>

    <div v-if="modalType" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content card-panel">
        <h3 class="modal-title">
          {{ modalType === 'info' ? '修改会员资料' : modalType === 'remark' ? '修改后台备注' : modalType === 'level' ? '调整会员等级' : '会员资产调整' }}
        </h3>

        <form v-if="modalType === 'info'" class="modal-form" @submit.prevent="submitInfo">
          <div class="form-item">
            <label>姓名</label>
            <input v-model="infoForm.name" type="text" maxlength="50" placeholder="会员姓名" />
          </div>
          <div class="form-item">
            <label>性别</label>
            <select v-model="infoForm.gender">
              <option value="">不修改</option>
              <option value="M">男</option>
              <option value="F">女</option>
            </select>
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="infoForm.phone" type="tel" maxlength="11" placeholder="11位手机号" />
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-solid submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </form>

        <form v-else-if="modalType === 'remark'" class="modal-form" @submit.prevent="submitRemark">
          <div class="form-item">
            <label>后台备注</label>
            <textarea v-model="remarkForm.remark" maxlength="500" placeholder="输入备注"></textarea>
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-solid submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '保存备注' }}
            </button>
          </div>
        </form>

        <form v-else-if="modalType === 'level'" class="modal-form" @submit.prevent="submitLevel">
          <div class="form-item">
            <label>会员</label>
            <input type="text" :value="selectedMember?.name" disabled />
          </div>
          <div class="form-item">
            <label>会员等级</label>
            <select v-model="levelForm.level">
              <option v-for="item in levelTypes" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-solid submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认调整' }}
            </button>
          </div>
        </form>

        <form v-else class="modal-form" @submit.prevent="submitOperate">
          <div class="form-item">
            <label>会员</label>
            <input type="text" :value="selectedMember?.name" disabled />
          </div>
          <div class="form-item">
            <label>操作类型</label>
            <select v-model="operateForm.type">
              <option v-for="item in operationTypes" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>操作值</label>
            <input v-model="operateForm.value" type="number" min="0" step="0.01" placeholder="输入大于 0 的数值" />
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="form-actions">
            <button type="button" class="btn-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-solid submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认操作' }}
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
  grid-template-columns: repeat(4, minmax(120px, 1fr)) auto auto;
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

.page-success {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(90, 199, 37, 0.12);
  color: #2f8f12;
  font-size: 14px;
  font-weight: bold;
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
  min-width: 980px; /* 强制移动端发生横向溢出 */
}

.data-table th:nth-child(5),
.data-table th:nth-child(6),
.data-table th:nth-child(7),
.data-table td:nth-child(5),
.data-table td:nth-child(6),
.data-table td:nth-child(7) {
  text-align: center;
}

.data-table th:nth-child(8),
.data-table td:nth-child(8) {
  min-width: 150px;
}

.data-table th:nth-child(9),
.data-table td:nth-child(9) {
  width: 180px;
}

.data-table th {
  padding: 14px 12px;
  color: #111;
  font-weight: bold;
  border-bottom: 2px solid #eee;
  font-size: 14px;
}

.data-table td {
  padding: 14px 12px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
  font-size: 14px;
  white-space: nowrap;
}

.data-table tr:hover td {
  background: #fcfbf8;
}

.highlight-col {
  color: var(--tx-red) !important;
  font-weight: bold;
}
.font-bold { font-weight: bold; }
.text-muted { color: var(--text-muted); }

.empty-row {
  text-align: center;
  color: var(--text-muted);
  padding: 32px 12px !important;
}

.value-badge {
  display: inline-block;
  min-width: 38px;
  background: #f8f8f8;
  padding: 5px 10px;
  border-radius: 7px;
  font-weight: 600;
  border: 1px solid #ebebeb;
  text-align: center;
  line-height: 1;
}

.remark-cell {
  color: #888;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table-actions {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 2px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
  width: max-content;
}

.table-action {
  height: 30px;
  min-width: 44px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.18s ease;
}

.table-action:hover {
  border-color: #ddd;
  background: #fff;
  transform: translateY(-1px);
}

.table-action.danger {
  background: #fff7f7;
  border-color: rgba(193, 39, 45, 0.2);
  color: var(--tx-red);
}

.table-action.danger:hover {
  border-color: rgba(193, 39, 45, 0.45);
  background: #fff;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 90%;
  max-width: 420px;
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
.form-item select,
.form-item textarea {
  width: 100%;
  padding: 12px;
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 15px;
}

.form-item textarea {
  min-height: 110px;
  resize: vertical;
}

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
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

.form-actions .btn-outline {
  flex: 1;
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
    gap: 14px;
  }
  .data-table td:last-child {
    border-bottom: none;
  }
  /* 注入通过 data-label 设置的属性标题 */
  .data-table td::before {
    content: attr(data-label);
    flex: 0 0 72px;
    font-weight: bold;
    color: #888;
    text-align: left;
  }
  
  .remark-cell {
    max-width: none;
    text-align: right;
    overflow: visible;
    text-overflow: clip;
  }
  .table-actions {
    display: grid;
    grid-template-columns: repeat(3, 54px);
    justify-content: end;
    gap: 6px;
    width: max-content;
    max-width: none;
    margin-left: auto;
    padding: 0;
    border: none;
    background: transparent;
  }
  .table-action {
    min-width: 54px;
    padding: 0 8px;
  }
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
