import { Navigate, Route, Routes } from 'react-router-dom'
import { GeneralInfoPage } from './pages/GeneralInfoPage'
import { LocationDetailPage } from './pages/LocationDetailPage'
import { LocationsPage } from './pages/LocationsPage'
import { LocationsLayoutPage } from './pages/LocationsLayoutPage'
import { FolioPage } from './pages/FolioPage'
import { QuoteStatePage } from './pages/QuoteStatePage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/cotizador" replace />} />
      <Route path="/cotizador" element={<FolioPage />} />
      <Route path="/quotes/:folio/general-info" element={<GeneralInfoPage />} />
      <Route path="/quotes/:folio/locations" element={<LocationsPage />} />
      <Route path="/quotes/:folio/locations/:indice" element={<LocationDetailPage />} />
      <Route path="/quotes/:folio/locations/layout" element={<LocationsLayoutPage />} />
      <Route path="/quotes/:folio/state" element={<QuoteStatePage />} />
      <Route path="*" element={<Navigate to="/cotizador" replace />} />
    </Routes>
  )
}

export default App
