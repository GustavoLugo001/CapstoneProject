import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { fetchTrees, updateTreeDates, predictTreeCare } from "./api";
import TreeMap from "./TreeMap";

const Dashboard = () => {
  const userData = JSON.parse(localStorage.getItem("userData") || "{}");

  return (
    
    <div>
      <h2>Welcome, {userData.username}</h2>
      <p style={{ textAlign: "right" }}>Role: {userData.role}</p>
      {/* Conditionally display admin code if the role is ROLE_ADMIN */}
      {userData.role === "ROLE_ADMIN" && userData.adminCode && (
        <p style={{ textAlign: "right" }} >Admin Code: {userData.adminCode}</p>
      )}
      <TreeMap />
    </div>
    
  );
};

export default Dashboard;
