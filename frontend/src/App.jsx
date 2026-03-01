import { Routes, Route, Link } from 'react-router-dom';
import Chess from './chess/chessModes.jsx';
import Navbar from './nav.jsx';

export default function App() {  
  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar onSelectMode={(mode) => {
        window.location.href = `/chess?mode=${mode}`;
      }} />

      <main className="pt-16"> 
        <Routes>
          <Route path="/chess" element={<Chess />} />
        </Routes>
      </main>
    </div>
  );
}