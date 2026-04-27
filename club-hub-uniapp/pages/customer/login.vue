<template>
  <view class="page">
    <view class="header">
      <view class="brand">醉<text>♠</text>岛 Bar</view>
      <view class="title">客户登录</view>
      <view class="desc">输入办卡手机号，查看你的会员信息</view>
    </view>

    <view class="form">
      <input
        class="input"
        type="number"
        maxlength="11"
        v-model="phone"
        placeholder="请输入手机号"
        placeholder-class="placeholder"
      />
      <button class="submit" :disabled="loading" @tap="submit">
        {{ loading ? '登录中...' : '登录并查看' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { customerLogin } from '@/api/customerAuth'
import cookie from '@/utils/cookie'

const phone = ref('')
const loading = ref(false)

const submit = async () => {
  const value = String(phone.value || '').trim()
  if (!/^1[3-9]\d{9}$/.test(value)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  if (loading.value) return
  loading.value = true

  try {
    const data = await customerLogin({ phone: value })
    const token = data?.token || data?.accessToken || (typeof data === 'string' ? data : '')
    if (!token) {
      uni.showToast({ title: '登录失败：未返回登录凭证', icon: 'none' })
      return
    }
    cookie.set('accessToken', token)
    cookie.set('userRole', data?.role || 'customer')
    cookie.set('customerPhone', data?.phone || value)
    uni.reLaunch({ url: '/pages/customer/info' })
  } catch (err) {
    console.error('[customerLogin] error:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 110rpx 44rpx 56rpx;
  box-sizing: border-box;
  background: #f7f4ef;
}

.header {
  margin-bottom: 72rpx;
}

.brand {
  color: #11100f;
  font-size: 38rpx;
  font-weight: 900;
  margin-bottom: 56rpx;

  text {
    color: #c1272d;
    margin: 0 8rpx;
  }
}

.title {
  color: #11100f;
  font-size: 58rpx;
  font-weight: 900;
  margin-bottom: 18rpx;
}

.desc {
  color: #786f65;
  font-size: 28rpx;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.input {
  height: 104rpx;
  padding: 0 30rpx;
  border-radius: 14rpx;
  background: #fff;
  color: #11100f;
  font-size: 32rpx;
  border: 1px solid #ebe2d8;
}

.placeholder {
  color: #b7aca0;
}

.submit {
  width: 100%;
  height: 104rpx;
  line-height: 104rpx;
  margin: 0;
  border-radius: 14rpx;
  background: #11100f;
  color: #fff;
  font-size: 32rpx;
  font-weight: 800;

  &::after {
    border: none;
  }

  &[disabled] {
    opacity: 0.58;
  }
}
</style>
