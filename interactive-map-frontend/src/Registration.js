import React, { useState } from "react";
import { register } from "./api"; 

const Registration = () => {
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        email: "",
        role: "ROLE_USER",  // Default role
        adminCode: "",  // Optional field
    });

    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await register(formData); 

            if (response) {
                setMessage("Registration successful!");
                console.log("User registered:", response);
            } else {
                setMessage("Registration failed. Please try again.");
            }
        } catch (error) {
            console.error("Error during registration:", error);
            setMessage("Registration error. Please check your input.");
        }
    };

    return (
        <div>
            <h2>Register</h2>
            {message && <p>{message}</p>}
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    name="username"
                    placeholder="Username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />
                <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                />
                <select name="role" value={formData.role} onChange={handleChange}>
                    <option value="ROLE_USER">User</option>
                    <option value="ROLE_ADMIN">Admin</option>
                </select>
                {formData.role === "ROLE_USER" && (
                    <input
                        type="text"
                        name="adminCode"
                        placeholder="Admin Code (for users)"
                        value={formData.adminCode}
                        onChange={handleChange}
                    />
                )}
                <button type="submit">Register</button>
            </form>
        </div>
    );
};

export default Registration;
