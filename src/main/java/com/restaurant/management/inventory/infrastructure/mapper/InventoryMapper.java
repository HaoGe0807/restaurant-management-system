package com.restaurant.management.inventory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.management.inventory.domain.model.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存Mapper接口
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    /**
     * 根据SPU ID查找所有相关库存
     * 需要关联商品表查询
     */
    @Select("""
        SELECT i.* FROM inventories i
        INNER JOIN product_sku ps ON i.sku_id = ps.sku_id
        WHERE ps.spu_id = #{spuId}
    """)
    List<Inventory> findBySpuId(@Param("spuId") String spuId);
    
    /**
     * 查找需要补货的库存
     */
    @Select("""
        SELECT * FROM inventories
        WHERE (available_quantity + reserved_quantity + occupied_quantity) <= safety_stock
        AND status = 'NORMAL'
    """)
    List<Inventory> findInventoriesNeedingReplenishment();
    
    /**
     * 查找库存积压的商品
     */
    @Select("""
        SELECT * FROM inventories
        WHERE (available_quantity + reserved_quantity + occupied_quantity) >= max_stock
        AND status = 'NORMAL'
    """)
    List<Inventory> findOverstockedInventories();
    
    /**
     * 获取库存汇总统计
     */
    @Select("""
        SELECT
            sku_id,
            SUM(available_quantity) as total_available,
            SUM(reserved_quantity) as total_reserved,
            SUM(occupied_quantity) as total_occupied
        FROM inventories
        WHERE sku_id = #{skuId}
        GROUP BY sku_id
    """)
    InventorySummaryDto getInventorySummary(@Param("skuId") String skuId);
    
    /**
     * 批量更新库存状态
     */
    @Select("""
        UPDATE inventories
        SET status = #{status}
        WHERE sku_id IN
        <foreach collection="skuIds" item="skuId" open="(" separator="," close=")">
            #{skuId}
        </foreach>
    """)
    void batchUpdateStatus(@Param("skuIds") List<String> skuIds, @Param("status") String status);
    
    /**
     * 库存汇总DTO
     */
    class InventorySummaryDto {
        private String skuId;
        private Integer totalAvailable;
        private Integer totalReserved;
        private Integer totalOccupied;
        
        // getters and setters
        public String getSkuId() { return skuId; }
        public void setSkuId(String skuId) { this.skuId = skuId; }
        
        public Integer getTotalAvailable() { return totalAvailable; }
        public void setTotalAvailable(Integer totalAvailable) { this.totalAvailable = totalAvailable; }
        
        public Integer getTotalReserved() { return totalReserved; }
        public void setTotalReserved(Integer totalReserved) { this.totalReserved = totalReserved; }
        
        public Integer getTotalOccupied() { return totalOccupied; }
        public void setTotalOccupied(Integer totalOccupied) { this.totalOccupied = totalOccupied; }
    }
}