import { HttpUtil } from '../utils/HttpUtil';

export interface PersonalityCardDef {
  id: number;
  name: string;
  description: string;
  rarity: number;
  emoji?: string;
  personalityType?: string;
  trait?: string;
}

export interface UserCard {
  id: number;
  cardId: number;
  name: string;
  rarity: number;
  emoji?: string;
  description: string;
  quantity: number;
  isNew?: boolean;
}

export class CardsApi {
  static async getDefinitions(): Promise<PersonalityCardDef[]> {
    return await HttpUtil.get<PersonalityCardDef[]>('/api/cards/definitions');
  }

  static async getUserCards(): Promise<UserCard[]> {
    return await HttpUtil.get<UserCard[]>('/api/cards/user');
  }

}
