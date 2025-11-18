package com.restaurant.management.product.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 商品 SKU（库存量单位）
 */
@Getter
@Setter
@TableName("product_sku")
public class ProductSku extends BaseEntity {

    @TableField("sku_id")
    private String skuId;

    @TableField("spu_id")
    private String spuId;

    @TableField("sku_name")
    private String skuName;

    private BigDecimal price;

    /**
     * JSON 存储规格属性
     */
    @TableField("attributes")
    private String attributes;

    private ProductStatus status;

    @TableField(exist = false)
    private int initialQuantity;

    public static ProductSku create(String skuName, BigDecimal price, String attributes, int initialQuantity) {
        ProductSku sku = new ProductSku();
        sku.skuId = generateSkuId();
        sku.skuName = skuName;
        sku.price = price;
        sku.attributes = attributes;
        sku.status = ProductStatus.ACTIVE;
        sku.initialQuantity = Math.max(initialQuantity, 0);
        return sku;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    private static String generateSkuId() {
        return "SKU" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}


