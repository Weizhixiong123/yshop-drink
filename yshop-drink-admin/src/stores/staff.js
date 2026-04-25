import { reactive } from 'vue'
import { createStaff, deleteStaff, fetchStaffList, updateStaff } from '../api/staff.js'

function resolveApiMessage(error, fallback) {
  return error?.response?.data?.msg || error?.message || fallback
}

function normalizeStaffItem(item) {
  return {
    id: item.id,
    name: item.name || '',
    phone: item.phone || '',
    role: item.role === 'owner' ? '店东' : '店员',
    status: item.status === 0 ? '禁用' : '正常',
    rawStatus: item.status
  }
}

export const staffStore = reactive({
  list: [],

  async loadStaff() {
    try {
      const response = await fetchStaffList()
      const result = response.data

      if (result?.code !== 200) {
        throw new Error(result?.msg || '店员列表加载失败')
      }

      this.list = Array.isArray(result?.data) ? result.data.map(normalizeStaffItem) : []
      return this.list
    } catch (error) {
      throw new Error(resolveApiMessage(error, '店员列表加载失败'))
    }
  },

  async addStaff(staff) {
    try {
      const response = await createStaff(staff)
      const result = response.data

      if (result?.code !== 200) {
        throw new Error(result?.msg || '录入店员失败')
      }

      await this.loadStaff()
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '录入店员失败'))
    }
  },

  async updateStaff(staff) {
    try {
      const response = await updateStaff(staff)
      const result = response.data

      if (result?.code !== 200) {
        throw new Error(result?.msg || '修改员工信息失败')
      }

      await this.loadStaff()
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '修改员工信息失败'))
    }
  },

  async deleteStaff(id) {
    try {
      const response = await deleteStaff(id)
      const result = response.data

      if (result?.code !== 200) {
        throw new Error(result?.msg || '删除员工失败')
      }

      await this.loadStaff()
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '删除员工失败'))
    }
  }
})
