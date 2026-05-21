export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}

export interface BridgeError {
  code: string
  message: string
  details?: Record<string, unknown>
}

export interface BridgeEnvelope<TData> {
  ok: boolean
  requestId: string
  data?: TData
  error?: BridgeError
}

declare global {
  interface Window {
    booBridge?: {
      call(requestJson: string): string
    }
  }
}

function nextRequestId(): string {
  return `req_${Date.now()}_${Math.random().toString(16).slice(2)}`
}

export class WebViewBooBridge implements BooBridge {
  async call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes> {
    if (!window.booBridge?.call) {
      throw new Error("Bridge unavailable")
    }

    const requestId = nextRequestId()
    const raw = window.booBridge.call(JSON.stringify({ namespace, method, payload, requestId }))
    const envelope = JSON.parse(raw) as BridgeEnvelope<TRes>

    if (!envelope.ok) {
      const code = envelope.error?.code ?? "INTERNAL_ERROR"
      const message = envelope.error?.message ?? "Bridge call failed"
      throw new Error(`${code}: ${message}`)
    }

    return envelope.data as TRes
  }
}
