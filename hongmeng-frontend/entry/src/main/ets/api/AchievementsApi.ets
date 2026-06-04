import { HttpUtil } from '../utils/HttpUtil';

export interface AchievementDTO {
  id: number;
  code: string;
  name: string;
  description: string;
  conditionField: string;
  conditionValue: number;
  rewardType?: string;
  rewardValue?: string;
  icon?: string;
  unlocked?: boolean;
  unlockedAt?: string;
}

export class AchievementsApi {
  static async getAll(): Promise<AchievementDTO[]> {
    return await HttpUtil.get<AchievementDTO[]>('/api/achievements/list');
  }

  static async getUser(): Promise<AchievementDTO[]> {
    return await HttpUtil.get<AchievementDTO[]>('/api/achievements/user');
  }
}
