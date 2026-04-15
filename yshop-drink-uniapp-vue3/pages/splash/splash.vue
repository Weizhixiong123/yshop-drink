<template>
	<view class="splash-container">
		<view v-if="!showBgImage" class="fallback-background">
			<view class="glow glow-top"></view>
			<view class="glow glow-middle"></view>
			<view class="glow glow-bottom"></view>
		</view>
		<image
			v-if="showBgImage"
			class="bg-image"
			src="/static/images/image.png"
			mode="aspectFill"
			@error="handleBgImageError"
		></image>
		<view v-if="!showBgImage" class="screen-mask"></view>
		
		<view v-if="!showBgImage" class="logo-container">
			<image
				v-if="showLogoImage"
				class="logo"
				src="/static/images/logo_white.png"
				mode="aspectFit"
				@error="handleLogoImageError"
			></image>
			<text v-else class="logo-text">Seer</text>
		</view>

		<view class="skip-btn" @tap="skip">
			跳过 {{ countdown }}
		</view>

		<view v-if="!showBgImage" class="footer-text">
			<view class="text-cn">微醺时刻，看见灵魂底色</view>
			<view class="text-en">In a moment of slight drunkenness, seeing</view>
			<view class="text-en">the soul's underlying color</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const countdown = ref(3);
const showBgImage = ref(true);
const showLogoImage = ref(true);
let timer = null;
let redirected = false;

const clearTimer = () => {
	if (timer) {
		clearInterval(timer);
		timer = null;
	}
};

const handleBgImageError = () => {
	showBgImage.value = false;
};

const handleLogoImageError = () => {
	showLogoImage.value = false;
};

const skip = () => {
	if (redirected) {
		return;
	}

	redirected = true;
	clearTimer();
	uni.switchTab({
		url: '/pages/index/index'
	});
};

onMounted(() => {
	timer = setInterval(() => {
		countdown.value--;
		if (countdown.value <= 0) {
			skip();
		}
	}, 1000);
});

onUnmounted(() => {
	clearTimer();
});
</script>

<style lang="scss" scoped>
.splash-container {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	width: 100vw;
	height: 100vh;
	overflow: hidden;
	background: linear-gradient(180deg, #080808 0%, #120f0c 62%, #22140d 100%);
}

.fallback-background {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 0;
	background:
		radial-gradient(circle at 84% 14%, rgba(239, 172, 56, 0.26) 0, rgba(239, 172, 56, 0) 10%),
		radial-gradient(circle at 90% 27%, rgba(232, 144, 52, 0.2) 0, rgba(232, 144, 52, 0) 7%),
		radial-gradient(circle at 82% 43%, rgba(253, 194, 98, 0.12) 0, rgba(253, 194, 98, 0) 6%);
}

.glow {
	position: absolute;
	border-radius: 999rpx;
	background: radial-gradient(circle, rgba(249, 178, 73, 0.85) 0, rgba(249, 178, 73, 0) 70%);
	filter: blur(10rpx);
	opacity: 0.72;
}

.glow-top {
	top: 120rpx;
	right: 36rpx;
	width: 56rpx;
	height: 56rpx;
}

.glow-middle {
	top: 250rpx;
	right: 72rpx;
	width: 38rpx;
	height: 38rpx;
	opacity: 0.55;
}

.glow-bottom {
	top: 350rpx;
	right: 24rpx;
	width: 72rpx;
	height: 72rpx;
	opacity: 0.42;
}

.bg-image {
	width: 100%;
	height: 100%;
	position: absolute;
	top: 0;
	left: 0;
	z-index: 1;
}

.screen-mask {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 1;
	background: linear-gradient(180deg, rgba(0, 0, 0, 0.2) 0%, rgba(0, 0, 0, 0.28) 48%, rgba(0, 0, 0, 0.52) 100%);
}

.logo-container {
	position: absolute;
	top: 22%;
	left: 0;
	width: 100%;
	display: flex;
	justify-content: center;
	z-index: 2;
	
	.logo {
		width: 400rpx;
		height: 150rpx;
	}

	.logo-text {
		color: #ffffff;
		font-size: 120rpx;
		font-style: italic;
		font-family: Georgia, 'Times New Roman', serif;
		font-weight: 400;
		letter-spacing: 4rpx;
		text-shadow: 0 10rpx 32rpx rgba(0, 0, 0, 0.25);
	}
}

.skip-btn {
	position: absolute;
	bottom: 92rpx;
	right: 40rpx;
	z-index: 5;
	background-color: rgba(58, 58, 58, 0.5);
	color: #fff;
	font-size: 26rpx;
	padding: 10rpx 30rpx;
	border-radius: 40rpx;
	backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 255, 255, 0.16);
}

.footer-text {
	position: absolute;
	bottom: 140rpx;
	left: 0;
	width: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	z-index: 2;
	
	.text-cn {
		color: #ffffff;
		font-size: 32rpx;
		letter-spacing: 4rpx;
		margin-bottom: 20rpx;
		font-weight: 300;
	}
	
	.text-en {
		color: #b0b0b0;
		font-size: 18rpx;
		letter-spacing: 1rpx;
		margin-bottom: 6rpx;
	}
}
</style>
