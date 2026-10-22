// utils/storage.js - 微信小程序端持久化存储管理
const STORAGE_KEYS = {
  ITEMS: 'AI_STORAGE_ITEMS',
  CONTAINERS: 'AI_STORAGE_CONTAINERS',
  CATEGORIES: 'AI_STORAGE_CATEGORIES',
  API_CONFIG: 'AI_STORAGE_API_CONFIG',
  SETTINGS: 'AI_STORAGE_SETTINGS'
};

const DEFAULT_CATEGORIES = [
  { id: 'cat_digi', name: '电子数码', icon: '💻', color: '#1890FF' },
  { id: 'cat_part', name: '电子元器件', icon: '⚡', color: '#722ED1' },
  { id: 'cat_tool', name: '工具五金', icon: '🔧', color: '#FA8C16' },
  { id: 'cat_food', name: '食品饮料', icon: '🍱', color: '#52C41A' },
  { id: 'cat_med', name: '药品个护', icon: '💊', color: '#F5222D' },
  { id: 'cat_cloth', name: '服饰箱包', icon: '👔', color: '#13C2C2' },
  { id: 'cat_beauty', name: '美妆护肤', icon: '💄', color: '#EB2F96' },
  { id: 'cat_home', name: '日常居家', icon: '🏠', color: '#2F54EB' },
  { id: 'cat_camp', name: '户外露营', icon: '⛺', color: '#FAAD14' },
  { id: 'cat_toy', name: '模型收藏', icon: '🤖', color: '#A0D911' },
  { id: 'cat_baby', name: '母婴亲子', icon: '🍼', color: '#FF85C0' },
  { id: 'cat_doc', name: '重要证件', icon: '📑', color: '#FA541C' }
];

const DEFAULT_CONTAINERS = [
  { id: 'c_root_1', name: '主卧', parentId: null, type: 'ROOM' },
  { id: 'c_root_2', name: '客厅', parentId: null, type: 'ROOM' },
  { id: 'c_root_3', name: '书房/创客工作室', parentId: null, type: 'ROOM' },
  { id: 'c_root_4', name: '厨房', parentId: null, type: 'ROOM' },
  { id: 'c_sub_1', name: '元器件防静电抽屉柜', parentId: 'c_root_3', type: 'CABINET' },
  { id: 'c_sub_2', name: '3D打印与测试工具架', parentId: 'c_root_3', type: 'SHELF' },
  { id: 'c_sub_3', name: '家庭医药急救箱', parentId: 'c_root_2', type: 'BOX' },
  { id: 'c_sub_4', name: '数码配件收纳盒', parentId: 'c_root_3', type: 'BOX' }
];

function initStorage() {
  if (!wx.getStorageSync(STORAGE_KEYS.CATEGORIES)) {
    wx.setStorageSync(STORAGE_KEYS.CATEGORIES, DEFAULT_CATEGORIES);
  }
  if (!wx.getStorageSync(STORAGE_KEYS.CONTAINERS)) {
    wx.setStorageSync(STORAGE_KEYS.CONTAINERS, DEFAULT_CONTAINERS);
  }
  if (!wx.getStorageSync(STORAGE_KEYS.ITEMS)) {
    wx.setStorageSync(STORAGE_KEYS.ITEMS, []);
  }
  if (!wx.getStorageSync(STORAGE_KEYS.API_CONFIG)) {
    wx.setStorageSync(STORAGE_KEYS.API_CONFIG, {
      provider: 'OFFLINE_HEURISTIC',
      apiKey: '',
      model: 'hunyuan-vision',
      autoDetectPrice: true,
      autoExpiryDays: true
    });
  }
}

// 物品 CRUD
function getItems() {
  return wx.getStorageSync(STORAGE_KEYS.ITEMS) || [];
}

function getItemById(id) {
  const items = getItems();
  return items.find(item => item.id === id) || null;
}

function saveItem(item) {
  const items = getItems();
  if (!item.id) {
    item.id = 'item_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
    item.createdAt = Date.now();
    item.updatedAt = Date.now();
    items.unshift(item);
  } else {
    const index = items.findIndex(i => i.id === item.id);
    item.updatedAt = Date.now();
    if (index >= 0) {
      items[index] = item;
    } else {
      items.unshift(item);
    }
  }
  wx.setStorageSync(STORAGE_KEYS.ITEMS, items);
  return item;
}

function deleteItem(id) {
  let items = getItems();
  items = items.filter(i => i.id !== id);
  wx.setStorageSync(STORAGE_KEYS.ITEMS, items);
  return true;
}

function batchInsertItems(newItems) {
  const items = getItems();
  newItems.forEach(item => {
    if (!item.id) {
      item.id = 'item_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
      item.createdAt = Date.now();
    }
    item.updatedAt = Date.now();
    items.unshift(item);
  });
  wx.setStorageSync(STORAGE_KEYS.ITEMS, items);
  return true;
}

// 容器 CRUD
function getContainers() {
  return wx.getStorageSync(STORAGE_KEYS.CONTAINERS) || DEFAULT_CONTAINERS;
}

function saveContainer(container) {
  const containers = getContainers();
  if (!container.id) {
    container.id = 'c_' + Date.now();
    containers.push(container);
  } else {
    const idx = containers.findIndex(c => c.id === container.id);
    if (idx >= 0) containers[idx] = container;
    else containers.push(container);
  }
  wx.setStorageSync(STORAGE_KEYS.CONTAINERS, containers);
  return container;
}

function deleteContainer(id) {
  let containers = getContainers();
  containers = containers.filter(c => c.id !== id && c.parentId !== id);
  wx.setStorageSync(STORAGE_KEYS.CONTAINERS, containers);
}

// 分类 CRUD
function getCategories() {
  return wx.getStorageSync(STORAGE_KEYS.CATEGORIES) || DEFAULT_CATEGORIES;
}

// API 配置
function getApiConfig() {
  return wx.getStorageSync(STORAGE_KEYS.API_CONFIG) || {
    provider: 'OFFLINE_HEURISTIC',
    apiKey: '',
    model: 'hunyuan-vision'
  };
}

function setApiConfig(config) {
  wx.setStorageSync(STORAGE_KEYS.API_CONFIG, config);
}

// 数据导出与全量导入
function exportAllData() {
  return JSON.stringify({
    version: '2.0.0',
    exportTime: new Date().toISOString(),
    items: getItems(),
    containers: getContainers(),
    categories: getCategories()
  }, null, 2);
}

function importAllData(jsonString) {
  try {
    const data = JSON.parse(jsonString);
    if (data.items && Array.isArray(data.items)) {
      wx.setStorageSync(STORAGE_KEYS.ITEMS, data.items);
    }
    if (data.containers && Array.isArray(data.containers)) {
      wx.setStorageSync(STORAGE_KEYS.CONTAINERS, data.containers);
    }
    if (data.categories && Array.isArray(data.categories)) {
      wx.setStorageSync(STORAGE_KEYS.CATEGORIES, data.categories);
    }
    return { success: true, count: (data.items || []).length };
  } catch (e) {
    return { success: false, error: e.message };
  }
}

module.exports = {
  initStorage,
  getItems,
  getItemById,
  saveItem,
  deleteItem,
  batchInsertItems,
  getContainers,
  saveContainer,
  deleteContainer,
  getCategories,
  getApiConfig,
  setApiConfig,
  exportAllData,
  importAllData
};
