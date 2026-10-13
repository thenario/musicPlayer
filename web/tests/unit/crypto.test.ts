import { afterEach, describe, expect, it, vi } from 'vitest'
import { encryptPassword } from '@/utils/crypto'

const vectors: [string, string][] = [
  ['', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'],
  ['abc', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad'],
  ['password123', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'],
  ['密码🎵', '656c107e57ea663a15e580ff16043102b34131404ee79cd157aa13f866d75268'],
  ['x'.repeat(200), 'aa20c23e3201834050679e1d88941b9a6fed0557c9a705cb2c315e2e63fd486d'],
]

afterEach(() => vi.unstubAllGlobals())

describe('encryptPassword', () => {
  it.each([
    ['HTTPS / localhost', globalThis.crypto],
    ['LAN HTTP without SubtleCrypto', {}],
    ['browser without Crypto', undefined],
  ])('matches SHA-256 in %s', async (_context, cryptoApi) => {
    vi.stubGlobal('crypto', cryptoApi)
    for (const [password, expected] of vectors) {
      await expect(encryptPassword(password)).resolves.toBe(expected)
    }
  })
})
