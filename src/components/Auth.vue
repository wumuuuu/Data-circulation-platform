<script setup>
import { onMounted, ref } from 'vue'
import { onLogin, onRegister, validateToken,validateResetCode } from '@/service/AuthService.js'
import '@/assets/login_bg.jpg'
import { ElMessage } from 'element-plus'
// 控制注册与登录表单的显示，默认显示登录
const isRegister = ref(false);
const isLogin = ref(true);
const isForget = ref(false);
const formRef = ref(null);
const form = ref(null);
const forgetFormRef = ref(null);
const rememberMe = ref(false);
const countdown = ref(0); // 验证码倒计时

// 定义数据模型
const registerData = ref({
  username: '',
  password: '',
  rePassword: '',
  public_key: '',
  role:'普通用户',
  securityQuestion:'',
  securityAnswer:''
});

const forgetData = ref({
  username:'',
  code: '',
  newPassword: '',
  securityQuestion:'',
  securityAnswer:'',
  confirmPassword: ''
});

const loginData = ref({
  username: '',
  password: ''
});



onMounted(async () => {
  const token = localStorage.getItem('authToken');
  console.log(token);
  if(token){
    await validateToken(token);
  }
});

// 去除用户名空格
const trimUsername = (username) => {
  return username.replace(/\s+/g, '');
};

// 密码复杂度验证
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'));
    return;
  }

  // 去除空格
  const trimmedValue = value.trim();

  // 长度至少8位
  if (trimmedValue.length < 8) {
    callback(new Error('密码长度至少8位'));
    return;
  }

  // 包含数字
  if (!/\d/.test(trimmedValue)) {
    callback(new Error('密码必须包含数字'));
    return;
  }

  // 包含特殊字符
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(trimmedValue)) {
    callback(new Error('密码必须包含特殊字符'));
    return;
  }

  callback();
};

// 二次校验密码的函数
const checkRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次确认密码'));
  } else if (value !== registerData.value.password) {
    callback(new Error('二次确认密码不相同请重新输入'));
  } else {
    callback();
  }
};

// 用户名验证
const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'));
    return;
  }

  // 去除空格
  const trimmedValue = value.replace(/\s+/g, '');

  // 长度验证
  if (trimmedValue.length < 5 || trimmedValue.length > 16) {
    callback(new Error('用户名长度应为5-16个字符'));
    return;
  }

  // 更新去除空格后的用户名
  if (isRegister.value) {
    registerData.value.username = trimmedValue;
  } else {
    loginData.value.username = trimmedValue;
  }

  callback();
};

// 确认密码验证（用于忘记密码）
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== forgetData.value.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

function validateSecurityQuestion(rule, value, callback) {
  if (value === '') {
    callback(new Error('请选择密保问题'));
  } else {
    callback();
  }
}

const handleSelectVisibleChange1 = (visible) => {
  if (!visible) {
    formRef.value?.validateField('securityQuestion');
  }
};

const handleSelectVisibleChange2 = (visible) => {
  if (!visible) {
    forgetFormRef.value?.validateField('securityQuestion');
  }
};

const rules = ref({
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  rePassword: [{ validator: checkRePassword, trigger: 'blur' }],
  securityQuestion: [
    { validator: validateSecurityQuestion, trigger: ['change', 'blur'] }
  ],
  securityAnswer: [
    { required: true, message: '请填写密保答案', trigger: 'blur' },
    { min: 2, max: 50, message: '密保答案长度应为2-50个字符', trigger: 'blur' }
  ],
});

const loginRules = ref({
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
});

const forgetRules = ref({
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  securityQuestion: [
    { validator: validateSecurityQuestion, trigger: ['change', 'blur'] }
  ],
  securityAnswer: [
    { required: true, message: '请填写密保答案', trigger: 'blur' },
    { min: 2, max: 50, message: '密保答案长度应为2-50个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
});
const register = async () => {
  try {
    formRef.value.validate(async (valid) => {
      if (valid) {
        // 提交前再次去除用户名空格
        registerData.value.username = trimUsername(registerData.value.username);
        if (await onRegister(registerData.value)) {
          toLogin();
        }
      } else {
        return false;
      }
    });

    clearRegisterData();
  } catch (error) {
    console.error('注册失败', error);
  }
};

const ResetCode = async (forgetData) => {
  try {
    forgetFormRef.value.validate(async (valid) => {
      if (valid) {
        const data = await validateResetCode(forgetData);
        if (data === "修改密码成功") {
          ElMessage.success("修改密码成功");
        } else {
          ElMessage.error("修改密码失败");
        }
        clearForgetData();
        toLogin();
      } else {
        return false;
      }
    });
  } catch (error) {
    console.error('修改密码报错', error);
  }
};

const login = async () => {
  form.value.validate(async (valid) => {
    if (valid) {
      // 提交前去除用户名空格
      loginData.value.username = trimUsername(loginData.value.username);
      await onLogin(loginData.value, rememberMe.value);
    } else {
      return false
    }
  });
};

// 清空数据模型
const clearRegisterData = () => {
  registerData.value = {
    username: '',
    password: '',
    rePassword: '',
    public_key: '',
    role:'普通用户',
    securityQuestion:'',
    securityAnswer:''
  };
};

// 清空忘记密码数据
const clearForgetData = () => {
  forgetData.value = {
    securityQuestion: '',
    securityAnswer:'',
    code: '',
    newPassword: '',
    confirmPassword: ''
  };
  countdown.value = 0;
};

const toRegister = () => {
  isRegister.value = true;
  isLogin.value = false;
  isForget.value = false;
  clearForgetData();
};

const toLogin = () => {
  isRegister.value = false;
  isLogin.value = true;
  isForget.value = false;
  clearForgetData();
};

const toForget = () => {
  isRegister.value = false;
  isLogin.value = false;
  isForget.value = true;
  clearRegisterData();
};
</script>


<template>
  <el-row class="login-page" justify="center" align="middle" type="flex">
    <el-col :offset="0" class="form" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
      <!-- 注册表单 -->
      <el-form ref="formRef" size="large" autocomplete="off" v-if="isRegister" :model="registerData" :rules="rules">
        <el-form-item class="reg-log_wrapper">
          <h1 class="title">宁波市民卡联合确权数据流转平台</h1>
        </el-form-item>


        <el-form-item prop="username" class="reg-log">
          <label for="username" class="reg-log_name">用户名</label>
          <el-input class="enter" placeholder="输入用户名" v-model="registerData.username"></el-input>
        </el-form-item>

        <el-form-item prop="securityQuestion" class="reg-log">
          <label for="securityQuestion" class="reg-log_name">密保问题</label>
          <el-select
            class="enter"
            v-model="registerData.securityQuestion"
            placeholder="请选择密保问题"
            clearable
            @visible-change="handleSelectVisibleChange1"
          >
            <el-option label="您母亲的姓名是？" value="您母亲的姓名是？" />
            <el-option label="您父亲的出生地是？" value="您父亲的出生地是？" />
            <el-option label="您的出生城市是？" value="您的出生城市是？" />
            <el-option label="您小学班主任的名字是？" value="您小学班主任的名字是？" />
            <el-option label="您的第一所学校的名字是？" value="您的第一所学校的名字是？" />
            <el-option label="您的第一份工作是在哪里？" value="您的第一份工作是在哪里？" />
            <el-option label="您最喜欢的一本书是？" value="您最喜欢的一本书是？" />
            <el-option label="您最喜欢的电影是？" value="您最喜欢的电影是？" />
            <el-option label="您的宠物名字是？" value="您的宠物名字是？" />
            <el-option label="您大学期间最好的朋友名字是？" value="您大学期间最好的朋友名字是？" />
            <el-option label="您的配偶名字是？" value="您的配偶名字是？" />
            <el-option label="您最喜欢的食物是？" value="您最喜欢的食物是？" />
            <el-option label="您高中班主任的名字是？" value="您高中班主任的名字是？" />
            <el-option label="您第一次出国去的国家是？" value="您第一次出国去的国家是？" />
            <el-option label="您小时候最喜欢的玩具是？" value="您小时候最喜欢的玩具是？" />
            <el-option label="您第一个手机的品牌是？" value="您第一个手机的品牌是？" />
            <el-option label="您最喜欢的运动是？" value="您最喜欢的运动是？" />
            <el-option label="您最喜欢的明星是？" value="您最喜欢的明星是？" />
            <el-option label="您小时候住的街道名称是？" value="您小时候住的街道名称是？" />
            <el-option label="您最喜欢的颜色是？" value="您最喜欢的颜色是？" />
          </el-select>
        </el-form-item>

        <el-form-item prop="securityAnswer" class="reg-log">
          <label for="securityAnswer" class="reg-log_name">密保答案</label>
          <el-input class="enter" placeholder="输入密保答案" v-model="registerData.securityAnswer"></el-input>
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
      <el-form ref="form" size="large" autocomplete="off" v-if="isLogin" :model="loginData" :rules="loginRules" @submit.prevent="login">
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
            <el-link type="primary" :underline="false" @click="toForget" >忘记密码？</el-link>
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

      <!-- 忘记密码表单 -->
      <el-form ref="forgetFormRef" size="large" autocomplete="off" v-if="isForget" :model="forgetData" :rules="forgetRules">
        <el-form-item class="reg-log_wrapper">
          <h1 class="title">数据流转平台</h1>
        </el-form-item>
        <el-form-item prop="username" class="reg-log">
          <label for="username" class="reg-log_name">用户名</label>
          <el-input class="enter" placeholder="请输入用户名" v-model="forgetData.username"></el-input>
        </el-form-item>
        <el-form-item prop="securityQuestion" class="reg-log">
          <label for="securityQuestion" class="reg-log_name">密保问题</label>
          <el-select
            class="enter"
            v-model="forgetData.securityQuestion"
            placeholder="请选择密保问题"
            @visible-change="handleSelectVisibleChange2"
            clearable
          >
            <el-option label="您母亲的姓名是？" value="您母亲的姓名是？" />
            <el-option label="您父亲的出生地是？" value="您父亲的出生地是？" />
            <el-option label="您的出生城市是？" value="您的出生城市是？" />
            <el-option label="您小学班主任的名字是？" value="您小学班主任的名字是？" />
            <el-option label="您的第一所学校的名字是？" value="您的第一所学校的名字是？" />
            <el-option label="您的第一份工作是在哪里？" value="您的第一份工作是在哪里？" />
            <el-option label="您最喜欢的一本书是？" value="您最喜欢的一本书是？" />
            <el-option label="您最喜欢的电影是？" value="您最喜欢的电影是？" />
            <el-option label="您的宠物名字是？" value="您的宠物名字是？" />
            <el-option label="您大学期间最好的朋友名字是？" value="您大学期间最好的朋友名字是？" />
            <el-option label="您的配偶名字是？" value="您的配偶名字是？" />
            <el-option label="您最喜欢的食物是？" value="您最喜欢的食物是？" />
            <el-option label="您高中班主任的名字是？" value="您高中班主任的名字是？" />
            <el-option label="您第一次出国去的国家是？" value="您第一次出国去的国家是？" />
            <el-option label="您小时候最喜欢的玩具是？" value="您小时候最喜欢的玩具是？" />
            <el-option label="您第一个手机的品牌是？" value="您第一个手机的品牌是？" />
            <el-option label="您最喜欢的运动是？" value="您最喜欢的运动是？" />
            <el-option label="您最喜欢的明星是？" value="您最喜欢的明星是？" />
            <el-option label="您小时候住的街道名称是？" value="您小时候住的街道名称是？" />
            <el-option label="您最喜欢的颜色是？" value="您最喜欢的颜色是？" />
          </el-select>
        </el-form-item>

        <el-form-item prop="securityAnswer" class="reg-log">
          <label for="securityAnswer" class="reg-log_name">密保答案</label>
          <el-input class="enter" placeholder="输入密保答案" v-model="forgetData.securityAnswer"></el-input>
        </el-form-item>

        <el-form-item prop="newPassword" class="reg-log">
            <label for="newPassword" class="reg-log_name">新密码</label>
            <el-input
              class="enter"
              type="password"
              placeholder="请输入新密码"
              v-model="forgetData.newPassword"
              show-password>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword" class="reg-log">
            <label for="confirmPassword" class="reg-log_name">确认密码</label>
            <el-input
              class="enter"
              type="password"
              placeholder="请再次输入新密码"
              v-model="forgetData.confirmPassword"
              show-password>
            </el-input>
          </el-form-item>

        <el-form-item class="button-wrapper">
          <el-button
            class="button"
            type="primary"
            auto-insert-space
            @click="ResetCode(forgetData) ">提交
          </el-button>
        </el-form-item>

        <el-form-item class="flex-pro">
          <div class="flex-center">
            <el-link type="info" :underline="false" @click="toLogin" class="reg-flex">
              返回登录
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
  height: 150vh;

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

::v-deep(.enter .el-select__selected-item) {
  justify-content: center !important; /* 让文字居中显示 */
  text-align: center;
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
