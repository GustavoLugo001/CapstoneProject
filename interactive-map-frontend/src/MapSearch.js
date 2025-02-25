import React, { useState } from "react";
import { useMap } from "react-leaflet";

const MapSearch = () => {
  const map = useMap();
  const [query, setQuery] = useState("");
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(null);
    if (!query) return;

    try {
      // Use the Nominatim API for geocoding
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`
      );
      const data = await response.json();
      if (data && data.length > 0) {
        const { lat, lon } = data[0];
        // Fly the map to the found coordinates
        map.flyTo([parseFloat(lat), parseFloat(lon)], 13);
      } else {
        setError("No results found");
      }
    } catch (err) {
      console.error("Search error:", err);
      setError("Error searching for location");
    }
  };

  return (
    <div
      style={{
        position: "absolute",
        top: 10,
        left: 50,
        zIndex: 1000,
        background: "white",
        padding: "8px",
        borderRadius: "4px",
        boxShadow: "0 2px 6px rgba(0,0,0,0.3)"
      }}
    >
      <form onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search for a location..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{ width: "200px", padding: "4px" }}
        />
        <button type="submit" style={{ marginLeft: "4px", padding: "4px 8px" }}>
          Search
        </button>
      </form>
      {error && <div style={{ color: "red", marginTop: "4px" }}>{error}</div>}
    </div>
  );
};

export default MapSearch;
