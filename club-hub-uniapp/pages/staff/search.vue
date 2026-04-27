<template>
  <view class="page-wrap">
    <view class="search-header">
      <view class="search-box">
        <view class="css-search-icon"></view>
        <input 
          class="search-input" 
          type="text" 
          maxlength="30"
          v-model="keyword" 
          placeholder="输入会员ID、姓名或手机尾号搜索..." 
          placeholder-class="ph-style" 
          @confirm="doSearch"
        />
        <view class="search-btn" @tap="doSearch">搜索</view>
      </view>
    </view>
    
    <view class="result-list">
      <view class="result-item" v-for="(item, index) in displayResults" :key="index" @tap="navToDetail(item)">
        <view class="item-header">
          <view class="user-info">
            <text class="user-name">{{item.name || '微信用户'}}</text>
            <view class="tag">
              <text class="phone-txt">{{ getMaskedPhone(item.phone) }}</text>
            </view>
          </view>
        </view>
        
        <view class="item-stats">
          <view class="stat">
            <text class="val">{{item.liquor}}</text>
            <text class="lbl">存酒</text>
          </view>
          <view class="stat">
            <text class="val">{{item.points}}</text>
            <text class="lbl">积分</text>
          </view>
          <view class="stat">
            <text class="val">¥{{item.balance}}</text>
            <text class="lbl">余额</text>
          </view>
        </view>
          
        <view class="item-remark" v-if="item.remark">
          备注: {{item.remark}}
        </view>
      </view>
      
      <view class="empty-state" v-if="hasSearched && results.length === 0">
        <view class="empty-icon">📂</view>
        <text>未找到匹配该关键词的会员</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { memberSearch } from '@/api/staff'
import { connectStaffSocket } from '@/utils/staffSocket'

const keyword = ref('')
const hasSearched = ref(false)
const results = ref([])
const pageVisible = ref(false)

const displayResults = computed(() => results.value)

onLoad(() => {
  uni.$on('MEMBER_DATA_UPDATE', onMemberDataUpdate)
})

onShow(() => {
  pageVisible.value = true
  connectStaffSocket()
})

onHide(() => {
  pageVisible.value = false
})

onUnload(() => {
  uni.$off('MEMBER_DATA_UPDATE', onMemberDataUpdate)
})

const getMaskedPhone = (phone) => {
  if (!phone) return '****'
  if(results.value.length > 1) {
    return phone;
  }
  return `****${phone.slice(-4)}`
}

const doSearch = async (silent = false) => {
  keyword.value = String(keyword.value || '').trim()
  if (!keyword.value) {
    uni.showToast({ title: '请输入搜索词', icon: 'none' })
    return
  }
  
  hasSearched.value = true;
  if (!silent) {
    uni.showLoading({ title: '搜索中...' })
  }
  
  try {
    const data = await memberSearch(keyword.value)
    // API 返回的 wine 字段映射为前端的 liquor 以保持显示一致
    if (Array.isArray(data)) {
      results.value = data.map(item => ({
        ...item,
        liquor: item.wine != null ? item.wine : item.liquor,
        balance: item.balance != null ? Number(item.balance).toFixed(2) : '0.00'
      }))
    } else {
      results.value = []
    }
  } catch (err) {
    console.error('[memberSearch] error:', err)
    results.value = []
  } finally {
    if (!silent) {
      uni.hideLoading()
    }
  }
}

const onMemberDataUpdate = (message) => {
  if (!pageVisible.value) {
    return
  }
  const existsInCurrentResult = results.value.some(item => Number(item.id) === Number(message.memberId))
  if (existsInCurrentResult) {
    doSearch(true)
  }
}

const navToDetail = (item) => {
  // 使用会员 ID 跳转到详情页，由详情页调 API 获取最新数据
  uni.navigateTo({
    url: `/pages/staff/detail?id=${item.id}`
  })
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f5f2eb;
  padding: 30rpx;
}

.search-header {
  margin-bottom: 40rpx;

  .search-box {
    display: flex;
    align-items: center;
    background: #ffffff;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 99rpx;
    height: 96rpx;
    padding: 0 12rpx 0 30rpx;
    box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.04);

    .css-search-icon {
      position: relative;
      width: 32rpx;
      height: 32rpx;
      border: 5rpx solid #111111;
      border-radius: 50%;
      margin-right: 20rpx;
      margin-left: 4rpx;
      box-sizing: border-box;

      &::after {
        content: '';
        position: absolute;
        bottom: -7rpx;
        right: -9rpx;
        width: 12rpx;
        height: 5rpx;
        background: #111111;
        transform: rotate(45deg);
        border-radius: 3rpx;
      }
    }

    .search-input {
      flex: 1;
      height: 100%;
      color: #111111;
      font-size: 30rpx;
    }

    .search-btn {
      width: 140rpx;
      height: 76rpx;
      line-height: 76rpx;
      text-align: center;
      background: #111111;
      color: #ffffff;
      font-size: 28rpx;
      font-weight: bold;
      border-radius: 99rpx;

      &:active { opacity: 0.8; }
    }
  }
}

:deep(.ph-style) {
  color: #a0a0a0;
  font-size: 28rpx;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.result-item {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.06);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
    background: #fafafa;
  }

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    border-bottom: 1px dashed rgba(0,0,0,0.06);
    padding-bottom: 20rpx;

    .user-info {
      display: flex;
      align-items: center;

      .user-name {
        color: #111111;
        font-size: 32rpx;
        font-weight: bold;
        margin-right: 16rpx;
      }
      .tag {
        background: #f5f5f5;
        border: 1px solid rgba(0,0,0,0.05);
        border-radius: 8rpx;
        padding: 4rpx 12rpx;
        .phone-txt { color: #555555; font-size: 24rpx; }
      }
    }


  .item-stats {
    display: flex;
    justify-content: space-around;
    
    .stat {
      display: flex;
      flex-direction: column;
      align-items: center;

      .val {
        color: #111111;
        font-size: 40rpx;
        font-weight: 600;
        margin-bottom: 8rpx;
      }

      .lbl {
        color: #8a8a8a;
        font-size: 24rpx;
      }
    }
  }
  
  .item-remark {
    margin-top: 24rpx;
    padding-top: 20rpx;
    border-top: 1px dashed rgba(0,0,0,0.06);
    color: #666666;
    font-size: 24rpx;
    line-height: 1.4;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: #8a8a8a;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
    opacity: 0.5;
  }
}
</style>
