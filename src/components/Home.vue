<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus';
import {handleCommand, handleSelect} from '@/router.js'
import { fetchDataRecord } from '@/service/HomeService.js'

const activeMenu = ref('1');
const username = localStorage.getItem('username');
const userRole = localStorage.getItem('role');  // 获取当前用户角色

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

// 分页相关数据
const tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数

// 详情对话框的可见性和选中的行数据
const dialogVisible = ref(false);
const Outline = ref();

// 计算分页后的数据
const formattedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return tableData.value.slice(start, end).map(record => {
    return {
      ...record,
      usageTimeFormatted: formatDate(record.time),
      dataIdShortened: shortenDataId(record.dataId)
    };
  });
});

onMounted(async () => {
  tableData.value = await fetchDataRecord(tableData);
  console.log(tableData.value);
});


function formatDate(dateString) {
  const date = new Date(dateString);
  const formattedDate = date.toLocaleDateString(); // 日期部分
  const formattedTime = date.toLocaleTimeString(); // 时间部分
  return `${formattedDate}\n${formattedTime}`;
}

// 点击查看按钮的处理函数
const onCheck = async (row) => {
  dialogVisible.value = true;
  Outline.value = row.outline;
  console.log(row.outline);
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
function shortenDataId(dataId) {
  const maxLength = 6; // 显示的字符总长度（首尾各5个字符）
  if (dataId.length <= maxLength) return dataId;
  const start = dataId.slice(0, 4);
  const end = dataId.slice(-4);
  return `${start}...${end}`;
}
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
            <el-col :span="16">
              <el-card style="height: 87vh;">
                <div class="sign">数据流转记录</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="formattedTableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="time" label="流转时间" align="center" />
                    <el-table-column prop="taskId" label="任务ID" align="center" />
                    <el-table-column prop="dataId" label="数据ID" align="center">
                      <template #default="scope">
                        <el-tooltip :content="scope.row.dataId" placement="top">
                          <span>{{ scope.row.dataIdShortened }}</span>
                        </el-tooltip>
                      </template>
                    </el-table-column>
                    <el-table-column prop="creator" label="数据所有方" align="center"/>
                    <el-table-column prop="b" label="联合公钥" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.b)">复制公钥</el-button>
                      </template>
                    </el-table-column>
                    <el-table-column prop="y" label="联合签名" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.y)">复制签名</el-button>
                      </template>
                    </el-table-column>

                    <el-table-column fixed="right" label="详情" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="onCheck(scope.row)">查看</el-button>
                      </template>
                    </el-table-column>
                  </el-table>

                  <!-- 详情对话框 -->
                  <el-dialog
                    v-model="dialogVisible"
                    title="数据大纲"
                    width="50%"
                    @close="dialogVisible = false"
                    :append-to-body="true"
                  >
                    <el-descriptions header="详细信息" :title="Outline" >
                      {{Outline}}
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
/* 基本设置 */
body, html {
  font-family: 'Arial', sans-serif;
  color: #333;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-size: 14px; /* 设置基础字体大小，方便整体适配 */
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
  font-size: 15px; /* 侧边栏菜单字体大小适中 */
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
  font-size: 15px; /* 顶部栏头像文字大小 */
  transition: color 0.3s ease;
}

.el-avatar:hover {
  color: #ffd04b;
}

/* 内容区 */
.sign {
  font-size: 16px; /* 调整内容区标题大小 */
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
}

.el-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.el-table th, .el-table td {
  font-size: 13px; /* 表格内容字体小而精炼 */
}

/* 表单 */
.form-row {
  margin-top: 15px;
}

.label-col {
  font-size: 14px;
  font-weight: bold;
  text-align: right;
  color: #333;
}

.input-col {
  padding-left: 10px;
}

.el-input,
.el-select {
  width: 100%;
  transition: border-color 0.3s ease;
}

.el-input:focus,
.el-select:focus {
  border-color: #ffd04b;
}

.button-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.el-button {
  font-size: 14px;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 上传部分 */
.el-upload .el-button {
  margin-right: 10px;
}

.el-progress {
  width: 100%;
  font-size: 13px; /* 调整进度条文字大小 */
}

/* 分页 */
.el-pagination {
  margin-top: 20px;
  font-size: 13px; /* 分页文字适配 */
}

/* 弹出卡片 */
.el-card .close-btn {
  font-size: 24px;
  color: #409eff;
  position: absolute;
  right: 15px;
  top: 10px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.el-card .close-btn:hover {
  color: #ffd04b;
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
</style>
