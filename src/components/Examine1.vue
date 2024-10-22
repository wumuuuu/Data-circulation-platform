<!--Examine1.vue-->
<script setup>
import { ref, computed, onMounted, h } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import {
  onSubmit,
  addMember,
  fetchApplications,
  encryptCsvFileWithProgress,
  decryptCsvFileWithProgress,
  fetchFiles, onReject
} from '@/service/Examine1Service.js'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
const activeMenu = ref('4');
const username = localStorage.getItem('username');

const formData = ref({
  signer: {
    members: []
  },
  selectFile: '',
  taskType: 'sign',
  status:'',
  username:'',
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
  const index = signer.value.members.findIndex(m => m.name === member.name);
  if (index !== -1) {
    signer.value.members.splice(index, 1);
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
    fileOutline.value
  );
};


// 解密文件并保存在本地
// const decryptAndSave = async () => {
//   isProcessing.value = true; // 显示进度条
//
//   const startTime = Date.now(); // 记录开始时间
//   const progressCallback = (progressValue) => {
//     progress.value = progressValue; // 更新进度
//   };
//
//   try {
//     await decryptCsvFileWithProgress(selectedFile.value, progressCallback); // 调用解密函数
//     ElMessage.success('文件解密成功并已保存到本地');
//   } catch (error) {
//     ElMessage.error('文件解密失败');
//   } finally {
//     isProcessing.value = false; // 停止进度条
//   }
// };

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
        <el-menu-item index="1">
          <span>主页</span>
        </el-menu-item>
        <el-menu-item index="2">
          <span>申请</span>
        </el-menu-item>
        <el-menu-item index="3">
          <span>处理</span>
        </el-menu-item>
        <el-menu-item index="4">
          <span>数据所有方审批</span>
        </el-menu-item>
        <el-menu-item index="5">
          <span>审核员审批</span>
        </el-menu-item>
        <el-menu-item index="6">
          <span>管理</span>
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
                    <el-table-column prop="username" label="用户名" align="center" width="100"/>
                    <el-table-column prop="text" label="申请内容" width="400">
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
                  <el-col :span="6" class="label-col">命名数据：</el-col>
                  <el-col :span="18" class="input-col">
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
                <el-form :model="signer" label-width="100px">
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
:deep(.el-step__icon-inner) {
  font-size: 15px !important;
}

:deep(.el-steps__line) {
  height: 3px !important;
}

:deep(.el-step__title) {
  font-size: 13px !important;
}
.custom-button {
  width: 200px;  /* 固定宽度 */
  height: 60px;  /* 按钮高度 */
  margin-bottom: 10px;
}
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
  margin-bottom: 10px; /* 行之间的间隔 */
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
.custom-button-text {
  font-size: 17px; /* 自定义字体大小 */
}

</style>