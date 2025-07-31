import React, { useState, useEffect } from 'react';


const ChessBoard =  ({fen}) => {
  // Initial board setup (standard chess starting position)
  // 'r': rook, 'n': knight, 'b': bishop, 'q': queen, 'k': king, 'p': pawn
  // Lowercase for black, uppercase for white
  const initialBoard = fenParser(fen);


  const [board, setBoard] = useState(initialBoard);
  

  const [draggedFrom, setDraggedFrom] = useState(null);

const handleDragStart = (e, index) => {
  setDraggedFrom(index);
};

const handleDrop = async (e, toIndex) => {
  if (draggedFrom === null) return;

  const from = 63 - draggedFrom;
  const to = 63 - toIndex;

  // Convert index to algebraic square (e.g., 0 -> a1, 63 -> h8)
  const indexToSquare = (i) => {
    const file = String.fromCharCode(97 + (i % 8)); // a-h
    const rank = Math.floor(i / 8) + 1; // 1-8
    return `${file}${rank}`;
  };

  const fromSquare = indexToSquare(from);
  const toSquare = indexToSquare(to);
  

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
    <div className="bg-gray-800 shadow-2xl rounded-lg overflow-hidden w-full max-w-lg aspect-square">
      <div className="grid grid-cols-8 grid-rows-8 w-full h-full">
        {Array.from({ length: 8 }, (_, rowIndex) => (
         Array.from({ length: 8 }, (_, colIndex) => {
         const index = 63 - (rowIndex * 8 + colIndex);
         const piece = board[index];
         const isLightSquare = (rowIndex + colIndex) % 2 === 0;
         const squareColorClass = isLightSquare ? 'bg-gray-300' : 'bg-gray-600';
         const pieceSvg = getPieceSvg(piece);
            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                className={`flex items-center justify-center rounded-md ${squareColorClass}`}
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => handleDrop(e, index)}
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

export default ChessBoard;