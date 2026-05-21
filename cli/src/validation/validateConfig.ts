import { readFileSync } from "node:fs";

export interface ConfigValidationResult {
  ok: boolean;
  errors: string[];
}

export function validateConfig(configPath: string): ConfigValidationResult {
  const body = JSON.parse(readFileSync(configPath, "utf8"));
  const errors: string[] = [];

  const required = ["appId", "appName", "startUrl", "permissions", "plugins"];
  for (const field of required) {
    if (!(field in body)) errors.push(`Missing required field: ${field}`);
  }

  if (body.permissions && !Array.isArray(body.permissions)) {
    errors.push("permissions must be an array");
  }

  if (body.plugins && typeof body.plugins !== "object") {
    errors.push("plugins must be an object");
  }

  return { ok: errors.length === 0, errors };
}
