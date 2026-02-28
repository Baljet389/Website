import React, { useState, useMemo, useEffect, use } from 'react';
import { useLocalChess } from './chessEngine.jsx';
import { useOnlineChess } from './chessOnline.jsx';
import {TimerCard, ChessBoardUI, GameOverModal, EmbedLink} from './chessUI.jsx';
import { useChessTimer, isWhite, sharedStates } from './chessCommon.jsx';

const ChessGame = ({ gameState, timeControl, gameMode, uuid, playerColor, joinWithID }) => {
    const [stop, setStop] = useState(null);
    const [timeInSeconds, bonusTime] = useMemo(() => timeControl.split(',').map(Number), [timeControl]);
        
    const [rotation, setRotation] = useState(playerColor === "w" ? 0 : 180);
    const [rotationMode, setRotationMode] = useState(gameMode === "0" ? "rotate" : "fixed");
    
    const isOnline = gameMode === "2";
    const [joined, setJoined] = useState(isOnline ? false : true);
    const [state, setState] = useState(gameState);

    const { counterBlack, counterWhite, syncTimers,
     } = useChessTimer(timeControl, stop, setStop, state, joined);
    
    const game = isOnline ? useOnlineChess(uuid, joinWithID, playerColor
                                            , state, setState, joined, setJoined) : 
                            useLocalChess(uuid, gameMode === "1", playerColor
                                            , state, setState, counterBlack, counterWhite
                                            , syncTimers, bonusTime);


    useEffect(() => {
        if (state.checkmate) setStop(isWhite(state) ? "Black Won!" : "White Won!");
        if (state.draw) setStop("Draw!");
    }, [state]);

    useEffect(() => {
        if (rotationMode === "rotate") 
            setRotation(isWhite(state) ? 0 : 180);
        else 
            setRotation(playerColor === "w" ? 0 : 180);
    }, [state, rotationMode, playerColor]);

    return (
        <div className="chess-layout">
            <div className="relative flex items-center justify-center p-4">
            <div className="flex flex-col items-center mr-8"> 
               <div className={`flex ${rotation === 180 ? 'flex-col-reverse' : 'flex-col'} items-center mr-8`}> 
                    <TimerCard 
                        time={counterBlack} 
                        player="Black"
                        isCurrentTurn={!isWhite(state)} 
                    />
                    <TimerCard 
                        time={counterWhite} 
                        player="White"
                        isCurrentTurn={isWhite(state)} 
                    />
                </div>
                {!joined &&<EmbedLink link={`${import.meta.env.VITE_DOMAIN}/chess?gameID=${uuid}`}
                 maxChars={8} label={'Invite a friend:'}/>}

            </div>
            <ChessBoardUI
                uuid={uuid} 
                board={game.board}
                onMove={(move) => game.doMove(move, { black: counterBlack, white: counterWhite })}
                rotation={rotation}
            />
        </div>
            {stop &&
            <div className = "absolute inset-0 bg-opacity-50 flex items-center justify-center z-10">
                <GameOverModal message={stop} />
            </div>}
        </div>
    );
};

export default ChessGame;