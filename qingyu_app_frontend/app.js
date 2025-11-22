//app.js
App({

  serverUrl: "http://localhost:8081",
  userInfo: null,
  eventBus: {
    // 存储订阅者
    subscribers: {},
    
    // 订阅事件
    subscribe(eventName, callback) {
      if (!this.subscribers[eventName]) {
        this.subscribers[eventName] = [];
      }
      this.subscribers[eventName].push(callback);
    },
    
    // 发布事件
    publish(eventName, data) {
      if (this.subscribers[eventName]) {
        this.subscribers[eventName].forEach(callback => {
          callback(data);
        });
      }
    },
    
    // 取消订阅
    unsubscribe(eventName, callback) {
      if (this.subscribers[eventName]) {
        this.subscribers[eventName] = this.subscribers[eventName].filter(cb => cb !== callback);
      }
    }
  },
  
  onLaunch: function () {
    //调用API从本地缓存中获取数据
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
    
    // 应用启动时，从本地缓存恢复用户登录状态
    this.restoreUserInfo();
  },
  
  /**
   * 从本地缓存恢复用户信息
   */
  restoreUserInfo: function() {
    try {
      const userInfo = wx.getStorageSync('userInfo');
      if (userInfo) {
        this.userInfo = userInfo;
        console.log('应用启动：已恢复用户登录状态', userInfo);
      }
    } catch (e) {
      console.error('恢复用户信息失败:', e);
    }
  },
  
  /**
   * 更新并保存用户信息
   */
  updateUserInfo: function(userInfo) {
    try {
      this.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
      console.log('用户信息已更新:', userInfo);
      return true;
    } catch (e) {
      console.error('保存用户信息失败:', e);
      return false;
    }
  },
  
  /**
   * 清除用户信息（退出登录）
   */
  clearUserInfo: function() {
    try {
      this.userInfo = null;
      wx.removeStorageSync('userInfo');
      console.log('用户信息已清除');
      return true;
    } catch (e) {
      console.error('清除用户信息失败:', e);
      return false;
    }
  },
  
  /**
   * 检查用户是否已登录
   */
  isLoggedIn: function() {
    return this.userInfo !== null;
  },
  
  /**
   * 获取用户ID
   */
  getUserId: function() {
    return this.userInfo && this.userInfo.userId ? this.userInfo.userId : null;
  },
  
  /**
   * 全局页面访问控制
   * 在每个页面显示前检查登录状态
   */
  onShow: function() {
    // 监听页面切换，检查登录状态
    const pages = getCurrentPages();
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      const pagePath = currentPage.route;
      
      // 不需要登录的页面白名单
      const whiteList = ['pages/login/login', 'pages/register/register'];
      
      // 如果当前页面不在白名单中且用户未登录，则重定向到登录页
      if (!whiteList.includes(pagePath) && !this.isLoggedIn()) {
        console.log('用户未登录，重定向到登录页');
        // 避免循环重定向
        if (pagePath !== 'pages/login/login') {
          wx.redirectTo({
            url: '/pages/login/login'
          });
        }
      }
    }
  },
  
  // 保留原有getUserInfo方法以兼容旧代码
  getUserInfo: function(cb){
    var that = this
    if(this.userInfo){
      typeof cb == "function" && cb(this.userInfo)
    }else{
      //调用登录接口
      wx.login({
        success: function () {
          wx.getUserInfo({
            success: function (res) {
              that.userInfo = res.userInfo
              that.updateUserInfo(res.userInfo);
              typeof cb == "function" && cb(res.userInfo)
            }
          })
        }
      })
    }
  },
  
  // 发布新内容后通知首页刷新
  notifyNewsPublished: function() {
    this.eventBus.publish('newsPublished');
  }
})