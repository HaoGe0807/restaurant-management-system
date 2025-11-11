# restaurant-management-system

基于DDD（领域驱动设计）和Java技术栈的餐厅管理项目

## 项目结构

本项目采用DDD架构思想，按照限界上下文（Bounded Context）进行分包，每个限界上下文都是一个独立的大分包。

### 整体架构

```
com.restaurant.management
├── common/                    # 公共模块
│   ├── domain/               # 领域基础（值对象、实体基类等）
│   ├── infrastructure/       # 基础设施（配置、工具类等）
│   └── exception/            # 异常定义
│
├── order/                    # 订单限界上下文（大分包）
│   ├── api/                  # 接口层（Controller、DTO、Assembler）
│   ├── application/          # 应用层（Service、Command、Query）
│   ├── domain/               # 领域层（核心业务逻辑）
│   │   ├── model/            # 领域模型（Entity、ValueObject、Aggregate）
│   │   ├── repository/       # 仓储接口
│   │   ├── service/          # 领域服务
│   │   └── event/            # 领域事件
│   └── infrastructure/       # 基础设施层（Repository实现、外部服务适配）
│       ├── persistence/      # 持久化实现
│       └── external/         # 外部服务适配
│
├── product/                  # 商品限界上下文（大分包）
│   └── [同上结构]
│
├── inventory/                # 库存限界上下文（大分包）
│   └── [同上结构]
│
└── RestaurantManagementApplication.java # 应用启动类
```

### 限界上下文说明

#### 1. Order（订单上下文）
- **职责**：管理订单的创建、支付、取消等业务
- **核心聚合**：Order（订单聚合根）、OrderItem（订单项）
- **主要功能**：
  - 创建订单
  - 订单支付
  - 订单取消
  - 订单查询

#### 2. Product（商品上下文）
- **职责**：管理商品信息、商品上下架等业务
- **核心聚合**：Product（商品聚合根）
- **主要功能**：
  - 创建商品
  - 商品信息管理
  - 商品上下架
  - 商品查询

#### 3. Inventory（库存上下文）
- **职责**：管理库存的预留、扣减、释放等业务
- **核心聚合**：Inventory（库存聚合根）
- **主要功能**：
  - 库存预留
  - 库存扣减
  - 库存释放
  - 库存查询

### DDD分层说明

每个限界上下文都遵循DDD四层架构：

1. **API层（接口层）**
   - Controller：处理HTTP请求
   - DTO：数据传输对象
   - Assembler：DTO与领域对象的转换器

2. **Application层（应用层）**
   - ApplicationService：应用服务，协调领域对象
   - Command：命令对象
   - Query：查询对象

3. **Domain层（领域层）**
   - Model：领域模型（Entity、ValueObject、Aggregate）
   - Repository：仓储接口（领域层定义）
   - Service：领域服务
   - Event：领域事件

4. **Infrastructure层（基础设施层）**
   - Persistence：持久化实现（Repository实现、JPA接口）
   - External：外部服务适配

### 技术栈

- **Java 17**
- **Spring Boot 3.1.0**
- **Spring Data JPA**
- **MySQL 8.0**
- **Lombok**
- **Maven**

### 设计原则

1. **领域层独立**：领域层不依赖基础设施层
2. **聚合根**：每个聚合只有一个根实体
3. **仓储模式**：领域层定义接口，基础设施层实现
4. **领域事件**：用于限界上下文间通信
5. **应用服务**：编排领域对象，处理事务边界

### 开发规范

- 领域模型放在 `domain/model` 包下
- 仓储接口在领域层定义，实现在基础设施层
- 应用服务负责事务管理和跨聚合协调
- 使用Command对象封装业务命令
- 使用DTO进行API层的数据传输

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 运行项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

## 后续规划

- [ ] 添加用户（User）限界上下文
- [ ] 添加支付（Payment）限界上下文
- [ ] 添加物流（Logistics）限界上下文
- [ ] 实现领域事件机制
- [ ] 添加单元测试和集成测试
- [ ] 完善异常处理机制
