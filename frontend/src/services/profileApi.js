import axiosClient from "./axiosClient";

// Matches ProfileController exactly (/api/profile/**, any authenticated role).
const profileApi = {
  updateProfile: (name, phone, profilePicFile) => {
    const formData = new FormData();
    formData.append(
      "profile",
      new Blob([JSON.stringify({ name, phone })], { type: "application/json" })
    );
    if (profilePicFile) {
      formData.append("profilePicFile", profilePicFile);
    }
    return axiosClient
      .put("/profile", formData, { headers: { "Content-Type": "multipart/form-data" } })
      .then((res) => res.data);
  },

  changePassword: (currentPassword, newPassword, confirmPassword) =>
    axiosClient
      .put("/profile/password", { currentPassword, newPassword, confirmPassword })
      .then((res) => res.data),

  sendOtp: () => axiosClient.post("/profile/otp/send").then((res) => res.data),

  changePasswordWithOtp: (otp, newPassword, confirmPassword) =>
    axiosClient
      .put("/profile/password/otp", { otp, newPassword, confirmPassword })
      .then((res) => res.data),
};

export default profileApi;
