import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { WebViewBooBridge, BridgeEnvelope } from './client'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeEnvelope<T>(ok: boolean, data?: T, code?: string, message?: string): string {
  const envelope: BridgeEnvelope<T> = {
    ok,
    requestId: 'req_test',
    ...(ok ? { data } : { error: { code: code ?? 'ERR', message: message ?? 'error msg' } }),
  }
  return JSON.stringify(envelope)
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('WebViewBooBridge', () => {
  let bridge: WebViewBooBridge

  beforeEach(() => {
    bridge = new WebViewBooBridge()
    // Reset window.booBridge before each test
    delete (window as Window & { booBridge?: unknown }).booBridge
  })

  afterEach(() => {
    vi.restoreAllMocks()
    delete (window as Window & { booBridge?: unknown }).booBridge
  })

  // -------------------------------------------------------------------------
  // Bridge availability guards
  // -------------------------------------------------------------------------

  it('throws "Bridge unavailable" when window.booBridge is not set', async () => {
    await expect(bridge.call('system', 'ping', {})).rejects.toThrow('Bridge unavailable')
  })

  it('throws "Bridge unavailable" when window.booBridge.call is not a function', async () => {
    ;(window as Window & { booBridge: unknown }).booBridge = {}
    await expect(bridge.call('system', 'ping', {})).rejects.toThrow('Bridge unavailable')
  })

  it('throws "Bridge unavailable" when window.booBridge is explicitly undefined', async () => {
    ;(window as Window & { booBridge: unknown }).booBridge = undefined
    await expect(bridge.call('system', 'ping', {})).rejects.toThrow('Bridge unavailable')
  })

  // -------------------------------------------------------------------------
  // Request serialisation
  // -------------------------------------------------------------------------

  it('passes namespace, method, and payload to the native bridge', async () => {
    let capturedJson: string | undefined
    window.booBridge = {
      call: (json: string) => {
        capturedJson = json
        return makeEnvelope(true, { ack: true })
      },
    }

    await bridge.call('system', 'ping', { extra: 42 })

    expect(capturedJson).toBeDefined()
    const parsed = JSON.parse(capturedJson!)
    expect(parsed.namespace).toBe('system')
    expect(parsed.method).toBe('ping')
    expect(parsed.payload).toEqual({ extra: 42 })
  })

  it('includes a requestId in every outbound call', async () => {
    let capturedJson: string | undefined
    window.booBridge = {
      call: (json: string) => {
        capturedJson = json
        return makeEnvelope(true, {})
      },
    }

    await bridge.call('system', 'ping', {})

    const parsed = JSON.parse(capturedJson!)
    expect(typeof parsed.requestId).toBe('string')
    expect(parsed.requestId.length).toBeGreaterThan(0)
  })

  it('requestId starts with "req_"', async () => {
    let capturedJson: string | undefined
    window.booBridge = {
      call: (json: string) => {
        capturedJson = json
        return makeEnvelope(true, {})
      },
    }

    await bridge.call('system', 'ping', {})

    const parsed = JSON.parse(capturedJson!)
    expect(parsed.requestId).toMatch(/^req_/)
  })

  it('generates unique requestIds across consecutive calls', async () => {
    const ids: string[] = []
    window.booBridge = {
      call: (json: string) => {
        ids.push(JSON.parse(json).requestId as string)
        return makeEnvelope(true, {})
      },
    }

    await bridge.call('system', 'ping', {})
    await bridge.call('system', 'ping', {})
    await bridge.call('system', 'ping', {})

    const unique = new Set(ids)
    expect(unique.size).toBe(3)
  })

  it('passes null payload correctly', async () => {
    let capturedJson: string | undefined
    window.booBridge = {
      call: (json: string) => {
        capturedJson = json
        return makeEnvelope(true, null)
      },
    }

    await bridge.call<null, unknown>('system', 'ping', null)

    const parsed = JSON.parse(capturedJson!)
    expect(parsed.payload).toBeNull()
  })

  // -------------------------------------------------------------------------
  // Successful response handling
  // -------------------------------------------------------------------------

  it('returns data from a successful envelope', async () => {
    window.booBridge = {
      call: () => makeEnvelope(true, { ack: true }),
    }

    const result = await bridge.call<object, { ack: boolean }>('system', 'ping', {})

    expect(result).toEqual({ ack: true })
  })

  it('returns complex nested data from a successful envelope', async () => {
    const responseData = { user: { id: 1, name: 'Alice' }, items: [1, 2, 3] }
    window.booBridge = {
      call: () => makeEnvelope(true, responseData),
    }

    const result = await bridge.call<object, typeof responseData>('api', 'getUser', {})

    expect(result).toEqual(responseData)
  })

  it('returns undefined data when envelope data field is absent but ok is true', async () => {
    window.booBridge = {
      call: () => JSON.stringify({ ok: true, requestId: 'req_test' }),
    }

    const result = await bridge.call<object, undefined>('system', 'ping', {})

    expect(result).toBeUndefined()
  })

  // -------------------------------------------------------------------------
  // Error response handling
  // -------------------------------------------------------------------------

  it('throws an error when envelope ok is false', async () => {
    window.booBridge = {
      call: () => makeEnvelope(false, undefined, 'VALIDATION_ERROR', 'Invalid payload'),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow()
  })

  it('throws with "CODE: message" format when ok is false', async () => {
    window.booBridge = {
      call: () => makeEnvelope(false, undefined, 'VALIDATION_ERROR', 'Invalid payload'),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow(
      'VALIDATION_ERROR: Invalid payload'
    )
  })

  it('throws with RUNTIME_NOT_READY code when bridge reports runtime not ready', async () => {
    window.booBridge = {
      call: () => makeEnvelope(false, undefined, 'RUNTIME_NOT_READY', 'Runtime is not ready'),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow(
      'RUNTIME_NOT_READY: Runtime is not ready'
    )
  })

  it('uses INTERNAL_ERROR as fallback code when error field is absent', async () => {
    window.booBridge = {
      call: () => JSON.stringify({ ok: false, requestId: 'req_test' }),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow('INTERNAL_ERROR:')
  })

  it('uses "Bridge call failed" as fallback message when error.message is absent', async () => {
    window.booBridge = {
      call: () => JSON.stringify({ ok: false, requestId: 'req_test' }),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow(
      'INTERNAL_ERROR: Bridge call failed'
    )
  })

  it('uses error code from envelope when present, even if message is missing', async () => {
    window.booBridge = {
      call: () => JSON.stringify({ ok: false, requestId: 'req_test', error: { code: 'MY_CODE' } }),
    }

    await expect(bridge.call('system', 'ping', {})).rejects.toThrow('MY_CODE:')
  })

  // -------------------------------------------------------------------------
  // Edge cases and regression guards
  // -------------------------------------------------------------------------

  it('does not mutate the payload object passed in', async () => {
    const payload = { value: 42 }
    const original = { ...payload }
    window.booBridge = {
      call: () => makeEnvelope(true, {}),
    }

    await bridge.call('system', 'ping', payload)

    expect(payload).toEqual(original)
  })

  it('handles a completely empty payload object', async () => {
    window.booBridge = {
      call: () => makeEnvelope(true, {}),
    }

    await expect(bridge.call('system', 'ping', {})).resolves.toEqual({})
  })

  it('handles the bridge returning a JSON array (ok=true, data is array)', async () => {
    window.booBridge = {
      call: () => JSON.stringify({ ok: true, requestId: 'req_test', data: [1, 2, 3] }),
    }

    const result = await bridge.call<object, number[]>('system', 'list', {})

    expect(result).toEqual([1, 2, 3])
  })
})