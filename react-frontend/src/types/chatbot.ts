export interface Message {
  content: string;
  isUser: boolean;
  meta: string;
}

export interface ApiResponse {
  response: string;
  cached: boolean;
  processing_time: number;
}

export interface ApiRequest {
  question: string;
  use_cache: boolean;
  n_results: number;
}
