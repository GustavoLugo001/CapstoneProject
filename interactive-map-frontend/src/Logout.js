import React from 'react';

const Logout = () => {
  const handleLogout = () => {
    localStorage.removeItem("token"); // Remove JWT from storage
    window.location.href = "/login";    // Redirect to login page
  };

  return (
    <button onClick={handleLogout} style={{ textAlign: "right" }}>
      Logout
    </button>
  );
};

export default Logout;
