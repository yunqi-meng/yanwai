# 言外 - 听懂TA的潜台词

一款基于 AI 深度分析的对话潜台词解读工具，附带游戏化的人格卡牌收集系统和完整的管理后台。

## 技术栈

### 后端
- Java 22
- SpringBoot 3.2.4
- MyBatis-Plus 3.5.5
- MySQL 8.0 / H2（开发模式）
- OkHttp3 4.12.0（AI API 调用）
- FastJSON 2.0.43
- Knife4j 4.4.0（API 文档）
- Spring Retry（AI 接口重试 + 熔断）

### 前端
- **Web 前端**：Vue 3 + Vite 5 + Element Plus + Pinia + ECharts
- **管理后台**：Vue 3 + Vite 5 + Element Plus + ECharts
- **鸿蒙前端**：ArkTS + ArkUI

## 项目结构

```
/workspace
├── yanwai-backend/           # SpringBoot 后端项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/yanwai/
│       │   ├── YanwaiApplication.java
│       │   ├── entity/          # 实体类（9个）
│       │   ├── mapper/          # MyBatis Mapper（9个）
│       │   ├── service/         # 服务层（5个接口，6个实现）
│       │   ├── controller/      # REST 控制器（4个）
│       │   ├── dto/             # 数据传输对象（8个）
│       │   ├── config/          # 配置类（6个）
│       │   ├── constant/        # 常量定义（2个）
│       │   └── util/            # 工具类（1个）
│       └── resources/
│           ├── application.yml
│           └── sql/init.sql     # 数据库初始化脚本
│
├── yanwai-frontend/          # Vue 3 Web 前端项目
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/
│       ├── stores/              # Pinia 状态管理
│       ├── api/                 # API 封装
│       ├── views/               # 页面组件（7个）
│       └── assets/              # 样式资源
│
├── yanwai-admin/             # Vue 3 管理后台项目
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/
│       ├── stores/              # Pinia 状态管理
│       ├── api/                 # API 封装
│       ├── components/          # 公共组件
│       └── views/               # 页面组件（8个）
│
└── hongmeng-frontend/         # 鸿蒙 ArkTS 前端项目
    ├── entry/
    │   └── src/main/ets/
    │       ├── api/             # API 模块（4个）
    │       ├── pages/           # 页面（7个）
    │       ├── store/           # 状态管理
    │       ├── utils/           # 工具类
    │       └── entryability/    # 入口Ability
    └── AppScope/
```

## 快速开始

### 1. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source yanwai-backend/src/main/resources/sql/init.sql
```

### 2. 配置后端

编辑 `yanwai-backend/src/main/resources/application.yml`:

```yaml
yanwai:
  ai:
    api-key: your-api-key           # 替换为你的 AI API 密钥
    base-url: https://api.deepseek.com  # 或其他兼容 API
    model: deepseek-chat
    enable-mock: true              # 开发时开启模拟，无需真实 API
```

或通过环境变量配置：
```bash
export AI_API_KEY=your-api-key
export AI_ENABLE_MOCK=true
```

### 3. 启动后端

```bash
cd yanwai-backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

API 文档地址: http://localhost:8080/doc.html (Knife4j)

### 4. 启动 Web 前端

```bash
cd yanwai-frontend
npm install
npm run dev
```

前端应用将在 http://localhost:3000 启动

### 5. 启动管理后台（可选）

```bash
cd yanwai-admin
npm install
npm run dev
```

管理后台将在 http://localhost:3001 启动

默认管理员账号：admin / 123456

## 功能模块

### 核心功能
- ✅ AI 对话潜台词分析
- ✅ 情绪曲线可视化（ECharts）
- ✅ 逐句翻译解读
- ✅ 沟通建议生成
- ✅ 视觉 AI 图片分析（OCR 识别对话截图）
- ✅ 模拟 AI 响应（开发模式，无需 API Key）

### 游戏化系统
- ✅ 随机卡牌掉落（普通/稀有/史诗/传说，共 70 张）
- ✅ 卡牌碎片收集与合成
- ✅ 成就系统（23 个成就，8 个类别）
- ✅ 会员传说概率翻倍（5% → 10%）
- ✅ 分享报告生成（html2canvas 截图）

### 用户系统
- ✅ 设备级用户识别
- ✅ 每日免费次数限制（3 次/天）
- ✅ 广告开通会员（24 小时会员）
- ✅ 用户数据统计与行为画像
- ✅ 历史记录管理

### 管理后台
- ✅ 管理员登录认证
- ✅ 数据看板（用户趋势、分析趋势、热门分析）
- ✅ 用户管理（列表、详情、封禁/解封、会员管理）
- ✅ 卡牌管理（增删改查）
- ✅ 成就管理（增删改查）
- ✅ 分析记录管理
- ✅ 系统配置管理
- ✅ 操作日志审计

## API 接口

### 用户模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录/注册 |
| `/api/user/stats` | GET | 获取用户统计 |
| `/api/user/reset` | POST | 重置用户数据 |
| `/api/user/can-watch-ad` | GET | 检查可否观看广告 |
| `/api/user/watch-ad-complete` | POST | 广告观看完成回调 |

### 分析模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/analysis/decode` | POST | 对话分析解码 |
| `/api/analysis/share` | POST | 分享回调 |
| `/api/analysis/history` | GET | 获取历史记录（分页） |
| `/api/analysis/history/:id` | GET | 获取历史详情 |
| `/api/analysis/history/:id` | DELETE | 删除历史记录 |

### 游戏模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/cards/definitions` | GET | 获取卡牌定义 |
| `/api/cards/user` | GET | 获取用户卡牌 |
| `/api/achievements/list` | GET | 获取所有成就 |
| `/api/achievements/user` | GET | 获取用户成就 |

### 管理后台模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/login` | POST | 管理员登录 |
| `/api/admin/info` | GET | 获取管理员信息 |
| `/api/admin/dashboard/overview` | GET | 数据看板概览 |
| `/api/admin/dashboard/trend` | GET | 趋势数据 |
| `/api/admin/users` | GET | 用户列表 |
| `/api/admin/users/{id}` | GET/PUT | 用户详情/更新 |
| `/api/admin/users/{id}/ban` | POST | 封禁用户 |
| `/api/admin/users/{id}/unban` | POST | 解封用户 |
| `/api/admin/cards` | GET | 卡牌列表 |
| `/api/admin/achievements` | GET | 成就列表 |
| `/api/admin/analysis` | GET | 分析记录列表 |
| `/api/admin/configs` | GET | 系统配置列表 |
| `/api/admin/logs` | GET | 操作日志列表 |

## 配置说明

### AI 模型配置

支持 OpenAI 格式的 API，包括：
- DeepSeek（默认）
- 阿里通义千问
- 百度文心一言
- 智谱 ChatGLM

只需修改 `application.yml` 中的 base-url 和 model 即可切换。

### 视觉 AI 配置

支持图片分析功能，通过视觉 AI 模型识别对话图片内容：
- 配置项：vision-base-url、vision-api-key、vision-model、vision-url
- 默认使用豆包视觉模型 doubao-seed-1-6-vision-250815

### 游戏概率配置

```yaml
yanwai:
  game:
    card-drop-probability: 0.3    # 30% 掉落概率
    fragment-count-for-synthesis: 5  # 合成所需碎片数
    free-daily-analysis: 3        # 免费每日次数
```

### 稀有度概率

| 稀有度 | 普通用户 | 会员用户 |
|--------|---------|---------|
| 普通   | 60%     | 50%     |
| 稀有   | 25%     | 25%     |
| 史诗   | 10%     | 15%     |
| 传说   | 5%      | 10%     |

## 部署方案

### Docker Compose（推荐）

```bash
docker-compose up -d
```

三个容器编排：
- `yanwai-mysql`：MySQL 8.0 + 自动执行 init.sql
- `yanwai-backend`：SpringBoot 应用，连接 MySQL
- `yanwai-frontend`：Nginx 托管 Vue 产物，反向代理 /api 到后端

### 手动部署

**后端**：
```bash
cd yanwai-backend
mvn clean package -DskipTests
java -jar target/yanwai-backend-1.0.0.jar
```

**前端**：
```bash
cd yanwai-frontend
npm install
npm run build
# dist/ 部署到 Nginx
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/yanwai-frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 数据库设计

共 **9 张业务表**，均使用 InnoDB + utf8mb4，MyBatis-Plus 统一管理逻辑删除。

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 用户表 | openid, member_level, daily_analysis_count, last_analysis_date, total_analysis, login_days, legend_count |
| `analysis_record` | 分析记录表 | original_text, analysis_result(JSON), relationship, emotion_curve(JSON) |
| `personality_card_def` | 卡牌定义表 | name, rarity(1-4), emoji, description, personality_type |
| `user_card` | 用户卡牌表 | user_id, card_id, quantity, is_new |
| `user_card_fragment` | 用户碎片表 | user_id, card_id, fragment_count |
| `achievement_def` | 成就定义表 | code, name, condition_field, condition_op, condition_value |
| `user_achievement` | 用户成就表 | user_id, achievement_id, unlocked_at |
| `admin` | 管理员表 | username, password(MD5), role, status |
| `system_config` | 系统配置表 | config_key, config_value, description |
| `operation_log` | 操作日志表 | admin_id, operation, method, params, ip |

## 项目亮点

1. **AI + 游戏化深度融合**：每次分析不仅是功能消费，更是一次"抽卡"体验
2. **动态成就匹配系统**：成就条件通过数据库字段化配置，新增成就无需改代码
3. **广告变现闭环**：观看广告 → 24h 会员 → 无限分析 + 传说概率翻倍
4. **行为画像追踪**：深夜/职场/情感三类场景自动归类，连续登录统计，传说卡牌收集追踪
5. **视觉 AI 支持**：支持上传图片进行对话分析（视觉模型识别图片内容）
6. **AI 接口可替换**：标准 OpenAI 格式，一行配置切换不同 AI 提供商
7. **多端支持**：Web 前端 + 管理后台 + 鸿蒙原生应用
8. **完整管理后台**：数据看板、用户管理、内容管理、系统配置、操作日志
9. **一键部署**：Docker Compose 三容器编排
10. **Mock 开发模式**：无 API Key 也能完整体验全部功能

## 软著申请

本项目已按照《潜台词解码社交洞察游戏化软件 V1.0》进行设计：

- 源代码量：后端 Java + 前端 Vue + 管理后台 + 鸿蒙前端 超过 8000 行
- 核心创新：基于对话语义分析的人格卡牌随机掉落算法
- 特色功能：成就条件动态匹配系统、视觉 AI 图片识别、管理后台

## 待完善方向

| 优先级 | 内容 | 说明 |
|-------|------|------|
| 高 | 单元测试 + 集成测试 | 当前覆盖率不足 |
| 高 | 安全加固 | JWT 鉴权 + 请求频率限制 |
| 中 | Redis 缓存 | 用户数据 + 卡牌定义缓存 |
| 中 | CI/CD 自动化 | GitHub Actions 自动构建/部署 |
| 中 | 图片 OCR 完善 | 后端可接 OCR 服务 |
| 低 | 监控告警 | Actuator + Prometheus + Grafana |
| 低 | 数据备份 | 定期 MySQL 备份策略 |

## 常用命令速查

```bash
# 后端开发启动
cd yanwai-backend && mvn spring-boot:run

# 前端开发启动
cd yanwai-frontend && npm run dev

# 管理后台启动
cd yanwai-admin && npm run dev

# API 文档
# 访问 http://localhost:8080/doc.html

# Docker 一键部署
docker-compose up -d

# 后端打包
cd yanwai-backend && mvn clean package -DskipTests

# 前端打包
cd yanwai-frontend && npm run build

# 管理后台打包
cd yanwai-admin && npm run build
```

## License

MIT License
