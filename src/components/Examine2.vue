<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import { update, fetchApplications } from '@/service/Examine2Service.js'
const activeMenu = ref('5');
const username = localStorage.getItem('username');
const userRole = localStorage.getItem('role');  // 获取当前用户角色
// 分页相关数据
const tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(6); // 每页显示条数
const rowStatus = ref({});
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
onMounted(async () => {
  tableData.value = await fetchApplications();
  console.log(tableData.value);

  // 初始化每一行的状态
  tableData.value.forEach(item => {
    rowStatus.value[item.id] = {
      isEditing: false, // 是否在编辑状态
      rejectReason: ''   // 拒绝理由
    };
  });
});

const toggleReject = (id) => {
  rowStatus.value[id].isEditing = true; // 设置为编辑状态
};


// 计算分页后的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

// 点击“取消”时的操作
const onCancel = (id) => {
  rowStatus.value[id].isEditing = false; // 退出编辑模式，恢复“同意”和“拒绝”按钮
  rowStatus.value[id].rejectReason = ''; // 清空拒绝理由
};

// 同意申请
const onAgree = async (id, Type, username) => {
  if(Type === '签名') {
    await update(username, id, tableData, '等待数据所有方审核', '');
  }else{
    await update(username, id, tableData, '申请已通过', '等待验证');
  }
};

// 拒绝申请
const onReject = async (id, username) => {
  const reason = rowStatus.value[id].rejectReason || '';
  await update(username, id, tableData, '平台审核未通过', reason);

  // 移除该行并退出编辑模式
  tableData.value = tableData.value.filter(item => item.id !== id);
  onCancel(id); // 操作完成后退出编辑模式
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
            <el-col :span="24">
              <el-card style="height: 87vh;">
                <div class="sign">待处理的申请</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="62.5vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="applicationTime" label="申请时间" align="center"/>、
                    <el-table-column prop="username" label="用户名" align="center"/>
                    <el-table-column prop="applicationType" label="申请类型" align="center"/>
                    <el-table-column label="申请内容" width="380">
                      <template #default="scope">
                        <div v-if="scope.row.applicationType === '签名'">
                          <div>需求：{{ scope.row.text }}</div>
                          <div >时间：{{ scope.row.startDate }} - {{ scope.row.endDate }}</div>
                        </div>
                        <div v-if="scope.row.applicationType === '确权'">
                          <div>需求：对ID为 {{ scope.row.text }} 的流转数据进行确权</div>
                        </div>
                        <div v-if="scope.row.applicationType === '仲裁'">
                          <div>需求：对ID为 {{ scope.row.text }} 的流转数据签名Y进行仲裁</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" align="center">
                      <template #default="scope">
                        <!-- 如果当前行处于编辑模式，显示输入框和确定/取消按钮，否则显示同意/拒绝按钮 -->
                        <div v-if="rowStatus[scope.row.id].isEditing">
                          <el-input v-model="rowStatus[scope.row.id].rejectReason" placeholder="请输入拒绝理由" />
                          <el-button type="primary" size="small" @click="onReject(scope.row.id, scope.row.username)">
                            确定
                          </el-button>
                          <el-button type="text" size="small" @click="onCancel(scope.row.id)">
                            取消
                          </el-button>
                        </div>
                        <div v-else>
                          <el-button type="primary" size="small" @click="onAgree(scope.row.id, scope.row.applicationType, scope.row.username)">
                            同意
                          </el-button>
                          <el-button type="primary" size="small" @click="toggleReject(scope.row.id)">
                            拒绝
                          </el-button>
                        </div>
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
  font-size: 15px;
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
  font-size: 15px;
  transition: color 0.3s ease;
}

.el-avatar:hover {
  color: #ffd04b;
}

/* 内容区 */
.sign {
  font-size: 16px;
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
}

.el-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.el-table th, .el-table td {
  font-size: 13px;
  text-align: center;
}

.el-table .el-input {
  width: 150px;
  font-size: 13px;
  margin-right: 10px;
}

.el-table .el-button {
  font-size: 13px;
}

/* 分页 */
.el-pagination {
  margin-top: 20px;
  font-size: 13px;
  text-align: center;
}

/* 按钮颜色 */
.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
  font-size: 14px;
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

/* 表格内容对齐 */
.el-table-column {
  padding: 8px;
}
</style>
