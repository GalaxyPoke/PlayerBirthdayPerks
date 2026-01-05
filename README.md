# PlayerBirthdayPerks

**仿王者荣耀生日福利系统** - Minecraft 1.21.1 插件

[![GitHub](https://img.shields.io/github/license/GalaxyPoke/PlayerBirthdayPerks)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)](https://www.minecraft.net/)

## 📖 简介

PlayerBirthdayPerks 是一个仿照王者荣耀生日福利系统设计的 Minecraft 插件。玩家可以设置自己的生日，在生日当天登录服务器获得专属福利奖励！

## ✨ 功能特性

### 核心功能
- 🎂 **生日设置**：支持年月日完整生日设置，带GUI选择器
- 🎁 **生日福利**：生日当天可领取专属福利（支持命令发放物品，兼容模组）
- 📢 **全服广播**：生日当天登录时全服庆祝（支持Title）
- 🎆 **烟花效果**：领取福利时释放绚丽烟花
- 💖 **粒子特效**：生日当天玩家周围显示粒子环绕
- 🖼️ **头像框系统**：获得限时生日专属头像框
- 🖥️ **可配置GUI**：所有界面均可通过YAML配置自定义
- 📋 **生日排行榜**：查看即将过生日的玩家列表
- 🌐 **多语言支持**：内置中英文，支持自定义语言

### 奖励类型
- 命令奖励（支持原版/模组物品、经济、权限等）
- 经验值奖励
- 金钱奖励（需要Vault支持）
- 烟花效果
- 音效效果
- 头像框

### 集成支持
- 🔌 **PlaceholderAPI**：提供丰富的占位符
- 💰 **Vault**：金钱奖励支持
- 🎮 **模组兼容**：通过命令支持模组物品

### 技术特性
- ⚡ **高性能**：HikariCP 连接池 + 异步数据库操作
- 💾 **双数据库支持**：SQLite 和 MySQL
- 🔒 **安全可靠**：参数化查询防SQL注入
- 📦 **智能缓存**：减少数据库压力
- 🎨 **完整配置**：所有功能均可配置
- 📱 **轻量级**：不打包冗余依赖

## 📋 命令

主命令：`/pbp`（别名：`/birthday`、`/bd`）

### 玩家命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/pbp` | 打开GUI主菜单 | `birthday.use` |
| `/pbp set <年> <月> <日>` | 命令行设置生日 | `birthday.set` |
| `/pbp info` | 查看生日信息 | `birthday.info` |
| `/pbp claim` | 领取生日福利 | `birthday.claim` |
| `/pbp list [天数]` | 查看即将过生日的玩家 | `birthday.list` |
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
| `birthday.list` | 查看生日列表 | 所有人 |
| `birthday.admin` | 管理员权限 | OP |
| `birthday.admin.reload` | 重载配置 | OP |
| `birthday.admin.reset` | 重置数据 | OP |
| `birthday.admin.give` | 给予福利 | OP |
| `birthday.admin.check` | 查看他人 | OP |

## 🔌 PlaceholderAPI 占位符

需要安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 插件

| 占位符 | 描述 |
|--------|------|
| `%birthday_date%` | 生日日期 (MM月dd日) |
| `%birthday_date_full%` | 完整日期 (yyyy年MM月dd日) |
| `%birthday_age%` | 年龄 |
| `%birthday_days_until%` | 距离生日天数 |
| `%birthday_is_today%` | 是否今天生日 (true/false) |
| `%birthday_claimed%` | 是否已领取 (true/false) |
| `%birthday_claimed_status%` | 领取状态 (已领取/未领取) |
| `%birthday_zodiac%` | 星座 |
| `%birthday_status%` | 状态文本 |
| `%birthday_has_frame%` | 是否有头像框 |
| `%birthday_frame_prefix%` | 头像框前缀 |

## ⚙️ 配置

### 配置文件结构

```
plugins/PlayerBirthdayPerks/
├── config.yml          # 基础配置
├── messages.yml        # 中文消息
├── rewards.yml         # 奖励配置
├── lang/
│   └── en_US.yml       # 英文语言
└── menu/               # GUI配置
    ├── main-menu.yml
    ├── reward-preview.yml
    ├── set-birthday.yml
    ├── birthday-info.yml
    └── help.yml
```

### rewards.yml 奖励配置

所有物品奖励通过命令发放，支持原版和模组物品：

```yaml
commands:
  # 原版物品
  item-cake:
    enabled: true
    command: "give %player% minecraft:cake 1"
    delay: 0
  
  # 模组物品
  mod-item:
    enabled: true
    command: "give %player% modid:item_name 1"
    delay: 0
  
  # 带NBT的物品
  enchanted-sword:
    enabled: true
    command: "give %player% minecraft:diamond_sword{Enchantments:[{id:sharpness,lvl:5}]} 1"
    delay: 0
  
  # 经济奖励
  eco-reward:
    enabled: false
    command: "eco give %player% 1000"
    delay: 0

# 粒子效果
particle:
  enabled: true
  type: HEART
  count: 5
  radius: 1.5
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

### v1.1.0
- ✨ 新增 PlaceholderAPI 支持（丰富的占位符）
- ✨ 新增生日排行榜功能 (`/pbp list`)
- ✨ 新增生日粒子效果（可配置）
- ✨ 新增多语言支持（中文/英文）
- ✨ 新增可配置GUI系统（menu文件夹）
- 🔧 奖励配置独立到 `rewards.yml`
- 🔧 物品奖励改为命令发放，支持模组物品
- 🔧 优化代码结构

### v1.0.0
- 初始版本发布
- 完整的生日福利系统（支持年月日设置）
- GUI图形界面（主菜单、设置生日、信息查看、管理员面板）
- SQLite 和 MySQL 双数据库支持
- 高性能异步操作
- 完善的配置系统

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 🔗 链接

- [GitHub](https://github.com/GalaxyPoke/PlayerBirthdayPerks)
- [Issues](https://github.com/GalaxyPoke/PlayerBirthdayPerks/issues)
