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
		<view class="screen-mask"></view>
		
		<view class="logo-container">
			<image
				class="logo"
				src="/static/images/logo_white.png"
				mode="aspectFit"
			></image>
		</view>

		<view class="skip-btn" @tap="skip">
			跳过 {{ countdown }}
		</view>

		<view class="footer-text">
			<view class="text-cn">底牌未揭，ALL IN 今夜</view>
			<view class="text-en">TEXAS HOLD'EM & PREMIUM PUB</view>
			<view class="text-en">May the flop be with you</view>
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
@keyframes fadeInSlideUp {
	0% { opacity: 0; transform: translateY(30rpx); }
	100% { opacity: 1; transform: translateY(0); }
}

@keyframes pulseGlow {
	0% { transform: scale(1); opacity: 0.6; }
	50% { transform: scale(1.1); opacity: 0.8; }
	100% { transform: scale(1); opacity: 0.6; }
}

@keyframes floatLogo {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0px); }
}

.splash-container {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	width: 100vw;
	height: 100vh;
	overflow: hidden;
	background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
}

.fallback-background {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 0;
	background:
		radial-gradient(circle at 84% 14%, rgba(255, 204, 112, 0.2) 0, rgba(255, 204, 112, 0) 20%),
		radial-gradient(circle at 90% 27%, rgba(255, 140, 0, 0.15) 0, rgba(255, 140, 0, 0) 15%),
		radial-gradient(circle at 20% 70%, rgba(138, 43, 226, 0.1) 0, rgba(138, 43, 226, 0) 30%);
}

.glow {
	position: absolute;
	border-radius: 999rpx;
	background: radial-gradient(circle, rgba(255, 204, 112, 0.8) 0, rgba(255, 204, 112, 0) 70%);
	filter: blur(15rpx);
	opacity: 0.6;
	animation: pulseGlow 4s infinite ease-in-out;
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
	z-index: 0;
}

.screen-mask {
	position: absolute;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 1;
	background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.3) 50%, rgba(0, 0, 0, 0.7) 100%);
}

.logo-container {
	position: absolute;
	top: 22%;
	left: 0;
	width: 100%;
	display: flex;
	justify-content: center;
	z-index: 2;
	animation: floatLogo 6s infinite ease-in-out;
	
	.logo {
		width: 400rpx;
		height: 150rpx;
		filter: drop-shadow(0 10rpx 20rpx rgba(0,0,0,0.5));
	}

	.logo-text {
		color: #ffffff;
		font-size: 130rpx;
		font-style: italic;
		font-family: Georgia, 'Times New Roman', serif;
		font-weight: 500;
		letter-spacing: 6rpx;
		text-shadow: 0 15rpx 40rpx rgba(255, 190, 80, 0.4);
		background: linear-gradient(to right, #ffd700, #ff8c00);
		-webkit-background-clip: text;
		background-clip: text;
		-webkit-text-fill-color: transparent;
	}
}

.skip-btn {
	position: absolute;
	bottom: 92rpx;
	right: 40rpx;
	z-index: 5;
	background: rgba(255, 255, 255, 0.1);
	backdrop-filter: blur(15px);
	border: 1px solid rgba(255, 255, 255, 0.2);
	box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
	color: #fff;
	font-size: 26rpx;
	padding: 12rpx 36rpx;
	border-radius: 40rpx;
	transition: all 0.3s ease;
}

.skip-btn:active {
    transform: scale(0.95);
    background: rgba(255, 255, 255, 0.2);
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
	opacity: 0;
	animation: fadeInSlideUp 1.2s ease-out 0.5s forwards;
	
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
