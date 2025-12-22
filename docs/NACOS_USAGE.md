# Nacos 配置中心使用指南

## 一、Nacos 可视化控制台

### 1. 启动 Nacos 服务器

#### 方式一：Docker 启动（推荐）

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
2. 解压后进入 `bin` 目录
3. 启动：
   - **Linux/Mac**: `sh startup.sh -m standalone`
   - **Windows**: `startup.cmd -m standalone`

### 2. 访问控制台

- **访问地址**: http://localhost:8848/nacos
- **默认用户名**: `nacos`
- **默认密码**: `nacos`

登录后即可看到 Nacos 控制台界面。

## 二、在控制台中配置

### 1. 创建配置

1. **进入配置管理**
   - 左侧菜单：**配置管理** → **配置列表**

2. **创建新配置**
   - 点击右上角 **+** 按钮

3. **填写配置信息**
   - **Data ID**: `restaurant-management-system.yml`
     - 必须与 `application.yml` 中的 `spring.application.name` 一致
   - **Group**: `DEFAULT_GROUP`（默认组）
   - **配置格式**: 选择 `YAML`
   - **配置内容**: 见下方示例

4. **发布配置**
   - 点击 **发布** 按钮

### 2. 配置示例

在 Nacos 控制台的配置内容区域，粘贴以下配置：

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

### 3. 配置管理功能

Nacos 控制台提供以下功能：

- ✅ **配置查看**: 查看已发布的配置内容
- ✅ **配置编辑**: 修改配置后重新发布
- ✅ **配置历史**: 查看配置的版本历史
- ✅ **配置回滚**: 回滚到历史版本
- ✅ **配置监听**: 查看哪些应用正在监听该配置
- ✅ **配置导出/导入**: 批量管理配置

## 三、应用如何使用配置

### 1. 应用配置

在 `application.yml` 中配置 Nacos 连接信息：

```yaml
nacos:
  config:
    enabled: true  # 启用 Nacos 配置中心
    server-addr: localhost:8848  # Nacos 服务器地址
    namespace: ""  # 命名空间（可选，留空表示使用默认命名空间）
    group: DEFAULT_GROUP  # 配置组
    data-id: ${spring.application.name}.yml  # 配置文件名
```

### 2. 应用启动流程

1. 应用启动时，会连接 Nacos 服务器
2. 根据 `data-id` 和 `group` 从 Nacos 拉取配置
3. 将 Nacos 配置与本地 `application.yml` 合并
4. **Nacos 配置优先级更高**，会覆盖本地同名配置

### 3. 配置刷新

当前实现：
- 配置变更后，Nacos 会通知应用
- 应用会收到配置更新事件
- ⚠️ **注意**: 当前简化版本需要重启应用才能完全生效

## 四、不使用 Nacos（本地配置）

如果不想使用 Nacos，可以：

### 1. 禁用 Nacos

在 `application.yml` 中设置：

```yaml
nacos:
  config:
    enabled: false  # 禁用 Nacos，使用本地配置
```

### 2. 直接在 application.yml 中配置

```yaml
# 流量切分配置（本地配置）
traffic:
  split:
    order-new-logic:
      enabled: true
      percentage: 10
    product-new-feature:
      enabled: true
      percentage: 50
```

### 3. 使用环境变量

也可以通过环境变量或启动参数覆盖配置：

```bash
# 通过环境变量
export TRAFFIC_SPLIT_ORDER_NEW_LOGIC_ENABLED=true
export TRAFFIC_SPLIT_ORDER_NEW_LOGIC_PERCENTAGE=10

# 通过启动参数
java -jar app.jar --traffic.split.order-new-logic.enabled=true --traffic.split.order-new-logic.percentage=10
```

## 五、配置优先级

配置的优先级（从高到低）：

1. **启动参数** (`--key=value`)
2. **环境变量** (`KEY=VALUE`)
3. **Nacos 配置** (如果启用)
4. **application.yml** (本地配置)
5. **默认值** (代码中的默认值)

## 六、常见问题

### 1. 无法连接 Nacos

**问题**: 应用启动时报错，无法连接 Nacos

**解决**:
- 检查 Nacos 服务是否启动: `curl http://localhost:8848/nacos/`
- 检查防火墙是否开放 8848 端口
- 如果 Nacos 未启动，设置 `nacos.config.enabled=false` 使用本地配置

### 2. 配置不生效

**问题**: 在 Nacos 中修改配置后，应用没有更新

**解决**:
- 检查 `Data ID` 是否与 `spring.application.name` 一致
- 检查 `Group` 是否匹配
- 当前版本需要重启应用才能完全生效
- 查看应用日志，确认是否收到配置更新通知

### 3. 配置冲突

**问题**: Nacos 配置和本地配置冲突

**说明**:
- Nacos 配置会覆盖本地同名配置
- 如果某个配置在 Nacos 中不存在，会使用本地配置
- 建议：基础配置放在本地，动态配置放在 Nacos

## 七、最佳实践

1. **开发环境**: 使用本地配置 (`nacos.config.enabled=false`)
2. **生产环境**: 使用 Nacos 配置中心，便于统一管理
3. **配置分类**:
   - **静态配置**: 数据源、Redis 等，放在 `application.yml`
   - **动态配置**: 流量切分、功能开关等，放在 Nacos
4. **配置命名**: 使用清晰的命名空间和分组，便于管理
5. **配置备份**: 定期导出 Nacos 配置，做好备份

## 八、Nacos 控制台界面说明

### 主要功能区域

1. **配置管理**
   - 配置列表：查看所有配置
   - 历史版本：查看配置变更历史
   - 监听查询：查看配置监听情况

2. **服务管理**
   - 服务列表：查看注册的服务（如果使用服务发现）

3. **命名空间**
   - 管理不同环境的配置（dev、test、prod）

4. **权限控制**
   - 用户管理、角色管理、权限管理

### 配置操作

- **编辑**: 点击配置名称进入编辑页面
- **克隆**: 复制配置到其他命名空间或分组
- **删除**: 删除不需要的配置
- **导出**: 导出配置为文件
- **导入**: 从文件导入配置

