export interface Suggestion {
  display_name: string
  lat: string
  lon: string
}

export async function buscarSugestoes(query: string): Promise<Suggestion[]> {
  if (!query) return []
  const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=5`
  const res = await fetch(url)
  const data = await res.json()
  return data as Suggestion[]
}
