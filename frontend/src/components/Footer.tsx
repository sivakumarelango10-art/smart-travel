import React from 'react';
import { ShieldCheck, Terminal } from 'lucide-react';
import { APP_NAME, APP_VERSION } from '../config/constants';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-slate-800/80 bg-slate-950 py-8 text-slate-400 text-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>{APP_NAME} — Production Architecture & Foundation</span>
          <span className="text-slate-600">|</span>
          <span className="font-mono text-slate-500">v{APP_VERSION}</span>
        </div>

        <div className="flex items-center gap-4 text-slate-500">
          <a
            href={import.meta.env.VITE_SWAGGER_URL || '/swagger-ui.html'}
            target="_blank"
            rel="noreferrer"
            className="hover:text-sky-400 transition flex items-center gap-1 font-mono"
          >
            <Terminal className="w-3.5 h-3.5" />
            Swagger Docs
          </a>
          <span>•</span>
          <span>Java 21 • Spring Boot 3.3 • MongoDB • React 18</span>
        </div>
      </div>
    </footer>
  );
};
