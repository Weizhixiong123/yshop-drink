<script setup>
import { onLoad } from '@dcloudio/uni-app'

onLoad(() => {
  uni.redirectTo({ url: '/pages/staff/login' })
})
</script>

<template>
  <view class="redirect-page"></view>
</template>

<style lang="scss" scoped>
.redirect-page {
  min-height: 100vh;
  background: #0d0b0a;
}
</style>
