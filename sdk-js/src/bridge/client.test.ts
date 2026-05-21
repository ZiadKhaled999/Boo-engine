import test from 'node:test'
import assert from 'node:assert/strict'
import { WebViewBooBridge } from './client.ts'
import type { BridgeEnvelope } from './client.ts'

function makeEnvelope<T>(ok: boolean, data?: T, code?: string, message?: string): string {
  const envelope: BridgeEnvelope<T> = {
    ok,
    requestId: 'req_test',
    ...(ok ? { data } : { error: { code: code ?? 'ERR', message: message ?? 'error msg' } }),
  }
  return JSON.stringify(envelope)
}

function createWindow() {
  ;(globalThis as typeof globalThis & { window: Window }).window = {} as Window
}

test('WebViewBooBridge', async (t) => {
  await t.test('throws "Bridge unavailable" when window.booBridge is not set', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    await assert.rejects(bridge.call('system', 'ping', {}), /Bridge unavailable/)
  })

  await t.test('throws "Bridge unavailable" when window.booBridge.call is not a function', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    ;(window as Window & { booBridge: unknown }).booBridge = {}
    await assert.rejects(bridge.call('system', 'ping', {}), /Bridge unavailable/)
  })

  await t.test('passes namespace, method, and payload to the native bridge', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    let capturedJson: string | undefined

    window.booBridge = {
      call: (json: string) => {
        capturedJson = json
        return makeEnvelope(true, { ack: true })
      },
    }

    await bridge.call('system', 'ping', { extra: 42 })
    assert.ok(capturedJson)
    const parsed = JSON.parse(capturedJson)
    assert.equal(parsed.namespace, 'system')
    assert.equal(parsed.method, 'ping')
    assert.deepEqual(parsed.payload, { extra: 42 })
  })

  await t.test('includes a requestId in every outbound call', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    let capturedJson: string | undefined
    window.booBridge = { call: (json: string) => ((capturedJson = json), makeEnvelope(true, {})) }

    await bridge.call('system', 'ping', {})

    assert.ok(capturedJson)
    const parsed = JSON.parse(capturedJson)
    assert.equal(typeof parsed.requestId, 'string')
    assert.ok(parsed.requestId.length > 0)
  })

  await t.test('returns data from a successful envelope', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    window.booBridge = { call: () => makeEnvelope(true, { ack: true }) }
    const result = await bridge.call<object, { ack: boolean }>('system', 'ping', {})
    assert.deepEqual(result, { ack: true })
  })

  await t.test('throws with fallback code and message when envelope error fields are absent', async () => {
    createWindow()
    const bridge = new WebViewBooBridge()
    window.booBridge = { call: () => JSON.stringify({ ok: false, requestId: 'req_test' }) }
    await assert.rejects(bridge.call('system', 'ping', {}), /INTERNAL_ERROR: Bridge call failed/)
  })
})
