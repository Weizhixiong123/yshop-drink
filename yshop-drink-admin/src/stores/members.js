import { reactive } from 'vue'
import {
  fetchMemberList,
  operateMember,
  updateMemberInfo,
  updateMemberRemark
} from '../api/member.js'

function resolveApiMessage(error, fallback) {
  return error?.response?.data?.msg || error?.message || fallback
}

function normalizeGender(gender) {
  const value = String(gender || '').toLowerCase()

  if (gender === 1 || value === '1' || value === 'm' || value === 'male') {
    return '男'
  }

  if (gender === 2 || value === '2' || value === 'f' || value === 'female') {
    return '女'
  }

  return gender || '--'
}

function normalizeMemberItem(item) {
  return {
    id: item.id,
    name: item.name || '--',
    gender: normalizeGender(item.gender),
    rawGender: item.gender || '',
    phone: item.phone || '--',
    liquor: item.liquor ?? item.wine ?? item.storedWine ?? '--',
    points: item.points ?? item.point ?? item.integral ?? '--',
    balance: item.balance ?? item.money ?? item.amount ?? '--',
    remark: item.remark || item.note || item.memo || '--'
  }
}

function resolvePageData(data) {
  if (Array.isArray(data)) {
    return {
      list: data,
      total: data.length,
      pageNum: 1,
      pageSize: data.length || 10
    }
  }

  const list = data?.records || data?.list || data?.rows || []

  return {
    list: Array.isArray(list) ? list : [],
    total: Number(data?.total ?? data?.count ?? list.length ?? 0),
    pageNum: Number(data?.current ?? data?.pageNum ?? 1),
    pageSize: Number(data?.size ?? data?.pageSize ?? 10)
  }
}

export const memberStore = reactive({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,

  lastQuery: {},

  async loadMembers(params = {}) {
    try {
      const query = {
        pageNum: params.pageNum || this.pageNum,
        pageSize: params.pageSize || this.pageSize,
        id: params.id || undefined,
        name: params.name || undefined,
        gender: params.gender || undefined,
        phone: params.phone || undefined
      }
      this.lastQuery = query

      const response = await fetchMemberList(query)
      const result = response.data

      if (result?.code !== undefined && Number(result.code) !== 200) {
        throw new Error(result?.msg || '会员档案加载失败')
      }

      const pageData = resolvePageData(result?.data ?? result)
      this.list = pageData.list.map(normalizeMemberItem)
      this.total = pageData.total
      this.pageNum = pageData.pageNum || query.pageNum
      this.pageSize = pageData.pageSize || query.pageSize

      return this.list
    } catch (error) {
      throw new Error(resolveApiMessage(error, '会员档案加载失败'))
    }
  },

  async updateInfo(payload) {
    try {
      const response = await updateMemberInfo(payload)
      const result = response.data

      if (result?.code !== undefined && Number(result.code) !== 200) {
        throw new Error(result?.msg || '修改会员信息失败')
      }

      await this.loadMembers(this.lastQuery)
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '修改会员信息失败'))
    }
  },

  async updateRemark(payload) {
    try {
      const response = await updateMemberRemark(payload)
      const result = response.data

      if (result?.code !== undefined && Number(result.code) !== 200) {
        throw new Error(result?.msg || '修改会员备注失败')
      }

      await this.loadMembers(this.lastQuery)
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '修改会员备注失败'))
    }
  },

  async operate(payload) {
    try {
      const response = await operateMember(payload)
      const result = response.data

      if (result?.code !== undefined && Number(result.code) !== 200) {
        throw new Error(result?.msg || '会员资产操作失败')
      }

      await this.loadMembers(this.lastQuery)
      return result?.data
    } catch (error) {
      throw new Error(resolveApiMessage(error, '会员资产操作失败'))
    }
  }
})
