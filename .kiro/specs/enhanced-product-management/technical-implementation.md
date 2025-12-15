# 库存单据体系技术实现文档

## 技术架构概述

库存单据体系采用领域驱动设计（DDD）架构，结合事件驱动架构（EDA）实现商品与库存的深度集成。系统使用Spring Boot 3.2.0作为基础框架，集成MyBatis-Plus进行数据持久化，Redis提供缓存支持，RabbitMQ处理异步消息。

## 核心技术组件

### 1. 领域模型设计

#### 单据聚合根实现

```java
/**
 * 库存单据聚合根
 * 负责管理单据的完整生命周期和业务规则
 */
@Entity
@Table(name = "stock_document")
@Data
@EqualsAndHashCode(callSuper = true)
public class StockDocument extends AggregateRoot {
    
    @Id
    @Column(name = "document_id")
    private String documentId;
    
    @Column(name = "document_no")
    private String documentNo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_status")
    private DocumentStatus status;
    
    @Column(name = "warehouse_id")
    private String warehouseId;
    
    @Column(name = "operator_id")
    private String operatorId;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @Column(name = "remark")
    private String remark;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "approve_time")
    private LocalDateTime approveTime;
    
    @Column(name = "execute_time")
    private LocalDateTime executeTime;
    
    // 单据明细（不持久化到数据库，通过Repository加载）
    @Transient
    private List<StockDocumentItem> items = new ArrayList<>();
    
    // 审批记录（不持久化到数据库，通过Repository加载）
    @Transient
    private List<DocumentApproval> approvals = new ArrayList<>();
    
    /**
     * 创建库存单据
     */
    public static StockDocument create(CreateStockDocumentCommand command) {
        StockDocument document = new StockDocument();
        document.documentId = generateDocumentId();
        document.documentNo = generateDocumentNo(command.getType(), command.getWarehouseId());
        document.type = command.getType();
        document.status = DocumentStatus.DRAFT;
        document.warehouseId = command.getWarehouseId();
        document.operatorId = command.getOperatorId();
        document.remark = command.getRemark();
        document.createTime = LocalDateTime.now();
        
        // 设置单据明细
        document.setItems(command.getItems().stream()
            .map(item -> StockDocumentItem.create(document.documentId, item))
            .collect(Collectors.toList()));
        
        // 计算总金额
        document.calculateTotalAmount();
        
        // 发布单据创建事件
        document.addDomainEvent(new StockDocumentCreatedEvent(document.documentId, document.type));
        
        return document;
    }
    
    /**
     * 提交审核
     */
    public void submitForApproval() {
        if (status != DocumentStatus.DRAFT) {
            throw new InvalidDocumentStatusException("只有草稿状态的单据才能提交审核");
        }
        
        // 验证单据完整性
        validateDocumentCompleteness();
        
        // 验证业务规则
        validateBusinessRules();
        
        // 更新状态
        this.status = DocumentStatus.PENDING;
        
        // 发布提交审核事件
        addDomainEvent(new StockDocumentSubmittedEvent(this.documentId, this.type, this.totalAmount));
    }
    
    /**
     * 审核通过
     */
    public void approve(String approverId, String approvalComment) {
        if (status != DocumentStatus.PENDING) {
            throw new InvalidDocumentStatusException("只有待审核状态的单据才能审核");
        }
        
        // 验证审核权限
        validateApprovalAuthority(approverId);
        
        // 记录审核信息
        DocumentApproval approval = DocumentApproval.create(this.documentId, approverId, 
            ApprovalResult.APPROVED, approvalComment);
        this.approvals.add(approval);
        
        // 更新状态
        this.status = DocumentStatus.APPROVED;
        this.approveTime = LocalDateTime.now();
        
        // 发布审核通过事件
        addDomainEvent(new StockDocumentApprovedEvent(this.documentId, this.type, approverId));
    }
    
    /**
     * 执行单据
     */
    public void execute() {
        if (status != DocumentStatus.APPROVED) {
            throw new InvalidDocumentStatusException("只有已审核状态的单据才能执行");
        }
        
        // 验证执行条件
        validateExecutionConditions();
        
        // 更新状态
        this.status = DocumentStatus.EXECUTED;
        this.executeTime = LocalDateTime.now();
        
        // 发布单据执行事件
        addDomainEvent(new StockDocumentExecutedEvent(this.documentId, this.type, this.items));
    }
    
    /**
     * 取消单据
     */
    public void cancel(String cancelReason) {
        if (status == DocumentStatus.EXECUTED) {
            throw new InvalidDocumentStatusException("已执行的单据不能取消");
        }
        
        this.status = DocumentStatus.CANCELLED;
        this.remark = cancelReason;
        
        // 发布单据取消事件
        addDomainEvent(new StockDocumentCancelledEvent(this.documentId, this.type, cancelReason));
    }
    
    /**
     * 计算总金额
     */
    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 验证单据完整性
     */
    private void validateDocumentCompleteness() {
        if (items == null || items.isEmpty()) {
            throw new DocumentValidationException("单据明细不能为空");
        }
        
        for (StockDocumentItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new DocumentValidationException("商品数量必须大于0");
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new DocumentValidationException("商品单价不能为负数");
            }
        }
    }
    
    /**
     * 验证业务规则
     */
    private void validateBusinessRules() {
        switch (type) {
            case OUTBOUND_SALE:
            case OUTBOUND_PRODUCTION:
            case OUTBOUND_TRANSFER:
                validateOutboundRules();
                break;
            case INBOUND_PURCHASE:
            case INBOUND_PRODUCTION:
            case INBOUND_RETURN:
                validateInboundRules();
                break;
            default:
                // 其他类型的验证规则
                break;
        }
    }
    
    /**
     * 验证出库规则
     */
    private void validateOutboundRules() {
        // 验证库存充足性
        for (StockDocumentItem item : items) {
            // 这里需要调用库存服务验证
            // 为了保持聚合根的纯净性，实际验证逻辑在领域服务中实现
        }
    }
    
    /**
     * 验证入库规则
     */
    private void validateInboundRules() {
        // 验证入库相关业务规则
        // 例如：采购入库需要验证采购订单
    }
    
    /**
     * 验证审核权限
     */
    private void validateApprovalAuthority(String approverId) {
        // 权限验证逻辑
        // 实际实现中会调用权限服务
    }
    
    /**
     * 验证执行条件
     */
    private void validateExecutionConditions() {
        // 验证执行条件
        // 例如：库存状态、系统状态等
    }
    
    /**
     * 生成单据ID
     */
    private static String generateDocumentId() {
        return "DOC" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    /**
     * 生成单据编号
     */
    private static String generateDocumentNo(DocumentType type, String warehouseId) {
        String typeCode = getTypeCode(type);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = generateSequence(type, warehouseId, dateStr);
        return String.format("%s-%s-%s-%s", typeCode, warehouseId, dateStr, sequence);
    }
    
    /**
     * 获取单据类型代码
     */
    private static String getTypeCode(DocumentType type) {
        switch (type) {
            case INBOUND_PURCHASE: return "PI";
            case INBOUND_PRODUCTION: return "MI";
            case INBOUND_RETURN: return "RI";
            case OUTBOUND_SALE: return "SO";
            case OUTBOUND_PRODUCTION: return "MO";
            case OUTBOUND_TRANSFER: return "TO";
            case TRANSFER: return "TR";
            case ADJUSTMENT: return "AD";
            default: return "OT";
        }
    }
    
    /**
     * 生成序列号
     */
    private static String generateSequence(DocumentType type, String warehouseId, String dateStr) {
        // 实际实现中会使用Redis或数据库序列
        // 这里简化处理
        return String.format("%04d", System.currentTimeMillis() % 10000);
    }
}
```

#### 单据明细实体实现

```java
/**
 * 库存单据明细实体
 */
@Entity
@Table(name = "stock_document_item")
@Data
@EqualsAndHashCode(callSuper = true)
public class StockDocumentItem extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "document_id")
    private String documentId;
    
    @Column(name = "sku_id")
    private String skuId;
    
    @Column(name = "sku_name")
    private String skuName;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(name = "batch_no")
    private String batchNo;
    
    @Column(name = "production_date")
    private LocalDate productionDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "remark")
    private String remark;
    
    /**
     * 创建单据明细
     */
    public static StockDocumentItem create(String documentId, CreateStockDocumentItemCommand command) {
        StockDocumentItem item = new StockDocumentItem();
        item.documentId = documentId;
        item.skuId = command.getSkuId();
        item.skuName = command.getSkuName();
        item.quantity = command.getQuantity();
        item.unitPrice = command.getUnitPrice();
        item.batchNo = command.getBatchNo();
        item.productionDate = command.getProductionDate();
        item.expiryDate = command.getExpiryDate();
        item.remark = command.getRemark();
        return item;
    }
    
    /**
     * 计算明细金额
     */
    public BigDecimal calculateAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

### 2. 领域服务实现

#### 单据领域服务

```java
/**
 * 库存单据领域服务
 * 处理复杂的业务逻辑和跨聚合的操作
 */
@Service
@RequiredArgsConstructor
public class StockDocumentDomainService {
    
    private final StockDocumentRepository stockDocumentRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    
    /**
     * 创建库存单据
     */
    public StockDocument createStockDocument(CreateStockDocumentCommand command) {
        // 验证仓库存在
        Warehouse warehouse = warehouseRepository.findById(command.getWarehouseId())
            .orElseThrow(() -> new WarehouseNotFoundException("仓库不存在: " + command.getWarehouseId()));
        
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new WarehouseInactiveException("仓库未激活: " + command.getWarehouseId());
        }
        
        // 验证商品信息
        validateProductItems(command.getItems());
        
        // 验证库存（出库单据）
        if (isOutboundDocument(command.getType())) {
            validateInventoryAvailability(command.getWarehouseId(), command.getItems());
        }
        
        // 创建单据
        StockDocument document = StockDocument.create(command);
        
        // 保存单据
        return stockDocumentRepository.save(document);
    }
    
    /**
     * 执行库存单据
     */
    @Transactional
    public void executeStockDocument(String documentId) {
        // 获取单据
        StockDocument document = stockDocumentRepository.findById(documentId)
            .orElseThrow(() -> new StockDocumentNotFoundException("单据不存在: " + documentId));
        
        // 执行单据
        document.execute();
        
        // 更新库存
        updateInventoryByDocument(document);
        
        // 保存单据
        stockDocumentRepository.save(document);
    }
    
    /**
     * 验证商品信息
     */
    private void validateProductItems(List<CreateStockDocumentItemCommand> items) {
        for (CreateStockDocumentItemCommand item : items) {
            ProductSku sku = productRepository.findSkuById(item.getSkuId())
                .orElseThrow(() -> new ProductNotFoundException("商品不存在: " + item.getSkuId()));
            
            if (sku.getStatus() != ProductStatus.ACTIVE) {
                throw new ProductInactiveException("商品未激活: " + item.getSkuId());
            }
        }
    }
    
    /**
     * 验证库存可用性
     */
    private void validateInventoryAvailability(String warehouseId, List<CreateStockDocumentItemCommand> items) {
        for (CreateStockDocumentItemCommand item : items) {
            Inventory inventory = inventoryRepository.findBySkuIdAndWarehouseId(item.getSkuId(), warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException("库存不存在: " + item.getSkuId()));
            
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientInventoryException(
                    String.format("库存不足: SKU=%s, 需要=%d, 可用=%d", 
                        item.getSkuId(), item.getQuantity(), inventory.getAvailableQuantity()));
            }
        }
    }
    
    /**
     * 根据单据更新库存
     */
    private void updateInventoryByDocument(StockDocument document) {
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = inventoryRepository.findBySkuIdAndWarehouseId(
                item.getSkuId(), document.getWarehouseId())
                .orElseThrow(() -> new InventoryNotFoundException("库存不存在: " + item.getSkuId()));
            
            switch (document.getType()) {
                case INBOUND_PURCHASE:
                case INBOUND_PRODUCTION:
                case INBOUND_RETURN:
                    inventory.increase(item.getQuantity());
                    break;
                case OUTBOUND_SALE:
                case OUTBOUND_PRODUCTION:
                case OUTBOUND_TRANSFER:
                    inventory.decrease(item.getQuantity());
                    break;
                case ADJUSTMENT:
                    // 调整单据需要特殊处理
                    handleAdjustmentInventory(inventory, item);
                    break;
                default:
                    throw new UnsupportedOperationException("不支持的单据类型: " + document.getType());
            }
            
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 处理调整库存
     */
    private void handleAdjustmentInventory(Inventory inventory, StockDocumentItem item) {
        // 调整数量可能为正（盘盈）或负（盘亏）
        if (item.getQuantity() > 0) {
            inventory.increase(item.getQuantity());
        } else {
            inventory.decrease(Math.abs(item.getQuantity()));
        }
    }
    
    /**
     * 判断是否为出库单据
     */
    private boolean isOutboundDocument(DocumentType type) {
        return type == DocumentType.OUTBOUND_SALE ||
               type == DocumentType.OUTBOUND_PRODUCTION ||
               type == DocumentType.OUTBOUND_TRANSFER;
    }
}
```

### 3. 应用服务实现

#### 单据应用服务

```java
/**
 * 库存单据应用服务
 * 协调领域服务，处理事务边界
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockDocumentApplicationService {
    
    private final StockDocumentDomainService stockDocumentDomainService;
    private final StockDocumentRepository stockDocumentRepository;
    private final ApprovalService approvalService;
    private final NotificationService notificationService;
    
    /**
     * 创建入库单据
     */
    public StockDocumentResponse createInboundDocument(CreateInboundDocumentCommand command) {
        // 转换为通用创建命令
        CreateStockDocumentCommand createCommand = CreateStockDocumentCommand.builder()
            .type(command.getInboundType())
            .warehouseId(command.getWarehouseId())
            .operatorId(command.getOperatorId())
            .remark(command.getRemark())
            .items(command.getItems())
            .build();
        
        // 创建单据
        StockDocument document = stockDocumentDomainService.createStockDocument(createCommand);
        
        // 发送通知
        notificationService.sendDocumentCreatedNotification(document);
        
        return convertToResponse(document);
    }
    
    /**
     * 创建出库单据
     */
    public StockDocumentResponse createOutboundDocument(CreateOutboundDocumentCommand command) {
        // 转换为通用创建命令
        CreateStockDocumentCommand createCommand = CreateStockDocumentCommand.builder()
            .type(command.getOutboundType())
            .warehouseId(command.getWarehouseId())
            .operatorId(command.getOperatorId())
            .remark(command.getRemark())
            .items(command.getItems())
            .build();
        
        // 创建单据
        StockDocument document = stockDocumentDomainService.createStockDocument(createCommand);
        
        // 发送通知
        notificationService.sendDocumentCreatedNotification(document);
        
        return convertToResponse(document);
    }
    
    /**
     * 提交单据审核
     */
    public void submitDocumentForApproval(String documentId) {
        StockDocument document = getDocumentById(documentId);
        
        // 提交审核
        document.submitForApproval();
        
        // 保存单据
        stockDocumentRepository.save(document);
        
        // 启动审批流程
        approvalService.startApprovalProcess(document);
        
        // 发送审核通知
        notificationService.sendApprovalNotification(document);
    }
    
    /**
     * 审核单据
     */
    public void approveDocument(ApproveDocumentCommand command) {
        StockDocument document = getDocumentById(command.getDocumentId());
        
        // 验证审核权限
        approvalService.validateApprovalAuthority(command.getApproverId(), document);
        
        // 审核单据
        document.approve(command.getApproverId(), command.getApprovalComment());
        
        // 保存单据
        stockDocumentRepository.save(document);
        
        // 发送审核结果通知
        notificationService.sendApprovalResultNotification(document, command.getApproverId());
    }
    
    /**
     * 执行单据
     */
    public void executeDocument(String documentId) {
        // 执行单据（包含库存更新）
        stockDocumentDomainService.executeStockDocument(documentId);
        
        // 获取更新后的单据
        StockDocument document = getDocumentById(documentId);
        
        // 发送执行完成通知
        notificationService.sendExecutionCompletedNotification(document);
    }
    
    /**
     * 取消单据
     */
    public void cancelDocument(CancelDocumentCommand command) {
        StockDocument document = getDocumentById(command.getDocumentId());
        
        // 取消单据
        document.cancel(command.getCancelReason());
        
        // 保存单据
        stockDocumentRepository.save(document);
        
        // 发送取消通知
        notificationService.sendCancellationNotification(document, command.getCancelReason());
    }
    
    /**
     * 查询单据详情
     */
    @Transactional(readOnly = true)
    public StockDocumentResponse getDocumentDetail(String documentId) {
        StockDocument document = getDocumentById(documentId);
        return convertToResponse(document);
    }
    
    /**
     * 分页查询单据列表
     */
    @Transactional(readOnly = true)
    public PageResponse<StockDocumentResponse> queryDocuments(QueryDocumentsCommand command) {
        Page<StockDocument> documents = stockDocumentRepository.findByConditions(
            command.getType(),
            command.getStatus(),
            command.getWarehouseId(),
            command.getStartDate(),
            command.getEndDate(),
            PageRequest.of(command.getPageNum() - 1, command.getPageSize())
        );
        
        List<StockDocumentResponse> responses = documents.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<StockDocumentResponse>builder()
            .content(responses)
            .totalElements(documents.getTotalElements())
            .totalPages(documents.getTotalPages())
            .pageNum(command.getPageNum())
            .pageSize(command.getPageSize())
            .build();
    }
    
    /**
     * 获取单据统计信息
     */
    @Transactional(readOnly = true)
    public DocumentStatisticsResponse getDocumentStatistics(DocumentStatisticsQuery query) {
        return stockDocumentRepository.getStatistics(
            query.getWarehouseId(),
            query.getStartDate(),
            query.getEndDate()
        );
    }
    
    /**
     * 根据ID获取单据
     */
    private StockDocument getDocumentById(String documentId) {
        return stockDocumentRepository.findById(documentId)
            .orElseThrow(() -> new StockDocumentNotFoundException("单据不存在: " + documentId));
    }
    
    /**
     * 转换为响应对象
     */
    private StockDocumentResponse convertToResponse(StockDocument document) {
        return StockDocumentResponse.builder()
            .documentId(document.getDocumentId())
            .documentNo(document.getDocumentNo())
            .type(document.getType())
            .status(document.getStatus())
            .warehouseId(document.getWarehouseId())
            .operatorId(document.getOperatorId())
            .totalAmount(document.getTotalAmount())
            .remark(document.getRemark())
            .createTime(document.getCreateTime())
            .approveTime(document.getApproveTime())
            .executeTime(document.getExecuteTime())
            .items(document.getItems().stream()
                .map(this::convertItemToResponse)
                .collect(Collectors.toList()))
            .build();
    }
    
    /**
     * 转换明细为响应对象
     */
    private StockDocumentItemResponse convertItemToResponse(StockDocumentItem item) {
        return StockDocumentItemResponse.builder()
            .skuId(item.getSkuId())
            .skuName(item.getSkuName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .batchNo(item.getBatchNo())
            .productionDate(item.getProductionDate())
            .expiryDate(item.getExpiryDate())
            .remark(item.getRemark())
            .build();
    }
}
```

### 4. 数据访问层实现

#### 单据仓储实现

```java
/**
 * 库存单据仓储实现
 */
@Repository
@RequiredArgsConstructor
public class StockDocumentRepositoryImpl implements StockDocumentRepository {
    
    private final StockDocumentMapper stockDocumentMapper;
    private final StockDocumentItemMapper stockDocumentItemMapper;
    private final DocumentApprovalMapper documentApprovalMapper;
    
    @Override
    public Optional<StockDocument> findById(String documentId) {
        StockDocument document = stockDocumentMapper.selectById(documentId);
        if (document != null) {
            // 加载单据明细
            List<StockDocumentItem> items = stockDocumentItemMapper.selectList(
                new LambdaQueryWrapper<StockDocumentItem>()
                    .eq(StockDocumentItem::getDocumentId, documentId)
                    .orderByAsc(StockDocumentItem::getId)
            );
            document.setItems(items);
            
            // 加载审批记录
            List<DocumentApproval> approvals = documentApprovalMapper.selectList(
                new LambdaQueryWrapper<DocumentApproval>()
                    .eq(DocumentApproval::getDocumentId, documentId)
                    .orderByAsc(DocumentApproval::getCreateTime)
            );
            document.setApprovals(approvals);
        }
        return Optional.ofNullable(document);
    }
    
    @Override
    @Transactional
    public StockDocument save(StockDocument document) {
        if (document.getId() == null) {
            // 新增单据
            stockDocumentMapper.insert(document);
            
            // 保存单据明细
            for (StockDocumentItem item : document.getItems()) {
                item.setDocumentId(document.getDocumentId());
                stockDocumentItemMapper.insert(item);
            }
        } else {
            // 更新单据
            stockDocumentMapper.updateById(document);
            
            // 删除原有明细
            stockDocumentItemMapper.delete(
                new LambdaQueryWrapper<StockDocumentItem>()
                    .eq(StockDocumentItem::getDocumentId, document.getDocumentId())
            );
            
            // 重新保存明细
            for (StockDocumentItem item : document.getItems()) {
                item.setDocumentId(document.getDocumentId());
                stockDocumentItemMapper.insert(item);
            }
        }
        
        // 保存审批记录
        for (DocumentApproval approval : document.getApprovals()) {
            if (approval.getId() == null) {
                documentApprovalMapper.insert(approval);
            }
        }
        
        return document;
    }
    
    @Override
    public Page<StockDocument> findByConditions(DocumentType type, DocumentStatus status, 
                                               String warehouseId, LocalDateTime startDate, 
                                               LocalDateTime endDate, Pageable pageable) {
        
        LambdaQueryWrapper<StockDocument> queryWrapper = new LambdaQueryWrapper<StockDocument>()
            .eq(type != null, StockDocument::getType, type)
            .eq(status != null, StockDocument::getStatus, status)
            .eq(StringUtils.hasText(warehouseId), StockDocument::getWarehouseId, warehouseId)
            .ge(startDate != null, StockDocument::getCreateTime, startDate)
            .le(endDate != null, StockDocument::getCreateTime, endDate)
            .orderByDesc(StockDocument::getCreateTime);
        
        IPage<StockDocument> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<StockDocument> result = stockDocumentMapper.selectPage(page, queryWrapper);
        
        return new PageImpl<>(result.getRecords(), pageable, result.getTotal());
    }
    
    @Override
    public DocumentStatisticsResponse getStatistics(String warehouseId, LocalDate startDate, LocalDate endDate) {
        return stockDocumentMapper.getStatistics(warehouseId, startDate, endDate);
    }
    
    @Override
    public List<StockDocument> findPendingDocuments(String warehouseId) {
        LambdaQueryWrapper<StockDocument> queryWrapper = new LambdaQueryWrapper<StockDocument>()
            .eq(StockDocument::getStatus, DocumentStatus.PENDING)
            .eq(StringUtils.hasText(warehouseId), StockDocument::getWarehouseId, warehouseId)
            .orderByAsc(StockDocument::getCreateTime);
        
        return stockDocumentMapper.selectList(queryWrapper);
    }
}
```

#### MyBatis Mapper实现

```java
/**
 * 库存单据Mapper
 */
@Mapper
public interface StockDocumentMapper extends BaseMapper<StockDocument> {
    
    /**
     * 获取单据统计信息
     */
    @Select("""
        SELECT 
            COUNT(*) as totalCount,
            COUNT(CASE WHEN status = 'DRAFT' THEN 1 END) as draftCount,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingCount,
            COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approvedCount,
            COUNT(CASE WHEN status = 'EXECUTED' THEN 1 END) as executedCount,
            COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelledCount,
            SUM(CASE WHEN status = 'EXECUTED' THEN total_amount ELSE 0 END) as totalAmount
        FROM stock_document 
        WHERE (#{warehouseId} IS NULL OR warehouse_id = #{warehouseId})
          AND (#{startDate} IS NULL OR DATE(create_time) >= #{startDate})
          AND (#{endDate} IS NULL OR DATE(create_time) <= #{endDate})
    """)
    DocumentStatisticsResponse getStatistics(@Param("warehouseId") String warehouseId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * 获取单据编号序列
     */
    @Select("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(document_no, -4) AS UNSIGNED)), 0) + 1
        FROM stock_document 
        WHERE document_no LIKE CONCAT(#{prefix}, '%')
          AND DATE(create_time) = #{date}
    """)
    Integer getNextSequence(@Param("prefix") String prefix, @Param("date") LocalDate date);
}

/**
 * 库存单据明细Mapper
 */
@Mapper
public interface StockDocumentItemMapper extends BaseMapper<StockDocumentItem> {
    
    /**
     * 批量插入单据明细
     */
    @Insert("""
        <script>
        INSERT INTO stock_document_item 
        (document_id, sku_id, sku_name, quantity, unit_price, batch_no, production_date, expiry_date, remark)
        VALUES
        <foreach collection="items" item="item" separator=",">
        (#{item.documentId}, #{item.skuId}, #{item.skuName}, #{item.quantity}, 
         #{item.unitPrice}, #{item.batchNo}, #{item.productionDate}, #{item.expiryDate}, #{item.remark})
        </foreach>
        </script>
    """)
    void batchInsert(@Param("items") List<StockDocumentItem> items);
}
```

### 5. 事件处理机制

#### 领域事件定义

```java
/**
 * 库存单据创建事件
 */
@Data
@AllArgsConstructor
public class StockDocumentCreatedEvent implements DomainEvent {
    private String documentId;
    private DocumentType documentType;
    private LocalDateTime occurredOn = LocalDateTime.now();
}

/**
 * 库存单据执行事件
 */
@Data
@AllArgsConstructor
public class StockDocumentExecutedEvent implements DomainEvent {
    private String documentId;
    private DocumentType documentType;
    private List<StockDocumentItem> items;
    private LocalDateTime occurredOn = LocalDateTime.now();
}
```

#### 事件处理器实现

```java
/**
 * 库存单据事件处理器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockDocumentEventHandler {
    
    private final InventoryApplicationService inventoryApplicationService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    
    /**
     * 处理单据创建事件
     */
    @EventListener
    @Async
    public void handleDocumentCreated(StockDocumentCreatedEvent event) {
        log.info("处理单据创建事件: documentId={}, type={}", 
            event.getDocumentId(), event.getDocumentType());
        
        try {
            // 记录审计日志
            auditService.recordDocumentCreated(event.getDocumentId(), event.getDocumentType());
            
            // 发送创建通知（如果需要）
            // notificationService.sendDocumentCreatedNotification(event);
            
        } catch (Exception e) {
            log.error("处理单据创建事件失败: documentId={}", event.getDocumentId(), e);
        }
    }
    
    /**
     * 处理单据执行事件
     */
    @EventListener
    @Async
    public void handleDocumentExecuted(StockDocumentExecutedEvent event) {
        log.info("处理单据执行事件: documentId={}, type={}", 
            event.getDocumentId(), event.getDocumentType());
        
        try {
            // 更新库存成本（如果是入库单据）
            if (isInboundDocument(event.getDocumentType())) {
                updateInventoryCost(event.getItems());
            }
            
            // 记录审计日志
            auditService.recordDocumentExecuted(event.getDocumentId(), event.getItems());
            
            // 发送执行完成通知
            notificationService.sendExecutionCompletedNotification(event);
            
        } catch (Exception e) {
            log.error("处理单据执行事件失败: documentId={}", event.getDocumentId(), e);
        }
    }
    
    /**
     * 更新库存成本
     */
    private void updateInventoryCost(List<StockDocumentItem> items) {
        for (StockDocumentItem item : items) {
            inventoryApplicationService.updateInventoryCost(
                item.getSkuId(), 
                item.getUnitPrice(), 
                item.getQuantity()
            );
        }
    }
    
    /**
     * 判断是否为入库单据
     */
    private boolean isInboundDocument(DocumentType type) {
        return type == DocumentType.INBOUND_PURCHASE ||
               type == DocumentType.INBOUND_PRODUCTION ||
               type == DocumentType.INBOUND_RETURN;
    }
}
```

### 6. 缓存策略实现

#### 单据缓存配置

```java
/**
 * 单据缓存配置
 */
@Configuration
@EnableCaching
public class StockDocumentCacheConfig {
    
    /**
     * 单据缓存管理器
     */
    @Bean("stockDocumentCacheManager")
    public CacheManager stockDocumentCacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    /**
     * 缓存配置
     */
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

#### 缓存使用示例

```java
/**
 * 带缓存的单据查询服务
 */
@Service
@RequiredArgsConstructor
public class CachedStockDocumentService {
    
    private final StockDocumentRepository stockDocumentRepository;
    
    /**
     * 缓存单据详情
     */
    @Cacheable(value = "stock-document", key = "#documentId", cacheManager = "stockDocumentCacheManager")
    public StockDocumentResponse getDocumentDetail(String documentId) {
        StockDocument document = stockDocumentRepository.findById(documentId)
            .orElseThrow(() -> new StockDocumentNotFoundException("单据不存在: " + documentId));
        
        return convertToResponse(document);
    }
    
    /**
     * 清除单据缓存
     */
    @CacheEvict(value = "stock-document", key = "#documentId", cacheManager = "stockDocumentCacheManager")
    public void evictDocumentCache(String documentId) {
        // 缓存清除
    }
    
    /**
     * 缓存单据统计
     */
    @Cacheable(value = "document-statistics", key = "#warehouseId + ':' + #startDate + ':' + #endDate", 
               cacheManager = "stockDocumentCacheManager")
    public DocumentStatisticsResponse getDocumentStatistics(String warehouseId, LocalDate startDate, LocalDate endDate) {
        return stockDocumentRepository.getStatistics(warehouseId, startDate, endDate);
    }
}
```

### 7. 性能优化实现

#### 批量操作支持

```java
/**
 * 批量单据操作服务
 */
@Service
@RequiredArgsConstructor
public class BatchStockDocumentService {
    
    private final StockDocumentRepository stockDocumentRepository;
    private final StockDocumentDomainService stockDocumentDomainService;
    
    /**
     * 批量创建单据
     */
    @Transactional
    public List<StockDocumentResponse> batchCreateDocuments(List<CreateStockDocumentCommand> commands) {
        List<StockDocument> documents = new ArrayList<>();
        
        for (CreateStockDocumentCommand command : commands) {
            StockDocument document = stockDocumentDomainService.createStockDocument(command);
            documents.add(document);
        }
        
        // 批量保存
        stockDocumentRepository.saveAll(documents);
        
        return documents.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 批量审核单据
     */
    @Transactional
    public void batchApproveDocuments(List<String> documentIds, String approverId, String comment) {
        List<StockDocument> documents = stockDocumentRepository.findAllById(documentIds);
        
        for (StockDocument document : documents) {
            if (document.getStatus() == DocumentStatus.PENDING) {
                document.approve(approverId, comment);
            }
        }
        
        // 批量保存
        stockDocumentRepository.saveAll(documents);
    }
    
    /**
     * 批量执行单据
     */
    @Transactional
    public void batchExecuteDocuments(List<String> documentIds) {
        for (String documentId : documentIds) {
            try {
                stockDocumentDomainService.executeStockDocument(documentId);
            } catch (Exception e) {
                log.error("批量执行单据失败: documentId={}", documentId, e);
                // 继续执行其他单据
            }
        }
    }
}
```

#### 异步处理实现

```java
/**
 * 异步单据处理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncStockDocumentService {
    
    private final StockDocumentDomainService stockDocumentDomainService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 异步执行单据
     */
    @Async("stockDocumentExecutor")
    public CompletableFuture<Void> executeDocumentAsync(String documentId) {
        try {
            // 设置执行状态
            setExecutionStatus(documentId, "PROCESSING");
            
            // 执行单据
            stockDocumentDomainService.executeStockDocument(documentId);
            
            // 设置完成状态
            setExecutionStatus(documentId, "COMPLETED");
            
            log.info("异步执行单据完成: documentId={}", documentId);
            
        } catch (Exception e) {
            // 设置失败状态
            setExecutionStatus(documentId, "FAILED");
            log.error("异步执行单据失败: documentId={}", documentId, e);
            throw e;
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 获取执行状态
     */
    public String getExecutionStatus(String documentId) {
        return (String) redisTemplate.opsForValue().get("document:execution:" + documentId);
    }
    
    /**
     * 设置执行状态
     */
    private void setExecutionStatus(String documentId, String status) {
        redisTemplate.opsForValue().set("document:execution:" + documentId, status, Duration.ofHours(1));
    }
}

/**
 * 异步执行器配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("stockDocumentExecutor")
    public TaskExecutor stockDocumentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("StockDocument-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

这个技术实现文档提供了库存单据体系的完整技术实现方案，包括领域模型、服务层、数据访问层、事件处理、缓存策略和性能优化等各个方面的详细实现代码。