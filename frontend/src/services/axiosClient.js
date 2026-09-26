import axios from "axios";

// Backend runs on :8080, all REST endpoints live under /api (see every
// controller's @RequestMapping — confirmed by reading the actual Step
// 6/7 controllers, not guessed).
const axiosClient = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Attaches "Authorization: Bearer <token>" to every request automatically.
// Token lives in sessionStorage, not localStorage, so it clears when the
// tab closes rather than lingering indefinitely.
axiosClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("trackviro_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// A 401 means the token is missing/expired/invalid — clear it and send
// the user back to login. Every other error status is left for the
// calling page to handle (validation messages, business-rule codes,
// etc. — see ErrorResponse's shape from GlobalExceptionHandler).
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem("trackviro_token");
      sessionStorage.removeItem("trackviro_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
