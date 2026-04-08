import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
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
import ContributorApp from './pages/contributorapp.jsx'
import Profile from './pages/profile.jsx'
import Request from './pages/request.jsx'
import AdoptersInfo from './pages/adoptersinfo.jsx'
import UserManage from './pages/usermange.jsx'

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
        <Route path="/contribute" element={<Contribute />} />
        <Route path="/contributor-application" element={<ContributorApp />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/request" element={<Request />} />
        <Route path="/user-management" element={<UserManage />} />
        <Route path="/adopters-info" element={<AdoptersInfo />} />
      </Routes>
    </Router>
  )
}

export default App
