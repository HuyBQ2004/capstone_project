"use client";

import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

const markerIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

export function SchoolMapPreviewImpl({
  lat,
  lng,
  name,
  address,
}: {
  lat: number | null;
  lng: number | null;
  name: string;
  address?: string;
}) {
  if (lat == null || lng == null) {
    return (
      <div className="flex h-52 items-center justify-center rounded-lg border bg-muted/40 text-sm text-muted-foreground">
        Chưa có thông tin tọa độ GPS cho trường này.
      </div>
    );
  }

  const position: [number, number] = [lat, lng];

  return (
    <div className="h-52 overflow-hidden rounded-lg border shadow-sm">
      <MapContainer center={position} zoom={15} className="h-full w-full">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={position} icon={markerIcon}>
          <Popup>
            <div className="p-1">
              <strong className="block text-sm">{name}</strong>
              {address && <span className="text-xs text-muted-foreground">{address}</span>}
              <br />
              <span className="text-xs font-mono text-primary">Tọa độ: {lat.toFixed(5)}, {lng.toFixed(5)}</span>
            </div>
          </Popup>
        </Marker>
      </MapContainer>
    </div>
  );
}
