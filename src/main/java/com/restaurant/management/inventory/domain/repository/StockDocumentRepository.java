package com.restaurant.management.inventory.domain.repository;

import com.restaurant.management.inventory.domain.model.DocumentStatus;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 库存单据仓储接口
 * 定义库存单据的持久化操作
 */
public interface StockDocumentRepository {
    
    /**
     * 保存单据
     */
    StockDocument save(StockDocument document);
    
    /**
     * 根据ID查找单据
     */
    Optional<StockDocument> findById(Long id);
    
    /**
     * 根据单据ID查找单据
     */
    Optional<StockDocument> findByDocumentId(String documentId);
    
    /**
     * 根据单据编号查找单据
     */
    Optional<StockDocument> findByDocumentNo(String documentNo);
    
    /**
     * 删除单据
     */
    void delete(StockDocument document);
    
    /**
     * 根据ID删除单据
     */
    void deleteById(Long id);
    
    /**
     * 查找指定状态的单据列表
     */
    List<StockDocument> findByStatus(DocumentStatus status);
    
    /**
     * 查找指定类型的单据列表
     */
    List<StockDocument> findByType(DocumentType type);
    
    /**
     * 查找指定仓库的单据列表
     */
    List<StockDocument> findByWarehouseId(String warehouseId);
    
    /**
     * 查找指定操作人的单据列表
     */
    List<StockDocument> findByOperatorId(String operatorId);
    
    /**
     * 查找指定时间范围内的单据列表
     */
    List<StockDocument> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内指定状态的单据列表
     */
    List<StockDocument> findByCreateTimeBetweenAndStatus(LocalDateTime startTime, LocalDateTime endTime, DocumentStatus status);
    
    /**
     * 查找指定时间范围内指定类型的单据列表
     */
    List<StockDocument> findByCreateTimeBetweenAndType(LocalDateTime startTime, LocalDateTime endTime, DocumentType type);
    
    /**
     * 查找待审核的单据列表
     */
    List<StockDocument> findPendingDocuments();
    
    /**
     * 查找已审核但未执行的单据列表
     */
    List<StockDocument> findApprovedButNotExecutedDocuments();
    
    /**
     * 查找指定仓库和SKU相关的单据列表
     */
    List<StockDocument> findByWarehouseIdAndSkuId(String warehouseId, String skuId);
    
    /**
     * 统计指定状态的单据数量
     */
    long countByStatus(DocumentStatus status);
    
    /**
     * 统计指定类型的单据数量
     */
    long countByType(DocumentType type);
    
    /**
     * 统计指定时间范围内的单据数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检查单据编号是否存在
     */
    boolean existsByDocumentNo(String documentNo);
    
    /**
     * 检查单据ID是否存在
     */
    boolean existsByDocumentId(String documentId);
    
    /**
     * 分页查询单据列表
     */
    List<StockDocument> findWithPagination(int offset, int limit);
    
    /**
     * 根据条件分页查询单据列表
     */
    List<StockDocument> findByConditionsWithPagination(DocumentType type, DocumentStatus status, 
                                                       String warehouseId, LocalDateTime startTime, 
                                                       LocalDateTime endTime, int offset, int limit);
}