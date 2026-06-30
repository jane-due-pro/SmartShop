# SmartShop 智能商城系统 - 项目报告

## 一、项目概述

SmartShop 是一个基于 Spring Boot 4.0.7 + MyBatis + Thymeleaf + Spring Security 构建的全栈商城管理系统。项目采用分层架构设计，实现了商品管理、用户认证、权限控制、分页查询、动态条件搜索和双重缓存等核心功能。

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.7 | 基础框架 |
| Spring Security | 6.x | 安全认证与授权 |
| Spring Cache + Redis | - | 双重缓存优化 |
| MyBatis | 4.0.1 | 持久层框架 |
| Thymeleaf | - | 模板引擎 |
| PageHelper | 2.1.0 | 物理分页插件 |
| MySQL | 8.x | 数据库 |
| Lombok | - | 代码简化 |
| Java | 21 | 开发语言 |

---

## 二、项目结构

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
│   │   └── EmailVerification.java
│   ├── mapper/                            # MyBatis Mapper接口
│   │   ├── UserMapper.java
│   │   ├── RoleMapper.java
│   │   ├── UserRoleMapper.java
│   │   ├── CategoryMapper.java
│   │   ├── ProductMapper.java
│   │   └── EmailVerificationMapper.java
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
│   │   └── CategoryController.java
│   ├── config/
│   │   └── SecurityConfig.java            # Spring Security配置
│   └── dto/
│       └── PageResult.java
├── src/main/resources/
│   ├── application.yml                    # 主配置文件
│   ├── application-dev.yml                # 开发环境配置
│   ├── application-prd.yml                # 生产环境配置
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

---

## 三、各阶段实现说明

### 阶段一：搭建伪单页框架与多环境配置

**实现内容：**

1. **多环境配置**
   - `application.yml`：主配置文件，激活 `dev` 环境
   - `application-dev.yml`：开发环境，数据库 localhost:3306/smart_shop
   - `application-prd.yml`：生产环境，关闭调试日志

2. **伪单页框架**
   - `Index.html`：外层固定顶部标题栏 + 左侧导航栏 + 内容区域
   - 使用 `<iframe>` 标签实现内容区域切换
   - 点击左侧菜单在同一个页面内切换内容

3. **主页设计**
   - 顶部渐变色标题栏，显示系统名称和当前用户
   - 左侧深色侧边栏，包含导航菜单
   - 使用 `sec:authorize` 实现权限菜单显示控制

### 阶段二：打通持久层与数据基础渲染

**实现内容：**

1. **实体类**（使用 Lombok @Data）
   - User：用户实体，包含 id、username、password、active、email
   - Role：角色实体
   - UserRole：用户角色关联实体
   - Category：商品分类实体
   - Product：商品实体，包含 categoryName（关联查询字段）

2. **Mapper 接口**（使用 @Select 注解）
   - UserMapper：用户增删改查，支持按用户名/邮箱查询
   - RoleMapper：角色查询，支持按用户ID查询角色列表
   - CategoryMapper：分类 CRUD
   - ProductMapper：商品 CRUD，多表联查获取分类名称

3. **多表联查**
   ```sql
   SELECT p.*, c.name AS categoryName 
   FROM product p 
   INNER JOIN category c ON p.cat_id = c.id
   ```

4. **Thymeleaf 渲染**
   - Welcome 页面展示商品列表和分类列表
   - 使用 `th:each` 循环渲染商品表格
   - 使用 `${...}` 表达式显示数据

### 阶段三：动态条件搜索与数据分页处理

**实现内容：**

1. **动态条件查询**（MyBatis 动态 SQL）
   - 按分类筛选（精确匹配）
   - 按商品名称模糊搜索（LIKE）
   - 按价格区间查询（>= 和 <=）
   - 使用 `<where>` 和 `<if>` 标签实现动态拼接

2. **物理分页**（PageHelper）
   - 在 Service 层使用 `PageHelper.startPage()`
   - 返回 `PageInfo<Product>` 封装分页数据

3. **前端分页控件**
   - 首页、上一页、下一页、尾页按钮
   - 翻页时保留查询条件（表单参数传递）

### 阶段四：安全认证与动态权限菜单

**实现内容：**

1. **自定义登录页面**
   - `login.html`：支持密码登录和邮箱验证码登录两种方式
   - Tab 切换实现不同登录方式

2. **UserDetailsService 实现**
   - `CustomUserDetailsService`：从数据库加载用户信息
   - 支持用户名或邮箱登录
   - 加载用户角色权限列表

3. **Spring Security 配置**
   - `/login`, `/register`, `/sendCode`, `/resetPassword` 等路径匿名访问
   - `/admin/**` 路径需要 `ROLE_admin` 角色
   - 其他路径需要认证

4. **权限菜单控制**
   - 使用 `sec:authorize="hasRole('admin')"` 控制菜单显示
   - 管理员可看到"分类管理"菜单
   - 普通用户隐藏管理菜单

### 阶段五：高并发场景下的双重缓存优化

**实现内容：**

1. **声明式缓存**（Spring Cache @Cacheable）
   - 启用 `@EnableCaching`
   - `CategoryServiceImpl`：
     - `@Cacheable(key = "'all'")` 缓存分类列表
     - `@CachePut` 更新时同步缓存
     - `@CacheEvict` 删除时清理缓存
   - `ProductServiceImpl`：商品单个查询缓存

2. **编程式缓存**（RedisTemplate）
   - `ProductServiceImpl.searchWithPage()`：
     - 优先查 Redis 缓存
     - 缓存未命中则查 MySQL 并写入 Redis
     - 设置 30 分钟过期时间
   - 增删改操作时清理相关缓存键

3. **缓存策略**
   - 分页查询使用 Redis 缓存（复杂查询）
   - 单个实体查询使用 Spring Cache（简单查询）
   - 控制台日志证明缓存命中

---

## 四、额外功能实现

### 4.1 用户注册

- 注册页面：`register.html`
- 支持用户名、密码、邮箱注册
- 需要邮箱验证码验证
- 默认注册为普通用户（ROLE_normal）

### 4.2 找回密码

- 找回密码页面：`resetPassword.html`
- 输入注册邮箱获取验证码
- 验证通过后设置新密码

### 4.3 邮箱验证码登录

- 登录页面的"邮箱验证码登录" Tab
- 输入邮箱获取验证码
- 验证通过后自动登录

### 4.4 邮件发送服务

- 使用 Spring Mail 发送邮件
- 支持 HTML 格式邮件
- 6位数字验证码，5分钟有效
- 异步发送，不阻塞主线程

---

## 五、数据库设计

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

---

## 六、功能测试

### 6.1 登录功能
- ✅ 密码登录：输入用户名和密码，验证通过后跳转主页
- ✅ 邮箱验证码登录：输入邮箱和验证码，验证通过后自动登录
- ✅ 错误提示：用户名或密码错误时显示提示信息

### 6.2 注册功能
- ✅ 用户注册：填写用户名、密码、邮箱、验证码
- ✅ 验证码发送：异步发送邮件，前端倒计时显示
- ✅ 用户名重复检测：注册时检查用户名是否已存在

### 6.3 找回密码
- ✅ 邮箱验证：输入邮箱获取验证码
- ✅ 密码重置：验证通过后设置新密码

### 6.4 商品管理
- ✅ 商品列表：分页展示商品信息
- ✅ 动态搜索：按分类、名称、价格区间组合查询
- ✅ 添加商品：填写商品信息后提交
- ✅ 编辑商品：修改商品信息后保存
- ✅ 删除商品：确认后删除商品

### 6.5 分类管理
- ✅ 分类列表：展示所有分类
- ✅ 添加分类：填写分类名称和描述
- ✅ 编辑分类：修改分类信息
- ✅ 删除分类：删除分类

### 6.6 权限控制
- ✅ 管理员：可访问所有功能
- ✅ 普通用户：隐藏分类管理菜单

---

## 七、运行说明

### 7.1 环境要求
- JDK 21+
- MySQL 8.x
- Redis（可选，用于分页缓存）
- Maven 3.x

### 7.2 数据库初始化
```sql
-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS smart_shop DEFAULT CHARSET utf8mb4;
USE smart_shop;

-- 2. 执行用户提供的SQL脚本创建表和插入数据

-- 3. 执行邮箱验证码表脚本
source sql/email_verification.sql
```

### 7.3 配置修改
修改 `application-dev.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_shop?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 7.4 启动项目
```bash
# 编译打包
mvn clean package -DskipTests

# 启动项目
java -jar target/SmartShop-0.0.1-SNAPSHOT.jar
```

### 7.5 访问系统
- 登录页面：http://localhost:8080/login
- 管理员账号：admin / 123456
- 普通用户账号：bob / 123456

---

## 八、总结

本项目完整实现了五个阶段的实训任务：

1. **阶段一**：搭建了伪单页框架，使用 iframe 实现内容区域切换；配置了多环境文件
2. **阶段二**：打通了 MyBatis 持久层，实现了多表联查和动态渲染
3. **阶段三**：实现了动态条件搜索和 PageHelper 物理分页
4. **阶段四**：集成了 Spring Security，实现了用户认证、授权和权限菜单控制
5. **阶段五**：实现了双重缓存策略（声明式 + 编程式），提升系统性能

额外实现了：
- 用户注册（默认普通用户角色）
- 找回密码功能
- 邮箱验证码登录

系统功能完整，代码结构清晰，符合企业级开发规范。
