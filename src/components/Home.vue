<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ListDataRecord, QueryPermissions } from '@/service/HomeAPI.js'

const activeMenu = ref('1');
const router = useRouter();
const username = localStorage.getItem('username');

// 分页相关数据
const tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数

// 详情对话框的可见性和选中的行数据
const dialogVisible = ref(false);
const selectedRow = ref({});

// 计算分页后的数据
const formattedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end).map(record => {
    return {
      ...record,
      usageTimeFormatted: formatDate(record.usageTime),
      dataidShortened: shortenDataId(record.dataid)
    };
  });
});

onMounted(() => {
  fetchDataRecord();
});

// 获取数据记录列表
const fetchDataRecord = async () => {
  await ListDataRecord(tableData);
};

function formatDate(dateString) {
  const date = new Date(dateString);
  const formattedDate = date.toLocaleDateString(); // 日期部分
  const formattedTime = date.toLocaleTimeString(); // 时间部分
  return `${formattedDate}\n${formattedTime}`;
}

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
  }
};

// 处理登出
const handleCommand = (command) => {
  if (command === 'logout') {
    // 清除本地存储中的用户信息
    localStorage.clear();
    // 跳转到登录页面
    router.push('/');
  }
};

// 点击查看按钮的处理函数
const onCheck = async (row) => {
  // 查找是否有权限
  let role = await QueryPermissions(row.dataid);
  if (role) {
    selectedRow.value = row; // 存储选中的行数据
    dialogVisible.value = true; // 打开对话框
  } else {
    ElMessage.error('没有权限');
  }
};

// 点击下载按钮的处理函数
const onDownload = async (row) => {
  // 查找是否有权限
  let role = await QueryPermissions(row.dataid);
  if (role === '可下载' || role === '可流转') {
    // 处理下载逻辑
  } else {
    ElMessage.error('没有权限');
  }
};

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败');
  });
};

// 缩短数据ID中间部分的方法
function shortenDataId(dataid) {
  const maxLength = 10; // 显示的字符总长度（首尾各5个字符）
  if (dataid.length <= maxLength) return dataid;
  const start = dataid.slice(0, 8);
  const end = dataid.slice(-5);
  return `${start}...${end}`;
}
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
                <div class="sign">数据流转记录</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="formattedTableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="usageTimeFormatted" label="流转时间" align="center" />
                    <el-table-column prop="dataidShortened" label="数据ID" align="center">
                      <template #default="scope">
                        <el-tooltip :content="scope.row.dataid" placement="top">
                          <span>{{ scope.row.dataidShortened }}</span>
                        </el-tooltip>
                      </template>
                    </el-table-column>

                    <el-table-column prop="signb" label="联合公钥" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.signb)">复制公钥</el-button>
                      </template>
                    </el-table-column>
                    <el-table-column label="联合签名" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.signy)">复制签名</el-button>
                      </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="onCheck(scope.row)">查看数据</el-button>
                        <el-button link type="primary" size="small" @click="onDownload(scope.row)">下载数据</el-button>
                      </template>
                    </el-table-column>
                  </el-table>

                  <!-- 详情对话框 -->
                  <el-dialog
                    v-model="dialogVisible"
                    title="详情信息"
                    width="50%"
                    @close="dialogVisible = false"
                    :append-to-body="true"
                  >
                    <el-descriptions header="详细信息">
                      111111
                    </el-descriptions>
                    <span slot="footer" class="dialog-footer">
                      <el-button @click="dialogVisible = false">关闭</el-button>
                    </span>
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
            <el-col :span="8">
              <el-card style="height: 87vh;">
                <!-- 其他内容 -->
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

.sign{
  text-align: center;
  font-size: 22px;
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
