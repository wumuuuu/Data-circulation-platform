<script setup>
import { ref, computed, onMounted } from 'vue'
import router from '@/router.js'
import { deleteUser, listUsers, updateUser } from '@/service/UserMgrService.js'

const activeMenu = ref('4');
const username = localStorage.getItem('username');

// 分页相关数据
let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数

// 存储当前正在编辑的用户信息
const editingUser = ref({
  id: null,
  username: '',
  role: ''
});
const isEditing = ref(false);

onMounted(() => {
  fetchApplications();
});

// 获取用户列表
const fetchApplications = async () => {
  tableData.value = await listUsers();
  console.log(tableData);
};

// 计算分页后的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end);
});

//点击登出并跳转到登录界面
const handleCommand = (command) => {
  if (command === 'logout') {
    // 清理登录状态，例如移除 token 或用户信息
    localStorage.clear();
    console.log('localStorage');
    // 然后跳转到登录页面
    router.push('/');
  }
};

// 处理不同的菜单选择
const handleSelect = (index) => {
  switch (index) {
    case '1':
      router.push('/home');
      break;
    case '2':
      router.push('/application');
      break;
    case '3':
      router.push('/approvalProcess');
      break;
    case '4':
      router.push('/UserMgr');
      break;
  }
};

// 删除用户
const onDelete = async (username) => {
  if(await deleteUser(username) === 'success'){
    await fetchApplications(); // 删除成功后重新加载用户列表
  }
};

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
    alert('用户信息更新成功');
    await fetchApplications(); // 更新成功后重新加载用户列表
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
          <span>审批</span>
        </el-menu-item>
        <el-menu-item index="4">
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
                        <el-input v-if="isEditing && editingUser.id === scope.row.id" v-model="editingUser.role" />
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
</style>
