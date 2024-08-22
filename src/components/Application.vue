<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { fApplicationStatus, handleSubmit } from '@/service/ApplicationAPI.js'
import { SignTaskUpdates } from '../service/wsService.js'
import { CountKey } from '@/service/SignProcessService.js'
import { ElMessage } from 'element-plus'

const activeMenu = ref('2');
const router = useRouter();

// 在组件挂载后自动连接 WebSocket
onMounted(() => {
  fetchApplicationStatus();
});

const applyStep1 = ref(null);
const applyStep2 = ref(null);
const applyStep3 = ref(null);
const username = localStorage.getItem('username');

const fetchApplicationStatus = async () => {
  await fApplicationStatus(applyStep1, applyStep2, applyStep3);
};

//点击登出并跳转到登录界面
const handleCommand = (command) => {
  if (command === 'logout') {
    // 清理登录状态，例如移除 token 或用户信息
    localStorage.clear();
    // 然后跳转到登录页面
    router.push('/');
  }
};
const handleSelect = (index) => {
  // 根据 index 处理导航，例如跳转到不同的页面
  switch (index) {
    case '1':
      router.push('/home');
      break;
    case '2':
      router.push('/Application');
      break;
    case '3':
      router.push('/approvalProcess');
      break;
  }
};

const formData1 = ref({
  text: '', // 用户提交的申请文本
  dateTimeRange: [] // 用户选择的日期和时间范围
});
const formData2 = ref({
  text: '', // 用户提交的申请文本
  dateTimeRange: [] // 用户选择的日期和时间范围
});
const formData3 = ref({
  text: '', // 用户提交的申请文本
  dateTimeRange: [] // 用户选择的日期和时间范围
});

// 提交表单
const onSubmit = async (formData, Type) => {
  await handleSubmit(formData, Type);
}

// 重置表单
const onReset = (formData) => {
  formData.text = '';
  formData.dateTimeRange = null;
};

//添加私钥进行计算
const handleKeySelection = async(file) => {
  await CountKey(file);
  return false; // 阻止自动上传
};

// 检查当前用户是否正在签名
const isCurrentUserSigning = () => {
  const currentUser = localStorage.getItem('username');
  return SignTaskUpdates.value.some(task => task.username === currentUser && task.status === '正在签名');
}
</script>
<template>
  <el-container style="height: 100vh; width: 100%;">
    <!-- 侧边栏 -->
    <el-aside width="205px" class="custom-aside">
      <div class="logo"> <strong>数据流转平台</strong></div>
      <el-menu :default-active="activeMenu" class="custom-menu" @select="handleSelect">
        <el-menu-item index="1">
          <span >主页</span>
        </el-menu-item>
        <el-menu-item index="2">
          <span>申请</span>
        </el-menu-item>
        <el-menu-item index="3">
          <span>审批</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header>
        <el-dropdown @command="handleCommand">
          <el-avatar> {{username}} </el-avatar>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">登出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 主内容区 -->
      <el-container >
        <!-- 右侧未申请流程展示区 -->
        <el-aside width="100%" style="padding: 20px;">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card style="height: 87vh; display: flex; flex-direction: column; justify-content: center; align-items: center;" v-if="applyStep1 === 0" >
                <div style="height: 65vh; overflow: auto;" >
                  <div class = "sign">
                    提交签名申请
                  </div>
                  <el-divider />
                  <el-form style="width: 100%" >
                    <el-input style="height: 30vh" type="textarea" :rows="10" placeholder="说明此次申请"  v-model="formData1.text">
                    </el-input>
                    <div>
                      <el-date-picker
                        v-model="formData1.dateTimeRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                      ></el-date-picker>
                    </div>
                    <el-divider />
                  </el-form>
                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit(formData1,'签名申请')">提交</el-button>
                      <el-button @click="onReset(formData1)">重置</el-button>
                    </el-col>
                  </el-row>
                </div>

              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep1 === 1">
                <!-- 等待审批界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      等待管理员审批
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep1 === 2">
                <!-- 等待流程开始界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      申请已通过等待流程开始
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep1 === 3">
                <div class = "sign">
                  正在进行的签名
                </div>
                <el-divider />
                <div style="height: 60vh; overflow: auto;">
                  <el-table :data="SignTaskUpdates" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="username" label="用户名" align="center" />
                    <el-table-column prop="status" label="状态" align="center"/>
                  </el-table>

                </div>
                <el-row>
                  <el-col :span="24" class="input-col">
                    <div v-if="!isCurrentUserSigning()" style="margin-bottom: 10px; font-size: 15px; color: dodgerblue">还未轮到您签名</div>
                    <el-upload class="upload-demo" :before-upload="handleKeySelection">
                      <el-button type="primary" plain style="width: 100%;" :disabled="!isCurrentUserSigning()">
                        添加私钥计算
                      </el-button>
                    </el-upload>
                  </el-col>
                </el-row>

              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card style="height: 87vh; display: flex; flex-direction: column; justify-content: center; align-items: center;" v-if="applyStep2 === 0" >
                <div style="height: 65vh; overflow: auto;" >
                  <div class = "sign">
                    提交确权申请
                  </div>
                  <el-divider />
                  <el-form style="width: 100%" >
                    <el-input style="height: 30vh" type="textarea" :rows="10" placeholder="说明此次申请" v-model="formData2.text">
                    </el-input>
                    <div>
                      <el-date-picker
                        v-model="formData2.dateTimeRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                      ></el-date-picker>
                    </div>
                    <el-divider />
                  </el-form>
                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit(formData2,'确权申请')">提交</el-button>
                      <el-button @click="onReset(formData2)">重置</el-button>
                    </el-col>
                  </el-row>
                </div>

              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep2 === 1">
                <!-- 等待审批界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      等待管理员审批
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep1 === 2">
                <!-- 等待流程开始界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      申请已通过等待流程开始
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep2 === 3">
                <div class = "sign">
                  正在进行的确权
                </div>
                <el-divider />
                <div style="height: 65vh; overflow: auto;">
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                </div>
                <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card style="height: 87vh; display: flex; flex-direction: column; justify-content: center; align-items: center;" v-if="applyStep3 === 0" >
                <div style="height: 65vh; overflow: auto;" >
                  <div class = "sign">
                    提交仲裁申请
                  </div>
                  <el-divider />
                  <el-form style="width: 100%" >
                    <el-input style="height: 30vh" type="textarea" :rows="10" placeholder="说明此次申请" v-model="formData3.text">
                    </el-input>
                    <div>
                        <el-date-picker
                          v-model="formData3.dateTimeRange"
                          type="datetimerange"
                          range-separator="至"
                          start-placeholder="开始日期"
                          end-placeholder="结束日期"
                          format="YYYY-MM-DD HH:mm:ss"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          style="width: 100%"
                        ></el-date-picker>
                    </div>
                    <el-divider />
                  </el-form>
                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit(formData3,'仲裁申请')">提交</el-button>
                      <el-button @click="onReset(formData3)">重置</el-button>
                    </el-col>
                  </el-row>
                </div>

              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep3 === 1">
                <!-- 等待审批界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px; ">
                      等待管理员审批
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep1 === 2">
                <!-- 等待流程开始界面 -->
                <div style="height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div style="text-align: center;">
                    <el-icon style="font-size: 48px; margin-bottom: 20px;">
                      <i class="el-icon-loading"></i>
                    </el-icon>
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      申请已通过等待流程开始
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep3 === 3">
                <div class = "sign">
                  正在仲裁的确权
                </div>
                <el-divider />
                <div style="height: 65vh; overflow: auto;">
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                </div>
                <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>


<style scoped>
.el-header{
  background-color: #365380;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-right: 20px;
  color: #fff;
  cursor: pointer;
}
.sign{
  text-align: center;
  font-size: 22px;
}
.form-row {
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px; /* 行之间的间隔 */
}

.label-col {
  text-align: left;
}

.input-col {
  text-align: center;
}

.button-col {
  text-align: right;
}
.el-table {
  display: flex;
  justify-content: center;
  margin-bottom: 24px; /* 表格与下方按钮的间隔 */
}
.el-aside{
  background-color: #f4f8f9;
}
.custom-aside {
  background-color: #365380;
  color: #fff;
  padding: 20px 0;
  text-align: center;
}

.logo {
  font-size: 24px;
  color: #fff;
  margin-bottom: 30px;
  font-family: 'Arial', sans-serif;
}

.logo strong {
  font-weight: bold;
}

.custom-menu {
  border-right: none;
}

.el-menu{
  background-color: #365380;
}
.el-menu-item {
  font-size: 18px;
  color: #afafaf;
  margin-bottom: 10px;
  padding: 10px 20px;
  text-align: center;

}

.el-menu-item:hover {
  background-color: #365380;
}

.el-menu-item.is-active {
  background-color: #365380;
  color: #fff;
  border-right: 5px solid #e67e22; /* 右侧橙色条 */
}
.el-dropdown-link {
  color: #fff !important;
}

</style>
