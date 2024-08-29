<script setup>
import { ref, computed, onMounted } from 'vue'
import router from '@/router.js'
// import { listUsers } from '@/service/UserMgrService.js'

const activeMenu = ref('2');
const username = localStorage.getItem('username');

// 分页相关数据
// let tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数

// 表格数据
const tableData = ref([
  { time: 1, status: '等待管理员审核' },
  { time: 2, status: '管理员审核未通过' },
  { time: 3, status: '数据所有方审核未通过' },
  { time: 4, status: '流程已开启' }
]);

// 定义步骤描述
const stepDescriptions = [
  { title: '已提交' },
  { title: '管理员审核' },
  { title: '数据所有方审核' },
  { title: '流程' }
];

/// 根据当前状态获取步骤索引
function getStepIndex(status) {
  const statusToStep = {
    '已提交': 0,
    '等待管理员审核': 1,
    '管理员审核通过': 2,
    '管理员审核未通过': 1,  // 审核未通过仍处于审核步骤
    '等待数据所有方审核': 2,
    '数据所有方审核通过': 3,
    '数据所有方审核未通过': 2,  // 审核未通过仍处于审核步骤
    '流程已开启': 3
  };
  return statusToStep[status] || 0;
}

// 获取步骤状态
function getStepStatus(status, stepIndex) {
  const stepOrder = [
    '已提交',
    '等待管理员审核',
    '管理员审核通过',
    '管理员审核未通过',
    '等待数据所有方审核',
    '数据所有方审核通过',
    '数据所有方审核未通过',
    '流程已开启'
  ];

  const currentIndex = stepOrder.indexOf(status);

  // 如果管理员审核未通过，则返回第二步error，后续步骤wait
  if (status === '管理员审核未通过') {
    if (stepIndex === 1) {
      return 'error';
    } else if (stepIndex > 1) {
      return 'wait';
    }
  }

  // 如果数据所有方审核未通过，则返回第三步error，后续步骤wait
  if (status === '数据所有方审核未通过') {
    if (stepIndex === 2) {
      return 'error';
    } else if (stepIndex > 2) {
      return 'wait';
    }
  }

  if (stepIndex < currentIndex) {
    return 'success';
  } else if (stepIndex === currentIndex) {
    return 'process';
  } else {
    return 'wait';
  }
}


onMounted(() => {
  fetchApplications();
  // updateTaskSteps();
});

// 获取申请记录列表
const fetchApplications = async () => {
  // tableData.value = await listUsers();
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
    sessionStorage.clear();
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
            <el-col :span="16">
              <el-card style="height: 87vh;">
                <div class="sign">历史申请</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="time" label="申请时间" align="center" width = "100%"/>
                    <el-table-column prop="status" label="进展" align="center" width = "600">
                      <template #default="scope">
                        <el-steps style="width: 100%" :active="getStepIndex(scope.row.status)" align-center finish-status="success">
                          <el-step
                            v-for="(step, index) in stepDescriptions"
                            :key="index"
                            :title="step.title"
                            :status="getStepStatus(scope.row.status, index)"
                          />
                        </el-steps>
                      </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="onDelete(scope.row.username)">
                          删除
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
            <el-col :span="8">
              <el-card style="height: 87vh;">
                <div class="sign">用户管理</div>
                <el-divider />
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