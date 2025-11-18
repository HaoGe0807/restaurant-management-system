package com.restaurant.management.product.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.product.domain.event.ProductCreatedEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 商品 SPU（标准产品单元）聚合根
 * 负责维护商品的基础信息以及其下属的 SKU 列表
 */
@Getter
@Setter
@TableName("product_spu")
public class ProductSpu extends BaseEntity implements AggregateRoot {

    /**
     * 存储该聚合根产生的所有领域事件（仅存在于内存）
     */
    @TableField(exist = false)
    private List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * SPU 对应的 SKU 列表
     */
    @TableField(exist = false)
    private List<ProductSku> skus = new ArrayList<>();

    /**
     * 业务 ID
     */
    @TableField("spu_id")
    private String spuId;

    /**
     * 商品名称（SPU 级别）
     */
    @TableField("spu_name")
    private String spuName;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品状态
     */
    private ProductStatus status;

    public static ProductSpu create(String spuName, String description) {
        ProductSpu spu = new ProductSpu();
        spu.spuId = generateSpuId();
        spu.spuName = spuName;
        spu.description = description;
        spu.status = ProductStatus.ACTIVE;
        return spu;
    }

    public void addSku(ProductSku sku) {
        if (sku == null) {
            return;
        }
        sku.setSpuId(this.spuId);
        this.skus.add(sku);
    }

    public void replaceSkus(List<ProductSku> newSkus) {
        this.skus.clear();
        if (newSkus == null) {
            return;
        }
        newSkus.forEach(this::addSku);
    }

    /**
     * 发布商品创建事件，携带所有 SKU 的初始化信息
     */
    public void publishProductCreatedEvent() {
        if (this.skus == null || this.skus.isEmpty()) {
            return;
        }
        List<ProductCreatedEvent.SkuSnapshot> skuSnapshots = this.skus.stream()
                .map(sku -> new ProductCreatedEvent.SkuSnapshot(
                        sku.getSkuId(),
                        sku.getSkuName(),
                        sku.getInitialQuantity()
                ))
                .collect(Collectors.toList());

        addDomainEvent(new ProductCreatedEvent(this.spuId, this.spuName, skuSnapshots));
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        return domainEvents;
    }

    @Override
    public void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public List<ProductSku> getSkus() {
        return skus;
    }

    private static String generateSpuId() {
        return "SPU" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}


