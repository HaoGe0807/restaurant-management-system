# Nacos 配置中心与流量切分使用指南

## 重要说明

⚠️ **当前实现是简化版本**：
- 使用 Nacos Client 直接集成，不依赖 Spring Cloud Alibaba
- 配置刷新需要手动实现（当前版本已实现基础框架）
- 如需完整的动态刷新功能，建议：
  1. 使用 Spring Cloud Alibaba（需要 Spring Cloud 依赖）
  2. 或手动实现更完善的配置刷新机制
  3. 或使用 Apollo 等成熟的配置中心

✅ **流量切分功能已完整实现**，可以正常使用。

---

## 一、Nacos 配置中心

### 1. 安装 Nacos

#### 方式一：Docker 安装（推荐）

```bash
docker run -d \
  --name nacos-server \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  nacos/nacos-server:v2.3.0
```

#### 方式二：本地安装

1. 下载 Nacos：https://github.com/alibaba/nacos/releases
2. 解压并进入 `bin` 目录
3. 启动：
   - Linux/Mac: `sh startup.sh -m standalone`
   - Windows: `startup.cmd -m standalone`

### 2. 访问 Nacos 控制台

- 地址：http://localhost:8848/nacos
- 默认用户名/密码：`nacos/nacos`

### 3. 配置应用

在 Nacos 控制台中创建配置：

1. **命名空间**（可选）：创建命名空间，如 `dev`、`prod`
2. **配置管理** → **配置列表** → **+** 按钮
3. 填写配置信息：
   - **Data ID**: `restaurant-management-system.yml`（与 `application.name` 一致）
   - **Group**: `DEFAULT_GROUP`
   - **配置格式**: `YAML`
   - **配置内容**: 见下方示例

### 4. 配置示例

在 Nacos 中创建 `restaurant-management-system.yml` 配置：

```yaml
# 流量切分配置
traffic:
  split:
    # 订单新逻辑的流量切分
    order-new-logic:
      enabled: true   # 启用新逻辑
      percentage: 10  # 10% 的流量进入新逻辑
    # 商品新功能的流量切分
    product-new-feature:
      enabled: true
      percentage: 50  # 50% 的流量进入新功能
```

### 5. 应用配置

在 `application.yml` 中配置 Nacos 连接信息：

```yaml
nacos:
  config:
    enabled: true  # 设置为 false 可以禁用 Nacos（使用本地配置）
    server-addr: localhost:8848
    namespace: ""  # 如果使用命名空间，填写命名空间ID
    group: DEFAULT_GROUP
    data-id: ${spring.application.name}.yml
```

**注意**：
- 如果 `nacos.config.enabled=false`，应用将使用 `application.yml` 中的本地配置
- 流量切分功能仍然可用，只是配置来源不同

## 二、流量切分功能

### 1. 功能说明

流量切分（Traffic Split）用于实现灰度发布，可以动态调整进入新功能的流量比例，无需发布代码。

**核心特性：**
- ✅ 动态配置：在 Nacos 中修改配置即可生效（需要重启应用或实现配置刷新）
- ✅ 一致性保证：同一用户/订单总是走相同的逻辑（基于 identifier 的 hashCode）
- ✅ 灵活控制：支持 0-100% 的任意比例切分
- ✅ 功能开关：可以随时启用/禁用新功能

### 2. 使用方式

#### 方式一：使用 `shouldUseNewFeature()` 方法

```java
@Autowired
private TrafficSplitter trafficSplitter;

public void someMethod(Long userId) {
    if (trafficSplitter.shouldUseNewFeature("order-new-logic", userId)) {
        // 新逻辑
        doNewLogic();
    } else {
        // 旧逻辑
        doOldLogic();
    }
}
```

#### 方式二：使用 `split()` 方法（推荐）

```java
@Autowired
private TrafficSplitter trafficSplitter;

public Order createOrder(CreateOrderCommand command) {
    return trafficSplitter.split(
        "order-new-logic",           // 功能名称
        command.getUserId(),         // 标识符（用于保证一致性）
        () -> createOrderNewLogic(command),  // 新逻辑
        () -> createOrderOldLogic(command)   // 旧逻辑
    );
}
```

#### 方式三：无返回值版本

```java
trafficSplitter.split(
    "order-new-logic",
    userId,
    () -> {
        // 新逻辑
        log.info("使用新逻辑");
    },
    () -> {
        // 旧逻辑
        log.info("使用旧逻辑");
    }
);
```

### 3. 配置说明

在 Nacos 或 `application.yml` 中配置流量切分：

```yaml
traffic:
  split:
    {功能名称}:
      enabled: true/false    # 是否启用
      percentage: 0-100      # 流量比例（0-100）
```

**示例：**

```yaml
traffic:
  split:
    # 订单新逻辑：10% 流量
    order-new-logic:
      enabled: true
      percentage: 10
    
    # 商品新功能：50% 流量
    product-new-feature:
      enabled: true
      percentage: 50
    
    # 支付新逻辑：100% 流量（全量发布）
    payment-new-logic:
      enabled: true
      percentage: 100
```

### 4. 工作原理

1. **一致性保证**：使用 `identifier` 的 `hashCode` 取模 100，确保同一 `identifier` 总是得到相同的结果
2. **流量分配**：如果 `hashCode % 100 < percentage`，则走新逻辑，否则走旧逻辑
3. **配置加载**：应用启动时从 Nacos 或本地配置加载，配置变更需要重启应用（或实现动态刷新）

### 5. 实际示例

在 `OrderApplicationService` 中已经实现了流量切分示例：

```java
@Transactional
public Order createOrder(CreateOrderCommand command) {
    return trafficSplitter.split(
        "order-new-logic",
        command.getUserId(),
        () -> createOrderWithNewLogic(command),  // 新逻辑
        () -> createOrderWithOldLogic(command)   // 旧逻辑
    );
}
```

**使用步骤：**

1. 在 Nacos 中配置 `traffic.split.order-new-logic.enabled=true` 和 `percentage=10`
2. 重启应用（或等待配置刷新）
3. 观察日志，10% 的订单会走新逻辑
4. 如果新逻辑运行正常，逐步提高 `percentage`（如 20、50、100）
5. 如果发现问题，可以立即设置 `enabled=false` 或 `percentage=0` 回滚

### 6. 监控和调试

#### 查看功能配置

```java
TrafficSplitProperties.FeatureConfig config = 
    trafficSplitter.getFeatureConfig("order-new-logic");
System.out.println("Enabled: " + config.isEnabled());
System.out.println("Percentage: " + config.getPercentage());
```

#### 日志输出

流量切分工具会输出详细的日志：

```
DEBUG - 功能 order-new-logic 流量切分: identifier=123, hash=456, bucket=56, percentage=10, shouldUse=false
INFO  - 功能 order-new-logic 使用新逻辑: identifier=123
```

## 三、常见问题

### 1. Nacos 连接失败

**问题**：应用启动时报错，无法连接 Nacos

**解决**：
- 检查 Nacos 服务是否启动：`curl http://localhost:8848/nacos/`
- 检查 `application.yml` 中的 `nacos.config.server-addr` 配置
- 如果 Nacos 未启用，可以设置 `nacos.config.enabled=false`，使用本地配置

### 2. 配置不生效

**问题**：修改 Nacos 配置后，应用中的配置没有更新

**解决**：
- 当前版本需要重启应用才能生效
- 如需动态刷新，需要实现配置刷新机制（当前已提供基础框架）
- 或者使用 Spring Cloud Alibaba 的 `@RefreshScope`

### 3. 流量切分不准确

**问题**：实际流量比例与配置不一致

**说明**：
- 流量切分基于 `hashCode`，在大量请求下会趋于配置的比例
- 单个请求可能不准确，但整体流量会符合配置
- 同一 `identifier` 总是走相同逻辑，保证一致性

### 4. 如何回滚

**方式一**：设置 `enabled=false`
```yaml
traffic:
  split:
    order-new-logic:
      enabled: false  # 立即回滚到旧逻辑
```

**方式二**：设置 `percentage=0`
```yaml
traffic:
  split:
    order-new-logic:
      enabled: true
      percentage: 0  # 0% 流量，全部走旧逻辑
```

**注意**：修改配置后需要重启应用才能生效（或实现动态刷新）

## 四、最佳实践

1. **渐进式发布**：从 1% → 10% → 50% → 100% 逐步提高流量
2. **监控告警**：结合 Prometheus 监控新功能的错误率、响应时间等指标
3. **快速回滚**：发现问题立即设置 `enabled=false` 或 `percentage=0`，然后重启应用
4. **功能命名**：使用清晰的功能名称，如 `order-new-logic`、`product-new-feature`
5. **一致性标识**：选择合适的 `identifier`（如 `userId`、`orderId`），确保同一用户/订单的一致性
6. **本地开发**：设置 `nacos.config.enabled=false`，直接在 `application.yml` 中配置

