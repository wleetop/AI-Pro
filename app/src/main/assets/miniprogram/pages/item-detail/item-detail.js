// pages/item-detail/item-detail.js
const storage = require('../../utils/storage.js');
const util = require('../../utils/util.js');

Page({
  data: {
    id: null,
    item: null
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
    }
  },

  onShow() {
    if (this.data.id) {
      this.loadItem(this.data.id);
    }
  },

  loadItem(id) {
    const raw = storage.getItemById(id);
    if (raw) {
      const expiryStatus = util.getExpiryStatus(raw.expireDate);
      this.setData({
        item: {
          ...raw,
          expiryStatus
        }
      });
    }
  },

  onAdjustQty(e) {
    const delta = parseInt(e.currentTarget.dataset.delta, 10);
    const item = { ...this.data.item };
    let newQty = (item.quantity || 1) + delta;
    if (newQty < 0) newQty = 0;
    item.quantity = newQty;

    storage.saveItem(item);
    this.setData({ item });
    wx.showToast({ title: delta > 0 ? '入库 +1' : '出库 -1', icon: 'none' });
  },

  onEditItem() {
    wx.navigateTo({
      url: `/pages/item-edit/item-edit?id=${this.data.id}`
    });
  },

  onDeleteItem() {
    wx.showModal({
      title: '确认删除',
      content: `确定要删除 [${this.data.item.name}] 吗？`,
      confirmColor: '#F5222D',
      success: (res) => {
        if (res.confirm) {
          storage.deleteItem(this.data.id);
          wx.showToast({ title: '已删除', icon: 'success' });
          setTimeout(() => {
            wx.navigateBack();
          }, 500);
        }
      }
    });
  }
});
