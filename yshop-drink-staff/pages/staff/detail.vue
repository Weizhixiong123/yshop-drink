<template>
  <view class="page-wrap" v-if="member">
    <!-- User Ident Card -->
    <view class="card user-card">
      <view class="ident-box">
        <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
        <view class="info">
          <view class="name">{{ member.name }}</view>
          <view class="phone">{{ getDisplayPhone() }}</view>
        </view>
      </view>
      <view class="remark-box" v-if="member.remark">
        <text class="label">客户备注：</text>
        <text class="r-text">{{ member.remark }}</text>
      </view>
    </view>

    <view class="section-title">业务操作</view>

    <!-- Operations Card -->
    <view class="card op-card">
      <!-- 存酒 -->
      <view class="op-row">
        <view class="left">
          <view class="lbl">当前存酒库 (瓶)</view>
          <view class="val">{{ member.liquor }}</view>
        </view>
        <view class="right btns">
          <view class="btn primary" @tap="openModal('liquor', 'add')">+ 存入酒水</view>
          <view class="btn outline" @tap="openModal('liquor', 'sub')">- 取出酒水</view>
        </view>
      </view>
      
      <view class="divider"></view>

      <!-- 积分 -->
      <view class="op-row">
        <view class="left">
          <view class="lbl">剩余积分</view>
          <view class="val">{{ member.points }}</view>
        </view>
        <view class="right btns">
          <view class="btn primary" @tap="openModal('points', 'add')">+ 赠送/赢牌</view>
          <view class="btn outline" @tap="openModal('points', 'sub')">- 兑换扣减</view>
        </view>
      </view>

      <view class="divider"></view>

      <!-- 储值余额展示 -->
      <view class="op-row balance-row">
        <view class="left">
          <view class="lbl">储值余额</view>
          <view class="val price">¥{{ member.balance }}</view>
        </view>
        <view class="right btns">
          <view class="btn primary" @tap="openModal('balance', 'add')">+ 储值充值</view>
          <view class="btn outline" @tap="openModal('balance', 'sub')">- 消费扣减</view>
        </view>
      </view>
    </view>

    <!-- Operation Modal -->
    <view class="modal-mask" v-if="showModal" @tap="closeModal"></view>
    <view class="custom-modal" :class="{'show': showModal}">
      <view class="m-header">{{ modalTitle }}</view>
      <view class="m-body">
        <view class="m-info">当前系统值：<text class="v">{{ originalVal }}</text></view>
        
        <view class="input-wrap">
          <view class="prefix">{{ opType === 'add' ? '+' : '-' }}</view>
          <input 
            class="m-input" 
            type="number" 
            v-model="opAmount" 
            placeholder="输入变更数量" 
            placeholder-style="color:#a0a0a0;"
            :focus="showModal"
          />
        </view>
        
        <view class="m-preview">
          操作后最终结果为：
          <text class="highlight">{{ previewVal }}</text>
        </view>
      </view>
      
      <view class="m-footer">
        <view class="f-btn cancel" @tap="closeModal">取消</view>
        <view class="f-btn confirm" @tap="submitOp">确认执行</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const member = ref(null)

const showModal = ref(false)
const opTarget = ref('') // 'liquor' | 'points'
const opType = ref('')   // 'add' | 'sub'
const opAmount = ref('')

onLoad((options) => {
  if (options.member) {
    try {
      member.value = JSON.parse(decodeURIComponent(options.member))
    } catch(e) {
      console.error(e)
    }
  }
})

const getDisplayPhone = () => {
    return member.value?.phone || '未知'
}

const modalTitle = computed(() => {
  let t = '存酒';
  if (opTarget.value === 'points') t = '积分';
  if (opTarget.value === 'balance') t = '储值余额';
  const action = opType.value === 'add' ? '增加' : '扣减';
  return `${action}${t}操作`;
})

const originalVal = computed(() => {
  if(!member.value) return 0;
  return Number(member.value[opTarget.value]) || 0
})

const previewVal = computed(() => {
  const current = originalVal.value;
  const mod = Number(opAmount.value) || 0;
  
  let res = 0;
  if (opType.value === 'add') {
    res = current + mod;
  } else {
    res = current - mod;
    if (res < 0) res = 0;
  }
  
  if (opTarget.value === 'balance') {
    return res.toFixed(2);
  }
  return res;
})

const openModal = (target, type) => {
  opTarget.value = target
  opType.value = type
  opAmount.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const submitOp = () => {
  const mod = Number(opAmount.value)
  if(!mod || mod <= 0) {
    uni.showToast({ title: '请输入有效的数量', icon: 'none' })
    return
  }

  if (opType.value === 'sub' && mod > originalVal.value) {
    uni.showToast({ title: '扣减数量不能大于当前总量', icon: 'none' })
    return
  }

  uni.showLoading({ title: '处理中...' })
  setTimeout(() => {
    member.value[opTarget.value] = previewVal.value
    uni.hideLoading()
    uni.showToast({ title: '操作成功', icon: 'success' })
    closeModal()
  }, 600)
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f5f2eb;
  padding: 30rpx;
  position: relative;
}

.card {
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.08);
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.user-card {
  .ident-box {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;

    .avatar {
      width: 90rpx;
      height: 90rpx;
      border-radius: 50%;
      background: #111111;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      font-size: 40rpx;
      font-weight: bold;
      margin-right: 24rpx;
      box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.1);
    }

    .info {
      .name {
        color: #111111;
        font-size: 34rpx;
        font-weight: bold;
        margin-bottom: 6rpx;
      }
      .phone {
        color: #666666;
        font-size: 26rpx;
        letter-spacing: 1px;
      }
    }
  }

  .remark-box {
    background: #f7f7f7;
    padding: 20rpx;
    border-radius: 12rpx;
    border-left: 6rpx solid #111111;

    .label {
      color: #8a8a8a;
      font-size: 24rpx;
      display: block;
      margin-bottom: 6rpx;
    }
    .r-text {
      color: #333333;
      font-size: 26rpx;
      line-height: 1.5;
    }
  }
}

.section-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
}

.op-card {
  padding: 0 30rpx;
}

.divider {
  height: 1px;
  background: rgba(0,0,0,0.06);
}

.op-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 0;

  .left {
    .lbl {
      color: #8a8a8a;
      font-size: 24rpx;
      margin-bottom: 10rpx;
    }
    .val {
      color: #111111;
      font-size: 48rpx;
      font-weight: 600;
      &.price { color: #111111; }
    }
  }

  .btns {
    display: flex;
    flex-direction: column;
    gap: 16rpx;

    .btn {
      width: 180rpx;
      height: 60rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      font-weight: bold;

      &.primary {
        background: #111111;
        color: #ffffff;
      }

      &.outline {
        border: 1px solid rgba(0,0,0,0.1);
        color: #111111;
        background: #ffffff;
      }

      &:active { opacity: 0.8; transform: scale(0.96); }
    }
  }

  &.balance-row {
    .status-txt {
      color: #73c991;
      font-size: 24rpx;
      background: rgba(115, 201, 145, 0.1);
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
    }
  }
}

/* Modal styling */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.72);
  z-index: 100;
}

.custom-modal {
  position: fixed;
  top: 50%; left: 50%;
  transform: translate(-50%, -40%);
  width: 620rpx;
  background: #ffffff;
  border-radius: 34rpx;
  z-index: 101;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s;
  box-shadow: 0 -18rpx 48rpx rgba(0, 0, 0, 0.16);

  &.show {
    opacity: 1;
    pointer-events: auto;
    transform: translate(-50%, -50%);
  }

  .m-header {
    text-align: center;
    color: #111111;
    font-size: 34rpx;
    font-weight: bold;
    padding: 40rpx 0 20rpx;
  }

  .m-body {
    padding: 20rpx 40rpx;

    .m-info {
      color: #8c8c8c;
      font-size: 26rpx;
      text-align: center;
      margin-bottom: 30rpx;
      .v { color: #111111; font-weight: bold; }
    }

    .input-wrap {
      display: flex;
      align-items: center;
      background: #f7f7f7;
      border: 1px solid rgba(0, 0, 0, 0.06);
      border-radius: 16rpx;
      height: 90rpx;
      padding: 0 30rpx;

      .prefix {
        color: #111111;
        font-size: 36rpx;
        font-weight: bold;
        margin-right: 16rpx;
      }
      .m-input {
        flex: 1;
        height: 100%;
        color: #111111;
        font-size: 32rpx;
      }
    }

    .m-preview {
      margin-top: 30rpx;
      text-align: center;
      color: #8c8c8c;
      font-size: 26rpx;
      
      .highlight {
        color: #111111;
        font-size: 36rpx;
        font-weight: bold;
        margin-left: 10rpx;
      }
    }
  }

  .m-footer {
    display: flex;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    margin-top: 30rpx;

    .f-btn {
      flex: 1;
      height: 110rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;

      &.cancel { color: #8a8a8a; border-right: 1px solid rgba(0, 0, 0, 0.06); }
      &.confirm { color: #111111; font-weight: bold; }
      
      &:active { background: rgba(0,0,0,0.03); }
    }
  }
}
</style>
