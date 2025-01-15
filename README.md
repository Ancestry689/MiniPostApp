# MiniPostApp
A social media app built with Android and Node.js.

## 项目简介
MiniPost是一个基于 Android 和 Node.js 的社交应用，支持用户注册、登录、发布帖子、浏览帖子、评论和点赞功能。

---

## 功能列表
- **用户模块**：
  - 用户注册
  - 用户登录
  - 个人信息管理
- **帖子模块**：
  - 发布帖子（支持文字和图片）
  - 浏览帖子
  - 评论帖子
  - 点赞帖子
- **评论模块**：
  - 发布评论
  - 查看评论
- **图片模块**：
  - 图片上传
  - 图片下载
  - 图片存储
  - 图片展示

---

## 技术栈
- **前端**：
  - Android  (Java)
  - Retrofit（网络请求）
  - Glide（图片加载）
- **后端**：
  - Node.js
  - Express（Web 框架）
  - MySQL（数据库）
  - Multer（文件上传）
- **工具**：
  - Git（版本控制）
  - Android Studio（开发工具）
  - Postman（API 测试）

---

## 运行步骤

### 1. 克隆仓库
```bash
git clone https://github.com/Ancestry689/MiniPostApp.git
cd MiniPostApp
```

### 2. 数据库配置
1. **登录 MySQL**：
```bash
mysql -u root -p
```
2. **创建数据库**：
```sql
CREATE DATABASE minipost_database;
```
3. **退出 MySQL**：
```sql
exit;
```
4. **导入表结构**：
```bash
mysql -u root -p minipost_database < database.sql
```
5. **修改后端数据库配置**：
	1. 打开 backend/db.js 文件。
	2. 修改数据库连接信息。
  ```javascript
  module.exports = {
    host: 'localhost',      // 数据库主机
    user: 'your_username',  // 数据库用户名
    password: 'your_password', // 数据库密码
    database: 'minipost_database'   // 数据库名称
  };
  ```

### 3. 启动后端
1. **进入后端目录**：
```bash
cd backend
```
2. **安装依赖**：
```bash
npm install expess
npm install multer
npm install cors
npm install mysql
npm install bcryptjs
npm install jsonwebtoken
```
3. **启动服务器**：
```bash
node app.js
```

### 4. 运行前端
1. 使用 Android Studio 打开 frontend/MiniPost 项目。
2. 连接设备或模拟器。
3. 点击“Run”按钮运行应用。

## 项目结构
```
MiniPostApp/
├── frontend/          
│   ├── MiniPost/		# Android 项目
│   │   ├── app/           # 应用代码
│   │   ├── build.gradle   # Gradle 配置文件
│   │   └── ...
├── backend/           # Node.js 项目
│   ├── app.js         # 主入口文件
│   └── db.js      # 数据库配置
├── database.sql       # 数据库脚本
└── README.md          # 项目说明
```

## 作者
### 陈杭

