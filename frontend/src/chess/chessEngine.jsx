import { useState, useCallback, useEffect, useRef, useMemo } from 'react';
import { makeMove, engineMakeMove } from "./chessAPI.js";
import { fenParser } from "./chessCommon.jsx";

export const useLocalChess = (uuid, isEngineMode, playerColor, state, setState,
                                counterBlack, counterWhite, syncTimers, bonusTime
) => {
    const [board, setBoard] = useState(() => fenParser(state.fen));
    const isCalculating = useRef(false); 


    const doMove = useCallback(async (move, counters) => {
        try {
            const response = await makeMove(move, uuid);
            const newState = await response.json();
            setBoard(fenParser(newState.fen));
            setState(newState);
            return true;
        } catch (error) {
            console.error("Move failed", error);
            return false;
        }
    }, [uuid]);

    useEffect(() => {
        const engineColor = playerColor === "w" ? "b" : "w";
        const nextTurn = state.fen.split(" ")[1];

        if (isEngineMode && nextTurn === engineColor && !state.checkmate && !state.draw && !isCalculating.current) {
            isCalculating.current = true;
            const time = engineColor === "w" ? counterWhite : counterBlack;
            engineMakeMove(time * 10, bonusTime * 1000, uuid).then(async (res) => {
                const newState = await res.json();
                setBoard(fenParser(newState.fen));
                setState(newState);
                isCalculating.current = false;
            }).catch(() => {
                isCalculating.current = false;
            });
        }
    }, [state, isEngineMode, playerColor, uuid]);

    return { state, board, doMove};
};