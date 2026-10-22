// pages/index/index.js
const storage = require('../../utils/storage.js');
const util = require('../../utils/util.js');

Page({
  data: {
    items: [],
    filteredItems: [],
    categories: [],
    selectedCategory: 'ALL',
    searchKeyword: '',
    totalValue: '0.00',
    expiringCount: 0,
    containersCount: 0,
    sortMode: 'TIME_DESC',
    sortModeName: '最新'
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  },

  loadData() {
    const rawItems = storage.getItems();
    const categories = storage.getCategories();
    const containers = storage.getContainers();

    let totalVal = 0;
    let expCount = 0;

    const items = rawItems.map(item => {
      const price = parseFloat(item.price || item.estimatedPrice || 0);
      const qty = parseInt(item.quantity || 1, 10);
      totalVal += price * qty;

      const expiryStatus = util.getExpiryStatus(item.expireDate);
      if (expiryStatus.status === 'EXPIRED' || expiryStatus.status === 'WARNING') {
        expCount++;
      }

      return {
        ...item,
        price: price > 0 ? price.toFixed(2) : null,
        expiryStatus
      };
    });

    this.setData({
      items,
      categories,
      containersCount: containers.length,
      totalValue: totalVal.toFixed(2),
      expiringCount: expCount
    }, () => {
      this.applyFilterAndSort();
    });
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value }, () => {
      this.applyFilterAndSort();
    });
  },

  onClearSearch() {
    this.setData({ searchKeyword: '' }, () => {
      this.applyFilterAndSort();
    });
  },

  onSelectCategory(e) {
    const cat = e.currentTarget.dataset.category;
    this.setData({ selectedCategory: cat }, () => {
      this.applyFilterAndSort();
    });
  },

  onToggleSort() {
    const nextSort = this.data.sortMode === 'TIME_DESC' ? 'EXPIRY_ASC' : (this.data.sortMode === 'EXPIRY_ASC' ? 'PRICE_DESC' : 'TIME_DESC');
    const sortNames = {
      'TIME_DESC': '最新',
      'EXPIRY_ASC': '保质期最近',
      'PRICE_DESC': '价值最高'
    };
    this.setData({
      sortMode: nextSort,
      sortModeName: sortNames[nextSort]
    }, () => {
      this.applyFilterAndSort();
    });
  },

  applyFilterAndSort() {
    let list = [...this.data.items];

    // 分类筛选
    if (this.data.selectedCategory !== 'ALL') {
      list = list.filter(i => i.categoryName === this.data.selectedCategory);
    }

    // 关键词搜索
    if (this.data.searchKeyword.trim()) {
      const kw = this.data.searchKeyword.toLowerCase().trim();
      list = list.filter(i => 
        (i.name && i.name.toLowerCase().includes(kw)) ||
        (i.categoryName && i.categoryName.toLowerCase().includes(kw)) ||
        (i.spec && i.spec.toLowerCase().includes(kw)) ||
        (i.brand && i.brand.toLowerCase().includes(kw)) ||
        (i.suggestedLocation && i.suggestedLocation.toLowerCase().includes(kw)) ||
        (i.barcode && i.barcode.toLowerCase().includes(kw)) ||
        (i.tags && i.tags.some(t => t.toLowerCase().includes(kw)))
      );
    }

    // 排序
    if (this.data.sortMode === 'EXPIRY_ASC') {
      list.sort((a, b) => (a.expiryStatus?.days || 999) - (b.expiryStatus?.days || 999));
    } else if (this.data.sortMode === 'PRICE_DESC') {
      list.sort((a, b) => (parseFloat(b.price || 0) - parseFloat(a.price || 0)));
    } else {
      list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    }

    this.setData({ filteredItems: list });
  },

  onItemClick(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/item-detail/item-detail?id=${id}`
    });
  },

  onQuickAdd() {
    wx.navigateTo({
      url: '/pages/item-edit/item-edit'
    });
  },

  onScanCode() {
    wx.scanCode({
      success: (res) => {
        const barcode = res.result;
        // 查找已有物品
        const matched = this.data.items.find(i => i.barcode === barcode);
        if (matched) {
          wx.navigateTo({
            url: `/pages/item-detail/item-detail?id=${matched.id}`
          });
        } else {
          wx.showModal({
            title: '扫描成功',
            content: `扫描条码/二维码: ${barcode}，是否立即创建新物品？`,
            confirmText: '录入新物品',
            success: (mRes) => {
              if (mRes.confirm) {
                wx.navigateTo({
                  url: `/pages/item-edit/item-edit?barcode=${encodeURIComponent(barcode)}`
                });
              }
            }
          });
        }
      },
      fail: (err) => {
        console.log('Scan code cancel/fail', err);
      }
    });
  }
});
