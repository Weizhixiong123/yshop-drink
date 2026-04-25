import { handleLoginFailure } from '@/utils'
import { isWeixin } from '@/utils/util'
import { VUE_APP_API_URL } from '@/config'
import cookie from '@/utils/cookie'

const defaultOpt = { login: true }

/**
 * 基于 uni.request 的封装，替代 flyio
 * 保持原有 token 注入、401 处理、错误 toast 等逻辑
 */
function uniRequest(url, data, options) {
  return new Promise((resolve, reject) => {
    const token = cookie.get('accessToken')
    console.log('--> % token % token:\n', token)

    const method = (options.method || 'GET').toUpperCase()
    const fullUrl = /^https?:\/\//.test(url) ? url : VUE_APP_API_URL + url

    uni.request({
      url: fullUrl,
      data: data,
      method: method,
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token,
        ...(options.headers || {}),
      },
      success(res) {
        const resData = res.data || {}

        // #ifdef H5
        if (resData.code == 1004004002) {
          if (isWeixin()) {
            const indexUrl = cookie.get('index_url')
            console.log('redirect_uri:', indexUrl)
            location.href = indexUrl
            return
          }
        }
        // #endif

        if (res.statusCode === 401) {
          handleLoginFailure()
          reject({ msg: '未登录', toLogin: true })
          return
        }

        if (res.statusCode !== 200) {
          reject({ msg: '请求失败', res, data: resData })
          return
        }

        if (resData.code == 401) {
          uni.hideLoading()
          handleLoginFailure()
          uni.showToast({ title: resData.msg, icon: 'none', duration: 2000 })
          reject({ msg: resData.msg, res, data: resData })
          return
        }

        if (resData.code != 0 && resData.code != 200) {
          uni.showToast({ title: resData.msg, icon: 'none', duration: 2000 })
          reject({ data: resData, res })
          return
        }

        resolve(resData.data)
      },
      fail(err) {
        if (err.errMsg && err.errMsg.includes('Network')) {
          handleLoginFailure()
          reject({ msg: '未登录', toLogin: true })
          return
        }
        reject(err)
      },
    })
  })
}

function baseRequest(options) {
  const { url, params, data } = options
  const payload = params || data
  return uniRequest(url, payload, options)
}

/**
 * http 请求基础类
 */
const request = ['post', 'put', 'patch'].reduce((request, method) => {
  request[method] = (url, data = {}, options = {}) => {
    console.log(url, data)
    return baseRequest(Object.assign({ url, data, method }, defaultOpt, options))
  }
  return request
}, {})

;['get', 'delete', 'head'].forEach(method => {
  request[method] = (url, params = {}, options = {}) => {
    return baseRequest(Object.assign({ url, params, method }, defaultOpt, options))
  }
})

export default request

