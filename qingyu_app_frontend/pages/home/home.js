// pages/home/home.js
const app = getApp();

Page({
  /**
   * 页面的初始数据
   */
  data: {
    newsList: [], // 新闻列表数据
    loading: false, // 加载状态
    swiperList: [] // 轮播图数据
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
    // 先检查登录状态，如果已登录则加载数据
    if (this.checkLoginStatus()) {
      // 页面加载时请求新闻数据
      this.getNewsList();
      // 初始化轮播图数据
      this.initSwiperData();
    }
    
    // 订阅新闻发布事件，当有新内容发布时自动刷新数据
    app.eventBus.subscribe('newsPublished', this.handleNewsPublished.bind(this));
  },
  
  /**
   * 处理新闻发布事件
   */
  handleNewsPublished: function() {
    console.log('收到新内容发布通知，刷新新闻列表');
    this.getNewsList();
  },
  
  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
    // 页面卸载时取消订阅，避免内存泄漏
    app.eventBus.unsubscribe('newsPublished', this.handleNewsPublished.bind(this));
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
    if (this.checkLoginStatus()) {
      // 从其他页面返回时刷新数据，确保能看到最新的发布内容
      this.getNewsList();
    }
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
    // 执行下拉刷新时重新获取新闻列表
    this.getNewsList();
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  /**
   * 初始化轮播图数据
   */
  initSwiperData: function() {
    // 轮播图数据，用户可以将图片放在 images/banners 目录下
    const swiperData = [
      { id: 1, image: '../../images/banners/banner1.png' },
      { id: 2, image: '../../images/banners/banner2.png' },
      { id: 3, image: '../../images/banners/banner3.png' }
    ];
    this.setData({
      swiperList: swiperData
    });
  },

  /**
   * 轮播图点击事件
   */
  onSwiperItemTap: function(e) {
    const index = e.currentTarget.dataset.index;
    console.log('轮播图点击:', index, this.data.swiperList[index]);
    // 可以在这里添加轮播图点击跳转逻辑
  },

  onReachBottom: function () {
    
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {
    
  },
  
  // 跳转到发布页面
  goToPublish: function() {
    wx.navigateTo({
      url: '/pages/publish/publish'
    })
  },

  // 跳转到紧急上报页面
  goToEmergencyReport: function() {
    wx.navigateTo({
      url: '/pages/report/report'
    })
  },

  /**
   * 获取新闻列表
   */
  getNewsList: function() {
    var that = this;
    that.setData({ loading: true });
    // 显示加载提示
    wx.showLoading({
      title: '加载中...',
    })
    // 请求后端API
    wx.request({
      url: 'http://localhost:8081/news/list', // 后端API地址（端口修正为8081）
      method: 'GET',
      success: function(res) {
      // 隐藏加载提示
      wx.hideLoading();
      wx.stopPullDownRefresh(); // 停止下拉刷新动画
      that.setData({ loading: false });
      // 处理返回数据
      console.log('后端返回数据：', res.data);
      if (res.data.status === 200) {
          // 查看后端返回的原始数据
          console.log('后端原始返回数据：', res.data.data);
          // 检查后端返回数据的结构
          console.log('第一个新闻项数据：', res.data.data[0]);
          // 转换数据格式，处理旧格式（news字段嵌套）和新格式（直接返回属性）
          var formattedNews = res.data.data.map(item => {
            // 旧数据结构：{ news: {...}, images: [...] }
            // 新数据结构：{ ...news properties..., images: [...] }
            const newsProps = item.news || item;
            return {
              ...newsProps,
              images: Array.isArray(item.images || item.images) ? (item.images || item.images) : []
            };
          });
          console.log('所有新闻：', formattedNews);
          // 过滤审核通过的新闻
          var approvedNews = formattedNews.filter(item => item.status === 1);
          console.log('审核通过的新闻：', approvedNews);
          that.setData({
            newsList: approvedNews
          });
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          });
        }
      },
      fail: function(error) {
      // 隐藏加载提示
      wx.hideLoading();
      wx.stopPullDownRefresh(); // 停止下拉刷新动画
      that.setData({ loading: false });
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      });
      console.log('请求失败', error);
    }
    });
  },

  /**
   * 跳转到新闻详情页
   */
  goToNewsDetail: function(event) {
    var newsId = event.currentTarget.dataset.newsid;
    wx.navigateTo({
      url: '/pages/newsdetail/newsdetail?id=' + newsId
    });
  }
})