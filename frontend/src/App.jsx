import { useEffect, useState } from 'react';
import SightingMap from './components/SightingMap.jsx';

const API_BASE = '';
const OPTION_PAGE_SIZE = 250;
const SHAPE_PAGE_SIZE = 50;
const MAP_LIMIT_MIN = 1000;
const MAP_LIMIT_MAX = 100000;
const MAP_LIMIT_STEP = 2500;
const SUPPORTED_COUNTRY_CODES = ['AU', 'CA', 'GB', 'US'];
const OTHER_COUNTRY_FILTER = '__other__';
const OTHER_COUNTRY_LABEL = 'Other / unknown';
const ANY_SHAPE_FILTER = '';
const ANY_SHAPE_LABEL = 'Any shape';

/**
 * Creates a fresh filter object with the default dashboard values.
 *
 * The dashboard starts with a supported country instead of an all-countries option.
 *
 * @returns {{ countryCode: string, shapeName: string }} A default filter state.
 */
const createDefaultFilters = () => ({
  countryCode: SUPPORTED_COUNTRY_CODES[0],
  shapeName: ANY_SHAPE_FILTER
});

/**
 * Builds the fixed country filter options shown in the UI.
 *
 * @returns {Array<{ value: string, label: string }>} Available country filter options.
 */
const buildCountryOptions = (locations) => {
  const availableCountryCodes = new Set(
    (Array.isArray(locations) ? locations : [])
      .map((location) => location?.countryCode?.trim().toUpperCase())
      .filter(Boolean)
  );

  const countryCodes = availableCountryCodes.size > 0
    ? SUPPORTED_COUNTRY_CODES.filter((countryCode) => availableCountryCodes.has(countryCode))
    : SUPPORTED_COUNTRY_CODES;

  return [
    ...countryCodes.map((countryCode) => ({
      value: countryCode,
      label: countryCode
    })),
    ...SUPPORTED_COUNTRY_CODES.filter((countryCode) => !countryCodes.includes(countryCode)).map((countryCode) => ({
      value: countryCode,
      label: countryCode
    })),
    {
      value: OTHER_COUNTRY_FILTER,
      label: OTHER_COUNTRY_LABEL
    }
  ];
};

/**
 * Builds the shape filter options shown in the UI.
 *
 * @param {Array<object>} shapes Raw shape objects from the backend.
 * @returns {Array<{ value: string, label: string }>} Available shape filter options.
 */
const buildShapeOptions = (shapes) => {
  const uniqueShapes = Array.from(
    new Set(
      (Array.isArray(shapes) ? shapes : [])
        .map((shape) => shape?.name?.trim())
        .filter(Boolean)
    )
  ).sort((a, b) => a.localeCompare(b));

  return [
    {
      value: ANY_SHAPE_FILTER,
      label: ANY_SHAPE_LABEL
    },
    ...uniqueShapes.map((name) => ({
      value: name,
      label: name
    }))
  ];
};

/**
 * Reads the current authentication state from the backend.
 *
 * @returns {Promise<{ authenticated: boolean, name?: string, login?: string } | null>} The authenticated user data, or null when signed out.
 */
const fetchAuthenticatedUser = async () => {
  try {
    const response = await fetch(`${API_BASE}/api/auth/check`, {
      credentials: 'include'
    });

    if (!response.ok) {
      return null;
    }

    const data = await response.json();
    return data.authenticated ? data : null;
  } catch (error) {
    console.error('Failed to load user:', error);
    throw new Error('Failed to verify authentication status. Please try again.');
  }
};

/**
 * Loads the backend locations endpoint and returns the fixed country filter options.
 *
 * @returns {Promise<Array<{ value: string, label: string }>>} Available country codes.
 */
const fetchCountryOptions = async () => {
  const response = await fetch(`${API_BASE}/api/locations?page=0&size=${OPTION_PAGE_SIZE}`, {
    credentials: 'include'
  });

  if (!response.ok) {
    throw new Error(`Failed to load filter options (${response.status})`);
  }

  const locations = await response.json();
  return buildCountryOptions(locations);
};

/**
 * Loads the backend shapes endpoint and returns the available shape filter options.
 *
 * @returns {Promise<Array<{ value: string, label: string }>>} Available shape options.
 */
const fetchShapeOptions = async () => {
  const response = await fetch(`${API_BASE}/api/shapes?page=0&size=${SHAPE_PAGE_SIZE}`, {
    credentials: 'include'
  });

  if (!response.ok) {
    throw new Error(`Failed to load shape options (${response.status})`);
  }

  const shapes = await response.json();
  return buildShapeOptions(shapes);
};

const App = () => {
  const [user, setUser] = useState(null);
  const [countryOptions, setCountryOptions] = useState([]);
  const [shapeOptions, setShapeOptions] = useState(() => buildShapeOptions([]));
  const [filters, setFilters] = useState(() => ({
    ...createDefaultFilters(),
    shapeName: ANY_SHAPE_FILTER
  }));
  const [draftFilters, setDraftFilters] = useState(() => ({
    ...createDefaultFilters(),
    shapeName: ANY_SHAPE_FILTER
  }));
  const [mapLimit, setMapLimit] = useState(3000);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  /**
   * Loads the dashboard's auth state and updates the local user state.
   *
   * @returns {Promise<boolean>} True when the user is authenticated.
   */
  const loadUser = async () => {
    const authenticatedUser = await fetchAuthenticatedUser();

    if (authenticatedUser) {
      setUser(authenticatedUser);
      return true;
    }

    setUser(null);
    return false;
  };

  /**
   * Loads country filter options for the authenticated dashboard.
   */
  const loadFilterOptions = async () => {
    const [uniqueCountries, availableShapes] = await Promise.all([
      fetchCountryOptions(),
      fetchShapeOptions()
    ]);

    setCountryOptions(uniqueCountries);
    setShapeOptions(availableShapes);
  };

  /**
   * Fetches auth and filter data in the correct order and updates the page state.
   */
  const bootstrap = async () => {
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
      setUser(null);
      setCountryOptions([]);
      setError(err?.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    bootstrap();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    /**
     * Re-runs the bootstrap flow when the browser restores this page from cache.
     */
    const handlePageShow = () => {
      bootstrap();
    };

    window.addEventListener('pageshow', handlePageShow);
    return () => window.removeEventListener('pageshow', handlePageShow);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /**
   * Updates the draft filter form while the user is selecting filter values.
   *
   * @param {React.ChangeEvent<HTMLSelectElement>} event The change event from the filter form.
   */
  const handleDraftFilterChange = (event) => {
    const { name, value } = event.target;
    setDraftFilters((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  /**
   * Applies the draft filter values to the active dashboard filter state.
   *
   * @param {React.FormEvent<HTMLFormElement>} event The submit event from the filter form.
   */
  const handleApplyFilters = (event) => {
    event.preventDefault();
    setFilters(draftFilters);
  };

  /**
   * Resets both the active filters and the draft form back to their defaults.
   */
  const handleClearFilters = () => {
    setDraftFilters(createDefaultFilters());
    setFilters({
      ...createDefaultFilters(),
      shapeName: ANY_SHAPE_FILTER
    });
  };

  /**
   * Updates the amount of map data to fetch and render.
   *
   * @param {React.ChangeEvent<HTMLInputElement>} event The range slider change event.
   */
  const handleMapLimitChange = (event) => {
    setMapLimit(Number(event.target.value));
  };

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

      {error && (
        <section className="panel" role="alert">
          <h2>Could not load the dashboard</h2>
          <p>{error}</p>
          <button type="button" onClick={bootstrap}>
            Retry
          </button>
        </section>
      )}

      {user && (
        <>
          <section className="panel">
            <h2>Map view</h2>
            <p className="helper-text">
              The map loads sightings across the dataset up to your chosen limit, clusters them for performance, and respects the active country and shape filter.
            </p>
            <label className="map-control">
              <span>Map datapoints to load: {mapLimit}</span>
              <input
                type="range"
                min={MAP_LIMIT_MIN}
                max={MAP_LIMIT_MAX}
                step={MAP_LIMIT_STEP}
                value={mapLimit}
                onChange={handleMapLimitChange}
              />
              <small>Range {MAP_LIMIT_MIN}–{MAP_LIMIT_MAX}. Higher values may take longer to load.</small>
            </label>
            {!loading && !error && (
              <SightingMap countryCode={filters.countryCode} shapeName={filters.shapeName} limit={mapLimit} />
            )}

            <h2>Filters</h2>
            <form className="filters" onSubmit={handleApplyFilters}>
              <label>
                Country
                <select name="countryCode" value={draftFilters.countryCode} onChange={handleDraftFilterChange}>
                  {countryOptions.map(({ value, label }) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Shape
                <select name="shapeName" value={draftFilters.shapeName} onChange={handleDraftFilterChange}>
                  {shapeOptions.map(({ value, label }) => (
                    <option key={value || 'any-shape'} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
              <div className="actions">
                <button type="submit">Apply</button>
                <button type="button" className="button-secondary" onClick={handleClearFilters}>
                  Clear
                </button>
              </div>
            </form>
            <p className="helper-text">
              Country filter updates the map data. Choose AU, CA, GB, US, or Other / unknown for countries outside that set.
              Shape filter lets you narrow the map further by UFO shape.
            </p>
          </section>
        </>
      )}
    </main>
  );
};

export default App;
