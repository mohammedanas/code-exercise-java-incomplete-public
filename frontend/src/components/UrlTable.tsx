import type { UrlListItem } from '../types/api';

interface UrlTableProps {
  urls: UrlListItem[];
  page: number;
  pageSize: number;
  total: number;
  onPageChange: (page: number) => void;
  onDelete: (alias: string) => void;
}

export function UrlTable({ urls, page, pageSize, total, onPageChange, onDelete }: UrlTableProps) {
  if (urls.length === 0) {
    return (
      <div className="empty-state">
        <p>No shortened URLs yet.</p>
      </div>
    );
  }

  const pageCount = Math.ceil(total / pageSize);
  const startIndex = page * pageSize;

  return (
    <div>
      <div className="url-table-wrapper">
      <table className="url-table">
        <thead>
          <tr>
            <th scope="col">Short URL</th>
            <th scope="col">Original URL</th>
            <th scope="col" className="actions-cell">Actions</th>
          </tr>
        </thead>
        <tbody>
          {urls.map(({ alias, fullUrl, shortUrl }) => (
            <tr key={alias}>
              <td>
                <a className="short-url-link" href={shortUrl} target="_blank" rel="noreferrer">
                  {shortUrl}
                </a>
              </td>
              <td>
                <span className="full-url" title={fullUrl}>{fullUrl}</span>
              </td>
              <td className="actions-cell">
                <button type="button" onClick={() => onDelete(alias)} aria-label={`Delete ${alias}`}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>
      {total > pageSize && (
        <nav className="url-pagination" aria-label="URL list pages">
          <span aria-live="polite">
            Showing {startIndex + 1}-{Math.min(startIndex + urls.length, total)} of {total}
          </span>
          <div className="url-pagination__controls">
            <button
              type="button"
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
              aria-label="Previous page"
            >
              Previous
            </button>
            <span>Page {page + 1} of {pageCount}</span>
            <button
              type="button"
              onClick={() => onPageChange(page + 1)}
              disabled={page === pageCount - 1}
              aria-label="Next page"
            >
              Next
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}
