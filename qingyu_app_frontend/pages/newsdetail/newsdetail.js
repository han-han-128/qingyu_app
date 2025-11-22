// pages/newsdetail/newsdetail.js
const app = getApp();
Page({
  /**
   * 页面的初始数据
   */
  data: {
    news: null, // 新闻详情数据
    newsId: ''
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
    // 检查用户登录状态，如果已登录则加载数据
    if (this.checkLoginStatus()) {
      var newsId = options.id;
      if (newsId) {
        this.setData({
          newsId: newsId
        });
        this.getNewsDetail(newsId);
      }
    }
  },
  
  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    // 每次显示页面都检查登录状态
    this.checkLoginStatus();
  },

  /**
  onLoad: function (options) {
    var newsId = options.id;
    if (newsId) {
      this.getNewsDetail(newsId);
    }
  },

  /**
   * 获取新闻详情
   */
  getNewsDetail: function(newsId) {
    console.log('获取新闻详情，ID:', newsId);
    var that = this;
    wx.showLoading({
      title: '加载中...',
    })
    wx.request({
      url: 'http://localhost:8081/news/' + newsId, // 后端API地址（端口修正为8081）
      method: 'GET',
      success: function(res) {
        console.log('新闻详情请求成功，响应数据:', res.data);
        wx.hideLoading();
        if (res.data.status === 200) {
          // 转换数据格式，将news和images合并到同一个对象
          var newsDetail = {
            ...res.data.data.news,
            images: res.data.data.images
          };
          console.log('合并后的新闻详情:', newsDetail);
          that.setData({
            news: newsDetail
          });
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          });
        }
      },
      fail: function(error) {
        console.log('新闻详情请求失败，错误信息:', error);
        wx.hideLoading();
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  }
});