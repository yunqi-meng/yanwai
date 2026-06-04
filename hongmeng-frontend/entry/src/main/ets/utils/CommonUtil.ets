import preferences from '@ohos.data.preferences';
import common from '@ohos.app.ability.common';

export interface DecodeResult {
  text: string;
  analysis: any;
  newCard?: any;
  newAchievements?: any[];
  originalImage?: string;
}

export class CommonUtil {
  private static context: common.Context;
  private static currentDecodeResult: DecodeResult | null = null;

  static init(ctx: common.Context) {
    this.context = ctx;
  }

  static setDecodeResult(result: DecodeResult) {
    this.currentDecodeResult = result;
  }

  static getDecodeResult(): DecodeResult | null {
    return this.currentDecodeResult;
  }

  static clearDecodeResult() {
    this.currentDecodeResult = null;
  }

  static formatTime(timeStr: string): string {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    
    return date.toLocaleDateString('zh-CN');
  }

  static validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  static validatePassword(password: string): { valid: boolean; message: string } {
    if (password.length < 6) {
      return { valid: false, message: '密码长度至少6位' };
    }
    if (!/[a-zA-Z]/.test(password)) {
      return { valid: false, message: '密码需包含字母' };
    }
    if (!/[0-9]/.test(password)) {
      return { valid: false, message: '密码需包含数字' };
    }
    return { valid: true, message: '' };
  }
}
