// pages/my/my.js
const app = getApp()
Page({
  /**
   * 页面的初始数据
   */
  data: {
    userInfo: null, // 用户信息
    isLoggedIn: false // 登录状态
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    // 每次显示页面时，检查用户登录状态
    this.checkUserLoginStatus();
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {
    
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
    
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    // 下拉刷新时重新检查用户登录状态
    this.checkUserLoginStatus();
    wx.stopPullDownRefresh();
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {
    
  },

  /**
   * 检查用户登录状态
   */
  checkUserLoginStatus: function() {
    // 从全局获取用户信息
    const userInfo = app.userInfo || wx.getStorageSync('userInfo');
    
    if (userInfo) {
      // 创建用户信息的副本，避免直接修改原对象
      const userInfoCopy = { ...userInfo };
      
      // 尝试从多个可能的字段中提取用户ID
      const userId = userInfo.userId || userInfo.id || userInfo.openId || userInfo.userid || userInfo.uid || null;
      
      // 确保userInfo对象中有userId字段
      userInfoCopy.userId = userId;
      
      console.log('检查用户ID:', userId, '用户信息:', userInfoCopy);
      
      this.setData({
        userInfo: userInfoCopy,
        isLoggedIn: true
      });
    } else {
      this.setData({
        userInfo: null,
        isLoggedIn: false
      });
    }
  },

  /**
   * 退出登录
   */
  logout: function() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 使用全局方法清除用户信息
          app.clearUserInfo();
          
          // 更新页面状态
          this.setData({
            userInfo: null,
            isLoggedIn: false
          });
          
          // 显示退出成功提示
          wx.showToast({
            title: '已退出登录',
            icon: 'success',
            duration: 1500
          });
          
          // 退出后跳转到登录页面
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }, 1500);
        }
      }
    });
  },
  
  /**
   * 去登录
   */
  goLogin: function() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },
  
  /**
   * 我的发布
   */
  goMyPublish: function() {
    console.log('进入我的发布页面');
    // 检查用户是否登录
    const userInfo = wx.getStorageSync('userInfo') || getApp().globalData.userInfo;
    if (!userInfo) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    // 获取用户ID
    let userId = userInfo.userId || userInfo.id || userInfo.openId || userInfo.userid || userInfo.uid;
    if (!userId) {
      wx.showToast({
        title: '获取用户ID失败',
        icon: 'none'
      });
      return;
    }
    
    console.log('用户ID:', userId);
    
    // 跳转到新的发布历史页面，并传递userId参数
    wx.navigateTo({
      url: `/pages/mypublishlist/mypublishlist?userId=${userId}`
    });
  },
  
  /**
   * 我的收藏
   */
  goMyCollection: function() {
    if (!this.data.isLoggedIn) {
      this.goLogin();
      return;
    }
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },
  
  /**
   * 设置页面
   */
  goSettings: function() {
    if (!this.data.isLoggedIn) {
      this.goLogin();
      return;
    }
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },
  
  /**
   * 我的上报
   */
  goMyReport: function() {
    console.log('进入我的上报页面');
    // 检查用户是否登录
    const userInfo = wx.getStorageSync('userInfo') || getApp().globalData.userInfo;
    if (!userInfo) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    // 获取用户ID
    let userId = userInfo.userId || userInfo.id || userInfo.openId || userInfo.userid || userInfo.uid;
    if (!userId) {
      wx.showToast({
        title: '获取用户ID失败',
        icon: 'none'
      });
      return;
    }
    
    console.log('用户ID:', userId);
    
    // 跳转到我的上报页面，并传递userId参数
    wx.navigateTo({
      url: `/pages/myreportlist/myreportlist?userId=${userId}`
    });
  },
  
  /**
   * 我参与的公益活动
   */
  goMyActivities: function() {
    if (!this.data.isLoggedIn) {
      this.goLogin();
      return;
    }
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },
  
  /**
   * 联系客服
   */
  goCustomerService: function() {
    wx.showModal({
      title: '联系客服',
      content: '客服电话: 400-123-4567\n工作时间: 9:00-18:00',
      showCancel: false,
      confirmText: '确定'
    });
  }
})