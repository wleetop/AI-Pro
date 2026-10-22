// utils/localHeuristic.js - 微信小程序离线智能启发式识别引擎
const BRAND_MAP = [
  { brand: 'STMicroelectronics', aliases: ['意法半导体', 'stm32', 'stm32f103', 'stm32f407', 'stlink'], category: '电子元器件', location: '书房/创客工作室/元器件防静电抽屉柜' },
  { brand: 'Espressif', aliases: ['乐鑫', 'esp32', 'esp32s3', 'esp32c3', 'esp8266'], category: '电子元器件', location: '书房/创客工作室/元器件防静电抽屉柜' },
  { brand: 'Raspberry Pi', aliases: ['树莓派', 'raspberry pi', 'rpi', 'rp2040', 'pico'], category: '电子元器件', location: '书房/创客工作室/单板微电脑柜' },
  { brand: 'Bambu Lab', aliases: ['拓竹', 'bambu', 'x1c', 'p1s', 'a1mini'], category: '工具五金', location: '书房/创客工作室/3D打印与测试工具架' },
  { brand: 'Fluke', aliases: ['福禄克', 'fluke', '万用表'], category: '工具五金', location: '书房/创客工作室/3D打印与测试工具架' },
  { brand: 'Apple', aliases: ['苹果', 'iphone', 'ipad', 'macbook', 'airpods'], category: '电子数码', location: '书房/创客工作室/数码配件收纳盒' },
  { brand: 'DJI', aliases: ['大疆', 'mavic', 'pocket', 'action', 'osmo'], category: '电子数码', location: '书房/创客工作室/数码配件收纳盒' },
  { brand: 'Sony', aliases: ['索尼', 'ps5', 'playstation', 'a7m4'], category: '电子数码', location: '客厅/数码收纳' },
  { brand: 'Nike', aliases: ['耐克', 'nike', 'aj'], category: '服饰箱包', location: '玄关/鞋柜' },
  { brand: 'Estee Lauder', aliases: ['雅诗兰黛', '小棕瓶'], category: '美妆护肤', location: '主卧/梳妆台' }
];

const KEYWORD_RULES = [
  { keyword: '贴片电阻', category: '电子元器件', unit: '盘', location: '书房/创客工作室/元器件防静电抽屉柜', tags: ['电阻', '0805', '阻容感'], price: 15 },
  { keyword: '电容', category: '电子元器件', unit: '包', location: '书房/创客工作室/元器件防静电抽屉柜', tags: ['电解电容', '滤波'], price: 20 },
  { keyword: '单片机', category: '电子元器件', unit: '块', location: '书房/创客工作室/元器件防静电抽屉柜', tags: ['MCU', '核心板'], price: 25 },
  { keyword: '开发板', category: '电子元器件', unit: '块', location: '书房/创客工作室/元器件防静电抽屉柜', tags: ['嵌入式', '测试'], price: 45 },
  { keyword: '焊台', category: '工具五金', unit: '台', location: '书房/创客工作室/3D打印与测试工具架', tags: ['T12', '焊接工具'], price: 280 },
  { keyword: '螺丝刀', category: '工具五金', unit: '套', location: '书房/创客工作室/3D打印与测试工具架', tags: ['精密五金', '拆机'], price: 39 },
  { keyword: '3d打印耗材', category: '工具五金', unit: '卷', location: '书房/创客工作室/3D打印与测试工具架', tags: ['PLA', '耗材'], price: 65 },
  { keyword: '创可贴', category: '药品个护', unit: '盒', location: '客厅/家庭医药急救箱', tags: ['急救', '外用'], price: 12, expiryDays: 365 },
  { keyword: '布洛芬', category: '药品个护', unit: '盒', location: '客厅/家庭医药急救箱', tags: ['退烧止痛', '应急'], price: 22, expiryDays: 180 },
  { keyword: '感冒灵', category: '药品个护', unit: '盒', location: '客厅/家庭医药急救箱', tags: ['内服药'], price: 18, expiryDays: 180 },
  { keyword: '抽纸', category: '日常居家', unit: '提', location: '客厅/茶几', tags: ['日用品', '纸品'], price: 25 },
  { keyword: '咖啡豆', category: '食品饮料', unit: '袋', location: '厨房/咖啡角', tags: ['精品咖啡', '冲饮'], price: 85, expiryDays: 90 },
  { keyword: '充电宝', category: '电子数码', unit: '个', location: '书房/创客工作室/数码配件收纳盒', tags: ['快充', '随身'], price: 129 }
];

function recognizeLocally(rawText) {
  const text = (rawText || '').toLowerCase().trim();
  let matchedName = rawText || '智能归纳物品';
  let matchedCategory = '日常居家';
  let matchedUnit = '个';
  let matchedLocation = '客厅/收纳箱';
  let matchedQuantity = 1;
  let matchedPrice = 0;
  let matchedExpiryDays = 0;
  let matchedTags = ['本地智能分析'];
  let matchedBrand = '';
  let matchedSpec = '';

  // 1. 匹配品牌
  for (const b of BRAND_MAP) {
    if (b.aliases.some(alias => text.includes(alias.toLowerCase()))) {
      matchedBrand = b.brand;
      matchedCategory = b.category;
      matchedLocation = b.location;
      matchedTags.push(b.brand);
      break;
    }
  }

  // 2. 匹配关键字
  for (const rule of KEYWORD_RULES) {
    if (text.includes(rule.keyword.toLowerCase())) {
      matchedName = rawText.trim();
      matchedCategory = rule.category;
      matchedUnit = rule.unit || matchedUnit;
      matchedLocation = rule.location || matchedLocation;
      if (rule.price) matchedPrice = rule.price;
      if (rule.expiryDays) matchedExpiryDays = rule.expiryDays;
      if (rule.tags) matchedTags = matchedTags.concat(rule.tags);
      break;
    }
  }

  // 3. 提取数量 (如 10个, 2包, 5片, 100pcs)
  const qtyMatch = text.match(/(\d+)\s*(个|件|台|盒|袋|包|只|把|支|条|瓶|卷|盘|双|根|顶|箱|片|粒|pcs|pin)/i);
  if (qtyMatch) {
    matchedQuantity = parseInt(qtyMatch[1], 10);
    matchedUnit = qtyMatch[2];
  }

  // 4. 提取电子与工业规格 (如 10k, 100uF, 3.3V, 0805, SOP8, TO-220, Type-C)
  const specMatch = text.match(/(\d+(?:\.\d+)?\s*(?:k|m|u|n|p)?(?:Ω|ohm|uf|nf|pf|v|a|w|mah|gb|tb|g|kg|ml|l|mm|cm|m))|([0-2][0-9]{3}|to-220|to-92|sop8|dip8|ssop|qfn|tssop|sot-23|t12|c245|c210|pla|petg)/i);
  if (specMatch) {
    matchedSpec = specMatch[0];
  }

  // 5. 计算保质期到期时间
  let expireDate = '';
  if (matchedExpiryDays > 0) {
    const d = new Date();
    d.setDate(d.getDate() + matchedExpiryDays);
    expireDate = d.toISOString().split('T')[0];
  }

  return {
    name: matchedName,
    categoryName: matchedCategory,
    unit: matchedUnit,
    quantity: matchedQuantity,
    suggestedLocation: matchedLocation,
    estimatedPrice: matchedPrice,
    spec: matchedSpec,
    brand: matchedBrand,
    tags: Array.from(new Set(matchedTags)),
    expireDate: expireDate,
    description: `[微信小程序离线智能识别] 来源: ${rawText}`
  };
}

module.exports = {
  recognizeLocally
};
