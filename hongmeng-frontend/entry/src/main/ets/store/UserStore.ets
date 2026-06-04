import { UserStats, UserApi } from '../api/UserApi';
import { HttpUtil } from '../utils/HttpUtil';
import preferences from '@ohos.data.preferences';
import common from '@ohos.app.ability.common';

export class UserStore {
  private static instance: UserStore;
  private context: common.Context | null = null;
  
  userId: number | null = null;
  nickname: string = '';
  memberLevel: number = 0;
  dailyAnalysisCount: number = 0;
  totalAnalysis: number = 0;
  totalShare: number = 0;
  lateNightCount: number = 0;
  workplaceCount: number = 0;
  romanceCount: number = 0;
  memberExpireTime: string | null = null;
  canWatchAd: boolean = true;

  private constructor() {}

  static getInstance(): UserStore {
    if (!UserStore.instance) {
      UserStore.instance = new UserStore();
    }
    return UserStore.instance;
  }

  init(ctx: common.Context) {
    this.context = ctx;
  }

  get isLoggedIn(): boolean {
    return this.userId !== null;
  }

  get remainingAnalysis(): number {
    const limit = this.memberLevel === 1 ? 10 : 3;
    return Math.max(0, limit - this.dailyAnalysisCount);
  }

  get dailyLimit(): number {
    return this.memberLevel === 1 ? 10 : 3;
  }

  private updateFromStats(stats: UserStats) {
    this.userId = stats.userId;
    this.nickname = stats.nickname;
    this.memberLevel = stats.memberLevel;
    this.dailyAnalysisCount = stats.dailyAnalysisCount;
    this.totalAnalysis = stats.totalAnalysis;
    this.totalShare = stats.totalShare;
    this.lateNightCount = stats.lateNightCount;
    this.workplaceCount = stats.workplaceCount;
    this.romanceCount = stats.romanceCount;
    this.memberExpireTime = stats.memberExpireTime || null;
    this.canWatchAd = stats.canWatchAd;
  }

  async login(email: string, password: string): Promise<boolean> {
    try {
      const stats = await UserApi.login(email, password);
      this.updateFromStats(stats);
      await HttpUtil.setUserId(stats.userId);
      return true;
    } catch (e) {
      console.error('Login failed:', JSON.stringify(e));
      return false;
    }
  }

  async register(email: string, password: string): Promise<boolean> {
    try {
      const stats = await UserApi.register(email, password);
      this.updateFromStats(stats);
      await HttpUtil.setUserId(stats.userId);
      return true;
    } catch (e) {
      console.error('Register failed:', JSON.stringify(e));
      return false;
    }
  }

  async guestLogin(): Promise<boolean> {
    try {
      let deviceId = '';
      if (this.context) {
        try {
          const prefs = await preferences.getPreferences(this.context, 'YanwaiPreferences');
          const savedDeviceId = prefs.getSync('deviceId', '') as string;
          if (savedDeviceId) {
            deviceId = savedDeviceId;
          } else {
            deviceId = 'device_' + Date.now();
            await prefs.put('deviceId', deviceId);
            await prefs.flush();
          }
        } catch (e) {
          deviceId = 'device_' + Date.now();
        }
      } else {
        deviceId = 'device_' + Date.now();
      }

      const stats = await UserApi.guestLogin(deviceId);
      this.updateFromStats(stats);
      await HttpUtil.setUserId(stats.userId);
      return true;
    } catch (e) {
      console.error('Guest login failed:', JSON.stringify(e));
      return false;
    }
  }

  async fetchStats(): Promise<void> {
    if (!this.userId) return;
    try {
      const stats = await UserApi.getStats();
      this.updateFromStats(stats);
    } catch (e) {
      console.error('Fetch stats failed:', JSON.stringify(e));
    }
  }

  async resetData(): Promise<void> {
    if (!this.userId) return;
    try {
      await UserApi.reset();
      await this.fetchStats();
    } catch (e) {
      console.error('Reset failed:', JSON.stringify(e));
    }
  }

  incrementDailyCount() {
    this.dailyAnalysisCount++;
    this.totalAnalysis++;
  }

  async watchAdComplete(): Promise<boolean> {
    if (!this.userId) return false;
    try {
      const stats = await UserApi.watchAdComplete();
      this.updateFromStats(stats);
      return true;
    } catch (e) {
      console.error('Watch ad failed:', JSON.stringify(e));
      return false;
    }
  }

  async checkCanWatchAd(): Promise<boolean> {
    if (!this.userId) return false;
    try {
      const result = await UserApi.checkCanWatchAd();
      this.canWatchAd = result.canWatch;
      return result.canWatch;
    } catch (e) {
      console.error('Check can watch ad failed:', JSON.stringify(e));
      return false;
    }
  }

  async updateInfo(newNickname: string): Promise<boolean> {
    if (!this.userId) return false;
    try {
      await UserApi.updateInfo(newNickname);
      this.nickname = newNickname;
      return true;
    } catch (e) {
      console.error('Update info failed:', JSON.stringify(e));
      return false;
    }
  }

  async logout(): Promise<void> {
    this.userId = null;
    this.nickname = '';
    this.memberLevel = 0;
    this.dailyAnalysisCount = 0;
    this.totalAnalysis = 0;
    this.totalShare = 0;
    this.lateNightCount = 0;
    this.workplaceCount = 0;
    this.romanceCount = 0;
    this.memberExpireTime = null;
    this.canWatchAd = true;
    await HttpUtil.clearUserId();
    if (this.context) {
      try {
        const prefs = await preferences.getPreferences(this.context, 'YanwaiPreferences');
        await prefs.delete('deviceId');
        await prefs.flush();
      } catch (e) {
        console.error('Clear deviceId failed:', JSON.stringify(e));
      }
    }
  }
}
