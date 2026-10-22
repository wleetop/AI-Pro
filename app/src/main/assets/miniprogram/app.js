// app.js - 微信小程序全局逻辑
const storage = require('./utils/storage.js');

App({
  onLaunch() {
    // 初始化本地存储结构
    storage.initStorage();
    
    // 检查并提示更新
    if (wx.canIUse('getUpdateManager')) {
      const updateManager = wx.getUpdateManager();
      updateManager.onCheckForUpdate(function (res) {
        if (res.hasUpdate) {
          updateManager.onUpdateReady(function () {
            wx.showModal({
              title: '更新提示',
              content: '新版本已经准备好，是否重启应用？',
              success: function (res) {
                if (res.confirm) {
                  updateManager.applyUpdate();
                }
              }
            });
          });
        }
      });
    }
  },
  globalData: {
    userInfo: null,
    version: '2.0.0-MiniProgram'
  }
});
