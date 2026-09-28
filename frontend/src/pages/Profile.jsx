import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import { useAuth } from "../context/AuthContext";
import profileApi from "../services/profileApi";

const HOME_BY_ROLE = { EMPLOYEE: "/employee", MANAGER: "/manager", FINANCE: "/finance" };

// Ported from the old fragments/profile_sidebar.html modals (the
// cleanest version of this UI in the old app — nothing actually
// included it there, but its markup is the model this page follows).
export default function Profile() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState(user.name);
  const [phone, setPhone] = useState(user.phone || "");
  const [profilePicFile, setProfilePicFile] = useState(null);
  const [profileMessage, setProfileMessage] = useState("");
  const [profileError, setProfileError] = useState("");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [pwdMessage, setPwdMessage] = useState("");
  const [pwdError, setPwdError] = useState("");

  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [otpNewPassword, setOtpNewPassword] = useState("");
  const [otpConfirmPassword, setOtpConfirmPassword] = useState("");
  const [otpMessage, setOtpMessage] = useState("");
  const [otpError, setOtpError] = useState("");

  async function handleProfileSave(e) {
    e.preventDefault();
    setProfileError("");
    setProfileMessage("");
    try {
      await profileApi.updateProfile(name, phone, profilePicFile);
      setProfileMessage("Profile updated.");
      setProfilePicFile(null);
    } catch (err) {
      setProfileError(err.response?.data?.message || "Could not update profile.");
    }
  }

  async function handlePasswordChange(e) {
    e.preventDefault();
    setPwdError("");
    setPwdMessage("");
    try {
      await profileApi.changePassword(currentPassword, newPassword, confirmPassword);
      setPwdMessage("Password changed.");
      setCurrentPassword(""); setNewPassword(""); setConfirmPassword("");
    } catch (err) {
      setPwdError(err.response?.data?.message || "Could not change password.");
    }
  }

  async function handleSendOtp() {
    setOtpError(""); setOtpMessage("");
    try {
      const res = await profileApi.sendOtp();
      setOtpMessage(res.message);
      setOtpSent(true);
    } catch (err) {
      setOtpError(err.response?.data?.message || "Could not send OTP.");
    }
  }

  async function handleOtpPasswordChange(e) {
    e.preventDefault();
    setOtpError(""); setOtpMessage("");
    try {
      const res = await profileApi.changePasswordWithOtp(otp, otpNewPassword, otpConfirmPassword);
      setOtpMessage(res.message);
      setOtp(""); setOtpNewPassword(""); setOtpConfirmPassword(""); setOtpSent(false);
    } catch (err) {
      setOtpError(err.response?.data?.message || "Could not change password.");
    }
  }

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: false, onClick: () => navigate(HOME_BY_ROLE[user.role]) },
    { label: "My Profile", icon: "👤", active: true, onClick: () => {} },
  ];

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="My Profile" subtitle="Manage your account details and password" />

        <div className="row g-3" style={{ maxWidth: 700 }}>
          <div className="col-12">
            <div className="table-panel active">
              <div className="panel-header"><div className="panel-title">Details</div></div>
              <div className="panel-body">
                {profileMessage && <div className="alert-msg alert-ok">{profileMessage}</div>}
                {profileError && <div className="alert-msg alert-err">{profileError}</div>}
                <form onSubmit={handleProfileSave}>
                  <div className="mb-2">
                    <label className="field-label">Name</label>
                    <input className="field-control" value={name} onChange={(e) => setName(e.target.value)} required />
                  </div>
                  <div className="mb-2">
                    <label className="field-label">Phone</label>
                    <input className="field-control" value={phone} onChange={(e) => setPhone(e.target.value)} />
                  </div>
                  <div className="mb-3">
                    <label className="field-label">Profile Picture</label>
                    <label className="upload-box d-block">
                      <input type="file" className="d-none" accept=".jpg,.jpeg,.png" onChange={(e) => setProfilePicFile(e.target.files[0] || null)} />
                      {profilePicFile ? profilePicFile.name : "📎 Click to upload JPEG or PNG"}
                    </label>
                  </div>
                  <button type="submit" className="btn-primary-custom">Save</button>
                </form>
              </div>
            </div>
          </div>

          <div className="col-12">
            <div className="table-panel active">
              <div className="panel-header"><div className="panel-title">Change Password</div></div>
              <div className="panel-body">
                {pwdMessage && <div className="alert-msg alert-ok">{pwdMessage}</div>}
                {pwdError && <div className="alert-msg alert-err">{pwdError}</div>}
                <form onSubmit={handlePasswordChange}>
                  <div className="mb-2">
                    <input type="password" className="field-control" placeholder="Current password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
                  </div>
                  <div className="mb-2">
                    <input type="password" className="field-control" placeholder="New password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
                  </div>
                  <div className="mb-3">
                    <input type="password" className="field-control" placeholder="Confirm new password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
                  </div>
                  <button type="submit" className="btn-primary-custom">Change Password</button>
                </form>
              </div>
            </div>
          </div>

          <div className="col-12">
            <div className="table-panel active">
              <div className="panel-header"><div className="panel-title">Change Password via OTP</div></div>
              <div className="panel-body">
                {otpMessage && <div className="alert-msg alert-ok">{otpMessage}</div>}
                {otpError && <div className="alert-msg alert-err">{otpError}</div>}
                {!otpSent ? (
                  <button className="btn-primary-custom" onClick={handleSendOtp}>Send OTP to my email</button>
                ) : (
                  <form onSubmit={handleOtpPasswordChange}>
                    <div className="mb-2 otp-inputs">
                      <input type="text" maxLength={6} className="field-control" placeholder="OTP" value={otp} onChange={(e) => setOtp(e.target.value)} required />
                    </div>
                    <div className="mb-2">
                      <input type="password" className="field-control" placeholder="New password" value={otpNewPassword} onChange={(e) => setOtpNewPassword(e.target.value)} required />
                    </div>
                    <div className="mb-3">
                      <input type="password" className="field-control" placeholder="Confirm new password" value={otpConfirmPassword} onChange={(e) => setOtpConfirmPassword(e.target.value)} required />
                    </div>
                    <button type="submit" className="btn-primary-custom">Change Password</button>
                  </form>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
