import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import authApi from "../services/authApi";

const HOME_BY_ROLE = {
  EMPLOYEE: "/employee",
  MANAGER: "/manager",
  FINANCE: "/finance",
};

// Ported from the old login.html: .login-split (left form panel / right
// teal gradient panel with the brand mark), same alert-msg / btn-login /
// forgot-link classes, same OTP modal-less step flow.
export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [showForgot, setShowForgot] = useState(false);
  const [forgotStep, setForgotStep] = useState("email");
  const [forgotEmail, setForgotEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [forgotMessage, setForgotMessage] = useState("");
  const [forgotError, setForgotError] = useState("");

  async function handleLogin(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const user = await login(email, password);
      navigate(HOME_BY_ROLE[user.role] || "/");
    } catch (err) {
      setError(err.response?.data?.message || "Login failed. Check your credentials.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSendOtp(e) {
    e.preventDefault();
    setForgotError("");
    setForgotMessage("");
    try {
      const res = await authApi.sendResetOtp(forgotEmail);
      setForgotMessage(res.message);
      setForgotStep("reset");
    } catch (err) {
      setForgotError(err.response?.data?.message || "Could not send OTP.");
    }
  }

  async function handleResetPassword(e) {
    e.preventDefault();
    setForgotError("");
    setForgotMessage("");
    try {
      const res = await authApi.resetPassword(forgotEmail, otp, newPassword, confirmPassword);
      setForgotMessage(res.message + " You can log in now.");
      setForgotStep("email");
      setOtp("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setForgotError(err.response?.data?.message || "Could not reset password.");
    }
  }

  return (
    <div className="login-split">
      <div className="login-left">
        <div style={{ maxWidth: 380, width: "100%", margin: "0 auto" }}>
          <div className="login-logo-mark">💼</div>
          <div className="login-brand mb-4">TrackViro</div>

          {!showForgot ? (
            <>
              <h2>Welcome back</h2>
              <p className="text-muted mb-4">Log in to your account</p>

              {error && <div className="alert-msg alert-err">{error}</div>}

              <form onSubmit={handleLogin}>
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    className="form-control"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                <button type="submit" className="btn-login" disabled={submitting}>
                  {submitting ? "Logging in…" : "Log In"}
                </button>
              </form>
              <button
                className="forgot-link mt-3"
                onClick={() => {
                  setShowForgot(true);
                  setForgotEmail(email);
                }}
              >
                Forgot password?
              </button>
            </>
          ) : (
            <>
              <h2>Reset password</h2>
              <p className="text-muted mb-4">
                {forgotStep === "email" ? "We'll email you a one-time code." : "Enter the code and a new password."}
              </p>

              {forgotMessage && <div className="alert-msg alert-ok">{forgotMessage}</div>}
              {forgotError && <div className="alert-msg alert-err">{forgotError}</div>}

              {forgotStep === "email" ? (
                <form onSubmit={handleSendOtp}>
                  <div className="mb-3">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className="form-control"
                      value={forgotEmail}
                      onChange={(e) => setForgotEmail(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn-login">
                    Send OTP
                  </button>
                </form>
              ) : (
                <form onSubmit={handleResetPassword}>
                  <div className="mb-3 otp-inputs">
                    <label className="form-label">OTP</label>
                    <input
                      type="text"
                      maxLength={6}
                      className="form-control"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value)}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">New password</label>
                    <input
                      type="password"
                      className="form-control"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Confirm new password</label>
                    <input
                      type="password"
                      className="form-control"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn-login">
                    Reset Password
                  </button>
                </form>
              )}

              <button
                className="forgot-link mt-3"
                onClick={() => {
                  setShowForgot(false);
                  setForgotStep("email");
                  setForgotMessage("");
                  setForgotError("");
                }}
              >
                Back to login
              </button>
            </>
          )}
        </div>
      </div>
      <div className="login-right">
        <div style={{ textAlign: "center", color: "white" }}>
          <div style={{ fontSize: "4rem" }}>📊</div>
          <div style={{ fontSize: "1.1rem", fontWeight: 700, marginTop: 12 }}>
            Corporate Expense Tracker
          </div>
        </div>
      </div>
    </div>
  );
}
