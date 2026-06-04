<template>
  <div class="config-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Setting /></el-icon>
        <span>系统配置</span>
      </div>
      <div class="page-desc">管理系统参数和配置</div>
    </div>
    
    <div class="content-card">
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-alert
            title="修改配置后需要重启服务才能生效"
            type="warning"
            show-icon
            :closable="false"
            class="config-alert"
          />
        </div>
        <el-button type="primary" @click="handleSave" :loading="saving" class="save-btn">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
      </div>
      
      <el-tabs v-model="activeTab" class="config-tabs">
        <el-tab-pane name="ai">
          <template #label>
            <div class="tab-label">
              <el-icon><Cpu /></el-icon>
              <span>AI配置</span>
            </div>
          </template>
          <div class="tab-content">
            <el-form :model="aiConfig" label-width="140px" class="config-form">
              <div class="form-section">
                <div class="section-title">
                  <el-icon><Connection /></el-icon>
                  <span>基础AI配置</span>
                </div>
                <el-form-item label="API Key">
                  <el-input v-model="aiConfig.apiKey" placeholder="请输入AI API Key" show-password class="form-input">
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="API地址">
                  <el-input v-model="aiConfig.baseUrl" placeholder="请输入API Base URL" class="form-input">
                    <template #prefix>
                      <el-icon><Link /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="模型名称">
                  <el-input v-model="aiConfig.model" placeholder="请输入模型名称" class="form-input">
                    <template #prefix>
                      <el-icon><Cpu /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </div>
              
              <div class="form-section">
                <div class="section-title">
                  <el-icon><View /></el-icon>
                  <span>视觉AI配置</span>
                </div>
                <el-form-item label="视觉API Key">
                  <el-input v-model="aiConfig.visionApiKey" placeholder="请输入视觉API Key" show-password class="form-input">
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="视觉API地址">
                  <el-input v-model="aiConfig.visionBaseUrl" placeholder="请输入视觉API Base URL" class="form-input">
                    <template #prefix>
                      <el-icon><Link /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="视觉模型">
                  <el-input v-model="aiConfig.visionModel" placeholder="请输入视觉模型名称" class="form-input">
                    <template #prefix>
                      <el-icon><Cpu /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </div>
              
              <div class="form-section">
                <div class="section-title">
                  <el-icon><Setting /></el-icon>
                  <span>其他设置</span>
                </div>
                <el-form-item label="Mock模式">
                  <div class="switch-wrapper">
                    <el-switch v-model="aiConfig.enableMock" active-color="#667eea" />
                    <span class="switch-desc">开启后将使用模拟AI响应，无需API Key</span>
                  </div>
                </el-form-item>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="game">
          <template #label>
            <div class="tab-label">
              <el-icon><Trophy /></el-icon>
              <span>游戏配置</span>
            </div>
          </template>
          <div class="tab-content">
            <el-form :model="gameConfig" label-width="140px" class="config-form">
              <div class="form-section">
                <div class="section-title">
                  <el-icon><Present /></el-icon>
                  <span>卡牌掉落配置</span>
                </div>
                <el-form-item label="卡牌掉落概率">
                  <div class="slider-wrapper">
                    <el-slider v-model="gameConfig.cardDropProbability" :min="0" :max="100" :format-tooltip="val => val + '%'" class="form-slider" />
                    <span class="slider-value">{{ gameConfig.cardDropProbability }}%</span>
                  </div>
                </el-form-item>
                <el-form-item label="每日免费次数">
                  <el-input-number v-model="gameConfig.freeDailyAnalysis" :min="0" :max="100" class="form-number" />
                </el-form-item>
                <el-form-item label="碎片合成数量">
                  <el-input-number v-model="gameConfig.fragmentCountForSynthesis" :min="1" :max="100" class="form-number" />
                </el-form-item>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="member">
          <template #label>
            <div class="tab-label">
              <el-icon><Medal /></el-icon>
              <span>会员配置</span>
            </div>
          </template>
          <div class="tab-content">
            <el-form :model="memberConfig" label-width="140px" class="config-form">
              <div class="form-section">
                <div class="section-title">
                  <el-icon><Star /></el-icon>
                  <span>会员权益配置</span>
                </div>
                <el-form-item label="广告时长(秒)">
                  <el-input-number v-model="memberConfig.adDuration" :min="1" :max="300" class="form-number" />
                </el-form-item>
                <el-form-item label="会员时长(小时)">
                  <el-input-number v-model="memberConfig.memberDurationHours" :min="1" :max="720" class="form-number" />
                </el-form-item>
                <el-form-item label="传说概率(普通)">
                  <div class="slider-wrapper">
                    <el-slider v-model="memberConfig.legendProbabilityNormal" :min="0" :max="100" class="form-slider" />
                    <span class="slider-value">{{ memberConfig.legendProbabilityNormal }}%</span>
                  </div>
                </el-form-item>
                <el-form-item label="传说概率(会员)">
                  <div class="slider-wrapper">
                    <el-slider v-model="memberConfig.legendProbabilityMember" :min="0" :max="100" class="form-slider" />
                    <span class="slider-value">{{ memberConfig.legendProbabilityMember }}%</span>
                  </div>
                </el-form-item>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllConfigs, updateConfig } from '@/api/system'

const activeTab = ref('ai')
const saving = ref(false)

const aiConfig = reactive({
  apiKey: '',
  baseUrl: '',
  model: '',
  visionApiKey: '',
  visionBaseUrl: '',
  visionModel: '',
  enableMock: false
})

const gameConfig = reactive({
  cardDropProbability: 30,
  freeDailyAnalysis: 3,
  fragmentCountForSynthesis: 5
})

const memberConfig = reactive({
  adDuration: 30,
  memberDurationHours: 24,
  legendProbabilityNormal: 5,
  legendProbabilityMember: 10
})

const configMapping = {
  'yanwai.ai.api-key': { config: 'aiConfig', key: 'apiKey' },
  'yanwai.ai.base-url': { config: 'aiConfig', key: 'baseUrl' },
  'yanwai.ai.model': { config: 'aiConfig', key: 'model' },
  'yanwai.ai.vision-api-key': { config: 'aiConfig', key: 'visionApiKey' },
  'yanwai.ai.vision-base-url': { config: 'aiConfig', key: 'visionBaseUrl' },
  'yanwai.ai.vision-model': { config: 'aiConfig', key: 'visionModel' },
  'yanwai.ai.enable-mock': { config: 'aiConfig', key: 'enableMock' },
  'yanwai.game.card-drop-probability': { config: 'gameConfig', key: 'cardDropProbability' },
  'yanwai.game.free-daily-analysis': { config: 'gameConfig', key: 'freeDailyAnalysis' },
  'yanwai.game.fragment-count-for-synthesis': { config: 'gameConfig', key: 'fragmentCountForSynthesis' },
  'yanwai.member.ad-duration': { config: 'memberConfig', key: 'adDuration' },
  'yanwai.member.duration-hours': { config: 'memberConfig', key: 'memberDurationHours' },
  'yanwai.member.legend-probability-normal': { config: 'memberConfig', key: 'legendProbabilityNormal' },
  'yanwai.member.legend-probability-member': { config: 'memberConfig', key: 'legendProbabilityMember' }
}

onMounted(() => {
  loadConfigs()
})

async function loadConfigs() {
  try {
    const res = await getAllConfigs()
    const configs = res.data || []
    
    configs.forEach(item => {
      const mapping = configMapping[item.configKey]
      if (mapping) {
        const configObj = mapping.config === 'aiConfig' ? aiConfig :
                         mapping.config === 'gameConfig' ? gameConfig : memberConfig
        
        let value = item.configValue
        if (value === 'true') value = true
        else if (value === 'false') value = false
        else if (!isNaN(value) && value !== '') value = Number(value)
        
        configObj[mapping.key] = value
      }
    })
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

async function handleSave() {
  saving.value = true
  try {
    const configs = []
    
    Object.entries({
      'yanwai.ai.api-key': aiConfig.apiKey,
      'yanwai.ai.base-url': aiConfig.baseUrl,
      'yanwai.ai.model': aiConfig.model,
      'yanwai.ai.vision-api-key': aiConfig.visionApiKey,
      'yanwai.ai.vision-base-url': aiConfig.visionBaseUrl,
      'yanwai.ai.vision-model': aiConfig.visionModel,
      'yanwai.ai.enable-mock': String(aiConfig.enableMock)
    }).forEach(([key, value]) => {
      configs.push(updateConfig({ key, value: String(value) }))
    })
    
    Object.entries({
      'yanwai.game.card-drop-probability': String(gameConfig.cardDropProbability),
      'yanwai.game.free-daily-analysis': String(gameConfig.freeDailyAnalysis),
      'yanwai.game.fragment-count-for-synthesis': String(gameConfig.fragmentCountForSynthesis)
    }).forEach(([key, value]) => {
      configs.push(updateConfig({ key, value }))
    })
    
    Object.entries({
      'yanwai.member.ad-duration': String(memberConfig.adDuration),
      'yanwai.member.duration-hours': String(memberConfig.memberDurationHours),
      'yanwai.member.legend-probability-normal': String(memberConfig.legendProbabilityNormal),
      'yanwai.member.legend-probability-member': String(memberConfig.legendProbabilityMember)
    }).forEach(([key, value]) => {
      configs.push(updateConfig({ key, value }))
    })
    
    await Promise.all(configs)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.config-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.page-title .el-icon {
  color: #667eea;
  font-size: 28px;
}

.page-desc {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.toolbar-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  flex: 1;
}

.config-alert {
  border-radius: 10px;
  border: none;
  background: #fff7e6;
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 600;
}

.save-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.config-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.config-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: #f0f0f0;
}

.config-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

.config-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  font-weight: 600;
}

.config-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #667eea, #764ba2);
  height: 3px;
  border-radius: 2px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-content {
  padding: 0 20px;
}

.config-form {
  max-width: 700px;
}

.form-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #f5f7fa;
  border-radius: 12px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.section-title .el-icon {
  color: #667eea;
  font-size: 20px;
}

.form-input {
  max-width: 500px;
}

.form-number {
  width: 200px;
}

.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-desc {
  font-size: 13px;
  color: #999;
}

.slider-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  max-width: 500px;
}

.form-slider {
  flex: 1;
}

.slider-value {
  min-width: 50px;
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
  text-align: right;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

:deep(.el-slider__runway) {
  height: 8px;
  border-radius: 4px;
  background: #e8e8e8;
}

:deep(.el-slider__bar) {
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

:deep(.el-slider__button) {
  width: 20px;
  height: 20px;
  border: 3px solid #667eea;
  background: #fff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

:deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-color: transparent;
}
</style>
