const express = require('express');
const cors = require('cors');
const multer = require('multer');
const app = express();

const storage = multer.memoryStorage();
const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024
  },
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('仅支持图片文件'), false);
    }
  }
});

app.use(cors());
app.use(express.json());

let user = {
  userId: 1,
  openid: 'device_1234567890',
  nickname: '测试用户',
  memberLevel: 0,
  dailyAnalysisCount: 0,
  lastAnalysisDate: new Date().toISOString().split('T')[0],
  totalAnalysis: 0,
  totalShare: 0,
  lateNightCount: 0,
  workplaceCount: 0,
  romanceCount: 0,
  points: 0,
  keys: {
    normal: 0,
    starlight: 0,
    gold: 0
  }
};

let analysisRecords = [];

const achievements = [
  { id: 1, code: 'first_decode', name: '初窥门径', description: '完成第一次对话分析', icon: '🌱' },
  { id: 2, code: 'decode_10', name: '渐入佳境', description: '累计分析10次对话', icon: '📈' },
  { id: 3, code: 'decode_50', name: '洞察秋毫', description: '累计分析50次对话', icon: '🔍' },
  { id: 4, code: 'share_1', name: '初次分享', description: '分享分析结果1次', icon: '📤' },
  { id: 5, code: 'night_owl', name: '夜猫子', description: '深夜分析10次', icon: '🦉' }
];

let userAchievements = [];

const cards = [
  { id: 1, name: '直球选手', rarity: 1, emoji: '🎯', description: '说话直接，从不拐弯抹角' },
  { id: 2, name: '暖心天使', rarity: 1, emoji: '🌈', description: '总是给人温暖和鼓励' },
  { id: 3, name: '理性思考者', rarity: 1, emoji: '🧊', description: '冷静分析，不带情绪色彩' },
  { id: 4, name: '幽默达人', rarity: 1, emoji: '😄', description: '用幽默化解尴尬气氛' },
  { id: 5, name: '倾听者', rarity: 1, emoji: '👂', description: '善于倾听，少说多听' },
  { id: 6, name: '情感翻译官', rarity: 2, emoji: '💬', description: '能精准捕捉话语背后的情绪' },
  { id: 7, name: '矛盾调解员', rarity: 2, emoji: '⚖️', description: '善于化解冲突和尴尬' },
  { id: 8, name: '读心专家', rarity: 3, emoji: '🧠', description: '轻松看穿他人内心想法' },
  { id: 9, name: '潜台词宗师', rarity: 4, emoji: '🔥', description: '能听出所有言外之意' },
  { id: 10, name: '人际大师', rarity: 4, emoji: '👑', description: '掌握所有人际交往秘诀' }
];

let userCards = [];

const mockChatExamples = [
  `A: 今天天气真好
B: 是啊
A: 我们出去走走吧
B: 随便`,
  `小红: 在吗？
小明: 嗯嗯，怎么了
小红: 周末有空吗
小明: 应该有吧...怎么
小红: 想约你看个电影
小明: 哦...行啊`,
  `老板: 方案看完了
小王: 感觉怎么样
老板: 还行吧
小王: 是需要再改改吗
老板: 你觉得呢`,
  `闺蜜: 我男朋友今天又惹我生气了
我: 怎么了？
闺蜜: 他说我今天的衣服不好看
我: 别理他，直男都这样
闺蜜: 可是我真的很喜欢那件衣服...
我: 那就穿！让他说去吧`
];

const treasures = [
  { id: 1, name: '温柔低语', category: 'mark', rarity: 1, emoji: '💭', value: 80, description: '轻柔的呢喃，传递温暖的心意' },
  { id: 2, name: '轻声挂念', category: 'mark', rarity: 1, emoji: '💝', value: 100, description: '默默的关心，藏在字里行间' },
  { id: 3, name: '善意余光', category: 'mark', rarity: 1, emoji: '✨', value: 70, description: '不经意间流露出的善意' },
  { id: 4, name: '安静陪伴', category: 'mark', rarity: 1, emoji: '🌙', value: 90, description: '无声的陪伴，胜过千言万语' },
  { id: 5, name: '淡淡温柔', category: 'mark', rarity: 1, emoji: '🌸', value: 110, description: '如同春风般的温柔' },
  { id: 6, name: '礼貌分寸', category: 'mark', rarity: 1, emoji: '🤝', value: 60, description: '恰到好处的礼貌与距离' },
  { id: 7, name: '浅浅心动', category: 'mark', rarity: 1, emoji: '💗', value: 130, description: '不经意间泛起的涟漪' },
  { id: 8, name: '日常暖意', category: 'mark', rarity: 1, emoji: '☀️', value: 150, description: '平凡日子里的温暖瞬间' },
  
  { id: 9, name: '微光释然', category: 'star', rarity: 2, emoji: '🌟', value: 350, description: '在微光中找到释然' },
  { id: 10, name: '晚风释怀', category: 'star', rarity: 2, emoji: '🌬️', value: 400, description: '晚风带走心中的烦恼' },
  { id: 11, name: '温柔自愈', category: 'star', rarity: 2, emoji: '💚', value: 380, description: '温柔地治愈内心的伤痕' },
  { id: 12, name: '静谧思绪', category: 'star', rarity: 2, emoji: '🌊', value: 420, description: '如同深海般宁静的思绪' },
  { id: 13, name: '心事微光', category: 'star', rarity: 2, emoji: '💫', value: 360, description: '心事如同微弱的星光' },
  { id: 14, name: '情绪柔光', category: 'star', rarity: 2, emoji: '🌈', value: 450, description: '温柔的光芒照亮情绪' },
  { id: 15, name: '治愈星点', category: 'star', rarity: 2, emoji: '⭐', value: 480, description: '星星点点的治愈力量' },
  { id: 16, name: '温柔底色', category: 'star', rarity: 2, emoji: '🎨', value: 600, description: '生命中温柔的底色' },
  
  { id: 17, name: '双向默契', category: 'secret', rarity: 3, emoji: '🤝', value: 1500, description: '无需言语的心灵相通' },
  { id: 18, name: '隐秘偏爱', category: 'secret', rarity: 3, emoji: '💘', value: 1800, description: '藏在心底的特别偏爱' },
  { id: 19, name: '无声懂得', category: 'secret', rarity: 3, emoji: '🤫', value: 2000, description: '沉默中彼此理解' },
  { id: 20, name: '分寸偏爱', category: 'secret', rarity: 3, emoji: '🎯', value: 1600, description: '恰到好处的偏爱' },
  { id: 21, name: '走心陪伴', category: 'secret', rarity: 3, emoji: '❤️', value: 2200, description: '用心的陪伴最珍贵' },
  { id: 22, name: '隐晦温柔', category: 'secret', rarity: 3, emoji: '🌑', value: 2400, description: '藏在暗处的温柔' },
  { id: 23, name: '心意藏绪', category: 'secret', rarity: 3, emoji: '💌', value: 2600, description: '信件中藏着的心意' },
  { id: 24, name: '深层共情', category: 'secret', rarity: 3, emoji: '🌀', value: 2800, description: '深入灵魂的共鸣' },
  
  { id: 25, name: '灵魂同频', category: 'soul', rarity: 4, emoji: '🔮', value: 6000, description: '灵魂深处的共鸣' },
  { id: 26, name: '一眼通透', category: 'soul', rarity: 4, emoji: '👁️', value: 7000, description: '一眼看透人心' },
  { id: 27, name: '真心归处', category: 'soul', rarity: 4, emoji: '🏠', value: 8000, description: '找到心灵的归宿' },
  { id: 28, name: '人间清醒', category: 'soul', rarity: 4, emoji: '💎', value: 9000, description: '保持清醒的认知' },
  { id: 29, name: '极致温柔', category: 'soul', rarity: 4, emoji: '👑', value: 10000, description: '极致的温柔力量' },
  { id: 30, name: '宿命共鸣', category: 'soul', rarity: 4, emoji: '⭐', value: 11000, description: '命中注定的相遇' },
  { id: 31, name: '心有灵犀', category: 'soul', rarity: 4, emoji: '🦋', value: 11500, description: '无需言语的默契' },
  { id: 32, name: '永恒心语', category: 'soul', rarity: 4, emoji: '💫', value: 12000, description: '永恒不变的心声' }
];

let userTreasures = [];

const keyPrices = {
  normal: 100,
  starlight: 300,
  gold: 1000
};

const treasureLogs = [];
const pointsLogs = [];

app.post('/api/user/login', (req, res) => {
  res.json({
    code: 200,
    data: {
      userId: user.userId,
      nickname: user.nickname,
      memberLevel: user.memberLevel,
      dailyAnalysisCount: user.dailyAnalysisCount,
      totalAnalysis: user.totalAnalysis,
      points: user.points,
      keys: user.keys
    }
  });
});

app.get('/api/user/stats', (req, res) => {
  res.json({
    code: 200,
    data: {
      ...user,
      totalTreasures: userTreasures.reduce((sum, t) => sum + t.quantity, 0),
      treasureProgress: userTreasures.length + '/' + treasures.length
    }
  });
});

app.post('/api/analysis/upload', upload.single('image'), (req, res) => {
  if (!req.file) {
    return res.json({
      code: 400,
      message: '请上传图片'
    });
  }
  
  setTimeout(() => {
    const randomIndex = Math.floor(Math.random() * mockChatExamples.length);
    const extractedText = mockChatExamples[randomIndex];
    
    res.json({
      code: 200,
      data: {
        text: extractedText,
        message: 'OCR解析成功'
      }
    });
  }, 1000 + Math.random() * 1000);
});

app.post('/api/analysis/decode', (req, res) => {
  const { text } = req.body;
  
  const dailyLimit = user.memberLevel === 1 ? 10 : 3;
  if (user.dailyAnalysisCount >= dailyLimit) {
    return res.json({
      code: 400,
      message: user.memberLevel === 1 ? '今日分析次数已用完' : '今日免费分析次数已用完，请开通VIP'
    });
  }
  
  user.dailyAnalysisCount++;
  user.totalAnalysis++;
  user.points += 50;
  
  pointsLogs.unshift({
    id: pointsLogs.length + 1,
    type: 'earn',
    amount: 50,
    description: '解码分析获得积分',
    createdAt: new Date().toISOString()
  });
  
  const analysisResult = {
    subtext: '这是一个测试潜台词分析结果。通过分析对话内容，我们可以发现说话者的真实意图可能与表面意思有所不同。',
    relationship: '朋友/同事',
    emotionCurve: [
      { time: 0, value: 50 },
      { time: 1, value: 65 },
      { time: 2, value: 45 },
      { time: 3, value: 70 },
      { time: 4, value: 55 }
    ],
    translations: [
      { original: '今天天气真好', subtext: '想找个话题开始聊天' },
      { original: '是啊', subtext: '礼貌性回应，等待进一步话题' },
      { original: '我们出去走走吧', subtext: '发出邀请，可能有话想说' },
      { original: '随便', subtext: '愿意但不主动，看对方安排' }
    ],
    advice: [
      '可以主动询问对方最近的情况',
      '找个安静的地方散步聊天效果更好',
      '注意观察对方的肢体语言',
      '保持轻松愉快的氛围'
    ]
  };
  
  let newCard = null;
  if (Math.random() < 0.4) {
    const rarityRoll = Math.random();
    let selectedCards;
    if (rarityRoll < 0.6) {
      selectedCards = cards.filter(c => c.rarity === 1);
    } else if (rarityRoll < 0.85) {
      selectedCards = cards.filter(c => c.rarity === 2);
    } else if (rarityRoll < 0.97) {
      selectedCards = cards.filter(c => c.rarity === 3);
    } else {
      selectedCards = cards.filter(c => c.rarity === 4);
    }
    newCard = selectedCards[Math.floor(Math.random() * selectedCards.length)];
    
    const existing = userCards.find(c => c.cardId === newCard.id);
    if (existing) {
      existing.quantity++;
    } else {
      userCards.push({
        cardId: newCard.id,
        cardName: newCard.name,
        rarity: newCard.rarity,
        emoji: newCard.emoji,
        description: newCard.description,
        quantity: 1,
        isNew: true
      });
    }
  }
  
  let newAchievements = [];
  if (user.totalAnalysis === 1 && !userAchievements.find(a => a.code === 'first_decode')) {
    const ach = achievements.find(a => a.code === 'first_decode');
    if (ach) {
      userAchievements.push({ ...ach, unlockedAt: new Date().toISOString() });
      newAchievements.push(ach);
    }
  }
  
  const record = {
    id: analysisRecords.length + 1,
    originalText: text,
    analysisResult: JSON.stringify(analysisResult),
    relationship: analysisResult.relationship,
    createdAt: new Date().toISOString()
  };
  analysisRecords.unshift(record);
  
  res.json({
    code: 200,
    data: {
      analysis: analysisResult,
      newCard: newCard ? { ...newCard, cardId: newCard.id } : null,
      newAchievements: newAchievements,
      pointsEarned: 50,
      currentPoints: user.points
    }
  });
});

app.get('/api/analysis/history', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const pageSize = parseInt(req.query.pageSize) || 10;
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  
  res.json({
    code: 200,
    data: {
      records: analysisRecords.slice(start, end),
      total: analysisRecords.length,
      page: page,
      pageSize: pageSize
    }
  });
});

app.delete('/api/analysis/history/:id', (req, res) => {
  const id = parseInt(req.params.id);
  analysisRecords = analysisRecords.filter(r => r.id !== id);
  res.json({ code: 200 });
});

app.get('/api/analysis/history/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const record = analysisRecords.find(r => r.id === id);
  if (record) {
    res.json({
      code: 200,
      data: record
    });
  } else {
    res.json({ code: 404 });
  }
});

app.post('/api/analysis/share', (req, res) => {
  user.totalShare++;
  res.json({ code: 200 });
});

app.get('/api/cards/definitions', (req, res) => {
  res.json({ code: 200, data: cards });
});

app.get('/api/cards/user', (req, res) => {
  res.json({ code: 200, data: userCards });
});

app.get('/api/achievements/definitions', (req, res) => {
  res.json({ code: 200, data: achievements });
});

app.get('/api/achievements/user', (req, res) => {
  res.json({ code: 200, data: userAchievements });
});

app.post('/api/user/reset', (req, res) => {
  user.dailyAnalysisCount = 0;
  user.totalAnalysis = 0;
  user.totalShare = 0;
  user.lateNightCount = 0;
  user.workplaceCount = 0;
  user.romanceCount = 0;
  user.points = 0;
  user.keys = { normal: 0, starlight: 0, gold: 0 };
  analysisRecords = [];
  userCards = [];
  userAchievements = [];
  userTreasures = [];
  res.json({ code: 200 });
});

app.get('/api/treasures/definitions', (req, res) => {
  res.json({ code: 200, data: treasures });
});

app.get('/api/treasures/user', (req, res) => {
  res.json({ code: 200, data: userTreasures });
});

app.post('/api/shop/buy-key', (req, res) => {
  const { keyType } = req.body;
  const price = keyPrices[keyType];
  
  if (!price) {
    return res.json({ code: 400, message: '无效的钥匙类型' });
  }
  
  if (user.points < price) {
    return res.json({ code: 400, message: '积分不足' });
  }
  
  user.points -= price;
  user.keys[keyType]++;
  
  pointsLogs.unshift({
    id: pointsLogs.length + 1,
    type: 'spend',
    amount: price,
    description: `购买${keyType === 'normal' ? '普通钥匙' : keyType === 'starlight' ? '星光钥匙' : '鎏金钥匙'}`,
    createdAt: new Date().toISOString()
  });
  
  res.json({
    code: 200,
    data: {
      points: user.points,
      keys: user.keys
    }
  });
});

app.post('/api/gacha/open-box', (req, res) => {
  const { boxType } = req.body;
  
  let keyType, rarityWeights;
  switch (boxType) {
    case 'normal':
      keyType = 'normal';
      rarityWeights = [0.65, 0.25, 0.08, 0.02];
      break;
    case 'starlight':
      keyType = 'starlight';
      rarityWeights = [0.35, 0.40, 0.18, 0.07];
      break;
    case 'gold':
      keyType = 'gold';
      const isVIP = user.memberLevel === 1;
      rarityWeights = isVIP ? [0.10, 0.20, 0.40, 0.30] : [0.15, 0.25, 0.40, 0.20];
      break;
    default:
      return res.json({ code: 400, message: '无效的宝箱类型' });
  }
  
  if (user.keys[keyType] <= 0) {
    return res.json({ code: 400, message: '钥匙不足' });
  }
  
  user.keys[keyType]--;
  
  const rand = Math.random();
  let rarity;
  let cumulative = 0;
  for (let i = 0; i < rarityWeights.length; i++) {
    cumulative += rarityWeights[i];
    if (rand < cumulative) {
      rarity = i + 1;
      break;
    }
  }
  
  const availableTreasures = treasures.filter(t => t.rarity === rarity);
  const treasure = availableTreasures[Math.floor(Math.random() * availableTreasures.length)];
  
  const existing = userTreasures.find(t => t.treasureId === treasure.id);
  if (existing) {
    existing.quantity++;
  } else {
    userTreasures.push({
      treasureId: treasure.id,
      name: treasure.name,
      category: treasure.category,
      rarity: treasure.rarity,
      emoji: treasure.emoji,
      value: treasure.value,
      description: treasure.description,
      quantity: 1,
      locked: false,
      obtainedAt: new Date().toISOString()
    });
  }
  
  treasureLogs.unshift({
    id: treasureLogs.length + 1,
    treasureId: treasure.id,
    name: treasure.name,
    rarity: treasure.rarity,
    emoji: treasure.emoji,
    boxType: boxType,
    createdAt: new Date().toISOString()
  });
  
  res.json({
    code: 200,
    data: {
      treasure: treasure,
      keys: user.keys,
      isNew: !existing
    }
  });
});

app.post('/api/treasures/sell', (req, res) => {
  const { treasureId, quantity } = req.body;
  
  const treasure = userTreasures.find(t => t.treasureId === treasureId);
  if (!treasure) {
    return res.json({ code: 400, message: '藏品不存在' });
  }
  
  if (treasure.locked) {
    return res.json({ code: 400, message: '该藏品已锁定，无法出售' });
  }
  
  if (treasure.quantity < quantity) {
    return res.json({ code: 400, message: '数量不足' });
  }
  
  const totalValue = treasure.value * quantity;
  user.points += totalValue;
  treasure.quantity -= quantity;
  
  if (treasure.quantity === 0) {
    userTreasures = userTreasures.filter(t => t.treasureId !== treasureId);
  }
  
  pointsLogs.unshift({
    id: pointsLogs.length + 1,
    type: 'earn',
    amount: totalValue,
    description: `出售${treasure.name}×${quantity}`,
    createdAt: new Date().toISOString()
  });
  
  res.json({
    code: 200,
    data: {
      points: user.points,
      soldValue: totalValue
    }
  });
});

app.post('/api/treasures/toggle-lock', (req, res) => {
  const { treasureId } = req.body;
  
  const treasure = userTreasures.find(t => t.treasureId === treasureId);
  if (!treasure) {
    return res.json({ code: 400, message: '藏品不存在' });
  }
  
  treasure.locked = !treasure.locked;
  
  res.json({
    code: 200,
    data: {
      treasureId: treasureId,
      locked: treasure.locked
    }
  });
});

app.get('/api/points/logs', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const pageSize = parseInt(req.query.pageSize) || 20;
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  
  res.json({
    code: 200,
    data: {
      logs: pointsLogs.slice(start, end),
      total: pointsLogs.length,
      page: page,
      pageSize: pageSize
    }
  });
});

app.get('/api/gacha/logs', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const pageSize = parseInt(req.query.pageSize) || 20;
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  
  res.json({
    code: 200,
    data: {
      logs: treasureLogs.slice(start, end),
      total: treasureLogs.length,
      page: page,
      pageSize: pageSize
    }
  });
});

app.post('/api/user/buy-vip', (req, res) => {
  user.memberLevel = 1;
  user.dailyAnalysisCount = 0;
  
  res.json({
    code: 200,
    data: {
      message: 'VIP开通成功',
      memberLevel: 1,
      dailyLimit: 10
    }
  });
});

app.get('/api/shop/prices', (req, res) => {
  res.json({
    code: 200,
    data: {
      keyPrices: keyPrices,
      vipPrice: 5,
      vipDailyLimit: 10,
      freeDailyLimit: 3
    }
  });
});

const PORT = 8080;
app.listen(PORT, () => {
  console.log(`Mock server running on http://localhost:${PORT}`);
});
