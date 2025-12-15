package com.restaurant.management.inventory.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.inventory.domain.model.Inventory;
import com.restaurant.management.inventory.domain.model.InventoryStatus;
import com.restaurant.management.inventory.domain.repository.InventoryRepository;
import com.restaurant.management.inventory.infrastructure.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    
    private final InventoryMapper inventoryMapper;
    
    @Override
    public Inventory save(Inventory inventory) {
        if (inventory.getId() == null) {
            inventoryMapper.insert(inventory);
        } else {
            inventoryMapper.updateById(inventory);
        }
        return inventory;
    }
    
    @Override
    public Optional<Inventory> findBySkuIdAndWarehouseId(String skuId, String warehouseId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getSkuId, skuId)
            .eq(Inventory::getWarehouseId, warehouseId);
        
        Inventory inventory = inventoryMapper.selectOne(queryWrapper);
        return Optional.ofNullable(inventory);
    }
    
    @Override
    public List<Inventory> findBySkuId(String skuId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getSkuId, skuId);
        
        return inventoryMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Inventory> findByWarehouseId(String warehouseId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getWarehouseId, warehouseId);
        
        return inventoryMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Inventory> findBySpuId(String spuId) {
        // 这里需要关联查询商品表来获取SPU下的所有SKU
        // 简化实现，实际项目中需要通过JOIN查询或者先查询SKU列表再查询库存
        return inventoryMapper.findBySpuId(spuId);
    }
    
    @Override
    public void delete(Inventory inventory) {
        inventoryMapper.deleteById(inventory.getId());
    }
    
    @Override
    public void deleteById(Long id) {
        inventoryMapper.deleteById(id);
    }
    
    @Override
    public void deleteBySkuIdAndWarehouseId(String skuId, String warehouseId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getSkuId, skuId)
            .eq(Inventory::getWarehouseId, warehouseId);
        
        inventoryMapper.delete(queryWrapper);
    }
    
    @Override
    public boolean existsBySkuIdAndWarehouseId(String skuId, String warehouseId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getSkuId, skuId)
            .eq(Inventory::getWarehouseId, warehouseId);
        
        return inventoryMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public List<Inventory> findInventoriesNeedingReplenishment() {
        // 使用Mapper中的专门方法
        return inventoryMapper.findInventoriesNeedingReplenishment();
    }
    
    @Override
    public List<Inventory> findOverstockedInventories() {
        // 使用Mapper中的专门方法
        return inventoryMapper.findOverstockedInventories();
    }
    
    @Override
    public List<Inventory> findByStatus(InventoryStatus status) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<Inventory>()
            .eq(Inventory::getStatus, status);
        
        return inventoryMapper.selectList(queryWrapper);
    }
}