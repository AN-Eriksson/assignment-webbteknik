import { useEffect, useState } from 'react';

const API_BASE = '';

export default function App() {
  const [me, setMe] = useState(null);
  const [sightings, setSightings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setError('');

        const meResponse = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
        if (meResponse.ok) {
          setMe(await meResponse.json());
        } else {
          setMe(null);
        }

        const sightingsResponse = await fetch(`${API_BASE}/api/sightings?page=0&size=10`, {
          credentials: 'include'
        });

        if (!sightingsResponse.ok) {
          setError(`Failed to load sightings (${sightingsResponse.status})`);
          return;
        }

        const data = await sightingsResponse.json();
        setSightings(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err?.message || 'Something went wrong');
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  return (
    <main className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">UFO Dashboard</p>
          <h1>Minimal React frontend</h1>
          <p className="subtitle">
            GitHub login + first bridge to UFO API.
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
        <h2>Latest sightings</h2>
        {loading && <p>Loading…</p>}
        {error && <p className="error">{error}</p>}
        {!loading && !error && (
          <ul className="sightings">
            {sightings.map((sighting) => (
              <li key={sighting.id}>
                <strong>{sighting.city || 'Unknown city'}</strong>
                <span>
                  {sighting.shapeName || 'unknown shape'} · {sighting.datePosted || 'no date'}
                </span>
                <p>{sighting.comments || 'No comments'}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}


