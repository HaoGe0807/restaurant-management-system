package com.restaurant.management.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.event.WarehouseActivatedEvent;
import com.restaurant.management.inventory.domain.event.WarehouseDeactivatedEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 仓库聚合根
 * 管理仓库的基本信息和状态
 */
@Getter
@Setter
@TableName("warehouse")
public class Warehouse extends BaseEntity implements AggregateRoot {
    
    /**
     * 存储该聚合根产生的所有领域事件（仅存在于内存）
     */
    @TableField(exist = false)
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 仓库ID（业务主键）
     */
    @TableField("warehouse_id")
    private String warehouseId;
    
    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;
    
    /**
     * 仓库编码
     */
    @TableField("warehouse_code")
    private String warehouseCode;
    
    /**
     * 仓库地址
     */
    private String address;
    
    /**
     * 仓库管理员ID
     */
    @TableField("manager_id")
    private String managerId;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 仓库状态
     */
    private WarehouseStatus status;
    
    /**
     * 仓库类型
     */
    @TableField("warehouse_type")
    private WarehouseType type;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建仓库
     */
    public static Warehouse create(String warehouseName, String warehouseCode, String address, 
                                 String managerId, WarehouseType type) {
        Warehouse warehouse = new Warehouse();
        warehouse.warehouseId = generateWarehouseId();
        warehouse.warehouseName = warehouseName;
        warehouse.warehouseCode = warehouseCode;
        warehouse.address = address;
        warehouse.managerId = managerId;
        warehouse.type = type;
        warehouse.status = WarehouseStatus.ACTIVE;
        
        return warehouse;
    }
    
    /**
     * 激活仓库
     */
    public void activate() {
        if (status == WarehouseStatus.ACTIVE) {
            return;
        }
        
        this.status = WarehouseStatus.ACTIVE;
        
        // 发布仓库激活事件
        addDomainEvent(new WarehouseActivatedEvent(this.warehouseId, this.warehouseName));
    }
    
    /**
     * 停用仓库
     */
    public void deactivate(String reason) {
        if (status == WarehouseStatus.INACTIVE) {
            return;
        }
        
        this.status = WarehouseStatus.INACTIVE;
        this.remark = reason;
        
        // 发布仓库停用事件
        addDomainEvent(new WarehouseDeactivatedEvent(this.warehouseId, this.warehouseName, reason));
    }
    
    /**
     * 更新仓库信息
     */
    public void updateInfo(String warehouseName, String address, String managerId, String phone) {
        this.warehouseName = warehouseName;
        this.address = address;
        this.managerId = managerId;
        this.phone = phone;
    }
    
    /**
     * 验证仓库是否可用
     */
    public void validateAvailable() {
        if (status != WarehouseStatus.ACTIVE) {
            throw new IllegalStateException("仓库未激活，无法进行库存操作");
        }
    }
    
    /**
     * 生成仓库ID
     */
    private static String generateWarehouseId() {
        return "WH" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
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
}