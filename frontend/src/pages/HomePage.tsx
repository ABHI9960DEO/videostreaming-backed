import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../components/EmptyState";
import { Loader } from "../components/Loader";
import { videoService } from "../services/videoService";
import type { VideoItem } from "../types";

export function HomePage() {
  const [videos, setVideos] = useState<VideoItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    videoService
      .list()
      .then((res) => setVideos(res.data.data ?? []))
      .catch(() => setError("Unable to load videos. Start backend and try again."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader text="Loading videos..." />;

  return (
    <div>
      <h2>Latest Videos</h2>
      {error && <p className="error">{error}</p>}
      <div className="card-grid">
        {videos.map((video) => (
          <Link to={`/videos/${video.id}`} className="card" key={video.id}>
            <h3>{video.title}</h3>
            <p>{video.description || "No description"}</p>
            <small>By {video.uploaderName}</small>
          </Link>
        ))}
      </div>
      {!error && videos.length === 0 && <EmptyState message="No videos yet." />}
    </div>
  );
}
