import { HttpUtil } from '../utils/HttpUtil';

export interface UserStats {
  userId: number;
  nickname: string;
  memberLevel: number;
  dailyAnalysisCount: number;
  totalAnalysis: number;
  totalShare: number;
  lateNightCount: number;
  workplaceCount: number;
  romanceCount: number;
  memberExpireTime?: string;
  canWatchAd: boolean;
}

export interface LoginRequest {
  email?: string;
  password?: string;
  openid?: string;
  deviceId?: string;
}

export class UserApi {
  static async login(email: string, password: string): Promise<UserStats> {
    return await HttpUtil.post<UserStats>('/api/user/login', { email, password });
  }

  static async register(email: string, password: string): Promise<UserStats> {
    return await HttpUtil.post<UserStats>('/api/user/register', { email, password });
  }

  static async guestLogin(deviceId: string): Promise<UserStats> {
    return await HttpUtil.post<UserStats>('/api/user/guest-login', { deviceId });
  }

  static async getStats(): Promise<UserStats> {
    return await HttpUtil.get<UserStats>('/api/user/stats');
  }

  static async reset(): Promise<void> {
    return await HttpUtil.post<void>('/api/user/reset');
  }

  static async updateInfo(nickname: string): Promise<void> {
    return await HttpUtil.post<void>('/api/user/update-info', { nickname });
  }

  static async watchAdComplete(): Promise<UserStats> {
    return await HttpUtil.post<UserStats>('/api/user/watch-ad-complete');
  }

  static async checkCanWatchAd(): Promise<{ canWatch: boolean }> {
    return await HttpUtil.get<{ canWatch: boolean }>('/api/user/can-watch-ad');
  }
}
