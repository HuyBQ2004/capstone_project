package com.sfms.mf07.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mẫu số 1 phần I — thực phẩm tươi sống (QĐ 1246 Điều 3). Bảng con, KHÔNG có cột audit/`code`
 * riêng (M07-D01) — PK UUID sinh thẳng ở DB, không lộ qua path param.
 */
@Entity
@Table(name = "step1_fresh_rows")
public class Step1FreshRow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InspectionSession session;

    @Column(name = "row_order", nullable = false)
    private int rowOrder;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "received_at", nullable = false, length = 32)
    private String receivedAt;

    @Column(nullable = false, length = 32)
    private String quantity;

    @Column(name = "supplier_name", nullable = false, length = 160)
    private String supplierName;

    @Column(name = "supplier_contact", length = 200)
    private String supplierContact;

    @Column(length = 120)
    private String deliverer;

    @Column(name = "has_invoice", nullable = false)
    private boolean hasInvoice;

    @Column(name = "has_vet_cert", nullable = false)
    private boolean hasVetCert;

    @Column(name = "has_quarantine_cert", nullable = false)
    private boolean hasQuarantineCert;

    @Column(name = "sensory_pass", nullable = false)
    private boolean sensoryPass;

    @Column(name = "quick_test", nullable = false, length = 8)
    private String quickTest = "NONE";

    @Column(length = 500)
    private String action;

    @Column(name = "photo_count", nullable = false)
    private int photoCount;

    public UUID getId() {
        return id;
    }

    public InspectionSession getSession() {
        return session;
    }

    public void setSession(InspectionSession session) {
        this.session = session;
    }

    public int getRowOrder() {
        return rowOrder;
    }

    public void setRowOrder(int rowOrder) {
        this.rowOrder = rowOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public String getDeliverer() {
        return deliverer;
    }

    public void setDeliverer(String deliverer) {
        this.deliverer = deliverer;
    }

    public boolean isHasInvoice() {
        return hasInvoice;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public boolean isHasVetCert() {
        return hasVetCert;
    }

    public void setHasVetCert(boolean hasVetCert) {
        this.hasVetCert = hasVetCert;
    }

    public boolean isHasQuarantineCert() {
        return hasQuarantineCert;
    }

    public void setHasQuarantineCert(boolean hasQuarantineCert) {
        this.hasQuarantineCert = hasQuarantineCert;
    }

    public boolean isSensoryPass() {
        return sensoryPass;
    }

    public void setSensoryPass(boolean sensoryPass) {
        this.sensoryPass = sensoryPass;
    }

    public String getQuickTest() {
        return quickTest;
    }

    public void setQuickTest(String quickTest) {
        this.quickTest = quickTest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }
}
