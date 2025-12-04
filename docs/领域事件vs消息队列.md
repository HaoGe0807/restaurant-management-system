# 领域事件 vs 消息队列（MQ）

## 当前实现分析

### 当前架构流程

```
创建商品 → 保存到数据库 → 事件保存到 domain_events 表（事务内）
                                    ↓
                            定时任务扫描（DomainEventOutboxProcessor）
                                    ↓
                        Spring ApplicationEventPublisher（进程内）
                                    ↓
                    @TransactionalEventListener 监听（ProductCreatedEventHandler）
                                    ↓
                            创建库存（同一进程内）
```

### 当前实现的局限性

1. **进程内通信**：事件通过 Spring 的 `ApplicationEventPublisher` 发布，只能在同一个 JVM 进程内传递
2. **服务耦合**：商品服务和库存服务必须在同一个应用中
3. **无法跨服务**：如果商品服务和库存服务是独立的微服务，当前方案无法工作
4. **扩展性受限**：无法支持多个消费者、无法做负载均衡

## 为什么电商场景要用 MQ？

### 1. **微服务架构的必然选择**

在电商系统中，通常采用微服务架构：

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  商品服务   │         │  库存服务   │         │  订单服务   │
│ Product-Svc │         │ Inventory   │         │  Order-Svc  │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       └───────────────────────┼───────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   消息队列 (MQ)      │
                    │  RabbitMQ / Kafka   │
                    └─────────────────────┘
```

**问题**：如果商品服务和库存服务是独立的微服务（可能部署在不同服务器、不同数据库），进程内事件无法跨服务传递。

**解决方案**：使用 MQ，消息可以跨进程、跨服务、跨网络传递。

### 2. **可靠性保证**

#### 当前实现的问题：
- 如果事件处理器崩溃，事件可能丢失
- 虽然有重试机制，但依赖数据库轮询，效率较低
- 无法保证消息的持久化和顺序性

#### MQ 的优势：
- **消息持久化**：消息存储在 MQ 中，即使消费者宕机，消息也不会丢失
- **自动重试**：MQ 提供完善的重试机制和死信队列
- **消息确认**：消费者处理完成后确认，未确认的消息会被重新投递
- **顺序保证**：某些 MQ（如 Kafka）支持分区顺序消费

### 3. **解耦和扩展性**

#### 场景：一个事件需要多个消费者

**当前实现**：只能通过多个 `@EventListener` 监听，但都在同一进程中

**MQ 方案**：
```
商品创建事件
    ↓
  MQ Topic/Queue
    ├─→ 库存服务：创建库存
    ├─→ 搜索服务：更新搜索索引
    ├─→ 推荐服务：更新推荐算法
    └─→ 通知服务：发送通知
```

**优势**：
- 新增消费者不需要修改发布者代码
- 消费者可以独立部署、独立扩展
- 消费者故障不影响其他消费者

### 4. **削峰填谷**

电商场景特点：
- **大促期间**：商品创建量激增（如双11）
- **库存服务**：需要处理大量库存创建请求

**当前实现**：如果库存服务处理慢，会阻塞事件处理线程

**MQ 方案**：
- MQ 作为缓冲层，可以积压消息
- 库存服务按自己的处理能力消费
- 避免系统过载

### 5. **最终一致性**

分布式系统无法保证强一致性，只能保证最终一致性。

**场景**：创建商品时创建库存

**强一致性方案**（不推荐）：
```java
// 需要分布式事务（2PC、TCC），性能差、复杂度高
@Transactional
public void createProduct() {
    productService.save();  // 商品服务
    inventoryService.create(); // 库存服务（跨服务调用）
}
```

**最终一致性方案**（推荐）：
```java
// 商品服务：只保证商品创建成功
@Transactional
public void createProduct() {
    productService.save();
    eventPublisher.publish(new ProductCreatedEvent()); // 发布事件
}

// 库存服务：异步消费事件，创建库存
@RabbitListener(queues = "product.created")
public void handle(ProductCreatedEvent event) {
    inventoryService.create(event.getSkuId());
}
```

**优势**：
- 商品创建立即返回，用户体验好
- 库存创建异步处理，不阻塞主流程
- 即使库存创建失败，也可以通过补偿机制处理

## 方案对比

| 特性 | 当前实现（进程内事件） | MQ 方案 |
|------|---------------------|---------|
| **通信范围** | 同一进程内 | 跨进程、跨服务、跨网络 |
| **服务解耦** | 低（必须在同一应用） | 高（完全解耦） |
| **可靠性** | 中等（依赖数据库） | 高（MQ 持久化） |
| **扩展性** | 低（单进程） | 高（多消费者、负载均衡） |
| **性能** | 高（内存通信） | 中等（网络通信） |
| **复杂度** | 低 | 中等（需要 MQ 基础设施） |
| **适用场景** | 单体应用、同一服务内 | 微服务、分布式系统 |

## 如何升级到 MQ 方案？

### 方案 1：在 OutboxProcessor 中发送到 MQ

修改 `DomainEventOutboxProcessor`，将事件发送到 MQ 而不是 `ApplicationEventPublisher`：

```java
@Component
@RequiredArgsConstructor
public class DomainEventOutboxProcessor {
    
    private final DomainEventMapper domainEventMapper;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate; // 或 KafkaTemplate
    
    @Scheduled(fixedDelayString = "${domain-event.outbox.fixed-delay-ms:2000}")
    public void publishPendingEvents() {
        List<DomainEventEntity> pendingEvents = domainEventMapper.findPendingEvents(batchSize);
        for (DomainEventEntity entity : pendingEvents) {
            try {
                DomainEvent event = deserializeEvent(entity);
                
                // 发送到 MQ 而不是 ApplicationEventPublisher
                rabbitTemplate.convertAndSend(
                    "product.exchange",
                    "product.created",
                    event
                );
                
                domainEventMapper.markPublished(entity.getId());
            } catch (Exception ex) {
                handleFailure(entity.getId(), currentRetry, ex.getMessage());
            }
        }
    }
}
```

### 方案 2：消费者端监听 MQ

在库存服务中：

```java
@Component
@RequiredArgsConstructor
public class ProductCreatedEventHandler {
    
    private final InventoryDomainService inventoryDomainService;
    
    @RabbitListener(queues = "product.created.queue")
    public void handle(ProductCreatedEvent event) {
        // 处理库存创建
        event.getSkus().forEach(skuSnapshot -> {
            inventoryDomainService.createInventory(
                skuSnapshot.getSkuId(),
                skuSnapshot.getInitialQuantity()
            );
        });
    }
}
```

## 总结

### 当前实现适合：
- ✅ 单体应用
- ✅ 同一服务内的模块解耦
- ✅ 快速开发、简单场景

### MQ 方案适合：
- ✅ 微服务架构
- ✅ 跨服务通信
- ✅ 高可靠性要求
- ✅ 需要削峰填谷
- ✅ 需要多个消费者
- ✅ 最终一致性场景

### 建议

1. **当前阶段**：如果商品和库存还在同一服务中，当前实现已经足够
2. **未来演进**：当服务拆分后，将 `DomainEventOutboxProcessor` 改为发送到 MQ
3. **平滑迁移**：事务性发件箱模式 + MQ 的组合，既保证了可靠性，又实现了服务解耦

**关键点**：事务性发件箱模式（Outbox Pattern）是连接领域事件和 MQ 的桥梁，它确保了：
- 业务数据和事件数据在同一事务中保存
- 事件不会丢失
- 可以灵活选择事件投递方式（进程内事件 or MQ）

