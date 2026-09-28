import axiosClient from "./axiosClient";

// Matches ManagerController + the manager half of LimitRequestController
// exactly (/api/manager/**, requires role MANAGER). Department scoping
// happens server-side via the JWT — never sent from the client.
const managerApi = {
  getDashboard: () => axiosClient.get("/manager/dashboard").then((res) => res.data),

  getPendingExpenses: () => axiosClient.get("/manager/expenses").then((res) => res.data),
  getAllExpenses: () => axiosClient.get("/manager/expenses/all").then((res) => res.data),
  approveExpense: (id) =>
    axiosClient.patch(`/manager/expenses/${id}/approve`).then((res) => res.data),
  rejectExpense: (id, reason) =>
    axiosClient.patch(`/manager/expenses/${id}/reject`, { reason }).then((res) => res.data),

  getTeam: () => axiosClient.get("/manager/team").then((res) => res.data),

  getCategoryChart: () => axiosClient.get("/manager/analytics/category").then((res) => res.data),
  getMonthlyChart: () => axiosClient.get("/manager/analytics/monthly").then((res) => res.data),

  getPendingLimitRequests: () =>
    axiosClient.get("/manager/limit-requests").then((res) => res.data),
  approveLimitRequest: (id) =>
    axiosClient.patch(`/manager/limit-requests/${id}/approve`).then((res) => res.data),
  rejectLimitRequest: (id, reason) =>
    axiosClient
      .patch(`/manager/limit-requests/${id}/reject`, { reason })
      .then((res) => res.data),
};

export default managerApi;
