package com.restaurant.management.inventory.application;

import com.restaurant.management.inventory.domain.model.DocumentStatus;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import com.restaurant.management.inventory.domain.repository.StockDocumentRepository;
import com.restaurant.management.inventory.domain.service.StockDocumentExecutionEngine;
import com.restaurant.management.inventory.domain.service.StockDocumentFactory;
import com.restaurant.management.inventory.domain.service.StockDocumentStateMachine;
import com.restaurant.management.common.domain.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 库存单据服务测试
 * 测试库存单据管理的完整业务流程
 */
@ExtendWith(MockitoExtension.class)
class StockDocumentServiceTest {
    
    @Mock
    private StockDocumentRepository stockDocumentRepository;
    
    @Mock
    private StockDocumentFactory stockDocumentFactory;
    
    @Mock
    private StockDocumentStateMachine stateMachine;
    
    @Mock
    private StockDocumentExecutionEngine executionEngine;
    
    @Mock
    private DomainEventPublisher domainEventPublisher;
    
    @InjectMocks
    private StockDocumentService stockDocumentService;
    
    private StockDocument testDocument;
    private List<StockDocumentFactory.PurchaseItem> purchaseItems;
    
    @BeforeEach
    void setUp() {
        // 创建测试数据
        testDocument = new StockDocument();
        testDocument.setDocumentId("DOC123456789");
        testDocument.setDocumentNo("PI-WH001-20231215-0001");
        testDocument.setType(DocumentType.INBOUND_PURCHASE);
        testDocument.setStatus(DocumentStatus.DRAFT);
        testDocument.setWarehouseId("WH001");
        testDocument.setOperatorId("USER001");
        testDocument.setTotalAmount(BigDecimal.valueOf(1000.00));
        testDocument.setRemark("测试采购入库单");
        
        // 创建采购项目
        purchaseItems = Arrays.asList(
            new StockDocumentFactory.PurchaseItem("SKU001", 10, BigDecimal.valueOf(50.00), "商品1"),
            new StockDocumentFactory.PurchaseItem("SKU002", 20, BigDecimal.valueOf(25.00), "商品2")
        );
    }
    
    @Test
    void testCreatePurchaseInboundDocument() {
        // Given
        when(stockDocumentFactory.createPurchaseInboundDocument(anyString(), anyString(), anyList(), anyString()))
            .thenReturn(testDocument);
        when(stockDocumentRepository.save(any(StockDocument.class))).thenReturn(testDocument);
        
        // When
        StockDocument result = stockDocumentService.createPurchaseInboundDocument(
            "WH001", "USER001", purchaseItems, "测试采购入库单");
        
        // Then
        assertNotNull(result);
        assertEquals("DOC123456789", result.getDocumentId());
        assertEquals(DocumentType.INBOUND_PURCHASE, result.getType());
        assertEquals(DocumentStatus.DRAFT, result.getStatus());
        
        verify(stockDocumentFactory).createPurchaseInboundDocument("WH001", "USER001", purchaseItems, "测试采购入库单");
        verify(stockDocumentRepository).save(testDocument);
        verify(domainEventPublisher).publishAll(testDocument.getDomainEvents());
    }
    
    @Test
    void testSubmitDocumentForApproval() {
        // Given
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        doNothing().when(stateMachine).validateSubmitForApproval(testDocument);
        when(stockDocumentRepository.save(any(StockDocument.class))).thenReturn(testDocument);
        
        // When
        stockDocumentService.submitDocumentForApproval("DOC123456789");
        
        // Then
        verify(stateMachine).validateSubmitForApproval(testDocument);
        verify(stockDocumentRepository).save(testDocument);
        verify(domainEventPublisher).publishAll(testDocument.getDomainEvents());
    }
    
    @Test
    void testApproveDocument() {
        // Given
        testDocument.setStatus(DocumentStatus.PENDING);
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        doNothing().when(stateMachine).validateApproval(testDocument, "APPROVER001");
        when(stockDocumentRepository.save(any(StockDocument.class))).thenReturn(testDocument);
        
        // When
        stockDocumentService.approveDocument("DOC123456789", "APPROVER001");
        
        // Then
        verify(stateMachine).validateApproval(testDocument, "APPROVER001");
        verify(stockDocumentRepository).save(testDocument);
        verify(domainEventPublisher).publishAll(testDocument.getDomainEvents());
    }
    
    @Test
    void testExecuteDocument() {
        // Given
        testDocument.setStatus(DocumentStatus.APPROVED);
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        doNothing().when(stateMachine).validateExecution(testDocument);
        doNothing().when(executionEngine).validateInventoryBeforeExecution(testDocument);
        doNothing().when(executionEngine).executeDocument(testDocument);
        when(stockDocumentRepository.save(any(StockDocument.class))).thenReturn(testDocument);
        
        // When
        stockDocumentService.executeDocument("DOC123456789");
        
        // Then
        verify(stateMachine).validateExecution(testDocument);
        verify(executionEngine).validateInventoryBeforeExecution(testDocument);
        verify(executionEngine).executeDocument(testDocument);
        verify(stockDocumentRepository).save(testDocument);
        verify(domainEventPublisher).publishAll(testDocument.getDomainEvents());
    }
    
    @Test
    void testCancelDocument() {
        // Given
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        doNothing().when(stateMachine).validateCancellation(testDocument, "取消原因");
        when(stockDocumentRepository.save(any(StockDocument.class))).thenReturn(testDocument);
        
        // When
        stockDocumentService.cancelDocument("DOC123456789", "取消原因");
        
        // Then
        verify(stateMachine).validateCancellation(testDocument, "取消原因");
        verify(stockDocumentRepository).save(testDocument);
        verify(domainEventPublisher).publishAll(testDocument.getDomainEvents());
    }
    
    @Test
    void testGetDocumentById() {
        // Given
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        
        // When
        StockDocument result = stockDocumentService.getDocumentById("DOC123456789");
        
        // Then
        assertNotNull(result);
        assertEquals("DOC123456789", result.getDocumentId());
        verify(stockDocumentRepository).findByDocumentId("DOC123456789");
    }
    
    @Test
    void testGetDocumentByIdNotFound() {
        // Given
        when(stockDocumentRepository.findByDocumentId("NONEXISTENT")).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> stockDocumentService.getDocumentById("NONEXISTENT"));
        assertEquals("单据不存在: NONEXISTENT", exception.getMessage());
    }
    
    @Test
    void testGetPendingDocuments() {
        // Given
        List<StockDocument> pendingDocuments = Arrays.asList(testDocument);
        when(stockDocumentRepository.findPendingDocuments()).thenReturn(pendingDocuments);
        
        // When
        List<StockDocument> result = stockDocumentService.getPendingDocuments();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDocument, result.get(0));
        verify(stockDocumentRepository).findPendingDocuments();
    }
    
    @Test
    void testGetAvailableActions() {
        // Given
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        List<DocumentStatus> availableActions = Arrays.asList(DocumentStatus.PENDING, DocumentStatus.CANCELLED);
        when(stateMachine.getAvailableTransitions(DocumentStatus.DRAFT)).thenReturn(availableActions);
        
        // When
        List<DocumentStatus> result = stockDocumentService.getAvailableActions("DOC123456789");
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(DocumentStatus.PENDING));
        assertTrue(result.contains(DocumentStatus.CANCELLED));
        verify(stateMachine).getAvailableTransitions(DocumentStatus.DRAFT);
    }
    
    @Test
    void testCanEditDocument() {
        // Given
        when(stockDocumentRepository.findByDocumentId("DOC123456789")).thenReturn(Optional.of(testDocument));
        when(stateMachine.canEdit(DocumentStatus.DRAFT)).thenReturn(true);
        
        // When
        boolean result = stockDocumentService.canEditDocument("DOC123456789");
        
        // Then
        assertTrue(result);
        verify(stateMachine).canEdit(DocumentStatus.DRAFT);
    }
    
    @Test
    void testBatchApproveDocuments() {
        // Given
        List<String> documentIds = Arrays.asList("DOC001", "DOC002", "DOC003");
        StockDocument doc1 = createTestDocument("DOC001", DocumentStatus.PENDING);
        StockDocument doc2 = createTestDocument("DOC002", DocumentStatus.PENDING);
        StockDocument doc3 = createTestDocument("DOC003", DocumentStatus.PENDING);
        
        when(stockDocumentRepository.findByDocumentId("DOC001")).thenReturn(Optional.of(doc1));
        when(stockDocumentRepository.findByDocumentId("DOC002")).thenReturn(Optional.of(doc2));
        when(stockDocumentRepository.findByDocumentId("DOC003")).thenReturn(Optional.of(doc3));
        
        doNothing().when(stateMachine).validateApproval(any(StockDocument.class), eq("APPROVER001"));
        when(stockDocumentRepository.save(any(StockDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        stockDocumentService.batchApproveDocuments(documentIds, "APPROVER001");
        
        // Then
        verify(stockDocumentRepository, times(3)).save(any(StockDocument.class));
        verify(domainEventPublisher, times(3)).publishAll(anyList());
    }
    
    @Test
    void testGetDocumentStatistics() {
        // Given
        when(stockDocumentRepository.countByCreateTimeBetween(any(), any())).thenReturn(100L);
        when(stockDocumentRepository.countByStatus(DocumentStatus.PENDING)).thenReturn(10L);
        when(stockDocumentRepository.countByStatus(DocumentStatus.APPROVED)).thenReturn(20L);
        when(stockDocumentRepository.countByStatus(DocumentStatus.EXECUTED)).thenReturn(60L);
        when(stockDocumentRepository.countByStatus(DocumentStatus.CANCELLED)).thenReturn(10L);
        
        // When
        StockDocumentService.DocumentStatistics result = stockDocumentService.getDocumentStatistics(
            java.time.LocalDateTime.now().minusDays(30), java.time.LocalDateTime.now());
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalCount());
        assertEquals(10L, result.getPendingCount());
        assertEquals(20L, result.getApprovedCount());
        assertEquals(60L, result.getExecutedCount());
        assertEquals(10L, result.getCancelledCount());
    }
    
    private StockDocument createTestDocument(String documentId, DocumentStatus status) {
        StockDocument document = new StockDocument();
        document.setDocumentId(documentId);
        document.setStatus(status);
        document.setType(DocumentType.INBOUND_PURCHASE);
        document.setWarehouseId("WH001");
        document.setOperatorId("USER001");
        return document;
    }
}