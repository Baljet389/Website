import { Routes, Route, Link } from 'react-router-dom';
import Chess from './Chess/Chess.jsx';
import Maze from './Maze.jsx';

export default function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <div className="flex justify-center items-center h-screen bg-gray-100">
            <div className="grid grid-cols-2 gap-8">
              <Link to="/chess">
                <div className="text-center p-10 bg-white rounded-2xl shadow-lg text-xl font-bold hover:bg-gray-200 cursor-pointer">
                  Chess
                </div>
              </Link>
              <Link to="/maze">
                <div className="text-center p-10 bg-white rounded-2xl shadow-lg text-xl font-bold hover:bg-gray-200 cursor-pointer">
                  Maze
                </div>
              </Link>
            </div>
          </div>
        }
      />
      <Route path="/chess" element={<Chess />} />
      <Route path="/maze" element={<Maze />} />
    </Routes>
  );
}