import axiosClient from "./axiosClient";

// Matches AuthController exactly (/api/auth/**, all public):
//   POST /login                      LoginRequest{email,password} -> LoginResponse{token,tokenType,expiresInMs,user}
//   GET  /me                         -> UserResponse
//   POST /forgot-password/send-otp   ForgotPasswordRequest{email} -> ApiMessage
//   POST /forgot-password/reset      ResetPasswordRequest{email,otp,newPassword,confirmPassword} -> ApiMessage
const authApi = {
  login: (email, password) =>
    axiosClient.post("/auth/login", { email, password }).then((res) => res.data),

  me: () => axiosClient.get("/auth/me").then((res) => res.data),

  sendResetOtp: (email) =>
    axiosClient.post("/auth/forgot-password/send-otp", { email }).then((res) => res.data),

  resetPassword: (email, otp, newPassword, confirmPassword) =>
    axiosClient
      .post("/auth/forgot-password/reset", { email, otp, newPassword, confirmPassword })
      .then((res) => res.data),
};

export default authApi;
