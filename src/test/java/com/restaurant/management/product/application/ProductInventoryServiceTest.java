package com.restaurant.management.product.application;

import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import com.restaurant.management.product.application.command.CreateProductCommand;
import com.restaurant.management.product.domain.model.ProductSpu;
import com.restaurant.management.product.domain.model.ProductStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品库存联动服务测试
 */
@ExtendWith(MockitoExtension.class)
class ProductInventoryServiceTest {
    
    @Mock
    private com.restaurant.management.product.domain.service.ProductDomainService productDomainService;
    
    @Mock
    private InventoryDomainService inventoryDomainService;
    
    @InjectMocks
    private ProductInventoryService productInventoryService;
    
    @Test
    void testCreateProductWithInventory() {
        // Given
        CreateProductCommand command = new CreateProductCommand();
        command.setSpuName("测试商品");
        command.setDescription("测试商品描述");
        
        ProductSpu mockProduct = ProductSpu.create("测试商品", "测试商品描述");
        
        when(productDomainService.createProduct(any(CreateProductCommand.class)))
            .thenReturn(mockProduct);
        
        // When
        ProductSpu result = productInventoryService.createProductWithInventory(command);
        
        // Then
        assertNotNull(result);
        assertEquals("测试商品", result.getSpuName());
        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        
        // 验证商品创建服务被调用
        verify(productDomainService).createProduct(command);
        
        // 验证商品创建事件被发布（通过检查领域事件）
        assertFalse(result.getDomainEvents().isEmpty());
    }
    
    @Test
    void testActivateProductWithInventory() {
        // Given
        String spuId = "SPU123";
        ProductSpu mockProduct = ProductSpu.create("测试商品", "测试商品描述");
        mockProduct.deactivate(); // 先设置为下架状态
        
        when(productDomainService.getProductSpu(spuId)).thenReturn(mockProduct);
        when(productDomainService.updateProduct(any(ProductSpu.class))).thenReturn(mockProduct);
        
        // When
        ProductSpu result = productInventoryService.activateProductWithInventory(spuId);
        
        // Then
        assertNotNull(result);
        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        
        // 验证服务调用
        verify(productDomainService).getProductSpu(spuId);
        verify(productDomainService).updateProduct(mockProduct);
        
        // 验证状态变更事件被发布
        assertTrue(result.getDomainEvents().stream()
            .anyMatch(event -> event instanceof com.restaurant.management.product.domain.event.ProductStatusChangedEvent));
    }
    
    @Test
    void testValidateProductInventoryConsistency() {
        // Given
        String spuId = "SPU123";
        ProductSpu mockProduct = ProductSpu.create("测试商品", "测试商品描述");
        
        // 模拟SKU
        com.restaurant.management.product.domain.model.ProductSku sku1 = 
            com.restaurant.management.product.domain.model.ProductSku.create("SKU001", "测试SKU1", BigDecimal.valueOf(10.0), 100);
        com.restaurant.management.product.domain.model.ProductSku sku2 = 
            com.restaurant.management.product.domain.model.ProductSku.create("SKU002", "测试SKU2", BigDecimal.valueOf(20.0), 200);
        
        mockProduct.addSku(sku1);
        mockProduct.addSku(sku2);
        
        // 模拟库存
        Inventory inventory1 = Inventory.create("SKU001", "WH001", 100);
        Inventory inventory2 = Inventory.create("SKU002", "WH001", 200);
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        
        when(productDomainService.getProductSpu(spuId)).thenReturn(mockProduct);
        when(inventoryDomainService.findInventoriesBySpuId(spuId)).thenReturn(inventories);
        
        // When
        ProductInventoryService.ProductInventoryConsistencyReport report = 
            productInventoryService.validateProductInventoryConsistency(spuId);
        
        // Then
        assertNotNull(report);
        assertEquals(spuId, report.getSpuId());
        assertEquals("测试商品", report.getSpuName());
        assertEquals(2, report.getSkuCount());
        assertEquals(2, report.getInventoryRecordCount());
        assertTrue(report.isConsistent());
        assertTrue(report.getIssues().isEmpty());
        
        // 验证服务调用
        verify(productDomainService).getProductSpu(spuId);
        verify(inventoryDomainService).findInventoriesBySpuId(spuId);
    }
    
    @Test
    void testValidateProductInventoryConsistency_Inconsistent() {
        // Given
        String spuId = "SPU123";
        ProductSpu mockProduct = ProductSpu.create("测试商品", "测试商品描述");
        
        // 模拟2个SKU
        com.restaurant.management.product.domain.model.ProductSku sku1 = 
            com.restaurant.management.product.domain.model.ProductSku.create("SKU001", "测试SKU1", BigDecimal.valueOf(10.0), 100);
        com.restaurant.management.product.domain.model.ProductSku sku2 = 
            com.restaurant.management.product.domain.model.ProductSku.create("SKU002", "测试SKU2", BigDecimal.valueOf(20.0), 200);
        
        mockProduct.addSku(sku1);
        mockProduct.addSku(sku2);
        
        // 模拟只有1个库存记录（缺少SKU002的库存）
        Inventory inventory1 = Inventory.create("SKU001", "WH001", 100);
        List<Inventory> inventories = Arrays.asList(inventory1);
        
        when(productDomainService.getProductSpu(spuId)).thenReturn(mockProduct);
        when(inventoryDomainService.findInventoriesBySpuId(spuId)).thenReturn(inventories);
        
        // When
        ProductInventoryService.ProductInventoryConsistencyReport report = 
            productInventoryService.validateProductInventoryConsistency(spuId);
        
        // Then
        assertNotNull(report);
        assertEquals(spuId, report.getSpuId());
        assertEquals(2, report.getSkuCount());
        assertEquals(1, report.getInventoryRecordCount());
        assertFalse(report.isConsistent()); // 应该不一致
        assertFalse(report.getIssues().isEmpty()); // 应该有问题报告
        assertTrue(report.getIssues().contains("存在SKU没有对应的库存记录"));
    }
}