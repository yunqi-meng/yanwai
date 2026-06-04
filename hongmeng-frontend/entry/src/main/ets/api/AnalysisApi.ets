import { HttpUtil } from '../utils/HttpUtil';

export interface DecodeRequest {
  text: string;
}

export interface DecodeResponse {
  analysis: any;
  newCard?: any;
  newAchievements?: any[];
}

export interface HistoryItem {
  id: number;
  originalText: string;
  analysisResult?: string;
  relationship?: string;
  createdAt: string;
}

export interface HistoryListResponse {
  records: HistoryItem[];
  total: number;
}

export class AnalysisApi {
  static async decode(text: string): Promise<DecodeResponse> {
    return await HttpUtil.post<DecodeResponse>('/api/analysis/decode', { text });
  }

  static async decodeWithImage(base64Image: string, text?: string): Promise<DecodeResponse> {
    return await HttpUtil.post<DecodeResponse>('/api/analysis/decode-with-image', { 
      base64Image,
      text
    });
  }

  static async share(): Promise<any> {
    return await HttpUtil.post<any>('/api/analysis/share');
  }

  static async getHistory(page: number = 1, pageSize: number = 10): Promise<HistoryListResponse> {
    return await HttpUtil.get<HistoryListResponse>('/api/analysis/history', { page, pageSize });
  }

  static async deleteHistory(id: number): Promise<void> {
    return await HttpUtil.delete<void>(`/api/analysis/history/${id}`);
  }

  static async getHistoryDetail(id: number): Promise<HistoryItem> {
    return await HttpUtil.get<HistoryItem>(`/api/analysis/history/${id}`);
  }
}
