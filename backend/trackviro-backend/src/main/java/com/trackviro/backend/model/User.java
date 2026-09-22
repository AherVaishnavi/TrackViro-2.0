package com.trackviro.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Ported from com.example.demo.model.User (old STS project) unchanged
 * except the package name. Same table name, same fields, same
 * relationships. role stays a plain String per Step 2 instructions
 * (EMPLOYEE, MANAGER, FINANCE) — not converted to an enum yet.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeCode;
    private String name;

    @Column(unique = true)
    private String email;

    private String password;
    private String role; // EMPLOYEE, MANAGER, FINANCE

    private Double monthlyLimit;
    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    private String phone;            // phone number
    private String profilePic;       // filename of uploaded profile picture
    private String otp;              // OTP code (for email verification)
    private LocalDateTime otpExpiry; // OTP expiry time (10 minutes)

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public Double getMonthlyLimit() {
        return monthlyLimit;
    }
    public void setMonthlyLimit(Double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePic() {
        return profilePic;
    }
    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }
    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }
}
