<template>
	<layout>
		<view class="mine-container">
			<!-- Top Background -->
			<view class="bg-box">
				<!-- We use a background image matching the theme, you can replace the src with your actual background asset -->
				<image class="bg-img" src="https://images.unsplash.com/photo-1527281400683-1aae777175f8?q=80&w=600&auto=format&fit=crop" mode="aspectFill"></image>
				
				<view :style="{ height: statusBarHeight + 'px' }"></view>
				<view class="nav-bar"></view>
				
				<!-- User Info Row -->
				<view class="user-info-row d-flex align-items-center">
					<view class="avatar rounded-circle">
						<image :src="isLogin ? member.avatar ? member.avatar : '/static/images/mine/default.png' : '/static/images/mine/default.png'"></image>
					</view>
					<view class="user-name">
						<view v-if="isLogin" class="name-text text-truncate" @tap="serv({type:'pages',pages:'/pages/components/pages/mine/userinfo'})">{{ member.nickname }}</view>
						<view v-else class="name-text" @tap="login">Seer Bar用户</view>
					</view>
				</view>
			</view>

			<view class="content-box">
				<!-- Floating White Card -->
				<view class="white-card">
					<view class="card-header d-flex justify-content-between align-items-center">
						<view class="welcome">
							<view class="title">欢迎加入Seer Bar</view>
							<view class="subtitle">登录后解锁更多专属特权</view>
						</view>
						<view class="login-btn" @tap="login" v-if="!isLogin">注册/登录</view>
					</view>
					
					<view class="stats-row d-flex align-items-center">
						<view class="stat-item flex-1" @tap="serv({type:'pages', pages: '/pages/components/pages/balance/bill?cate=0'})">
							<view class="stat-label">余额</view>
							<view class="stat-value">{{ isLogin ? member.nowMoney : '*' }}</view>
						</view>
						<view class="divider"></view>
						<view class="stat-item flex-1" @tap="serv({type:'pages',pages:'/pages/components/pages/coupons/coupons'})">
							<view class="stat-label">优惠券</view>
							<view class="stat-value">{{ isLogin ? member.couponCount : '*' }}</view>
						</view>
						<view class="divider"></view>
						<view class="stat-item flex-1" @tap="serv({type:'pages', pages: '/pages/components/pages/balance/bill?cate=1'})">
							<view class="stat-label">积分</view>
							<view class="stat-value">{{ isLogin ? member.integral : '*' }}</view>
						</view>
					</view>
				</view>

				<!-- Services List -->
				<view class="service-list">
					<uv-cell-group :border="false">
						<block v-for="(item, index) in services" :key='index'>
							<uv-cell :title="item.name" v-if="item.type == 'contact'" :isLink="true" :border="index !== services.length - 1"></uv-cell>
							<uv-cell :isLink="true" :title="item.name" v-else-if="item.type == 'call'" v-on:click="makePhoneCall(item.phone)" :border="index !== services.length - 1"></uv-cell>
							<uv-cell :isLink="true" :title="item.name" v-else @tap="serv(item)" :border="index !== services.length - 1"></uv-cell>
						</block>
					</uv-cell-group>
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
  userGetUserInfo,
  mineService
} from '@/api/user'

const main = useMainStore()
const { member,isLogin } = storeToRefs(main)

const title = ref('个人中心')
const services = ref([])
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight

const growthValue = computed(() => { 
	if (!isLogin.value) return 0
	const {
		currentValue,
		needValue
	} = member.value
	return currentValue / (currentValue + needValue) * 100
})

onLoad(() => {
	getServices();
})	
onShow(() => {
	getUserInfo();
})

const getUserInfo = async() => {
	if (isLogin.value) {
		let data = await userGetUserInfo();
		if (data) {
			main.SET_MEMBER(data);
		}
	}
}
const defaultServices = [
	{ name: '订单中心', type: 'pages', pages: '/pages/components/pages/orders/orders' },
	{ name: '个人信息', type: 'pages', pages: '/pages/components/pages/mine/userinfo' },
	{ name: '客服中心', type: 'contact' },
	{ name: '我的地址', type: 'pages', pages: '/pages/components/pages/address/address' },
	{ name: '会员储值', type: 'pages', pages: '/pages/components/pages/balance/bill?cate=0' },
	{ name: '积分商城', type: 'pages', pages: '/pages/components/pages/scoreproduct/list' },
	{ name: '我的优惠券', type: 'pages', pages: '/pages/components/pages/coupons/coupons' },
]

const getServices = async() => {
	let data = null;
	try {
		data = await mineService();
	} catch(e) {
		console.log('mineService error:', e);
	}
	
	if (data && data.length > 0) {
		const hasUserInfo = data.some(item => item.name === '个人信息');
		const hasScore = data.some(item => item.name === '积分商城');

		if (!hasUserInfo) {
			data.splice(1, 0, { name: '个人信息', type: 'pages', pages: '/pages/components/pages/mine/userinfo' });
		}
		if (!hasScore) {
			let couponIdx = data.findIndex(item => item.name === '我的优惠券' || item.name === '卡券中心');
			if (couponIdx !== -1) {
				data.splice(couponIdx, 0, { name: '积分商城', type: 'pages', pages: '/pages/components/pages/scoreproduct/list' });
			} else {
				data.push({ name: '积分商城', type: 'pages', pages: '/pages/components/pages/scoreproduct/list' });
			}
		}
		services.value = data;
	} else {
		services.value = defaultServices;
	}
}
const makePhoneCall = (phoneNumber) => {
	uni.makePhoneCall({
		phoneNumber: phoneNumber,
	})
}
const login = () => {
	uni.navigateTo({
		url: '/pages/components/pages/login/login'
	})
}
const packages = () => {
	if (!isLogin.value) {
		login()
		return
	}
	uni.navigateTo({
		url: '/pages/components/pages/packages/index'
	})
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

	.service-list {
		background-color: #ffffff;
		border-radius: 20rpx;
		padding: 10rpx 20rpx;
		margin-bottom: 40rpx;
		box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.02);

		::v-deep .uv-cell {
			padding: 30rpx 10rpx !important;
		}
		
		::v-deep .uv-cell__title-text {
			font-size: 30rpx;
			color: #333333;
		}
	}
</style>
