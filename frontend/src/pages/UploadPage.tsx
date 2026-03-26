import { useState } from "react";
import { videoService } from "../services/videoService";

export function UploadPage() {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!file) {
      setError("Please choose a video file");
      return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("description", description);
    formData.append("file", file);

    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      await videoService.upload(formData);
      setMessage("Video uploaded successfully.");
      setTitle("");
      setDescription("");
      setFile(null);
    } catch {
      setError("Upload failed. Ensure backend upload endpoint exists and user has access.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="form" onSubmit={submit}>
      <h2>Upload Video</h2>
      <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Title" required />
      <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description" rows={4} />
      <input type="file" accept="video/*" onChange={(e) => setFile(e.target.files?.[0] ?? null)} required />
      {error && <p className="error">{error}</p>}
      {message && <p className="success">{message}</p>}
      <button disabled={loading} type="submit">{loading ? "Uploading..." : "Upload"}</button>
    </form>
  );
}
