package com.restaurant.management.inventory.infrastructure.event;

import com.restaurant.management.inventory.domain.service.InventoryDomainService;
import com.restaurant.management.product.domain.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 商品创建事件处理器
 * 监听ProductCreatedEvent，自动创建库存
 * 
 * 规则：最终一致性场景，使用领域事件异步处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreatedEventHandler {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * 处理商品创建事件
     * 在商品创建事务提交后异步执行
     */
    @Async("domainEventExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreatedEvent event) {
        try {
            log.info("收到商品创建事件，SPU: {}, SKU数量: {}", event.getSpuId(), event.getSkus().size());

            event.getSkus().forEach(skuSnapshot -> {
                log.info("创建库存，SKU: {}, 初始库存: {}", skuSnapshot.getSkuId(), skuSnapshot.getInitialQuantity());
                inventoryDomainService.createInventory(
                        skuSnapshot.getSkuId(),
                        skuSnapshot.getInitialQuantity()
                );
            });
            
            log.info("库存创建成功，SPU: {}", event.getSpuId());
        } catch (Exception e) {
            log.error("处理商品创建事件失败，SPU: {}", event.getSpuId(), e);
            // 可以在这里实现重试机制或补偿逻辑
            throw e;
        }
    }
}

