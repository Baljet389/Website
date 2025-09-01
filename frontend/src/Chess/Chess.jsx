import { Link } from 'react-router-dom';
import ChessBoard from "./ChessBoard.jsx";
import {putFen} from "./HandleAPI.js";
import { useEffect, useState } from 'react';


export default function Chess(){
	//rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	//8/4P1K1/3q4/3k4/8/8/8/8 w - - 0 1
	//8/5K2/4P3/4q3/8/8/8/k7 w - - 0 1
  var fen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
  
  const [gameState, setGameState] = useState(null);
  const [gameActive,setGameActive] = useState(false);
  const [timeControl,setTimeControl] = useState("5,3");
  const [gameMode,setGameMode] = useState("0");

  const handleInputChange = (e) => {
    setTimeControl(e.target.value);
  };
  const handleGameModeChange = (e) => {
    setGameMode(e.target.value);
  }

   const handleStartGame = () => {
    const parts = timeControl.split(',').map(Number);
    if (
      parts.length !== 2 ||
      parts.some((val) => isNaN(val) || val < 0)
    ) {
      alert("Please enter time control in the format: 10,2");
      return;
    }
    setGameActive(true);
  };

  useEffect(() => {
    const sendFen = async () => {
      const response = await putFen(fen);
      const data = await response.json();
      setGameState( data);
    };
   if (gameActive){
    sendFen();
   }
  }, [gameActive]);
  return(
    <div  className="min-h-screen bg-gray-100 flex items-center justify-center p-4 font-inter">
       <div className="absolute top-4 left-4">
        <Link to="/">
          <div className="text-center p-10 bg-gray-400 rounded-2xl shadow-lg text-xl font-bold hover:bg-gray-500 cursor-pointer">
                  Back
          </div>
        </Link>
      </div>
      {!gameActive &&  <div className="max-w-md mx-auto p-6 rounded-2xl shadow-lg bg-white space-y-4">
      <h2 className="text-2xl font-semibold text-gray-800">Start a Chess Game</h2>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Time Control [minutes, increment]:
        </label>
        <input
          type="text"
          value={timeControl}
          onChange={handleInputChange}
          placeholder="e.g. 10,2"
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Game Mode
        </label>
        <select name="Game Mode"
                className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                onChange={handleGameModeChange}>

           <option value="0">Two Player</option>
           <option value="1">Play against engine</option>
        </select>
      </div>
      <button
        onClick={handleStartGame}
        className="w-full bg-gray-600 hover:bg-gray-300 text-white font-semibold py-2 rounded-lg transition duration-150"
      >
        Start Game
      </button>
      </div>}
      {gameActive && gameState &&<ChessBoard gameState = {gameState} timeControl = {timeControl} gameMode={gameMode} rotationMode={gameMode==='0'?'0':'1'}/>}
    </div>
  );

  

}

      
    


