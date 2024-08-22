// DH.js
export async function generateKeyPair() {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: "DH",
      namedCurve: "P-256", // 椭圆曲线
    },
    true,
    ["deriveKey"]
  );
  return keyPair;
}

export async function exportPublicKey(keyPair) {
  return await window.crypto.subtle.exportKey("spki", keyPair.publicKey);
}

export async function importServerPublicKey(serverPublicKeyData) {
  return await window.crypto.subtle.importKey(
    "spki",
    serverPublicKeyData,
    {
      name: "DH",
      namedCurve: "P-256",
    },
    true,
    []
  );
}

export async function deriveSharedKey(keyPair, serverPublicKey) {
  return await window.crypto.subtle.deriveKey(
    {
      name: "DH",
      public: serverPublicKey,
    },
    keyPair.privateKey,
    { name: "AES-GCM", length: 256 },
    true,
    ["encrypt", "decrypt"]
  );
}

// 将公钥发送到服务器
export async function sendPublicKeyToServer(publicKey) {
  const publicKeyBase64 = btoa(String.fromCharCode(...new Uint8Array(publicKey)));

  const response = await fetch('/api/sendPublicKey', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ publicKey: publicKeyBase64 })
  });

  if (!response.ok) {
    throw new Error('Failed to send public key to server');
  }
}

// 从服务器接收公钥
export async function getServerPublicKey() {
  const response = await fetch('/api/getServerPublicKey', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error('Failed to get server public key');
  }

  const { publicKey: serverPublicKeyBase64 } = await response.json();
  const serverPublicKey = Uint8Array.from(atob(serverPublicKeyBase64), c => c.charCodeAt(0));

  return serverPublicKey.buffer;
}
