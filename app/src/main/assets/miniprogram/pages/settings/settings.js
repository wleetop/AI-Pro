// pages/settings/settings.js
const storage = require('../../utils/storage.js');

Page({
  data: {
    config: {},
    providerOptions: [
      { id: 'OFFLINE_HEURISTIC', name: '离线智能知识库 (无需联网与Key)' },
      { id: 'TENCENT_HUNYUAN', name: '腾讯混元大模型 (Hunyuan Vision)' },
      { id: 'ALIYUN_BAILIAN', name: '阿里云通义千问 (Qwen-VL)' },
      { id: 'ZHIPU_GLM', name: '智谱 AI (GLM-4V)' },
      { id: 'MOONSHOT_KIMI', name: 'Moonshot Kimi (月之暗面)' },
      { id: 'DEEPSEEK', name: 'DeepSeek / OpenAI 兼容' }
    ],
    providerIndex: 0,
    hideKey: true
  },

  onShow() {
    const config = storage.getApiConfig();
    const idx = this.data.providerOptions.findIndex(p => p.id === config.provider);
    this.setData({
      config,
      providerIndex: idx >= 0 ? idx : 0
    });
  },

  onProviderChange(e) {
    const idx = parseInt(e.detail.value, 10);
    const selected = this.data.providerOptions[idx];
    const newConfig = {
      ...this.data.config,
      provider: selected.id
    };
    storage.setApiConfig(newConfig);
    this.setData({
      providerIndex: idx,
      config: newConfig
    });
  },

  onApiKeyInput(e) {
    const key = e.detail.value;
    const newConfig = {
      ...this.data.config,
      apiKey: key
    };
    storage.setApiConfig(newConfig);
    this.setData({ config: newConfig });
  },

  toggleKeyVisibility() {
    this.setData({ hideKey: !this.data.hideKey });
  },

  onExportData() {
    const jsonStr = storage.exportAllData();
    wx.setClipboardData({
      data: jsonStr,
      success: () => {
        wx.showToast({ title: '全量备份已复制到剪贴板', icon: 'success' });
      }
    });
  },

  onImportData() {
    wx.getClipboardData({
      success: (res) => {
        if (!res.data || !res.data.trim()) {
          wx.showToast({ title: '剪贴板为空', icon: 'none' });
          return;
        }
        wx.showModal({
          title: '确认导入',
          content: '检测到剪贴板数据，导入将合并覆盖当前物品与空间结构，是否继续？',
          success: (mRes) => {
            if (mRes.confirm) {
              const result = storage.importAllData(res.data);
              if (result.success) {
                wx.showToast({ title: `成功导入 ${result.count} 件物品`, icon: 'success' });
              } else {
                wx.showModal({ title: '导入失败', content: result.error, showCancel: false });
              }
            }
          }
        });
      }
    });
  }
});
