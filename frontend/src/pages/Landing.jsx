import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../auth";

export default function Landing() {
  const { user, ready } = useAuth();
  if (ready && user) {
    return <Navigate to="/app" replace />;
  }

  return (
    <div className="page">
      <header className="topbar">
        <span className="wordmark">AstroGuide</span>
      </header>
      <main className="landing">
        <section>
          <p className="eyebrow">Vedic astrology</p>
          <h1>Save the birth details your chart will be built from.</h1>
          <p className="lede">
            Create an account, then keep a profile with the date, exact time, and place of birth.
            The Kundli and readings use that saved profile later.
          </p>
        </section>
        <aside className="panel actions">
          <h2>Start a profile</h2>
          <p>Your birth details stay on your account. Another login cannot open them.</p>
          <div className="action-row">
            <Link className="button" to="/signup">
              Create account
            </Link>
            <Link className="button secondary" to="/login">
              Log in
            </Link>
          </div>
        </aside>
      </main>
    </div>
  );
}
