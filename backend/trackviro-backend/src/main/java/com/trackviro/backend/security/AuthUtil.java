package com.trackviro.backend.security;

import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Not a controller — a small helper the Step 6 controllers will use
 * to read "who is making this request" off the JWT-populated
 * SecurityContext. This exists now because the service layer built in
 * Step 4 (ExpenseServiceImpl.approveByManager(expenseId,
 * actingManagerDeptId), submitExpense(employeeId, ...), etc.) already
 * expects an explicit actor identity as a parameter — this is where
 * that value will come from once controllers exist, instead of the
 * old app's session.getAttribute("loggedUser").
 */
@Component
public class AuthUtil {

    private final UserRepository userRepository;

    public AuthUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Lightweight identity straight from the JWT claims — no DB hit. */
    public CustomUserDetails currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new ResourceNotFoundException("No authenticated user in this request.");
        }
        return cud;
    }

    public Long currentUserId()       { return currentPrincipal().getId(); }
    public String currentRole()       { return currentPrincipal().getRole(); }
    public Long currentDepartmentId() { return currentPrincipal().getDepartmentId(); }

    /**
     * The full managed User entity, for the few places that still need
     * one (e.g. NotificationService.createNotification(User, ...),
     * which stayed entity-based in Step 4 since it's an internal
     * service-to-service call).
     */
    public User currentUser() {
        Long id = currentUserId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
