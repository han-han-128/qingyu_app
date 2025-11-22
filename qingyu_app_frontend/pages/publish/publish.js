// pages/publish/publish.js
const app = getApp();
Page({
  data: {
    title: '',
    content: '',
    images: [],
    maxImages: 9, // 最多上传9张图片
    authorId: '' // 将在登录后获取
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
    // 登录成功后更新authorId
    this.setData({
      authorId: app.getUserId() || ''
    });
    return true;
  },
  
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function() {
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

  // 标题输入
  onTitleInput: function(e) {
    this.setData({
      title: e.detail.value
    })
  },

  // 内容输入
  onContentInput: function(e) {
    this.setData({
      content: e.detail.value
    })
  },

  // 选择图片
  chooseImage: function() {
    const that = this;
    const remainingCount = that.data.maxImages - that.data.images.length;
    
    if (remainingCount <= 0) {
      wx.showToast({
        title: '最多只能上传9张图片',
        icon: 'none'
      });
      return;
    }

    wx.chooseImage({
      count: remainingCount,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePaths = res.tempFilePaths;
        that.setData({
          images: that.data.images.concat(tempFilePaths)
        })
      }
    })
  },

  // 删除图片
  deleteImage: function(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images;
    images.splice(index, 1);
    this.setData({
      images: images
    })
  },

  // 发布内容
  publishNews: function() {
    const { title, content, images, authorId } = this.data;
    const serverUrl = app.serverUrl;

    // 简单验证
    if (!title.trim()) {
      wx.showToast({
        title: '请输入标题',
        icon: 'none'
      });
      return;
    }

    if (!content.trim()) {
      wx.showToast({
        title: '请输入内容',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '发布中...',
    });

    // 前端只需要传递图片路径，不需要做特殊处理
    // 直接提交新闻内容和图片
    this.submitNews(title, content, authorId, images);
  },
  
  // 提交新闻内容
  submitNews: function(title, content, authorId, images) {
    // 直接使用8081端口，确保与服务器配置一致
    const serverUrl = 'http://localhost:8081';
    
    // 从app.js获取全局配置的服务器URL
    const appServerUrl = app.serverUrl;
    const finalServerUrl = appServerUrl || serverUrl;
    
    console.log('提交数据:', {title, content, authorId, images});
    console.log('请求URL:', finalServerUrl + '/news/publish');
    
    // 修复问题：后端期望authorId参数实际上是用户名而不是用户ID
    // 从app.userInfo中获取用户名，如果没有则使用authorId作为备选
    const username = app.userInfo && app.userInfo.username ? app.userInfo.username : (authorId || '1');
    
    // 处理图片上传 - wx.uploadFile一次只能上传一个文件
    if (images.length > 0) {
      // 修复问题：上传所有图片而不仅仅是第一张
      // 先上传第一张图片并提交基本信息
      this.uploadImageAndPublish(0, images, title, content, username, finalServerUrl);
    } else {
      // 如果没有图片，使用普通POST请求，必须使用表单格式
      wx.request({
        url: finalServerUrl + '/news/publish',
        method: 'POST',
        header: {
          'content-type': 'application/x-www-form-urlencoded'
        },
        data: {
          title: title,     // 必须的标题参数
          content: content, // 必须的内容参数
          authorId: username // 传递用户名而不是用户ID
        },
        success: (res) => {
          wx.hideLoading();
          console.log('发布结果:', res.data);
          
          if (res.statusCode === 200) {
            wx.showToast({
              title: '发布成功',
              icon: 'success'
            });
            // 发布成功后通知首页刷新数据
            app.notifyNewsPublished();
            // 返回首页
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } else {
            wx.showToast({
              title: res.data?.msg || '发布失败，请重试',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('发布请求失败:', err);
          wx.hideLoading();
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
        }
      });
    }
  },
  
  // 递归上传图片并发布新闻
  uploadImageAndPublish: function(index, images, title, content, authorId, serverUrl) {
    // 如果已经上传完所有图片，结束递归
    if (index >= images.length) {
      // 最后一张图片上传完成，显示成功消息
      wx.hideLoading();
      wx.showToast({
        title: '发布成功',
        icon: 'success'
      });
      // 发布成功后通知首页刷新数据
      app.notifyNewsPublished();
      // 返回首页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    // 上传当前图片
    wx.uploadFile({
      url: serverUrl + '/news/publish',
      filePath: images[index],
      name: 'files', // 与后端@RequestParam("files")对应
      formData: {
        title: title,
        content: content,
        authorId: authorId
      },
      success: (res) => {
        console.log('第' + (index + 1) + '张图片上传结果:', res.data);
        
        try {
          const data = JSON.parse(res.data);
          console.log('解析后的结果:', data);
          
          if (res.statusCode === 200) {
            // 第一张图片上传成功，继续上传下一张
            // 注意：这里只在上传第一张时提交完整信息，后续图片可以简化处理
            // 或者在实际应用中，先上传所有图片获取URL，然后一次性提交完整信息
            this.uploadImageAndPublish(index + 1, images, title, content, authorId, serverUrl);
          } else {
            wx.hideLoading();
            wx.showToast({
              title: data.msg || '发布失败，请重试',
              icon: 'none'
            });
          }
        } catch (e) {
          console.error('解析返回数据失败:', e);
          wx.hideLoading();
          wx.showToast({
            title: '返回数据格式错误',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('第' + (index + 1) + '张图片上传失败:', err);
        wx.hideLoading();
        wx.showToast({
          title: '图片上传失败: ' + (index + 1),
          icon: 'none'
        });
      }
    });
  },
});