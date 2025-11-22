const app=getApp()

Page({
  //定义全局变量data
  data: {
   
  },
  //跳转注册页面
  goRegister: function () {
    wx.redirectTo({
      url: '../register/register',
    })
  },

  //普通登录
  doLogin: function (e) {
    var formObject = e.detail.value;
    //console.log(formObject);
    var username = formObject.username;
    var password = formObject.password;
    //简单验证
    if (username.length == 0 || password.length == 0) {
      wx.showToast({
        title: '用户名或密码不能为空',
        icon: 'none',
        duration: 3000
      })
    } else {
      var serverUrl = app.serverUrl;
      wx.request({
        url: serverUrl + '/login',
        method: "POST",
        data: {
          username: username,
          password: password
        },
        header: {
          'content-type': 'application/json' //默认值
        },
        success: function (res) {
            console.log(res.data);
            var status = res.data.status;
            if (status == 200) {
              // 保存用户信息到本地缓存和全局变量
              const userInfo = res.data.data;
              if (userInfo) {
                // 保存到全局变量
                app.userInfo = userInfo;
                // 保存到本地缓存
                wx.setStorageSync('userInfo', userInfo);
                console.log('用户信息已保存:', userInfo);
              }
              
              //登录成功提示
              wx.showToast({
                title: "登录成功",
                icon: 'success',
                duration: 3000
              })
              
              // 登录成功后跳转到home页面
              setTimeout(function() {
                wx.switchTab({
                  url: '../home/home',
                })
              }, 1500)
            } else {
              wx.showToast({
                title: res.data.msg,
                icon: 'none',
                duration: 3000
              })
            }
          }
      })
    }
  },
  // 微信登录  
  goWxLogin: function (e) {
    console.log(e.detail.errMsg)
    console.log(e.detail.userInfo)
    console.log(e.detail.rawData)

    wx.login({
      success: function (res) {
        console.log(res)
        // 获取登录的临时凭证
        var code = res.code;
        // 调用后端，获取微信的session_key, secret
        var serverUrl = app.serverUrl;
        wx.request({
          url: serverUrl +"/wxLogin?code=" + code,
          method: "POST",
          success: function (result) {
            console.log(result);
            // 保存用户信息到本地缓存和全局变量
            const userInfo = result.data.data || e.detail.userInfo;
            // 保存到全局变量
            app.userInfo = userInfo;
            // 保存到本地缓存
            wx.setStorageSync('userInfo', userInfo);
            console.log('微信登录用户信息已保存:', userInfo);
            
            wx.switchTab({
              url: '../home/home',
            })
          }
        })
      }
    })
  },
})