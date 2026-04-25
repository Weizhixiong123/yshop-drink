<script setup>
import { onHide,onLaunch,onShow } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { useMainStore } from '@/store/store'
const main = useMainStore()

onLaunch(() => {
	console.log('App Launch')
})

onShow(() => {
    console.log('App Show')
})

onHide(() => {
   console.log('App Hide')
})

</script>

<style lang="scss">
@import '~@/static/style/app.scss';
//@import 'static/iconfont/iconfont.scss';
//@import url('./static/style/style.less');
@import 'static/style/yshop.css';

// /*每个页面公共css */
// page {
//   background-color: #f5f5f5;
// }
</style>
