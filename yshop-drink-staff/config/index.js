export const VUE_APP_API_URL = 'http://127.0.0.1:8080';
//export const VUE_APP_API_URL = "https://apidc.yixiang.co/app-api";
export const APP_ID = "wxdbdbc123c8c30b45";

// 本地测试店员微信登录时开启：微信 getPhoneNumber 失败也会请求后端，后端用 mock-phone 登录。
// 上线前务必改为 false，并清空后端 wechat.miniapp.mock-phone。
export const STAFF_WX_LOGIN_MOCK = true;
