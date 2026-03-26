import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Loader } from "../components/Loader";
import { channelService, type ChannelData } from "../services/channelService";

export function ChannelPage() {
  const { userId } = useParams();
  const [channel, setChannel] = useState<ChannelData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId) {
      setError("User id is missing.");
      setLoading(false);
      return;
    }

    channelService
      .getChannel(userId)
      .then((res) => {
        const payload = res.data;
        if (payload && typeof payload === "object" && "data" in payload) {
          setChannel(payload.data as ChannelData);
        } else {
          setChannel(payload as ChannelData);
        }
      })
      .catch(() => setError("Channel is unavailable."))
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <Loader text="Loading channel..." />;
  if (error) return <p className="error">{error}</p>;
  if (!channel) return <p className="muted">Channel not found.</p>;

  return (
    <div className="card">
      <h2>{channel.username}</h2>
      <p>{channel.bio || "No bio yet."}</p>
      <small>User #{channel.id}</small>
    </div>
  );
}
