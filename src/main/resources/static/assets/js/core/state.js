/**
 * Shared UI state for paging and filters (page, size, account/category/month filters).
 * This is the single source of truth for list view state.
 */

export const state = { page: 0, size: 20, acc: '', cat: '', yr: '', mo: '' };

export function initPageSizeFromStorage() {
  state.size = parseInt(localStorage.getItem('size') || '20', 10) || 20;
}
