import React, { useEffect, useRef, useState } from "react";
import { DownloadCloud, AlertTriangle } from "lucide-react";
import { getCurrentWebview } from "@tauri-apps/api/webview";
import { pickAndroidPackage, filenameFromPath, isApkOrXapk, isXapkFile } from "../lib/androidPackage";

interface SideloadZoneProps {
  onSideload: (localPath: string, filename: string, isXapk: boolean) => void;
  disabled: boolean;
}

export const SideloadZone: React.FC<SideloadZoneProps> = ({ onSideload, disabled }) => {
  const [isDragging, setIsDragging] = useState(false);
  const zoneRef = useRef<HTMLDivElement>(null);
  const disabledRef = useRef(disabled);
  const sideloadRef = useRef(onSideload);

  useEffect(() => {
    disabledRef.current = disabled;
  }, [disabled]);

  useEffect(() => {
    sideloadRef.current = onSideload;
  }, [onSideload]);

  // On Linux/WebKitGTK, Tauri intercepts OS file drops and emits webview
  // drag-drop events instead of DOM drop events, and `File.path` is never
  // populated. We therefore listen to the Tauri event and use `paths`.
  useEffect(() => {
    let active = true;
    let unlisten: (() => void) | undefined;

    // Positions arrive in physical pixels relative to the webview's top-left.
    const isOverZone = (x: number, y: number) => {
      const el = zoneRef.current;
      if (!el) return false;
      const r = el.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;
      const cx = x / dpr;
      const cy = y / dpr;
      return cx >= r.left && cx <= r.right && cy >= r.top && cy <= r.bottom;
    };

    const processPath = (path: string) => {
      const filename = filenameFromPath(path);
      if (!isApkOrXapk(filename)) {
        alert("Invalid file format. Please drop a .apk or .xapk package.");
        return;
      }
      sideloadRef.current(path, filename, isXapkFile(filename));
    };

    getCurrentWebview()
      .onDragDropEvent((event) => {
        const p = event.payload;
        if (p.type === "leave") {
          setIsDragging(false);
          return;
        }
        if (disabledRef.current) {
          setIsDragging(false);
          return;
        }
        const over = isOverZone(p.position.x, p.position.y);
        if (p.type === "enter" || p.type === "over") {
          setIsDragging(over);
        } else if (p.type === "drop") {
          setIsDragging(false);
          if (over && p.paths.length > 0) {
            processPath(p.paths[0]);
          }
        }
      })
      .then((fn) => {
        if (active) {
          unlisten = fn;
        } else {
          fn();
        }
      });

    return () => {
      active = false;
      unlisten?.();
    };
  }, []);

  const handleClick = async () => {
    if (disabled) return;
    const path = await pickAndroidPackage();
    if (!path) return;
    const filename = filenameFromPath(path);
    if (!isApkOrXapk(filename)) {
      alert("Invalid file format. Please choose a .apk or .xapk package.");
      return;
    }
    onSideload(path, filename, isXapkFile(filename));
  };

  return (
    <div
      ref={zoneRef}
      className={`sideload-zone glass-panel ${isDragging ? "dragging" : ""}`}
      onClick={handleClick}
      style={{
        opacity: disabled ? 0.5 : 1,
        cursor: disabled ? "not-allowed" : "pointer"
      }}
    >
      <DownloadCloud className="sideload-icon" />
      <div className="sideload-title">
        {disabled ? "Connect to a Device to Install Apps" : "Remote App Sideload Zone"}
      </div>
      <div className="sideload-desc">
        {disabled 
          ? "Establish connection first" 
          : "Drag & drop a .apk or .xapk here, or click to browse"
        }
      </div>
      {!disabled && (
        <div style={{ display: "flex", alignItems: "center", gap: "6px", marginTop: "12px", color: "var(--text-sec)", fontSize: "0.7rem" }}>
          <AlertTriangle style={{ width: 12, height: 12, color: "var(--warning)" }} />
          Triggers installation prompt on the target Phone or TV screen.
        </div>
      )}
    </div>
  );
};
