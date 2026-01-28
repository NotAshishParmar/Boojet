export const state = { page: 0, size: 20, acc: '', cat: '', yr: '', mo: '' };

export function initPageSizeFromStorage() {
  state.size = parseInt(localStorage.getItem('size') || '20', 10) || 20;
}
