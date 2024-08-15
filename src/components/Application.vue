<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApplicationStatus, submitApplication } from '@/api/user.js'

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
  try {
    applyStep1.value = 0;
    applyStep2.value = 0;
    applyStep3.value = 0;

    const response = await getApplicationStatus(username);
    if (response && response.code === 200) {
      const type = response.data.applicationType;
      const status = response.data.applicationStatus;
      let step = 0;
      if (status === 'REJECT' || status === null) {
        step = 0;
      } else if (status === 'PENDING') {
        step = 1;
      } else if (status === 'APPROVED') {
        step = 2;
      }
      if (type === '签名申请') {
        applyStep1.value = step;
      } else if (type === '确权申请') {
        applyStep2.value = step;
      } else {
        applyStep3.value = step;
      }
    }
  } catch (error) {
    console.error('Error fetching application status:', error);
  }
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
  // 检查表单内容是否已填写完整
  if (!formData.text) {
    ElMessage.warning('请输入申请说明');
    return;
  }

  if (!formData.dateTimeRange || formData.dateTimeRange.length !== 2) {
    ElMessage.warning('请选择日期时间范围');
    return;
  }

  // 准备要上传的数据
  const applicationData = {
    username: localStorage.getItem('username'), // 获取当前登录的用户名
    text: formData.text,
    startDate: formData.dateTimeRange ? formData.dateTimeRange[0] : null,
    endDate: formData.dateTimeRange ? formData.dateTimeRange[1] : null,
    applicationType: Type,
    applicationStatus: 'PENDING'
  };

  try {
    // 调用接口提交数据
    const response = await submitApplication(applicationData);
    console.log(applicationData);
    onReset(formData);
    ElMessage.success('申请提交成功');
  } catch (error) {
    console.error('Error:', error);
    ElMessage.error('申请提交失败');
  }
}

// 重置表单
const onReset = (formData) => {
  formData.text = '';
  formData.dateTimeRange = null;
};

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
                <div class = "sign">
                  正在进行的签名
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
              <el-card style="height: 87vh;" v-else-if="applyStep2 === 2">
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
                    <div class="sign" style="font-size: 24px; font-weight: bold; margin-bottom: 20px;">
                      等待管理员审批
                    </div>
                  </div>
                </div>
              </el-card>
              <el-card style="height: 87vh;" v-else-if="applyStep3 === 2">
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
