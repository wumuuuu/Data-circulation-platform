<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import { fetchUser, onDelete, updateUser } from '@/service/UserMgrService.js'

const activeMenu = ref('6');
const username = localStorage.getItem('username');
const userRole = localStorage.getItem('role');  // 获取当前用户角色
// 分页相关数据
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数
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


// 存储当前正在编辑的用户信息
const editingUser = ref({
  id: null,
  username: '',
  role: ''
});
const isEditing = ref(false);

onMounted(async () => {
  tableData.value = await fetchUser();
});

// 计算分页后的数据
const paginatedData = computed(() => {
  if (!Array.isArray(tableData.value)) {
    console.error("tableData is not an array:", tableData.value);
    return []; // 如果不是数组，返回空数组
  }

  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});


// 修改用户信息
const onModify = (id) => {
  const user = tableData.value.find(u => u.id === id);
  if (user) {
    editingUser.value = { ...user }; // 深拷贝用户信息到编辑状态
    isEditing.value = true; // 设置编辑状态为 true
  }
};

// 保存修改后的用户信息
const saveChanges = async () => {
  try {
    await updateUser(editingUser.value);
    isEditing.value = false; // 退出编辑模式
  } catch (error) {
    console.error('更新用户信息时出错:', error);
  }
};

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false; // 退出编辑模式
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
            <el-col :span="24">
              <el-card style="height: 87vh;">
                <div class="sign">用户管理</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="id" label="用户ID" align="center" />
                    <el-table-column prop="username" label="用户名" align="center" >
                      <template #default="scope">
                        <el-input v-if="isEditing && editingUser.id === scope.row.id" v-model="editingUser.username" />
                        <span v-else>{{ scope.row.username }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="role" label="用户权限" align="center" >
                      <template #default="scope">
                        <el-select v-if="isEditing && editingUser.id === scope.row.id" v-model="editingUser.role" placeholder="请选择角色">
                          <el-option label="Admin" value="Admin"></el-option>
                          <el-option label="普通用户" value="普通用户"></el-option>
                          <el-option label="数据所有方" value="数据所有方"></el-option>
                          <el-option label="审核人员" value="审核人员"></el-option>
                        </el-select>
                        <span v-else>{{ scope.row.role }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="onDelete(scope.row.username)">
                          删除
                        </el-button>
                        <el-button link type="primary" size="small" @click="onModify(scope.row.id)" v-if="!isEditing || editingUser.id !== scope.row.id">
                          修改
                        </el-button>
                        <el-button link type="primary" size="small" @click="saveChanges" v-if="isEditing && editingUser.id === scope.row.id">
                          完成
                        </el-button>
                        <el-button link type="primary" size="small" @click="cancelEdit" v-if="isEditing && editingUser.id === scope.row.id">
                          取消
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
          </el-row>
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 全局容器样式 */
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

/* 表格样式 */
.el-table th, .el-table td {
  font-size: 13px;
  text-align: center;
}

.el-table .el-input, .el-table .el-select {
  width: 150px;
  font-size: 13px;
}

.el-table .el-button {
  font-size: 13px;
}

/* 按钮颜色 */
.el-button--primary {
  background-color: white;
  border-color: #409eff;
  font-size: 14px;
}

.el-button--primary:hover {
  background-color: #ffd04b;
  border-color: #ffd04b;
  color: #333;
}

/* 分页 */
.el-pagination {
  margin-top: 20px;
  font-size: 13px;
  text-align: center;
}

/* 表格内容对齐 */
.el-table-column {
  padding: 8px;
}
</style>
