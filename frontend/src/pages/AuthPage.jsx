import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

export default function AuthPage({ mode }) {
  const isSignup = mode === "signup";
  const { user, ready, login, signup } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  if (ready && user) {
    return <Navigate to="/app" replace />;
  }

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      if (isSignup) {
        await signup(email, password);
      } else {
        await login(email, password);
      }
      navigate("/app");
    } catch (requestError) {
      const next = {};
      for (const fieldError of requestError.errors || []) {
        next[fieldError.field] = fieldError.message;
      }
      setFieldErrors(next);
      setError(requestError.message || "Could not continue");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <header className="topbar">
        <Link className="wordmark" to="/">
          AstroGuide
        </Link>
      </header>
      <main className="auth-layout">
        <form className="panel" onSubmit={onSubmit}>
          <h1>{isSignup ? "Create your account" : "Welcome back"}</h1>
          <p className="muted">
            {isSignup
              ? "Use an email and a password of at least 8 characters."
              : "Log in to see the birth profiles saved on this account."}
          </p>
          <label>
            Email
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
            {fieldErrors.email ? <span className="field-error">{fieldErrors.email}</span> : null}
          </label>
          <label>
            Password
            <input
              type="password"
              autoComplete={isSignup ? "new-password" : "current-password"}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={isSignup ? 8 : undefined}
              maxLength={72}
              required
            />
            {fieldErrors.password ? <span className="field-error">{fieldErrors.password}</span> : null}
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button type="submit" disabled={submitting}>
            {submitting ? "Please wait…" : isSignup ? "Create account" : "Log in"}
          </button>
          <p className="switch">
            {isSignup ? (
              <>
                Already have an account? <Link to="/login">Log in</Link>
              </>
            ) : (
              <>
                New here? <Link to="/signup">Create an account</Link>
              </>
            )}
          </p>
        </form>
      </main>
    </div>
  );
}
