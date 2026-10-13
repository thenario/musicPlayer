import { sha256 } from '@noble/hashes/sha2.js'
import { bytesToHex } from '@noble/hashes/utils.js'

/** SHA-256 十六进制哈希，兼容局域网 HTTP 的非安全上下文。 */
export async function encryptPassword(password: string): Promise<string> {
  const msgUint8 = new TextEncoder().encode(password)
  const subtle = globalThis.crypto?.subtle
  if (subtle) {
    const hashBuffer = await subtle.digest('SHA-256', msgUint8)
    return bytesToHex(new Uint8Array(hashBuffer))
  }
  return bytesToHex(sha256(msgUint8))
}
