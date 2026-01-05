# PlayerBirthdayPerks

**仿王者荣耀生日福利系统** - Minecraft 1.21.1 插件

## 📖 简介

PlayerBirthdayPerks 是一个仿照王者荣耀生日福利系统设计的 Minecraft 插件。玩家可以设置自己的生日，在生日当天登录服务器获得专属福利奖励！

## ✨ 功能特性

### 核心功能
- 🎂 **生日设置**：支持年月日完整生日设置，带GUI选择器
- 🎁 **生日福利**：生日当天可领取专属福利
- 📢 **全服广播**：生日当天登录时全服庆祝
- 🎆 **烟花效果**：领取福利时释放绚丽烟花
- 🖼️ **头像框系统**：获得限时生日专属头像框
- 🖥️ **GUI界面**：美观的图形界面操作

### 奖励类型
- 自定义物品奖励（支持自定义名称和Lore）
- 经验值奖励
- 金钱奖励（需要Vault支持）
- 命令执行奖励
- 音效效果

### 技术特性
- ⚡ **高性能**：HikariCP 连接池 + 异步数据库操作
- 💾 **双数据库支持**：SQLite 和 MySQL
- 🔒 **安全可靠**：参数化查询防SQL注入
- 📦 **智能缓存**：减少数据库压力
- 🎨 **完整配置**：所有功能均可配置
- 📱 **轻量级**：仅 ~95KB，不打包冗余依赖

## 📋 命令

主命令：`/pbp`（别名：`/birthday`、`/bd`）

### 玩家命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/pbp` | 打开GUI主菜单 | `birthday.use` |
| `/pbp set <年> <月> <日>` | 命令行设置生日 | `birthday.set` |
| `/pbp info` | 查看生日信息 | `birthday.info` |
| `/pbp claim` | 领取生日福利 | `birthday.claim` |
| `/pbp help` | 显示帮助信息 | `birthday.use` |

### 管理员命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/pbp admin gui` | 打开管理员面板 | `birthday.admin` |
| `/pbp admin reload` | 重载配置 | `birthday.admin.reload` |
| `/pbp admin reset <玩家>` | 重置玩家数据 | `birthday.admin.reset` |
| `/pbp admin give <玩家>` | 给予生日福利 | `birthday.admin.give` |
| `/pbp admin check <玩家>` | 查看玩家信息 | `birthday.admin.check` |

## 🔑 权限

| 权限节点 | 描述 | 默认 |
|----------|------|------|
| `birthday.use` | 使用基础命令 | 所有人 |
| `birthday.set` | 设置生日 | 所有人 |
| `birthday.info` | 查看信息 | 所有人 |
| `birthday.claim` | 领取福利 | 所有人 |
| `birthday.admin` | 管理员权限 | OP |
| `birthday.admin.reload` | 重载配置 | OP |
| `birthday.admin.reset` | 重置数据 | OP |
| `birthday.admin.give` | 给予福利 | OP |
| `birthday.admin.check` | 查看他人 | OP |

## ⚙️ 配置

### config.yml 主要配置

```yaml
# 数据库配置
database:
  type: sqlite  # sqlite 或 mysql
  
# 生日设置
birthday:
  allow-modify: false      # 是否允许修改生日
  modify-limit-per-year: 1 # 每年修改次数限制
  claim-window-days: 7     # 福利领取窗口期

# 福利配置
rewards:
  enabled: true
  login-notification: true
  broadcast:
    enabled: true
  firework:
    enabled: true
    amount: 3
```

### 自定义物品奖励

```yaml
rewards:
  items:
    - material: CAKE
      amount: 1
      name: "&d&l🎂 生日蛋糕"
      lore:
        - "&7专属于你的生日蛋糕"
        - "&8获得时间: %date%"
```

### 命令奖励

```yaml
rewards:
  commands:
    - "give %player% minecraft:diamond 5"
    - "eco give %player% 1000"
```

## 🛠️ 安装

1. 下载插件 jar 文件（约95KB）
2. 放入服务器 `plugins` 目录
3. 重启服务器或使用插件管理器加载
4. 编辑 `plugins/PlayerBirthdayPerks/config.yml` 进行配置
5. 使用 `/pbp admin reload` 重载配置

## 📦 依赖

- **必需**：Spigot/Paper 1.21.1+
- **可选**：Vault（金钱奖励功能）

> ⚠️ **注意**：插件使用 `compileOnly` 依赖，SQLite JDBC 和 HikariCP 已内置于 Spigot/Paper 服务器。如需使用 MySQL，请确保服务器已安装 MySQL Connector。

## 🔧 构建

```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

构建产物位于 `build/libs/PlayerBirthdayPerks-1.0.0.jar`（约95KB）

## 📁 项目结构

```
PlayerBirthdayPerks/
├── build.gradle
├── settings.gradle
├── README.md
└── src/main/
    ├── java/com/birthdayperks/
    │   ├── PlayerBirthdayPerks.java    # 主类
    │   ├── command/                     # 命令处理
    │   │   ├── BirthdayCommand.java
    │   │   └── BirthdayTabCompleter.java
    │   ├── listener/                    # 事件监听
    │   │   └── PlayerJoinListener.java
    │   ├── manager/                     # 管理器
    │   │   ├── ConfigManager.java
    │   │   ├── MessageManager.java
    │   │   ├── PlayerDataManager.java
    │   │   └── RewardManager.java
    │   ├── database/                    # 数据库层
    │   │   ├── Database.java
    │   │   ├── AbstractDatabase.java
    │   │   ├── SQLiteDatabase.java
    │   │   ├── MySQLDatabase.java
    │   │   └── DatabaseFactory.java
    │   ├── gui/                         # GUI界面
    │   │   ├── AbstractGui.java
    │   │   ├── GuiManager.java
    │   │   ├── MainMenuGui.java
    │   │   ├── SetBirthdayGui.java
    │   │   ├── BirthdayInfoGui.java
    │   │   ├── RewardPreviewGui.java
    │   │   ├── HelpGui.java
    │   │   ├── AdminGui.java
    │   │   ├── AdminPlayerListGui.java
    │   │   └── AdminPlayerDetailGui.java
    │   ├── model/                       # 数据模型
    │   │   └── PlayerData.java
    │   └── util/                        # 工具类
    │       ├── ColorUtil.java
    │       └── DateUtil.java
    └── resources/
        ├── plugin.yml
        ├── config.yml
        └── messages.yml
```

## 📝 更新日志

### v1.0.0
- 初始版本发布
- 完整的生日福利系统（支持年月日设置）
- GUI图形界面（主菜单、设置生日、信息查看、管理员面板）
- SQLite 和 MySQL 双数据库支持
- 高性能异步操作
- 完善的配置系统
- 轻量级打包（~95KB）

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
