package com.sfms.mf11.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * {@code incident_suspected_dishes} — món nghi vấn, khoá bằng {@code dishId} thật của MF05
 * (L33 đóng — {@code dishes.code} đã tồn tại từ V8, không nối bằng tên). TDS-MF11 §5.2.
 */
@Entity
@Table(name = "incident_suspected_dishes",
        uniqueConstraints = @UniqueConstraint(name = "uq_isd_inc_dish", columnNames = {"incident_id", "dish_id"}))
public class IncidentSuspectedDish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "dish_name", nullable = false, length = 180)
    private String dishName;

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }
}
