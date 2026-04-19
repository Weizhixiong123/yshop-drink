<template>
	<layout>
		<uv-navbar
		  :fixed="false"
		  title="seer"
		  bgColor="#111111"
		  left-arrow
		  leftIconColor="#f5f1e8"
		  :titleStyle="{color:'#f5f1e8', fontSize:'32rpx', fontWeight:'700'}"
		  @leftClick="$onClickLeft"
		/>
		
		<view class="container" v-if="!loading">
		<view class="main">
			<view class="nav">
				<view class="header">
					<view class="header-main">
						<view class="left" v-if="orderType == 'takein'">
							<view class="store-name" @tap="selectShop()">
								<text>{{ store.name || 'Seer Bar' }}</text>
							</view>
							<view class="store-location">
								<text class="location-dot"></text>
								<text>距离您{{kmUnit(store.dis)}}</text>
							</view>
						</view>
						<view class="left overflow-hidden" v-else>
							<view class="store-name" @tap="selectShop()">
								<view>{{ store.name || 'Seer Bar' }}</view>
							</view>
							<view class="store-location">
								<text class="location-dot"></text>
								<text v-if="store.distance > 0">配送距离{{store.distance}}km</text>
								<text v-else>本店暂不支持外卖</text>
							</view>
						</view>
						<view class="right">
							<view class="dinein" :class="{active: orderType == 'takein'}" @tap="takein">
								<text>堂食</text>
							</view>
							<view class="takeout" :class="{active: orderType == 'takeout'}" @tap="takout">
								<text>外卖</text>
							</view>
						</view>
					</view>
					<view class="header-sub">
						<view class="header-notice">{{ store.notice || '门店详情信息' }}</view>
						<view class="header-more" @tap="selectShop()">更多</view>
					</view>
				</view>
			</view>
		
			<!-- #ifdef H5 -->
			<view class="content"
				:style="{height: 'calc(100vh - 360rpx)'}">
				<!-- #endif -->
				<!-- #ifndef H5 -->
				<view class="content" :style="{height: 'calc(100vh - 360rpx)'}">
					<!-- #endif -->
					<scroll-view class="menus" :scroll-into-view="menuScrollIntoView" scroll-with-animation scroll-y>
						<view class="wrapper">
							<view class="menu" :id="`menu-${item.id}`" :class="{'current': item.id === currentCateId}"
								v-for="(item, index) in goods" :key="index" @tap="handleMenuTap(item.id)">
								<text>{{ item.name }}</text>
								<view class="dot" v-show="menuCartNum(item.id)">{{ menuCartNum(item.id) }}</view>
							</view>
						</view>
					</scroll-view>
					<!-- goods list begin -->
					<scroll-view class="goods" scroll-with-animation scroll-y :scroll-top="cateScrollTop"
						@scroll="handleGoodsScroll">
						<view class="wrapper">
							<view class="goods-toolbar">
								<view class="search-shell">
									<text class="search-shell-icon">⌕</text>
									<text class="search-shell-text">搜索</text>
								</view>
								<view class="toolbar-cate">{{ currentCateName }}</view>
							</view>
							<view class="list">
								<!-- category begin -->
								<view class="category" v-for="(item, index) in goods" :key="index"
									:id="`cate-${item.id}`">
									<view class="title">
										<text>{{ item.name }}</text>
										<image v-if="item.icon" mode="aspectFill" :src="item.icon" class="icon"></image>
									</view>
									<view class="items">
										<!-- 商品 begin -->
										<view class="good" v-for="(good, key) in item.goodsList" :key="key"
											:class="{'backgroud-grey': good.stock <= 0}">
											<image mode="aspectFill" :src="good.image" class="image"
												@tap="showGoodDetailModal(item, good)"></image>
											<view class="right">
												<text class="name">{{ good.storeName }}</text>
												<text class="tips">{{ good.storeInfo }}</text>
												<view class="price_and_action">
													<text class="price">￥{{ good.price }}</text>
													<view class="btn-group" v-if="good.stock > 0">
														<button type="primary" class="btn property_btn"
															hover-class="none" size="mini"
															@tap="showGoodDetailModal(item, good)">
															选规格
														</button>
														<view class="dot" v-show="goodCartNum(good.id)">
															{{ goodCartNum(good.id) }}</view>
													</view>
											

													<view v-if="good.stock == 0" class="soldout">已售罄</view>
												</view>

											</view>
										</view>
										<!-- 商品 end -->
									</view>
								</view>
								<!-- category end -->
								<view style="height: 110rpx;"></view>
							</view>
						</view>
					</scroll-view>
					<!-- goods list end -->
				</view>
				<!-- content end -->
				<!-- 购物车栏 begin -->
				<view class="cart-box" v-if="cart.length > 0">
					<view class="mark">
						<image src="/static/images/menu/cart.png" class="cart-img" @tap="openCartPopup"></image>
						<view class="tag">{{ getCartGoodsNumber }}</view>
					</view>
					<view class="price" @tap="openCartPopup">￥{{ getCartGoodsPrice }}</view>
					<button type="primary" class="pay-btn" @tap="toPay" :disabled="disabledPay">
						{{ disabledPay ? `差${spread}元起送` : '去结算' }}
					</button>
				</view>
				<view class="cart-box cart-box--idle" v-else>
					<view class="idle-copy">
						<text class="idle-title">{{ bottomBarText }}</text>
						<text class="idle-desc">{{ bottomBarHint }}</text>
					</view>
					<text class="idle-arrow">⌃</text>
				</view>
				<!-- 购物车栏 end -->
			</view>
			<!-- 商品详情模态框 begin -->
			<modal :show="goodDetailModalVisible" class="good-detail-modal" color="#171717" width="92%" custom
				padding="0rpx" radius="24rpx">
				<view class="cover">
					<view class="btn-group">
						<image src="/static/images/menu/close.png" @tap="closeGoodDetailModal"></image>
					</view>
				</view>
				<scroll-view class="detail" scroll-y>
					<view v-if="good.image" class="image">
						<image :src="good.image"></image>
					</view>

					<view class="wrapper">
						<view class="basic">
							<view class="name">{{ good.storeName }}</view>
							<view class="tips flex justify-between">{{ good.storeInfo }} <text class="score-text">可获积分:10</text></view>
						</view>
						<view class="properties">
							<view class="property" v-for="(item, index) in good.productAttr" :key="index">
								<view class="title">
									<text class="name">{{ item.attrName }}</text>
								</view>
								<view class="values">
									<view class="value" v-for="(value, key) in item.attrValueArr" :key="key"
										:class="{'default': value == newValue[index]}"
										@tap="changePropertyDefault(index, key,false)">
										{{ value }}
									</view>
								</view>
							</view>
						</view>
					</view>
				</scroll-view>
				<view class="action">
					<view class="left">
						<view class="price">￥{{ good.price }}</view>
						<view class="props">
							{{ good.valueStr }}
						</view>
					</view>
					<view class="btn-group">
						<text class="stock-text">库存：{{good.stock}}</text>
						<button type="default" plain class="btn" size="mini" hover-class="none"
							@tap="handlePropertyReduce">
							<view class="iconfont iconsami-select"></view>
						</button>
						<view class="number">{{ good.number }}</view>
						<button type="primary" class="btn" size="min" hover-class="none" @tap="handlePropertyAdd">
							<view class="iconfont iconadd-select"></view>
						</button>
					</view>
				</view>
				<view class="add-to-cart-btn" @tap="handleAddToCartInModal">
					<view>加入购物车</view>
				</view>
			</modal>
			<!-- 商品详情模态框 end -->
			<!-- 购物车详情popup -->
			<uv-popup ref="popup" mode="bottom" class="cart-popup">
				<template #default>
				<view  class="cart-popup">
					 <view class="top">
					  <text @tap="handleCartClear">清空</text>
					 </view>
					 <scroll-view class="cart-list" scroll-y>
					  <view class="wrapper">
					   <view class="item" v-for="(item, index) in cart" :key="index">
						<view class="left">
						 <view class="name">{{ item.name }}</view>
						 <view class="props">{{ item.valueStr }}</view>
						</view>
						<view class="center">
						 <text>￥{{ item.price }}</text>
						</view>
						<view class="right">
						 <button type="default" plain size="mini" class="btn" hover-class="none"
						  @tap="handleCartItemReduce(index)">
						  <view class="iconfont iconsami-select"></view>
						 </button>
						 <view class="number">{{ item.number }}</view>
						 <button type="primary" class="btn" size="min" hover-class="none"
						  @tap="handleCartItemAdd(index)">
						  <view class="iconfont iconadd-select"></view>
						 </button>
						</view>
					   </view>
							
					  </view>
					 </scroll-view>
				 </view>
				</template>
			</uv-popup>
			   <!-- 购物车详情popup -->
			<uv-toast ref="uToast"></uv-toast>
		</view>
		<!--轻提示-->
		<view class="loading" v-else>
			<uv-loading-icon  color="#DA5650" size=40 mode="circle" ></uv-loading-icon>
			<button type="primary" style="z-index: 3001;position: absolute;top: 650rpx;" @click="init"
				v-if="!store.id">定位最近的门店</button>
		<!-- 	<uv-toast ref="uToast"></uv-toast> -->
		</view>
	</layout>
</template>

<script setup>
import {
  ref,
  computed,
  nextTick
} from 'vue'
import { useMainStore } from '@/store/store'
import { storeToRefs } from 'pinia'
import { onLoad,onShow ,onPullDownRefresh,onHide} from '@dcloudio/uni-app'
import { formatDateTime,kmUnit } from '@/utils/util'
import {
  shopNearby,
  menuGoods
} from '@/api/goods'
import {
  menuAds
} from '@/api/market'
const main = useMainStore()
const { orderType,address, store,location,isLogin } = storeToRefs(main)
const title = ref('点餐')
const text = ref('滚动通知')

const goods = ref([])
const ads = ref([])
const loading = ref(true) 
const currentCateId = ref(0)
const cateScrollTop = ref(0)
const menuScrollIntoView = ref('')
const cart = ref([])
const goodDetailModalVisible = ref(false)
const good= ref({})
const category = ref({})
const cartPopupVisible = ref(false)
const sizeCalcState = ref(false)
const newValue = ref([])
const shopAd = ref('')
const popup = ref()



const newkmUnit = computed(() => (param) =>{
  console.log('param:',param)
  return '10km'
})
const currentCateName = computed(() => {
	const currentCate = goods.value.find(item => item.id === currentCateId.value)
	return currentCate?.name || goods.value[0]?.name || '经典鸡尾酒'
})
const goodCartNum = computed(() => { //计算单个饮品添加到购物车的数量
	return (id) => cart.value.reduce((acc, cur) => {
		if (cur.id === id) {
			return acc += cur.number
		}
		return acc
	}, 0)
})
const menuCartNum = computed(() =>{
	return (id) => cart.value.reduce((acc, cur) => {
		if (cur.cate_id === id) {
			return acc += cur.number
		}
		return acc
	}, 0)
})
const getCartGoodsNumber = computed(() => { //计算购物车总数
	return cart.value.reduce((acc, cur) => acc + cur.number, 0)
})
const getCartGoodsPrice = computed(() =>{ //计算购物车总价
	let price = cart.value.reduce((acc, cur) => acc + cur.number * cur.price, 0);
	return parseFloat(price).toFixed(2);
})
const disabledPay = computed(() => { //是否达到起送价
	const currentPrice = parseFloat(getCartGoodsPrice.value || 0)
	const minPrice = parseFloat(store.value.min_price || 0)
	return orderType.value == 'takeout' && currentPrice < minPrice
})
const spread = computed(() => { //差多少元起送
	if (orderType.value != 'takeout') return
	const currentPrice = parseFloat(getCartGoodsPrice.value || 0)
	const minPrice = parseFloat(store.value.min_price || 0)
	return parseFloat(Math.max(minPrice - currentPrice, 0).toFixed(2))
})
const bottomBarText = computed(() => {
	if (!store.value?.id) return '正在定位附近门店'
	if (store.value.status != 1) return '门店休息中'
	return orderType.value == 'takeout' ? '添加商品后可外卖结算' : '添加商品后可堂食下单'
})
const bottomBarHint = computed(() => {
	if (store.value?.status != 1) return store.value?.notice || '营业时间以门店公告为准'
	if (orderType.value == 'takeout' && store.value?.min_price) return `${store.value.min_price}元起送`
	return currentCateName.value
})

// 监听自定义事件
uni.$on('refreshMenu', () => {
	// 在这里执行onLoad逻辑
	console.log('refreshMenu1:',store.value.id)
	init()
})

onPullDownRefresh(() => {
	init()
})
onLoad(() => {
	init();
	refreshCart()
})
onHide(() => {
	// 重新进入要重新计算页面高度，否则有问题
	sizeCalcState.value = false;
})
onShow(() => {
	//init();
	refreshCart()
	shopAd.value = uni.getStorageSync('shopAd')
})

const in_array = (search, array) => {
	for (var i in array) {
		if (array[i] == search) {
			return true;
		}
	}
	return false;
}
const selectShop = () => {
	uni.navigateTo({
		url: '/pages/components/pages/shop/shop'
	})
}
const uToast = ref()
const  init = async() => { //页面初始化
	loading.value = true;

	let error = {},
		result = location.value
	console.log('result:',result)
	if (!location.value.hasOwnProperty('latitude')) {
		  console.log('result1:',location.value)
		  uni.getLocation(({
			 type: 'wgs84',
			 success: function (res) {
			   console.log('location1:',res)
		
				result = {
					latitude: res.latitude,
					longitude: res.longitude
				};
				getShopList(result)
			 },
			 fail: function (res) {
			  
			   uni.showToast({
			     title: '获取位置失败，请检查是否开启相关权限',
			     duration: 2000,
			     icon: 'error'
			   });
			   // 默认地为你为北京地址
			   result = {
			   	latitude: 39.919990,
			   	longitude: 116.456270
			   };
			   getShopList(result)
			 },
			 complete: function (res) {
			 }
		}));
		 if(!result.hasOwnProperty('latitude')){
			result = {
				latitude: 39.919990,
				longitude: 116.456270
			};
			getShopList(result)
		 }
		return
	}
	
	getShopList(result)
   
	
}
const getShopList = async(res) => {
	 console.log('location9:',res)
	if (res) {
		main.SET_LOCATION(res);
	
		let shop_id = 0;
		if (store.value.id) {
			shop_id = store.value.id;
		}
	
		let shop = await shopNearby({
			lat: res.latitude,
			lng: res.longitude,
			shop_id: shop_id,
			kw: ''
		});
		if (shop) {
			//广告图
			getAds(shop.id);
	
			shop.notice = shop.status == 1 ? shop.notice : '店铺营业时间为:' + formatDateTime(shop.startTime,'hh:mm')+' - '+formatDateTime(shop.endTime,'hh:mm') +
			'，不在营业时间内无法下单';
			// 设置店铺信息
			main.SET_STORE(shop);
			let mygoods = await menuGoods({
				shopId: shop.id
			});
			if (mygoods) {
				goods.value = mygoods;
				if (mygoods.length > 0) {
					currentCateId.value = mygoods[0].id;
					menuScrollIntoView.value = `menu-${mygoods[0].id}`;
				}
				refreshCart();
			}
			console.log('goods:',mygoods)
			console.log('goods:',goods.value)
			loading.value = false;
			uni.stopPullDownRefresh();
		}
	}
}
const refreshCart = () =>{
	if (goods.value && goods.value.length > 0) {
		let newGoods = goods.value;
		cart.value = [];
		let newCart = uni.getStorageSync('cart') || [];
		let tmpCart = [];
		if (newCart) {
			for (let i in newCart) {
				for (let ii in newGoods) {
					for (let iii in newGoods[ii].goodsList) {
						if (newCart[i].id == newGoods[ii].goodsList[iii].id) {
							tmpCart.push(newCart[i]);
						}
					}
				}
			}
			cart.value = tmpCart;
			cartPopupVisible.value = false;
		}
	}
}
const  getAds = async(shop_id) =>{
	let data = await menuAds({
		shop_id: shop_id ? shop_id : 0
	});
	if (data) {
		ads.value = data;
	}
}
const takout = (force = false) => {
	if (orderType.value == 'takeout' && force == false) return
	main.SET_ORDER_TYPE('takeout');

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} 

}
const takein = (force = false) => {
	if (orderType.value == 'takein' && force == false) return
	main.SET_ORDER_TYPE('takein');

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} 

}
const handleMenuTap = (id) => { //点击菜单项事件
	if (!sizeCalcState.value) {
		calcSize()
	}

	currentCateId.value = id
	nextTick(() => cateScrollTop.value = goods.value.find(item => item.id == id).top)
}
const handleGoodsScroll = ({ detail }) => { //商品列表滚动事件
	if (!sizeCalcState.value) {
		calcSize()
	}
	console.log('scrollTop:',detail)
	const {
		scrollTop
	} = detail
	let tabs = goods.value.filter(item => item.top <= scrollTop).reverse()
	if (tabs.length > 0) {
		currentCateId.value = tabs[0].id
	}
}
const calcSize = () => {
	let h = 10
	let view = uni.createSelectorQuery().select('#ads')
	if (view) {
		view.fields({
			size: true
		}, data => {
			if (data) {
				h += Math.floor(data.height)
			}
		}).exec()
	}
	goods.value.forEach(item => {
		let view = uni.createSelectorQuery().select(`#cate-${item.id}`)
		view.fields({
			size: true
		}, data => {
			console.log('h3:',h)
			item.top = h
			h += data.height
			item.bottom = h
		}).exec()
	})
	sizeCalcState.value = true
}
const handleAddToCart = (cate, newGood, num) =>{ //添加到购物车
	const index = cart.value.findIndex(item => {
		if (newGood) {
			return (item.id === newGood.id) && (item.valueStr === good.value.valueStr)
		} else {
			return item.id === newGood.id
		}
	})
	if (index > -1) {
		cart.value[index].number += num
	} else {
		cart.value.push({
			id: newGood.id,
			cate_id: cate.id,
			name: newGood.storeName,
			price: newGood.price,
			number: num,
			image: newGood.image,
			valueStr: good.value.valueStr
		})
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const handleReduceFromCart = (item, good) => {
	const index = cart.value.findIndex(item => item.id === good.id)
	cart.value[index].number -= 1
	if (cart.value[index].number <= 0) {
		cart.value.splice(index, 1)
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const showGoodDetailModal = (item, newGood) => {
	good.value = JSON.parse(JSON.stringify({
		...newGood,
		number: 1
	}))
	category.value = JSON.parse(JSON.stringify(item))
	goodDetailModalVisible.value = true;
	console.log('goodDetailModalVisible:',goodDetailModalVisible.value)
	changePropertyDefault(0, 0,true);
}
const closeGoodDetailModal = () => { //关闭饮品详情模态框
	goodDetailModalVisible.value = false
    category.value = {}
	good.value = {}
}
const changePropertyDefault = (index, key, isDefault) => { //改变默认属性值
	let valueStr = ''
	console.log('good:',good.value)
	if(isDefault){
		newValue.value = []
		for(let i = 0;i < good.value.productAttr.length;i++){
			newValue.value[i] = good.value.productAttr[i].attrValueArr[0]
		}

		//valueStr = newValue.value.join(',')

	}else{
		newValue.value[index] = good.value.productAttr[index].attrValueArr[key]
		//valueStr = newValue.value.join(',')
	}
	
	valueStr = newValue.value.join(',')
	let productValue = good.value.productValue[valueStr]
	if(!productValue) {
		let skukey = JSON.parse(JSON.stringify(newValue.value))
		skukey.sort((a, b) => a.localeCompare(b))
		//console.log('skukey:',skukey)
		valueStr = skukey.join(',')
		productValue = good.value.productValue[valueStr]
	}

	
	//let productValue = good.value.productValue[valueStr]
	good.value.number = 1;
	good.value.price = parseFloat(productValue.price).toFixed(2);
	good.value.stock = productValue.stock;
	good.value.image = productValue.image ? productValue.image : good.value.image;
	good.value.valueStr = valueStr

}
const handlePropertyAdd = () => {
	good.value.number += 1
}
const handlePropertyReduce = () => {
	if (good.value.number === 1) return
	good.value.number -= 1
}
const handleAddToCartInModal = () => {
	if (good.value.stock <= 0) {
		uToast.value.show({message:'商品库存不足',type: 'error'});
		return;
	}
	handleAddToCart(category.value, good.value, good.value.number)
	closeGoodDetailModal()
}
const openCartPopup = () => { //打开/关闭购物车列表popup
	popup.value.open()
}
const handleCartClear = () => { //清空购物车
	uni.showModal({
		title: '提示',
		content: '确定清空购物车么',
		success: ({
			confirm
		}) => {
			if (confirm) {
				popup.value.close()
				cart.value = []
				uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
			}
		}
	})
}
const handleCartItemAdd = (index) => {
	cart.value[index].number += 1
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const handleCartItemReduce = (index) => {
	if (cart.value[index].number === 1) {
		cart.value.splice(index, 1)
	} else {
		cart.value[index].number -= 1
	}
	if (!cart.value.length) {
		cartPopupVisible.value = false
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const toPay = () => {

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} else {
		if (store.value.status == 0) {
			uToast.value.show({message:'不在店铺营业时间内',type: 'error'});
			return;
		}
		if(orderType.value == 'takeout' && store.value.distance <= 0){
			uToast.value.show({message:'本店不支持外卖',type: 'error'});
			return;
		}
		// 判断当前是否在配送范围内
		if (orderType.value == 'takeout' && store.value.distance < store.value.far) {
			uToast.value.show({message:'选中的地址不在配送范围',type: 'error'});
			return;
		}

		uni.showLoading({
			title: '加载中'
		})
		uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))

		uni.navigateTo({
			url: '/pages/components/pages/pay/pay'
		})
	}

	uni.hideLoading()
}

</script>

<style lang="scss" scoped>

	/* #ifdef H5 */
	page {
		height: auto;
		min-height: 100%;
	}
	/* #endif */
	
	.container {
		overflow: hidden;
		position: relative;
		background: #0f0f0f;
		color: #f5f1e8;
	}
	
	.loading {
		width: 100%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #0f0f0f;
	
		image {
			width: 260rpx;
			height: 260rpx;
			position: relative;
			margin-top: -200rpx;
			/* #ifdef h5 */
			margin-top: 0;
			/* #endif */
		}
	}
	
	
	.main {
		width: 100%;
		height: 100%;
		position: relative;
	}
	
	.nav {
		width: 100%;
		height: 170rpx;
		display: flex;
		flex-direction: column;
	
		.header {
			width: 100%;
			display: flex;
			flex-direction: column;
			padding: 22rpx 24rpx 18rpx;
			background-color: #111111;
			height: 170rpx;
			border-bottom: 1px solid rgba(255, 255, 255, 0.06);

			.header-main {
				display: flex;
				align-items: center;
				justify-content: space-between;
			}

			.header-sub {
				display: flex;
				align-items: center;
				justify-content: space-between;
				margin-top: 18rpx;
			}

			.header-notice {
				flex: 1;
				color: #867d74;
				font-size: 22rpx;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}

			.header-more {
				margin-left: 16rpx;
				color: #c4baaf;
				font-size: 22rpx;
			}
	
			.left {
				flex: 1;
				display: flex;
				flex-direction: column;
	
				.store-name {
					display: flex;
					justify-content: flex-start;
					align-items: center;
					font-size: 36rpx;
					font-weight: 700;
					color: #f7f0e6;
					margin-bottom: 12rpx;
					.small {
						font-size: 22rpx;
						color: #857d74;
					}
				}
	
				.store-location {
					display: flex;
					justify-content: flex-start;
					align-items: center;
					color: #a69d93;
					font-size: 22rpx;
					gap: 8rpx;
	
					.location-dot {
						width: 12rpx;
						height: 12rpx;
						border-radius: 50%;
						background: #d2a46b;
						box-shadow: 0 0 0 4rpx rgba(210, 164, 107, 0.14);
					}
				}
			}
	
			.right {
				background-color: #1e1e1e;
				border: 1px solid rgba(255, 255, 255, 0.08);
				border-radius: 999rpx;
				display: flex;
				align-items: center;
				font-size: 22rpx;
				padding: 6rpx;
				color: #a49b91;
	
				.dinein,
				.takeout {
					position: relative;
					display: flex;
					align-items: center;
					justify-content: center;
					min-width: 86rpx;
					padding: 12rpx 18rpx;
					&.active {
						color: #111111;
						background-color: #f4efe7;
						border-radius: 999rpx;
						font-weight: 700;
					}
				}
	
				.takeout {
					margin-left: 6rpx;
				}
			}
		}

	}
	
	.content {
		width: 100%;
		height: calc(100vh - 320rpx);
		/* #ifdef H5 */
		height: calc(100vh - 320rpx - 110rpx);
		/* #endif */
		display: flex;
		background: #0f0f0f;
	
		.menus {
			width: 154rpx;
			height: 100%;
			overflow: hidden;
			background-color: #0b0b0b;
	
			.wrapper {
				width: 100%;
				height: 100%;
				padding: 16rpx 12rpx 180rpx;
	
				.menu {
					display: flex;
					align-items: center;
					justify-content: flex-start;
					padding: 22rpx 16rpx;
					margin-bottom: 10rpx;
					font-size: 24rpx;
					line-height: 1.35;
					color: #7f776f;
					position: relative;
					border-radius: 18rpx;
	
					&.current {
						background-color: #f5f1e8;
						color: #151515;
						font-weight: 700;
					}
	
					.dot {
						position: absolute;
						min-width: 30rpx;
						height: 30rpx;
						line-height: 30rpx;
						font-size: 18rpx;
						background: #d39d63;
						color: #111111;
						top: 10rpx;
						right: 10rpx;
						border-radius: 100%;
						text-align: center;
						padding: 0 6rpx;
						font-weight: 700;
					}
				}
			}
		}
	
		.goods {
			flex: 1;
			height: 100%;
			overflow: hidden;
			background-color: #111111;
	
			.wrapper {
				width: 100%;
				height: 100%;
				padding: 18rpx 18rpx 180rpx;
			}

			.goods-toolbar {
				display: flex;
				align-items: center;
				gap: 16rpx;
				margin-bottom: 22rpx;
			}

			.search-shell {
				flex: 1;
				height: 58rpx;
				display: flex;
				align-items: center;
				padding: 0 18rpx;
				border-radius: 999rpx;
				background: #161616;
				color: #6f685f;
				font-size: 22rpx;
			}

			.search-shell-icon {
				margin-right: 10rpx;
				font-size: 24rpx;
			}

			.toolbar-cate {
				max-width: 180rpx;
				padding: 12rpx 18rpx;
				border-radius: 999rpx;
				background: #171717;
				color: #d9d1c7;
				font-size: 22rpx;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
				border: 1px solid rgba(255, 255, 255, 0.08);
			}
	
			.list {
				width: 100%;
				font-size: $font-size-base;
	
				.category {
					width: 100%;
	
					.title {
						padding: 10rpx 2rpx 18rpx;
						display: flex;
						align-items: center;
						color: #cbb393;
						font-size: 24rpx;
						font-weight: 600;
	
						.icon {
							width: 28rpx;
							height: 28rpx;
							margin-left: 10rpx;
						}
					}
				}
				.category:last-child {
					margin-bottom: 180rpx;
				}
	
				.items {
					display: flex;
					flex-direction: column;
	
					.good {
						display: flex;
						align-items: stretch;
						padding: 18rpx;
						margin-bottom: 18rpx;
						border-radius: 22rpx;
						background: #191919;
						border: 1px solid rgba(255, 255, 255, 0.04);
						box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.22);
						.image {
							width: 136rpx;
							height: 136rpx;
							margin-right: 20rpx;
							border-radius: 14rpx;
							background: #252525;
							flex-shrink: 0;
						}
	
						.right {
							flex: 1;
							min-height: 136rpx;
							overflow: hidden;
							display: flex;
							flex-direction: column;
							align-items: flex-start;
							justify-content: space-between;
							padding-right: 4rpx;
	
							.name {
								font-size: 30rpx;
								font-weight: 700;
								color: #f5f0e7;
								margin-bottom: 8rpx;
								width: 100%;
								overflow: hidden;
								text-overflow: ellipsis;
								white-space: nowrap;
							}
	
							.tips {
								width: 100%;
								font-size: 22rpx;
								line-height: 1.45;
								color: #8f867d;
								margin-bottom: 12rpx;
								display: -webkit-box;
								-webkit-line-clamp: 2;
								-webkit-box-orient: vertical;
								overflow: hidden;
							}
	
							.price_and_action {
								width: 100%;
								display: flex;
								justify-content: space-between;
								align-items: center;
	
								.price {
									font-size: 34rpx;
									font-weight: 700;
									color: #f5f0e7;
								}
	
								.btn-group {
									display: flex;
									justify-content: space-between;
									align-items: center;
									position: relative;
	
									.btn {
										padding: 0 18rpx;
										box-sizing: border-box;
										font-size: 20rpx;
										height: 48rpx;
										line-height: 48rpx;
										background: #272727;
										color: #f3ede4;
										border: 1px solid rgba(255, 255, 255, 0.08);
	
										&.property_btn {
											border-radius: 999rpx;
										}
	
										&.add_btn,
										&.reduce_btn {
											padding: 0;
											width: 44rpx;
											border-radius: 44rpx;
										}
									}
	
									.dot {
										position: absolute;
										background-color: #d3a268;
										color: #111111;
										font-size: 18rpx;
										min-width: 32rpx;
										height: 32rpx;
										line-height: 32rpx;
										text-align: center;
										border-radius: 100%;
										right: -10rpx;
										top: -10rpx;
										padding: 0 6rpx;
										font-weight: 700;
									}
	
									.number {
										width: 44rpx;
										height: 44rpx;
										line-height: 44rpx;
										text-align: center;
									}
								}

								.soldout {
									font-size: 22rpx;
									color: #8e8477;
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	.good-detail-modal {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		background: #171717;
		color: #f5f1e8;
		border-radius: 24rpx;
	
		.cover {
			height: 28rpx;
			display: flex;
			justify-content: center;
			align-items: center;
	
			.btn-group {
				position: absolute;
				right: 16rpx;
				top: 10rpx;
				display: flex;
				align-items: center;
				justify-content: space-around;
				z-index: 210;
				 
				image {
					width: 72rpx;
					height: 72rpx;
				}
			}
		}
	
		.detail {
			width: 100%;
			min-height: 1vh;
			max-height: calc(90vh - 320rpx - 80rpx - 120rpx);
			position: relative;
	
			.image {
				padding-top: 36rpx;
				display: flex;
				justify-content: center;
				align-items: center;
				image {
					width: 320rpx;
					height: 320rpx;
					border-radius: 24rpx;
					background: #202020;
				}
			}
			.wrapper {
				width: 100%;
				height: 100%;
				overflow: hidden;
	
				.basic {
					padding: 24rpx 28rpx 30rpx;
					display: flex;
					flex-direction: column;
					.name {
						font-size: 34rpx;
						color: #f5f1e8;
						font-weight: 700;
						margin-bottom: 12rpx;
					}
					.tips {
						font-size: 24rpx;
						line-height: 1.5;
						color: #8f867d;

						.score-text {
							margin-left: 16rpx;
							color: #d3a268;
							flex-shrink: 0;
						}
					}
				}
	
				.properties {
					width: 100%;
					border-top: 1px solid rgba(255, 255, 255, 0.08);
					padding: 24rpx 28rpx 8rpx;
					display: flex;
					flex-direction: column;
	
					.property {
						width: 100%;
						display: flex;
						flex-direction: column;
						margin-bottom: 30rpx;
	
						.title {
							width: 100%;
							display: flex;
							justify-content: flex-start;
							align-items: center;
							margin-bottom: 18rpx;
	
							.name {
								font-size: 26rpx;
								color: #f5f1e8;
								margin-right: 20rpx;
								font-weight: 600;
							}
	
							.desc {
								flex: 1;
								font-size: 24rpx;
								color: #d3a268;
								overflow: hidden;
								text-overflow: ellipsis;
								white-space: nowrap;
							}
						}
	
						.values {
							width: 100%;
							display: flex;
							flex-wrap: wrap;
	
							.value {
								border-radius: 999rpx;
								background-color: #232323;
								padding: 14rpx 28rpx;
								font-size: 26rpx;
								color: #c4bbaf;
								margin-right: 16rpx;
								margin-bottom: 16rpx;
								border: 1px solid rgba(255, 255, 255, 0.08);
	
								&.default {
									background-color: #f5efe6;
									color: #111111;
									border-color: #f5efe6;
									font-weight: 700;
								}
							}
						}
					}
				}
			}
		}
	
		.action {
			display: flex;
			align-items: center;
			justify-content: space-between;
			background-color: #111111;
			height: 132rpx;
			padding: 0 28rpx;
			border-top: 1px solid rgba(255, 255, 255, 0.06);
	
			.left {
				flex: 1;
				display: flex;
				flex-direction: column;
				justify-content: center;
				margin-right: 20rpx;
				overflow: hidden;
	
				.price {
					font-size: 38rpx;
					color: #f5f1e8;
					font-weight: 700;
				}
	
				.props {
					color: #8f867d;
					font-size: 24rpx;
					width: 100%;
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
				}
			}
			.btn-group {
				display: flex;
				align-items: center;
				justify-content: space-around;

				.stock-text {
					margin-right: 18rpx;
					color: #8f867d;
					font-size: 22rpx;
				}
	
				.number {
					font-size: 28rpx;
					width: 44rpx;
					height: 44rpx;
					line-height: 44rpx;
					text-align: center;
					color: #f5f1e8;
				}
	
				.btn {
					padding: 0;
					font-size: 28rpx;
					width: 44rpx;
					height: 44rpx;
					line-height: 44rpx;
					border-radius: 100%;
					background: #242424;
					color: #f5f1e8;
					border: 1px solid rgba(255, 255, 255, 0.08);

					&::after {
						border: none;
					}
				}
			}
		}
	
		.add-to-cart-btn {
			display: flex;
			justify-content: center;
			align-items: center;
			background-color: #f5efe6;
			color: #111111;
			font-size: 30rpx;
			font-weight: 700;
			height: 92rpx;
			border-radius: 0 0 24rpx 24rpx;
		}
	}
	
	.cart-box {
		position: fixed;
		bottom: 30rpx;
		/* #ifdef H5 */
		bottom:var(--window-bottom);
		//bottom: 100rpx;
		/* #endif */
		left: 30rpx;
		right: 30rpx;
		height: 96rpx;
		border-radius: 48rpx;
		box-shadow: 0 18rpx 40rpx rgba(0, 0, 0, 0.38);
		background-color: #141414;
		border: 1px solid rgba(255, 255, 255, 0.08);
		display: flex;
		align-items: center;
		justify-content: space-between;
		z-index: 9999;
	
		.cart-img {
			width: 96rpx;
			height: 96rpx;
			position: relative;
			margin-top: -48rpx;
		}
	
		.pay-btn {
			height: 100%;
			padding: 0 30rpx;
			color: #111111;
			border-radius: 0 50rpx 50rpx 0;
			display: flex;
			align-items: center;
			font-size: $font-size-base;
			background: #f5efe6;

			&::after {
				border: none;
			}

			&[disabled] {
				background: #cabaa6 !important;
				color: #111111 !important;
				opacity: 0.75;
			}
		}
	
		.mark {
			padding-left: 46rpx;
			margin-right: 30rpx;
			position: relative;
	
			.tag {
				background-color: #d3a268;
				color: #111111;
				display: flex;
				justify-content: center;
				align-items: center;
				font-size: $font-size-sm;
				position: absolute;
				right: -10rpx;
				top: -50rpx;
				border-radius: 100%;
				padding: 4rpx;
				width: 40rpx;
				height: 40rpx;
				opacity: 0.9;
			}
		}
	
		.price {
			flex: 1;
			color: #f5efe6;
			font-size: 30rpx;
			font-weight: 700;
		}
	}

	.cart-box--idle {
		padding: 0 32rpx;
		background: rgba(20, 20, 20, 0.96);

		.idle-copy {
			flex: 1;
			display: flex;
			flex-direction: column;
			overflow: hidden;
		}

		.idle-title {
			font-size: 28rpx;
			font-weight: 700;
			color: #f5efe6;
			margin-bottom: 6rpx;
		}

		.idle-desc {
			font-size: 22rpx;
			color: #8f867d;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		.idle-arrow {
			color: #b9ae9f;
			font-size: 30rpx;
			padding-left: 24rpx;
		}
	}
	
	.cart-popup {
		background: transparent;

		.top {
			background-color: #161616;
			color: #b6ac9e;
			padding: 22rpx 30rpx;
			font-size: 24rpx;
			text-align: right;
			border-radius: 28rpx 28rpx 0 0;
			border-bottom: 1px solid rgba(255, 255, 255, 0.06);
		}
		.cart-list {
			background-color: #161616;
			width: 100%;
			overflow: hidden;
			min-height: 1vh;
			max-height: 60vh;
	
			.wrapper {
				height: 100%;
				display: flex;
				flex-direction: column;
				padding: 0 30rpx;
				margin-bottom: 156rpx;
	
				.item {
					display: flex;
					justify-content: space-between;
					align-items: center;
					padding: 30rpx 0;
					position: relative;
	
					&::after {
						content: ' ';
						position: absolute;
						bottom: 0;
						left: 0;
						width: 100%;
						background-color: rgba(255, 255, 255, 0.06);
						height: 1px;
					}
	
					.left {
						flex: 1;
						display: flex;
						flex-direction: column;
						overflow: hidden;
						margin-right: 30rpx;
	
						.name {
							font-size: 26rpx;
							color: #f5efe6;
							margin-bottom: 8rpx;
						}
						.props {
							color: #8f867d;
							font-size: 24rpx;
							overflow: hidden;
							text-overflow: ellipsis;
							white-space: nowrap;
						}
					}
	
					.center {
						margin-right: 120rpx;
						font-size: 28rpx;
						color: #f5efe6;
						font-weight: 700;
					}
	
					.right {
						display: flex;
						align-items: center;
						justify-content: space-between;
	
						.btn {
							width: 46rpx;
							height: 46rpx;
							border-radius: 100%;
							padding: 0;
							text-align: center;
							line-height: 46rpx;
							background: #242424;
							color: #f5f1e8;
							border: 1px solid rgba(255, 255, 255, 0.08);

							&::after {
								border: none;
							}
						}
	
						.number {
							font-size: 28rpx;
							width: 46rpx;
							height: 46rpx;
							text-align: center;
							line-height: 46rpx;
							color: #f5efe6;
						}
					}
				}
			}
		}
	}
	
	.backgroud-grey {
		background: #121212 !important;
		opacity: 0.58;
	}
</style>
