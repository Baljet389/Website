import { Link, useSearchParams } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';
import ChessGame from "./chessController.jsx";
import { putFen, getGameState } from "./chessAPI.js";

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

/** * Utility to generate a unique ID for the game session
 */
const generateUUID = () => {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
};

export default function Chess() {
    const [searchParams, setSearchParams] = useSearchParams();
    
    const [uuid, setUUID] = useState(() => generateUUID());
    const [gameState, setGameState] = useState(null);
    const [gameActive, setGameActive] = useState(false);
    const [timeControl, setTimeControl] = useState("5,3");
    const [gameMode, setGameMode] = useState("0");
    const [isWhite, setIsWhite] = useState(true);
    const [joinWithID, setJoinWithID] = useState(false);

 
    const startNewGame = useCallback(async () => {
        try {
            const response = await putFen(INITIAL_FEN, uuid, timeControl, isWhite);
            const data = await response.json();
            setGameState(data);
        } catch (error) {
            console.error("Failed to initialize game:", error);
            alert("Error connecting to server. Please try again.");
            setGameActive(false);
        }
    }, [uuid, timeControl, isWhite]);

    useEffect(() => {
        const gameID = searchParams.get('gameID');
        if (!gameID) {
            setJoinWithID(false);
            return;
        }

        const fetchExistingGame = async () => {
            try {
                const response = await getGameState(gameID);
                if (!response.ok) throw new Error("Game not found");
                
                const data = await response.json();
                setGameState(data);
                setTimeControl(data.timeControl);
                setIsWhite(!data.player1Turn); 
                setUUID(gameID);
                setGameMode("2");
                setJoinWithID(true);
                setGameActive(true);
                setSearchParams({}); 
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
    }, [gameActive, joinWithID, gameState, startNewGame]);

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
            <BackButton />

            {!gameActive ? (
                <SetupForm 
                    timeControl={timeControl}
                    setTimeControl={setTimeControl}
                    gameMode={gameMode}
                    setGameMode={setGameMode}
                    isWhite={isWhite}
                    setIsWhite={setIsWhite}
                    onStart={handleStartClick}
                />
            ) : (
                gameState && (
                    <ChessGame 
                        gameState={gameState} 
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

const BackButton = () => (
    <div className="absolute top-4 left-4">
        <Link to="/">
            <div className="px-6 py-3 bg-gray-400 rounded-2xl shadow-lg text-lg font-bold hover:bg-gray-500 transition-colors cursor-pointer">
                ← Back
            </div>
        </Link>
    </div>
);

const SetupForm = ({ timeControl, setTimeControl, gameMode, setGameMode, isWhite, setIsWhite, onStart }) => (
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
                <option value="0">Local: Two Player</option>
                <option value="1">VS Engine: Computer</option>
                <option value="2">Online: Invite Friend</option>
            </select>
        </div>

        {gameMode !== "0" && (
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

        <button
            onClick={onStart}
            className="w-full bg-gray-800 hover:bg-gray-700 text-white font-bold py-3 rounded-xl transition shadow-md active:transform active:scale-95"
        >
            Start Match
        </button>
    </div>
);