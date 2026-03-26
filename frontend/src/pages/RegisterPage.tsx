import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await register(username, email, password);
      navigate("/");
    } catch {
      setError("Registration failed. Validate fields or backend status.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="form" onSubmit={onSubmit}>
      <h2>Create account</h2>
      <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Username" required />
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" type="email" required />
      <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" type="password" required />
      {error && <p className="error">{error}</p>}
      <button disabled={loading} type="submit">{loading ? "Creating..." : "Create account"}</button>
      <p className="muted">Already have an account? <Link to="/login">Sign in</Link></p>
    </form>
  );
}
