import React, { useState, useEffect } from 'react';
import {getMoves,makeMove} from "./HandleAPI.js";

const ChessBoard =  ({gameState,timeControl}) => {
  // Initial board setup (standard chess starting position)
  // 'r': rook, 'n': knight, 'b': bishop, 'q': queen, 'k': king, 'p': pawn
  // Lowercase for black, uppercase for white
  const initialBoard = fenParser(gameState.fen);

  const [state,setState] = useState(gameState);
  const stateRef = React.useRef(state);

  const [board, setBoard] = useState(initialBoard);
  const [draggedFrom, setDraggedFrom] = useState(null);
  const [selectedSquare,setSelectedSquare] = useState(null);
  const [moves, setMoves] = useState([]);
  const [stop,setStop] = useState(null);

  const parts = timeControl.split(',').map(Number);
  const timeInSeconds = parts[0] * 60;
  const bonusTime = parts[1];

  const [counterBlack, setCounterBlack] = React.useState(timeInSeconds);
  const [counterWhite, setCounterWhite] = React.useState(timeInSeconds);


  React.useEffect(() => {
  stateRef.current = state;
    const turn = stateRef.current.fen.toString().split(" ")[1] === 'w';
    if(state.draw === true) setStop("Draw");
    else if(state.checkmate === true && turn) setStop("Black won!");
    else if(state.checkmate === true && !turn) setStop("White won!");
    //Increment
    if(turn) setCounterBlack(counterBlack + bonusTime);
    else setCounterWhite(counterWhite + bonusTime);
  }, [state]);

  React.useEffect(() =>{
    if(counterBlack <= 0) setStop("White won on time!");
    else if(counterWhite <= 0) setStop("Black has won on time!");
  },[counterBlack,counterWhite]);

  useEffect(() => {
    if(stop != null) return;
     const interval =  setInterval(() => {
        if(stateRef.current.fen.toString().split(" ")[1] === 'w') {
        setCounterWhite(prevTime => {
          if (prevTime > 0) return Math.floor(10 * (prevTime - 0.1)) / 10; 
        });}
        else if (stateRef.current.fen.toString().split(" ")[1] === 'b'){
        setCounterBlack(prevTime => {
          if (prevTime > 0) return Math.floor(10 * (prevTime - 0.1)) / 10; 
        });}
        
      }, 100);
      return ()=> clearInterval(interval)
  }, [stop]);

  const fetchMoves = async (square) => {
    if (square === null) return;
    const response = await getMoves(square);
    const data = await response.json();
    setMoves(data.moves);
  };

  const doMove = async (move) => {
    if (move ==null) return;
    if (stop) return;
    const response = await makeMove(move);
    const newState = await response.json();
    setBoard(fenParser(newState.fen));
    setState(newState);
  };

  const isMoveValid = (from,to) =>{
    const packedMove =(from | (to<<6));
    for (const move of moves){
      if((move & 0xFFF) === packedMove) return move;
    }
    return null 
  };
 const getMoveTargets = () => {
  return moves.map(m => ({
    to: (m >> 6) & 0x3F
  }));
};
    

const handleSquareClick = async (e,index) =>{
  const clickedPiece = board[index];

  if (selectedSquare === null){
    setSelectedSquare(index);
    await fetchMoves(index)
  }
  else {
    const selectPiece = board[selectedSquare];
    if(selectPiece && clickedPiece && getPieceColor(clickedPiece) === getPieceColor(selectPiece)){
        await fetchMoves(index);
        setSelectedSquare(index);
        return;
    }

    const move = isMoveValid(selectedSquare,index);
    if (move != null) await doMove(move);
    setSelectedSquare(null)
    setMoves([]);
  }
  
}
const handleDragStart = (e, index) => {
  setDraggedFrom(index);
  fetchMoves(index)
};

const handleDrop = async (e, index) => {
  if (draggedFrom === null) return;
  const move = isMoveValid(draggedFrom,index);
  if(move != null) await doMove(move);
  setDraggedFrom(null);
  setMoves([]);
}
  
  const getPieceColor = (piece) => {return  piece[0] === piece[0].toUpperCase() ? 'w' : 'b';}  
  const getPieceSvg = (piece) => {
    if (!piece) return null; 
    const color = getPieceColor(piece); 
    const type = piece[0].toLowerCase(); 

    let pieceName;
    switch (type) {
      case 'r': pieceName = 'rook'; break;
      case 'n': pieceName = 'knight'; break;
      case 'b': pieceName = 'bishop'; break;
      case 'q': pieceName = 'queen'; break;
      case 'k': pieceName = 'king'; break;
      case 'p': pieceName = 'pawn'; break;
      default: return null;
    }
    return `chess/${pieceName}-${color}.svg`;
  };

  return (
    <div className="relative flex items-center justify-center">
    <div className="bg-gray-800 shadow-2xl rounded-lg overflow-hidden w-full max-w-lg aspect-square">
        
        <div className="grid grid-cols-8 grid-rows-8 w-full h-full">
        {Array.from({ length: 8 }, (_, rowIndex) => (
         Array.from({ length: 8 }, (_, colIndex) => {
         const index = 63 - (rowIndex * 8 + colIndex);
         const piece = board[index];

         const isLightSquare = (rowIndex + colIndex) % 2 === 0;
         let squareColorClass = isLightSquare ? 'bg-gray-300' : 'bg-gray-600';
         const targets = getMoveTargets();
         let capture = false;
         let possibleTarget = false;

         for(const target of targets){
            if(target.to === index){
               possibleTarget= true;
               if(piece) capture = true;
               break;
              }
         }
         if (index === selectedSquare && piece) squareColorClass = 'bg-emerald-400';
         else if(capture) squareColorClass = 'bg-rose-500';
         else if(possibleTarget) squareColorClass = 'bg-cyan-400';

         const pieceSvg = getPieceSvg(piece);
            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                className={`flex items-center justify-center rounded-md ${squareColorClass}`}
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => handleDrop(e, index)}
                onClick={(e) => handleSquareClick(e,index)}
              >
                {pieceSvg && (
                  <img
                    src={pieceSvg}
                    alt={piece}
                    draggable
                    onDragStart={(e) => handleDragStart(e, index)}
                    className="w-full h-full object-contain"
                    
                  />
                )}
              </div>
            );
          })
        ))}
      </div>
    </div>
      <div>
        <div className="transform rotate-180">
          <TimerCard time={counterBlack} player= "Black"/>
        </div>
          <TimerCard time ={counterWhite} player = "White"/>
      </div>
        {stop && (
        <div   className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50">
        <div className="bg-gray-300 p-6 rounded-xl text-center shadow-2xl">
          <h2 className="text-2xl font-bold text-bg-gray-600">{stop}</h2>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-400"
          >
            Restart
          </button>
        </div>
        </div>
    )}
    </div>
    
  );
};
function fenParser(fen){
  const board = new Array(64).fill("");

  const fenParts = fen.toString().split(" ");
  const rows = fenParts[0].split("/");
  let square = 63;

  for(const row of rows){
    const chars = Array.from(row);

    for(const char of chars){
      if(/\d/.test(char)){
        square -= parseInt(char);
      }
      else{
      board[square] = char;
      square--;
      }
    }
  }
  return board;
}
const TimerCard = ({ time, player }) => {
  const color = (player ==="White")? 'bg-gray-300':'bg-gray-600';
return (
  <div className={`${color} shadow-xl rounded-2xl p-4 text-center w-40`}>
    <h2 className="text-lg font-semibold text-gray-700">{player}</h2>
    <p className="text-4xl font-bold text-gray-900">
      {time < 60
        ? (time >= 0 ? time.toFixed(1) : "0.0") + "s"
        : `${String(Math.floor(time / 60)).padStart(2, '0')}:${String(Math.floor(time % 60)).padStart(2, '0')}`}
    </p>
  </div>
);
};


export default ChessBoard;