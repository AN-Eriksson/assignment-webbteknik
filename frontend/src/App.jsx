import { useEffect, useState } from 'react';
import SightingMap from './components/SightingMap.jsx';

const API_BASE = '';
const OPTION_PAGE_SIZE = 250;
const MAP_LIMIT_MIN = 1;
const MAP_LIMIT_MAX = 10000;
const MAP_LIMIT_STEP = 250;

const EMPTY_FILTERS = {
  countryCode: ''
};

function buildQueryParams(activeFilters) {
  const params = new URLSearchParams();

  if (activeFilters.countryCode.trim()) {
    params.set('countryCode', activeFilters.countryCode.trim());
  }

  return params.toString();
}

export default function App() {
  const [user, setUser] = useState(null);
  const [countryOptions, setCountryOptions] = useState([]);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [draftFilters, setDraftFilters] = useState(EMPTY_FILTERS);
  const [mapLimit, setMapLimit] = useState(3000);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadUser() {
    try {
      const response = await fetch(`${API_BASE}/api/auth/check`, {
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (data.authenticated) {
          setUser(data);
          return true;
        }
      }
    } catch (err) {
      console.error('Failed to load user:', err);
    }

    setUser(null);
    return false;
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

  async function bootstrap() {
    try {
      setLoading(true);
      setError('');
      const isAuthenticated = await loadUser();

      if (isAuthenticated) {
        await loadFilterOptions();
      } else {
        setCountryOptions([]);
      }
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

  useEffect(() => {
    const handlePageShow = () => {
      bootstrap();
    };

    window.addEventListener('pageshow', handlePageShow);
    return () => window.removeEventListener('pageshow', handlePageShow);
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
  }

  function onClearFilters() {
    setDraftFilters(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
  }

  function onMapLimitChange(event) {
    setMapLimit(Number(event.target.value));
  }

  return (
    <main className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">UFO Dashboard</p>
          <h1>Sightings overview</h1>
          <p className="subtitle">GitHub login + bridge to UFO API + interactive filters and mapping.</p>
        </div>
        {user ? (
          <div className="user-section">
            <span className="greeting">Hej, {user.name || user.login}</span>
            <a className="button button-logout" href="/api/auth/logout">
              Log out
            </a>
          </div>
        ) : (
          <a className="button" href="/oauth2/authorization/github">
            Log in with GitHub
          </a>
        )}
      </header>

      {user && (
        <>
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
            <p className="helper-text">Country filter updates the map data. The map will respects your filter selection.</p>
          </section>

          <section className="panel">
            <h2>Map view</h2>
            <p className="helper-text">
              The map loads sightings across the dataset up to your chosen limit, clusters them for performance, and respects the active country filter.
            </p>
            <label className="map-control">
              <span>Map datapoints to load: {mapLimit}</span>
              <input
                type="range"
                min={MAP_LIMIT_MIN}
                max={MAP_LIMIT_MAX}
                step={MAP_LIMIT_STEP}
                value={mapLimit}
                onChange={onMapLimitChange}
              />
              <small>Range {MAP_LIMIT_MIN}–{MAP_LIMIT_MAX}. Higher values may take longer to load.</small>
            </label>
            {!loading && !error && <SightingMap countryCode={filters.countryCode} limit={mapLimit} />}
          </section>
        </>
      )}
    </main>
  );
}
