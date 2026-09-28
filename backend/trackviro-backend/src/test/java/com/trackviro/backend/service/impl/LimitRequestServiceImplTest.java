package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.limitrequest.FinanceApproveRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestCreateRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;
import com.trackviro.backend.exception.AccessDeniedForResourceException;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.model.Department;
import com.trackviro.backend.model.LimitRequest;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.LimitRequestRepository;
import com.trackviro.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LimitRequestServiceImpl.
 *
 * approveByFinance is the one method worth testing carefully here: it
 * mutates TWO entities (the request itself, and the employee's
 * monthlyLimit) — see its own Javadoc about why @Transactional was
 * added. A test asserting the exact new monthlyLimit value is exactly
 * the kind of check that would have caught a silent regression there.
 */
@ExtendWith(MockitoExtension.class)
class LimitRequestServiceImplTest {

    @Mock private LimitRequestRepository limitRequestRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private LimitRequestServiceImpl limitRequestService;

    private User employee;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setId(10L);

        employee = new User();
        employee.setId(1L);
        employee.setName("Asha Rao");
        employee.setMonthlyLimit(10000.0);
        employee.setDepartment(dept);
    }

    // ── submitRequest ─────────────────────────────────────────

    @Test
    void submitRequest_savesPendingRequest_whenNoneAlreadyPending() {
        LimitRequestCreateRequest request = new LimitRequestCreateRequest(5000.0, "Client travel increased");

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(limitRequestRepository.existsByEmployeeAndStatus(employee, "PENDING")).thenReturn(false);
        when(limitRequestRepository.save(any(LimitRequest.class))).thenAnswer(inv -> {
            LimitRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        LimitRequestResponse response = limitRequestService.submitRequest(1L, request);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.requestedAmount()).isEqualTo(5000.0);
    }

    @Test
    void submitRequest_throwsAlreadyPendingBusinessRuleException_whenOneAlreadyExists() {
        LimitRequestCreateRequest request = new LimitRequestCreateRequest(5000.0, "Another request");

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(limitRequestRepository.existsByEmployeeAndStatus(employee, "PENDING")).thenReturn(true);

        assertThatThrownBy(() -> limitRequestService.submitRequest(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode()).isEqualTo("ALREADY_PENDING"));

        verify(limitRequestRepository, never()).save(any());
    }

    // ── approveByManager (object-level authorization) ───────────

    @Test
    void approveByManager_approves_whenSameDepartment() {
        LimitRequest req = buildRequest(employee, "PENDING");
        when(limitRequestRepository.findById(20L)).thenReturn(Optional.of(req));
        when(limitRequestRepository.save(any(LimitRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LimitRequestResponse response = limitRequestService.approveByManager(20L, 10L);

        assertThat(response.status()).isEqualTo("MANAGER_APPROVED");
    }

    @Test
    void approveByManager_throwsAccessDenied_whenDifferentDepartment() {
        LimitRequest req = buildRequest(employee, "PENDING"); // employee's dept id is 10L
        when(limitRequestRepository.findById(20L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> limitRequestService.approveByManager(20L, 999L))
                .isInstanceOf(AccessDeniedForResourceException.class);

        verify(limitRequestRepository, never()).save(any());
    }

    // ── approveByFinance (the two-entity mutation) ──────────────

    @Test
    void approveByFinance_increasesEmployeeMonthlyLimitByApprovedAmount() {
        LimitRequest req = buildRequest(employee, "MANAGER_APPROVED");
        FinanceApproveRequest approveRequest = new FinanceApproveRequest(3000.0);

        when(limitRequestRepository.findById(20L)).thenReturn(Optional.of(req));
        when(limitRequestRepository.save(any(LimitRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LimitRequestResponse response = limitRequestService.approveByFinance(20L, approveRequest);

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.approvedAmount()).isEqualTo(3000.0);

        // The employee's limit was 10000.0 before this — confirm it's
        // now exactly 13000.0, and that the updated User was actually saved.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getMonthlyLimit()).isEqualTo(13000.0);
    }

    @Test
    void rejectByFinance_setsRejectedStatusAndReason() {
        LimitRequest req = buildRequest(employee, "MANAGER_APPROVED");
        when(limitRequestRepository.findById(20L)).thenReturn(Optional.of(req));
        when(limitRequestRepository.save(any(LimitRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LimitRequestResponse response = limitRequestService.rejectByFinance(20L, "Budget exceeded for quarter");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.rejectReason()).isEqualTo("Budget exceeded for quarter");
        // Rejecting must never touch the employee's monthly limit.
        verify(userRepository, never()).save(any());
    }

    // ── Helpers ───────────────────────────────────────────────

    private LimitRequest buildRequest(User owner, String status) {
        LimitRequest r = new LimitRequest();
        r.setEmployee(owner);
        r.setRequestedAmount(3000.0);
        r.setReason("Increased travel");
        r.setStatus(status);
        return r;
    }
}
