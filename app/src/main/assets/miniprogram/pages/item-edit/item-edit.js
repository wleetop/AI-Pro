// pages/item-edit/item-edit.js
const storage = require('../../utils/storage.js');

Page({
  data: {
    formData: {
      id: null,
      name: '',
      categoryName: '电子数码',
      locationPath: '',
      quantity: 1,
      unit: '个',
      price: '',
      spec: '',
      brand: '',
      barcode: '',
      expireDate: '',
      tags: []
    },
    tagsInput: '',
    categories: [],
    categoryNames: [],
    categoryIndex: 0
  },

  onLoad(options) {
    const categories = storage.getCategories();
    const categoryNames = categories.map(c => c.name);

    if (options.id) {
      const existing = storage.getItemById(options.id);
      if (existing) {
        const catIdx = categoryNames.indexOf(existing.categoryName);
        this.setData({
          formData: { ...existing },
          tagsInput: (existing.tags || []).join(', '),
          categories,
          categoryNames,
          categoryIndex: catIdx >= 0 ? catIdx : 0
        });
        return;
      }
    }

    if (options.barcode) {
      this.setData({
        'formData.barcode': decodeURIComponent(options.barcode)
      });
    }

    this.setData({
      categories,
      categoryNames,
      'formData.categoryName': categoryNames[0] || '电子数码'
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    this.setData({
      [`formData.${field}`]: value
    });
  },

  onCategoryChange(e) {
    const idx = parseInt(e.detail.value, 10);
    this.setData({
      categoryIndex: idx,
      'formData.categoryName': this.data.categoryNames[idx]
    });
  },

  onDateChange(e) {
    this.setData({
      'formData.expireDate': e.detail.value
    });
  },

  onTagsInput(e) {
    this.setData({ tagsInput: e.detail.value });
  },

  onScanBarcode() {
    wx.scanCode({
      success: (res) => {
        this.setData({ 'formData.barcode': res.result });
      }
    });
  },

  onSave() {
    const { formData, tagsInput } = this.data;
    if (!formData.name || !formData.name.trim()) {
      wx.showToast({ title: '请输入物品名称', icon: 'none' });
      return;
    }

    const tags = tagsInput.split(/[,，\s]+/).filter(t => t.trim().length > 0);

    const toSave = {
      ...formData,
      name: formData.name.trim(),
      quantity: parseInt(formData.quantity || 1, 10),
      price: parseFloat(formData.price || 0),
      tags: tags
    };

    storage.saveItem(toSave);
    wx.showToast({ title: '已成功保存', icon: 'success' });
    setTimeout(() => {
      wx.navigateBack();
    }, 500);
  }
});
