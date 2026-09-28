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

import java.time.LocalDate;
import java.util.UUID;

/** Mẫu số 1 phần II — thực phẩm khô/bao gói sẵn. Bảng con, PK UUID (M07-D01). */
@Entity
@Table(name = "step1_dry_rows")
public class Step1DryRow {

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

    @Column(length = 160)
    private String producer;

    @Column(name = "producer_address", length = 240)
    private String producerAddress;

    @Column(name = "received_at", nullable = false, length = 32)
    private String receivedAt;

    @Column(nullable = false, length = 32)
    private String quantity;

    @Column(name = "supplier_name", nullable = false, length = 160)
    private String supplierName;

    @Column(length = 120)
    private String deliverer;

    @Column(name = "supplier_contact", length = 200)
    private String supplierContact;

    /** BR-092 — hạn sử dụng, dùng để chặn Bước 1 dùng lô quá HSD (INSP-092). */
    private LocalDate expiry;

    @Column(length = 16)
    private String storage;

    @Column(name = "has_invoice", nullable = false)
    private boolean hasInvoice;

    @Column(name = "sensory_pass", nullable = false)
    private boolean sensoryPass;

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

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getProducerAddress() {
        return producerAddress;
    }

    public void setProducerAddress(String producerAddress) {
        this.producerAddress = producerAddress;
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

    public String getDeliverer() {
        return deliverer;
    }

    public void setDeliverer(String deliverer) {
        this.deliverer = deliverer;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public boolean isHasInvoice() {
        return hasInvoice;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public boolean isSensoryPass() {
        return sensoryPass;
    }

    public void setSensoryPass(boolean sensoryPass) {
        this.sensoryPass = sensoryPass;
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
