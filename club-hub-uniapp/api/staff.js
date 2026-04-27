import api from './api'

/**
 * StaffController - 员工操作台 API
 * 对接 Apifox 文档: https://s.apifox.cn/953980df-9b3f-41a9-8fa5-6d11c8c9cd69
 */

/**
 * 新客开卡 (POST /api/staff/member/register)
 * @param {Object} data - MemberRegisterRequest
 * @param {string} data.name - 姓名 (必填, maxLength: 50)
 * @param {string} data.gender - 性别 M/F (pattern: ^[MF]$)
 * @param {string} data.phone - 手机号 (必填, pattern: ^1[3-9]\d{9}$)
 * @param {number} [data.wine] - 初始存酒数
 * @param {number} [data.points] - 初始积分
 * @param {number} [data.balance] - 初始储值余额
 * @param {string} [data.remark] - 备注 (maxLength: 500)
 * @returns {Promise} Result { code, msg, data }
 */
export function memberRegister(data) {
  return api.post('/api/staff/member/register', data)
}

/**
 * 按会员ID、姓名、手机号尾号搜索 (GET /api/staff/member/search)
 * @param {string} keyword - 搜索关键词
 * @returns {Promise} Result { code, msg, data: [...] }
 */
export function memberSearch(keyword) {
  return api.get('/api/staff/member/search', { keyword })
}

/**
 * 会员详情 (GET /api/staff/member/detail/{id})
 * @param {number|string} id - 会员ID
 * @returns {Promise} Result { code, msg, data: {...} }
 */
export function memberDetail(id) {
  return api.get(`/api/staff/member/detail/${id}`)
}

/**
 * 加减操作 (POST /api/staff/member/operate)
 * @param {Object} data - MemberOperateRequest
 * @param {number} data.memberId - 会员ID (必填, int64)
 * @param {string} data.type - 操作类型 (必填, enum: ADD_POINTS|SUB_POINTS|ADD_WINE|SUB_WINE|ADD_BALANCE|SUB_BALANCE)
 * @param {number} data.value - 变更数量 (必填)
 * @returns {Promise} Result { code, msg, data }
 */
export function memberOperate(data) {
  return api.post('/api/staff/member/operate', data)
}

/**
 * 修改备注 (PUT /api/staff/member/remark)
 * @param {Object} data - MemberRemarkUpdateRequest
 * @param {number} data.id - 会员ID (必填, int64)
 * @param {string} [data.remark] - 备注 (maxLength: 500)
 * @returns {Promise} Result { code, msg, data }
 */
export function memberUpdateRemark(data) {
  return api.put('/api/staff/member/remark', data)
}

/**
 * 修改会员基本信息 (PUT /api/staff/member/info)
 * @param {Object} data - MemberInfoUpdateRequest
 * @param {number} data.id - 会员ID (必填, int64)
 * @param {string} [data.name] - 姓名
 * @param {string} [data.gender] - 性别
 * @param {string} [data.phone] - 手机号
 * @returns {Promise} Result { code, msg, data }
 */
export function memberUpdateInfo(data) {
  return api.put('/api/staff/member/info', data)
}
