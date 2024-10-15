import { get, post } from '@/utils/request.js'

export async function fetchTask(userName) {
  try {

    // 在请求中传递 creatorName 作为查询参数
    const response = await get(`/task/signTask?userName=${encodeURIComponent(userName)}`);

    // 检查响应是否成功
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || 'Failed to fetch Tasks');
    }
  } catch (error) {
    console.error('Error fetching Tasks:', error);
    throw error; // 抛出错误，便于调用方捕获
  }
}

export async function calculate(file, Data, username) {

  const p = BigInt('132165373947571709001890899559578394061572732290158236845675979056783176833192189640519330577968623712019753279011546461561086378291703395170828826203868040544703192493236905634659492348075654172349595065574318562378095706622284475060330389667603958501055142626804746804365447731489915179943331725842802927799');
  let y = BigInt(Data.y);
  let b = BigInt(Data.b);

  const pemContent = await readFileContent(file);
  const privateKey = BigInt(await extractKeyFromPem(pemContent));
  y = BigInt(await modularExponentiation(y, privateKey, p));
  b = BigInt(await modularExponentiation(b, privateKey, p));

  const response = await post('/task/update', {
    taskId: Data.taskId,
    username: username,
    y: y.toString(),
    b: b.toString(),
  });

}

// 用于读取文件内容的异步函数
function readFileContent(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      resolve(reader.result); // 成功读取文件，返回文件内容
    };

    reader.onerror = (error) => {
      reject(error); // 如果读取出错，拒绝 Promise
    };

    // 读取文件内容为文本
    reader.readAsText(file);
  });
}

async function extractKeyFromPem (pem) {
  // 去除 PEM 的头和尾部分
  const pemContents = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, '')
    .replace(/-----END PRIVATE KEY-----/, '')
    .replace(/\s+/g, ''); // 移除所有空白字符（换行、空格）

  return pemContents; // 返回密钥的字节数组，或者你可以根据需要进一步处理
}

async function modularExponentiation(base, exponent, modulus) {

  let result = BigInt(1);
  base = base % modulus;

  while (exponent > 0) {
    if (exponent % BigInt(2) === BigInt(1)) {
      result = (result * base) % modulus;
    }
    exponent = exponent >> BigInt(1); // 将指数右移一位（等同于除以2）
    base = (base * base) % modulus;
  }
  return result;
}