Component({
  data: {
    value: '', // 初始值设置为空，避免第一次加载时闪烁
    list: [
      {
        icon: 'home',
        value: 'home',
        label: '首页',
      },
      {
        icon: 'map',
        value: 'map',
        label: '地图',
      },
      {
        icon: 'scan',
        value: 'identify',
        label: '识别',
      },
      {
        icon: 'heart',
        value: 'activity',
        label: '公益',
      },
      {
        icon: 'user',
        value: 'my',
        label: '我的',
      },
    ],
  },
  lifetimes: {
    ready() {
      const pages = getCurrentPages();
      const curPage = pages[pages.length - 1];
      if (curPage) {
        const nameRe = /pages\/(\w+)\/\w+/.exec(curPage.route);
        if (nameRe === null) return;
        if (nameRe[1]) {
          this.setData({
            value: nameRe[1],
          });
        }
      }

      // 页面初始化完成
    },
  },
  methods: {
    handleChange(e) {
      const { value } = e.detail;
      wx.switchTab({ url: `/pages/${value}/${value}` });
    },

  },
});
