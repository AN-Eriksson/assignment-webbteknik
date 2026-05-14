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

function createClusterIcon(pointCount) {
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
}

function hasCoordinates(sighting) {
  return Number.isFinite(Number(sighting?.latitude)) && Number.isFinite(Number(sighting?.longitude));
}

function toClusterFeature(sighting) {
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
}

function buildMapQuery(countryCode, limit) {
  const params = new URLSearchParams({
    limit: String(limit)
  });

  if (countryCode.trim()) {
    params.set('countryCode', countryCode.trim());
  }

  return params.toString();
}

function MapViewportTracker({ onViewportChange }) {
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
}

function ClusterPointMarker({ cluster, clusterIndex }) {
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
}

function SightingPointMarker({ sighting }) {
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
}

function ClusterMarkers({ clusters, clusterIndex }) {
  return clusters.map((cluster) => {
    if (cluster.properties.cluster) {
      return <ClusterPointMarker key={`cluster-${cluster.id}`} cluster={cluster} clusterIndex={clusterIndex} />;
    }

    const sighting = cluster.properties.sighting;
    return <SightingPointMarker key={`point-${sighting.id}`} sighting={sighting} />;
  });
}

export default function SightingMap({ countryCode = '', fallbackSightings = [], limit = 3000 }) {
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
      if (
        current.zoom === zoom &&
        current.bounds &&
        current.bounds.equals(bounds)
      ) {
        return current;
      }

      return { bounds, zoom };
    });
  }, []);

  useEffect(() => {
    const abortController = new AbortController();
    const timeoutId = window.setTimeout(async () => {
      setMapLoading(true);
      setMapError('');

      try {
        const query = buildMapQuery(countryCode, limit);
        const response = await fetch(`${API_BASE}/api/sightings/map?${query}`, {
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
          setMapError(error?.message || 'Failed to load map sightings');
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
  }, [countryCode, limit]);

  return (
    <div className="map-wrap">
      <p className="map-status">
        {mapLoading && 'Loading sightings for the map…'}
        {!mapLoading && mapError && mapError}
        {!mapLoading && !mapError && mapSightings.length === 0 && fallbackSightings.length > 0 && 'Showing fallback sample pins while the full map dataset loads.'}
        {!mapLoading && !mapError && mapPoints.length === 0 && 'No sightings found for the current map data.'}
        {!mapLoading && !mapError && mapPoints.length > 0 && `Showing ${mapPoints.length} sightings on the map.`}
      </p>
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
}




