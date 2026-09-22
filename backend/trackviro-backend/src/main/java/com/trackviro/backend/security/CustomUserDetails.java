package com.trackviro.backend.security;

import com.trackviro.backend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Replaces the old CustomUserDetailsService's approach of returning a
 * generic org.springframework.security.core.userdetails.User built
 * with .withUsername(...).password(...).authorities(...) and then
 * separately stashing the full User entity into HttpSession so
 * controllers could read session.getAttribute("loggedUser").
 *
 * With no session at all, this class carries the fields a future
 * controller actually needs straight off the token: id (for
 * "getEmployeeExpenses(employeeId)" -style calls), role, and
 * departmentId (for the object-level authorization params
 * ExpenseServiceImpl/LimitRequestServiceImpl already expect —
 * actingManagerDeptId — built in Step 4).
 */
public class CustomUserDetails implements UserDetails {

    private final Long    id;
    private final String  email;
    private final String  password;
    private final String  name;
    private final String  role;          // EMPLOYEE | MANAGER | FINANCE
    private final Long    departmentId;  // null for a FINANCE user with no department
    private final boolean active;

    public CustomUserDetails(User u) {
        this.id           = u.getId();
        this.email        = u.getEmail();
        this.password     = u.getPassword();
        this.name         = u.getName();
        this.role         = u.getRole();
        this.departmentId = u.getDepartment() != null ? u.getDepartment().getId() : null;
        this.active       = Boolean.TRUE.equals(u.getIsActive());
    }

    public Long   getId()           { return id; }
    public String getName()         { return name; }
    public String getRole()         { return role; }
    public Long   getDepartmentId() { return departmentId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String  getPassword()             { return password; }
    @Override public String  getUsername()             { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return active; }
}
