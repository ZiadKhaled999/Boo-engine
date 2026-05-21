import { cpSync, mkdirSync, rmSync } from "node:fs";
import { resolve } from "node:path";

export function buildWebAssets(webDistDir: string, outDir: string): string {
  const source = resolve(webDistDir);
  const destination = resolve(outDir, "assets", "www");
  rmSync(destination, { recursive: true, force: true });
  mkdirSync(destination, { recursive: true });
  cpSync(source, destination, { recursive: true });
  return destination;
}
