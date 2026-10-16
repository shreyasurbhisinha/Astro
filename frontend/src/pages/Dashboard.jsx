import { useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../auth";
import ChartPanel from "../ChartPanel";

const emptyForm = {
  name: "",
  dateOfBirth: "",
  timeOfBirth: "",
  birthplace: "",
};

function fieldMap(errors) {
  const next = {};
  for (const fieldError of errors || []) {
    next[fieldError.field] = fieldError.message;
  }
  return next;
}

export default function Dashboard() {
  const { token, user, logout } = useAuth();
  const [profiles, setProfiles] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [chartProfileId, setChartProfileId] = useState(null);
  const [chart, setChart] = useState(null);
  const [chartLoading, setChartLoading] = useState(false);
  const [chartError, setChartError] = useState("");

  async function loadProfiles() {
    const data = await api("/api/profiles", { token });
    setProfiles(data);
  }

  useEffect(() => {
    let cancelled = false;
    loadProfiles()
      .catch((requestError) => {
        if (!cancelled) {
          setError(requestError.message || "Could not load profiles");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  function updateField(name, value) {
    setForm((current) => ({ ...current, [name]: value }));
  }

  function startEdit(profile) {
    setEditingId(profile.id);
    setForm({
      name: profile.name,
      dateOfBirth: profile.dateOfBirth,
      timeOfBirth: profile.timeOfBirth.slice(0, 5),
      birthplace: profile.birthplace,
    });
    setMessage("");
    setError("");
    setFieldErrors({});
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
    setFieldErrors({});
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    setFieldErrors({});
    const body = {
      name: form.name,
      dateOfBirth: form.dateOfBirth,
      timeOfBirth: form.timeOfBirth,
      birthplace: form.birthplace,
    };
    try {
      if (editingId) {
        await api(`/api/profiles/${editingId}`, { method: "PUT", body, token });
        setMessage("Profile updated.");
      } else {
        await api("/api/profiles", { method: "POST", body, token });
        setMessage("Profile saved.");
      }
      setEditingId(null);
      setForm(emptyForm);
      setChart(null);
      setChartProfileId(null);
      setChartError("");
      await loadProfiles();
    } catch (requestError) {
      setFieldErrors(fieldMap(requestError.errors));
      setError(requestError.message || "Could not save the profile");
    } finally {
      setSaving(false);
    }
  }

  async function onDelete(profile) {
    if (!window.confirm(`Delete the birth profile for ${profile.name}?`)) {
      return;
    }
    setError("");
    try {
      await api(`/api/profiles/${profile.id}`, { method: "DELETE", token });
      if (editingId === profile.id) {
        cancelEdit();
      }
      if (chartProfileId === profile.id) {
        setChart(null);
        setChartProfileId(null);
        setChartError("");
      }
      setMessage("Profile deleted.");
      await loadProfiles();
    } catch (requestError) {
      setError(requestError.message || "Could not delete the profile");
    }
  }

  async function onViewChart(profile) {
    setChartProfileId(profile.id);
    setChart(null);
    setChartError("");
    setChartLoading(true);
    setError("");
    try {
      const data = await api(`/api/profiles/${profile.id}/kundli`, { token });
      setChart(data);
      await loadProfiles();
    } catch (requestError) {
      setChartError(requestError.message || "Could not calculate the chart");
    } finally {
      setChartLoading(false);
    }
  }

  return (
    <div className="page">
      <header className="topbar">
        <span className="wordmark">AstroGuide</span>
        <div className="account">
          <span>{user.email}</span>
          <button type="button" className="secondary" onClick={logout}>
            Log out
          </button>
        </div>
      </header>
      <main className="dashboard">
        <form className="panel" onSubmit={onSubmit}>
          <h1>{editingId ? "Edit birth profile" : "New birth profile"}</h1>
          <p className="muted">
            Date, exact time, and place of birth. Coordinates are resolved when the chart is calculated.
          </p>
          <label>
            Name
            <input value={form.name} onChange={(event) => updateField("name", event.target.value)} required />
            {fieldErrors.name ? <span className="field-error">{fieldErrors.name}</span> : null}
          </label>
          <label>
            Date of birth
            <input
              type="date"
              value={form.dateOfBirth}
              onChange={(event) => updateField("dateOfBirth", event.target.value)}
              required
            />
            {fieldErrors.dateOfBirth ? <span className="field-error">{fieldErrors.dateOfBirth}</span> : null}
          </label>
          <label>
            Time of birth
            <input
              type="time"
              value={form.timeOfBirth}
              onChange={(event) => updateField("timeOfBirth", event.target.value)}
              required
            />
            {fieldErrors.timeOfBirth ? <span className="field-error">{fieldErrors.timeOfBirth}</span> : null}
          </label>
          <label>
            Place of birth
            <input
              value={form.birthplace}
              onChange={(event) => updateField("birthplace", event.target.value)}
              placeholder="City, state, country"
              required
            />
            {fieldErrors.birthplace ? <span className="field-error">{fieldErrors.birthplace}</span> : null}
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          {message ? <p className="form-ok">{message}</p> : null}
          <div className="action-row">
            <button type="submit" disabled={saving}>
              {saving ? "Saving…" : editingId ? "Save changes" : "Save profile"}
            </button>
            {editingId ? (
              <button type="button" className="secondary" onClick={cancelEdit}>
                Cancel
              </button>
            ) : null}
          </div>
        </form>

        <section>
          <h2>Saved profiles</h2>
          {loading ? <p className="muted">Loading profiles…</p> : null}
          {!loading && profiles.length === 0 ? (
            <p className="muted">No birth profiles yet. Add the first one with the form.</p>
          ) : null}
          <div className="profile-list">
            {profiles.map((profile) => (
              <article className="profile-card" key={profile.id}>
                <h3>{profile.name}</h3>
                <p>
                  {profile.dateOfBirth} · {profile.timeOfBirth.slice(0, 5)}
                </p>
                <p>{profile.birthplace}</p>
                <div className="action-row">
                  <button type="button" onClick={() => onViewChart(profile)} disabled={chartLoading}>
                    {chartLoading && chartProfileId === profile.id ? "Calculating…" : "View chart"}
                  </button>
                  <button type="button" className="secondary" onClick={() => startEdit(profile)}>
                    Edit
                  </button>
                  <button type="button" className="secondary danger" onClick={() => onDelete(profile)}>
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>
        <ChartPanel chart={chart} loading={chartLoading} error={chartError} />
      </main>
    </div>
  );
}
