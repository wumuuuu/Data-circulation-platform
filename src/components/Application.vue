<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import {
  fetchApplications,
  fetchDataOwners,
  onSubmit, onSubmit1
} from '@/service/ApplicationService.js'
import { CircleCheckFilled, CircleCloseFilled, Clock } from '@element-plus/icons-vue'

const activeMenu = ref('2');
const username = localStorage.getItem('username');
const selectedForm = ref('');  // 用于跟踪用户选择的表单
const formSelected = ref(false); // 标记是否选择了表单
const options = ref([]); // 用于存储从后端获取的用户数据
const taskId = ref('');


// 用于根据选择显示对应的卡片
function showForm(type) {
  selectedForm.value = type;
  formSelected.value = true; // 设置为true以隐藏按钮卡片并显示表单卡片
  formData.value.type = type;
}

const formData = ref({
  dataUser:'',
  text: '', // 用户提交的申请文本
  type:'',
  dateTimeRange: [] // 用户选择的日期和时间范围
});

// 分页相关数据
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(6); // 每页显示条数

onMounted(async () => {
  tableData.value = await fetchApplications();
  options.value = await fetchDataOwners();
  // updateTaskSteps();
});

// 计算分页后的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

// 对话框显示控制
const dialogVisible = ref(false);
// 对话框内容
const dialogContent = ref('');

// 打开对话框并显示 explanation 内容
const openDialog = (explanation) => {
  dialogContent.value = explanation || '无详细说明';
  dialogVisible.value = true;
  console.log(dialogVisible.value);
};


// 重置签名表单
const onReset = () => {
  formData.value.text = '';
  formData.value.dataUser = null;
  formData.value.dateTimeRange = null;
};

// 重置确权表单
const onReset1 = () => {
  taskId.value = null;
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
                <div class="sign">申请记录</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="62.5vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="applicationTime" label="申请时间" align="center" width = "165"/>
                    <el-table-column prop="applicationType" label="申请类型" align="center" width = "85"/>
                    <el-table-column label="状态" align="center">
                      <template #default="scope">
                        <div style="display: flex; align-items: center; justify-content: center;">
                          <span>{{ scope.row.status }}</span>
                          <el-icon v-if="scope.row.status.includes('未通过')" style="color: red; margin-left: 8px;"><CircleCloseFilled /></el-icon>
                          <el-icon v-else-if="scope.row.status.includes('通过') || scope.row.status.includes('成功')" style="color: green; margin-left: 8px;"><CircleCheckFilled /></el-icon>
                          <el-icon v-else style="margin-left: 8px;"><Clock /></el-icon>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column prop="text" label="申请内容" width = "250" >
                      <template #default="scope">
                        <div>
                          <div>需求：{{ scope.row.text }}</div>
                          <div>时间：{{ scope.row.startDate }} - {{ scope.row.endDate }}</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="其他" align="center" width = "70" >
                      <template #default="scope">
                        <el-button
                          link
                          type="primary"
                          size="small"
                          :disabled="!scope.row.explanation"
                          @click="openDialog(scope.row.explanation)"
                        >
                            详情
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <!-- 对话框 -->
                  <el-dialog title="详细信息"  v-model="dialogVisible" width="30%">
                    <p>{{ dialogContent }}</p>
                    <template #footer>
                      <el-button @click="dialogVisible = false">关闭</el-button>
                    </template>
                  </el-dialog>
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
            <el-col :span="8" >
              <!-- 按钮卡片，点击按钮后隐藏 -->
              <el-card v-if="!formSelected" style="height: 87vh; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                <!-- 按钮排列 -->
                <el-row :gutter="20" type="flex" justify="center" style="height: 100px;">
                  <el-col>
                    <el-button type="primary" @click="showForm('sign')" class="custom-button custom-button-text">签名申请</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="20" type="flex" justify="center" style="height: 100px;">
                  <el-col>
                    <el-button type="primary" @click="showForm('confirm')" class="custom-button custom-button-text">确权申请</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="20" type="flex" justify="center" style="height: 100px;">
                  <el-col>
                    <el-button type="primary" @click="showForm('arbitrate')" class="custom-button custom-button-text">仲裁申请</el-button>
                  </el-col>
                </el-row>
              </el-card>


              <!-- 根据选择显示对应的表单卡片 -->
              <el-card v-if="formSelected" style="height: 87vh; display: flex; flex-direction: column; justify-content: center; align-items: center; position: relative;" >
                <el-button
                  type="text"
                  style="position: absolute; right: 20px; top: 10px; font-size: 30px; cursor: pointer;"
                  @click="formSelected = false"
                >×</el-button>
                <!-- 根据选择的表单类型显示不同的内容 -->
                <div v-if="selectedForm === 'sign'" style="height: 70vh; overflow: auto;  width: 350px">
                  <div class = "sign">
                    提交签名申请
                  </div>
                  <el-divider />
                  <el-form>
                    <el-form-item label="选择数据所有方：" :rules="{required: true}">
                      <el-select placeholder="请选择" v-model="formData.dataUser">
                        <!-- 动态生成选项 -->
                        <el-option
                          v-for="username in options"
                          :key="username"
                          :label="username"
                          :value="username"
                        />
                      </el-select>

                    </el-form-item>
                    <el-form-item :rules="{required: true}">
                      <el-input style="height: 30vh" type="textarea" :rows="10" placeholder="说明此次申请具体要求"  v-model="formData.text"/>
                    </el-form-item>
                    <el-form-item :rules="{required: true}">
                      <el-date-picker
                        v-model="formData.dateTimeRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                      ></el-date-picker>
                    </el-form-item>
                    <el-divider />
                  </el-form>

                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit(formData); onReset();">提交</el-button>
                      <el-button @click="onReset()">重置</el-button>
                    </el-col>
                  </el-row>
                </div>

                <div v-else-if="selectedForm === 'confirm'" style="height: 70vh; overflow: auto; width: 350px">
                  <!-- 确权申请表单内容 -->
                  <div class = "sign">
                    提交确权申请
                  </div>
                  <el-divider />
                  <el-form>
                    <el-form-item label="输入要确权的任务ID：" :rules="{required: true}">
                      <el-input v-model="taskId"/>
                    </el-form-item>
                    <el-divider />
                  </el-form>

                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit1(taskId); onReset1();">提交</el-button>
                      <el-button @click="onReset1()">重置</el-button>
                    </el-col>
                  </el-row>
                </div>
                <div v-else-if="selectedForm === 'arbitrate'">
                  <!-- 仲裁申请表单内容 -->
                </div>
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