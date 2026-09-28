import axiosClient from "./axiosClient";

// Matches FinanceController + CategoryController + DepartmentController +
// UserController + the finance half of LimitRequestController exactly
// (all under /api/finance/**, requires role FINANCE — global scope, no
// department restriction, matching the backend's own design).
const financeApi = {
  getDashboard: () => axiosClient.get("/finance/dashboard").then((res) => res.data),

  getManagerApprovedExpenses: () =>
    axiosClient.get("/finance/expenses/manager-approved").then((res) => res.data),
  getFinalApprovedExpenses: () =>
    axiosClient.get("/finance/expenses/final-approved").then((res) => res.data),
  finalApprove: (id) =>
    axiosClient.patch(`/finance/expenses/${id}/final-approve`).then((res) => res.data),
  reimburse: (id) =>
    axiosClient.patch(`/finance/expenses/${id}/reimburse`).then((res) => res.data),
  softDeleteExpense: (id) =>
    axiosClient.delete(`/finance/expenses/${id}`).then((res) => res.data),

  getCategoryChart: () => axiosClient.get("/finance/analytics/category").then((res) => res.data),
  getDepartmentChart: () =>
    axiosClient.get("/finance/analytics/department").then((res) => res.data),
  getMonthlyTrendChart: () =>
    axiosClient.get("/finance/analytics/monthly-trend").then((res) => res.data),
  getTopSpendersChart: () =>
    axiosClient.get("/finance/analytics/top-spenders").then((res) => res.data),

  getPendingLimitRequests: () =>
    axiosClient.get("/finance/limit-requests").then((res) => res.data),
  approveLimitRequest: (id, approvedAmount) =>
    axiosClient
      .patch(`/finance/limit-requests/${id}/approve`, { approvedAmount })
      .then((res) => res.data),
  rejectLimitRequest: (id, reason) =>
    axiosClient
      .patch(`/finance/limit-requests/${id}/reject`, { reason })
      .then((res) => res.data),

  getCategories: () => axiosClient.get("/finance/categories").then((res) => res.data),
  createCategory: (name, maxLimit) =>
    axiosClient.post("/finance/categories", { name, maxLimit }).then((res) => res.data),

  getDepartments: () => axiosClient.get("/finance/departments").then((res) => res.data),
  createDepartment: (name) =>
    axiosClient.post("/finance/departments", { name }).then((res) => res.data),

  getUsers: () => axiosClient.get("/finance/users").then((res) => res.data),
  createUser: (userData) =>
    axiosClient.post("/finance/users", userData).then((res) => res.data),
};

export default financeApi;
