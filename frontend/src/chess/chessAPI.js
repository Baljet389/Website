
 export const putFen = async(fen, mode, variation, timeLeft, increment, white) => {
    return await fetch(`${import.meta.env.VITE_API}/api/chess/start`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fen: fen,
      mode: mode,
      variation: variation,
      timeLeft: timeLeft,
      increment: increment,
      white: white
    }),
  });  }

export const getMoves = async(square,uuid) =>{
    return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/getMoves?square=${square}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
}

export const makeMove = async(move,uuid) =>{
  return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/makeMove`,{
    method:'POST',
    headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
      move:move
    }),
  })
}

export const engineMakeMove = async(timeLeft, increment, uuid) =>{
  return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/makeEngineMove`,{
    method:'POST',
    headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
      timeLeft:timeLeft,
	    increment:increment
    }),
  })
}
export const deleteGame = async(uuid) =>{
	 return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/deleteGame`,{
    method:'DELETE',
    headers: { 'Content-Type': 'application/json' }
  });
}

export const getGameState = async(uuid) =>{
  return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/getGameState`,{
    method:'GET',
    headers:{'Content-Type':'application/json'}
  });
}
export const getGameInfo = async(uuid) =>{
  return await fetch(`${import.meta.env.VITE_API}/api/chess/${uuid}/getGameInfo`,{
    method:'GET',
    headers:{'Content-Type':'application/json'}
  });
}

export default null