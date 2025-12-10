# 餐厅管理系统

基于 DDD（领域驱动设计）架构的餐厅管理系统，采用 Spring Boot 3.1.0 构建。

## 项目结构

```
restaurant-management-system/
├── src/main/java/com/restaurant/management/
│   ├── common/              # 公共模块（领域基类、基础设施）
│   ├── product/             # 商品限界上下文
│   ├── inventory/           # 库存限界上下文
│   └── order/               # 订单限界上下文
├── src/main/resources/
│   ├── mapper/              # MyBatis XML 映射文件
│   └── sql/                 # 数据库建表脚本
└── docs/                    # 文档目录
```

## 技术栈

- **框架**: Spring Boot 3.1.0
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0+
- **缓存**: Caffeine (本地缓存) + Redis (分布式缓存)
- **消息队列**: RabbitMQ
- **API 文档**: Swagger/OpenAPI (springdoc-openapi)
- **构建工具**: Maven 3.6+
- **监控指标**: Spring Boot Actuator + Micrometer Prometheus

## 环境要求

- **JDK**: Java 17+（必需）
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（可选，用于缓存）
- RabbitMQ 3.8+（可选，用于消息队列）

### Java 版本配置

**重要**: 此项目需要 Java 17，但不会影响其他项目。

#### 方式一：使用启动脚本（推荐）

项目根目录提供了 `run-with-java17.sh` 脚本，会自动查找并使用 Java 17：

```bash
# 运行项目
./run-with-java17.sh spring-boot:run

# 编译项目
./run-with-java17.sh clean compile

# 执行其他 Maven 命令
./run-with-java17.sh test
```

#### 方式二：安装 Java 17 并配置项目级别

1. **安装 Java 17**（如果还没有安装）：
   ```bash
   # 使用 Homebrew
   brew install --cask temurin17
   
   # 或者访问 https://adoptium.net/zh-CN/temurin/releases/?version=17
   ```

2. **查找 Java 17 路径**：
   ```bash
   /usr/libexec/java_home -V
   ```

3. **配置项目使用 Java 17**：
   
   编辑 `.mvn/jvm.config` 文件，取消注释并设置正确的路径：
   ```bash
   -Djava.home=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
   ```
   
   或者编辑 `pom.xml` 中的 `maven-compiler-plugin`，取消注释 `executable` 配置。

#### 方式三：临时设置环境变量（仅当前终端会话）

```bash
# 设置 Java 17 路径
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

# 验证版本
java -version

# 运行项目
mvn spring-boot:run
```

### 数据库配置

1. **创建数据库**

```sql
CREATE DATABASE restaurant_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **执行建表脚本**

项目中的 SQL 脚本位于 `src/main/resources/sql/` 目录下：
- `product_spu_sku.sql` - 商品 SPU 和 SKU 表
- `domain_events.sql` - 领域事件表
- 其他表的 SQL 脚本（如订单表、库存表等）

按顺序执行这些 SQL 脚本创建表结构。

3. **配置数据库连接**

编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root        # 修改为你的数据库用户名
    password: root        # 修改为你的数据库密码
```

**配置说明：**
- `url`: 数据库连接地址，默认端口 3306，数据库名 `restaurant_management`
- `username`: MySQL 用户名
- `password`: MySQL 密码
- `serverTimezone`: 时区设置，建议使用 `Asia/Shanghai`

**使用不同环境配置：**

可以创建 `application-dev.yml`、`application-prod.yml` 等环境配置文件，然后在 `application.yml` 中激活：

```yaml
spring:
  profiles:
    active: dev  # 激活开发环境配置
```

### 运行项目

```bash
# 方式一：使用启动脚本（推荐）
./run-with-java17.sh spring-boot:run

# 方式二：如果已配置项目级别 Java 17
mvn spring-boot:run

# 方式三：临时设置环境变量后
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

项目启动后，默认运行在 `http://localhost:8080`

### 监控与指标

- 健康检查: `http://localhost:8080/actuator/health`
- 指标数据: `http://localhost:8080/actuator/prometheus`

示例 Prometheus 抓取配置：

```yaml
scrape_configs:
  - job_name: 'restaurant-management-system'
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['localhost:8080']
```

### API 文档（Swagger）

项目集成了 Swagger/OpenAPI，启动后可以通过以下地址访问：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 文档 JSON**: http://localhost:8080/v3/api-docs

在 Swagger UI 中可以：
- 查看所有 API 接口
- 查看请求/响应参数说明
- 在线测试 API 接口
- 导出 API 文档

### 中间件启动

#### Redis（可选）
```bash
# macOS
brew services start redis

# 或直接运行
redis-server
```

#### RabbitMQ（可选）
```bash
# macOS
brew services start rabbitmq

# 或直接运行
rabbitmq-server

# 访问管理界面（默认端口 15672）
# 用户名/密码：guest/guest
# http://localhost:15672
```

**注意**：如果不需要 Redis 或 RabbitMQ，可以在 `application.yml` 中注释相关配置，应用仍可正常启动。

## 后续规划

- [ ] 添加用户（User）限界上下文
- [ ] 添加支付（Payment）限界上下文
- [ ] 添加物流（Logistics）限界上下文
- [ ] 实现领域事件机制
- [ ] 添加单元测试和集成测试
- [ ] 完善异常处理机制
