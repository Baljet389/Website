import { useState, useCallback, useMemo, useEffect, useRef } from 'react';
import { getMoves } from "./chessAPI.js";

export const fenParser = (fen) => {
    const [fenString] = fen.toString().split(" ");
    const rows = fenString.split("/");
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
};

export const getPieceColor = (piece) => piece && (piece[0] === piece[0].toUpperCase() ? 'w' : 'b');

export const getPieceSvg = (piece) => {
    if (!piece) return null;
    const color = getPieceColor(piece);
    const pieceMap = { 'r': 'rook', 'n': 'knight', 'b': 'bishop', 'q': 'queen', 'k': 'king', 'p': 'pawn' };
    const pieceName = pieceMap[piece.toLowerCase()];
    return pieceName ? `chess/${pieceName}-${color}.svg` : null;
};

export const isMoveValid = (from, to, moves) => {
    const packedMove = (from | (to << 6));
    for (const move of moves) {
        if ((move & 0xFFF) === packedMove) return move;
    }
    return null;
};

export const useSquareInteractions = (uuid, board, onMove) => {
    const [selectedSquare, setSelectedSquare] = useState(null);
    const [moves, setMoves] = useState([]);

    const fetchMoves = useCallback(async (square) => {
        if (square === null) return;
        try {
            const response = await getMoves(square, uuid);
            const data = await response.json();
            setMoves(data.moves);
        } catch (error) {
            setMoves([]);
        }
    }, [uuid]);

    const handleSquareClick = async (index) => {
        if (selectedSquare === null) {
            setSelectedSquare(index);
            await fetchMoves(index);
        } else {
            const move = isMoveValid(selectedSquare, index, moves);
            if (move != null) {
                const success = await onMove(move);
                if (success) {
                    setSelectedSquare(null);
                    setMoves([]);
                }
            } else {
                setSelectedSquare(index);
                await fetchMoves(index);
            }
        }
    };

    return {
        selectedSquare,
        moves,
        handleSquareClick,
        setMoves,
        setSelectedSquare
    };
};

export const useChessTimer = (timeControl, stop, setStop, gameState, joined) => {
    const parts = useMemo(() => timeControl.split(',').map(Number), [timeControl]);
    const [timeInSeconds, bonusTime] = parts;
    const initialTime = timeInSeconds * 6000;
	const bonusTimeCenti = bonusTime * 100;

    const [counterBlack, setCounterBlack] = useState(initialTime);
    const [counterWhite, setCounterWhite] = useState(initialTime);
    const isFirstRender = useRef(true);

    const syncTimers = useCallback((black, white) => {
    setCounterBlack(black);
    setCounterWhite(white);
    }, []);
    

    useEffect(() => {
        if (counterBlack <= 0) setStop("White won on time! ⏱️");
        else if (counterWhite <= 0) setStop("Black won on time! ⏱️");
    }, [counterBlack, counterWhite]);

    useEffect(() => {
        if (stop || !joined) return;
        const turn = isWhite(gameState);
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
    }, [stop, gameState]);

    useEffect(() => {
        if (isFirstRender.current) {
            isFirstRender.current = false;
            return;
        }
        const turn = isWhite(gameState);
        if (turn) setCounterBlack(prev => prev + bonusTimeCenti);
        else setCounterWhite(prev => prev + bonusTimeCenti);
    }, [gameState])

   
    return {
        counterBlack,
        counterWhite,
        syncTimers,
    };
};

export const isWhite = (state) => {
    return state.fen.split(" ")[1] === 'w';
}
export const currentTurn = (state) => {
    return state.fen.split(" ")[1];
}
export const sharedStates = (gameState, joined, gameHistory) => {
    const [state, setState] = useState(gameState);
    const [joinedState, setJoined] = useState(joined);
    const [gameHistoryState, setGameHistory] = useState(gameHistory);

    return (
        state, setState,
        joinedState, setJoined,
        gameHistoryState, setGameHistory
    );
}