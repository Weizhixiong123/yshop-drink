import { reactive } from 'vue'

export const memberStore = reactive({
  list: [
    { id: 1, name: '王思嘉', gender: '女', phone: '13812345678', liquor: '2 瓶', points: 450, balance: '¥2500', remark: 'SA证、VIP桌常客' },
    { id: 2, name: '李东', gender: '男', phone: '13987654321', liquor: '0', points: 120, balance: '¥500', remark: '偏好安静卡座' },
    { id: 3, name: '陈子豪', gender: '男', phone: '13700001111', liquor: '5 瓶', points: 3000, balance: '¥10000', remark: '大客户、自带洋酒' }
  ]
})
