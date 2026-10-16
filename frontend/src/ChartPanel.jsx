function formatDegree(value) {
  return `${Number(value).toFixed(2)}°`;
}

function formatWhen(value, timezone) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeZone: timezone || "UTC",
  }).format(new Date(value));
}

function PlacementTable({ rows }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Body</th>
            <th>Sign</th>
            <th>Degree</th>
            <th>House</th>
            <th>Nakshatra</th>
            <th>Pada</th>
            <th>Navamsa</th>
            <th>Motion</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.name}>
              <td>{row.name}</td>
              <td>{row.sign}</td>
              <td>{formatDegree(row.degreeInSign)}</td>
              <td>{row.house}</td>
              <td>{row.nakshatra}</td>
              <td>{row.pada}</td>
              <td>{row.navamsaSign}</td>
              <td>{row.retrograde ? "Retrograde" : "Direct"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default function ChartPanel({ chart, loading, error }) {
  if (loading) {
    return (
      <section className="chart-wrap panel">
        <p className="muted">Calculating the chart…</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="chart-wrap panel">
        <p className="form-error">{error}</p>
      </section>
    );
  }

  if (!chart) {
    return null;
  }

  const rows = [chart.ascendant, ...chart.planets];

  return (
    <section className="chart-wrap panel">
      <h2>Sidereal chart</h2>
      <p className="muted">
        Lahiri ayanamsa {formatDegree(chart.ayanamsaDegrees)}, whole-sign houses. These are traditional
        placements, not predictions. Current mahadasha: {chart.currentMahadasha}.
      </p>
      <p className="muted">
        {chart.birthplace} · {chart.latitude}, {chart.longitude} · {chart.timezone}
      </p>
      <PlacementTable rows={rows} />
      <h3>Houses</h3>
      <div className="house-row">
        {chart.houses.map((house) => (
          <span key={house.number}>
            {house.number}. {house.sign}
          </span>
        ))}
      </div>
      <h3>Mahadashas</h3>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Lord</th>
              <th>Starts</th>
              <th>Ends</th>
            </tr>
          </thead>
          <tbody>
            {chart.mahadashas.map((period) => (
              <tr
                key={`${period.lord}-${period.start}`}
                className={period.lord === chart.currentMahadasha ? "current-dasha" : undefined}
              >
                <td>{period.lord}</td>
                <td>{formatWhen(period.start, chart.timezone)}</td>
                <td>{formatWhen(period.end, chart.timezone)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
