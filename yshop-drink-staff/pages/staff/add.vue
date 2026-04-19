<template>
  <view class="page-wrap">
    <view class="form-container">
      <view class="form-item">
        <text class="label">姓名</text>
        <input class="input" v-model="form.name" placeholder="请输入姓名" placeholder-class="ph-style" />
      </view>

      <view class="form-item">
        <text class="label">性别</text>
        <view class="radio-group">
          <view class="radio-item" :class="{active: form.gender === 1}" @tap="form.gender = 1">先生</view>
          <view class="radio-item" :class="{active: form.gender === 0}" @tap="form.gender = 0">女士</view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">手机号 <text class="req">*</text></text>
        <input class="input" type="number" maxlength="11" v-model="form.phone" placeholder="请输入11位手机号" placeholder-class="ph-style" @blur="checkPhone" />
      </view>

      <view class="row-items">
        <view class="form-item half">
          <text class="label">初始存酒数 (瓶)</text>
          <input class="input" type="number" v-model="form.liquor" placeholder="0" placeholder-class="ph-style" />
        </view>
        <view class="form-item half">
          <text class="label">初始积分</text>
          <input class="input" type="number" v-model="form.points" placeholder="0" placeholder-class="ph-style" />
        </view>
      </view>

      <view class="form-item">
        <text class="label">初始储值余额 (元)</text>
        <input class="input" type="digit" v-model="form.balance" placeholder="0.00" placeholder-class="ph-style" />
      </view>

      <view class="form-item">
        <text class="label">备注</text>
        <textarea class="textarea" v-model="form.remark" placeholder="这里可以填写客人的偏好（如：喜欢靠窗、常喝威士忌...）" placeholder-class="ph-style"></textarea>
      </view>

      <view class="submit-btn" @tap="submit">确认开卡</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({
  name: '',
  gender: 1, // 1: Male, 0: Female
  phone: '',
  liquor: '',
  points: '',
  balance: '',
  remark: ''
})

const checkPhone = () => {
  if (form.value.phone.length === 11) {
    if (form.value.phone === '13800138000') {
      uni.showToast({
        title: '该手机号已注册会员，请勿重复开卡',
        icon: 'none',
        duration: 3000
      })
    }
  }
}

const submit = () => {
  if (!form.value.phone) {
    uni.showToast({ title: '手机号为必填项', icon: 'none' })
    return
  }
  if (form.value.phone.length !== 11) {
    uni.showToast({ title: '请输入正确的11位手机号', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '正在开卡...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '开卡成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 800)
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f5f2eb;
  padding: 30rpx;
}

.form-container {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.08);
}

.form-item {
  margin-bottom: 40rpx;

  .label {
    display: block;
    font-size: 28rpx;
    color: #111111;
    font-weight: bold;
    margin-bottom: 16rpx;
    .req { color: #e74c3c; margin-left: 8rpx; }
  }

  .input {
    width: 100%;
    height: 90rpx;
    background: #f7f7f7;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 16rpx;
    padding: 0 30rpx;
    color: #111111;
    font-size: 30rpx;
    box-sizing: border-box;
    transition: all 0.2s;
    &:focus { border-color: #111111; background: #ffffff; }
  }

  .textarea {
    width: 100%;
    height: 180rpx;
    background: #f7f7f7;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 16rpx;
    padding: 24rpx 30rpx;
    color: #111111;
    font-size: 30rpx;
    box-sizing: border-box;
    transition: all 0.2s;
    &:focus { border-color: #111111; background: #ffffff; }
  }
}

:deep(.ph-style) {
  color: #a0a0a0;
}

.row-items {
  display: flex;
  justify-content: space-between;
  .half {
    width: 48%;
  }
}

.radio-group {
  display: flex;
  gap: 20rpx;

  .radio-item {
    flex: 1;
    height: 90rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f7f7;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 16rpx;
    color: #666;
    font-size: 30rpx;

    &.active {
      background: #111111;
      border-color: #111111;
      color: #ffffff;
      font-weight: bold;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: #111111;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 60rpx;
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.16);
  
  &:active { opacity: 0.9; transform: scale(0.98); }
}
</style>
