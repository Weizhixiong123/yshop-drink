<template>
  <view class="page-wrap">
    <view class="search-header">
      <view class="search-box">
        <view class="css-search-icon"></view>
        <input 
          class="search-input" 
          type="number" 
          maxlength="11"
          v-model="keyword" 
          placeholder="输入手机号或尾号(4位及以上)搜索..." 
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
          <view class="nav-arrow">前往操作 ></view>
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
        <text>未找到匹配该尾号的会员</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const keyword = ref('')
const hasSearched = ref(false)
const results = ref([])

const db = [
  { id: 1, name: '陈先生', phone: '13812345678', liquor: 2, points: 500, balance: '1000.00', remark: '喜欢窗边位置' },
  { id: 2, name: '李女士', phone: '13987655678', liquor: 0, points: 150, balance: '200.00', remark: '不要加冰' },
  { id: 3, name: '张先生', phone: '13500000001', liquor: 12, points: 8888, balance: '5000.00', remark: '大客户' },
  { id: 4, name: '王老板', phone: '13800001111', liquor: 5, points: 2000, balance: '8888.00', remark: '尾号1111测试账户' },
]

const displayResults = computed(() => results.value)

const getMaskedPhone = (phone) => {
  if(results.value.length > 1) {
    return phone;
  }
  return `****${phone.slice(-4)}`
}

const doSearch = () => {
  if (!keyword.value) {
    uni.showToast({ title: '请输入搜索词', icon: 'none' })
    return
  }
  
  hasSearched.value = true;
  uni.showLoading({ title: '搜索中...' })
  
  setTimeout(() => {
    const res = db.filter(m => m.phone.includes(keyword.value))
    results.value = res;
    uni.hideLoading()
  }, 400)
}

const navToDetail = (item) => {
  uni.navigateTo({
    url: `/pages/staff/detail?member=${encodeURIComponent(JSON.stringify(item))}`
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

    .nav-arrow {
      color: #8c8c8c;
      font-size: 24rpx;
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
