import { useAuth } from "../context/AuthContext";

export function DashboardPage() {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  return (
    <section className="card">
      <h2>Account</h2>
      <p><strong>Username:</strong> {user.username}</p>
      <p><strong>Email:</strong> {user.email}</p>
      <p><strong>Role:</strong> {user.role}</p>
      <p><strong>User ID:</strong> {user.id}</p>
    </section>
  );
}
