import { Link, useSearchParams } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';
import ChessGame from "./chessController.jsx";
import { putFen, getGameState, getGameInfo } from "./chessAPI.js";
import {ChessMode, ChessVariation} from "./chessCommon.jsx";

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';


export default function Chess() {
    const [searchParams, setSearchParams] = useSearchParams();
    
    const [uuid, setUUID] = useState(null);
    const [gameState, setGameState] = useState(null);
    const [gameActive, setGameActive] = useState(false);
    const [timeControl, setTimeControl] = useState("5,3");
    const [gameMode, setGameMode] = useState(ChessMode.LOCAL);
    const [chess960, setChess960] = useState(false);
    const [isWhite, setIsWhite] = useState(true);
    const [joinWithID, setJoinWithID] = useState(false);

 
    const startNewGame = useCallback(async () => {
        try {
            const times = timeControl.split(',').map(Number);
            const timeLeftMs = times[0] * 60 * 1000;
            const incrementMs = times[1] * 1000;

            const response = await putFen(INITIAL_FEN, gameMode,
                                         chess960 ? ChessVariation.CHESS960 : ChessVariation.STANDARD,
                                         timeLeftMs, incrementMs, isWhite);
            const data = await response.json();
            setUUID(data.gameID);

            const responseState = await getGameState(data.gameID);
            const stateData = await responseState.json();
            setGameState(stateData); 
        } catch (error) {
            alert("Error connecting to server. Please try again.");
            setGameActive(false);
        }
    }, [uuid, timeControl, isWhite, gameMode, chess960]);

    useEffect(() => {
        const gameModeParam = searchParams.get('mode');
        if(gameModeParam) {
            switch (gameModeParam) {
                case ChessMode.LOCAL:
                    setGameMode(ChessMode.LOCAL);
                    break;
                case ChessMode.ENGINE:
                    setGameMode(ChessMode.ENGINE);
                    break;
                case ChessMode.ONLINE:
                    setGameMode(ChessMode.ONLINE);
                    break;
                default:
                    setGameMode(ChessMode.LOCAL);
            
            }
            setSearchParams({});
        }

        const gameID = searchParams.get('gameID');
        if (!gameID) {
            setJoinWithID(false);
            return;
        }

        const fetchExistingGame = async () => {
            try {
                const response = await getGameInfo(gameID);
                if (!response.ok) throw new Error("Game not found");
                
                const data = await response.json();
                setUUID(data.gameID);
                const timeControl = `${Math.floor(data.timeLeft / 60000)},${Math.floor(data.increment / 1000)}`;
                setTimeControl(timeControl);
                setIsWhite(data.white); 
                
                const responseState = await getGameState(data.gameID);
                const stateData = await responseState.json();
                setGameState(stateData);

                setJoinWithID(true);
                setGameActive(true);
                setSearchParams({});
                setGameMode(ChessMode.ONLINE); 
            } catch (error) {
                console.error("Link join error:", error);
                setJoinWithID(false);
            }
        };

        fetchExistingGame();
    }, [searchParams, setSearchParams]);

    useEffect(() => {
        if (gameActive && !joinWithID && !gameState) {
            startNewGame();
        }
    }, [gameActive]);

    const handleStartClick = () => {
        const parts = timeControl.split(',').map(Number);
        if (parts.length !== 2 || parts.some(val => isNaN(val) || val < 0)) {
            alert("Please enter time control in the format: minutes,increment (e.g. 10,2)");
            return;
        }
        setGameActive(true);
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4 font-inter">

            {!gameActive ? (
                <SetupForm 
                    timeControl={timeControl}
                    setTimeControl={setTimeControl}
                    gameMode={gameMode}
                    setGameMode={setGameMode}
                    isWhite={isWhite}
                    setIsWhite={setIsWhite}
                    chess960={chess960}
                    setChess960={setChess960}
                    onStart={handleStartClick}
                />
            ) : (
                gameState && (
                    <ChessGame 
                        initialState={gameState} 
                        timeControl={timeControl} 
                        gameMode={gameMode}  
                        uuid={uuid} 
                        playerColor={isWhite ? "w" : "b"} 
                        joinWithID={joinWithID}
                    />
                )
            )}
        </div>
    );
}



const SetupForm = ({
                    timeControl, 
                    setTimeControl, 
                    gameMode, 
                    setGameMode, 
                    isWhite, 
                    setIsWhite,
                    chess960,
                    setChess960, 
                    onStart }) => (
    <div className="max-w-md w-full p-8 rounded-2xl shadow-xl bg-white space-y-6">
        <h2 className="text-2xl font-bold text-gray-800 text-center">
            New Chess Game
        </h2>

        <div>
            <label className="block text-sm font-semibold text-gray-600 mb-2">
                Time Control <span className="font-normal text-gray-400">(min, inc)</span>
            </label>
            <input
                type="text"
                value={timeControl}
                onChange={(e) => setTimeControl(e.target.value)}
                placeholder="e.g. 10,2"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition"
            />
        </div>

        <div>
            <label className="block text-sm font-semibold text-gray-600 mb-2">Game Mode</label>
            <select
                value={gameMode}
                onChange={(e) => setGameMode(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition"
            >
                <option value={ChessMode.LOCAL}>Local: Two Player</option>
                <option value={ChessMode.ENGINE}>VS Engine: Computer</option>
                <option value={ChessMode.ONLINE}>Online: Invite Friend</option>
            </select>
        </div>

        {gameMode !== ChessMode.LOCAL && (
            <div className="flex items-center space-x-3 p-2 bg-gray-50 rounded-lg">
                <input
                    id="playWhite"
                    type="checkbox"
                    checked={isWhite}
                    onChange={() => setIsWhite(!isWhite)}
                    className="h-5 w-5 text-blue-600 rounded focus:ring-blue-500"
                />
                <label htmlFor="playWhite" className="text-sm font-medium text-gray-700 cursor-pointer">
                    Play as White
                </label>
            </div>
        )}
     
        <div className="flex items-center space-x-3 p-2 bg-gray-50 rounded-lg">
            <input
                id="Chess960"
                type="checkbox"
                checked={chess960}
                onChange={() => setChess960(!chess960)}
                className="h-5 w-5 text-blue-600 rounded focus:ring-blue-500"
            />
            <label htmlFor="Chess960" className="text-sm font-medium text-gray-700 cursor-pointer">
                Play Chess960
            </label>
        </div>
    

        <button
            onClick={onStart}
            className="w-full bg-gray-800 hover:bg-gray-700 text-white font-bold py-3 rounded-xl transition shadow-md active:transform active:scale-95"
        >
            Start Match
        </button>
    </div>
);