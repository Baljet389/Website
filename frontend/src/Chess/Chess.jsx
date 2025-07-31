import { Link } from 'react-router-dom';
import ChessBoard from "./ChessBoard.jsx";
import {putFen} from "./HandleAPI.js";
import { useEffect, useState } from 'react';


export default function Chess(){
  var fen = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

  useEffect(() => {
    const sendFen = async () => {
      const response = await putFen(fen);
      const data = await response.json();
      console.log('API response:', data);
    };

    sendFen();
  }, []);
  return(
    <div  className="min-h-screen bg-gray-100 flex items-center justify-center p-4 font-inter">
       <div className="absolute top-4 left-4"> {}
        <Link to="/">
          <div className="text-center p-10 bg-gray-400 rounded-2xl shadow-lg text-xl font-bold hover:bg-gray-200 cursor-pointer">
                  Back
          </div>
        </Link>
      </div>
      <ChessBoard fen = {fen}/>
    </div>
  );
}


