import type { BooBridge } from '../bridge/client'

export function createAppPlugin(bridge: BooBridge) {
  return {
    readMetadata: () =>
      bridge.call<undefined, { appId: string; appName: string; startUrl: string }>(
        'app',
        'readMetadata',
        undefined
      ),
    readPermissions: () => bridge.call<undefined, string[]>('app', 'readPermissions', undefined),
  }
}
