export const VUE_APP_API_URL = 'http://localhost:8080';
export const VUE_APP_STAFF_WS_URL = VUE_APP_API_URL.replace(/^http/, 'ws') + '/ws/staff';
export const VUE_APP_CUSTOMER_WS_URL = VUE_APP_API_URL.replace(/^http/, 'ws') + '/ws/customer';
export const VUE_APP_WS_URL = VUE_APP_STAFF_WS_URL;
//export const VUE_APP_API_URL = "https://apidc.yixiang.co/app-api";
export const APP_ID = "wxda35e363d4245b4a";

export const IS_DEV = process.env.NODE_ENV === 'development';
export const STAFF_WX_LOGIN_MOCK = IS_DEV;
