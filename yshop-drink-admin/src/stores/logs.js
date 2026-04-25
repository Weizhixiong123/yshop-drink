import { reactive } from 'vue'
import { fetchOperationLogList } from '../api/log.js'

const operationTypeMap = {
  add_points: '加积分',
  sub_points: '减积分',
  add_wine: '加酒',
  sub_wine: '减酒',
  add_balance: '加储值',
  sub_balance: '减储值'
}

function resolveApiMessage(error, fallback) {
  return error?.response?.data?.msg || error?.message || fallback
}

function formatNumber(value) {
  if (value === null || value === undefined || value === '') {
    return '--'
  }

  return String(value)
}

function normalizeLogItem(item) {
  const operationValue = formatNumber(item.operationValue)
  const beforeValue = formatNumber(item.beforeValue)
  const afterValue = formatNumber(item.afterValue)

  return {
    id: item.id,
    staff: item.staffName || item.staffId || '--',
    staffId: item.staffId || '',
    time: item.createTime || '--',
    memberId: item.memberId || '',
    memberName: item.memberName || '--',
    type: operationTypeMap[item.operationType] || item.operationType || '--',
    operationType: item.operationType || '',
    value: operationValue === '--' ? '--' : `${isIncreaseType(item.operationType) ? '+' : '-'}${operationValue}`,
    diff: `${beforeValue} -> ${afterValue}`
  }
}

function isIncreaseType(operationType) {
  return ['add_points', 'add_wine', 'add_balance'].includes(operationType)
}

function resolvePageData(data) {
  if (Array.isArray(data)) {
    return {
      list: data,
      total: data.length,
      pageNum: 1,
      pageSize: data.length || 20
    }
  }

  const list = data?.list || data?.records || data?.rows || []

  return {
    list: Array.isArray(list) ? list : [],
    total: Number(data?.total ?? data?.count ?? list.length ?? 0),
    pageNum: Number(data?.pageNum ?? data?.current ?? 1),
    pageSize: Number(data?.pageSize ?? data?.size ?? 20)
  }
}

export const logStore = reactive({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 20,

  async loadLogs(params = {}) {
    try {
      const query = {
        memberId: params.memberId || undefined,
        staffId: params.staffId || undefined,
        operationType: params.operationType || undefined,
        startTime: params.startTime || undefined,
        endTime: params.endTime || undefined,
        pageNum: params.pageNum || this.pageNum,
        pageSize: params.pageSize || this.pageSize
      }

      const response = await fetchOperationLogList(query)
      const result = response.data

      if (result?.code !== undefined && Number(result.code) !== 200) {
        throw new Error(result?.msg || '操作日志加载失败')
      }

      const pageData = resolvePageData(result?.data ?? result)
      this.list = pageData.list.map(normalizeLogItem)
      this.total = pageData.total
      this.pageNum = pageData.pageNum || query.pageNum
      this.pageSize = pageData.pageSize || query.pageSize

      return this.list
    } catch (error) {
      throw new Error(resolveApiMessage(error, '操作日志加载失败'))
    }
  }
})
