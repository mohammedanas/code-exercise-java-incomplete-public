export interface ShortenUrlRequest {
  fullUrl: string;
  customAlias?: string;
}

export interface ShortenUrlResponse {
  alias: string;
  fullUrl: string;
  shortUrl: string;
}

export interface UrlListItem {
  alias: string;
  fullUrl: string;
  shortUrl: string;
}

export interface PagedResult<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface ApiError {
  error: string;
}
