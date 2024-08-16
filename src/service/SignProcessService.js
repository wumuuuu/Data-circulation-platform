import CryptoJS from 'crypto-js';
import {modularExponentiation, p, g} from '../utils/CountKey.js'
import bigInt from 'big-integer'

// 开启签名流程
export const OpenTheSignProcess = async (form) => {

  let key = form.value.keyList[0];
  key = BigInt(await readFileContent(key));

  //将数据和待签名参数m拼接后hash
  const H_hex = await hash_m(form); // 获取十六进制格式的哈希值
  const H = BigInt(`0x${H_hex}`); // 将十六进制字符串转换为 BigInt

  const x = modularExponentiation(H, BigInt(2), p);
  const y = modularExponentiation(H, key, p);
  const b = modularExponentiation(g, key, p);

  console.log("x = " + x + ", y = " + y + ", b = " + b);
};

// 计算 hash_m
export const hash_m = async (form) => {
  let file = form.value.fileList[0]; // 获取第一个文件
  let signers = form.value.signer;   // 获取签名者列表

  // 读取文件内容
  let content = await readFileContent(file);

  // 拼接签名者的用户名
  for (let signer of signers) {
    if (signer.username) {
      content += signer.username;
    }
  }

  // 计算拼接字符串的哈希值
  const hash = CryptoJS.SHA256(content);  // 使用拼接后的 content
  const H = hash.toString(CryptoJS.enc.Hex);  // 将哈希值转换为十六进制字符串
  form.value.x = H;  // 如果需要将 x 存储在 form 中
  return H;  // 返回 x
};

// 辅助函数：读取文件内容并返回为字符串
const readFileContent = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(reader.error);
    reader.readAsText(file); // 将文件内容读取为文本
  });
};
