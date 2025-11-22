// pages/publish/publish.js
const app = getApp();

Page({
  data: {
    title: '',
    content: '',
    images: [],
    maxImages: 9, // 最多上传9张图片
    authorId: 'id1' // 这里应该从登录状态中获取，暂时硬编码
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

    // 创建上传任务
    const uploadTasks = images.map((filePath) => {
      return new Promise((resolve, reject) => {
        wx.uploadFile({
          url: serverUrl + '/news/publish',
          filePath: filePath,
          name: 'files',
          formData: {
            title: title,
            content: content,
            authorId: authorId
          },
          success(res) {
            resolve(res.data);
          },
          fail(err) {
            reject(err);
          }
        })
      })
    });

    // 如果没有图片，直接提交表单
    if (images.length === 0) {
      wx.request({
        url: serverUrl + '/news/publish',
        method: 'POST',
        header: {
          'content-type': 'application/x-www-form-urlencoded'
        },
        data: {
          title: title,
          content: content,
          authorId: authorId
        },
        success(res) {
          wx.hideLoading();
          if (res.statusCode === 200) {
            wx.showToast({
              title: '发布成功',
              icon: 'success'
            });
            // 发布成功后返回首页
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } else {
            wx.showToast({
              title: '发布失败，请重试',
              icon: 'none'
            });
          }
        },
        fail() {
          wx.hideLoading();
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
        }
      });
    } else {
      // 上传所有图片
      Promise.all(uploadTasks)
        .then(() => {
          wx.hideLoading();
          wx.showToast({
            title: '发布成功',
            icon: 'success'
          });
          // 发布成功后返回首页
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        })
        .catch(() => {
          wx.hideLoading();
          wx.showToast({
            title: '发布失败，请重试',
            icon: 'none'
          });
        });
    }
  }
})