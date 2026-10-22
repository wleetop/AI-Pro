// utils/util.js - 实用工具函数
const formatTime = date => {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`;
};

const formatNumber = n => {
  n = n.toString();
  return n[1] ? n : `0${n}`;
};

function getExpiryStatus(expireDateStr) {
  if (!expireDateStr) return { status: 'NORMAL', text: '长期有效', days: 999 };
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(expireDateStr);
  target.setHours(0, 0, 0, 0);
  const diffDays = Math.ceil((target - today) / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return { status: 'EXPIRED', text: `已过期 ${Math.abs(diffDays)} 天`, days: diffDays };
  } else if (diffDays <= 30) {
    return { status: 'WARNING', text: `临期剩 ${diffDays} 天`, days: diffDays };
  } else {
    return { status: 'NORMAL', text: `剩余 ${diffDays} 天`, days: diffDays };
  }
}

module.exports = {
  formatTime,
  getExpiryStatus
};
