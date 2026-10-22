// pages/containers/containers.js
const storage = require('../../utils/storage.js');

Page({
  data: {
    containersTree: []
  },

  onShow() {
    this.loadContainers();
  },

  loadContainers() {
    const rawContainers = storage.getContainers();
    const items = storage.getItems();

    // 构建层级树
    const roots = rawContainers.filter(c => !c.parentId);
    const tree = roots.map(root => {
      const children = rawContainers.filter(c => c.parentId === root.id).map(child => {
        const itemCount = items.filter(i => (i.locationPath && i.locationPath.includes(child.name)) || (i.suggestedLocation && i.suggestedLocation.includes(child.name))).length;
        return {
          ...child,
          itemCount
        };
      });

      const rootItemCount = items.filter(i => (i.locationPath && i.locationPath.includes(root.name)) || (i.suggestedLocation && i.suggestedLocation.includes(root.name))).length;

      return {
        ...root,
        itemCount: rootItemCount,
        children
      };
    });

    this.setData({ containersTree: tree });
  },

  onAddRootContainer() {
    wx.showModal({
      title: '新建根空间/房间',
      editable: true,
      placeholderText: '例如: 主卧、创客工作室、阳台',
      success: (res) => {
        if (res.confirm && res.content && res.content.trim()) {
          storage.saveContainer({
            name: res.content.trim(),
            parentId: null,
            type: 'ROOM'
          });
          this.loadContainers();
          wx.showToast({ title: '创建成功', icon: 'success' });
        }
      }
    });
  },

  onAddChild(e) {
    const { parentId, parentName } = e.currentTarget.dataset;
    wx.showModal({
      title: `在 [${parentName}] 下添加容器`,
      editable: true,
      placeholderText: '例如: 0805电阻元件盒、上层抽屉',
      success: (res) => {
        if (res.confirm && res.content && res.content.trim()) {
          storage.saveContainer({
            name: res.content.trim(),
            parentId: parentId,
            type: 'BOX'
          });
          this.loadContainers();
          wx.showToast({ title: '添加成功', icon: 'success' });
        }
      }
    });
  },

  onDeleteContainer(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除空间将同时移除其下的子容器关联，是否继续？',
      confirmColor: '#FF4D4F',
      success: (res) => {
        if (res.confirm) {
          storage.deleteContainer(id);
          this.loadContainers();
          wx.showToast({ title: '已删除', icon: 'success' });
        }
      }
    });
  }
});
