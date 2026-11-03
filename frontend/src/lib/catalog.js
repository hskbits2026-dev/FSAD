// Shared visuals for product categories: a gradient for the placeholder image,
// a human label, and an initial for the tile. Keeps cards/detail/cart consistent.
const MAP = {
  ELECTRONICS: { label: 'Electronics', grad: 'linear-gradient(145deg,#8aa0b8,#4a5e74)' },
  ELECTRICAL: { label: 'Electrical', grad: 'linear-gradient(145deg,#e9cd90,#c69a53)' },
  FURNITURE: { label: 'Furniture', grad: 'linear-gradient(145deg,#c08a6a,#7a4c2e)' },
  COSMETICS: { label: 'Cosmetics', grad: 'linear-gradient(145deg,#e0aeb7,#b06678)' },
  TOYS_AND_BOOKS: { label: 'Toys & Books', grad: 'linear-gradient(145deg,#a6c08f,#687f49)' },
}
const FALLBACK = { label: 'Other', grad: 'linear-gradient(145deg,#bcb6c8,#6a6480)' }

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
  return `$${Number(amount).toFixed(2)}`
}
