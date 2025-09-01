 
 export const putFen = async(fen) => {
    return await fetch(`${import.meta.env.VITE_API}/projects/chess/start`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fen:fen,
      engine:false
    }),
  });  }

export const getMoves = async(square) =>{
    return await fetch(`${import.meta.env.VITE_API}/projects/chess/getMoves?square=${square}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
}

export const makeMove = async(move) =>{
  return await fetch(`${import.meta.env.VITE_API}/projects/chess/makeMove`,{
    method:'POST',
    headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
      move:move
    }),
  })
}

export const engineMakeMove = async(timeLeft: any,increment: any) =>{
  return await fetch(`${import.meta.env.VITE_API}/projects/chess/makeEngineMove`,{
    method:'POST',
    headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
      timeLeft:timeLeft,
	    increment:increment
    }),
  })
}

export default null