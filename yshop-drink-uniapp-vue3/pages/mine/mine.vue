<template>
	<layout>
		<view class="mine-container">

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
						<text class="link" @tap.stop="serv({type:'content', id:29, name:'用户协议'})">《用户协议》</text>
					</view>
				</view>
			</view>
			<uv-toast ref="uToast"></uv-toast>

			<!-- Top Background -->
			<view class="bg-box">
				<!-- We use a background image matching the theme, you can replace the src with your actual background asset -->
				<image class="bg-img" src="/static/images/mine_header_bg_unsplash.jpg" mode="aspectFill"></image>
				
				<view :style="{ height: statusBarHeight + 'px' }"></view>
				<view class="nav-bar"></view>
				
				<!-- User Info Row -->
				<view class="user-info-row d-flex align-items-center">
					<view class="avatar rounded-circle">
						<image :src="isLogin ? member.avatar ? member.avatar : '/static/images/mine/default.png' : '/static/images/mine/default.png'"></image>
					</view>
					<view class="user-name">
						<view v-if="isLogin" class="name-text text-truncate">
							<template v-if="member.name && member.name !== '微信用户'">
								{{ member.name }}_{{ String(member.id || '').padStart(3, '0') }}
							</template>
							<template v-else>醉<text class="spade">♠</text>岛 Bar_{{ String(member.id || '').padStart(3, '0') }}</template>
						</view>
						<view v-else class="name-text" @tap="login">醉<text class="spade">♠</text>岛 Bar用户</view>
					</view>
				</view>
			</view>

			<view class="content-box">
				<!-- Floating White Card -->
				<view class="white-card">
					<view class="card-header d-flex justify-content-between align-items-center">
						<view class="welcome">
							<view class="title" v-if="!isLogin">欢迎加入醉<text class="spade">♠</text>岛 Bar</view>
							<view class="title" v-else>我的资产</view>
							
							<view class="subtitle" v-if="!isLogin">登录后查看账户信息</view>
						</view>
						<view class="login-btn" @tap="login" v-if="!isLogin">注册/登录</view>
					</view>

					<view class="stats-row d-flex align-items-center">
						<view class="stat-item flex-1">
							<view class="stat-label">存酒</view>
							<view class="stat-value">{{ isLogin ? (member.wine || 0) : '*' }}</view>
						</view>
						<view class="divider"></view>
						<view class="stat-item flex-1">
							<view class="stat-label">积分</view>
							<view class="stat-value">{{ isLogin ? Number(member.points || 0).toFixed(2) : '*' }}</view>
						</view>
						<view class="divider"></view>
						<view class="stat-item flex-1">
							<view class="stat-label">储值余额</view>
							<view class="stat-value">{{ isLogin ? Number(member.balance || 0).toFixed(2) : '*' }}</view>
						</view>
					</view>					
				</view>
			</view>
		</view>
	</layout>
</template>

<script setup>
import {
  ref,
  computed
} from 'vue'
import { useMainStore } from '@/store/store'
import { storeToRefs } from 'pinia'
import { onLoad,onShow} from '@dcloudio/uni-app'
import { formatDateTime,kmUnit } from '@/utils/util'
import {
  userGetUserInfo
} from '@/api/user'
import {
  customerLogin
} from '@/api/auth'

const main = useMainStore()
const { member,isLogin } = storeToRefs(main)

const title = ref('个人中心')
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight
const showPopup = ref(false)
const isChecked = ref(false)
const uToast = ref()

const growthValue = computed(() => { 
	if (!isLogin.value) return 0
	const {
		currentValue,
		needValue
	} = member.value
	return currentValue / (currentValue + needValue) * 100
})

onShow(() => {
	getUserInfo();
})

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
			// 先用 data 里的信息做个保底，让 isLogin 能变成 true
			main.SET_MEMBER(data.userInfo || { phone: code });
			main.SET_TOKEN(data.accessToken || data.token || '');
			
			uni.setStorage({
				key: 'userinfo',
				data: data.userInfo || { phone: code }
			});
			uni.setStorage({
				key: 'accessToken',
				data: data.accessToken || data.token || ''
			});
			
			uToast.value.show({
				message: '登录成功',
				type: 'success'
			});
			await getUserInfo();
			closePopup();
		}
	} catch (err) {
		console.error('登录失败', err);
	}
}

const showManualLogin = () => {
	uToast.value.show({
		message: '请在微信小程序中使用快捷登录',
		type: 'warning'
	});
}

const closePopup = () => {
	showPopup.value = false;
}

const onChange = () => {
	isChecked.value = !isChecked.value
}

const getUserInfo = async() => {
	// 只要 store 中有 token 或者缓存中有 token 即可请求用户信息
	const token = main.token || uni.getStorageSync('accessToken');
	if (token) {
		// 尝试从 store 或缓存中获取 phone
		const userInfo = main.member && main.member.phone ? main.member : uni.getStorageSync('userinfo') || {};
		const phone = userInfo.phone || userInfo.mobile;
		if (phone) {
			let data = await userGetUserInfo({ phone: phone });
			if (data) {
				// 保留原本的 phone 字段，防止新接口不返回 phone 导致下次查询失败
				data.phone = phone;
				main.SET_MEMBER(data);
				uni.setStorage({
					key: 'userinfo',
					data: data
				});
			}
		} else {
			console.log('未获取到手机号，跳过查询 info');
		}
	}
}

const login = () => {
	showPopup.value = true;
}

const serv = (item) => {
	switch (item.type) {
		case 'pages':
			if (!isLogin.value) {
				login()
				return
			}
			uni.navigateTo({
				url: item.pages
			})
			break;
		case 'miniprogram':
			uni.navigateToMiniProgram({
				appId: item.app_id
			})
			break;
		case 'menu':
			uni.navigateTo({
				url: '/pages/components/pages/mine/service?id=' + item.id + '&name=' + item.name
			})
			break;
		case 'content':
			uni.navigateTo({
				url: '/pages/components/pages/mine/content?id=' + item.id + '&name=' + item.name
			})
			break;
	}
}

</script>

<style lang="scss" scoped>
	page {
		height: auto;
		min-height: 100%;
		background-color: #f8f8f8;
	}

	.mine-container {
		position: relative;
		width: 100%;
		min-height: 100vh;
		background-color: #f8f8f8;
	}

	.bg-box {
		position: relative;
		width: 100%;
		height: 460rpx;
		
		.bg-img {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			z-index: 0;
		}

		.nav-bar {
			position: relative;
			z-index: 1;
			height: 44px;
		}

		.user-info-row {
			position: relative;
			z-index: 1;
			padding: 0 40rpx;
			margin-top: 20rpx;

			.avatar {
				width: 120rpx;
				height: 120rpx;
				background-color: #FFFFFF;
				display: flex;
				align-items: center;
				justify-content: center;
				border-radius: 50%;
				box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);

				image {
					width: 110rpx;
					height: 110rpx;
					border-radius: 50%;
				}
			}

			.user-name {
				margin-left: 30rpx;
				.name-text {
					font-size: 36rpx;
					font-weight: bold;
					color: #ffffff;
					text-shadow: 0 2rpx 10rpx rgba(0,0,0,0.5);
					
					.spade {
						color: #111111;
						-webkit-text-stroke: 2px #ffffff;
						font-weight: 900;
						text-shadow: none;
						margin: 0 4rpx;
					}
				}
			}
		}
	}

	.content-box {
		position: relative;
		z-index: 2;
		padding: 0 30rpx;
		margin-top: -60rpx;
	}

	.white-card {
		background-color: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx 30rpx;
		box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.05);
		margin-bottom: 30rpx;

		.card-header {
			margin-bottom: 50rpx;
			
			.welcome {
				.title {
					font-size: 32rpx;
					font-weight: bold;
					color: #333333;
					margin-bottom: 12rpx;
					
					.spade {
						color: #111111;
						-webkit-text-stroke: 1.5px #ffffff;
						position: relative;
						font-weight: 900;
						margin: 0 2rpx;
						/* 增加文字阴影，使白色描边能在白底上凸显 */
						text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.4);
					}
				}
				.subtitle {
					font-size: 24rpx;
					color: #999999;
				}
			}

			.login-btn {
				background-color: #000000;
				color: #ffffff;
				font-size: 24rpx;
				padding: 14rpx 36rpx;
				border-radius: 50rpx;
				font-weight: 500;
			}
		}

		.stats-row {
			text-align: center;
			justify-content: space-between;

			.stat-item {
				display: flex;
				flex-direction: column;
				align-items: center;

				.stat-label {
					font-size: 26rpx;
					color: #666666;
					margin-bottom: 20rpx;
				}
				.stat-value {
					font-size: 32rpx;
					font-weight: bold;
					color: #333333;
				}
			}

			.divider {
				width: 1px;
				height: 40rpx;
				background-color: #f0f0f0;
			}
		}
	}

	/* Popup mask */
	.popup-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.72);
		z-index: 100;
	}

	/* Bottom popup card */
	.popup-card {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 101;
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
			}
		}

		.phone-login-btn {
			width: 100%;
			height: 96rpx;
			line-height: 96rpx;
			background: #111111;
			color: #ffffff;
			font-size: 32rpx;
			font-weight: 700;
			border-radius: 48rpx;
			margin-bottom: 32rpx;
			letter-spacing: 0;
			box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.16);

			&::after { border: none; }
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

</style>
