<template>
	<layout>
		<uv-navbar
		  :fixed="false"
		  :title="title"
		  left-arrow
		  @leftClick="$onClickLeft"
		/>
		<view class="wrap">
		<view class="top"></view>
		<view class="content">
			<view class="title">欢迎登录</view>
			<view class="subtitle">TEXAS HOLD'EM & PUB</view>
			
			<view class="login-action">
				<!-- #ifdef MP-WEIXIN -->
				<button class="wechat-login-btn" open-type="getPhoneNumber" @getphonenumber="loginForWechatMini">
					微信一键快捷登录
				</button>
				<!-- #endif -->
				<!-- #ifndef MP-WEIXIN -->
				<view class="tips">请在微信小程序中打开以使用一键登录</view>
				<!-- #endif -->
			</view>
		</view>
		
		<view class="buttom">
			<view class="hint">
				<radio value="1" :checked="isChecked" @tap.stop="onChange" color="#d4a45b" style="transform: scale(0.7);" />
				<text class="hint-text">我已经阅读并遵守</text>
				<text class="link" @tap="serv(29,'用户协议')">《用户协议》</text>
				<text class="hint-text">与</text>
				<text class="link" @tap="serv(30,'隐私政策')">《隐私政策》</text>
			</view>
		</view>
		<uv-toast ref="uToast"></uv-toast>
	</view>
	</layout>
</template>

<script setup>
import {
  ref,
  computed
} from 'vue'
import { onLoad,onShow } from '@dcloudio/uni-app'
import { useMainStore } from '@/store/store'
import {
  userAuthSession,
  userLoginForWechatMini,
  smsSend,
  userLogin
} from '@/api/auth'
import * as util  from '@/utils/util'
import { mobile as testMobible } from '@/uni_modules/uv-ui-tools/libs/function/test'
const main = useMainStore()
const title = ref('登录')
const mobile = ref('')
const captcha = ref('')
const captchaText = ref('获取验证码')	
const password = ref('')
const seconds = ref(60)
const isChecked = ref(false)
const openid = ref(main.openid)
const uToast = ref()
const uCode = ref()

const captchaStyle = computed(() => {
  let style = {};
  if(mobile.value && captchaText.value == '获取验证码') {
  	style.color = "#fff";
  	style.backgroundColor = '#f9ae3d';
  }
  return style;
});

onShow(() => {
   
	// #ifdef MP-WEIXIN
	if(!openid.value){
		wechatMiniLogin();
	}
	
	// #endif
})


const wechatMiniLogin = () => {
	//this.$u.toast('登录中');
	uni.login({
		provider: 'weixin'
	}).then(async (res) => {
		let data = await userAuthSession({
			code: res.code
		});
		if (data) {
			console.log('data.openId001:',data.openId)
			main.SET_OPENID(data.openId)
			openid.value = data.openId
		}
	});
}



const loginForWechatMini = async (e) => {
	if(!isChecked.value){
		uToast.value.show({
			message: '请勾选下面协议',
			type: 'error'
		});
		return
	}
	if (e.detail.encryptedData && e.detail.iv) {
		let data = await userLoginForWechatMini({
			encryptedData: e.detail.encryptedData,
			iv: e.detail.iv,
			openid: openid.value
		});
		if (data) {
			main.SET_MEMBER(data.userInfo);
			main.SET_TOKEN(data.accessToken);
			uToast.value.show({
				title: '登录成功',
				type: 'success'
			});
			setTimeout(function() {
				uni.navigateBack();
			}, 2000);
		}
	}
}

const getCaptcha = async () => {
			
	if (testMobible(mobile.value) == false) {
		uToast.value.show({
			message: '手机号码格式不对',
			type: 'error'
		});
		return
	}
	
	let data = await smsSend({
		mobile: mobile.value,
		scene: 1
	});		
	if (data) {
		uCode.value.start();
		
	}
}

// 验证码开始计时	
const startCaptcha = () => {
	console.log('start')
}
// 验证码结束
const endCaptcha = () => {
	console.log('end')
	captchaText.value = '获取验证码';
}
const changeCapcha = (text)  => {
	console.log('change:' + text)
	captchaText.value = text;
}
// 提交
const submit = () => {
	
	if (testMobible(mobile.value) == false) {
		uToast.value.show({
			message: '手机号码格式不对',
			type: 'error'
		});
		return
	}
	
	if(!isChecked.value){
		uToast.value.show({
			message: '请勾选下面协议',
			type: 'error'
		});
		return
	}
	
	login()

}

// 登录
const login = async () => {
	let from = 'routine'
	// #ifdef H5
	from = 'h5'
	if(util.isWeixin()){
		from = 'wechat'
	}
	
	// #endif
	let data = await userLogin({
		mobile: mobile.value,
		code: captcha.value,
		from: from,
		openid: openid.value
	})
	if (data) {
		uni.setStorage({
			key: 'userinfo',
			data: data.userInfo
		});
		uni.setStorage({
			key: 'accessToken',
			data: data.accessToken
		});
		main.SET_MEMBER(data.userInfo);
		main.SET_TOKEN(data.accessToken);
		uToast.value.show({
			message: '登录成功',
			type: 'success'
		});
		setTimeout(function() {
			uni.navigateBack();
		}, 2000);
	}
}

const serv = (id,name) => {
	uni.navigateTo({
			url: '/pages/components/pages/mine/content?id=' + id + '&name=' + name
	})
}

const onChange = () => {
	isChecked.value = !isChecked.value
}

</script>

<style lang="scss" scoped>
.wrap {
	background-color: #f5f2eb;
	font-size: 28rpx;
	position: relative;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	padding-bottom: env(safe-area-inset-bottom);

	.content {
		width: 600rpx;
		margin: 160rpx auto 0;
		text-align: center;

		.title {
			font-size: 64rpx;
			font-weight: 900;
			color: #120d0c;
			margin-bottom: 12rpx;
			letter-spacing: 2rpx;
		}

		.subtitle {
			font-size: 24rpx;
			color: #d4a45b;
			font-weight: 700;
			letter-spacing: 4rpx;
			margin-bottom: 200rpx;
		}

		.login-action {
			margin-top: 60rpx;

			.wechat-login-btn {
				background: linear-gradient(135deg, #1aad19, #148b14) !important;
				color: #ffffff;
				font-size: 32rpx;
				font-weight: 600;
				border-radius: 999rpx;
				height: 96rpx;
				line-height: 96rpx;
				box-shadow: 0 10rpx 30rpx rgba(26, 173, 25, 0.3);
				transition: transform 0.2s, box-shadow 0.2s;
				
				&::after {
					border: none;
				}
				
				&:active {
					transform: scale(0.96);
					box-shadow: 0 4rpx 12rpx rgba(26, 173, 25, 0.2);
				}
			}

			.tips {
				color: #a2988c;
				font-size: 24rpx;
				margin-top: 20rpx;
			}
		}
	}

	.buttom {
		padding: 40rpx 0;
		
		.hint {
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 22rpx;
			
			.hint-text {
				color: #a2988c;
			}
			
			.link {
				color: #120d0c;
				font-weight: 600;
				margin: 0 4rpx;
			}
		}
	}
}
</style>
