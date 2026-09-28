import axiosClient from "./axiosClient";

// Matches EmployeeController + NotificationController exactly
// (all under /api/employee/**, requires role EMPLOYEE).
const employeeApi = {
  getDashboard: () => axiosClient.get("/employee/dashboard").then((res) => res.data),

  getCategories: () => axiosClient.get("/employee/categories").then((res) => res.data),

  getExpenses: () => axiosClient.get("/employee/expenses").then((res) => res.data),

  /**
   * POST /api/employee/expenses, multipart/form-data with two parts:
   *   "expense"  — ExpenseRequest as a JSON part (must carry an
   *                 explicit application/json Content-Type on the part
   *                 itself, not just the outer request — otherwise
   *                 Spring's @RequestPart binds it as
   *                 application/octet-stream and rejects it, which is
   *                 exactly the bug already fixed on the backend side
   *                 with the @Content/@Schema Swagger annotations).
   *                 A plain string value in FormData.append() does NOT
   *                 carry a content-type, so it's wrapped in a Blob
   *                 here to force one.
   *   "billFile" — optional File object (JPEG/PNG/PDF, 5MB max)
   *
   * expenseData: { expenseDate, categoryId, amount, description }
   * billFile: a File from an <input type="file"> or null
   */
  submitExpense: (expenseData, billFile) => {
    const formData = new FormData();
    formData.append(
      "expense",
      new Blob([JSON.stringify(expenseData)], { type: "application/json" })
    );
    if (billFile) {
      formData.append("billFile", billFile);
    }
    return axiosClient
      .post("/employee/expenses", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((res) => res.data);
  },

  getCategoryChart: () => axiosClient.get("/employee/analytics/category").then((res) => res.data),
  getMonthlyChart: () => axiosClient.get("/employee/analytics/monthly").then((res) => res.data),

  getLimitRequests: () => axiosClient.get("/employee/limit-requests").then((res) => res.data),
  submitLimitRequest: (requestedAmount, reason) =>
    axiosClient
      .post("/employee/limit-requests", { requestedAmount, reason })
      .then((res) => res.data),

  getNotifications: () => axiosClient.get("/employee/notifications").then((res) => res.data),
  getUnreadCount: () =>
    axiosClient.get("/employee/notifications/unread-count").then((res) => res.data),
  markAllRead: () =>
    axiosClient.post("/employee/notifications/mark-read").then((res) => res.data),
};

export default employeeApi;
