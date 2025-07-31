 
 export const putFen = async(fen) => {
    return await fetch(`${import.meta.env.VITE_API}/projects/chess/start`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fen:fen
    }),
  });  }