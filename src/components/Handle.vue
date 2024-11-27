<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import { calculateArbitration, calculateConfirm, calculateSign, fetchTask } from '@/service/HandleService.js'
const activeMenu = ref('3');
const username = localStorage.getItem('username');
const userRole = localStorage.getItem('role');  // 获取当前用户角色
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
    { index: '3', name: '处理', roles: ['Admin', '普通用户', '数据所有方'] },
    { index: '4', name: '数据所有方审批', roles: ['数据所有方'] },
    { index: '5', name: '审核员审批', roles: ['Admin'] },
    { index: '6', name: '管理', roles: ['Admin'] }
  ];

  return menus.filter(menu => menu.roles.includes(role));  // 过滤出用户角色可访问的菜单项
});
// 分页相关数据1
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(6); // 每页显示条数

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

<style scoped>
/* 基本设置 */
body, html {
  font-family: 'Arial', sans-serif;
  color: #333;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-size: 14px;
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
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #3a4a5f;
  letter-spacing: 1px;
  cursor: pointer;
}

.custom-menu {
  background-color: transparent;
  font-size: 16px;
}

.custom-menu .el-menu-item {
  color: #c0c4cc;
  padding: 15px 20px;
  transition: all 0.3s ease;
  border-radius: 5px;
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
  font-size: 16px;
  transition: color 0.3s ease;
}

.el-avatar:hover {
  color: #ffd04b;
}

/* 内容区 */
.sign {
  font-size: 18px;
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
  padding: 20px;
  margin-bottom: 20px;
}

.el-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.el-table th, .el-table td {
  font-size: 14px;
  text-align: center;
  padding: 10px;
}

.el-table .el-upload .el-button {
  font-size: 13px;
  padding: 6px 12px;
}

.el-upload .el-button {
  margin-right: 10px;
}

.el-progress {
  width: 100%;
  font-size: 13px;
}

/* 分页 */
.el-pagination {
  margin-top: 20px;
  font-size: 13px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 按钮颜色 */
.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
  font-size: 14px;
  transition: all 0.3s ease;
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

/* 小屏幕优化 */
@media screen and (max-width: 768px) {
  .el-container {
    flex-direction: column;
  }

  .el-header {
    justify-content: space-between;
    padding: 10px 20px;
  }

  .custom-aside {
    width: 100%;
    height: auto;
    position: relative;
  }

  .custom-menu {
    font-size: 14px;
  }

  .el-card {
    margin-bottom: 15px;
    padding: 15px;
  }

  .el-table th, .el-table td {
    font-size: 12px;
  }

  .el-avatar {
    font-size: 14px;
  }

  .el-pagination {
    font-size: 12px;
  }
}
</style>
