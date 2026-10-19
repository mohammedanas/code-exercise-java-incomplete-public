import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { UrlTable } from '../UrlTable';
import type { UrlListItem } from '../../types/api';

const mockUrls: UrlListItem[] = [
  { alias: 'abc', fullUrl: 'https://example.com', shortUrl: 'http://localhost:8080/abc' },
  { alias: 'def', fullUrl: 'https://another.com/path', shortUrl: 'http://localhost:8080/def' },
];

const defaultPaginationProps = {
  page: 0,
  pageSize: 5,
  total: mockUrls.length,
  onPageChange: vi.fn(),
};

describe('UrlTable', () => {
  it('shows empty state when there are no URLs', () => {
    render(<UrlTable urls={[]} {...defaultPaginationProps} onDelete={vi.fn()} />);
    expect(screen.getByText(/no shortened urls yet/i)).toBeInTheDocument();
  });

  it('renders a row for each URL', () => {
    render(<UrlTable urls={mockUrls} {...defaultPaginationProps} onDelete={vi.fn()} />);
    expect(screen.getByText('http://localhost:8080/abc')).toBeInTheDocument();
    expect(screen.getByText('http://localhost:8080/def')).toBeInTheDocument();
  });

  it('calls onDelete with the alias when delete is clicked', () => {
    const onDelete = vi.fn();
    render(<UrlTable urls={mockUrls} {...defaultPaginationProps} onDelete={onDelete} />);
    fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    expect(onDelete).toHaveBeenCalledWith('abc');
  });

  it('shows pagination and requests the next server page', () => {
    const onPageChange = vi.fn();
    const urls = Array.from({ length: 5 }, (_, index) => ({
      alias: `alias-${index}`,
      fullUrl: `https://example.com/${index}`,
      shortUrl: `http://localhost:8080/alias-${index}`,
    }));
    render(
      <UrlTable
        urls={urls}
        page={0}
        pageSize={5}
        total={8}
        onPageChange={onPageChange}
        onDelete={vi.fn()}
      />,
    );

    expect(screen.getByText('Showing 1-5 of 8')).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 2')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /next page/i }));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });
});
