// pages/map/map.js
const app = getApp()
Page({
  /**
   * 页面的初始数据
   */
  data: {
    
  },
  
  /**
   * 检查用户登录状态
   */
  checkLoginStatus: function() {
    if (!app.isLoggedIn()) {
      console.log('未登录，跳转到登录页');
      wx.redirectTo({
        url: '../login/login'
      });
      return false;
    }
    return true;
  },
  
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    // 检查用户登录状态
    this.checkLoginStatus();
  },
  
  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    // 每次显示页面都检查登录状态
    this.checkLoginStatus();
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
    
  }
})