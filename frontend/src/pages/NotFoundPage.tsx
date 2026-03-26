import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="card">
      <h2>Page not found</h2>
      <p className="muted">The page you requested does not exist.</p>
      <Link to="/">Go home</Link>
    </div>
  );
}
