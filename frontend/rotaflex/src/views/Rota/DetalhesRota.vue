<template lang="pug">
div.app-container
  header.app-header
    h1 Rota e Abastecimento

  main.app-main
    form.route-form(@submit.prevent="calcularRota")
      .form-group
        label(for="origem") Origem
        input#origem(
          type="text"
          v-model="origemText"
          autocomplete="off"
          placeholder="Digite o endereço de origem"
          @input="onOrigemInput"
          @focus="origemFocused = true"
          @blur="onOrigemBlur"
        )
        ul.suggestions(v-if="origemFocused && origemSugestoes.length")
          li(
            v-for="(sug, i) in origemSugestoes"
            :key="i"
            @mousedown.prevent="selectOrigem(sug)"
          )
            | {{ formatSuggestion(sug) }}

      .form-group
        label(for="destino") Destino
        input#destino(
          type="text"
          v-model="destinoText"
          autocomplete="off"
          placeholder="Digite o endereço de destino"
          @input="onDestinoInput"
          @focus="destinoFocused = true"
          @blur="onDestinoBlur"
        )
        ul.suggestions(v-if="destinoFocused && destinoSugestoes.length")
          li(
            v-for="(sug, i) in destinoSugestoes"
            :key="i"
            @mousedown.prevent="selectDestino(sug)"
          )
            | {{ formatSuggestion(sug) }}

      .form-group
        label(for="tipoVeiculo") Tipo de Transporte
        select#tipoVeiculo.form-control(v-model="tipoVeiculo")
          option(value="carro") Carro (até 1 tonelada)
          option(value="caminhao_pequeno") Caminhão Pequeno (1 a 3 toneladas)
          option(value="caminhao_medio") Caminhão Médio (3 a 7 toneladas)
          option(value="onibus") Ônibus
          option(value="a_pe") A Pé


      p.velocidade-info(v-if="velocidadeMedia > 0")
        | Velocidade média estimada: {{ velocidadeMedia.toFixed(1) }} km/h
      p.velocidade-info(v-else)
        | Velocidade média estimada indisponível.



      .form-group.inline-inputs(v-if="tipoVeiculo !== 'a_pe'")
        label(for="kmPorLitro") Consumo do veículo (km/litro)
        input#kmPorLitro(
          type="number"
          v-model.number="kmPorLitro"
          min="0.1"
          step="0.1"
          required
          placeholder="Ex: 12.5"
        )

        label(for="precoLitro") Preço do litro do combustível (R$)
        input#precoLitro(
          type="number"
          v-model.number="precoLitro"
          min="0"
          step="0.01"
          required
          placeholder="Ex: 4.59"
        )


      button(type="submit" class="btn-primary") Calcular Rota


    #map

    section.route-info(v-if="rotaInfo")
      h2 Informações da Rota
      p
        strong Distância:
        |  {{ (rotaInfo.distance / 1000).toFixed(2) }} km
      p
        strong Tempo estimado:
        |  {{ formatarTempoEstimado(tempoEstimado) }}
      p(v-if="tipoVeiculo !== 'a_pe'")
        strong Consumo estimado:
        |  {{ consumoLitros.toFixed(2) }} litros
      p(v-if="tipoVeiculo !== 'a_pe'")
        strong Custo estimado:
        |  R$ {{ custoLitros.toFixed(2) }}
      p(v-else)
        em Você escolheu ir a pé, sem custo de combustível.


    button.btn-primary(v-if="rotaInfo && steps.length" @click="iniciarNavegacao") Iniciar Navegação
    section.directions(v-if="navegando")
      h3 Instruções de Navegação
      ol
        li(
          v-for="(step, i) in steps"
          :key="i"
          :class="{ 'passo-atual': i === passoAtual }"
        )
          | {{ step.instruction }} — {{ (step.distance / 1000).toFixed(2) }} km

    button.btn-warning(v-if="navegando" @click="pararGeolocalizacao") Parar Navegação

</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed, watch } from 'vue'
import L, { Map as LeafletMap, GeoJSON } from 'leaflet'
import 'leaflet/dist/leaflet.css'

interface Suggestion {
  display_name: string
  lat: string
  lon: string
  address?: {
    road?: string
    pedestrian?: string
    footway?: string
    house_number?: string
    suburb?: string
    neighbourhood?: string
    city_district?: string
    city?: string
    town?: string
    village?: string
    state?: string
    country?: string
  }
}
interface RouteInfo {
  distance: number
  duration: number
  geometry: any
}

export default defineComponent({
  name: 'RotaAutocomplete',
  setup() {
    const origemText = ref('')
    const destinoText = ref('')
    const kmPorLitro = ref(10)
    const precoLitro = ref(5)
    const rotaInfo = ref<RouteInfo | null>(null)
    const steps = ref<any[]>([])
    const userPosition = ref<[number, number] | null>(null)
    const watchId = ref<number | null>(null)
    const tipoVeiculo = ref('carro')

    const origemSugestoes = ref<Suggestion[]>([])
    const destinoSugestoes = ref<Suggestion[]>([])

    const origemFocused = ref(false)
    const destinoFocused = ref(false)

    let origemTimeout: ReturnType<typeof setTimeout>
    let destinoTimeout: ReturnType<typeof setTimeout>

    let map: LeafletMap
    let rotaLayer: GeoJSON | null = null
    let userMarker: L.Marker | null = null
    const velocidadesMedias: Record<string, number> = {
      a_pe: 2, // 5 km/h para a pé
      carro: 30, // 40 km/h carro
      caminhao_pequeno: 20, // 30 km/h caminhão pequeno
      caminhao_medio: 15, // 25 km/h caminhão médio
      onibus: 20, // 20 km/h ônibus
    }
    const velocidadeMedia = computed(() => {
      return velocidadesMedias[tipoVeiculo.value] ?? 0 // se não encontrar, retorna 0
    })

    onMounted(() => {
      map = L.map('map').setView([-23.5505, -46.6333], 13)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
      }).addTo(map)
    })
    const navegando = ref(false)
    function atualizarPosicaoNoMapa() {
      if (!userPosition.value || !map) return

      const latlng = L.latLng(userPosition.value[0], userPosition.value[1])
      if (!userMarker) {
        userMarker = L.marker(latlng, {
          icon: L.icon({
            iconUrl: 'https://cdn-icons-png.flaticon.com/512/149/149059.png', // ícone de pessoa
            iconSize: [30, 30],
            iconAnchor: [15, 15],
          }),
        }).addTo(map)
      } else {
        userMarker.setLatLng(latlng)
      }

      // Opcional: centralizar o mapa na posição atual
      map.panTo(latlng)
    }
    watch(tipoVeiculo, (novoTipo) => {
      if (origemText.value && destinoText.value) {
        calcularRota()
      }
    })
    const passoAtual = ref(0)

    function atualizarPassoAtual() {
      if (!userPosition.value || steps.value.length === 0) return

      const userLatLng = L.latLng(userPosition.value[0], userPosition.value[1])

      for (let i = passoAtual.value; i < steps.value.length; i++) {
        const step = steps.value[i]
        const stepLatLng = L.latLng(getStepLatLng(step))

        const distance = userLatLng.distanceTo(stepLatLng)

        if (distance < 30) {
          passoAtual.value = i + 1
        }
      }
    }
    function formatarTempoEstimado(minutosTotais: number): string {
      const minutos = Math.floor(minutosTotais)
      const segundos = Math.round((minutosTotais - minutos) * 60)
      return `${minutos}m ${segundos}s`
    }
    function getStepLatLng(step: any): [number, number] {
      const coord = step?.geometry?.coordinates?.[0]
      if (!coord) throw new Error('Coordenada do passo inválida')
      return [coord[1], coord[0]] // invertido porque Leaflet usa [lat, lon]
    }
    function iniciarNavegacao() {
      navegando.value = true
      iniciarGeolocalizacao()
      alert('Navegação iniciada! Siga as instruções no painel abaixo.')
    }

    function iniciarGeolocalizacao() {
      if (!navigator.geolocation) {
        alert('Geolocalização não suportada pelo navegador.')
        return
      }

      watchId.value = navigator.geolocation.watchPosition(
        (pos) => {
          const lat = pos.coords.latitude
          const lon = pos.coords.longitude

          userPosition.value = [lat, lon] // <- ATUALIZA posição global
          atualizarPosicaoNoMapa()
          atualizarPassoAtual() // <- Atualiza passo com base na posição atual
        },
        (err) => {
          console.error('Erro na geolocalização:', err)
        },
        { enableHighAccuracy: true, maximumAge: 1000 },
      )
    }

    function pararGeolocalizacao() {
      if (watchId.value !== null) {
        navigator.geolocation.clearWatch(watchId.value)
        watchId.value = null
      }
    }

    // Função para buscar sugestões no Nominatim
    async function buscarSugestoes(query: string): Promise<Suggestion[]> {
      if (!query) return []
      const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
        query,
      )}&format=json&addressdetails=1&limit=5`
      const res = await fetch(url)
      const data = await res.json()
      return data as Suggestion[]
    }

    // Autocomplete Origem
    function onOrigemInput() {
      clearTimeout(origemTimeout)
      origemTimeout = setTimeout(async () => {
        origemSugestoes.value = await buscarSugestoes(origemText.value)
      }, 300)
    }
    function selectOrigem(sug: Suggestion) {
      origemText.value = formatSuggestion(sug)
      origemSugestoes.value = []
      origemFocused.value = false
    }
    function onOrigemBlur() {
      setTimeout(() => {
        origemFocused.value = false
      }, 200)
    }
    function formatSuggestion(sug: Suggestion): string {
      if (!sug.address) {
        // Se não tem endereço, retorna display_name mesmo
        return sug.display_name || 'Endereço desconhecido'
      }

      const addr = sug.address

      // Pega os campos mais comuns
      const rua = addr.road || addr.pedestrian || addr.footway || ''
      const numero = addr.house_number || ''
      const bairro = addr.suburb || addr.neighbourhood || addr.city_district || ''
      const cidade = addr.city || addr.town || addr.village || ''
      const estado = addr.state || ''
      const pais = addr.country || ''

      // Monta as partes só se não forem vazias
      const partes: string[] = []

      if (rua) {
        partes.push(rua + (numero ? `, ${numero}` : ''))
      }

      if (bairro || cidade) {
        const bairroCidade = [bairro, cidade].filter(Boolean).join(', ')
        partes.push(bairroCidade)
      }

      if (estado || pais) {
        const estadoPais = [estado, pais].filter(Boolean).join(' – ')
        partes.push(estadoPais)
      }

      // Se nada montou, retorna display_name
      if (partes.length === 0) {
        return sug.display_name || 'Endereço desconhecido'
      }

      return partes.join(' – ')
    }

    // Autocomplete Destino
    function onDestinoInput() {
      clearTimeout(destinoTimeout)
      destinoTimeout = setTimeout(async () => {
        destinoSugestoes.value = await buscarSugestoes(destinoText.value)
      }, 300)
    }
    function selectDestino(sug: Suggestion) {
      destinoText.value = formatSuggestion(sug)
      destinoSugestoes.value = []
      destinoFocused.value = false
    }
    function onDestinoBlur() {
      setTimeout(() => {
        destinoFocused.value = false
      }, 200)
    }

    // Geocodificar um endereço para coordenadas [lon, lat]
    async function geocode(address: string): Promise<[number, number]> {
      const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
        address,
      )}&format=json&limit=1`
      const res = await fetch(url)
      const data = await res.json()
      if (data.length === 0) throw new Error(`Endereço não encontrado: ${address}`)
      return [parseFloat(data[0].lon), parseFloat(data[0].lat)]
    }

    // Formatar tempo em segundos para string legível
    function formatDuration(seconds: number): string {
      const hrs = Math.floor(seconds / 3600)
      const mins = Math.floor((seconds % 3600) / 60)
      const segs = Math.floor(seconds % 60)
      return `${hrs > 0 ? hrs + 'h ' : ''}${mins}m ${segs}s`
    }

    // Chamar OpenRouteService para calcular a rota
    async function calcularRota() {
      if (!origemText.value || !destinoText.value) {
        alert('Informe origem e destino')
        return
      }

      try {
        const origemCoords = await geocode(origemText.value)
        const destinoCoords = await geocode(destinoText.value)

        if (rotaLayer) {
          rotaLayer.remove()
          rotaLayer = null
        }

        // Coloque sua chave API da OpenRouteService aqui:
        const apiKey = '5b3ce3597851110001cf6248d4d4bd629ef345799f58075047de5a69'

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
          alert('Não foi possível calcular a rota')
          return
        }

        const feature = data.features[0]
        steps.value = (feature.properties.segments[0].steps || []).map((s: any) => ({
          ...s,
          instruction: traduzirInstrucao(s.instruction),
        }))

        rotaInfo.value = {
          distance: feature.properties.summary.distance,
          duration: feature.properties.summary.duration,
          geometry: feature.geometry,
        }

        rotaLayer = L.geoJSON(data).addTo(map)
        map.fitBounds(rotaLayer.getBounds())
      } catch (error: any) {
        alert(error.message || 'Erro ao calcular rota')
      }
    }
    function traduzirInstrucao(instrucao: string): string {
      // Mapeamento de direções
      const direcoes: Record<string, string> = {
        north: 'norte',
        south: 'sul',
        east: 'leste',
        west: 'oeste',
        northeast: 'nordeste',
        southeast: 'sudeste',
        northwest: 'noroeste',
        southwest: 'sudoeste',
      }

      return (
        instrucao
          // Traduz "Head direction" → "Siga em frente (direção traduzida)"
          .replace(/Head (\w+)/i, (_, dir) => {
            const direcaoTraduzida = direcoes[dir.toLowerCase()] || dir
            return `Siga em frente (${direcaoTraduzida})`
          })

          // Traduções diretas
          .replace(/Turn left/i, 'Vire à esquerda')
          .replace(/Turn right/i, 'Vire à direita')
          .replace(/Continue/i, 'Continue em frente')
          .replace(/Arrive.*/i, 'Você chegou ao destino')

          // Troca preposições
          .replace(/ on /i, ' na ')
          .replace(/ onto /i, ' na ')

          // Traduz direções restantes entre parênteses, ex: (southwest)
          .replace(
            /\((north|south|east|west|northeast|southeast|northwest|southwest)\)/gi,
            (match, dir) => {
              return `(${direcoes[dir.toLowerCase()] || dir})`
            },
          )
      )
    }
    const fatorConsumo = computed(() => {
      switch (tipoVeiculo.value) {
        case 'a_pe':
          return 0 // a pé não consome combustível
        case 'carro':
          return 1 // base para carro
        case 'caminhao_pequeno':
          return 2 // digamos o dobro do consumo do carro
        case 'caminhao_medio':
          return 3.5 // mais pesado, mais consumo
        case 'onibus':
          return 4 // mais pesado ainda
        default:
          return 1
      }
    })
    const distancia = computed(() => {
      return rotaInfo.value ? rotaInfo.value.distance / 1000 : 0 // km
    })

    const tempoEstimado = computed(() => {
      const distKm = distancia.value
      const vel = velocidadeMedia.value

      if (vel === 0) return 0 // evita divisão por zero, só no caso

      return (distKm / vel) * 60 // tempo em minutos
    })

    const consumoLitros = computed(() => {
      if (!rotaInfo.value) return 0
      if (tipoVeiculo.value === 'a_pe') return 0
      return (rotaInfo.value.distance / 1000 / kmPorLitro.value) * fatorConsumo.value
    })

    const custoLitros = computed(() => consumoLitros.value * precoLitro.value)

    return {
      origemText,
      destinoText,
      kmPorLitro,
      precoLitro,
      rotaInfo,
      origemSugestoes,
      destinoSugestoes,
      origemFocused,
      destinoFocused,
      onOrigemInput,
      selectOrigem,
      onOrigemBlur,
      onDestinoInput,
      selectDestino,
      onDestinoBlur,
      calcularRota,
      formatDuration,
      tipoVeiculo,
      consumoLitros,
      custoLitros,
      formatSuggestion,
      steps,
      navegando,
      iniciarNavegacao,
      passoAtual,
      getStepLatLng,
      pararGeolocalizacao,
      velocidadeMedia,
      tempoEstimado,
      formatarTempoEstimado,
      // se quiser usar externamente
    }
  },
})
</script>

<style scoped>
/* Reset básico */
* {
  box-sizing: border-box;
}

.app-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  color: #222;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 1rem;
}

.app-header {
  margin-bottom: 1rem;
  text-align: center;
  color: #0d3b66;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

h1 {
  margin: 0;
  font-weight: 700;
}

.app-main {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 1000px;
  width: 100%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.route-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  position: relative;
  display: flex;
  flex-direction: column;
}

.inline-inputs {
  flex-direction: row;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.inline-inputs label,
.inline-inputs input {
  flex: 1 1 45%;
}

label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #344055;
}

input[type='text'],
input[type='number'] {
  padding: 0.5rem 0.75rem;
  font-size: 1rem;
  border: 2px solid #a3bffa;
  border-radius: 8px;
  transition: border-color 0.3s;
}

input[type='text']:focus,
input[type='number']:focus {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 6px #3b82f6aa;
}

button.btn-primary {
  display: block; /* faz o botão ser bloco */
  margin: 1.5rem auto 0 auto; /* 1.5rem em cima, auto centraliza horizontalmente */
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  user-select: none;
  align-items: center;
}

button.btn-primary:hover {
  background-color: #2563eb;
}

#map {
  height: 400px;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.suggestions {
  position: absolute;
  background: white;
  width: 100%;
  max-height: 160px;
  overflow-y: auto;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  margin-top: 2px;
  padding: 0;
  list-style: none;
  z-index: 10;
}

.suggestions li {
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  color: #334155;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.suggestions li:hover {
  background-color: #e0e7ff;
}

.route-info {
  background: #f0f4ff;
  border-radius: 10px;
  padding: 1rem 1.5rem;
  color: #1e293b;
  box-shadow: inset 0 0 8px #cbd5e1;
}

.route-info h2 {
  margin-top: 0;
  margin-bottom: 0.8rem;
  font-weight: 700;
  color: #2563eb;
}

.route-info p {
  margin: 0.3rem 0;
  font-size: 1rem;
}

.form-group {
  position: relative;
  margin-bottom: 1.5rem;
}

select#tipoVeiculo {
  padding: 0.5rem 0.75rem;
  font-size: 1rem;
  border: 2px solid #a3bffa;
  border-radius: 8px;
  transition: border-color 0.3s;
  font-family: inherit; /* Para manter a mesma fonte */
}

select#tipoVeiculo:focus {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 6px #3b82f6aa;
  background: white;
  color: #222;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

.suggestions {
  position: absolute;
  top: 100%; /* Fica logo abaixo do input */
  left: 0;
  right: 0;
  background: white;
  max-height: 180px;
  overflow-y: auto;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
  padding: 0;
  list-style: none;
  z-index: 1000;
  margin-top: 2px;
}

.suggestions {
  position: absolute;
  background: white;
  border: 1px solid #ccc;
  max-height: 150px;
  overflow-y: auto;
  width: 100%;
  z-index: 10;
  padding-left: 0;
  list-style: none;
  margin-top: 0;
}

.suggestions li {
  padding: 8px;
  cursor: pointer;
}

.suggestions li:hover {
  background-color: #f0f0f0;
}

.route-info {
  margin-top: 20px;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
}

button.btn-warning {
  display: block; /* faz o botão ser bloco */
  margin: 1.5rem auto 0 auto; /* 1.5rem em cima, auto centraliza horizontalmente */
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  user-select: none;
  align-items: center;
}

button.btn-warning:hover {
  background-color: #0056b3;
}

.directions {
  margin-top: 20px;
  background: #e6f0ff;
  padding: 15px;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
}

.directions ol {
  padding-left: 20px;
}

.directions li {
  margin-bottom: 8px;
}
.passo-atual {
  font-weight: bold;
  color: #007bff;
  background: #e0f0ff;
  padding: 5px;
  border-radius: 4px;
}
#map {
  height: 400px;
  width: 100%;
  margin-top: 1rem;
}
.velocidade-info {
  font-size: 0.9em;
  color: #555;
  margin-top: 5px;
}
</style>
