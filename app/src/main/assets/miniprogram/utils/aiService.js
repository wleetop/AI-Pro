// utils/aiService.js - 微信小程序端多模态 AI 识别服务
const storage = require('./storage.js');
const localHeuristic = require('./localHeuristic.js');

/**
 * 核心识别方法：支持 Base64 图片 / 文本识别
 * 自动适配云端大模型与离线启发式规则降级
 */
function analyzeImageOrText(options) {
  const { imageBase64, userPrompt, onProgress } = options;
  const config = storage.getApiConfig();

  return new Promise((resolve, reject) => {
    // 检查是否配置了云端大模型 API Key
    if (!config.apiKey || config.provider === 'OFFLINE_HEURISTIC') {
      // 降级使用小程序离线智能识别
      if (onProgress) onProgress('正在启动小程序离线智能知识库...');
      setTimeout(() => {
        const result = localHeuristic.recognizeLocally(userPrompt || '智能识别物品');
        resolve({
          source: 'OFFLINE_HEURISTIC',
          items: [result],
          rawSummary: '已通过小程序离线知识库完成特征匹配与分类推荐'
        });
      }, 500);
      return;
    }

    // 组织 OpenAI / 兼容大模型 API 请求体
    if (onProgress) onProgress(`正在连接云端大模型 (${config.provider})...`);

    const promptText = `你是一个专业的物品收纳与元器件资产管理专家。请分析传入的图片或文本描述，精确识别包含的物品列表。
必须以标准 JSON 数组格式返回识别结果，格式如下：
[
  {
    "name": "物品精确名称",
    "categoryName": "电子数码/电子元器件/工具五金/药品个护/食品饮料/服饰箱包/美妆护肤/日常居家/户外露营/模型收藏/母婴亲子/重要证件",
    "suggestedLocation": "推荐收纳位置(如 书房/工作室/元器件柜)",
    "quantity": 1,
    "unit": "个/包/盒/台/支/块/瓶",
    "spec": "型号规格(如 10k 0805, 100uF 25V, 500ml)",
    "brand": "品牌名称",
    "estimatedPrice": 0.0,
    "tags": ["标签1", "标签2"],
    "description": "简要功能或状态描述"
  }
]
用户附加描述: ${userPrompt || '请识别图中的所有物品'}`;

    let messages = [];
    if (imageBase64) {
      messages = [
        {
          role: 'user',
          content: [
            { type: 'text', text: promptText },
            { type: 'image_url', image_url: { url: `data:image/jpeg;base64,${imageBase64}` } }
          ]
        }
      ];
    } else {
      messages = [
        { role: 'user', content: promptText }
      ];
    }

    // 默认 OpenAI 兼容接口地址映射
    let endpoint = 'https://api.deepseek.com/chat/completions';
    if (config.provider === 'ALIYUN_BAILIAN') {
      endpoint = 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions';
    } else if (config.provider === 'TENCENT_HUNYUAN') {
      endpoint = 'https://api.hunyuan.cloud.tencent.com/v1/chat/completions';
    } else if (config.provider === 'ZHIPU_GLM') {
      endpoint = 'https://open.bigmodel.cn/api/paas/v4/chat/completions';
    } else if (config.provider === 'MOONSHOT_KIMI') {
      endpoint = 'https://api.moonshot.cn/v1/chat/completions';
    }

    wx.request({
      url: endpoint,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${config.apiKey}`
      },
      data: {
        model: config.model || 'gpt-4o-mini',
        messages: messages,
        temperature: 0.3
      },
      timeout: 30000,
      success(res) {
        if (res.statusCode === 200 && res.data && res.data.choices && res.data.choices[0]) {
          const content = res.data.choices[0].message.content;
          try {
            // 提取 JSON 块
            const jsonMatch = content.match(/\[[\s\S]*\]/);
            if (jsonMatch) {
              const parsedItems = JSON.parse(jsonMatch[0]);
              resolve({
                source: config.provider,
                items: parsedItems,
                rawSummary: content
              });
              return;
            }
          } catch (e) {
            console.error('JSON parse error:', e);
          }
          // 若解析失败，本地保底
          const fallback = localHeuristic.recognizeLocally(userPrompt || content.substring(0, 50));
          resolve({
            source: 'FALLBACK_HEURISTIC',
            items: [fallback],
            rawSummary: content
          });
        } else {
          // 云端返回错误，转为本地识别
          console.warn('AI API Error, fallback to local', res);
          const fallback = localHeuristic.recognizeLocally(userPrompt || '识别物品');
          resolve({
            source: 'FALLBACK_HEURISTIC',
            items: [fallback],
            rawSummary: `云端接口响应异常(${res.statusCode})，已自动切换本地智能引擎`
          });
        }
      },
      fail(err) {
        console.warn('Network fail, fallback to local', err);
        const fallback = localHeuristic.recognizeLocally(userPrompt || '识别物品');
        resolve({
          source: 'OFFLINE_FALLBACK',
          items: [fallback],
          rawSummary: '网络连接超时，已通过离线知识库完成识别'
        });
      }
    });
  });
}

module.exports = {
  analyzeImageOrText
};
