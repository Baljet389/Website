import { useState, useEffect, useRef} from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { fenParser, currentTurn } from "./chessCommon.jsx";

export const useOnlineChess = (uuid, joinWithID, playerColor,
                                 state, setState, joined, setJoined) => {
    const [board, setBoard] = useState(() => fenParser(state.fen));
    const stompClientRef = useRef(null);

    useEffect(() => {
        const socket = new SockJS(`${import.meta.env.VITE_API}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                stompClient.subscribe(`/topic/${uuid}`, (response) => {
                    const body = JSON.parse(response.body);
                    setState(body);
                    setBoard(fenParser(body.fen));
                    setJoined(true);
                });
                if (joinWithID) {
                    stompClient.publish({ destination: `/app/${uuid}.join`, body: JSON.stringify({}) });
                }
            },
        });
        stompClient.activate();
        stompClientRef.current = stompClient;
        return () => stompClient.deactivate();
    }, [uuid]);

    const doMove = (move, counters) => {
        if (stompClientRef.current?.connected && playerColor === currentTurn(state) && joined) {
            stompClientRef.current.publish({
                destination: `/app/${uuid}.makeMove`,
                body: JSON.stringify({ move, blackTime: counters.black, whiteTime: counters.white }),
            });
            return true;
        }
        return false;
    };

    return { state, board, doMove};
};