// pages/identify/identify.js
const app = getApp();
Page({
  /**
   * 页面的初始数据
   */
  data: {
    imageUrl: '',
    recognizedImageUrl: ''
  },// 存储选择或拍摄的图片路径
  
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
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
    
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    // 每次显示页面都检查登录状态
    this.checkLoginStatus();
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
   * 选择图片（从相册）
   */
  showImagePicker: function () {
    wx.showActionSheet({
      itemList: ['从相册选择', '拍摄照片'],
      success: (res) => {
        let sourceType = res.tapIndex === 0 ? ['album'] : ['camera'];
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          sourceType: sourceType,
          success: (res) => {
            this.setData({
              imageUrl: res.tempFiles[0].tempFilePath
            });
          },
          fail: (err) => {
            console.error('选择图片失败:', err);
          }
        });
      },
      fail: (err) => {
        console.error('显示选择菜单失败:', err);
      }
    });
  },
  startIdentify: function () {
    // TODO: 实现识别功能，调用后端API
    if (!this.data.imageUrl) {
      wx.showToast({
        title: '请先选择或拍摄图片',
        icon: 'none'
      });
      return;
    }
    wx.showLoading({
      title: '识别中...'
    });
    // 调用后端识别API
    const app = getApp();
    const serverUrl = app.serverUrl;
    const imagePath = this.data.imageUrl;
    wx.uploadFile({
      url: serverUrl + '/detection/detect', // 后端识别接口
      filePath: imagePath,
      name: 'image', // 与后端接口参数名一致
      formData: {
        userId: app.userInfo ? (app.userInfo.username || app.userInfo.userId || 'test_user') : 'test_user' // 使用用户名或ID
      },
      // 识别成功后的处理
      success: (res) => {
        wx.hideLoading();
        if (res.statusCode === 200) {
          const result = JSON.parse(res.data);
          if (result.status === 200) {
            wx.showToast({
              title: '识别成功',
              icon: 'success'
            });
            this.setData({
              recognizedImageUrl: result.data.detectedImageUrl
            });
          } else {
            wx.showToast({
              title: '识别失败：' + result.msg,
              icon: 'none'
            });
          }
        } else {
          wx.showToast({
            title: '请求失败：' + res.statusCode,
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('调用后端识别API失败:', err);
        wx.showToast({
          title: '请求失败，请检查网络连接',
          icon: 'none'
        });
      }
    });
  },
  
  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {
    
  }
})