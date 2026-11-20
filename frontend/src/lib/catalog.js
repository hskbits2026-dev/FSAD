// Shared visuals for product categories: a gradient for the placeholder image,
// a human label, an initial for the tile, and a solid pill colour (fg/bg) so each
// category is visually distinct in tables/badges. Keeps all surfaces consistent.
const MAP = {
  ELECTRONICS: { label: 'Electronics', grad: 'linear-gradient(145deg,#8aa0b8,#4a5e74)', pill: { fg: '#3f5876', bg: '#e4ebf2' } },
  FURNITURE: { label: 'Furniture', grad: 'linear-gradient(145deg,#c08a6a,#7a4c2e)', pill: { fg: '#8a4f2e', bg: '#f3e3d8' } },
  COSMETICS: { label: 'Cosmetics', grad: 'linear-gradient(145deg,#e0aeb7,#b06678)', pill: { fg: '#a85168', bg: '#f7e3e8' } },
  TOYS_AND_BOOKS: { label: 'Toys & Books', grad: 'linear-gradient(145deg,#a6c08f,#687f49)', pill: { fg: '#5f7a3e', bg: '#e9f0dd' } },
  SPORTS_EQUIPMENT: { label: 'Sports Equipment', grad: 'linear-gradient(145deg,#7ec8c3,#3c8a84)', pill: { fg: '#2f7a74', bg: '#dcefed' } },
}
const FALLBACK = { label: 'Other', grad: 'linear-gradient(145deg,#bcb6c8,#6a6480)', pill: { fg: '#6a6480', bg: '#ece9f0' } }

export const CATEGORIES = Object.keys(MAP)

export function categoryInfo(category) {
  return MAP[category] || FALLBACK
}

export function categoryLabel(category) {
  return categoryInfo(category).label
}

export function productInitial(name = '?') {
  return name.trim().charAt(0).toUpperCase() || '?'
}

export function money(amount) {
  return `₹${Number(amount).toFixed(2)}`
}
