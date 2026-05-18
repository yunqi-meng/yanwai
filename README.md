# 言外 - 听懂TA的潜台词

一款基于AI深度分析的对话潜台词解读工具，附带游戏化的人格卡牌收集系统。

## 技术栈

### 后端
- SpringBoot 2.7
- MyBatis-Plus 3.5
- MySQL 8.0
- OkHttp3 (AI接口调用)
- FastJSON
- Knife4j/Swagger (接口文档)

### 前端
- Vue 3
- Element Plus
- Pinia (状态管理)
- ECharts (图表)
- html2canvas (截图分享)
- canvas-confetti (礼花特效)

## 项目结构

```
/workspace
├── yanwai-backend/           # SpringBoot后端项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/yanwai/
│       │   ├── YanwaiApplication.java
│       │   ├── entity/          # 实体类
│       │   ├── mapper/          # MyBatis Mapper
│       │   ├── service/          # 服务层
│       │   ├── controller/       # REST控制器
│       │   ├── dto/             # 数据传输对象
│       │   ├── config/           # 配置类
│       │   └── constant/        # 常量定义
│       └── resources/
│           ├── application.yml
│           └── sql/init.sql     # 数据库初始化脚本
│
└── yanwai-frontend/          # Vue 3前端项目
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/
        ├── stores/              # Pinia状态管理
        ├── api/                 # API封装
        ├── views/               # 页面组件
        └── assets/              # 样式资源
```

## 快速开始

### 1. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行建表脚本
source /path/to/yanwai-backend/src/main/resources/sql/init.sql
```

### 2. 配置后端

编辑 `yanwai-backend/src/main/resources/application.yml`:

```yaml
yanwai:
  ai:
    api-key: your-api-key           # 替换为你的AI API密钥
    base-url: https://api.deepseek.com  # 或其他兼容API
    model: deepseek-chat
    enable-mock: true              # 开发时开启模拟，无需真实API
```

或者通过环境变量配置：
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

API文档地址: http://localhost:8080/doc.html (Knife4j)

### 4. 启动前端

```bash
cd yanwai-frontend
npm install
npm run dev
```

前端应用将在 http://localhost:3000 启动

## 功能模块

### 核心功能
- ✅ AI对话潜台词分析
- ✅ 情绪曲线可视化
- ✅ 逐句翻译解读
- ✅ 沟通建议生成
- ✅ 模拟AI响应（开发模式）

### 游戏化系统
- ✅ 随机卡牌掉落（普通/稀有/史诗/传说）
- ✅ 会员传说概率翻倍
- ✅ 碎片收集与合成
- ✅ 成就系统
- ✅ 分享报告生成

### 用户系统
- ✅ 设备级用户识别
- ✅ 每日免费次数限制
- ✅ 用户数据统计
- ✅ 会员功能（预留）

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录/注册 |
| `/api/user/stats` | GET | 获取用户统计 |
| `/api/user/reset` | POST | 重置用户数据 |
| `/api/analysis/decode` | POST | 对话分析解码 |
| `/api/analysis/share` | POST | 分享回调 |
| `/api/analysis/history` | GET | 获取历史记录（分页） |
| `/api/analysis/history/:id` | GET | 获取历史详情 |
| `/api/analysis/history/:id` | DELETE | 删除历史记录 |
| `/api/cards/definitions` | GET | 获取卡牌定义 |
| `/api/cards/user` | GET | 获取用户卡牌 |
| `/api/cards/synthesize` | POST | 合成卡牌 |
| `/api/achievements/list` | GET | 获取所有成就 |
| `/api/achievements/user` | GET | 获取用户成就 |

## 配置说明

### AI模型配置

支持OpenAI格式的API，包括：
- DeepSeek
- 阿里通义千问
- 百度文心一言
- 智谱ChatGLM

只需修改 `application.yml` 中的 base-url 和 model 即可切换。

### 游戏概率配置

```yaml
yanwai:
  game:
    card-drop-probability: 0.3    # 30%掉落概率
    fragment-count-for-synthesis: 5  # 合成所需碎片数
    free-daily-analysis: 3        # 免费每日次数
```

## 部署建议

### 后端部署
1. 打包: `mvn clean package -DskipTests`
2. 运行: `java -jar target/yanwai-backend-1.0.0.jar`
3. 或使用Docker容器化部署

### 前端部署
1. 打包: `npm run build`
2. 将 dist 目录部署到 Nginx
3. 配置 Nginx 反向代理到后端服务

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

## 软著申请

本项目已按照《潜台词解码社交洞察游戏化软件 V1.0》进行设计：

- 源代码量：后端Java + 前端Vue 超过3000行
- 核心创新：基于对话语义分析的人格卡牌随机掉落算法
- 特色功能：成就条件动态匹配系统

## 项目改进日志

### 已完善内容

1. **添加 .gitignore 文件** - 管理 Git 忽略文件
2. **完善 MyBatis-Plus 配置**
   - 自动填充字段（createdAt, updatedAt）
   - 分页拦截器配置
3. **配置属性绑定** - 使用 @ConfigurationProperties 统一管理
4. **添加环境变量支持** - 通过 .env 文件和环境变量配置
5. **404 页面** - 完善前端路由异常处理
6. **添加工具类** - TimeUtil 等基础工具
7. **完善依赖** - 添加 commons-lang3 等实用库

### 还可进一步完善的内容

1. **单元测试** - 添加 Service 层和 Controller 层测试
2. **接口文档** - 使用 Swagger/OpenAPI 自动生成文档
3. **缓存机制** - 使用 Redis 缓存用户数据和卡牌
4. **安全加固** - 添加用户鉴权（JWT/OAuth）、请求频率限制
5. **CI/CD 配置** - GitHub Actions/GitLab CI 自动化部署
6. **数据备份** - 定期备份任务配置
7. **监控告警** - 应用健康检查和日志监控
8. **国际化** - 支持多语言切换
9. **图片上传** - OCR 识别聊天截图功能
10. **历史记录管理** - 分页查询和删除历史分析

### 开发建议

1. 生产环境请使用真实的 API Key 和数据库密码
2. 建议将敏感配置放到环境变量中
3. 可以添加 API 熔断降级机制防止接口失败
4. 考虑使用 CDN 加速前端资源加载

## License

MIT License
