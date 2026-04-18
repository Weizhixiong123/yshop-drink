<template>
  <view class="layout">
    <slot />
  </view>
</template>

<script setup>

const props = defineProps(['size'])
</script>

<style lang="less">
.layout {
  min-height: 100%;
}
</style>
