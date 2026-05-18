import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import L from 'leaflet';
import Supercluster from 'supercluster';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import markerIcon2xUrl from 'leaflet/dist/images/marker-icon-2x.png';
import markerIconUrl from 'leaflet/dist/images/marker-icon.png';
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png';

const API_BASE = '';
const MAP_REQUEST_DEBOUNCE_MS = 250;

const mapMarkerIcon = new L.Icon({
  iconUrl: markerIconUrl,
  iconRetinaUrl: markerIcon2xUrl,
  shadowUrl: markerShadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

/**
 * Creates the icon used for cluster markers.
 *
 * @param {number} pointCount Number of sightings in the cluster.
 * @returns {L.DivIcon} Leaflet cluster icon.
 */
const createClusterIcon = (pointCount) => {
  const size = pointCount < 10 ? 36 : pointCount < 50 ? 42 : 50;
  const bg = pointCount < 10 ? '#38bdf8' : pointCount < 50 ? '#22d3ee' : '#818cf8';

  return L.divIcon({
    html: `<div style="
      width:${size}px;
      height:${size}px;
      border-radius:999px;
      display:flex;
      align-items:center;
      justify-content:center;
      background:${bg};
      color:#082f49;
      font-weight:800;
      border:3px solid rgba(15, 23, 42, 0.9);
      box-shadow:0 8px 20px rgba(15, 23, 42, 0.35);
    ">${pointCount}</div>`,
    className: '',
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2]
  });
};

/**
 * Checks whether a sighting has valid latitude and longitude values.
 *
 * @param {object} sighting A sighting item from the backend.
 * @returns {boolean} True when both coordinates are finite numbers.
 */
const hasCoordinates = (sighting) => {
  return Number.isFinite(Number(sighting?.latitude)) && Number.isFinite(Number(sighting?.longitude));
};

/**
 * Converts a sighting object into a GeoJSON feature for Supercluster.
 *
 * @param {object} sighting A sighting item from the backend.
 * @returns {object} GeoJSON feature representation of the sighting.
 */
const toClusterFeature = (sighting) => {
  return {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [Number(sighting.longitude), Number(sighting.latitude)]
    },
    properties: {
      sighting
    }
  };
};

/**
 * Builds the query string for the map API request.
 *
 * @param {string} countryCode Selected country filter value.
 * @param {string} shapeName Selected shape filter value.
 * @param {number} limit Maximum number of sightings to load.
 * @param bounds Current map bounds, if available.
 * @returns {string} Query string without the leading question mark.
 */
const buildMapQuery = (countryCode, shapeName, limit, bounds) => {
  const params = new URLSearchParams({
    limit: String(limit)
  });

  if (countryCode && countryCode.trim()) {
    params.set('countryCode', countryCode.trim());
  }

  if (shapeName && shapeName.trim()) {
    params.set('shapeName', shapeName.trim());
  }

  if (bounds) {
    // bounds is an object with getNorth/getSouth/getEast/getWest (Leaflet LatLngBounds)
    params.set('north', String(bounds.getNorth()));
    params.set('south', String(bounds.getSouth()));
    params.set('east', String(bounds.getEast()));
    params.set('west', String(bounds.getWest()));
  }

  return params.toString();
};

/**
 * Normalizes fetch and backend errors into user-friendly map messages.
 *
 * @param {unknown} error The error thrown while loading map data.
 * @returns {string} A readable message for the UI.
 */
const normalizeMapError = (error) => {
  const message = error?.message || 'Failed to load map sightings';

  if (message === 'Failed to fetch' || message === 'NetworkError when attempting to fetch resource.') {
    return 'Could not load map data. Please check that the backend is running and try again.';
  }

  return message;
};

/**
 * Keeps the stored viewport in sync with the Leaflet map.
 *
 * @param {{ onViewportChange: (bounds: import('leaflet').LatLngBounds, zoom: number) => void }} props Component props.
 * @returns {null} Nothing is rendered directly.
 */
const MapViewportTracker = ({ onViewportChange }) => {
  const map = useMap();

  useEffect(() => {
    const updateViewport = () => onViewportChange(map.getBounds(), map.getZoom());

    updateViewport();
    map.on('moveend', updateViewport);
    map.on('zoomend', updateViewport);

    return () => {
      map.off('moveend', updateViewport);
      map.off('zoomend', updateViewport);
    };
  }, [map, onViewportChange]);

  return null;
};

/**
 * Renders a cluster marker and zooms into it when clicked.
 *
 * @param {{ cluster: object, clusterIndex: Supercluster }} props Component props.
 * @returns {JSX.Element} A cluster marker.
 */
const ClusterPointMarker = ({ cluster, clusterIndex }) => {
  const map = useMap();
  const markerRef = useRef(null);
  const [longitude, latitude] = cluster.geometry.coordinates;
  const pointCount = cluster.properties.point_count;

  useEffect(() => {
    const marker = markerRef.current;
    if (!marker) {
      return undefined;
    }

    const handleClusterClick = () => {
      const expansionZoom = Math.min(clusterIndex.getClusterExpansionZoom(cluster.id), 18);
      map.flyTo([latitude, longitude], expansionZoom, { duration: 0.35 });
    };

    marker.on('click', handleClusterClick);

    return () => {
      marker.off('click', handleClusterClick);
    };
  }, [cluster.id, clusterIndex, latitude, longitude, map]);

  return (
    <Marker position={[latitude, longitude]} icon={createClusterIcon(pointCount)} ref={markerRef}>
      <Popup>
        <strong>{pointCount} sightings</strong>
        <br />
        Zoom in to inspect the pins in this area.
      </Popup>
    </Marker>
  );
};

/**
 * Renders one sighting marker with a popup.
 *
 * @param {{ sighting: object }} props Component props.
 * @returns {JSX.Element} A single sighting marker.
 */
const SightingPointMarker = ({ sighting }) => {
  const latitude = Number(sighting.latitude);
  const longitude = Number(sighting.longitude);
  const popupTitle = `${sighting.city || 'Unknown city'} • ${sighting.shapeName || 'unknown shape'}`;
  const commentText = sighting.comments || 'No comments';

  return (
    <Marker position={[latitude, longitude]} icon={mapMarkerIcon}>
      <Popup>
        <strong>{popupTitle}</strong>
        <br />
        {sighting.datePosted || 'No date'}
        <br />
        {sighting.countryCode ? `Country: ${sighting.countryCode.toUpperCase()}` : 'Country: unknown'}
        <br />
        Coordinates: {latitude.toFixed(3)}, {longitude.toFixed(3)}
        <br />
        Comment: {commentText}
      </Popup>
    </Marker>
  );
};

/**
 * Renders the visible cluster or sighting markers.
 *
 * @param {{ clusters: Array<object>, clusterIndex: Supercluster }} props Component props.
 * @returns {Array<JSX.Element>} A list of markers to render.
 */
const ClusterMarkers = ({ clusters, clusterIndex }) => {
  return clusters.map((cluster) => {
    if (cluster.properties.cluster) {
      return <ClusterPointMarker key={`cluster-${cluster.id}`} cluster={cluster} clusterIndex={clusterIndex} />;
    }

    const sighting = cluster.properties.sighting;
    return <SightingPointMarker key={`point-${sighting.id}`} sighting={sighting} />;
  });
};

/**
 * Displays the UFO sightings map with clustering and backend loading.
 *
 * @param {{ countryCode?: string, shapeName?: string, fallbackSightings?: Array<object>, limit?: number }} props Component props.
 * @returns {JSX.Element} The map section.
 */
const SightingMap = ({ countryCode = '', shapeName = '', fallbackSightings = [], limit = 3000 }) => {
  const [viewport, setViewport] = useState({ bounds: null, zoom: 2 });
  const [mapSightings, setMapSightings] = useState([]);
  const [mapLoading, setMapLoading] = useState(true);
  const [mapError, setMapError] = useState('');

  const activeSightings = mapSightings.length > 0 ? mapSightings : fallbackSightings;
  const mapPoints = useMemo(() => activeSightings.filter(hasCoordinates), [activeSightings]);

  const clusterIndex = useMemo(() => {
    const index = new Supercluster({
      radius: 60,
      maxZoom: 16
    });

    index.load(mapPoints.map(toClusterFeature));
    return index;
  }, [mapPoints]);

  const visibleClusters = useMemo(() => {
    const bounds = viewport.bounds;
    const bbox = bounds
      ? [bounds.getWest(), bounds.getSouth(), bounds.getEast(), bounds.getNorth()]
      : [-180, -85, 180, 85];

    return clusterIndex.getClusters(bbox, viewport.zoom);
  }, [clusterIndex, viewport.bounds, viewport.zoom]);

  const handleViewportChange = useCallback((bounds, zoom) => {
    setViewport((current) => {
      if (current.zoom === zoom && current.bounds && current.bounds.equals(bounds)) {
        return current;
      }

      return { bounds, zoom };
    });
  }, []);

  // Serialize bounds to a stable string so we can use it as a dependency
  const boundsKey = viewport.bounds
    ? `${viewport.bounds.getNorth()},${viewport.bounds.getSouth()},${viewport.bounds.getEast()},${viewport.bounds.getWest()}`
    : '';

  useEffect(() => {
    const abortController = new AbortController();
    const timeoutId = window.setTimeout(async () => {
      setMapLoading(true);
      setMapError('');

      try {
        const query = buildMapQuery(countryCode, shapeName, limit, viewport.bounds);
        const url = `${API_BASE}/api/sightings/map?${query}`;
        // include bounds and limit in the request so the backend can filter per-viewport
        const response = await fetch(url, {
          credentials: 'include',
          signal: abortController.signal
        });

        if (!response.ok) {
          setMapError(`Failed to load map sightings (${response.status})`);
          setMapSightings([]);
          return;
        }

        const data = await response.json();
        setMapSightings(Array.isArray(data) ? data : []);
      } catch (error) {
        if (error.name !== 'AbortError') {
          setMapError(normalizeMapError(error));
          setMapSightings([]);
        }
      } finally {
        if (!abortController.signal.aborted) {
          setMapLoading(false);
        }
      }
    }, MAP_REQUEST_DEBOUNCE_MS);

    return () => {
      window.clearTimeout(timeoutId);
      abortController.abort();
    };
    // Re-run when country, shape, limit or map viewport changes
  }, [countryCode, shapeName, limit, boundsKey, viewport.zoom]);

  return (
    <div className="map-wrap">
      {mapError ? (
        <div className="banner banner-error" role="alert">
          <strong>Could not load the map</strong>
          <p>{mapError}</p>
        </div>
      ) : (
        <p className="map-status">
          {mapLoading && 'Loading sightings for the map…'}
          {!mapLoading && !mapError && mapSightings.length === 0 && fallbackSightings.length > 0 && 'Showing fallback sample pins while the full map dataset loads.'}
          {!mapLoading && !mapError && mapPoints.length === 0 && 'No sightings found for the current map data.'}
          {!mapLoading && !mapError && mapPoints.length > 0 && `Showing ${mapPoints.length} sightings on the map.`}
        </p>
      )}
      <MapContainer center={[20, 0]} zoom={2} scrollWheelZoom={false} className="map-shell">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapViewportTracker onViewportChange={handleViewportChange} />
        <ClusterMarkers clusters={visibleClusters} clusterIndex={clusterIndex} />
      </MapContainer>
    </div>
  );
};

export default SightingMap;




