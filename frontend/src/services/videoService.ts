import { api } from "../lib/api";
import type { ApiResponse, VideoItem } from "../types";

export const videoService = {
  list() {
    return api.get<ApiResponse<VideoItem[]>>("/videos");
  },
  upload(payload: FormData) {
    return api.post("/videos", payload, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
};
