package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.user.UserCreateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.Department;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.DepartmentRepository;
import com.trackviro.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl. registerUser is the interesting one
 * here: it BCrypt-encodes the password (never asserts on the raw
 * value — that's exactly what an encoder mock is for) and has a
 * side-effect (auto-assigning a new MANAGER as their department's
 * manager) that's easy to silently break during a refactor.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(10L);
        department.setName("Engineering");
    }

    // ── registerUser ──────────────────────────────────────────

    @Test
    void registerUser_encodesPasswordAndSavesActiveUser() {
        UserCreateRequest request = new UserCreateRequest(
                "EMP100", "Priya Nair", "priya@trackviro.com",
                "plainPassword", "9999999999", "EMPLOYEE", 10L, 15000.0);

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashed:plainPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.registerUser(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("priya@trackviro.com");
        assertThat(response.isActive()).isTrue();

        // Confirm the encoder was actually used, and capture what was
        // saved to confirm the RAW password never reached the repository.
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed:plainPassword");
    }

    @Test
    void registerUser_throwsResourceNotFound_whenDepartmentDoesNotExist() {
        UserCreateRequest request = new UserCreateRequest(
                "EMP101", "Rahul Shah", "rahul@trackviro.com",
                "pw", null, "EMPLOYEE", 999L, 10000.0);

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_autoAssignsNewManagerToTheirDepartment() {
        UserCreateRequest request = new UserCreateRequest(
                "MGR001", "Neha Kapoor", "neha@trackviro.com",
                "pw", null, "MANAGER", 10L, 20000.0);

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        userService.registerUser(request);

        // Preserved exactly from the old app: creating a MANAGER
        // updates their department's manager_id automatically.
        ArgumentCaptor<Department> deptCaptor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(deptCaptor.capture());
        assertThat(deptCaptor.getValue().getManager().getName()).isEqualTo("Neha Kapoor");
    }

    @Test
    void registerUser_doesNotTouchDepartment_whenRoleIsEmployee() {
        UserCreateRequest request = new UserCreateRequest(
                "EMP102", "Employee Only", "e@trackviro.com",
                "pw", null, "EMPLOYEE", 10L, 10000.0);

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });

        userService.registerUser(request);

        // The auto-assign-manager side effect must only fire for MANAGER —
        // departmentRepository.save should never be called for an employee.
        verify(departmentRepository, never()).save(any());
    }

    // ── login (legacy method — see its own Javadoc: not wired to any
    //    controller, kept only because Step 4 preserved existing logic
    //    rather than deleting it) ─────────────────────────────

    @Test
    void login_returnsUser_whenEmailAndPasswordMatch() {
        User user = new User();
        user.setEmail("test@trackviro.com");
        user.setPassword("plainPassword"); // plaintext compare, as ported

        when(userRepository.findByEmailAndIsActiveTrue("test@trackviro.com")).thenReturn(user);

        User result = userService.login("test@trackviro.com", "plainPassword");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@trackviro.com");
    }

    @Test
    void login_returnsNull_whenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail("test@trackviro.com");
        user.setPassword("correctPassword");

        when(userRepository.findByEmailAndIsActiveTrue("test@trackviro.com")).thenReturn(user);

        User result = userService.login("test@trackviro.com", "wrongPassword");

        assertThat(result).isNull();
    }

    // ── Read methods ──────────────────────────────────────────

    @Test
    void getAllActiveUsers_returnsMappedUserResponses() {
        User u1 = new User();
        u1.setId(1L);
        u1.setName("A");
        u1.setIsActive(true);
        User u2 = new User();
        u2.setId(2L);
        u2.setName("B");
        u2.setIsActive(true);

        when(userRepository.findByIsActiveTrue()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userService.getAllActiveUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponse::name).containsExactly("A", "B");
    }
}
