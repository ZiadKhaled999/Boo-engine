export type BridgeRequest<TPayload = unknown> = {
  namespace: string;
  method: string;
  payload?: TPayload;
  requestId: string;
};

export type BridgeResult<TData = unknown> =
  | { ok: true; data: TData; requestId?: string }
  | { ok: false; error: { code: string; message: string; details?: unknown }; requestId?: string };

export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>;
}

const randomId = () => `req_${Date.now()}_${Math.random().toString(16).slice(2)}`;

export function createBridge(target: { call: (requestJson: string) => string }): BooBridge {
  return {
    async call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes> {
      const request: BridgeRequest<TReq> = { namespace, method, payload, requestId: randomId() };
      const raw = target.call(JSON.stringify(request));
      const parsed = JSON.parse(raw) as BridgeResult<TRes>;
      if (!parsed.ok) {
        throw new Error(`${parsed.error.code}: ${parsed.error.message}`);
      }
      return parsed.data;
    },
  };
}

export * from "./plugins";
