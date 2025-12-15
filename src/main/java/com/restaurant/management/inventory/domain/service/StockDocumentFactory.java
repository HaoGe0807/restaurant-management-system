package com.restaurant.management.inventory.domain.service;

import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import com.restaurant.management.inventory.domain.model.StockDocumentItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存单据工厂
 * 负责创建各种类型的库存单据，确保单据创建的一致性和完整性
 */
@Service
public class StockDocumentFactory {
    
    /**
     * 创建采购入库单
     */
    public StockDocument createPurchaseInboundDocument(String warehouseId, String operatorId, 
                                                      List<PurchaseItem> purchaseItems, String remark) {
        validatePurchaseItems(purchaseItems);
        
        List<StockDocumentItem> items = purchaseItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getRemark()
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.INBOUND_PURCHASE, warehouseId, operatorId, items, remark);
    }
    
    /**
     * 创建生产入库单
     */
    public StockDocument createProductionInboundDocument(String warehouseId, String operatorId,
                                                        List<ProductionItem> productionItems, String remark) {
        validateProductionItems(productionItems);
        
        List<StockDocumentItem> items = productionItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getRemark()
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.INBOUND_PRODUCTION, warehouseId, operatorId, items, remark);
    }
    
    /**
     * 创建退货入库单
     */
    public StockDocument createReturnInboundDocument(String warehouseId, String operatorId,
                                                    List<ReturnItem> returnItems, String remark) {
        validateReturnItems(returnItems);
        
        List<StockDocumentItem> items = returnItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getOriginalPrice(),
                item.getRemark()
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.INBOUND_RETURN, warehouseId, operatorId, items, remark);
    }
    
    /**
     * 创建销售出库单
     */
    public StockDocument createSaleOutboundDocument(String warehouseId, String operatorId,
                                                   List<SaleItem> saleItems, String remark) {
        validateSaleItems(saleItems);
        
        List<StockDocumentItem> items = saleItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getSalePrice(),
                item.getRemark()
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.OUTBOUND_SALE, warehouseId, operatorId, items, remark);
    }
    
    /**
     * 创建生产出库单
     */
    public StockDocument createProductionOutboundDocument(String warehouseId, String operatorId,
                                                         List<ProductionOutboundItem> outboundItems, String remark) {
        validateProductionOutboundItems(outboundItems);
        
        List<StockDocumentItem> items = outboundItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getRemark()
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.OUTBOUND_PRODUCTION, warehouseId, operatorId, items, remark);
    }
    
    /**
     * 创建调拨单
     */
    public StockDocument createTransferDocument(String sourceWarehouseId, String targetWarehouseId, 
                                               String operatorId, List<TransferItem> transferItems, String remark) {
        validateTransferItems(transferItems);
        
        List<StockDocumentItem> items = transferItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getQuantity(),
                item.getUnitCost(),
                String.format("调拨至仓库: %s", targetWarehouseId)
            ))
            .collect(Collectors.toList());
        
        StockDocument document = StockDocument.create(DocumentType.TRANSFER, sourceWarehouseId, operatorId, items, remark);
        // 设置目标仓库信息到备注中
        document.setRemark(String.format("%s [目标仓库: %s]", remark, targetWarehouseId));
        return document;
    }
    
    /**
     * 创建库存调整单
     */
    public StockDocument createAdjustmentDocument(String warehouseId, String operatorId,
                                                 List<AdjustmentItem> adjustmentItems, String remark) {
        validateAdjustmentItems(adjustmentItems);
        
        List<StockDocumentItem> items = adjustmentItems.stream()
            .map(item -> StockDocumentItem.create(
                item.getSkuId(),
                item.getAdjustmentQuantity(),
                item.getUnitCost(),
                String.format("调整原因: %s", item.getAdjustmentReason())
            ))
            .collect(Collectors.toList());
        
        return StockDocument.create(DocumentType.ADJUSTMENT, warehouseId, operatorId, items, remark);
    }
    
    // 验证方法
    private void validatePurchaseItems(List<PurchaseItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("采购商品列表不能为空");
        }
        for (PurchaseItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("采购数量必须大于0");
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("采购单价必须大于0");
            }
        }
    }
    
    private void validateProductionItems(List<ProductionItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("生产商品列表不能为空");
        }
        for (ProductionItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("生产数量必须大于0");
            }
        }
    }
    
    private void validateReturnItems(List<ReturnItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("退货商品列表不能为空");
        }
        for (ReturnItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("退货数量必须大于0");
            }
        }
    }
    
    private void validateSaleItems(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("销售商品列表不能为空");
        }
        for (SaleItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("销售数量必须大于0");
            }
        }
    }
    
    private void validateProductionOutboundItems(List<ProductionOutboundItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("生产出库商品列表不能为空");
        }
        for (ProductionOutboundItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("出库数量必须大于0");
            }
        }
    }
    
    private void validateTransferItems(List<TransferItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("调拨商品列表不能为空");
        }
        for (TransferItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("调拨数量必须大于0");
            }
        }
    }
    
    private void validateAdjustmentItems(List<AdjustmentItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("调整商品列表不能为空");
        }
        for (AdjustmentItem item : items) {
            if (item.getAdjustmentQuantity() == 0) {
                throw new IllegalArgumentException("调整数量不能为0");
            }
        }
    }
    
    // 内部类定义各种单据项
    public static class PurchaseItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String remark;
        
        // 构造函数和getter/setter
        public PurchaseItem(String skuId, Integer quantity, BigDecimal unitPrice, String remark) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.remark = remark;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public String getRemark() { return remark; }
    }
    
    public static class ProductionItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal unitCost;
        private String remark;
        
        public ProductionItem(String skuId, Integer quantity, BigDecimal unitCost, String remark) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.remark = remark;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public String getRemark() { return remark; }
    }
    
    public static class ReturnItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal originalPrice;
        private String remark;
        
        public ReturnItem(String skuId, Integer quantity, BigDecimal originalPrice, String remark) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.originalPrice = originalPrice;
            this.remark = remark;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public String getRemark() { return remark; }
    }
    
    public static class SaleItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal salePrice;
        private String remark;
        
        public SaleItem(String skuId, Integer quantity, BigDecimal salePrice, String remark) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.salePrice = salePrice;
            this.remark = remark;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getSalePrice() { return salePrice; }
        public String getRemark() { return remark; }
    }
    
    public static class ProductionOutboundItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal unitCost;
        private String remark;
        
        public ProductionOutboundItem(String skuId, Integer quantity, BigDecimal unitCost, String remark) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.remark = remark;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public String getRemark() { return remark; }
    }
    
    public static class TransferItem {
        private String skuId;
        private Integer quantity;
        private BigDecimal unitCost;
        
        public TransferItem(String skuId, Integer quantity, BigDecimal unitCost) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.unitCost = unitCost;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitCost() { return unitCost; }
    }
    
    public static class AdjustmentItem {
        private String skuId;
        private Integer adjustmentQuantity; // 正数为增加，负数为减少
        private BigDecimal unitCost;
        private String adjustmentReason;
        
        public AdjustmentItem(String skuId, Integer adjustmentQuantity, BigDecimal unitCost, String adjustmentReason) {
            this.skuId = skuId;
            this.adjustmentQuantity = adjustmentQuantity;
            this.unitCost = unitCost;
            this.adjustmentReason = adjustmentReason;
        }
        
        public String getSkuId() { return skuId; }
        public Integer getAdjustmentQuantity() { return adjustmentQuantity; }
        public BigDecimal getUnitCost() { return unitCost; }
        public String getAdjustmentReason() { return adjustmentReason; }
    }
}