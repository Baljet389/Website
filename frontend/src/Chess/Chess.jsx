import { Link, useSearchParams } from 'react-router-dom';
import ChessBoard from "./ChessBoard.jsx";
import {putFen,getGameState} from "./HandleAPI.js";
import { useEffect, useState } from 'react';



export default function Chess(){
	//rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	//8/4P1K1/3q4/3k4/8/8/8/8 w - - 0 1
	//8/5K2/4P3/4q3/8/8/8/k7 w - - 0 1
  const fen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
  const [uuid, setUUID] = useState(() => uuidv4());
  const [gameState, setGameState] = useState(null);
  const [gameActive,setGameActive] = useState(false);
  const [timeControl,setTimeControl] = useState("5,3");
  const [gameMode,setGameMode] = useState("0");
  const [turn, setTurn] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();
  const [joinWithID, setJoinWithID] = useState(false); 

  const handleChangeTurn = () => {
    setTurn(!turn);
  };
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
    if(joinWithID) return;
    const sendFen = async () => {
      try{
      const response = await putFen(fen,uuid,timeControl,turn);
      const data = await response.json()
      setGameState( data);
      console.log(uuid);
      }
      catch(error){
        console.error(error);
      }
    };
   if (gameActive){
    sendFen();
   }
  }, [gameActive]);


  useEffect(() =>{
    const gameID = searchParams.get('gameID');
    if(gameID){
      const getState = async() =>{
        try{
        const response = await getGameState(gameID);
        const data = await response.json();
        setGameState(data);
        setTimeControl(data.timeControl);
        setTurn(!data.player1Turn);
		    setUUID(gameID);
        setGameMode("2");
        setJoinWithID(true);
        }
        catch(error){ 
          console.log(error);
          return}
      }
      getState();
      setSearchParams({});
      setJoinWithID(true);
      setGameActive(true);
      
    }
    else{
      setJoinWithID(false)
    }
    
  },[searchParams])
  return(
    <div  className="min-h-screen bg-gray-100 flex items-center justify-center p-4 font-inter">
       <div className="absolute top-4 left-4">
        <Link to="/">
          <div className="text-center p-10 bg-gray-400 rounded-2xl shadow-lg text-xl font-bold hover:bg-gray-500 cursor-pointer">
                  Back
          </div>
        </Link>
      </div>
     {!gameActive && (
  <div className="max-w-md mx-auto p-6 rounded-2xl shadow-lg bg-white space-y-6">
    <h2 className="text-2xl font-semibold text-gray-800 text-center">
      Start a Chess Game
    </h2>

    {/* Time Control */}
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">
        Time Control <span className="text-gray-400">(minutes, increment)</span>
      </label>
      <input
        type="text"
        value={timeControl}
        onChange={handleInputChange}
        placeholder="e.g. 10,2"
        className="w-full px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
      />
    </div>

    {/* Game Mode */}
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">
        Game Mode
      </label>
      <select
        name="Game Mode"
        className="w-full px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
        onChange={handleGameModeChange}
      >
        <option value="0">Two Player</option>
        <option value="1">Play Against Engine</option>
        <option value="2">Two Player Via Internet</option>
      </select>
    </div>

    {/* Engine / Online Options */}
    {gameMode !== "0" && (
      <div className="flex items-center space-x-2 mt-2">
        <input
          id="playWhite"
          type="checkbox"
          checked={turn}
          onChange={handleChangeTurn}
          className="h-5 w-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <label htmlFor="playWhite" className="text-sm text-gray-700">
          Play as White
        </label>
      </div>
    )}

      <button
        onClick={handleStartGame}
        className="w-full bg-gray-600 hover:bg-gray-300 text-white font-semibold py-2 rounded-lg transition duration-150"
      >
        Start Game
      </button>
      </div>)}
      {gameActive && gameState &&<ChessBoard gameState = {gameState} timeControl = {timeControl} gameMode={gameMode}  uuid={uuid} playerColor={turn?"w":"b"} joinWithID={joinWithID}/>}
    </div>
  );

  

}

const uuidv4 = () => {
  return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
    (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
  );
}
      
    


