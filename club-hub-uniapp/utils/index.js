import cookie from '@/utils/cookie'
import { disconnectCustomerSocket, disconnectStaffSocket } from '@/utils/staffSocket'

export const handleLoginFailure = () => {
  disconnectStaffSocket()
  disconnectCustomerSocket()
  cookie.remove('accessToken')
  cookie.remove('userRole')
  cookie.remove('customerPhone')

  uni.redirectTo({
    url: '/pages/entry/entry',
  })
}
