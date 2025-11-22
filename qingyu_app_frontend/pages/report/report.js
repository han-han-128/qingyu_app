// pages/report/report.js
const app = getApp();

Page({
  data: {
    longitude: 0, // 经度
    latitude: 0, // 纬度
    locationName: '', // 位置名称
    markers: [], // 地图标记
    emergencyLevel: 1, // 紧急程度：1-一般，2-紧急
    description: '', // 现场描述
    images: [], // 上传的图片
    maxImages: 9, // 最多上传9张图片
    submitting: false, // 是否正在提交
    userInfo: null // 用户信息
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 检查用户登录状态
    this.checkLoginStatus();
    
    // 获取当前用户信息
    this.setData({
      userInfo: wx.getStorageSync('userInfo') || null
    });
    
    // 初始化地图位置
    this.initMapLocation();
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
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    // 每次显示页面都检查登录状态
    if (this.checkLoginStatus()) {
      // 页面显示时可以再次获取位置信息
      // 这里可以添加额外的显示逻辑
    }
  },

  // 返回上一页
  goBack: function() {
    wx.navigateBack();
  },

  // 初始化地图位置
  initMapLocation: function() {
    // 初始化位置信息，不自动获取，让用户手动点击
  },

  // 选择位置
  chooseLocation: function() {
    const that = this;
    
    // 检查位置权限
    wx.getSetting({
      success(settingRes) {
        // 如果位置权限已授权
        if (settingRes.authSetting['scope.userLocation']) {
          that._chooseLocation();
        } 
        // 如果位置权限未授权
        else if (settingRes.authSetting['scope.userLocation'] === undefined) {
          // 请求位置权限
          wx.authorize({
            scope: 'scope.userLocation',
            success() {
              // 授权成功，调用位置选择
              that._chooseLocation();
            },
            fail(err) {
              console.error('授权位置权限失败:', err);
              // 授权失败，提示用户手动开启
              that._showPermissionModal();
            }
          });
        } 
        // 如果位置权限被拒绝
        else {
          // 提示用户手动开启权限
          that._showPermissionModal();
        }
      },
      fail(err) {
        console.error('获取权限设置失败:', err);
        wx.showToast({
          title: '获取权限失败',
          icon: 'none'
        });
      }
    });
  },
  
  // 实际调用位置选择API
  _chooseLocation: function() {
    const that = this;
    
    wx.chooseLocation({
      success(res) {
        that.setData({
          longitude: res.longitude,
          latitude: res.latitude,
          locationName: res.name
        });
      },
      fail(err) {
        // 用户取消选择，不提示
        if (err.errMsg === 'chooseLocation:fail cancel') {
          return;
        }
        
        console.error('调用位置选择API失败:', err);
        wx.showToast({
          title: `获取位置失败: ${err.errMsg}`,
          icon: 'none',
          duration: 3000
        });
      }
    });
  },
  
  // 显示权限引导模态框
  _showPermissionModal: function() {
    wx.showModal({
      title: '位置权限未开启',
      content: '需要获取您的位置信息，请在设置中开启',
      success(modalRes) {
        if (modalRes.confirm) {
          wx.openSetting();
        }
      }
    });
  },

  // 选择紧急程度
  selectEmergencyLevel: function(e) {
    const level = e.currentTarget.dataset.level;
    this.setData({
      emergencyLevel: level
    });
  },

  // 现场描述输入
  onDescriptionInput: function(e) {
    this.setData({
      description: e.detail.value
    });
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
        });
      }
    });
  },

  // 选择视频
  chooseVideo: function() {
    const that = this;
    
    wx.chooseVideo({
      sourceType: ['album', 'camera'],
      maxDuration: 60,
      camera: 'back',
      success(res) {
        // 视频处理逻辑
        wx.showToast({
          title: '视频上传功能开发中',
          icon: 'none'
        });
      }
    });
  },

  // 删除图片
  deleteImage: function(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images;
    images.splice(index, 1);
    this.setData({
      images: images
    });
  },

  // 提交上报
  submitReport: function() {
    const that = this;
    
    // 表单验证
    if (!that.data.description.trim()) {
      wx.showToast({
        title: '请输入现场描述',
        icon: 'none'
      });
      return;
    }
    
    // 位置信息验证
    if (!that.data.longitude || !that.data.latitude) {
      wx.showToast({
        title: '请获取位置信息',
        icon: 'none'
      });
      return;
    }
    
    // 用户信息验证
    if (!that.data.userInfo) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    // 开始提交
    that.setData({
      submitting: true
    });
    
    // 调用后端API
    const serverUrl = app.serverUrl;
    
    // 准备上传的数据
    // 注意：后端期望的是用户名而不是用户ID，所以我们使用userInfo中的username或其他用户名标识
    const userInfo = that.data.userInfo || {};
    const uploadData = {
      userId: userInfo.username || userInfo.id || 'test_user', // 使用username如果存在，否则使用id
      longitude: that.data.longitude,
      latitude: that.data.latitude,
      address: that.data.locationName || '',
      description: that.data.description,
      emergencyLevel: that.data.emergencyLevel
    };
    
    console.log('提交的用户信息:', uploadData.userId);
    
    // 如果没有图片
    if (that.data.images.length === 0) {
      // 发送普通请求
      wx.request({
        url: serverUrl + '/report/emergency',
        method: 'POST',
        header: {
          'content-type': 'application/x-www-form-urlencoded'
        },
        data: uploadData,
        success(res) {
          that.handleSubmitSuccess(res);
        },
        fail(err) {
          that.handleSubmitFail(err);
        }
      });
    } else {
      // 有图片需要上传
      that.uploadImagesWithData(uploadData);
    }
  },

  // 上传图片和数据
  uploadImagesWithData: function(uploadData) {
    const that = this;
    const serverUrl = app.serverUrl;
    const images = this.data.images;
    
    // 微信小程序的wx.uploadFile一次只能上传一个文件，但可以通过name相同的方式传递多个文件
    // 这里我们先上传第一张图片，其余图片通过其他方式处理
    wx.uploadFile({
      url: serverUrl + '/report/emergency',
      filePath: images[0], // 指定第一个文件路径
      name: 'files',      // 文件参数名
      formData: uploadData, // 其他表单数据
      success(res) {
        try {
          const result = JSON.parse(res.data);
          that.handleSubmitSuccess(result);
        } catch (e) {
          console.error('解析返回结果失败:', e);
          that.handleSubmitFail({ message: '返回数据格式错误' });
        }
      },
      fail(err) {
        console.error('上传文件失败:', err);
        that.handleSubmitFail({ message: '图片上传失败，请重试' });
      }
    });
  },

  // 处理提交成功
  handleSubmitSuccess: function(res) {
    const that = this;
    
    that.setData({
      submitting: false
    });
    
    if (res.statusCode === 200 || res.code === 200 || res.data) {
      wx.showToast({
        title: '上报成功',
        icon: 'success'
      });
      
      // 成功后返回首页或上传记录页面
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } else {
      wx.showToast({
        title: '上报失败',
        icon: 'none'
      });
    }
  },

  // 处理提交失败
  handleSubmitFail: function(err) {
    const that = this;
    
    // 重置提交状态
    that.setData({
      submitting: false
    });
    
    // 确保隐藏所有可能的加载提示
    wx.hideLoading();
    
    // 显示错误提示
    const errorMsg = err.message || '网络请求失败';
    wx.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 3000
    });
    
    console.error('提交失败:', err);
  }
});