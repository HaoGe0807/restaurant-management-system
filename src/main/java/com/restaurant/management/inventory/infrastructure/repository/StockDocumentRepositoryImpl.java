package com.restaurant.management.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.restaurant.management.inventory.domain.model.DocumentStatus;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import com.restaurant.management.inventory.domain.model.StockDocumentItem;
import com.restaurant.management.inventory.domain.repository.StockDocumentRepository;
import com.restaurant.management.inventory.infrastructure.mapper.StockDocumentMapper;
import com.restaurant.management.inventory.infrastructure.mapper.StockDocumentItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 库存单据仓储实现
 * 基于MyBatis-Plus实现库存单据的持久化操作
 */
@Repository
public class StockDocumentRepositoryImpl implements StockDocumentRepository {
    
    @Autowired
    private StockDocumentMapper stockDocumentMapper;
    
    @Autowired
    private StockDocumentItemMapper stockDocumentItemMapper;
    
    @Override
    public StockDocument save(StockDocument document) {
        if (document.getId() == null) {
            // 新增单据
            stockDocumentMapper.insert(document);
            
            // 保存单据明细
            if (document.getItems() != null && !document.getItems().isEmpty()) {
                for (StockDocumentItem item : document.getItems()) {
                    item.setDocumentId(document.getDocumentId());
                    stockDocumentItemMapper.insert(item);
                }
            }
        } else {
            // 更新单据
            stockDocumentMapper.updateById(document);
            
            // 删除原有明细
            QueryWrapper<StockDocumentItem> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("document_id", document.getDocumentId());
            stockDocumentItemMapper.delete(deleteWrapper);
            
            // 重新保存明细
            if (document.getItems() != null && !document.getItems().isEmpty()) {
                for (StockDocumentItem item : document.getItems()) {
                    item.setDocumentId(document.getDocumentId());
                    item.setId(null); // 确保是新增
                    stockDocumentItemMapper.insert(item);
                }
            }
        }
        
        return document;
    }
    
    @Override
    public Optional<StockDocument> findById(Long id) {
        StockDocument document = stockDocumentMapper.selectById(id);
        if (document != null) {
            loadDocumentItems(document);
        }
        return Optional.ofNullable(document);
    }
    
    @Override
    public Optional<StockDocument> findByDocumentId(String documentId) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        StockDocument document = stockDocumentMapper.selectOne(wrapper);
        if (document != null) {
            loadDocumentItems(document);
        }
        return Optional.ofNullable(document);
    }
    
    @Override
    public Optional<StockDocument> findByDocumentNo(String documentNo) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_no", documentNo);
        StockDocument document = stockDocumentMapper.selectOne(wrapper);
        if (document != null) {
            loadDocumentItems(document);
        }
        return Optional.ofNullable(document);
    }
    
    @Override
    public void delete(StockDocument document) {
        if (document.getId() != null) {
            deleteById(document.getId());
        }
    }
    
    @Override
    public void deleteById(Long id) {
        // 先删除明细
        StockDocument document = stockDocumentMapper.selectById(id);
        if (document != null) {
            QueryWrapper<StockDocumentItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("document_id", document.getDocumentId());
            stockDocumentItemMapper.delete(itemWrapper);
        }
        
        // 再删除主表
        stockDocumentMapper.deleteById(id);
    }
    
    @Override
    public List<StockDocument> findByStatus(DocumentStatus status) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_status", status);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByType(DocumentType type) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_type", type);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByWarehouseId(String warehouseId) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByOperatorId(String operatorId) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", startTime, endTime);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByCreateTimeBetweenAndStatus(LocalDateTime startTime, LocalDateTime endTime, DocumentStatus status) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", startTime, endTime)
               .eq("document_status", status);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByCreateTimeBetweenAndType(LocalDateTime startTime, LocalDateTime endTime, DocumentType type) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", startTime, endTime)
               .eq("document_type", type);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findPendingDocuments() {
        return findByStatus(DocumentStatus.PENDING);
    }
    
    @Override
    public List<StockDocument> findApprovedButNotExecutedDocuments() {
        return findByStatus(DocumentStatus.APPROVED);
    }
    
    @Override
    public List<StockDocument> findByWarehouseIdAndSkuId(String warehouseId, String skuId) {
        // 通过关联查询找到包含指定SKU的单据
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId)
               .exists("SELECT 1 FROM stock_document_item sdi WHERE sdi.document_id = stock_document.document_id AND sdi.sku_id = {0}", skuId);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public long countByStatus(DocumentStatus status) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_status", status);
        return stockDocumentMapper.selectCount(wrapper);
    }
    
    @Override
    public long countByType(DocumentType type) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_type", type);
        return stockDocumentMapper.selectCount(wrapper);
    }
    
    @Override
    public long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", startTime, endTime);
        return stockDocumentMapper.selectCount(wrapper);
    }
    
    @Override
    public boolean existsByDocumentNo(String documentNo) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_no", documentNo);
        return stockDocumentMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByDocumentId(String documentId) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        return stockDocumentMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public List<StockDocument> findWithPagination(int offset, int limit) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time")
               .last("LIMIT " + offset + ", " + limit);
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    @Override
    public List<StockDocument> findByConditionsWithPagination(DocumentType type, DocumentStatus status,
                                                              String warehouseId, LocalDateTime startTime,
                                                              LocalDateTime endTime, int offset, int limit) {
        QueryWrapper<StockDocument> wrapper = new QueryWrapper<>();
        
        if (type != null) {
            wrapper.eq("document_type", type);
        }
        if (status != null) {
            wrapper.eq("document_status", status);
        }
        if (warehouseId != null && !warehouseId.trim().isEmpty()) {
            wrapper.eq("warehouse_id", warehouseId);
        }
        if (startTime != null && endTime != null) {
            wrapper.between("create_time", startTime, endTime);
        }
        
        wrapper.orderByDesc("create_time")
               .last("LIMIT " + offset + ", " + limit);
        
        List<StockDocument> documents = stockDocumentMapper.selectList(wrapper);
        documents.forEach(this::loadDocumentItems);
        return documents;
    }
    
    /**
     * 加载单据明细
     */
    private void loadDocumentItems(StockDocument document) {
        QueryWrapper<StockDocumentItem> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", document.getDocumentId())
               .orderByAsc("id");
        List<StockDocumentItem> items = stockDocumentItemMapper.selectList(wrapper);
        document.setItems(items);
    }
}