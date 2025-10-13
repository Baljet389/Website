import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { getMoves, makeMove, engineMakeMove, deleteGame } from "./HandleAPI.js";
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// --- Custom Hooks ---

/**
 * Parses the timeControl string and handles all timer state and logic.
 */
const useChessTimer = (timeControl, gameState, stop, setStop, gameHistory, joined) => {
    const parts = useMemo(() => timeControl.split(',').map(Number), [timeControl]);
    const [timeInSeconds, bonusTime] = parts;
    const initialTime = timeInSeconds * 6000;
	const bonusTimeCenti = bonusTime * 100;

    const [counterBlack, setCounterBlack] = useState(initialTime);
    const [counterWhite, setCounterWhite] = useState(initialTime);

    const syncTimers = useCallback((black, white) => {
    setCounterBlack(black);
    setCounterWhite(white);
    }, []);

    // Effect to check for time-out
    useEffect(() => {
        if (counterBlack <= 0) setStop("White won on time! ⏱️");
        else if (counterWhite <= 0) setStop("Black won on time! ⏱️");
    }, [counterBlack, counterWhite]);

    // Effect to handle the countdown
    useEffect(() => {
        if (stop || !joined) return;
        const turn = gameState.fen.toString().split(" ")[1] === 'w';
        let lastUpdate = Date.now();

        const interval = setInterval(() => {
            const now = Date.now();
            const delta = (now - lastUpdate) / 10; 
            lastUpdate = now;

            if (turn) {
                setCounterWhite(prev => Math.max(0, Math.floor( prev - delta)));
            } else {
                setCounterBlack(prev => Math.max(0, Math.floor(prev - delta)));
            }
        }, 10);

        return () => clearInterval(interval);
    }, [stop, gameState, joined]);

    // Increment
    useEffect(() => {
        const turn = gameState.fen.toString().split(" ")[1] === 'w';
        if (gameHistory.length === 0) return;
        if (turn) setCounterBlack(prev => prev + bonusTimeCenti);
        else setCounterWhite(prev => prev + bonusTimeCenti);

    }, [gameState])


    return {
        counterBlack,
        counterWhite,
        syncTimers,
    };
};

/**
 * Manages the main game state, API calls for moves, and game-over logic.
 */
const useGameState = (initialGameState, uuid, setStop) => {
    const initialBoard = useMemo(() => fenParser(initialGameState.fen), [initialGameState.fen]);
    const [state, setState] = useState(initialGameState);
    const [board, setBoard] = useState(initialBoard);
    const [turn, setTurn] = useState(initialGameState.fen.toString().split(" ")[1] === 'w');
    const [gameHistory, setGameHistory] = useState([]);
    const [engineMoves, setEngineMoves] = useState(false);

    
    const syncGame = useCallback((remoteState) => {
        setState(remoteState);
        setBoard(fenParser(remoteState.fen));
        if(state !== initialGameState) setGameHistory(prevHistory => [...prevHistory, { fen: remoteState.fen }]);
    }, []);
    // Internal function for the engine to make a move
    const engineMove = useCallback(async (timeLeft, increment) => {
        if (state.checkmate || state.draw ) return;
        setEngineMoves(true);
        try {
            const response = await engineMakeMove(timeLeft, increment, uuid);
            const newState = await response.json();
            setBoard(fenParser(newState.fen));
            setState(newState);
            setEngineMoves(false);
        } catch (error) {
            console.error("Engine move failed:", error);
            setEngineMoves(false);
        }
    }, []);

    // Effect to handle game end, turn change, and engine move
    useEffect(() => {
        const checkGameOver = () => {
            if (state.draw) {
                setStop("Draw! 🤝");
                return true;
            } else if (state.checkmate) {
                setStop(turn ? "White won! 👑" : "Black won! 👑");
                return true;
            }
            return false;
        };

        if (checkGameOver()) return;

        const nextTurn = state.fen.toString().split(" ")[1] === 'w';
        setTurn(nextTurn);


    }, [state]);

    // Function to execute a move
    const doMove = useCallback(async (move) => {
        if (state.checkmate || state.draw || engineMoves) return;
        try {
            const response = await makeMove(move, uuid);
            const newState = await response.json();
            setBoard(fenParser(newState.fen));
            setState(newState);
            setGameHistory(prevHistory => [...prevHistory, {fen: newState.fen }]);
            return true;
        } catch (error) {
            console.error("Move failed:", error);
            return false;
        }
    }, [state]);

    return {
        state,
        board,
        turn,
        engineMoves,
        doMove,
        engineMove,
        gameHistory,
        syncGame,
    };
};
const use2Player = (joinWithID, uuid, blackTime, whiteTime, syncGame, syncTimers, setJoined, gameMode) =>{
    if(gameMode !== "2") return;
    const stompClientRef = useRef(null);

    useEffect (()=>{
        const socket = new SockJS(`${import.meta.env.VITE_API}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay:5000,
            debug: (str) =>{
               // console.log(str);
            },
             onConnect: () => {
                stompClient.subscribe(`/topic/${uuid}`, (response) => {
                    const body = JSON.parse(response.body);
                    syncGame(body);
                    syncTimers(body.blackTime, body.whiteTime);
                    setJoined(true);
                });
                
                if(joinWithID) sendJoinGameMessage();
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        })
        stompClient.activate(); 
        stompClientRef.current = stompClient;
        
    // Cleanup on unmount
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
    },[])


    const sendJoinGameMessage = () => {
    if (stompClientRef.current && stompClientRef.current.connected) {
        try{ 
        stompClientRef.current.publish({
        destination: `/app/${uuid}.join`, 
        body: JSON.stringify({ blackTime: blackTime,
                               whiteTime: whiteTime, 
         }),
        });
        }
        catch(error){
            console.log(error);
        }
    }
  };
  const sendMakeMoveMessage = (move) => {
    if (stompClientRef.current && stompClientRef.current.connected) {
    try{
        stompClientRef.current.publish({
        destination: `/app/${uuid}.makeMove`, 
        body: JSON.stringify({ blackTime: blackTime,
                               whiteTime: whiteTime,
                               move: move,
         }),
      });
      return true;
    }
      catch(error){
        return false;
      }
    }
  }
  return sendMakeMoveMessage;
}
/**
 * Manages local state for user interactions (selection, dragging, and available moves).
 */
const useSquareInteractions = (uuid, board, doMove, isMoveValid, engineMoves, joined, sendMakeMoveMessage, gameMode, validTurn) => {
    const [draggedFrom, setDraggedFrom] = useState(null);
    const [selectedSquare, setSelectedSquare] = useState(null);
    const [moves, setMoves] = useState([]);

    const fetchMoves = useCallback(async (square) => {
        if (square === null || engineMoves) return;
        try {
            const response = await getMoves(square, uuid);
            const data = await response.json();
            setMoves(data.moves);
        } catch (error) {
            console.error("Failed to fetch moves:", error);
            setMoves([]);
        }
    }, [uuid, engineMoves]);

    const handleSquareClick = useCallback(async (index) => {
        const clickedPiece = board[index];
        const selectedPiece = board[selectedSquare];

        if (selectedSquare === null) {
            // First click: Select piece and fetch moves
            setSelectedSquare(index);
            await fetchMoves(index);
        } else {
            const sameColor = selectedPiece && clickedPiece && getPieceColor(clickedPiece) === getPieceColor(selectedPiece);

            if (sameColor) {
                // Second click on own piece: Select new piece
                await fetchMoves(index);
                setSelectedSquare(index);
                return;
            }

            // Attempt to make a move
            const move = isMoveValid(selectedSquare, index, moves);
            if (move != null) {
                var success;
                if(gameMode !== "2") success = await doMove(move);
                else if(joined === true && validTurn) success = sendMakeMoveMessage(move);
                if (success) {
                    setSelectedSquare(null);
                    setMoves([]);
                }
            } else {
                // Invalid move or clicking on empty square without move
                setSelectedSquare(null);
                setMoves([]);
            }
        }
    }, [selectedSquare, board, fetchMoves, isMoveValid, doMove, moves]);

    const handleDragStart = useCallback(async (e, index) => {
        setDraggedFrom(index);
        setSelectedSquare(index);
        fetchMoves(index);
    }, [fetchMoves]);

    const handleDrop = useCallback(async (e, index) => {
        if (draggedFrom === null) return;
        const move = isMoveValid(draggedFrom, index, moves);
        if (move != null) {
            if(gameMode !== "2") await doMove(move);
            else if(joined === true && validTurn) sendMakeMoveMessage(move);
        }
        setDraggedFrom(null);
        setSelectedSquare(null)
        setMoves([]);
    }, [draggedFrom, isMoveValid, doMove, moves]);

    return {
        selectedSquare,
        draggedFrom,
        moves,
        handleSquareClick,
        handleDragStart,
        handleDrop,
    };
};

function fenParser(fen) {
    const board = new Array(64).fill("");
    const [fenString] = fen.toString().split(" ");
    const rows = fenString.split("/");

    let square = 0;

    for (let i = 0; i < rows.length; i++) {
        const row = rows[i];
        for (const char of Array.from(row)) {
            if (/\d/.test(char)) {
                square += parseInt(char);
            } else {
                const newSquare = i * 8 + (square % 8);
                board[newSquare] = char;
                square++;
            }
        }
    }
    const originalBoard = new Array(64).fill("");
    let originalSquare = 63;

    for (const row of rows) {
        for (const char of Array.from(row)) {
            if (/\d/.test(char)) {
                originalSquare -= parseInt(char);
            } else {
                originalBoard[originalSquare] = char;
                originalSquare--;
            }
        }
    }
    return originalBoard;
}

const getPieceColor = (piece) => piece && (piece[0] === piece[0].toUpperCase() ? 'w' : 'b');

const getPieceSvg = (piece) => {
    if (!piece) return null;
    const color = getPieceColor(piece);
    const type = piece.toLowerCase();
    const pieceMap = { 'r': 'rook', 'n': 'knight', 'b': 'bishop', 'q': 'queen', 'k': 'king', 'p': 'pawn' };
    const pieceName = pieceMap[type];
    return pieceName ? `chess/${pieceName}-${color}.svg` : null;
};

const isMoveValid = (from, to, moves) => {
    const packedMove = (from | (to << 6));
    for (const move of moves) {
        if ((move & 0xFFF) === packedMove) return move;
    }
    return null;
};

const getMoveTargets = (moves) => moves.map(m => ({
    to: (m >> 6) & 0x3F
}));

// --- Sub-Components ---

const TimerCard = ({ time, player, isCurrentTurn }) => {
  const color = player === "White" ? "bg-gray-300" : "bg-gray-600";
  const safeTime = Math.max(time, 0);

  const totalSeconds = safeTime / 100;

  let displayTime;

  if (safeTime < 6000) {
    displayTime = `${totalSeconds.toFixed(1)}s`;
  } else {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = Math.floor(totalSeconds % 60);
    displayTime = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
  }

  return (
    <div
      className={`${isCurrentTurn ? "border-5 border-blue-800" : ""} ${color} shadow-xl rounded-2xl p-4 text-center w-40 mb-4 transition-all duration-300`}
    >
      <h2 className="text-lg font-semibold text-gray-700">{player}</h2>
      <p className="text-4xl font-bold text-gray-900">{displayTime}</p>
    </div>
  );
};

const EmbedLink = ({ link, maxChars = 60, label = "" }) => {
  const [copied, setCopied] = useState(false);
  const displayLink = link.length > maxChars ? link.slice(0, maxChars) + "..." : link;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(link);
      setCopied(true);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  return (
  <div className="p-3 rounded-2xl bg-gray-200 max-w-md">
    {label && (
      <p className="text-sm font-medium text-gray-700 mb-2">{label}</p>
    )}

    <div className="flex items-center justify-between gap-3">
      <a
        href={link}
        target="_blank"
        rel="noopener noreferrer"
        title={link}
        className="text-gray-800 font-medium hover:underline flex-1 min-w-0 truncate"
      >
        {displayLink}
      </a>

      <button
        onClick={handleCopy}
        className={`px-3 py-1.5 text-sm font-semibold rounded-lg transition-colors duration-200 shrink-0 ${
          copied
            ? "bg-gray-300 text-white"
            : "bg-gray-800 text-white hover:bg-gray-700"
        }`}
      >
        Copy
      </button>
    </div>
  </div>
);
};

const Square = React.memo(({
    index,
    piece,
    isLightSquare,
    selectedSquare,
    targets,
    rotationDeg,
    handleSquareClick,
    handleDragStart,
    handleDrop,
}) => {
    const isTarget = targets.some(target => target.to === index);
    const isCapture = isTarget && !!piece;

    let squareColorClass = isLightSquare ? 'bg-gray-300' : 'bg-gray-600';

    if (index === selectedSquare && piece) squareColorClass = 'bg-emerald-400';
    else if (isCapture) squareColorClass = 'bg-rose-500';

    const pieceSvg = getPieceSvg(piece);

    return (
        <div
            className={`flex items-center justify-center rounded-md ${squareColorClass} relative`}
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => handleDrop(e, index)}
            onClick={() => handleSquareClick(index)}
        >
            {isTarget && !piece && (
                <div className="absolute w-1/4 h-1/4 rounded-full bg-black opacity-30 pointer-events-none"></div>
            )}
            {pieceSvg && (
                <img
                    src={pieceSvg}
                    alt={piece}
                    draggable
                    onDragStart={(e) => handleDragStart(e, index)}
                    className="w-full h-full object-contain"
                    style={{ transform: `rotate(${rotationDeg}deg)` }}
                />
            )}
        </div>
    );
});


// --- Main Component ---

const ChessBoard = ({ gameState, timeControl, gameMode, uuid, playerColor,joinWithID }) => {
    const [stop, setStop] = useState(null); // Game end message
    const [joined, setJoined] = useState(gameMode === "2" ? false : true);
    const [validTurn, setValidTurn] = useState(false);

    const rotationMode = gameMode === '0' ? '0' : (playerColor === "w" ? "1" : "2");
    const [timeInSeconds, bonusTime] = timeControl.split(',').map(Number);

    const {
        state,
        board,
        turn,
        engineMoves,
        doMove,
        engineMove,
        gameHistory,
        syncGame,
    } = useGameState(
        gameState,
        uuid,
        setStop,
    );


    // Timer logic
    const { counterBlack, counterWhite, syncTimers }
        = useChessTimer(timeControl, state, stop, setStop, gameHistory, joined);

    const sendMakeMoveMessage = use2Player(
        joinWithID,uuid, counterBlack,counterWhite, syncGame, syncTimers, setJoined, gameMode);


    useEffect(() => {
        const engineColor = playerColor === "w" ? "b" : "w";
        const nextTurn = state.fen.split(" ")[1]; // "w" or "b"
        if (gameMode === "1" && !engineMoves && nextTurn === engineColor) {
            const timeLeft = nextTurn === "w" ? counterWhite : counterBlack;
            engineMove(timeLeft * 10, bonusTime * 1000, uuid);
        }
        setValidTurn(nextTurn === playerColor);
    }, [state]);


    // Interaction logic
    const {
        selectedSquare,
        moves,
        handleSquareClick,
        handleDragStart,
        handleDrop,
    } = useSquareInteractions(uuid, board, doMove, isMoveValid, engineMoves, joined, sendMakeMoveMessage, gameMode, validTurn);

    // Effect to clean up game on stop
    useEffect(() => {
        if (stop) {
            deleteGame(uuid).catch(err => console.error("Failed to delete game:", err));
        }
    }, [stop, uuid]);

    // Determine board rotation
    const rotationDeg = useMemo(() => {
        const rotationMap = {
            '0': turn ? 0 : 180, // Rotate to player's side
            '1': 0,             // Always white (0 degrees)
            '2': 180,           // Always black (180 degrees)
        };
        return rotationMap[rotationMode] || 0;
    }, [rotationMode, turn]);

    const targets = useMemo(() => getMoveTargets(moves), [moves]);
    return (
        <div className="relative flex items-center justify-center p-4">
            <div className="flex flex-col items-center mr-8">
                <TimerCard time={counterBlack} player={"Black"} isCurrentTurn={!turn} />
                <TimerCard time={counterWhite} player={"White"} isCurrentTurn={turn} />
                {gameMode === "2" && !joinWithID && !joined &&
                <EmbedLink link={`${import.meta.env.VITE_DOMAIN}/chess?gameID=${uuid}`} maxChars={8} label={'Invite a friend:'}/>}
            </div>
            <div className="bg-gray-800 shadow-2xl rounded-lg overflow-hidden w-full max-w-lg aspect-square">
                <div
                    className="grid grid-cols-8 grid-rows-8 w-full h-full"
                    style={{ transform: `rotate(${rotationDeg}deg)` }}
                >
                    {Array.from({ length: 64 }, (_, index) => {
                        const rowIndex = Math.floor(index / 8);
                        const colIndex = index % 8;

                        const flippedIndex = 63 - (rowIndex * 8 + colIndex);
                        const piece = board[flippedIndex];
                        const isLightSquare = (rowIndex + colIndex) % 2 === 0;

                        return (
                            <Square
                                key={index}
                                index={flippedIndex}
                                piece={piece}
                                isLightSquare={isLightSquare}
                                selectedSquare={selectedSquare}
                                targets={targets}
                                rotationDeg={rotationDeg}
                                handleSquareClick={handleSquareClick}
                                handleDragStart={handleDragStart}
                                handleDrop={handleDrop}
                            />
                        );
                    })}
                </div>
            </div>
            {/* Game Over Modal */}
            {stop && (
                <div className="absolute center bg-gray-200 p-8 rounded-xl text-center shadow-2xl">
                    <h2 className="text-3xl font-bold text-gray-800 mb-4">{stop}</h2>
                    <button
                        onClick={() => window.location.reload()}
                        className="mt-4 px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg hover:bg-gray-300 transition duration-200"
                    >
                        Restart Game
                    </button>
                </div>
            )}
        </div>
    );
};

export default ChessBoard;