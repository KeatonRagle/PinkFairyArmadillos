import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Navigate } from 'react-router-dom'
import Login from './pages/login.jsx'
import Signup from './pages/signup.jsx'
import Home from './pages/home.jsx'
import SelectAnimal from './pages/SelectAnimal.jsx'
import AnimalFilter from './pages/AnimalFilter.jsx'
import AboutUs from './pages/AboutUs.jsx'
import Help from './pages/help.jsx'
import DiscussionBoard from './pages/DiscussionBoard.jsx'
import ShelterInfo from './pages/ShelterInfo.jsx'
import Contribute from './pages/contribute.jsx'
import Request from './pages/request.jsx'
import { useAuth } from './auth/AuthContext.jsx'

// changes
function RoleRoute({ allowedRoles, children }) {
  const { token, role } = useAuth()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/home" replace />
  }

  return children
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/home" element={<Home />} />
        <Route path="/select-animal" element={<SelectAnimal />} />
        <Route path="/animal-filter" element={<AnimalFilter />} />
        <Route path="/discussion-board" element={<DiscussionBoard />} />
        <Route path="/shelter-info" element={<ShelterInfo />} />
        <Route path="/About" element={<AboutUs />} />
        <Route path="/help" element={<Help />} />
        <Route
          path="/contribute"
          element={
            // changes
            <RoleRoute allowedRoles={['ROLE_CONTRIBUTOR', 'ROLE_ADMIN']}>
              <Contribute />
            </RoleRoute>
          }
        />
        <Route
          path="/request"
          element={
            // changes
            <RoleRoute allowedRoles={['ROLE_ADMIN']}>
              <Request />
            </RoleRoute>
          }
        />
      </Routes>
    </Router>
  )
}

export default App
