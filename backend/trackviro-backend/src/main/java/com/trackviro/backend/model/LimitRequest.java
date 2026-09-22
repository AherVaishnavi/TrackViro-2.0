package com.trackviro.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Ported from com.example.demo.model.LimitRequest unchanged except the
 * package name. status remains a plain String:
 *   PENDING → MANAGER_APPROVED → APPROVED (finance sets approvedAmount)
 *   or → REJECTED at either stage.
 */
@Entity
@Table(name = "limit_requests")
public class LimitRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double requestedAmount;   // amount employee wants added
    private String reason;
    private String status;
    // PENDING          → waiting for manager
    // MANAGER_APPROVED → manager approved, waiting for finance
    // APPROVED         → finance approved and limit updated
    // REJECTED         → rejected at any stage

    private String rejectReason;
    private Double approvedAmount;    // finance sets actual amount to add

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Double getRequestedAmount() {
        return requestedAmount;
    }
    public void setRequestedAmount(Double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Double getApprovedAmount() {
        return approvedAmount;
    }
    public void setApprovedAmount(Double approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getEmployee() {
        return employee;
    }
    public void setEmployee(User employee) {
        this.employee = employee;
    }
}
