import { api } from "../lib/api";
import type { ApiResponse } from "../types";

export interface ChannelData {
  id: number;
  username: string;
  bio?: string;
  avatarUrl?: string;
}

export const channelService = {
  getChannel(userId: string) {
    return api.get<ApiResponse<ChannelData> | ChannelData>(`/users/${userId}/channel`);
  },
};
