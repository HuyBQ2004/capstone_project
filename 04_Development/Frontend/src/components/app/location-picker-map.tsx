"use client";

/**
 * OpenStreetMap picker (Leaflet) — chọn tọa độ trường/nhà cung cấp lúc đăng ký (BR-028: lat/lng
 * bắt buộc, [-90,90]/[-180,180]). Client-only (Leaflet đụng `window`) — file này chỉ được import
 * qua `next/dynamic({ssr:false})` ở `location-picker.tsx`, không import trực tiếp từ page.
 *
 * Không cần API key — OpenStreetMap tile server + Nominatim (geocode tìm theo địa chỉ) đều miễn
 * phí, đúng như sơ đồ kiến trúc (OSM là GPS provider, không phải Google Maps).
 */

import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { Locate, Loader2, Search, MapPin } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

// Ảnh marker mặc định của Leaflet trỏ path tương đối vỡ khi qua webpack/Next — trỏ lại CDN.
const markerIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

const DEFAULT_CENTER: [number, number] = [16.0, 106.0]; // giữa Việt Nam
const DEFAULT_ZOOM = 5;
const PICKED_ZOOM = 16;

interface SearchResultItem {
  display_name: string;
  lat: string;
  lon: string;
}

function ClickToPick({ onPick }: { onPick: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onPick(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

function FlyTo({ lat, lng }: { lat: number; lng: number }) {
  const map = useMap();
  useEffect(() => {
    map.flyTo([lat, lng], PICKED_ZOOM);
  }, [lat, lng, map]);
  return null;
}

export function LocationPickerMap({
  lat,
  lng,
  onChange,
}: {
  lat: number | null;
  lng: number | null;
  onChange: (lat: number, lng: number) => void;
}) {
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [gettingLocation, setGettingLocation] = useState(false);
  const [searchResults, setSearchResults] = useState<SearchResultItem[]>([]);
  const [searchError, setSearchError] = useState("");

  async function doSearch() {
    if (!query.trim()) return;
    setSearching(true);
    setSearchError("");
    setSearchResults([]);
    try {
      // Nominatim (OSM) — geocode địa chỉ thành tọa độ, miễn phí, hỗ trợ tìm kiếm Việt Nam.
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=vn&q=${encodeURIComponent(query)}`
      );
      const results = (await res.json()) as SearchResultItem[];
      if (results.length === 0) {
        setSearchError("Không tìm thấy địa chỉ này — thử gõ chi tiết hơn hoặc chọn trực tiếp trên bản đồ.");
        return;
      }
      setSearchResults(results);
      // Tự động chọn kết quả phù hợp nhất đầu tiên
      const first = results[0];
      onChange(parseFloat(first.lat), parseFloat(first.lon));
    } catch {
      setSearchError("Không tìm được vị trí — kiểm tra kết nối mạng.");
    } finally {
      setSearching(false);
    }
  }

  function handleSelectResult(item: SearchResultItem) {
    onChange(parseFloat(item.lat), parseFloat(item.lon));
    setSearchResults([]);
  }

  function handleGetCurrentLocation() {
    if (!navigator.geolocation) {
      setSearchError("Trình duyệt của bạn không hỗ trợ định vị GPS.");
      return;
    }
    setGettingLocation(true);
    setSearchError("");
    setSearchResults([]);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const currentLat = pos.coords.latitude;
        const currentLng = pos.coords.longitude;
        onChange(currentLat, currentLng);
        setGettingLocation(false);
      },
      (err) => {
        setGettingLocation(false);
        switch (err.code) {
          case err.PERMISSION_DENIED:
            setSearchError("Bạn đã từ chối quyền truy cập vị trí trên trình duyệt. Vui lòng cho phép quyền vị trí trong cài đặt trình duyệt.");
            break;
          case err.POSITION_UNAVAILABLE:
            setSearchError("Không thể xác định vị trí GPS hiện tại.");
            break;
          case err.TIMEOUT:
            setSearchError("Quá thời gian chờ lấy vị trí GPS.");
            break;
          default:
            setSearchError("Không thể lấy vị trí hiện tại.");
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  }

  return (
    <div className="space-y-2">
      <div className="flex flex-col gap-2 sm:flex-row">
        <div className="flex flex-1 gap-2">
          <Input
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              if (searchResults.length > 0) setSearchResults([]);
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                e.preventDefault();
                doSearch();
              }
            }}
            placeholder="Tìm tên trường, đường, địa chỉ (VD: Đoàn Thị Điểm, Cầu Giấy...)"
            className="flex-1"
          />
          <Button type="button" variant="outline" disabled={searching} onClick={doSearch}>
            {searching ? <Loader2 className="size-4 animate-spin" /> : <Search className="size-4" />}
          </Button>
        </div>

        <Button
          type="button"
          variant="secondary"
          className="flex items-center gap-1.5 whitespace-nowrap"
          disabled={gettingLocation}
          onClick={handleGetCurrentLocation}
          title="Lấy tọa độ vị trí hiện tại của bạn qua GPS"
        >
          {gettingLocation ? (
            <Loader2 className="size-4 animate-spin" />
          ) : (
            <Locate className="size-4 text-primary" />
          )}
          <span>Vị trí hiện tại</span>
        </Button>
      </div>

      {/* Đề xuất kết quả tìm kiếm */}
      {searchResults.length > 1 && (
        <div className="rounded-md border bg-popover p-1 shadow-md">
          <p className="px-2 py-1 text-xs font-semibold text-muted-foreground">Các địa chỉ gợi ý ({searchResults.length}):</p>
          <ul className="max-h-36 overflow-y-auto divide-y text-xs">
            {searchResults.map((item, index) => (
              <li key={index}>
                <button
                  type="button"
                  onClick={() => handleSelectResult(item)}
                  className="flex w-full items-start gap-1.5 px-2 py-1.5 text-left hover:bg-accent hover:text-accent-foreground"
                >
                  <MapPin className="mt-0.5 size-3.5 shrink-0 text-primary" />
                  <span className="line-clamp-2">{item.display_name}</span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {searchError && <p className="text-xs text-destructive">{searchError}</p>}

      <div className="h-64 overflow-hidden rounded-md border">
        <MapContainer center={lat && lng ? [lat, lng] : DEFAULT_CENTER} zoom={lat && lng ? PICKED_ZOOM : DEFAULT_ZOOM} className="h-full w-full">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickToPick onPick={onChange} />
          {lat != null && lng != null && (
            <>
              <Marker position={[lat, lng]} icon={markerIcon} />
              <FlyTo lat={lat} lng={lng} />
            </>
          )}
        </MapContainer>
      </div>

      <p className="text-xs text-muted-foreground">
        Bấm vào bản đồ để chọn vị trí, gõ từ khóa tìm kiếm, hoặc bấm <span className="font-medium text-foreground">Vị trí hiện tại</span>.
        {lat != null && lng != null && (
          <span className="ml-1 font-medium text-foreground">
            Đã chọn: {lat.toFixed(5)}, {lng.toFixed(5)}
          </span>
        )}
      </p>
    </div>
  );
}
