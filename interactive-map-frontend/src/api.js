import axios from 'axios';

const BASE_URL = "https://localhost:8443/api"; // Java Spring Boot API
const FLASK_AI_URL = "https://127.0.0.1:5000"; // Flask AI API

function getAuthHeaders() {
  const token = localStorage.getItem("token");
  if (!token) {
    console.error("❌ No token found in localStorage. User may not be authenticated.");
    return {};
  }
  if (token.startsWith("Bearer ")) {
    return { Authorization: token };
  }
  return { Authorization: `Bearer ${token}` };
}

// ---------------------------
// Authentication and User Actions

export const login = async (username, password) => {
  try {
    const response = await axios.post(`${BASE_URL}/login`, { username, password });
    localStorage.setItem('token', response.data.token);
    return response.data;
  } catch (error) {
    console.error('Login failed:', error.response?.data || error.message);
    return null;
  }
};

export const logout = () => {
  localStorage.removeItem("token");
};

export const register = async (userData) => {
  try {
    const response = await axios.post(`${BASE_URL}/register`, userData, {
      headers: { "Content-Type": "application/json" },
    });
    return response.data;
  } catch (error) {
    console.error("Registration error:", error);
    return null;
  }
};

// ---------------------------
// Trees

export async function fetchTrees() {
  const response = await fetch(`${BASE_URL}/trees`, {
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch (error) {
    throw new Error("Invalid JSON response from server");
  }
}

export async function addTree(tree) {
  const response = await fetch(`${BASE_URL}/trees`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(tree),
  });
  return response.json();
}

export async function updateTreeDates(treeId, updates) {
  const response = await fetch(`${BASE_URL}/trees/${treeId}/update-dates`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(updates),
  });
  return response.json();
}

export async function deleteTree(treeId) {
  const response = await fetch(`${BASE_URL}/trees/${treeId}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function updateTree(treeId, updatedTree) {
  const response = await fetch(`${BASE_URL}/trees/${treeId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(updatedTree),
  });
  return response.json();
}

export async function approveTree(treeId) {
  const response = await fetch(`${BASE_URL}/trees/approve/${treeId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to approve tree: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function denyTree(treeId) {
  const response = await fetch(`${BASE_URL}/trees/deny/${treeId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to deny tree: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

// ---------------------------
// AI Predictions (Trees)

export async function predictTreeCare(tree_id) {
  const response = await fetch(`${FLASK_AI_URL}/predict_tree_care`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify({ tree_id }),
  });
  return response.json();
}

// ---------------------------
// Water Lines

export async function fetchWaterLines() {
  const response = await fetch(`${BASE_URL}/water-lines`, {
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

export async function addWaterLine(waterLine) {
  const response = await fetch(`${BASE_URL}/water-lines`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(waterLine),
  });
  return response.json();
}

export async function deleteWaterLine(id) {
  const response = await fetch(`${BASE_URL}/water-lines/${id}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function approveWaterLine(lineId) {
  const response = await fetch(`${BASE_URL}/water-lines/approve/${lineId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to approve water line: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function denyWaterLine(lineId) {
  const response = await fetch(`${BASE_URL}/water-lines/deny/${lineId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to deny water line: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

// ---------------------------
// Electrical Lines

export async function fetchElectricalLines() {
  const response = await fetch(`${BASE_URL}/electrical-lines`, {
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

export async function addElectricalLine(electricalLine) {
  const response = await fetch(`${BASE_URL}/electrical-lines`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(electricalLine),
  });
  return response.json();
}

export async function deleteElectricalLine(id) {
  const response = await fetch(`${BASE_URL}/electrical-lines/${id}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function approveElectricalLine(lineId) {
  const response = await fetch(`${BASE_URL}/electrical-lines/approve/${lineId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to approve electrical line: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function denyElectricalLine(lineId) {
  const response = await fetch(`${BASE_URL}/electrical-lines/deny/${lineId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to deny electrical line: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

// ---------------------------
// Saved Locations

export async function fetchSavedLocations() {
  const response = await fetch(`${BASE_URL}/saved-locations`, {
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

export async function addSavedLocation(savedLocation) {
  const response = await fetch(`${BASE_URL}/saved-locations`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
    body: JSON.stringify(savedLocation),
  });
  return response.json();
}

export async function deleteSavedLocation(id) {
  const response = await fetch(`${BASE_URL}/saved-locations/${id}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function approveSavedLocation(locationId) {
  const response = await fetch(`${BASE_URL}/saved-locations/approve/${locationId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to approve saved location: ${response.status} ${response.statusText}`);
  }
  return response.text();
}

export async function denySavedLocation(locationId) {
  const response = await fetch(`${BASE_URL}/saved-locations/deny/${locationId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error(`Failed to deny saved location: ${response.status} ${response.statusText}`);
  }
  return response.text();
}
