<script setup>
import { Download, Lock, User } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { onLogin, onRegister, toSavePrivateKey, validateToken } from '@/service/AuthService.js'
import '@/assets/login_bg.jpg'

// 控制注册与登录表单的显示， 默认显示注册
const isRegister = ref(false);
const isLogin = ref(true);
const formRef = ref(null);
const rememberMe = ref(false);

// 定义数据模型
const registerData = ref({
  username: '',
  password: '',
  rePassword: '',
  public_key: '',
  role:'普通用户'
});
// 定义数据模型
const loginData = ref({
  username: '',
  password: ''
})
onMounted(async () => {
  const token = localStorage.getItem('authToken');
  console.log(token);
  if(token){
    await validateToken(token);
  }
});



// 二次校验密码的函数
const checkRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次确认密码'));
  } else if (value !== registerData.value.password) {
    callback('二次确认密码不相同请重新输入');
  }
};

// 定义表单校验规则
const rules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 16, message: '请输入长度5~16非空字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 16, message: '请输入长度5~16非空字符', trigger: 'blur' },
  ],
  rePassword: [{ validator: checkRePassword, trigger: 'blur' }], // 校验二次输入密码是否相同
});

// 使用文件系统访问 API 保存私钥到指定位置
const savePrivateKey = async () => {
  await toSavePrivateKey();
};

const register = async () => {
  // 校验表单

      try {
        await onRegister(registerData.value);
        console.log('注册成功');
        clearRegisterData(); // 清空表单数据
      } catch (error) {
        console.error('注册失败', error);
      }
};

const login = async () => {

  await onLogin(loginData.value, rememberMe.value);
};

// 定义函数，清空数据模型
const clearRegisterData = () => {
  registerData.value = {
    username: '',
    password: '',
    rePassword: '',
    public_key: '',
    role:'普通用户'
  };
};

const toRegister = () => {
  isRegister.value = true;
  isLogin.value = false;
};

const toLogin = () => {
  isRegister.value = false;
  isLogin.value = true;
};

</script>


<template>
  <el-row class="login-page" justify="center" align="middle" type="flex">
    <el-col :offset="0" class="form" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
      <!-- 注册表单 -->
      <el-form ref="formRef" size="large" autocomplete="off" v-if="isRegister" :model="registerData" :rules="rules">
        <el-form-item class="reg-log_wrapper">
          <h1 class="title">数据流转平台</h1>
        </el-form-item>


        <el-form-item prop="username" class="reg-log">
          <label for="username" class="reg-log_name">用户名</label>
          <el-input class="enter" placeholder="输入用户名" v-model="registerData.username"></el-input>

        </el-form-item>


        <el-form-item prop="password" class="reg-log">
          <label for="password" class="reg-log_name">密码</label>
          <el-input class="enter" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
        </el-form-item>
        <el-form-item prop="rePassword" class="reg-log">
          <label for="rePassword" class="reg-log_name">确认密码</label>
          <el-input class="enter" type="password" placeholder="请再次输入密码" v-model="registerData.rePassword"></el-input>
        </el-form-item>
        <!-- 注册按钮 -->
        <el-form-item class="button-wrapper">
          <el-button class="button" type="primary" auto-insert-space @click="register">
            注册
          </el-button>
        </el-form-item>
          <el-button class="button" id="savePrivateKeyButton" style="display:none;" @click="savePrivateKey">
            保存私钥<el-icon><Download /></el-icon>
          </el-button>
        <el-form-item class="flex-pro">
          <div class="flex-center">
            已有帐户？
            <el-link type="info" :underline="false" @click="toLogin" class="reg-flex">
              返回登录
            </el-link>
          </div>
        </el-form-item>

      </el-form>



      <!-- 登录表单 -->
      <el-form ref="form" size="large" autocomplete="off" v-if="isLogin" :model="loginData" :rules="rules" @submit.prevent="login">
        <el-form-item class="reg-log_wrapper">
          <h1 class="title">数据流转平台</h1>
        </el-form-item>
        <el-form-item prop="username" class="reg-log">
          <label for="username" class="reg-log_name">用户名</label>
          <el-input class="enter" placeholder="请输入用户名" v-model="loginData.username"></el-input>
        </el-form-item>
        <el-form-item prop="password" class="reg-log">
          <label for="password" class="reg-log_name">密码</label>
          <el-input class="enter" type="password" placeholder="请输入密码" v-model="loginData.password"></el-input>
        </el-form-item>

        <el-form-item class="login-flex">
          <div class="login-flex">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
        </el-form-item>


        <!-- 登录按钮 -->
        <el-form-item class="button-wrapper">
          <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
        </el-form-item>

        <el-form-item class="flex-pro">
          <div class="flex-center">
            首次使用？
            <el-link type="info" :underline="false" @click="toRegister" class="reg-flex">
              点我注册
            </el-link>
          </div>
        </el-form-item>

      </el-form>
    </el-col>
  </el-row>
</template>



<style lang="scss" scoped>
/* 样式 */
.login-page {
  /* 初始化 */
  user-select: none;
  overflow-y: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;

  /* 背景 */
  background: url("../assets/login_bg.jpg") no-repeat center center fixed;
  background-size: cover;
  color: #dde5f4;
}

.el-col {
  /* 表单轮廓 */
  background: #f1f7fe;
  padding: 1.5em 2em 3em;
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  box-shadow: 0 0 0.1em #e6e9f9;
  gap: 2em;

  /* 响应式设计 */
  @media (max-width: 768px) {
    padding: 1.5em 1em 2em;
    width: 90%;
  }

  @media (max-width: 480px) {
    width: 100%;
    padding: 1.5em 0.5em;
  }
}

.reg-log_wrapper {
  /* 表头标题 */
  margin: 1em 0 1.5em;
  display: flex;
  justify-content: center;
  width: 100%;
  align-items: center;

  .title {
    margin: auto;
    color: black;
  }
}

.reg-log {
  /* 登录注册表单 */
  background: white;
  box-shadow: 0 0 0.5em #e6e9f9;
  padding: 0.5em 1em 2em;
  display: flex;
  flex-direction: column;
  gap: 0.5em;
  border-radius: 5px;
  color: #4d4d4d;
  margin: 20px 0;
  max-height: 110px;
  overflow-y: hidden;

  .reg-log_name {
    margin: auto;
    height: 32px;
    font-size: 16px;
  }
}

.login-flex {
  /* 登录功能项 */
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin: -0.15em 0.3em;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
  }
}

.flex-pro {
  .flex-center {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin: auto;
    gap: 0.5em; /* 控制间距 */
    color: black;
  }

  .reg-flex {
    font-size: 14px;
    color: #409EFF;
    cursor: pointer;
  }
}

.button-wrapper {
  /* 按钮 */
  display: flex;
  justify-content: center;
  width: 100%;

  .button {
    padding: 1em;
    background: #3e4684;
    color: white;
    border: none;
    border-radius: 5px;
    font-weight: 600;
    width: 95%;
    margin: 1.5em auto;

    @media (max-width: 768px) {
      width: 100%;
    }
  }
}

::v-deep(.el-form-item .el-form-item__error) {
  /* 错误信息 */
  color: red;
  display: flex;
  position: relative;
  margin: auto;
}

::v-deep(.enter .el-input__inner) {
  /* 输入框 */
  text-align: center;
  color: #000;
  font-size: 14px;
}

::v-deep(.enter .el-input__wrapper) {
  border-radius: 5px;
}

#savePrivateKeyButton {
  /* 保存私钥按钮 */
  display: none;
}

.el-link {
  font-size: 14px;
  color: #409EFF;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-col {
    width: 80%;
    padding: 1.5em 1em 2em;
  }

  .reg-log_name {
    font-size: 14px;
  }

  .button {
    padding: 0.8em;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .el-col {
    width: 100%;
    padding: 1.5em 0.5em;
  }

  .reg-log_name {
    font-size: 14px;
  }

  .button {
    padding: 1em;
    font-size: 14px;
  }
}


</style>
