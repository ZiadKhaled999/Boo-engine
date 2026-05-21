import { cpSync, existsSync } from "node:fs";
import { join, resolve } from "node:path";
import { buildWebAssets } from "../build/buildWebAssets";

export function buildAndroidPackage(webDistDir: string, runtimeAndroidDir: string, configPath: string) {
  const assetDir = buildWebAssets(webDistDir, runtimeAndroidDir);
  const appAssetsDir = resolve(runtimeAndroidDir, "app", "src", "main", "assets");

  if (!existsSync(configPath)) {
    throw new Error(`Config file missing: ${configPath}`);
  }

  cpSync(configPath, join(appAssetsDir, "boo.config.json"));
  return { assetDir, configInjected: true };
}
