import CryptoJS from 'crypto-js'

// 定义大数
const p = BigInt('132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799');
const g = BigInt('436921');

/**
 * 生成私钥
 * @returns {bigint} 私钥
 */
export const generatePrivateKey = () => {
  // 生成 1024 位的随机私钥
  const privateKey = CryptoJS.lib.WordArray.random(128).toString(CryptoJS.enc.Hex); // 1024 / 8 = 128 bytes
  const testPrivateKey = CryptoJS.lib.WordArray.random(16).toString(CryptoJS.enc.Hex); // 16 bytes
  return BigInt('0x' + privateKey);
};

/**
 * 计算公钥
 * @returns {string} 公钥
 */
export const calculatePublicKey = (privateKey) => {
  // 使用模幂计算
  const PublicKey = modularExponentiation(g, privateKey, p);
  return PublicKey.toString();
};

// 模幂运算函数
export const modularExponentiation = (base, exponent, modulus) => {
  let result = BigInt(1);
  base = base % modulus;

  while (exponent > 0) {
    if (exponent % BigInt(2) === BigInt(1)) {
      result = (result * base) % modulus;
    }
    exponent = exponent >> BigInt(1); // 等于 exponent / 2
    base = (base * base) % modulus;
  }
  return result;
};