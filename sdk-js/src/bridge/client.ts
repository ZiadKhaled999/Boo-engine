export type BridgeRequest<TPayload = unknown> = {
  namespace: string
  method: string
  payload?: TPayload
  requestId: string
}

export type BridgeEnvelope<TData = unknown> = {
  ok: boolean
  requestId?: string
  data?: TData
  error?: {
    code?: string
    message?: string
    details?: unknown
  }
}

export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}

type NativeBridgeTarget = {
  call: (requestJson: string) => string
}

const randomId = () => `req_${Date.now()}_${Math.random().toString(16).slice(2)}`

const isTargetAvailable = (target: unknown): target is NativeBridgeTarget => {
  return (
    typeof target === 'object' &&
    target !== null &&
    'call' in target &&
    typeof (target as NativeBridgeTarget).call === 'function'
  )
}

export function createBridge(target: NativeBridgeTarget): BooBridge {
  return {
    async call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes> {
      const request: BridgeRequest<TReq> = { namespace, method, payload, requestId: randomId() }
      const raw = target.call(JSON.stringify(request))
      const parsed = JSON.parse(raw) as BridgeEnvelope<TRes>
      if (!parsed.ok) {
        const code = parsed.error?.code ?? 'INTERNAL_ERROR'
        const message = parsed.error?.message ?? 'Bridge call failed'
        throw new Error(`${code}: ${message}`)
      }
      return parsed.data as TRes
    },
  }
}

export class WebViewBooBridge implements BooBridge {
  async call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes> {
    if (!isTargetAvailable(window.booBridge)) {
      throw new Error('Bridge unavailable')
    }

    return createBridge(window.booBridge).call<TReq, TRes>(namespace, method, payload)
  }
}

declare global {
  interface Window {
    booBridge?: NativeBridgeTarget
  }
}
