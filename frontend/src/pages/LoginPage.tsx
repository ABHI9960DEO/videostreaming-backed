import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(email, password);
      navigate("/");
    } catch {
      setError("Login failed. Check credentials and backend status.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="form" onSubmit={onSubmit}>
      <h2>Login</h2>
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" type="email" required />
      <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" type="password" required />
      {error && <p className="error">{error}</p>}
      <button disabled={loading} type="submit">{loading ? "Signing in..." : "Sign in"}</button>
      <p className="muted">No account? <Link to="/register">Create one</Link></p>
    </form>
  );
}
