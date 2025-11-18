# 餐厅管理系统技术方案文档

## 一、技术栈变更

### 1.1 持久层框架切换
- **原方案**：Spring Data JPA
- **新方案**：MyBatis-Plus
- **原因**：支持复杂SQL查询，更灵活的SQL编写能力
- **影响范围**：所有仓储层实现（Product、Inventory、Order）

### 1.2 技术细节
- 使用 MyBatis-Plus 的 `BaseMapper` 提供基础CRUD
- 使用 `LambdaQueryWrapper` 进行类型安全的查询构建
- 预留 XML Mapper 文件支持复杂SQL（`src/main/resources/mapper/`）

---

## 二、业务ID改造

### 2.1 改造背景
商品表同时存在两种ID：
- **数据库自增ID** (`id`): 数据库内部使用，不对外暴露
- **业务ID** (`productId`): 业务唯一标识，所有对外接口使用

### 2.2 改造范围

#### 2.2.1 商品模块
- **创建商品**：自动生成业务ID（格式：`PROD` + 时间戳）
- **查询商品**：使用 `productId` 作为路径参数
- **更新商品**：使用 `productId` 定位商品
- **API变更**：
  - `GET /api/products/{productId}` - 使用业务ID查询
  - `POST /api/products` - 返回响应包含 `productId`

#### 2.2.2 库存模块
- **库存表**：`productId` 字段类型改为 `VARCHAR`，存储商品业务ID
- **所有库存操作**：使用商品业务ID进行关联
- **API变更**：
  - `GET /api/inventories/product/{productId}` - 使用业务ID查询
  - `POST /api/inventories/reserve` - 请求参数使用业务ID

#### 2.2.3 订单模块
- **订单项表**：`productId` 字段类型改为 `VARCHAR`，存储商品业务ID
- **创建订单**：订单项中的 `productId` 使用业务ID
- **API变更**：
  - `POST /api/orders` - 订单项中的 `productId` 为业务ID
  - `GET /api/orders/{id}` - 返回的订单项包含商品业务ID

### 2.3 数据库变更

#### products 表
```sql
ALTER TABLE products 
  ADD COLUMN product_id VARCHAR(64) UNIQUE NOT NULL COMMENT '商品业务ID';
CREATE INDEX idx_product_id ON products(product_id);
```

#### inventories 表
```sql
ALTER TABLE inventories 
  MODIFY COLUMN product_id VARCHAR(64) NOT NULL COMMENT '商品业务ID';
```

#### order_items 表
```sql
ALTER TABLE order_items 
  MODIFY COLUMN product_id VARCHAR(64) NOT NULL COMMENT '商品业务ID';
```

---

## 三、商品业务调整

### 3.1 移除分类ID
- **变更**：商品不再关联分类，移除 `categoryId` 字段
- **影响**：创建商品接口不再需要 `categoryId` 参数

### 3.2 默认初始化库存
- **变更**：创建商品时，无论是否传入初始库存，都会自动创建库存记录
- **规则**：
  - 如果传入 `initialQuantity`，使用传入值
  - 如果未传入或传入0，默认初始化为0
- **实现方式**：使用领域事件异步创建库存（最终一致性）

### 3.3 API变更

#### 创建商品接口
**请求**：`POST /api/products`
```json
{
  "productName": "商品名称",
  "description": "商品描述",
  "price": 99.99,
  "initialQuantity": 100  // 可选，默认0
}
```

**响应**：
```json
{
  "id": 1,                    // 数据库自增ID（内部使用）
  "productId": "PROD123456",  // 业务ID（对外使用）
  "productName": "商品名称",
  "description": "商品描述",
  "price": 99.99,
  "status": "ACTIVE",
  "createTime": "2024-01-01T10:00:00"
}
```

---

## 四、API接口变更汇总

### 4.1 商品接口

| 接口 | 方法 | 变更点 |
|------|------|--------|
| 创建商品 | POST /api/products | 移除 `categoryId`，`initialQuantity` 可选（默认0） |
| 查询商品 | GET /api/products/{productId} | 路径参数改为业务ID（String类型） |
| 响应字段 | - | 新增 `productId` 字段 |

### 4.2 库存接口

| 接口 | 方法 | 变更点 |
|------|------|--------|
| 预留库存 | POST /api/inventories/reserve | `productId` 改为 String 类型 |
| 查询库存 | GET /api/inventories/product/{productId} | 路径参数改为业务ID（String类型） |

### 4.3 订单接口

| 接口 | 方法 | 变更点 |
|------|------|--------|
| 创建订单 | POST /api/orders | 订单项中的 `productId` 改为 String 类型 |
| 订单响应 | - | 订单项中的 `productId` 为业务ID（String类型） |

---

## 五、前端对接注意事项

### 5.1 商品ID类型变更
- **原类型**：`Long` (数字)
- **新类型**：`String` (字符串，格式：`PROD` + 时间戳)
- **影响**：所有涉及商品ID的字段都需要改为字符串类型

### 5.2 必改字段清单

#### 商品相关
- 商品列表/详情中的商品ID字段
- 创建订单时的商品ID
- 库存查询时的商品ID参数

#### 订单相关
- 订单详情中的商品ID显示
- 订单项中的商品ID

### 5.3 接口调用示例

#### 创建商品
```javascript
POST /api/products
{
  "productName": "测试商品",
  "description": "商品描述",
  "price": 99.99,
  "initialQuantity": 100  // 可选，不传默认为0
}
```

#### 查询商品（使用业务ID）
```javascript
GET /api/products/PROD1234567890
```

#### 创建订单（使用业务ID）
```javascript
POST /api/orders
{
  "userId": 1,
  "items": [
    {
      "productId": "PROD1234567890",  // 字符串类型
      "productName": "测试商品",
      "quantity": 2,
      "unitPrice": 99.99
    }
  ]
}
```

---

## 六、测试要点

### 6.1 功能测试
1. ✅ 创建商品时自动生成业务ID
2. ✅ 使用业务ID查询商品
3. ✅ 创建商品时自动创建库存（默认0或指定值）
4. ✅ 创建订单时使用业务ID关联商品
5. ✅ 库存操作使用业务ID

### 6.2 兼容性测试
1. ✅ 验证业务ID格式正确性（`PROD` + 时间戳）
2. ✅ 验证业务ID唯一性
3. ✅ 验证库存自动创建（同步/异步）

### 6.3 边界测试
1. ✅ 创建商品时 `initialQuantity` 为0的情况
2. ✅ 创建商品时不传 `initialQuantity` 的情况
3. ✅ 使用不存在的业务ID查询商品

---

## 七、数据库迁移脚本

```sql
-- 1. 商品表添加业务ID字段
ALTER TABLE products 
  ADD COLUMN product_id VARCHAR(64) UNIQUE NOT NULL COMMENT '商品业务ID' AFTER id;
CREATE INDEX idx_product_id ON products(product_id);

-- 2. 为现有商品生成业务ID（示例）
UPDATE products SET product_id = CONCAT('PROD', UNIX_TIMESTAMP(NOW()), id) WHERE product_id IS NULL;

-- 3. 库存表修改product_id类型
ALTER TABLE inventories 
  MODIFY COLUMN product_id VARCHAR(64) NOT NULL COMMENT '商品业务ID';

-- 4. 订单项表修改product_id类型
ALTER TABLE order_items 
  MODIFY COLUMN product_id VARCHAR(64) NOT NULL COMMENT '商品业务ID';

-- 5. 更新库存表的product_id（关联商品业务ID）
UPDATE inventories i 
  INNER JOIN products p ON i.product_id = CAST(p.id AS CHAR)
  SET i.product_id = p.product_id;

-- 6. 更新订单项表的product_id（关联商品业务ID）
UPDATE order_items oi 
  INNER JOIN products p ON oi.product_id = CAST(p.id AS CHAR)
  SET oi.product_id = p.product_id;
```

---

## 八、回滚方案

如果出现问题需要回滚：

1. **代码回滚**：回退到使用数据库自增ID的版本
2. **数据回滚**：保留 `product_id` 字段，但恢复使用 `id` 字段进行关联
3. **前端回滚**：恢复使用数字类型的商品ID

---

## 九、时间计划

- **开发完成**：已完成
- **测试时间**：待定
- **上线时间**：待定

---

## 十、联系方式

如有技术问题，请联系开发团队。

