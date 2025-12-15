package com.restaurant.management.product.application;

import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import com.restaurant.management.product.application.command.CreateProductCommand;
import com.restaurant.management.product.application.command.UpdateProductCommand;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品库存联动应用服务
 * 负责协调商品和库存的联动操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInventoryService {
    
    private final ProductDomainService productDomainService;
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * 创建商品并自动创建库存
     */
    @Transactional
    public ProductSpu createProductWithInventory(CreateProductCommand command) {
        log.info("创建商品并自动创建库存: spuName={}", command.getSpuName());
        
        try {
            // 1. 创建商品
            ProductSpu product = productDomainService.createProduct(command);
            
            // 2. 商品创建成功后，发布商品创建事件
            // 事件处理器会自动创建库存记录
            product.publishProductCreatedEvent();
            
            log.info("商品创建完成，等待库存自动创建: spuId={}", product.getSpuId());
            
            return product;
            
        } catch (Exception e) {
            log.error("创建商品失败: spuName={}", command.getSpuName(), e);
            throw e;
        }
    }
    
    /**
     * 更新商品并同步库存信息
     */
    @Transactional
    public ProductSpu updateProductWithInventorySync(String spuId, UpdateProductCommand command) {
        log.info("更新商品并同步库存信息: spuId={}", spuId);
        
        try {
            // 1. 更新商品
            ProductSpu product = productDomainService.updateProduct(spuId, command);
            
            // 2. 发布商品更新事件
            // 事件处理器会自动同步库存中的商品信息
            product.updateProductInfo(command.getSpuName(), command.getDescription());
            
            log.info("商品更新完成，等待库存信息同步: spuId={}", spuId);
            
            return product;
            
        } catch (Exception e) {
            log.error("更新商品失败: spuId={}", spuId, e);
            throw e;
        }
    }
    
    /**
     * 删除商品并清理库存数据
     */
    @Transactional
    public void deleteProductWithInventoryCleanup(String spuId) {
        log.info("删除商品并清理库存数据: spuId={}", spuId);
        
        try {
            // 1. 获取商品信息
            ProductSpu product = productDomainService.getProductSpu(spuId);
            
            // 2. 验证商品状态并准备删除
            product.prepareForDeletion();
            
            // 3. 发布商品删除事件
            // 事件处理器会自动清理相关库存数据
            
            // 4. 删除商品
            productDomainService.deleteProduct(spuId);
            
            log.info("商品删除完成，等待库存数据清理: spuId={}", spuId);
            
        } catch (Exception e) {
            log.error("删除商品失败: spuId={}", spuId, e);
            throw e;
        }
    }
    
    /**
     * 商品上架并解冻库存
     */
    @Transactional
    public ProductSpu activateProductWithInventory(String spuId) {
        log.info("商品上架并解冻库存: spuId={}", spuId);
        
        try {
            // 1. 获取商品
            ProductSpu product = productDomainService.getProductSpu(spuId);
            
            // 2. 上架商品
            product.activate();
            
            // 3. 保存商品状态变更
            productDomainService.updateProduct(product);
            
            // 4. 事件处理器会自动解冻相关库存
            
            log.info("商品上架完成，等待库存解冻: spuId={}", spuId);
            
            return product;
            
        } catch (Exception e) {
            log.error("商品上架失败: spuId={}", spuId, e);
            throw e;
        }
    }
    
    /**
     * 商品下架并冻结库存
     */
    @Transactional
    public ProductSpu deactivateProductWithInventory(String spuId) {
        log.info("商品下架并冻结库存: spuId={}", spuId);
        
        try {
            // 1. 获取商品
            ProductSpu product = productDomainService.getProductSpu(spuId);
            
            // 2. 下架商品
            product.deactivate();
            
            // 3. 保存商品状态变更
            productDomainService.updateProduct(product);
            
            // 4. 事件处理器会自动冻结相关库存
            
            log.info("商品下架完成，等待库存冻结: spuId={}", spuId);
            
            return product;
            
        } catch (Exception e) {
            log.error("商品下架失败: spuId={}", spuId, e);
            throw e;
        }
    }
    
    /**
     * 验证商品库存一致性
     */
    @Transactional(readOnly = true)
    public ProductInventoryConsistencyReport validateProductInventoryConsistency(String spuId) {
        log.info("验证商品库存一致性: spuId={}", spuId);
        
        try {
            // 1. 获取商品信息
            ProductSpu product = productDomainService.getProductSpu(spuId);
            
            // 2. 获取库存信息
            var inventories = inventoryDomainService.findInventoriesBySpuId(spuId);
            
            // 3. 验证一致性
            ProductInventoryConsistencyReport report = new ProductInventoryConsistencyReport();
            report.setSpuId(spuId);
            report.setSpuName(product.getSpuName());
            report.setProductStatus(product.getStatus());
            report.setSkuCount(product.getSkus().size());
            report.setInventoryRecordCount(inventories.size());
            
            // 检查每个SKU是否都有对应的库存记录
            boolean allSkusHaveInventory = product.getSkus().stream()
                .allMatch(sku -> inventories.stream()
                    .anyMatch(inv -> inv.getSkuId().equals(sku.getSkuId())));
            
            report.setConsistent(allSkusHaveInventory);
            
            if (!allSkusHaveInventory) {
                report.addIssue("存在SKU没有对应的库存记录");
            }
            
            // 检查库存状态与商品状态是否一致
            boolean statusConsistent = inventories.stream()
                .allMatch(inv -> {
                    if (product.getStatus() == com.restaurant.management.product.domain.model.ProductStatus.INACTIVE) {
                        return inv.getStatus() == com.restaurant.management.inventory.domain.model.InventoryStatus.FROZEN;
                    } else {
                        return inv.getStatus() == com.restaurant.management.inventory.domain.model.InventoryStatus.NORMAL;
                    }
                });
            
            if (!statusConsistent) {
                report.addIssue("库存状态与商品状态不一致");
                report.setConsistent(false);
            }
            
            log.info("商品库存一致性验证完成: spuId={}, consistent={}", spuId, report.isConsistent());
            
            return report;
            
        } catch (Exception e) {
            log.error("验证商品库存一致性失败: spuId={}", spuId, e);
            throw e;
        }
    }
    
    /**
     * 商品库存一致性报告
     */
    public static class ProductInventoryConsistencyReport {
        private String spuId;
        private String spuName;
        private com.restaurant.management.product.domain.model.ProductStatus productStatus;
        private int skuCount;
        private int inventoryRecordCount;
        private boolean consistent;
        private java.util.List<String> issues = new java.util.ArrayList<>();
        
        // getters and setters
        public String getSpuId() { return spuId; }
        public void setSpuId(String spuId) { this.spuId = spuId; }
        
        public String getSpuName() { return spuName; }
        public void setSpuName(String spuName) { this.spuName = spuName; }
        
        public com.restaurant.management.product.domain.model.ProductStatus getProductStatus() { return productStatus; }
        public void setProductStatus(com.restaurant.management.product.domain.model.ProductStatus productStatus) { this.productStatus = productStatus; }
        
        public int getSkuCount() { return skuCount; }
        public void setSkuCount(int skuCount) { this.skuCount = skuCount; }
        
        public int getInventoryRecordCount() { return inventoryRecordCount; }
        public void setInventoryRecordCount(int inventoryRecordCount) { this.inventoryRecordCount = inventoryRecordCount; }
        
        public boolean isConsistent() { return consistent; }
        public void setConsistent(boolean consistent) { this.consistent = consistent; }
        
        public java.util.List<String> getIssues() { return issues; }
        public void setIssues(java.util.List<String> issues) { this.issues = issues; }
        
        public void addIssue(String issue) { this.issues.add(issue); }
    }
}