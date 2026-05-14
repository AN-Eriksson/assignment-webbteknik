import { useEffect, useMemo, useState } from 'react';
import {
  CartesianGrid,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts';
import SightingMap from './components/SightingMap.jsx';

const API_BASE = '';
const LIST_PAGE_SIZE = 10;
const CHART_SAMPLE_SIZE = 300;
const OPTION_PAGE_SIZE = 250;

const EMPTY_FILTERS = {
  countryCode: ''
};

const PIE_COLORS = ['#38bdf8', '#22d3ee', '#60a5fa', '#818cf8', '#a78bfa', '#34d399', '#f59e0b', '#f97316'];

function buildQueryParams(activeFilters, page, size) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size)
  });

  if (activeFilters.countryCode.trim()) {
    params.set('countryCode', activeFilters.countryCode.trim());
  }

  return params.toString();
}

export default function App() {
  const [me, setMe] = useState(null);
  const [sightings, setSightings] = useState([]);
  const [chartSightings, setChartSightings] = useState([]);
  const [countryOptions, setCountryOptions] = useState([]);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [draftFilters, setDraftFilters] = useState(EMPTY_FILTERS);
  const [page, setPage] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [loading, setLoading] = useState(true);
  const [pageLoading, setPageLoading] = useState(false);
  const [error, setError] = useState('');

  const shapeShareData = useMemo(() => {
    const counts = new Map();
    chartSightings.forEach((sighting) => {
      const key = sighting.shapeName || 'unknown';
      counts.set(key, (counts.get(key) || 0) + 1);
    });

    const total = chartSightings.length;
    return Array.from(counts.entries())
      .map(([shape, count]) => ({
        shape,
        count,
        percentage: total > 0 ? Number(((count / total) * 100).toFixed(1)) : 0
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 12)
      .map((entry, index) => ({
        ...entry,
        fill: PIE_COLORS[index % PIE_COLORS.length]
      }));
  }, [chartSightings]);

  const monthlyChartData = useMemo(() => {
    const counts = new Map();
    chartSightings.forEach((sighting) => {
      if (!sighting.datePosted) {
        return;
      }

      const month = sighting.datePosted.slice(0, 7);
      counts.set(month, (counts.get(month) || 0) + 1);
    });

    return Array.from(counts.entries())
      .map(([month, count]) => ({ month, count }))
      .sort((a, b) => a.month.localeCompare(b.month));
  }, [chartSightings]);

  async function loadSession() {
    const meResponse = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
    if (meResponse.ok) {
      setMe(await meResponse.json());
    } else {
      setMe(null);
    }
  }

  async function loadFilterOptions() {
    const locationsResponse = await fetch(`${API_BASE}/api/locations?page=0&size=${OPTION_PAGE_SIZE}`, {
      credentials: 'include'
    });

    if (locationsResponse.ok) {
      const locations = await locationsResponse.json();
      const uniqueCountries = Array.from(
        new Set((Array.isArray(locations) ? locations : []).map((location) => location?.countryCode).filter(Boolean))
      ).sort((a, b) => a.localeCompare(b));
      setCountryOptions(uniqueCountries);
    }
  }

  async function loadSightingsPage(activeFilters, activePage) {
    setPageLoading(true);
    setError('');

    try {
      const listParams = buildQueryParams(activeFilters, activePage, LIST_PAGE_SIZE);
      const listResponse = await fetch(`${API_BASE}/api/sightings?${listParams}`, {
        credentials: 'include'
      });

      if (!listResponse.ok) {
        setError(`Failed to load sightings (${listResponse.status})`);
        return;
      }

      const listData = await listResponse.json();
      const pageSightings = Array.isArray(listData) ? listData : [];
      setSightings(pageSightings);
      setHasNextPage(pageSightings.length === LIST_PAGE_SIZE);

      const chartParams = buildQueryParams(activeFilters, 0, CHART_SAMPLE_SIZE);
      const chartResponse = await fetch(`${API_BASE}/api/sightings?${chartParams}`, {
        credentials: 'include'
      });

      if (chartResponse.ok) {
        const chartData = await chartResponse.json();
        setChartSightings(Array.isArray(chartData) ? chartData : []);
      }
    } catch (err) {
      setError(err?.message || 'Something went wrong');
    } finally {
      setPageLoading(false);
    }
  }

  async function bootstrap() {
    try {
      setLoading(true);
      setError('');
      await Promise.all([loadSession(), loadFilterOptions()]);
      await loadSightingsPage(filters, 0);
    } catch (err) {
      setError(err?.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    bootstrap();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onDraftFilterChange(event) {
    const { name, value } = event.target;
    setDraftFilters((prev) => ({
      ...prev,
      [name]: value
    }));
  }

  function onApplyFilters(event) {
    event.preventDefault();
    setFilters(draftFilters);
    setPage(0);
    loadSightingsPage(draftFilters, 0);
  }

  function onClearFilters() {
    setDraftFilters(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setPage(0);
    loadSightingsPage(EMPTY_FILTERS, 0);
  }

  function goToPreviousPage() {
    const nextPage = Math.max(0, page - 1);
    setPage(nextPage);
    loadSightingsPage(filters, nextPage);
  }

  function goToNextPage() {
    const nextPage = page + 1;
    setPage(nextPage);
    loadSightingsPage(filters, nextPage);
  }

  return (
    <main className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">UFO Dashboard</p>
          <h1>Sightings overview</h1>
          <p className="subtitle">
            GitHub login + bridge to UFO API + Recharts visualizations + interactive filters.
          </p>
        </div>
        <a className="button" href="/oauth2/authorization/github">
          Log in with GitHub
        </a>
      </header>

      <section className="panel">
        <h2>Session</h2>
        {me ? (
          <pre>{JSON.stringify(me, null, 2)}</pre>
        ) : (
          <p>Not logged in yet. Click the login button above.</p>
        )}
      </section>

      <section className="panel">
        <h2>Filters</h2>
        <form className="filters" onSubmit={onApplyFilters}>
          <label>
            Country
            <select name="countryCode" value={draftFilters.countryCode} onChange={onDraftFilterChange}>
              <option value="">All countries</option>
              {countryOptions.map((countryCode) => (
                <option key={countryCode} value={countryCode}>
                  {countryCode.toUpperCase()}
                </option>
              ))}
            </select>
          </label>
          <div className="actions">
            <button type="submit">Apply</button>
            <button type="button" className="button-secondary" onClick={onClearFilters}>
              Clear
            </button>
          </div>
        </form>
        <p className="helper-text">
          Country filter updates both charts and the paginated list. Page size: {LIST_PAGE_SIZE} sightings.
        </p>
      </section>

      <section className="panel">
        <h2>Shape share (pie chart)</h2>
        {loading && <p>Loading dashboard…</p>}
        {pageLoading && !loading && <p>Refreshing data…</p>}
        {error && <p className="error">{error}</p>}
        {!loading && !error && shapeShareData.length === 0 && <p>No data for chart yet.</p>}
        {!loading && !error && shapeShareData.length > 0 && (
          <div className="chart-wrap" aria-label="Pie chart for sightings grouped by shape">
            <ResponsiveContainer width="100%" height={320}>
              <PieChart>
                <Pie
                  data={shapeShareData}
                  dataKey="count"
                  nameKey="shape"
                  cx="50%"
                  cy="50%"
                  outerRadius={110}
                  label={({ payload }) => `${payload.shape} (${payload.percentage}%)`}
                />
                <Tooltip formatter={(value, name, item) => [`${value} sightings`, `${item?.payload?.shape || name}`]} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <section className="panel">
        <h2>Sightings by month</h2>
        {!loading && !error && monthlyChartData.length === 0 && <p>No date data for line chart yet.</p>}
        {!loading && !error && monthlyChartData.length > 0 && (
          <div className="chart-wrap" aria-label="Line chart for sightings over time">
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={monthlyChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="month" stroke="#cbd5e1" />
                <YAxis stroke="#cbd5e1" />
                <Tooltip />
                <Line type="monotone" dataKey="count" stroke="#22d3ee" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <section className="panel">
        <h2>Map view</h2>
        <p className="helper-text">Pins are based on the same filtered sample as the charts, using the available coordinates.</p>
        {!loading && !error && <SightingMap sightings={chartSightings} />}
      </section>

      <section className="panel">
        <div className="list-header">
          <h2>Latest sightings</h2>
          <div className="pager">
            <button type="button" onClick={goToPreviousPage} disabled={pageLoading || page === 0}>
              Previous
            </button>
            <span>Page {page + 1}</span>
            <button type="button" onClick={goToNextPage} disabled={pageLoading || !hasNextPage}>
              Next
            </button>
          </div>
        </div>
        {!loading && !error && (
          <ul className="sightings">
            {sightings.length === 0 ? (
              <li>
                <strong>No sightings found</strong>
                <span>Try changing the filters.</span>
              </li>
            ) : (
              sightings.map((sighting) => (
                <li key={sighting.id}>
                  <strong>{sighting.city || 'Unknown city'}</strong>
                  <span>
                    {sighting.shapeName || 'unknown shape'} - {sighting.datePosted || 'no date'}
                  </span>
                  <p>{sighting.comments || 'No comments'}</p>
                </li>
              ))
            )}
          </ul>
        )}
      </section>
    </main>
  );
}
