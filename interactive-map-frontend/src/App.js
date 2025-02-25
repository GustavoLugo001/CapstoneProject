import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./Login";
import Registration from "./Registration";
import TreeMap from "./TreeMap";
import Logout from "./Logout"; // A simple logout component
import Dashboard from "./Dashboard";

function App() {
  return (
    <BrowserRouter>
      <div>
        <h1>Interactive AI Tree Map</h1>
        {/* Logout button (optional separate component) */}
        <Logout />
      </div>

      <Routes>
        {/* If user visits "/", redirect them to "/trees" */}
        <Route path="/" element={<Navigate to="/trees" />} />

        {/* The main map route */}
        <Route path="/dashboard/trees" element={<TreeMap />} />

        {/* Login and Registration */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/dashboard" element={<Dashboard />} />
        {/* 404 fallback if no routes match */}
        <Route path="*" element={<p>404 Not Found</p>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
