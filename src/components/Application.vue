<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import {
  Download,
  fetchApplications,
  fetchDataOwners,
  onSubmit, onSubmit1
} from '@/service/ApplicationService.js'
import { CircleCheckFilled, CircleCloseFilled, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useMenu } from '@/service/useMenu.js'
import { jwtDecode } from 'jwt-decode'

const token = sessionStorage.getItem('authToken');
const decoded = jwtDecode(token);  // 解析 JWT Token
const username = decoded.sub;
const userRole = decoded.role;  // 获取当前用户角色

const activeMenu = ref('2');
const selectedForm = ref('');  // 用于跟踪用户选择的表单
const formSelected = ref(false); // 标记是否选择了表单
const options = ref([]); // 用于存储从后端获取的用户数据
const taskId = ref('');

// 分页相关数据
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(5); // 每页显示条数

// 用户角色对应的可访问菜单项
const { availableMenus } = useMenu(userRole);
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

const loading = ref(true);  // 数据加载状态

onMounted(async () => {
  try {
    tableData.value = await fetchApplications();
    options.value = await fetchDataOwners();
  } catch (error) {
    console.error("数据加载失败", error);
  } finally {
    loading.value = false;  // 数据加载完成，更新 loading 状态
  }
});

const paginatedData = computed(() => {
  if (loading.value) {
    return [];  // 如果还在加载中，返回空数组
  }
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
                          <el-icon v-if="scope.row.status.includes('未通过')|| scope.row.status.includes('失败')" style="color: red; margin-left: 8px;"><CircleCloseFilled /></el-icon>
                          <el-icon v-else-if="scope.row.status.includes('已') || scope.row.status.includes('成功') || scope.row.status.includes('完成')" style="color: green; margin-left: 8px;"><CircleCheckFilled /></el-icon>
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
                          v-if="scope.row.explanation === '已允许下载该数据'"
                          link
                          type="primary"
                          size="small"
                          @click="Download(scope.row)"
                        >
                          下载
                        </el-button>
                        <el-button
                          v-else
                          link
                          type="primary"
                          size="small"
                          :disabled="!scope.row.explanation || scope.row.explanation === ''"
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
                    <el-button type="primary" @click="showForm('签名')" class="custom-button custom-button-text">签名申请</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="20" type="flex" justify="center" style="height: 100px;">
                  <el-col>
                    <el-button type="primary" @click="showForm('确权')" class="custom-button custom-button-text">确权申请</el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="20" type="flex" justify="center" style="height: 100px;">
                  <el-col>
                    <el-button type="primary" @click="showForm('仲裁')" class="custom-button custom-button-text">仲裁申请</el-button>
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
                <div v-if="selectedForm === '签名'" style="height: 70vh; overflow: auto;  width: 350px">
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

                <div v-else-if="selectedForm === '确权'" style="height: 70vh; overflow: auto; width: 350px">
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
                      <el-button type="primary" @click="onSubmit1(taskId, '确权'); onReset1();">提交</el-button>
                      <el-button @click="onReset1()">重置</el-button>
                    </el-col>
                  </el-row>
                </div>
                <div v-else-if="selectedForm === '仲裁'" style="height: 70vh; overflow: auto; width: 350px">
                  <!-- 仲裁申请表单内容 -->
                  <div class = "sign">
                    提交仲裁申请
                  </div>
                  <el-divider />
                  <el-form>
                    <el-form-item label="输入要仲裁的任务ID：" :rules="{required: true}">
                      <el-input v-model="taskId"/>
                    </el-form-item>
                    <el-divider />
                  </el-form>

                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit1(taskId, '仲裁'); onReset1();">提交</el-button>
                      <el-button @click="onReset1()">重置</el-button>
                    </el-col>
                  </el-row>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>

<style scoped src="@/css/Application.css">

</style>