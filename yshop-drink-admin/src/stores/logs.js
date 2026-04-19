import { reactive } from 'vue'

export const logStore = reactive({
  list: [
    { id: 1, staff: '店员A', time: '2023-10-10 14:30', target: '王思嘉 (13812345678)', type: '加积分', value: '+50', diff: '400 -> 450' },
    { id: 2, staff: '店员B', time: '2023-10-10 16:20', target: '李东 (13987654321)', type: '减酒', value: '-1', diff: '1 -> 0' },
    { id: 3, staff: '店长', time: '2023-10-10 19:00', target: '陈子豪 (13700001111)', type: '加储值', value: '+¥5000', diff: '¥5000 -> ¥10000' }
  ]
})
