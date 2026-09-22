package com.trackviro.backend.model;

import jakarta.persistence.*;

/**
 * Ported from com.example.demo.model.Department unchanged except the
 * package name. manager is a @OneToOne on the department side — a
 * department has at most one manager, matching UserServiceImpl's
 * auto-assign logic that will be ported in the services step.
 */
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }
}
