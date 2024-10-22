<script setup>
import { ref, computed, onMounted } from 'vue'
import {handleCommand, handleSelect} from '@/router.js'
import { update, fetchApplications } from '@/service/Examine2Service.js'
const activeMenu = ref('5');
const username = localStorage.getItem('username');

// 分页相关数据
const tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(6); // 每页显示条数
const rowStatus = ref({});

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
  if(Type === 'sign') {
    await update(username, id, tableData, '等待数据所有方审核');
  }else if(Type === 'confirm') {
    await update(username, id, tableData, '申请已通过');
  }
};

// 拒绝申请
const onReject = async (id) => {
  const reason = rowStatus.value[id].rejectReason || '';
  await update(id, tableData, '平台审核未通过', reason);

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
                        <div v-if="scope.row.applicationType === 'sign'">
                          <div>需求：{{ scope.row.text }}</div>
                          <div >时间：{{ scope.row.startDate }} - {{ scope.row.endDate }}</div>
                        </div>
                        <div v-if="scope.row.applicationType === 'confirm'">
                          <div>需求：对ID为 {{ scope.row.text }} 的流转数据进行确权</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" align="center">
                      <template #default="scope">
                        <!-- 如果当前行处于编辑模式，显示输入框和确定/取消按钮，否则显示同意/拒绝按钮 -->
                        <div v-if="rowStatus[scope.row.id].isEditing">
                          <el-input v-model="rowStatus[scope.row.id].rejectReason" placeholder="请输入拒绝理由" />
                          <el-button type="primary" size="small" @click="onReject(scope.row.id)">
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

</style>