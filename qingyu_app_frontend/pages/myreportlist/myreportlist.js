Page({
  data: {
    reportList: [],
    loading: true,
    userId: null
  },

  onLoad: function(options) {
    // 从选项中获取userId，如果没有则尝试从全局或本地存储获取
    if (options.userId) {
      this.setData({
        userId: options.userId
      });
      this.getReportList();
    } else {
      // 尝试从全局或本地存储获取用户信息
      const app = getApp();
      let userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo');
      
      if (userInfo) {
        // 尝试从多个可能的字段获取userId
        const userId = userInfo.userId || userInfo.id || userInfo.openId || userInfo.userid || userInfo.uid;
        if (userId) {
          this.setData({
            userId: userId
          });
          this.getReportList();
        } else {
          wx.showToast({
            title: '获取用户ID失败',
            icon: 'none'
          });
          this.setData({ loading: false });
        }
      } else {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        this.setData({ loading: false });
        // 延时返回上一页
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      }
    }
  },

  // 获取用户上报列表
  getReportList: function() {
    const { userId } = this.data;
    if (!userId) return;

    this.setData({ loading: true });

    wx.request({
      url: `http://localhost:8081/report/user/${userId}`,
      method: 'GET',
      success: (res) => {
        console.log('获取上报列表成功:', res.data);
        
        // 处理响应数据，适配后端可能的不同格式
        let reportList = [];
        if (res.data.data) {
          reportList = res.data.data;
        } else if (Array.isArray(res.data)) {
          reportList = res.data;
        }

        // 格式化时间并处理数据，确保只显示需要的信息
        const formattedList = reportList.map(item => {
          // 时间格式化
          let formattedTime = '';
          if (item.createTime || item.reportTime) {
            formattedTime = this.formatTime(item.createTime || item.reportTime);
          }
          
          // 处理位置信息，适配可能的不同字段名
          let location = '';
          if (item.location || item.address || item.position) {
            location = item.location || item.address || item.position;
          }
          
          // 处理紧急程度，适配可能的不同字段名和数值映射
          let urgency = '普通';
          if (item.urgencyLevel) {
            // 如果是数字，转换为文字描述
            if (typeof item.urgencyLevel === 'number') {
              switch(item.urgencyLevel) {
                case 1: urgency = '低'; break;
                case 2: urgency = '普通'; break;
                case 3: urgency = '高'; break;
                case 4: urgency = '紧急'; break;
                case 5: urgency = '非常紧急'; break;
                default: urgency = '普通';
              }
            } else {
              urgency = item.urgencyLevel;
            }
          } else if (item.urgency) {
            urgency = item.urgency;
          }
          
          // 处理内容信息，适配可能的不同字段名（content和description）
          let content = '';
          if (item.description || item.content) {
            content = item.description || item.content;
          }
          
          return {
            ...item,
            formattedTime: formattedTime,
            location: location,
            urgency: urgency,
            content: content
          };
        });

        this.setData({
          reportList: formattedList,
          loading: false
        });
      },
      fail: (err) => {
        console.error('获取上报列表失败:', err);
        wx.showToast({
          title: '获取数据失败',
          icon: 'none'
        });
        this.setData({ loading: false });
      }
    });
  },

  // 时间格式化函数
  formatTime: function(timeStr) {
    if (!timeStr) return '';
    
    // 处理不同格式的时间字符串
    try {
      let date;
      if (typeof timeStr === 'number') {
        date = new Date(timeStr);
      } else {
        // 处理ISO格式的时间字符串，去掉末尾的时区信息
        let cleanDate = timeStr;
        // 去掉末尾的时区信息，如 '+00:00'
        cleanDate = cleanDate.replace(/\+\d{2}:\d{2}$/, '');
        // 如果包含'T'分隔符，替换为空格
        cleanDate = cleanDate.replace('T', ' ');
        // 尝试转换常见的时间格式
        date = new Date(cleanDate.replace(/-/g, '/'));
      }
      
      if (isNaN(date.getTime())) {
        return timeStr;
      }
      
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (e) {
      console.error('时间格式化失败:', e);
      return timeStr;
    }
  },

  // 跳转到详情页
  goToDetail: function(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/reportdetail/reportdetail?id=${id}`
    });
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    this.getReportList();
    wx.stopPullDownRefresh();
  }
});