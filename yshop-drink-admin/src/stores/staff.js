import { reactive } from 'vue'

export const staffStore = reactive({
  list: [
    { id: 1, name: '王经理', phone: '13800000000', role: '店长', status: '正常' },
    { id: 2, name: '李四', phone: '13900000001', role: '店员', status: '正常' }
  ],
  addStaff(staff) {
    this.list.push({
      id: this.list.length + 1,
      ...staff,
      status: '正常'
    })
  }
})
