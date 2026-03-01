import React, { useState } from 'react';
import { ChessMode } from './chess/chessCommon.jsx';

const Navbar = ({ onSelectMode }) => {
  const [activeDropdown, setActiveDropdown] = useState(null);

  const navConfig = [
    { label: 'Home', href: '/' },
    {
      label: 'Play Chess',
      type: 'dropdown',
      options: [
        { label: 'Local Game', mode: ChessMode.LOCAL },
        { label: 'Against Engine', mode: ChessMode.ENGINE },
        { label: 'Online Multiplayer', mode: ChessMode.ONLINE },
      ],
    },
  ];

  const handleModeClick = (mode) => {
    onSelectMode(mode);
    setActiveDropdown(null);
  };

  return (
    <nav className="fixed top-0 left-0 right-0 bg-slate-900 border-b border-slate-800 text-slate-200 z-[100] h-16 shadow-lg">
      <div className="max-w-7xl mx-auto h-full px-4 flex items-center justify-between">
        
        <div className="flex items-center gap-8">
          <div className="text-xl font-bold text-white flex items-center gap-2 cursor-pointer">
            <div className="bg-blue-600 p-1 rounded shadow-inner">♟</div>
            <span className="tracking-tight">Grandmaster<span className="text-blue-500">App</span></span>
          </div>

          <div className="hidden md:flex items-center gap-1">
            {navConfig.map((item, idx) => (
              <div key={idx} className="relative group">
                {item.type === 'dropdown' ? (
                  <button
                    onClick={() => setActiveDropdown(activeDropdown === idx ? null : idx)}
                    className={`px-4 py-2 rounded-md hover:bg-slate-800 transition-all flex items-center gap-1 ${activeDropdown === idx ? 'text-blue-400 bg-slate-800' : ''}`}
                  >
                    {item.label}
                    <svg className={`w-4 h-4 transition-transform ${activeDropdown === idx ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" /></svg>
                  </button>
                ) : (
                  <a href={item.href} className="px-4 py-2 rounded-md hover:bg-slate-800 hover:text-white transition-all">
                    {item.label}
                  </a>
                )}

                {item.type === 'dropdown' && activeDropdown === idx && (
                  <>
                    <div className="fixed inset-0 z-[-1]" onClick={() => setActiveDropdown(null)} />
                    <div className="absolute top-full left-0 mt-2 w-56 bg-slate-800 border border-slate-700 rounded-xl shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
                      <div className="p-2">
                        {item.options.map((opt, oIdx) => (
                          <button
                            key={oIdx}
                            onClick={() => handleModeClick(opt.mode)}
                            className="w-full text-left px-4 py-3 text-sm rounded-lg hover:bg-blue-600 hover:text-white transition-colors flex items-center justify-between group/item"
                          >
                            {opt.label}
                            <span className="opacity-0 group-hover/item:opacity-100 transition-opacity">→</span>
                          </button>
                        ))}
                      </div>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button className="p-2 text-slate-400 hover:text-white transition-colors">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
          </button>
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 border border-slate-700 cursor-pointer" />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;