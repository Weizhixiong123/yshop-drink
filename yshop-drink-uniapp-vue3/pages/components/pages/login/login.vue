<template>
	<layout>
		<uv-navbar
		  :fixed="false"
		  :title="title"
		  left-arrow
		  @leftClick="$onClickLeft"
		/>
		<view class="wrap">

			<!-- Bottom popup overlay -->
			<view class="popup-mask" v-if="showPopup" @tap="closePopup"></view>
			<view class="popup-card" :class="{ 'popup-show': showPopup }">
				<view class="popup-close" @tap="closePopup">×</view>
				<image class="popup-logo" src="/static/images/login_bg.jpg" mode="aspectFill"></image>
				<view class="popup-title">欢迎加入 醉<text class="spade">♠</text>岛 Bar</view>

				<!-- 开发测试阶段统一使用 tap 触发 -->
				<button class="phone-login-btn" @tap="loginForWechatMini">
					手机号快捷登录 (开发测试)
				</button>

				<view class="popup-hint" @tap="onChange">
					<view class="custom-radio" :class="{ 'is-checked': isChecked }">
						<view class="radio-inner" v-if="isChecked"></view>
					</view>
					<view class="hint-content">
						<text class="hint-text">允许我们在必要场景下，合理使用您的个人信息，并且充分保障您的合法权利 我已阅读并同意</text>
						<text class="link" @tap.stop="serv(29,'用户协议')">《用户协议》</text>
					</view>
				</view>
			</view>

		</view>
		<uv-toast ref="uToast"></uv-toast>
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
  customerLogin
} from '@/api/auth'
import { refreshCustomerInfo } from '@/utils/customerSocket'
import * as util  from '@/utils/util'
const main = useMainStore()
const title = ref('登录')
const isChecked = ref(false)
const uToast = ref()
const showPopup = ref(true)

const loginForWechatMini = async () => {
	if(!isChecked.value){
		uToast.value.show({
			message: '请先勾选同意用户协议',
			type: 'error'
		});
		return
	}
	// 开发阶段：使用默认手机号登录
	const code = "17836175977"; // 替换为默认测试手机号
	console.log("准备调用 customerLogin, 手机号:", code);
	try {
		let data = await customerLogin({ phone: code });
		if (data) {
			const token = data.token || data.accessToken || ''
			const userInfo = data.userInfo || { phone: data.phone || code }
			uni.setStorage({
				key: 'userinfo',
				data: userInfo
			});
			uni.setStorage({
				key: 'accessToken',
				data: token
			});
			main.SET_MEMBER(userInfo);
			main.SET_TOKEN(token);
			refreshCustomerInfo();
			uToast.value.show({
				message: '登录成功',
				type: 'success'
			});
			setTimeout(function() {
				uni.navigateBack();
			}, 1500);
		}
	} catch (err) {
		console.error('登录失败', err);
	}
}

// H5/非微信环境备用登录
const showManualLogin = () => {
	uToast.value.show({
		message: '请在微信小程序中使用快捷登录',
		type: 'warning'
	});
}

const openPopup = () => {
	showPopup.value = true;
}

const closePopup = () => {
	showPopup.value = false;
	setTimeout(() => {
		uni.navigateBack();
	}, 300);
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
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	background: #000 url('/static/images/login_bg_new.png') no-repeat center center;
	background-size: cover;
	overflow: hidden;

	/* Popup mask */
	.popup-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.72);
		z-index: 10;
	}

	/* Bottom popup card */
	.popup-card {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		background: #ffffff;
		border-top-left-radius: 34rpx;
		border-top-right-radius: 34rpx;
		padding: 58rpx 44rpx calc(58rpx + env(safe-area-inset-bottom));
		transform: translateY(100%);
		transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
		box-shadow: 0 -18rpx 48rpx rgba(0, 0, 0, 0.16);
		border-top: 1px solid rgba(0, 0, 0, 0.06);

		&::before {
			content: '';
			position: absolute;
			top: 18rpx;
			left: 50%;
			width: 76rpx;
			height: 8rpx;
			border-radius: 999rpx;
			background: rgba(0, 0, 0, 0.12);
			transform: translateX(-50%);
		}

		&.popup-show {
			transform: translateY(0);
		}

		.popup-close {
			position: absolute;
			top: 34rpx;
			right: 34rpx;
			width: 58rpx;
			height: 58rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			background: #f5f5f5;
			border-radius: 50%;
			font-size: 42rpx;
			color: #111111;
			line-height: 1;
			padding-bottom: 4rpx;
		}

		.popup-logo {
			width: 112rpx;
			height: 112rpx;
			border-radius: 50%;
			margin-bottom: 26rpx;
			display: block;
			background: #f5f5f5;
			border: 2rpx solid rgba(0, 0, 0, 0.08);
			box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.12);
		}

		.popup-title {
			font-size: 36rpx;
			font-weight: 700;
			color: #111111;
			margin-bottom: 52rpx;
			letter-spacing: 0;

			.spade {
				color: #111111;
				-webkit-text-stroke: 1.5px #ffffff;
				font-weight: 900;
				margin: 0 2rpx;
				/* 增加文字阴影，使白色描边能在白底上凸显 */
				text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.4);
			}
		}

		.phone-login-btn {
			width: 100%;
			height: 96rpx;
			line-height: 96rpx;
			background: #111111 !important;
			color: #ffffff !important;
			font-size: 32rpx;
			font-weight: 700;
			border-radius: 48rpx;
			margin-bottom: 32rpx;
			letter-spacing: 0;
			box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.16);

			&::after { border: none !important; }
			&:active { transform: scale(0.98); opacity: 0.9; }
		}

		.popup-hint {
			display: flex;
			align-items: flex-start;
			padding: 22rpx 24rpx;
			background: #f7f7f7;
			border: 1px solid rgba(0, 0, 0, 0.06);
			border-radius: 20rpx;
			font-size: 22rpx;
			line-height: 36rpx;

			.custom-radio {
				width: 30rpx;
				height: 30rpx;
				border-radius: 50%;
				border: 2rpx solid #d8d8d8;
				margin-right: 16rpx;
				margin-top: 4rpx;
				flex-shrink: 0;
				display: flex;
				align-items: center;
				justify-content: center;
				box-sizing: border-box;

				&.is-checked {
					border-color: #111111;
					background-color: #111111;
				}

				.radio-inner {
					width: 12rpx;
					height: 12rpx;
					border-radius: 50%;
					background-color: #ffffff;
				}
			}

			.hint-content {
				flex: 1;
			}

			.hint-text {
				color: #8a8a8a;
			}

			.link {
				color: #111111;
				font-weight: 600;
			}
		}
	}
}
</style>
