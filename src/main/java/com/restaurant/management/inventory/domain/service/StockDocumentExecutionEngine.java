package com.restaurant.management.inventory.domain.service;

import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import com.restaurant.management.inventory.domain.model.StockDocumentItem;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import com.restaurant.management.inventory.domain.model.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 库存单据执行引擎
 * 负责执行各种类型的库存单据，处理实际的库存变更操作
 */
@Service
public class StockDocumentExecutionEngine {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private InventoryDomainService inventoryDomainService;
    
    /**
     * 执行库存单据
     */
    @Transactional
    public void executeDocument(StockDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("单据不能为空");
        }
        
        DocumentType type = document.getType();
        
        switch (type) {
            case INBOUND_PURCHASE:
                executeInboundPurchase(document);
                break;
            case INBOUND_PRODUCTION:
                executeInboundProduction(document);
                break;
            case INBOUND_RETURN:
                executeInboundReturn(document);
                break;
            case OUTBOUND_SALE:
                executeOutboundSale(document);
                break;
            case OUTBOUND_PRODUCTION:
                executeOutboundProduction(document);
                break;
            case OUTBOUND_TRANSFER:
                executeOutboundTransfer(document);
                break;
            case TRANSFER:
                executeTransfer(document);
                break;
            case ADJUSTMENT:
                executeAdjustment(document);
                break;
            default:
                throw new IllegalArgumentException("不支持的单据类型: " + type);
        }
    }
    
    /**
     * 执行采购入库
     */
    private void executeInboundPurchase(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("采购入库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            // 查找或创建库存记录
            Inventory inventory = findOrCreateInventory(item.getSkuId(), warehouseId);
            
            // 增加库存
            inventory.increase(item.getQuantity(), item.getUnitPrice(), reason);
            
            // 保存库存变更
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行生产入库
     */
    private void executeInboundProduction(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("生产入库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = findOrCreateInventory(item.getSkuId(), warehouseId);
            inventory.increase(item.getQuantity(), item.getUnitPrice(), reason);
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行退货入库
     */
    private void executeInboundReturn(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("退货入库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = findOrCreateInventory(item.getSkuId(), warehouseId);
            inventory.increase(item.getQuantity(), item.getUnitPrice(), reason);
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行销售出库
     */
    private void executeOutboundSale(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("销售出库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = getRequiredInventory(item.getSkuId(), warehouseId);
            
            // 检查库存充足性
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(String.format(
                    "库存不足 - SKU: %s, 需要: %d, 可用: %d", 
                    item.getSkuId(), item.getQuantity(), inventory.getAvailableQuantity()
                ));
            }
            
            // 扣减库存
            inventory.deduct(item.getQuantity(), reason);
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行生产出库
     */
    private void executeOutboundProduction(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("生产出库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = getRequiredInventory(item.getSkuId(), warehouseId);
            
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(String.format(
                    "库存不足 - SKU: %s, 需要: %d, 可用: %d", 
                    item.getSkuId(), item.getQuantity(), inventory.getAvailableQuantity()
                ));
            }
            
            inventory.deduct(item.getQuantity(), reason);
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行调拨出库
     */
    private void executeOutboundTransfer(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("调拨出库 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = getRequiredInventory(item.getSkuId(), warehouseId);
            
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(String.format(
                    "库存不足 - SKU: %s, 需要: %d, 可用: %d", 
                    item.getSkuId(), item.getQuantity(), inventory.getAvailableQuantity()
                ));
            }
            
            inventory.deduct(item.getQuantity(), reason);
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 执行仓库间调拨
     */
    private void executeTransfer(StockDocument document) {
        String sourceWarehouseId = document.getWarehouseId();
        String targetWarehouseId = extractTargetWarehouseId(document.getRemark());
        String reason = String.format("仓库调拨 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            // 源仓库出库
            Inventory sourceInventory = getRequiredInventory(item.getSkuId(), sourceWarehouseId);
            if (sourceInventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(String.format(
                    "源仓库库存不足 - SKU: %s, 需要: %d, 可用: %d", 
                    item.getSkuId(), item.getQuantity(), sourceInventory.getAvailableQuantity()
                ));
            }
            sourceInventory.deduct(item.getQuantity(), reason + " (出库)");
            inventoryRepository.save(sourceInventory);
            
            // 目标仓库入库
            Inventory targetInventory = findOrCreateInventory(item.getSkuId(), targetWarehouseId);
            targetInventory.increase(item.getQuantity(), item.getUnitPrice(), reason + " (入库)");
            inventoryRepository.save(targetInventory);
        }
    }
    
    /**
     * 执行库存调整
     */
    private void executeAdjustment(StockDocument document) {
        String warehouseId = document.getWarehouseId();
        String reason = String.format("库存调整 - 单据号: %s", document.getDocumentNo());
        
        for (StockDocumentItem item : document.getItems()) {
            Inventory inventory = getRequiredInventory(item.getSkuId(), warehouseId);
            
            int adjustmentQuantity = item.getQuantity();
            if (adjustmentQuantity > 0) {
                // 正调整 - 增加库存
                inventory.increase(adjustmentQuantity, item.getUnitPrice(), reason + " (增加)");
            } else if (adjustmentQuantity < 0) {
                // 负调整 - 减少库存
                int deductQuantity = Math.abs(adjustmentQuantity);
                if (inventory.getAvailableQuantity() < deductQuantity) {
                    throw new IllegalStateException(String.format(
                        "调整后库存不足 - SKU: %s, 需要减少: %d, 可用: %d", 
                        item.getSkuId(), deductQuantity, inventory.getAvailableQuantity()
                    ));
                }
                inventory.deduct(deductQuantity, reason + " (减少)");
            }
            // adjustmentQuantity == 0 的情况不需要处理
            
            inventoryRepository.save(inventory);
        }
    }
    
    /**
     * 查找或创建库存记录
     */
    private Inventory findOrCreateInventory(String skuId, String warehouseId) {
        Optional<Inventory> existingInventory = inventoryRepository.findBySkuIdAndWarehouseId(skuId, warehouseId);
        
        if (existingInventory.isPresent()) {
            return existingInventory.get();
        } else {
            // 创建新的库存记录
            Inventory newInventory = Inventory.create(skuId, warehouseId, 0);
            return inventoryRepository.save(newInventory);
        }
    }
    
    /**
     * 获取必须存在的库存记录
     */
    private Inventory getRequiredInventory(String skuId, String warehouseId) {
        return inventoryRepository.findBySkuIdAndWarehouseId(skuId, warehouseId)
            .orElseThrow(() -> new IllegalStateException(String.format(
                "库存记录不存在 - SKU: %s, 仓库: %s", skuId, warehouseId
            )));
    }
    
    /**
     * 从备注中提取目标仓库ID
     */
    private String extractTargetWarehouseId(String remark) {
        if (remark == null || !remark.contains("目标仓库:")) {
            throw new IllegalArgumentException("调拨单备注中未找到目标仓库信息");
        }
        
        // 解析格式: "备注内容 [目标仓库: WAREHOUSE_ID]"
        int startIndex = remark.indexOf("目标仓库:") + "目标仓库:".length();
        int endIndex = remark.indexOf("]", startIndex);
        
        if (endIndex == -1) {
            throw new IllegalArgumentException("调拨单备注格式错误");
        }
        
        return remark.substring(startIndex, endIndex).trim();
    }
    
    /**
     * 验证单据执行前的库存状态
     */
    public void validateInventoryBeforeExecution(StockDocument document) {
        DocumentType type = document.getType();
        
        // 只有出库类型的单据需要验证库存充足性
        if (isOutboundDocument(type)) {
            String warehouseId = document.getWarehouseId();
            
            for (StockDocumentItem item : document.getItems()) {
                Optional<Inventory> inventoryOpt = inventoryRepository.findBySkuIdAndWarehouseId(
                    item.getSkuId(), warehouseId);
                
                if (!inventoryOpt.isPresent()) {
                    throw new IllegalStateException(String.format(
                        "库存记录不存在 - SKU: %s, 仓库: %s", item.getSkuId(), warehouseId
                    ));
                }
                
                Inventory inventory = inventoryOpt.get();
                if (inventory.getAvailableQuantity() < item.getQuantity()) {
                    throw new IllegalStateException(String.format(
                        "库存不足 - SKU: %s, 需要: %d, 可用: %d", 
                        item.getSkuId(), item.getQuantity(), inventory.getAvailableQuantity()
                    ));
                }
            }
        }
    }
    
    /**
     * 判断是否为出库类型单据
     */
    private boolean isOutboundDocument(DocumentType type) {
        return type == DocumentType.OUTBOUND_SALE ||
               type == DocumentType.OUTBOUND_PRODUCTION ||
               type == DocumentType.OUTBOUND_TRANSFER ||
               type == DocumentType.TRANSFER;
    }
}