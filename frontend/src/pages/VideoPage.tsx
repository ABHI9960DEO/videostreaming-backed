import { useMemo } from "react";
import { useParams } from "react-router-dom";

const apiBase = import.meta.env.VITE_API_BASE_URL ?? "/api";

export function VideoPage() {
  const { id } = useParams();
  const src = useMemo(() => `${apiBase}/videos/${id}/stream`, [id]);

  return (
    <div>
      <h2>Watch Video #{id}</h2>
      <video controls width="100%" src={src}>
        Your browser does not support the video tag.
      </video>
    </div>
  );
}
