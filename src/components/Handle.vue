<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import { calculateArbitration, calculateConfirm, calculateSign, fetchTask } from '@/service/HandleService.js'
const activeMenu = ref('3');
const username = sessionStorage.getItem('username');
const userRole = sessionStorage.getItem('role');  // 获取当前用户角色
const formData = ref({
  dataUser:'',
  text: '', // 用户提交的申请文本
  type:'',
  dateTimeRange: [] // 用户选择的日期和时间范围
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
// 分页相关数据1
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(9); // 每页显示条数

onMounted(async () => {
  tableData.value = await fetchTask(username);
});

// 计算分页后的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

const privateKey = ref(null);

// 文件选择
const handleBeforeUpload = async (file, rowData) => {
  privateKey.value = file;
  // console.log(rowData.taskType);
  if(rowData.taskType === '签名'){
    await calculateSign(privateKey.value, rowData, username);
  } else if(rowData.taskType === '确权'){
    await calculateConfirm(privateKey.value, rowData, username);
  } else {
    await calculateArbitration(privateKey.value, rowData, username);
  }


  // 阻止自动上传，等待其他操作完成后再上传
  return false;
};

// 重置表单
const onReset = () => {
  formData.value.text = '';
  formData.value.dataUser = null;
  formData.value.dateTimeRange = null;
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
            <el-col :span="20">
              <el-card style="height: 87vh;">
                <div class="sign">待处理流程</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="62.5vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="completedAt" label="时间" align="center" />
                    <el-table-column prop="taskId" label="任务ID" align="center" />
                    <el-table-column prop="fileId" label="数据名" align="center" />
                    <el-table-column prop="taskType" label="类型" align="center" />
                    <el-table-column prop="status" label="状态" align="center" />
                    <el-table-column label="操作" align="center" width="100">
                        <template #default="scope">
                          <!-- 文件选择 -->
                          <el-upload
                            :before-upload="(file) => handleBeforeUpload(file, scope.row)"
                            :show-file-list="true"
                          >
                            <el-button type="primary" size="small" :disabled="scope.row.status !== 'in_progress'">
                              添加私钥
                            </el-button>
                          </el-upload>
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
          </el-row>
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>

<style scoped src="@/css/Handle.css">

</style>