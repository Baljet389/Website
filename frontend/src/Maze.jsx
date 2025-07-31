import { Link } from 'react-router-dom';

export default function Maze() {
  return (
    <div className="flex flex-col items-center justify-center h-screen bg-green-100">
      <h1 className="text-3xl font-bold mb-4">Maze Page</h1>
      <Link to="/" className="text-green-600 underline">Go Back</Link>
    </div>
  );
}