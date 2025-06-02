// src/services/routeService.ts

export interface RouteInfo {
  distance: number
  duration: number
  geometry: any
}

// Geocodifica um endereço usando Nominatim (OpenStreetMap)
export async function geocode(address: string): Promise<[number, number]> {
  const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
    address,
  )}&format=json&limit=1`
  const res = await fetch(url)
  const data = await res.json()
  if (data.length === 0) throw new Error(`Endereço não encontrado: ${address}`)
  return [parseFloat(data[0].lon), parseFloat(data[0].lat)]
}

// Chama OpenRouteService para calcular a rota entre duas coordenadas
export async function calcularRotaAPI(
  origemCoords: [number, number],
  destinoCoords: [number, number],
  apiKey: string,
): Promise<RouteInfo> {
  const response = await fetch(
    'https://api.openrouteservice.org/v2/directions/driving-car/geojson',
    {
      method: 'POST',
      headers: {
        Authorization: apiKey,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        coordinates: [origemCoords, destinoCoords],
      }),
    },
  )

  const data = await response.json()
  if (!data.features || data.features.length === 0) {
    throw new Error('Não foi possível calcular a rota')
  }

  const feature = data.features[0]
  return {
    distance: feature.properties.summary.distance,
    duration: feature.properties.summary.duration,
    geometry: feature.geometry,
  }
}
