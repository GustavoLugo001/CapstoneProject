import React, { useState, useEffect } from "react";
import "leaflet/dist/leaflet.css";
import MapSearch from "./MapSearch";
import {
  fetchTrees,
  predictTreeCare,
  addTree,
  deleteTree,
  updateTree,
  updateTreeDates,
  fetchWaterLines,
  addWaterLine,
  deleteWaterLine,
  fetchElectricalLines,
  addElectricalLine,
  deleteElectricalLine,
  fetchSavedLocations,
  addSavedLocation,
  deleteSavedLocation,
  approveTree,
  denyTree,
  approveWaterLine,
  denyWaterLine,
  approveElectricalLine,
  denyElectricalLine,
  approveSavedLocation,
  denySavedLocation,
} from "./api";
import {
  MapContainer,
  TileLayer,
  Circle,
  Popup,
  Polyline,
  Marker,
  useMapEvents,
} from "react-leaflet";

// Component to capture map clicks
function MapClickHandler({ mode, setNewTreePosition, setCurrentLineCoords, setNewLocationPosition }) {
  useMapEvents({
    
    click(e) {
      
      const { lat, lng } = e.latlng;
      // Based on the mode, decide what to do with the click
      if (mode === "tree") {
        setNewTreePosition({ latitude: lat, longitude: lng });
      } else if (mode === "water" || mode === "electrical") {
        // Add the clicked point to our line coords array
        setCurrentLineCoords((prev) => [...prev.map((p) => [...p]), [lat, lng]]);
      } else if (mode === "location") {
        setNewLocationPosition({ latitude: lat, longitude: lng });
      }
    },
    
  });
  
  return null;
}

const TreeMap = () => {
  // ===========================
  // State for all data
  // ===========================
  const [trees, setTrees] = useState([]);
  const [waterLines, setWaterLines] = useState([]);
  const [electricalLines, setElectricalLines] = useState([]);
  const [savedLocations, setSavedLocations] = useState([]);

  // ===========================
  // State for single-layer "mode"
  // ===========================
  // e.g. "tree", "water", "electrical", "location"
  const [mode, setMode] = useState("tree");

  // ===========================
  // Tree-related states
  // ===========================
  const [selectedTree, setSelectedTree] = useState(null);
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // For adding a new tree
  const [newTreePosition, setNewTreePosition] = useState(null);
  const [newTreeSpecies, setNewTreeSpecies] = useState("");
  const [newTreeHealthStatus, setNewTreeHealthStatus] = useState("");

  // For updating tree dates
  const [plantingDate, setPlantingDate] = useState("");
  const [lastWateringDate, setLastWateringDate] = useState("");
  const [lastFertilizationDate, setLastFertilizationDate] = useState("");

  // For editing health info
  const [species, setSpecies] = useState("");
  const [healthStatus, setHealthStatus] = useState("");
  const [healthNote, setHealthNote] = useState("");
  const [isEditingHealth, setIsEditingHealth] = useState(false);

  // ===========================
  // Water/Electrical line states
  // ===========================
  const [currentLineCoords, setCurrentLineCoords] = useState([]);
  const [newWaterLineName, setNewWaterLineName] = useState("");
  const [newWaterLineDescription, setNewWaterLineDescription] = useState("");
  const [newElectricalLineName, setNewElectricalLineName] = useState("");
  const [newElectricalLineDescription, setNewElectricalLineDescription] = useState("");

  // ===========================
  // Saved locations
  // ===========================
  const [newLocationPosition, setNewLocationPosition] = useState(null);
  const [newSavedLocationName, setNewSavedLocationName] = useState("");
  const [newSavedLocationDescription, setNewSavedLocationDescription] = useState("");

  // ===========================
  // Effects
  // ===========================
  useEffect(() => {
    // On mount, fetch all data
    (async () => {
      try {
        const treeData = await fetchTrees();
        setTrees(treeData);

        const waterData = await fetchWaterLines();
        setWaterLines(waterData);

        const electricalData = await fetchElectricalLines();
        setElectricalLines(electricalData);

        const locData = await fetchSavedLocations();
        setSavedLocations(locData);
      } catch (err) {
        console.error("Failed to load data:", err);
      }
    })();
  }, []);

  // When a tree is selected, populate health fields
  useEffect(() => {
    if (selectedTree) {
      setSpecies(selectedTree.species);
      setHealthStatus(selectedTree.healthStatus);
      setHealthNote(selectedTree.healthNote || "");
      setIsEditingHealth(false);
    }
  }, [selectedTree]);

  const userData = JSON.parse(localStorage.getItem("userData") || "{}");
  // ===========================
  // Mode switch UI
  // ===========================
  // const renderModeSwitch = () => (
  //   <div style={{ marginBottom: "10px" }}>
  //     <select value={mode} onChange={(e) => {
  //       setMode(e.target.value);
  //       setCurrentLineCoords([]); // clear line coords whenever we switch modes
  //     }}>
  //       <option value="tree">Add Tree</option>
  //       <option value="water">Add Water Line</option>
  //       <option value="electrical">Add Electrical Line</option>
  //       <option value="location">Add Saved Location</option>
  //     </select>
  //   </div>
  // );

  const handleUndoLastPoint = () => {
    if (currentLineCoords.length > 0) {
      setCurrentLineCoords((prev) => prev.slice(0, -1)); // Removes last point
    }
  };
  
  // ===========================
  // Add Tree
  // ===========================
  const handleAddTree = async () => {
    if (!newTreePosition) {
      alert("Click on the map to choose a location first!");
      return;
    }
    if (!newTreeSpecies.trim()) {
      alert("Please enter a species.");
      return;
    }
    if (!newTreeHealthStatus.trim()) {
      alert("Please enter a health status.");
      return;
    }
    const treeData = {
      latitude: parseFloat(newTreePosition.latitude),
      longitude: parseFloat(newTreePosition.longitude),
      species: newTreeSpecies,
      healthStatus: newTreeHealthStatus,
    };
    try {
      const result = await addTree(treeData);
      if (result) {
        // Refresh trees
        const updated = await fetchTrees();
        setTrees(updated);
      }
      // Clear form
      setNewTreePosition(null);
      setNewTreeSpecies("");
      setNewTreeHealthStatus("");
    } catch (err) {
      console.error("Error adding tree:", err);
    }
  };

  const handleDeleteTree = async (treeId) => {
    try {
      await deleteTree(treeId);
      const updated = await fetchTrees();
      setTrees(updated);
      if (selectedTree && selectedTree.id === treeId) {
        setSelectedTree(null);
      }
    } catch (err) {
      console.error("Error deleting tree:", err);
      alert("Failed to delete tree. Check console for details.");
    }
  };

  // ===========================
  // Water Line
  // ===========================
  const handleAddWaterLine = async () => {
    if (currentLineCoords.length < 2) {
      alert("At least 2 points are required to form a line.");
      return;
    }
    if (!newWaterLineName.trim()) {
      alert("Enter a name for this water line.");
      return;
    }
    const payload = {
      name: newWaterLineName,
      lineGeometry: JSON.stringify(currentLineCoords),
      description: newWaterLineDescription,
    };
    try {
      await addWaterLine(payload);
      const updated = await fetchWaterLines();
      setWaterLines(updated);
      // Clear
      setCurrentLineCoords([]);
      setNewWaterLineName("");
      setNewWaterLineDescription("");
    } catch (err) {
      console.error("Error adding water line:", err);
    }
  };

  const handleDeleteWaterLine = async (id) => {
    try {
      await deleteWaterLine(id);
      const updated = await fetchWaterLines();
      setWaterLines(updated);
    } catch (err) {
      console.error("Error deleting water line:", err);
    }
  };

  // ===========================
  // Electrical Line
  // ===========================
  const handleAddElectricalLine = async () => {
    if (currentLineCoords.length < 2) {
      alert("At least 2 points are required to form a line.");
      return;
    }
    if (!newElectricalLineName.trim()) {
      alert("Enter a name for this electrical line.");
      return;
    }
    const payload = {
      name: newElectricalLineName,
      lineGeometry: JSON.stringify(currentLineCoords),
      description: newElectricalLineDescription,
    };
    try {
      await addElectricalLine(payload);
      const updated = await fetchElectricalLines();
      setElectricalLines(updated);
      // Clear
      setCurrentLineCoords([]);
      setNewElectricalLineName("");
      setNewElectricalLineDescription("");
    } catch (err) {
      console.error("Error adding electrical line:", err);
    }
  };

  const handleDeleteElectricalLine = async (id) => {
    try {
      await deleteElectricalLine(id);
      const updated = await fetchElectricalLines();
      setElectricalLines(updated);
    } catch (err) {
      console.error("Error deleting electrical line:", err);
    }
  };

  // ===========================
  // Saved Location
  // ===========================
  const handleAddSavedLocation = async () => {
    if (!newLocationPosition || !newSavedLocationName.trim()) {
      alert("Pick a location on the map and enter a name.");
      return;
    }
    const payload = {
      name: newSavedLocationName,
      location: JSON.stringify([newLocationPosition.latitude, newLocationPosition.longitude]),
      description: newSavedLocationDescription,
    };
    try {
      await addSavedLocation(payload);
      const updated = await fetchSavedLocations();
      setSavedLocations(updated);
      // Clear
      setNewLocationPosition(null);
      setNewSavedLocationName("");
      setNewSavedLocationDescription("");
    } catch (err) {
      console.error("Error adding saved location:", err);
    }
  };

  const handleDeleteSavedLocation = async (id) => {
    try {
      await deleteSavedLocation(id);
      const updated = await fetchSavedLocations();
      setSavedLocations(updated);
    } catch (err) {
      console.error("Error deleting saved location:", err);
    }
  };

  // ===========================
  // Update Tree Dates
  // ===========================
  const handleUpdateDates = async () => {
    if (!selectedTree) {
      alert("No tree selected!");
      return;
    }
    const updates = {};
    if (plantingDate) updates.planting_date = plantingDate;
    if (lastWateringDate) updates.last_watering_date = lastWateringDate;
    if (lastFertilizationDate) updates.last_fertilization_date = lastFertilizationDate;

    try {
      await updateTreeDates(selectedTree.id, updates);
      const updatedTrees = await fetchTrees();
      setTrees(updatedTrees);

      const updatedTree = updatedTrees.find((t) => t.id === selectedTree.id);
      if (updatedTree) setSelectedTree(updatedTree);

      setPlantingDate("");
      setLastWateringDate("");
      setLastFertilizationDate("");
    } catch (err) {
      console.error("Error updating tree dates:", err);
    }
  };

  // ===========================
  // Health Info
  // ===========================
  const handleEditClick = () => setIsEditingHealth(true);
  const handleCancelClick = () => {
    if (selectedTree) {
      setSpecies(selectedTree.species);
      setHealthStatus(selectedTree.healthStatus);
      setHealthNote(selectedTree.healthNote || "");
    }
    setIsEditingHealth(false);
  };

  const handleSaveHealthInfo = async () => {
    if (!selectedTree) {
      alert("No tree selected!");
      return;
    }
    const updatedTree = {
      species,
      healthStatus,
      healthNote,
    };
    try {
      await updateTree(selectedTree.id, updatedTree);
      const updatedTrees = await fetchTrees();
      setTrees(updatedTrees);

      const found = updatedTrees.find((t) => t.id === selectedTree.id);
      setSelectedTree(found || null);
      setIsEditingHealth(false);
    } catch (err) {
      console.error("Error updating health info:", err);
    }
  };

  // ===========================
  // AI Predictions
  // ===========================
  const fetchPredictions = async (treeId) => {
    setLoading(true);
    setError(null);
    setPrediction(null);
    try {
      const result = await predictTreeCare(treeId);
      if (result) {
        setPrediction(result);
      } else {
        setError("Failed to fetch AI predictions.");
      }
    } catch (err) {
      setError("An error occurred while fetching AI predictions.");
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // Tree circle coloring
  // ===========================
  const getCircleColor = (tree) => {
    if (tree.approvalStatus === "PENDING") {
      return "brown";
    }
    if (tree.approvalStatus === "DENIED") {
      return "gray";
    }
    if (selectedTree && selectedTree.id === tree.id && prediction) {
      const isOverdue = prediction.notifications?.some((note) =>
        note.includes("overdue")
      );
      if (isOverdue) return "purple";
    }
    switch (tree.healthStatus) {
      case "Good": return "green";
      case "Bad":  return "red";
      case "Dead": return "black";
      default:     return "green";
    }
  };
 // Approval / Denial Handlers for Trees
  // ===========================
  const handleApproveTree = async (treeId) => {
    try {
      const result = await approveTree(treeId);
      console.log("Tree approved:", result);
      const updated = await fetchTrees();
      setTrees(updated);
    } catch (err) {
      console.error("Error approving tree:", err);
    }
  };

  const handleDenyTree = async (treeId) => {
    try {
      const result = await denyTree(treeId);
      console.log("Tree denied:", result);
      const updated = await fetchTrees();
      setTrees(updated);
    } catch (err) {
      console.error("Error denying tree:", err);
    }
  };

  // ===========================
  // Approval / Denial for Water Lines
  // ===========================
  const handleApproveWaterLine = async (lineId) => {
    try {
      const result = await approveWaterLine(lineId);
      console.log("Water line approved:", result);
      const updated = await fetchWaterLines();
      setWaterLines(updated);
    } catch (err) {
      console.error("Error approving water line:", err);
    }
  };

  const handleDenyWaterLine = async (lineId) => {
    try {
      const result = await denyWaterLine(lineId);
      console.log("Water line denied:", result);
      const updated = await fetchWaterLines();
      setWaterLines(updated);
    } catch (err) {
      console.error("Error denying water line:", err);
    }
  };

  // ===========================
  // Approval / Denial for Electrical Lines
  // ===========================
  const handleApproveElectricalLine = async (lineId) => {
    try {
      const result = await approveElectricalLine(lineId);
      console.log("Electrical line approved:", result);
      const updated = await fetchElectricalLines();
      setElectricalLines(updated);
    } catch (err) {
      console.error("Error approving electrical line:", err);
    }
  };

  const handleDenyElectricalLine = async (lineId) => {
    try {
      const result = await denyElectricalLine(lineId);
      console.log("Electrical line denied:", result);
      const updated = await fetchElectricalLines();
      setElectricalLines(updated);
    } catch (err) {
      console.error("Error denying electrical line:", err);
    }
  };

  // ===========================
  // Approval / Denial for Saved Locations
  // ===========================
  const handleApproveSavedLocation = async (locId) => {
    try {
      const result = await approveSavedLocation(locId);
      console.log("Saved location approved:", result);
      const updated = await fetchSavedLocations();
      setSavedLocations(updated);
    } catch (err) {
      console.error("Error approving saved location:", err);
    }
  };

  const handleDenySavedLocation = async (locId) => {
    try {
      const result = await denySavedLocation(locId);
      console.log("Saved location denied:", result);
      const updated = await fetchSavedLocations();
      setSavedLocations(updated);
    } catch (err) {
      console.error("Error denying saved location:", err);
    }
  };
  // ===========================
  // Renderers
  // ===========================
  const renderTrees = () =>
    trees.map((tree) => (
      <Circle
        key={tree.id}
        center={[tree.latitude, tree.longitude]}
        radius={0.5}
        pathOptions={{
          color: getCircleColor(tree),
          fillColor: getCircleColor(tree),
          fillOpacity: 0.5,
        }}
        eventHandlers={{
          click: () => {
            setSelectedTree(tree);
            fetchPredictions(tree.id);
          },
        }}
      >
        <Popup>
          <strong>Tree ID:</strong> {tree.id}
          <br />
          <br />
          Species: {tree.species}
          <br />
          Planted Date: {tree.plantingDate}
          <br />
          Last watering date: {tree.lastWateringDate}
          <br />
          Last Fertilization date: {tree.lastFertilizationDate}
          <br />
          Health: {tree.healthStatus}
          <br />
          Approval Status: {tree.approvalStatus}
          <br />
          {userData.role === "ROLE_ADMIN" && tree.approvalStatus === "PENDING" && (
            <>
              <button onClick={() => handleApproveTree(tree.id)}>Approve</button>
              <button onClick={() => handleDenyTree(tree.id)}>Deny</button>
              <br />
            </>
          )}
          <button onClick={() => fetchPredictions(tree.id)} disabled={loading}>
            {loading ? "Loading..." : "Get AI Predictions"}
          </button>
          <button onClick={() => handleDeleteTree(tree.id)}>Delete Tree</button>
        </Popup>
      </Circle>
    ));


  const renderWaterLines = () =>
    waterLines.map((line) => {
      const positions = JSON.parse(line.lineGeometry);
      const lineColor = line.approvalStatus === "APPROVED" ? "blue" : 
                      line.approvalStatus === "DENIED" ? "gray" : 
                      "purple";
      return (
        <Polyline key={line.id} positions={positions} pathOptions={{ color: lineColor }}>
          <Popup>
            <strong>Water Line ID:</strong> {line.id}
            <br />
            Water note: {line.description}
            <br />
            Approval Status: {line.approvalStatus}
          <br />
            {userData.role === "ROLE_ADMIN" && line.approvalStatus === "PENDING" && (
            <>
              <button onClick={() => handleApproveWaterLine(line.id)}>Approve</button>
              <button onClick={() => handleDenyWaterLine(line.id)}>Deny</button>
              <br />
            </>
            )}
            <button onClick={() => handleDeleteWaterLine(line.id)}>Delete Water Line</button>
          </Popup>
        </Polyline>
      );
    });

  const renderElectricalLines = () =>
    electricalLines.map((line) => {
      const positions = JSON.parse(line.lineGeometry);
      const lineColor = line.approvalStatus === "APPROVED" ? "orange" : 
                      line.approvalStatus === "DENIED" ? "gray" : 
                      "pink";
      return (
        <Polyline key={line.id} positions={positions} pathOptions={{ color: lineColor }}>
          <Popup>
            <strong>Electrical Line ID:</strong> {line.id}
            <br />
            Electrical note: {line.description}
            <br />
            Approval Status: {line.approvalStatus}
          <br />
            {userData.role === "ROLE_ADMIN" && line.approvalStatus === "PENDING" && (
            <>
              <button onClick={() => handleApproveElectricalLine(line.id)}>Approve</button>
              <button onClick={() => handleDenyElectricalLine(line.id)}>Deny</button>
              <br />
            </>
            )}
            <button onClick={() => handleDeleteElectricalLine(line.id)}>
              Delete Electrical Line
            </button>
          </Popup>
        </Polyline>
      );
    });

  const renderLocations = () =>
    savedLocations.map((loc) => {
      const coords = JSON.parse(loc.location);
      return (
        <Marker key={loc.id} position={coords}>
          <Popup>
            <strong>Location ID:</strong> {loc.id}
            <br />
            Saved location area: {loc.description}
            <br />
            Approval Status: {loc.approvalStatus}
          <br />
            {userData.role === "ROLE_ADMIN" && loc.approvalStatus === "PENDING" && (
            <>
              <button onClick={() => handleApproveSavedLocation(loc.id)}>Approve</button>
              <button onClick={() => handleDenySavedLocation(loc.id)}>Deny</button>
              <br />
            </>
            )}
            <button onClick={() => handleDeleteSavedLocation(loc.id)}>
              Delete Location
            </button>
          </Popup>
        </Marker>
      );
    });

  // ===========================
  // Render
  // ===========================
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      <h2>📍 Map select mode to add </h2>

      {/* Mode Selection */}
      <div style={{ marginBottom: "10px" }}>
        <select value={mode} onChange={(e) => {
          setMode(e.target.value);
          setCurrentLineCoords([]);
        }}>
          <option value="tree">Add Tree</option>
          <option value="water">Add Water Line</option>
          <option value="electrical">Add Electrical Line</option>
          <option value="location">Add Saved Location</option>
        </select>
      </div>
      {(mode === "water" || mode === "electrical") && currentLineCoords.length > 0 && (
      <button 
        onClick={handleUndoLastPoint}
        style={{
          position: "absolute",
          top: "465px",
          left: "465px",
          zIndex: 1000,  // Ensures it’s always on top
          padding: "5px 10px",
          backgroundColor: "white",
          color: "purple",
          border: "2px solid #ddd",
          cursor: "pointer",
          borderRadius: "5px",
        }}
      >
        Undo Last Point 🔙
      </button>
    )}

      {/* ========== MODE FORMS ========== */}
      {mode === "tree" && (
        <div style={{ marginBottom: 20, padding: 10, border: "1px solid #ddd", borderRadius: 5 }}>
          <h3>🌳 Add a New Tree {userData.role}</h3>
          {newTreePosition && (
            <p>Pending Tree: {newTreePosition.latitude}, {newTreePosition.longitude}</p>
          )}
          <input
            type="text"
            placeholder="Species"
            value={newTreeSpecies}
            onChange={(e) => setNewTreeSpecies(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <select
            value={newTreeHealthStatus}
            onChange={(e) => setNewTreeHealthStatus(e.target.value)}
            style={{ marginRight: "8px" }}
          >
            <option value="">Health Status</option>
            <option value="Good">Good</option>
            <option value="Bad">Bad</option>
            <option value="Dead">Dead</option>
          </select>
          <button onClick={handleAddTree}>➕ Add Tree</button>
        </div>
      )}

      {mode === "water" && (
        <div style={{ marginBottom: 10, padding: 10, border: "1px solid #ddd", borderRadius: 5 }}>
          <h3>Add Water Line</h3>
          {currentLineCoords.length > 0 && (
            <p>Points: {JSON.stringify(currentLineCoords)}</p>
          )}
          <input
            type="text"
            placeholder="Name"
            value={newWaterLineName}
            onChange={(e) => setNewWaterLineName(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <input
            type="text"
            placeholder="Description"
            value={newWaterLineDescription}
            onChange={(e) => setNewWaterLineDescription(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <button onClick={handleAddWaterLine}>Save Water Line</button>
        </div>
      )}

      {mode === "electrical" && (
        <div style={{ marginBottom: 10, padding: 10, border: "1px solid #ddd", borderRadius: 5 }}>
          <h3>Add Electrical Line</h3>
          {currentLineCoords.length > 0 && (
            <p>Points: {JSON.stringify(currentLineCoords)}</p>
          )}
          <input
            type="text"
            placeholder="Name"
            value={newElectricalLineName}
            onChange={(e) => setNewElectricalLineName(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <input
            type="text"
            placeholder="Description"
            value={newElectricalLineDescription}
            onChange={(e) => setNewElectricalLineDescription(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <button onClick={handleAddElectricalLine}>Save Electrical Line</button>
        </div>
      )}

      {mode === "location" && (
        <div style={{ marginBottom: 10, padding: 10, border: "1px solid #ddd", borderRadius: 5 }}>
          <h3>Add Saved Location</h3>
          {newLocationPosition && (
            <p>Location: {newLocationPosition.latitude}, {newLocationPosition.longitude}</p>
          )}
          <input
            type="text"
            placeholder="Name"
            value={newSavedLocationName}
            onChange={(e) => setNewSavedLocationName(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <input
            type="text"
            placeholder="Description"
            value={newSavedLocationDescription}
            onChange={(e) => setNewSavedLocationDescription(e.target.value)}
            style={{ marginRight: "8px" }}
          />
          <button onClick={handleAddSavedLocation}>Save Location</button>
        </div>
      )}

      {/* MAP */}
      <div style={{ width: "100%", height: "800px" }}>
        <MapContainer center={[37.7749, -122.4194]} zoom={13} style={{ height: "100%", width: "100%" }}>
          <TileLayer
            url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}"
            attribution='Tiles &copy; Esri &mdash; Source: Esri'
          />
            <MapSearch />
          <MapClickHandler
            mode={mode}
            setNewTreePosition={setNewTreePosition}
            setCurrentLineCoords={setCurrentLineCoords}
            setNewLocationPosition={setNewLocationPosition}
          />

          {/* Always render everything */}
          {renderTrees()}
          {renderWaterLines()}
          {renderElectricalLines()}
          {renderLocations()}

          {(mode === "water" || mode === "electrical") && currentLineCoords.length > 0 && (
            <>
              {/* Dashed Line for Visibility */}
              <Polyline
                positions={currentLineCoords}
                pathOptions={{
                  color: mode === "water" ? "blue" : "orange",
                  dashArray: "5, 10", // 🔥 Dashed pattern for better visibility
                  weight: 4,
                  opacity: 0.8,
                }}
              />
               <Circle
                    center={currentLineCoords[currentLineCoords.length - 1]}
                    radius={2}  // 🔥 Small circle dot
                    pathOptions={{ color: "black", fillColor: "black", fillOpacity: 1 }}
                  />
                </>
              )}

              

     
              
          {mode === "tree" && newTreePosition && (
          <Circle
            center={[newTreePosition.latitude, newTreePosition.longitude]}
            radius={0.5}
            pathOptions={{ color: "blue", fillColor: "blue", fillOpacity: 0.5 }}
          />
        )}


        </MapContainer>
      </div>

      {/* Update Panel for the selected tree (only if a tree is selected) */}
      {selectedTree && prediction && mode === "tree" && (
        <div
          style={{
            position: "absolute",
            top: "500px",
            right: "10px",
            width: "300px",
            backgroundColor: "white",
            border: "5px solid #ddd",
            borderRadius: "5px",
            padding: "10px",
            zIndex: 1000,
          }}
        >
          <h3>Update Dates for Tree {selectedTree.id}</h3>
          <label>
            Planting Date:
            <input
              type="date"
              value={plantingDate}
              onChange={(e) => setPlantingDate(e.target.value)}
            />
          </label>
          <br />
          <label>
            Last Watering Date:
            <input
              type="date"
              value={lastWateringDate}
              onChange={(e) => setLastWateringDate(e.target.value)}
            />
          </label>
          <br />
          <label>
            Last Fertilization Date:
            <input
              type="date"
              value={lastFertilizationDate}
              onChange={(e) => setLastFertilizationDate(e.target.value)}
            />
          </label>
          <br />
          <button onClick={handleUpdateDates}>Update Dates</button>
          <hr style={{ margin: "10px 0" }} />

          <h3>Health Information</h3>
          {!isEditingHealth ? (
            <>
              <p>
                <strong>Species:</strong> {species}
              </p>
              <p>
                <strong>Health Note:</strong> {healthNote}
              </p>
              <p>
                <strong>Health Status:</strong> {healthStatus}
              </p>
              <button onClick={handleEditClick}>Edit Health Info</button>
            </>
          ) : (
            <>
              <label>
                Species:
                <input
                  type="text"
                  value={species}
                  onChange={(e) => setSpecies(e.target.value)}
                />
              </label>
              <br />
              <label>
                Health Note:
                <textarea
                  rows="4"
                  cols="30"
                  value={healthNote}
                  onChange={(e) => setHealthNote(e.target.value)}
                  style={{ display: "block", marginTop: "5px", marginBottom: "10px" }}
                />
              </label>
              <br />
              <label>
                Health Status:
                <select
                  value={healthStatus}
                  onChange={(e) => setHealthStatus(e.target.value)}
                >
                  <option value="">Select Health Status</option>
                  <option value="Good">Good</option>
                  <option value="Bad">Bad</option>
                  <option value="Dead">Dead</option>
                </select>
              </label>
              <br />
              <button onClick={handleSaveHealthInfo}>Save</button>
              <button onClick={handleCancelClick} style={{ marginLeft: "8px" }}>
                Cancel
              </button>
            </>
          )}
          <hr style={{ margin: "10px 0" }} />

          <h3>🌿 Tree Health Insights (Tree {selectedTree.id})</h3>
          <p>
            <strong>🌧️ Conditions:</strong> {prediction.conditions}, {prediction.temperature} °F
          </p>
          <p>
            <strong>💧 Next Watering:</strong> {prediction.next_watering_date}, days:{" "}
            {prediction.predicted_next_watering_days}
          </p>
          <p>
            <strong>🌿 Next Fertilization:</strong> {prediction.next_fertilization_date}, days:{" "}
            {prediction.predicted_next_fertilization_days}
          </p>
          <p>
            <strong>🌰 Soil moisture estimate:</strong> {prediction.soil_moisture_level}
          </p>
          {prediction.notifications && prediction.notifications.length > 0 && (
            <div>
              <strong>⚠ Notifications:</strong>
              <ul>
                {prediction.notifications.map((note, index) => (
                  <li key={index}>{note}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {error && <p style={{ color: "red" }}>⚠ {error}</p>}
    </div>
  );
};

export default TreeMap;