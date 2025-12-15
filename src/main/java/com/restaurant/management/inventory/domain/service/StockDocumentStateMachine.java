package com.restaurant.management.inventory.domain.service;

import com.restaurant.management.inventory.domain.model.DocumentStatus;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 库存单据状态机
 * 管理单据的完整生命周期，确保状态转换的合法性和一致性
 */
@Service
public class StockDocumentStateMachine {
    
    /**
     * 定义各状态的合法后续状态
     */
    private static final Map<DocumentStatus, List<DocumentStatus>> STATE_TRANSITIONS = new EnumMap<>(DocumentStatus.class);
    
    static {
        // 草稿状态可以转换为：待审核、已取消
        STATE_TRANSITIONS.put(DocumentStatus.DRAFT, Arrays.asList(
            DocumentStatus.PENDING, DocumentStatus.CANCELLED
        ));
        
        // 待审核状态可以转换为：已审核、已拒绝、已取消
        STATE_TRANSITIONS.put(DocumentStatus.PENDING, Arrays.asList(
            DocumentStatus.APPROVED, DocumentStatus.REJECTED, DocumentStatus.CANCELLED
        ));
        
        // 已审核状态可以转换为：已执行、已取消
        STATE_TRANSITIONS.put(DocumentStatus.APPROVED, Arrays.asList(
            DocumentStatus.EXECUTED, DocumentStatus.CANCELLED
        ));
        
        // 已拒绝状态可以转换为：草稿（重新编辑）、已取消
        STATE_TRANSITIONS.put(DocumentStatus.REJECTED, Arrays.asList(
            DocumentStatus.DRAFT, DocumentStatus.CANCELLED
        ));
        
        // 已执行和已取消状态为终态，不能再转换
        STATE_TRANSITIONS.put(DocumentStatus.EXECUTED, Arrays.asList());
        STATE_TRANSITIONS.put(DocumentStatus.CANCELLED, Arrays.asList());
    }
    
    /**
     * 检查状态转换是否合法
     */
    public boolean canTransition(DocumentStatus fromStatus, DocumentStatus toStatus) {
        List<DocumentStatus> allowedTransitions = STATE_TRANSITIONS.get(fromStatus);
        return allowedTransitions != null && allowedTransitions.contains(toStatus);
    }
    
    /**
     * 验证单据是否可以提交审核
     */
    public void validateSubmitForApproval(StockDocument document) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的单据才能提交审核");
        }
        
        // 验证单据完整性
        validateDocumentCompleteness(document);
        
        // 根据单据类型进行特殊验证
        validateByDocumentType(document);
    }
    
    /**
     * 验证单据是否可以审核通过
     */
    public void validateApproval(StockDocument document, String approverId) {
        if (document.getStatus() != DocumentStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的单据才能审核");
        }
        
        if (approverId == null || approverId.trim().isEmpty()) {
            throw new IllegalArgumentException("审核人ID不能为空");
        }
        
        // 验证审核权限（这里可以扩展为更复杂的权限验证）
        validateApprovalPermission(document, approverId);
    }
    
    /**
     * 验证单据是否可以拒绝
     */
    public void validateRejection(StockDocument document, String approverId, String rejectionReason) {
        if (document.getStatus() != DocumentStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的单据才能拒绝");
        }
        
        if (approverId == null || approverId.trim().isEmpty()) {
            throw new IllegalArgumentException("审核人ID不能为空");
        }
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("拒绝原因不能为空");
        }
    }
    
    /**
     * 验证单据是否可以执行
     */
    public void validateExecution(StockDocument document) {
        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new IllegalStateException("只有已审核状态的单据才能执行");
        }
        
        // 验证执行前置条件
        validateExecutionPreconditions(document);
    }
    
    /**
     * 验证单据是否可以取消
     */
    public void validateCancellation(StockDocument document, String cancelReason) {
        if (document.getStatus() == DocumentStatus.EXECUTED) {
            throw new IllegalStateException("已执行的单据不能取消");
        }
        
        if (document.getStatus() == DocumentStatus.CANCELLED) {
            throw new IllegalStateException("单据已经是取消状态");
        }
        
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new IllegalArgumentException("取消原因不能为空");
        }
    }
    
    /**
     * 获取当前状态的可转换状态列表
     */
    public List<DocumentStatus> getAvailableTransitions(DocumentStatus currentStatus) {
        return STATE_TRANSITIONS.getOrDefault(currentStatus, Arrays.asList());
    }
    
    /**
     * 检查单据是否为终态
     */
    public boolean isFinalState(DocumentStatus status) {
        return status == DocumentStatus.EXECUTED || status == DocumentStatus.CANCELLED;
    }
    
    /**
     * 检查单据是否可以编辑
     */
    public boolean canEdit(DocumentStatus status) {
        return status == DocumentStatus.DRAFT;
    }
    
    /**
     * 验证单据完整性
     */
    private void validateDocumentCompleteness(StockDocument document) {
        if (document.getItems() == null || document.getItems().isEmpty()) {
            throw new IllegalArgumentException("单据明细不能为空");
        }
        
        if (document.getWarehouseId() == null || document.getWarehouseId().trim().isEmpty()) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        
        if (document.getOperatorId() == null || document.getOperatorId().trim().isEmpty()) {
            throw new IllegalArgumentException("操作人ID不能为空");
        }
        
        // 验证明细项
        document.getItems().forEach(item -> {
            if (item.getSkuId() == null || item.getSkuId().trim().isEmpty()) {
                throw new IllegalArgumentException("商品SKU ID不能为空");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
                throw new IllegalArgumentException("商品单价不能为负数");
            }
        });
    }
    
    /**
     * 根据单据类型进行特殊验证
     */
    private void validateByDocumentType(StockDocument document) {
        DocumentType type = document.getType();
        
        switch (type) {
            case INBOUND_PURCHASE:
                validatePurchaseInbound(document);
                break;
            case OUTBOUND_SALE:
                validateSaleOutbound(document);
                break;
            case TRANSFER:
                validateTransfer(document);
                break;
            case ADJUSTMENT:
                validateAdjustment(document);
                break;
            default:
                // 其他类型的特殊验证可以在这里添加
                break;
        }
    }
    
    /**
     * 验证采购入库单
     */
    private void validatePurchaseInbound(StockDocument document) {
        // 采购入库单的特殊验证逻辑
        document.getItems().forEach(item -> {
            if (item.getUnitPrice().signum() <= 0) {
                throw new IllegalArgumentException("采购入库单的商品单价必须大于0");
            }
        });
    }
    
    /**
     * 验证销售出库单
     */
    private void validateSaleOutbound(StockDocument document) {
        // 销售出库单的特殊验证逻辑
        // 这里可以添加库存充足性检查等
    }
    
    /**
     * 验证调拨单
     */
    private void validateTransfer(StockDocument document) {
        // 调拨单的特殊验证逻辑
        if (document.getRemark() == null || !document.getRemark().contains("目标仓库")) {
            throw new IllegalArgumentException("调拨单必须指定目标仓库");
        }
    }
    
    /**
     * 验证调整单
     */
    private void validateAdjustment(StockDocument document) {
        // 调整单的特殊验证逻辑
        // 调整单可以有负数量（减少库存）
    }
    
    /**
     * 验证审核权限
     */
    private void validateApprovalPermission(StockDocument document, String approverId) {
        // 这里可以实现复杂的权限验证逻辑
        // 例如：检查审核人是否有权限审核该类型的单据
        // 例如：检查审核人是否有权限审核该金额范围的单据
        
        // 简单的验证：操作人不能审核自己创建的单据
        if (document.getOperatorId().equals(approverId)) {
            throw new IllegalStateException("操作人不能审核自己创建的单据");
        }
    }
    
    /**
     * 验证执行前置条件
     */
    private void validateExecutionPreconditions(StockDocument document) {
        // 根据单据类型验证执行前置条件
        DocumentType type = document.getType();
        
        switch (type) {
            case OUTBOUND_SALE:
            case OUTBOUND_PRODUCTION:
            case TRANSFER:
                // 出库类单据需要验证库存充足性
                validateInventoryAvailability(document);
                break;
            default:
                // 其他类型的前置条件验证
                break;
        }
    }
    
    /**
     * 验证库存可用性（这里是简化版本，实际应该调用库存服务）
     */
    private void validateInventoryAvailability(StockDocument document) {
        // 这里应该调用库存服务检查每个SKU的可用库存
        // 为了保持领域服务的纯净性，这里只做基本验证
        // 实际的库存检查应该在应用服务层进行
        
        if (document.getItems().isEmpty()) {
            throw new IllegalArgumentException("出库单据明细不能为空");
        }
    }
}