# SmartShop 智慧商品信息管理系统

一个基于 Spring Boot 3.3.5 + MyBatis + Thymeleaf + Spring Security 构建的全栈商城管理系统。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| Spring Security | 6.x | 安全认证与授权 |
| Spring Cache + Redis | - | 双重缓存优化 |
| MyBatis | 3.0.5 | 持久层框架 |
| Thymeleaf | - | 模板引擎 |
| PageHelper | 2.1.0 | 物理分页插件 |
| MySQL | 8.x | 数据库 |
| Lombok | - | 代码简化 |
| Java | 21 | 开发语言 |

## 项目结构

```
SmartShop/
├── src/main/java/guat/lxy/bigdata/smartshop/
│   ├── SmartShopApplication.java          # 启动类
│   ├── entity/                            # 实体类
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── UserRole.java
│   │   ├── Category.java
│   │   ├── Product.java
│   │   └── ProductComment.java
│   ├── mapper/                            # MyBatis Mapper接口
│   │   ├── UserMapper.java
│   │   ├── RoleMapper.java
│   │   ├── UserRoleMapper.java
│   │   ├── CategoryMapper.java
│   │   ├── ProductMapper.java
│   │   └── ProductCommentMapper.java
│   ├── service/                           # 业务逻辑层
│   │   ├── UserService.java
│   │   ├── CategoryService.java
│   │   ├── ProductService.java
│   │   ├── EmailService.java
│   │   └── impl/
│   │       ├── UserServiceImpl.java
│   │       ├── CategoryServiceImpl.java
│   │       ├── ProductServiceImpl.java
│   │       └── CustomUserDetailsService.java
│   ├── controller/                        # 控制器层
│   │   ├── AuthController.java
│   │   ├── IndexController.java
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── CommentController.java
│   │   ├── FavoriteController.java
│   │   └── ProfileController.java
│   ├── config/
│   │   └── SecurityConfig.java            # Spring Security配置
│   └── util/
│       └── Result.java
├── src/main/resources/
│   ├── application.yml                    # 主配置文件
│   ├── application-dev.yml                # 开发环境配置
│   ├── application-prd.yml                # 生产环境配置
│   ├── static/                            # 静态资源
│   └── templates/                         # Thymeleaf模板
│       ├── login.html
│       ├── register.html
│       ├── resetPassword.html
│       ├── Index.html
│       ├── Welcome.html
│       ├── product/
│       │   ├── list.html
│       │   ├── add.html
│       │   └── edit.html
│       └── category/
│           ├── list.html
│           ├── add.html
│           └── edit.html
└── sql/
    └── email_verification.sql
```

## 功能特性

### 核心功能
- **用户认证**：支持密码登录和邮箱验证码登录
- **用户注册**：支持用户名、密码、邮箱注册，需邮箱验证码验证
- **密码重置**：通过邮箱验证码重置密码
- **商品管理**：商品的增删改查，支持动态条件搜索
- **分类管理**：商品分类的增删改查
- **商品评论**：用户可对商品进行评论
- **商品收藏**：用户可收藏喜欢的商品
- **个人资料**：用户可查看和修改个人信息

### 技术亮点
- **双重缓存策略**：声明式缓存（Spring Cache）+ 编程式缓存（Redis），提升系统性能
- **动态条件查询**：MyBatis 动态 SQL，支持按分类、名称、价格区间组合查询
- **物理分页**：使用 PageHelper 实现高效分页
- **权限控制**：Spring Security 实现角色权限控制，管理员可访问所有功能，普通用户隐藏管理菜单
- **异步邮件发送**：使用 Spring Mail 异步发送验证码，不阻塞主线程

## 快速开始

### 环境要求
- JDK 21+
- MySQL 8.x
- Redis（可选，用于分页缓存）
- Maven 3.x

### 数据库初始化

```sql
-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS smart_shop DEFAULT CHARSET utf8mb4;
USE smart_shop;

-- 2. 执行用户提供的SQL脚本创建表和插入数据

-- 3. 执行邮箱验证码表脚本
source sql/email_verification.sql
```

### 配置修改

修改 `src/main/resources/application-dev.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_shop?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password:        # 无密码留空
      database: 0
```

### 启动项目

```bash
# 编译打包
mvn clean package -DskipTests

# 启动项目
java -jar target/SmartShop-0.0.1-SNAPSHOT.jar
```

### 访问系统

- 登录页面：http://localhost:8080/login
- 管理员账号：admin / 123456
- 普通用户账号：bob / 123456

## 数据库设计

### 用户表 (t_user)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| username | VARCHAR(200) | 登录账号 |
| password | VARCHAR(200) | 登录密码 |
| active | INT(1) | 1可用 0不可用 |
| email | VARCHAR(200) | 邮箱地址 |

### 角色表 (t_role)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| role | VARCHAR(200) | 角色名 |

### 用户角色关联表 (t_user_role)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| user_id | INT | 用户ID |
| role_id | INT | 角色ID |

### 商品分类表 (category)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| name | VARCHAR(100) | 分类名称 |
| descp | VARCHAR(500) | 分类描述 |

### 商品表 (product)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| name | VARCHAR(100) | 商品名称 |
| photo_url | VARCHAR(500) | 图片URL |
| price | DOUBLE | 商品价格 |
| descp | VARCHAR(500) | 商品描述 |
| release_date | DATE | 发布日期 |
| cat_id | INT | 分类ID |

### 邮箱验证码表 (email_verification)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| email | VARCHAR(200) | 邮箱地址 |
| code | VARCHAR(10) | 验证码 |
| expire_time | DATETIME | 过期时间 |
| used | INT(1) | 0未使用 1已使用 |

## 功能测试

### 登录功能
- ✅ 密码登录：输入用户名和密码，验证通过后跳转主页
- ✅ 邮箱验证码登录：输入邮箱和验证码，验证通过后自动登录
- ✅ 错误提示：用户名或密码错误时显示提示信息

### 注册功能
- ✅ 用户注册：填写用户名、密码、邮箱、验证码
- ✅ 验证码发送：异步发送邮件，前端倒计时显示
- ✅ 用户名重复检测：注册时检查用户名是否已存在

### 找回密码
- ✅ 邮箱验证：输入邮箱获取验证码
- ✅ 密码重置：验证通过后设置新密码

### 商品管理
- ✅ 商品列表：分页展示商品信息
- ✅ 动态搜索：按分类、名称、价格区间组合查询
- ✅ 添加商品：填写商品信息后提交
- ✅ 编辑商品：修改商品信息后保存
- ✅ 删除商品：确认后删除商品

### 分类管理
- ✅ 分类列表：展示所有分类
- ✅ 添加分类：填写分类名称和描述
- ✅ 编辑分类：修改分类信息
- ✅ 删除分类：删除分类

### 权限控制
- ✅ 管理员：可访问所有功能
- ✅ 普通用户：隐藏分类管理菜单

## 开发说明

### 项目分层
- **Controller层**：处理HTTP请求，返回视图或JSON数据
- **Service层**：业务逻辑处理，使用@Transactional注解管理事务
- **Mapper层**：数据访问层，使用MyBatis实现数据库操作
- **Entity层**：实体类，使用Lombok简化代码

### 缓存策略
- **声明式缓存**：使用Spring Cache的@Cacheable、@CachePut、@CacheEvict注解
- **编程式缓存**：使用RedisTemplate手动操作Redis缓存
- **缓存键前缀**：统一使用"smartshop:"前缀，便于管理

### 安全配置
- 公开路径：/login, /register, /sendCode, /resetPassword
- 管理员路径：/admin/** 需要ROLE_admin角色
- 其他路径：需要认证

## 相关文档

- [项目报告](REPORT.md)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [MyBatis官方文档](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Spring Security官方文档](https://spring.io/projects/spring-security)

## 许可证

本项目为课程设计项目，仅供学习参考。
