import { useEffect, useMemo } from 'react';
import L from 'leaflet';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import markerIcon2xUrl from 'leaflet/dist/images/marker-icon-2x.png';
import markerIconUrl from 'leaflet/dist/images/marker-icon.png';
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png';

const mapMarkerIcon = new L.Icon({
  iconUrl: markerIconUrl,
  iconRetinaUrl: markerIcon2xUrl,
  shadowUrl: markerShadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

function hasCoordinates(sighting) {
  return Number.isFinite(Number(sighting?.latitude)) && Number.isFinite(Number(sighting?.longitude));
}

function MapFitBounds({ points }) {
  const map = useMap();

  useEffect(() => {
    if (!points.length) {
      return;
    }

    if (points.length === 1) {
      const [singlePoint] = points;
      map.setView([Number(singlePoint.latitude), Number(singlePoint.longitude)], 5);
      return;
    }

    const bounds = L.latLngBounds(points.map((point) => [Number(point.latitude), Number(point.longitude)]));
    map.fitBounds(bounds, { padding: [24, 24] });
  }, [map, points]);

  return null;
}

export default function SightingMap({ sightings }) {
  const mapPoints = useMemo(() => sightings.filter(hasCoordinates), [sightings]);

  if (mapPoints.length === 0) {
    return <p className="map-empty">No sightings with coordinates in the current sample.</p>;
  }

  return (
    <div className="map-wrap">
      <MapContainer center={[20, 0]} zoom={2} scrollWheelZoom={false} className="map-shell">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapFitBounds points={mapPoints} />
        {mapPoints.map((sighting) => {
          const latitude = Number(sighting.latitude);
          const longitude = Number(sighting.longitude);
          const popupTitle = `${sighting.city || 'Unknown city'} • ${sighting.shapeName || 'unknown shape'}`;

          return (
            <Marker
              key={sighting.id}
              position={[latitude, longitude]}
              icon={mapMarkerIcon}
            >
              <Popup>
                <strong>{popupTitle}</strong>
                <br />
                {sighting.datePosted || 'No date'}
                <br />
                {sighting.countryCode ? `Country: ${sighting.countryCode.toUpperCase()}` : 'Country: unknown'}
                <br />
                Coordinates: {latitude.toFixed(3)}, {longitude.toFixed(3)}
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
}




