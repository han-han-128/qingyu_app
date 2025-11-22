// pages/map/map.js
Page({
  data: {
    latitude: 25.9,
    longitude: 118.3,
    scale: 8,
    regions: {
      fujian: { latitude: 25.5, longitude: 118.5, scale: 8 },
      taiwan: { latitude: 23.573, longitude: 120.982, scale: 8 },
      quanzhouMangrove: { latitude: 24.90755000, longitude: 118.68580000, scale: 12 }
    },
    currentRegion: 'fujian',
    markers: [],
    monitorPoints: []
  },

  onLoad() {
    this.setRegion('fujian');
  },

  onShow() {
    if (this.data.currentRegion === 'quanzhouMangrove') {
      this.loadMonitorPoints();
    }
  },

  setRegion(key) {
    const r = this.data.regions[key];
    if (!r) return;
    this.setData({
      latitude: r.latitude,
      longitude: r.longitude,
      scale: r.scale,
      currentRegion: key
    });

    if (key === 'quanzhouMangrove') {
      this.loadMonitorPoints();
    } else {
      this.setData({ markers: [] });
    }
  },

  switchRegion(e) {
    const key = e.currentTarget.dataset.key;
    this.setRegion(key);
  },

  loadMonitorPoints() {
    console.log('开始加载监测点...');
    wx.request({
      url: 'http://localhost:8081/monitors',
      method: 'GET',
      data: {
        mapType: 'mangrove'
      },
      success: (res) => {
        console.log('监测点数据:', res.data);
        if (res.data.code === 200 && res.data.data) {
          const points = res.data.data;
          console.log('监测点数量:', points.length);
          
          points.forEach((point, index) => {
            console.log(`监测点${index + 1}:`, {
              id: point.id,
              name: point.name,
              latitude: point.latitude,
              longitude: point.longitude,
              latType: typeof point.latitude,
              lngType: typeof point.longitude
            });
          });

          const markers = points.map((point, index) => {
            const lat = parseFloat(point.latitude);
            const lng = parseFloat(point.longitude);
            console.log(`转换后坐标${index + 1}: lat=${lat}, lng=${lng}`);
            
            return {
              id: point.id,
              latitude: lat,
              longitude: lng,
              width: 35,
              height: 35,
              label: {
                content: point.name,
                color: '#ffffff',
                fontSize: 12,
                bgColor: '#ff0000',
                borderRadius: 5,
                padding: 5
              }
            };
          });
          
          console.log('生成的标记:', markers);
          console.log('当前地图中心:', {
            latitude: this.data.latitude,
            longitude: this.data.longitude,
            scale: this.data.scale
          });
          
          this.setData({
            monitorPoints: points,
            markers: markers
          }, () => {
            console.log('setData完成，当前markers数量:', this.data.markers.length);
            console.log('markers详情:', this.data.markers);
          });
        } else {
          console.log('数据格式不正确:', res.data);
        }
      },
      fail: (err) => {
        console.error('获取监测点失败:', err);
        wx.showToast({
          title: '获取监测点失败',
          icon: 'none'
        });
      }
    });
  },

  onMarkerTap(e) {
    const markerId = e.detail.markerId;
    console.log('点击标记:', markerId);
    wx.navigateTo({
      url: `/pages/monitor-detail/monitor-detail?id=${markerId}`
    });
  }
})