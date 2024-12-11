
<!--Examine1.vue-->
<script setup>
import { ref, computed, onMounted, h } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import {
  onSubmit,
  addMember,
  fetchApplications,
  encryptCsvFileWithProgress,
  fetchFiles, onReject,
} from '@/service/Examine1Service.js'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
const activeMenu = ref('4');
const username = localStorage.getItem('username');
const userRole = localStorage.getItem('role');  // 获取当前用户角色
const formData = ref({
  signer: {
    members: []
  },
  selectFile: '',
  taskType: '签名',
  status:'',
  username:'',
  applicationId:'',
});

const files = ref([]);

// 分页相关数据
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(6); // 每页显示条数
const fileName = ref();
const fileOutline = ref();

// 计算分页后的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

onMounted(async () => {
  tableData.value = await fetchApplications();
  files.value = await fetchFiles();
});

// 用户角色对应的可访问菜单项
const availableMenus = computed(() => {
  const role = userRole; // 获取当前用户角色

  // 根据角色过滤菜单项
  const menus = [
    { index: '1', name: '主页', roles: ['Admin', '普通用户', '数据所有方'] },
    { index: '2', name: '申请', roles: ['普通用户', '数据所有方'] },
    { index: '3', name: '处理', roles: ['Admin', '普通用户', '数据所有方','审核人员'] },
    { index: '4', name: '数据所有方审批', roles: ['数据所有方'] },
    { index: '5', name: '审核员审批', roles: ['Admin','审核人员'] },
    { index: '6', name: '管理', roles: ['Admin'] }
  ];

  return menus.filter(menu => menu.roles.includes(role));  // 过滤出用户角色可访问的菜单项
});

// 控制详情卡片显示
const isCardVisible = ref(false);
const selectedRow = ref(null); // 存储被点击行的数据

// 点击处理按钮时触发，显示详情卡片并存储当前行的数据
const showDetails = (row) => {
  selectedRow.value = row;
  isCardVisible.value = true; // 设置为 true 显示详情卡片
};

// 准备上传的签名人
const signer = ref({
  members: []
});
const memberSearch = ref(null);

// 删除成员
const removeMember = (member) => {
  console.log(formData.value.signer.members);
  const index = formData.value.signer.members.findIndex(m => m.username === member.username);
  if (index !== -1) {
    formData.value.signer.members.splice(index, 1);
  }
};


// 控制进度条状态
const isProcessing = ref(false);
const progress = ref(0);
const showUpload = ref(false);

// 存储选择的文件
const selectedFile = ref(null);


// 文件选择
const handleBeforeUpload = (file) => {
  selectedFile.value = file; // 选择的文件存储
  showUpload.value = true;
  ElMessage.success('已选中数据文件');
  return false; // 阻止自动上传，等待加密后再上传
};

const estimatedTime = ref(''); // 用于存储预计剩余时间



// 加密和上传文件
const encryptAndUpload = async () => {

  if (!fileName.value) {
    ElMessage.error("还未给数据命名");
    return;
  }
  isProcessing.value = true; // 显示进度条
  const startTime = Date.now(); // 记录开始时间
  await encryptCsvFileWithProgress(
    selectedFile.value,
    startTime,
    isProcessing.value,
    showUpload.value,
    estimatedTime,
    progress,
    fileName.value,
    username,
    fileOutline.value,
  );
};

// 提示
const open = () => {
  ElNotification({
    title: '注意',
    message: h('i', { style: 'color: teal' }, '请确认文件已经成功加密上传后再点击提交'),
  })
};

// 重置表单
const onReset = () => {
  formData.value = {
    signer: {
      members: []
    },
    selectFile: null
  };
};

// 解释拒绝原因的表单
const onExplain = (id) => {
  // 使用 ElMessageBox.prompt 来提示用户输入拒绝原因
  ElMessageBox.prompt('请输入拒绝原因', '拒绝原因', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/, // 确保输入不为空
    inputErrorMessage: '拒绝原因不能为空'
  }).then(async ({ value }) => {
    // 用户输入了拒绝原因并点击了确定
    ElMessage({
      type: 'success',
      message: `已提交拒绝原因: ${value}`
    });

    // 调用 onReject 函数，传入拒绝原因
    await onReject(value, id);
  }).catch(() => {
    // 用户取消了输入框
    ElMessage({
      type: 'info',
      message: '已取消操作'
    });
  });
};

</script>

<template>
  <el-container style="height: 100vh; width: 100%;">
    <!-- 侧边栏 -->
    <el-aside width="205px" class="custom-aside">
      <div class="logo"><strong>数据流转平台</strong></div>
      <el-menu :default-active="activeMenu" class="custom-menu" @select="handleSelect">
        <!-- 动态渲染菜单项 -->
        <el-menu-item
          v-for="menu in availableMenus"
          :key="menu.index"
          :index="menu.index"
        >
          <span>{{ menu.name }}</span>
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
      <el-container>
        <el-aside width="100%" style="padding: 20px;">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-card style="height: 87vh;">
                <div class="sign">待处理的申请</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="62.5vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="applicationTime" label="申请时间" align="center"/>
                    <el-table-column prop="id" label="申请ID" align="center"/>
                    <el-table-column prop="username" label="用户名" align="center" width="100"/>
                    <el-table-column prop="text" label="申请内容" width="300">
                      <template #default="scope">
                        <div>
                          <div>需求：{{ scope.row.text }}</div>
                          <div>时间范围：{{ scope.row.startDate }} - {{ scope.row.endDate }}</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" align="center" width="100">
                    <template #default="scope">
                      <el-button type="primary" size="small" @click="open(); showDetails(scope.row)">
                        处理
                      </el-button>
                    </template>
                    </el-table-column>
                  </el-table>
                </div>

                <!-- 分页控件 -->
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="tableData.length"
                  :page-size="pageSize"
                  v-model:currentPage="currentPage"
                  style="margin-top: 20px; text-align: center; display: flex; justify-content: center;"
                />
              </el-card>
            </el-col>
            <el-col :span="8" v-if="!isCardVisible">
              <el-card style="height: 87vh; position: relative;">
                <div class="sign">上传新数据</div>
                <el-divider />
                <el-row class="form-row">
                  <el-col :span="6" class="label-col" style="margin-top: 20px" >命名数据：</el-col>
                  <el-col :span="18" class="input-col" style="margin-top: 20px">
                    <el-input v-model="fileName"/>
                  </el-col>
                  <el-input style="height: 30vh; margin-top: 20px" type="textarea" :rows="10" placeholder="上传数据的概要"  v-model="fileOutline"/>
                </el-row>
                <!-- 文件选择和加密上传按钮部分 -->
                <div style="display: flex; justify-content: center; margin-top: 20px;">
                  <!-- 文件选择 -->
                  <el-upload
                    :before-upload="handleBeforeUpload"
                    :show-file-list="true"
                  >
                    <el-button type="primary">选择数据文件</el-button>
                  </el-upload>

                  <!-- 加密并上传按钮，文件选择后显示 -->
                  <el-button v-if="showUpload" type="success" @click="encryptAndUpload" style="margin-left: 20px;">加密并且上传</el-button>
                </div>

                <!-- 加密和上传进度条 -->
                <div v-if="isProcessing && progress !== 100" style="justify-content: center; margin-top: 20px;">
                  <el-progress
                    :text-inside="true"
                    :stroke-width="18"
                    :percentage="progress"
                    :style="{ width: '100%' }"
                  />
                </div>
                <p v-if="isProcessing && progress !== 100">预计剩余时间：{{ estimatedTime }}</p>

              </el-card>
            </el-col>
            <el-col :span="8" v-if="isCardVisible">
              <el-card style="height: 87vh; position: relative;">
                <el-button
                  type="text"
                  style="position: absolute; right: 20px; top: 10px; font-size: 30px; cursor: pointer;"
                  @click="isCardVisible = false"
                >×</el-button>
                <div class="sign">{{ selectedRow?.username }}的申请</div>
                <el-divider />
                <el-form label-width="100px">
                  <el-row class="form-row">
                    <el-col :span="6" class="label-col">选择数据：</el-col>
                    <el-select
                      v-model="formData.selectFile"
                      style="width: 73%"
                    >
                      <el-option
                        v-for="(file, index) in files"
                        :key="index"
                        :label="file"
                        :value="file"
                      ></el-option>
                    </el-select>

                  </el-row>
                  <!-- 参与成员部分 -->
                  <el-row class="form-row">
                    <el-col :span="6" class="label-col">参与成员：</el-col>
                    <el-col :span="12" class="input-col">
                      <el-input v-model="memberSearch" placeholder="输入用户名"></el-input>
                    </el-col>
                    <el-col :span="5" class="button-col">
                      <el-button type="primary" @click="addMember(memberSearch, formData.signer); memberSearch = null">添加</el-button>
                    </el-col>
                  </el-row>

                  <div style="height: 40vh; margin-bottom: 20px">
                    <el-table height="40vh" :data="formData.signer.members" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                      <el-table-column prop="username" label="成员名称" align="center" />
                      <el-table-column fixed="right" label="操作" align="center">
                        <template #default="scope">
                          <el-button type="text" size="small" @click="removeMember(scope.row)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  <el-divider />
                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit(formData,selectedRow.id, selectedRow.username); onReset()">提交</el-button>
                      <el-button type="danger" @click="onExplain(selectedRow.id)">拒绝</el-button>
                      <el-button @click="onReset">重置</el-button>
                    </el-col>
                  </el-row>
                </el-form>
              </el-card>

            </el-col>
          </el-row>
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 基本设置 */
body, html {
  font-family: 'Arial', sans-serif;
  color: #333;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-size: 14px; /* 设置基础字体大小，方便整体适配 */
}

/* 全局容器 */
.el-container {
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 侧边栏 */
.custom-aside {
  background: linear-gradient(135deg, #1f2f47, #304156);
  color: #fff;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #3a4a5f;
  letter-spacing: 1px;
}

.custom-menu {
  background-color: transparent;
  font-size: 15px; /* 侧边栏菜单字体大小适中 */
}

.custom-menu .el-menu-item {
  color: #c0c4cc;
  padding: 15px 20px;
  transition: all 0.3s ease;
}

.custom-menu .el-menu-item:hover {
  color: #ffd04b;
  background-color: rgba(255, 208, 75, 0.1);
}

.custom-menu .el-menu-item.is-active {
  background-color: #ffd04b;
  color: #333;
  font-weight: bold;
  border-radius: 5px;
}

/* 顶部栏 */
.el-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.1);
  height: 60px;
}

.el-avatar {
  cursor: pointer;
  font-weight: bold;
  color: #409eff;
  font-size: 15px; /* 顶部栏头像文字大小 */
  transition: color 0.3s ease;
}

.el-avatar:hover {
  color: #ffd04b;
}

/* 内容区 */
.sign {
  font-size: 16px; /* 调整内容区标题大小 */
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.el-divider {
  margin: 15px 0;
}

/* 卡片 */
.el-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.el-table th, .el-table td {
  font-size: 13px; /* 表格内容字体小而精炼 */
}

/* 表单 */
.form-row {
  margin-top: 15px;
}

.label-col {
  font-size: 14px;
  font-weight: bold;
  text-align: right;
  color: #333;
}

.input-col {
  padding-left: 10px;
}

.el-input,
.el-select {
  width: 100%;
  transition: border-color 0.3s ease;
}

.el-input:focus,
.el-select:focus {
  border-color: #ffd04b;
}

.button-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.el-button {
  font-size: 14px;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 上传部分 */
.el-upload .el-button {
  margin-right: 10px;
}

.el-progress {
  width: 100%;
  font-size: 13px; /* 调整进度条文字大小 */
}

/* 分页 */
.el-pagination {
  margin-top: 20px;
  font-size: 13px; /* 分页文字适配 */
}

/* 弹出卡片 */
.el-card .close-btn {
  font-size: 24px;
  color: #409eff;
  position: absolute;
  right: 15px;
  top: 10px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.el-card .close-btn:hover {
  color: #ffd04b;
}

/* 按钮颜色 */
.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
  font-size: 14px; /* 按钮文字适配 */
}

.el-button--primary:hover {
  background-color: #ffd04b;
  border-color: #ffd04b;
  color: #333;
}

.el-button--success {
  background-color: #67c23a;
  border-color: #67c23a;
}

.el-button--success:hover {
  background-color: #5cbd2a;
  border-color: #5cbd2a;
}

.el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.el-button--danger:hover {
  background-color: #f54848;
  border-color: #f54848;
}
</style>


