import http from '@ohos.net.http';
import preferences from '@ohos.data.preferences';
import common from '@ohos.app.ability.common';
import picker from '@ohos.file.picker';
import fs from '@ohos.file.fs';
import util from '@ohos.util';

const BASE_URL = 'http://10.0.2.2:8080';
const TIMEOUT = 180000;

export class HttpUtil {
  private static context: common.Context;
  private static userId: number | null = null;

  static init(ctx: common.Context) {
    this.context = ctx;
    this.loadUserId();
  }

  private static async loadUserId() {
    try {
      const prefs = await preferences.getPreferences(this.context, 'YanwaiPreferences');
      this.userId = prefs.getSync('userId', null) as number | null;
    } catch (e) {
      console.error('Load userId failed:', JSON.stringify(e));
    }
  }

  static async setUserId(id: number) {
    this.userId = id;
    try {
      const prefs = await preferences.getPreferences(this.context, 'YanwaiPreferences');
      await prefs.put('userId', id);
      await prefs.flush();
    } catch (e) {
      console.error('Save userId failed:', JSON.stringify(e));
    }
  }

  static async clearUserId() {
    this.userId = null;
    try {
      const prefs = await preferences.getPreferences(this.context, 'YanwaiPreferences');
      await prefs.delete('userId');
      await prefs.flush();
    } catch (e) {
      console.error('Clear userId failed:', JSON.stringify(e));
    }
  }

  static getUserId(): number | null {
    return this.userId;
  }

  private static createRequest(): http.HttpRequest {
    return http.createHttp();
  }

  private static buildHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    if (this.userId) {
      headers['X-User-Id'] = this.userId.toString();
    }
    return headers;
  }

  static async get<T>(url: string, params?: Record<string, any>): Promise<T> {
    const httpRequest = this.createRequest();
    let fullUrl = `${BASE_URL}${url}`;
    
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');
      if (queryString) {
        fullUrl += `?${queryString}`;
      }
    }

    try {
      const response = await httpRequest.request(fullUrl, {
        method: http.RequestMethod.GET,
        header: this.buildHeaders(),
        readTimeout: TIMEOUT,
        connectTimeout: TIMEOUT
      });

      return this.handleResponse<T>(response);
    } finally {
      httpRequest.destroy();
    }
  }

  static async post<T>(url: string, data?: any): Promise<T> {
    const httpRequest = this.createRequest();

    try {
      const response = await httpRequest.request(`${BASE_URL}${url}`, {
        method: http.RequestMethod.POST,
        header: this.buildHeaders(),
        extraData: data ? JSON.stringify(data) : undefined,
        readTimeout: TIMEOUT,
        connectTimeout: TIMEOUT
      });

      return this.handleResponse<T>(response);
    } finally {
      httpRequest.destroy();
    }
  }

  static async delete<T>(url: string): Promise<T> {
    const httpRequest = this.createRequest();

    try {
      const response = await httpRequest.request(`${BASE_URL}${url}`, {
        method: http.RequestMethod.DELETE,
        header: this.buildHeaders(),
        readTimeout: TIMEOUT,
        connectTimeout: TIMEOUT
      });

      return this.handleResponse<T>(response);
    } finally {
      httpRequest.destroy();
    }
  }

  private static handleResponse<T>(response: http.HttpResponse): T {
    if (response.responseCode === 200) {
      const result = typeof response.result === 'string' 
        ? JSON.parse(response.result) 
        : response.result;
      
      if (result.code === 200) {
        return result.data as T;
      } else {
        throw new Error(result.message || '请求失败');
      }
    } else {
      throw new Error(`HTTP ${response.responseCode}: ${response.responseMessage}`);
    }
  }
}
