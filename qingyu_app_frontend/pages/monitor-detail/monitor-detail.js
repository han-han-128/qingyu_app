// pages/monitor-detail/monitor-detail.js
Page({
  data: {
    monitorId: null,
    monitorInfo: null,
    monitorData: [],
    loading: true,
    hlsUrl: '',
    isStreaming: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ monitorId: options.id });
      this.loadMonitorDetail();
      this.loadMonitorData();
    }
  },

  loadMonitorDetail() {
    wx.request({
      url: `http://localhost:8081/monitors/${this.data.monitorId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          this.setData({
            monitorInfo: res.data.data,
            loading: false
          });
          wx.setNavigationBarTitle({
            title: res.data.data.name || '监测点详情'
          });
        }
      },
      fail: (err) => {
        console.error('获取监测点详情失败:', err);
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
        this.setData({ loading: false });
      }
    });
  },

  loadMonitorData() {
    wx.request({
      url: `http://localhost:8081/monitor-data/latest/${this.data.monitorId}`,
      method: 'GET',
      data: {
        limit: 20
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          this.setData({
            monitorData: res.data.data
          });
        }
      },
      fail: (err) => {
        console.error('获取监测数据失败:', err);
      }
    });
  },

  startVideo() {
    wx.showLoading({ title: '启动中...' });
    wx.request({
      url: `http://localhost:8081/video/start/${this.data.monitorId}`,
      method: 'GET',
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200 && res.data.data) {
          const hlsUrl = `http://localhost:8081${res.data.data.hlsUrl}`;
          this.setData({
            hlsUrl: hlsUrl,
            isStreaming: true
          });
          wx.showToast({
            title: '视频流已启动',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '启动失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('启动视频流失败:', err);
        wx.showToast({
          title: '启动失败',
          icon: 'none'
        });
      }
    });
  },

  stopVideo() {
    wx.request({
      url: `http://localhost:8081/video/stop/${this.data.monitorId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            hlsUrl: '',
            isStreaming: false
          });
          wx.showToast({
            title: '视频流已停止',
            icon: 'success'
          });
        }
      },
      fail: (err) => {
        console.error('停止视频流失败:', err);
      }
    });
  },

  onUnload() {
    if (this.data.isStreaming) {
      this.stopVideo();
    }
  },

  onPullDownRefresh() {
    this.loadMonitorDetail();
    this.loadMonitorData();
    setTimeout(() => {
      wx.stopPullDownRefresh();
    }, 1000);
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  },

  getDataTypeText(type) {
    if (!type) return '';
    
    const typeMap = {
      'temperature': '温度',
      'humidity': '湿度',
      'ph': 'PH值',
      'oxygen': '溶解氧',
      'salinity': '盐度',
      'turbidity': '浊度',
      'pressure': '压力',
      'conductivity': '电导率',
      'depth': '水深',
      'velocity': '流速',
      'flow': '流量',
      'ammonia': '氨氮',
      'nitrate': '硝酸盐',
      'phosphate': '磷酸盐'
    };
    
    if (typeMap[type]) return typeMap[type];
    
    const normalizedType = type.toLowerCase().replace(/([A-Z])/g, '_$1').replace(/-/g, '_').replace(/^_/, '');
    if (typeMap[normalizedType]) return typeMap[normalizedType];
    
    return type || '未知参数';
  },

  getDataUnit(type) {
    const unitMap = {
      'temperature': '°C',
      'humidity': '%',
      'ph': '',
      'oxygen': 'mg/L',
      'salinity': '‰',
      'turbidity': 'NTU'
    };
    return unitMap[type] || '';
  }
})