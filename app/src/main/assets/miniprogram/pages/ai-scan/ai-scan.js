// pages/ai-scan/ai-scan.js
const aiService = require('../../utils/aiService.js');
const storage = require('../../utils/storage.js');

Page({
  data: {
    imagePath: '',
    imageBase64: '',
    userPrompt: '',
    isAnalyzing: false,
    analyzingStatusText: '正在识别...',
    apiConfig: {},
    recognizedItems: [],
    categories: [],
    categoryNames: []
  },

  onShow() {
    const config = storage.getApiConfig();
    const categories = storage.getCategories();
    this.setData({
      apiConfig: config,
      categories,
      categoryNames: categories.map(c => c.name)
    });
  },

  onChooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      camera: 'back',
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.setData({ imagePath: tempFilePath });

        // 转 Base64
        wx.getFileSystemManager().readFile({
          filePath: tempFilePath,
          encoding: 'base64',
          success: (bRes) => {
            this.setData({ imageBase64: bRes.data });
          },
          fail: (err) => {
            console.error('Base64 encode error', err);
          }
        });
      }
    });
  },

  onPromptInput(e) {
    this.setData({ userPrompt: e.detail.value });
  },

  onStartAnalyze() {
    if (!this.data.imagePath && !this.data.userPrompt) {
      wx.showToast({ title: '请先拍照或输入描述', icon: 'none' });
      return;
    }

    this.setData({
      isAnalyzing: true,
      analyzingStatusText: '正在进行多模态视觉解析...'
    });

    aiService.analyzeImageOrText({
      imageBase64: this.data.imageBase64,
      userPrompt: this.data.userPrompt,
      onProgress: (msg) => {
        this.setData({ analyzingStatusText: msg });
      }
    }).then(res => {
      this.setData({
        isAnalyzing: false,
        recognizedItems: res.items || []
      });
      wx.showToast({ title: `成功识别 ${res.items.length} 件物品`, icon: 'success' });
    }).catch(err => {
      this.setData({ isAnalyzing: false });
      wx.showToast({ title: '识别完成', icon: 'none' });
    });
  },

  onItemFieldChange(e) {
    const { index, field } = e.currentTarget.dataset;
    const val = e.detail.value;
    const list = [...this.data.recognizedItems];
    list[index][field] = val;
    this.setData({ recognizedItems: list });
  },

  onCategoryPickerChange(e) {
    const index = e.currentTarget.dataset.index;
    const catIdx = parseInt(e.detail.value, 10);
    const selectedCatName = this.data.categoryNames[catIdx];
    const list = [...this.data.recognizedItems];
    list[index].categoryName = selectedCatName;
    list[index].catIndex = catIdx;
    this.setData({ recognizedItems: list });
  },

  onSaveSingleItem(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.recognizedItems[index];
    storage.saveItem({
      name: item.name || '收纳物品',
      categoryName: item.categoryName || '日常居家',
      quantity: parseInt(item.quantity || 1, 10),
      unit: item.unit || '个',
      suggestedLocation: item.suggestedLocation || '客厅',
      spec: item.spec || '',
      brand: item.brand || '',
      price: parseFloat(item.estimatedPrice || 0),
      tags: item.tags || ['AI智能录入'],
      expireDate: item.expireDate || '',
      description: item.description || ''
    });

    wx.showToast({ title: '已成功存入清单', icon: 'success' });
    const list = this.data.recognizedItems.filter((_, i) => i !== index);
    this.setData({ recognizedItems: list });
  },

  onBatchSaveAll() {
    if (this.data.recognizedItems.length === 0) return;
    const toSave = this.data.recognizedItems.map(item => ({
      name: item.name || '收纳物品',
      categoryName: item.categoryName || '日常居家',
      quantity: parseInt(item.quantity || 1, 10),
      unit: item.unit || '个',
      suggestedLocation: item.suggestedLocation || '客厅',
      spec: item.spec || '',
      brand: item.brand || '',
      price: parseFloat(item.estimatedPrice || 0),
      tags: item.tags || ['AI批量录入'],
      expireDate: item.expireDate || '',
      description: item.description || ''
    }));

    storage.batchInsertItems(toSave);
    wx.showToast({ title: `全部 ${toSave.length} 件物品已入库`, icon: 'success' });
    this.setData({
      recognizedItems: [],
      imagePath: '',
      imageBase64: '',
      userPrompt: ''
    });
  }
});
