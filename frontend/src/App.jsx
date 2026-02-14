import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Login from './pages/login.jsx'
import Signup from './pages/signup.jsx'
import Home from './pages/home.jsx'
import SelectAnimal from './pages/SelectAnimal.jsx'
import AboutUs from './pages/AboutUs.jsx'
import Help from './pages/help.jsx'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/home" element={<Home />} />
        <Route path="/select-animal" element={<SelectAnimal />} />
        <Route path="/About" element={<AboutUs />} />
        <Route path="/help" element={<Help />} />
      </Routes>
    </Router>
  )
}

export default App
