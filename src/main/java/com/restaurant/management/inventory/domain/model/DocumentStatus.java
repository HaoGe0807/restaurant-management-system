package com.restaurant.management.inventory.domain.model;

/**
 * 库存单据状态枚举
 */
public enum DocumentStatus {
    
    /**
     * 草稿状态 - 单据创建但未提交
     */
    DRAFT("草稿"),
    
    /**
     * 待审核状态 - 单据已提交等待审核
     */
    PENDING("待审核"),
    
    /**
     * 已审核状态 - 单据审核通过等待执行
     */
    APPROVED("已审核"),
    
    /**
     * 已拒绝状态 - 单据审核被拒绝
     */
    REJECTED("已拒绝"),
    
    /**
     * 已执行状态 - 单据已执行完成
     */
    EXECUTED("已执行"),
    
    /**
     * 已取消状态 - 单据已取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    DocumentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为终态
     */
    public boolean isTerminal() {
        return this == EXECUTED || this == CANCELLED;
    }
    
    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this != EXECUTED;
    }
    
    /**
     * 判断是否可以重新提交
     */
    public boolean canResubmit() {
        return this == REJECTED;
    }
    
    /**
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return this == DRAFT;
    }
}