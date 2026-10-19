import { useState, useCallback, useEffect } from 'react';
import { api } from '../services/api';
import type { ShortenUrlResponse, UrlListItem } from '../types/api';

interface UseUrlShortenerReturn {
  urls: UrlListItem[];
  page: number;
  pageSize: number;
  total: number;
  loading: boolean;
  error: string | null;
  lastCreated: ShortenUrlResponse | null;
  shorten: (fullUrl: string, customAlias?: string) => Promise<void>;
  deleteUrl: (alias: string) => Promise<void>;
  clearLastCreated: () => void;
  fetchAll: (page?: number) => Promise<void>;
}

export function useUrlShortener(): UseUrlShortenerReturn {
  const [urls, setUrls] = useState<UrlListItem[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastCreated, setLastCreated] = useState<ShortenUrlResponse | null>(null);

  const fetchAll = useCallback(async (requestedPage = 0) => {
    setLoading(true);
    setError(null);
    try {
      let result = await api.listAll(requestedPage, pageSize);
      if (requestedPage > 0 && result.items.length === 0 && result.total <= requestedPage * result.size) {
        result = await api.listAll(requestedPage - 1, result.size);
      }
      setUrls(result.items);
      setPage(result.page);
      setPageSize(result.size);
      setTotal(result.total);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load URLs.');
    } finally {
      setLoading(false);
    }
  }, [pageSize]);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const shorten = useCallback(async (fullUrl: string, customAlias?: string) => {
    setError(null);
    setLastCreated(null);
    try {
      const result = await api.shorten({ fullUrl, customAlias: customAlias || undefined });
      setLastCreated(result);
      // Refresh the list
      await fetchAll(0);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to shorten URL.');
    }
  }, [fetchAll]);

  const deleteUrl = useCallback(async (alias: string) => {
    setError(null);
    try {
      await api.delete(alias);
      await fetchAll(page);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Delete or Fetch operation failed.');
    }
  }, [fetchAll, page]);

  const clearLastCreated = useCallback(() => setLastCreated(null), []);

  return { urls, page, pageSize, total, loading, error, lastCreated, shorten, deleteUrl, clearLastCreated, fetchAll };
}
