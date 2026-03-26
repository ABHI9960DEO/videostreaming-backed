export type Role = "VIEWER" | "CREATOR" | "ADMIN";

export interface ApiResponse<T> {
  success: boolean;
  message?: string | null;
  data: T;
  timestamp: string;
}

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  role: Role;
  avatarUrl?: string | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userSummary: UserSummary;
}

export interface VideoItem {
  id: number;
  title: string;
  description?: string;
  uploaderId: number;
  uploaderName: string;
  streamUrl: string;
  thumbnailUrl?: string;
  createdAt: string;
}
