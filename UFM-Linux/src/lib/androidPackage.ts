import { open } from "@tauri-apps/plugin-dialog";

/**
 * Opens the native file picker and returns the absolute path of the selected
 * `.apk` / `.xapk` package, or `null` if the user cancelled.
 *
 * On Linux/WebKitGTK the `<input type="file">` element does not expose real
 * filesystem paths (no `File.path`), so a native dialog is required to obtain
 * an absolute path that the Rust upload commands can read.
 */
export async function pickAndroidPackage(): Promise<string | null> {
  const selected = await open({
    multiple: false,
    directory: false,
    filters: [{ name: "Android package", extensions: ["apk", "xapk"] }],
  });
  return typeof selected === "string" ? selected : null;
}

/** Returns the file name component of an absolute path (POSIX or Windows). */
export function filenameFromPath(path: string): string {
  const idx = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
  return idx === -1 ? path : path.slice(idx + 1);
}

export function isApkOrXapk(name: string): boolean {
  const n = name.toLowerCase();
  return n.endsWith(".apk") || n.endsWith(".xapk");
}

export function isXapkFile(name: string): boolean {
  return name.toLowerCase().endsWith(".xapk");
}
