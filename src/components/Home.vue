<script setup>
import { ref, computed, onMounted} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus';
import {handleCommand, handleSelect} from '@/router.js'
import { fetchDataRecord, searchUsernamesAPI, toSavePrivateKey } from '@/service/HomeService.js'
import { get, post } from '@/utils/request.js'
import { Search } from '@element-plus/icons-vue'
import { useMenu } from '@/service/useMenu.js'
import { jwtDecode } from 'jwt-decode'
import { string } from 'sockjs-client/lib/utils/random.js'

const activeMenu = ref('1');
const token = sessionStorage.getItem('authToken');
const decoded = jwtDecode(token);  // 解析 JWT Token
const username = decoded.sub;
const userRole = decoded.role;  // 获取当前用户角色

// 定义用户名建议列表
const usernameSuggestions = ref([]);
const name = ref();

// 用户角色对应的可访问菜单项
const { availableMenus } = useMenu(userRole);

// 分页相关数据
const tableData = ref([]);
const currentPage = ref(1); // 当前页
const pageSize = ref(7); // 每页显示条数

// 详情对话框的可见性和选中的行数据
const dialogVisible = ref(false);
const Outline = ref('');

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
  // tableData.value = await fetchDataRecord(tableData);
  // 1. 获取原始数据
  const rawData = await fetchDataRecord();

  // 2. 按时间降序排序（假设时间字段为 `time` 或 `timestamp`）
  if (rawData && rawData.length > 0) {
    rawData.sort((a, b) => {
      // 根据实际字段名调整（如 a.createTime, b.date 等）
      return new Date(b.time) - new Date(a.time); // 从新到旧
    });
  }

  // 3. 赋值给响应式变量（触发视图更新）
  tableData.value = rawData;
  await savePrivateKey();
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
  // Outline.value = row.outline;
  Outline.value = "数据信息：" + row.outline + '\n' + "授权细则：" + row.usagePolicy;

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
  const start = dataId.slice(0, 2);
  const end = dataId.slice(-3);
  return `${start}...${end}`;
}

// 用户名模糊查询
const searchUsernames = async () => {
  if (name === '') {
    usernameSuggestions.value = [];  // 清空建议列表
    return;
  }

  try {
    const response = await searchUsernamesAPI(name.value); // 调用后端接口获取匹配用户名
    usernameSuggestions.value = response.data || [];
    console.log(usernameSuggestions.value);
  } catch (error) {
    console.error("用户名查询失败", error);
  }
};

const savePrivateKey = async () => {
  try {
    document.body.classList.add('blur-active');
    // 1. 检查后端是否已记录私钥下载状态
    const response = await post(`/user/key-status?username=${encodeURIComponent(username)}`);

    // 2. 如果未下载过私钥，强制弹窗要求下载
    if (!response.success) {
      await ElMessageBox.confirm(
        '安全警告：您必须下载私钥文件才能继续使用系统',
        '强制下载',
        {
          confirmButtonText: '立即下载',
          cancelButtonText: '退出登录',
          type: 'error',
          closeOnClickModal: false,  // 禁止点击遮罩关闭
          closeOnPressEscape: false,  // 禁止ESC键关闭
          showClose: false,           // 隐藏关闭按钮
          lockScroll: true,          // 锁定页面滚动
          beforeClose: async (action, instance, done) => {
            if (action === 'confirm') {
              instance.confirmButtonLoading = true;
              try {
                const isSaved = await toSavePrivateKey(username);
                if (!isSaved) {
                  ElMessage.warning('您必须保存私钥才能继续使用系统');
                  instance.confirmButtonLoading = false;
                  return; // 阻止弹窗关闭
                }
                ElMessage.success('私钥下载完成！');
                done();
              } catch (err) {
                ElMessage.error(`下载失败: ${err.message}`);
                instance.confirmButtonLoading = false;
              }
            } else {
              done();
              handleCommand('logout');
            }
          }
        }
      );
    }
  } catch (error) {
    // 3. 网络请求失败的兜底处理
    await ElMessageBox.alert(
      '系统无法验证您的私钥状态，请重新登录后下载',
      '网络错误',
      {
        type: 'error',
        callback: async () => {
          handleCommand('logout');
        }
      }
    );
  }finally {
    // 无论成功或失败，都移除模糊效果
    document.body.classList.remove('blur-active');
  }
};

</script>

<template>
  <el-container style="height: 100vh; width: 100%;">
    <!-- 侧边栏 -->
    <el-aside width="205px" class="custom-aside">
      <div class="logo"><strong>宁波市民卡联合确权数据流转平台</strong></div>
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
      <el-header style="display: flex; align-items: center; gap: 10px;">
        <el-dropdown @command="handleCommand">
          <el-check-tag type="primary" size="large" checked>{{username}}</el-check-tag>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">登出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-tag :disable-transitions="true" type="danger" effect="dark">{{userRole}}</el-tag>
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
                    <el-table-column prop="taskId" label="任务ID" align="center"/>
                    <el-table-column prop="fileName" label="数据名称" align="center" width="200px" />
<!--                    <el-table-column prop="dataId" label="数据ID" align="center" width="70">-->
<!--                      <template #default="scope">-->
<!--                        <el-tooltip :content="scope.row.dataId" placement="top">-->
<!--                          <span>{{ scope.row.dataIdShortened }}</span>-->
<!--                        </el-tooltip>-->
<!--                      </template>-->
<!--                    </el-table-column>-->

                    <el-table-column prop="creator" label="数据所有方" align="center"/>
<!--                    <el-table-column prop="b" label="联合公钥" align="center">-->
<!--                      <template #default="scope">-->
<!--                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.b)">复制公钥</el-button>-->
<!--                      </template>-->
<!--                    </el-table-column>-->
<!--                    <el-table-column prop="y" label="联合签名" align="center">-->
<!--                      <template #default="scope">-->
<!--                        <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.y)">复制签名</el-button>-->
<!--                      </template>-->
<!--                    </el-table-column>-->

                    <el-table-column  label="数据概要" align="center" >
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
                    <el-descriptions header="详细信息" :title="Outline" style="white-space: pre-wrap;">
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
                 <el-form-item label="搜索用户：" style="margin-top: 20px">
                   <el-row>
                     <el-col>
                     <el-input v-model="name" placeholder="输入用户名" />
                     </el-col>
                   </el-row>
                   <el-button :icon="Search" primary style="margin-left: 10px" @click="searchUsernames"/>
                 </el-form-item>
                <!-- 显示模糊搜索到的用户 -->
                <el-table :data="usernameSuggestions" style="width: 100%" v-if="usernameSuggestions.length > 0" stripe :header-cell-style="{'text-align': 'center'}">
                  <el-table-column label="用户名" prop="USERNAME"  align="center"/>
                  <el-table-column label="角色" prop="ROLE"  align="center"/>
                  <el-table-column label="公钥" prop="PUBLIC_KEY"  align="center">
                    <template #default="scope">
                      <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.PUBLIC_KEY)">复制公钥</el-button>
                    </template>
                  </el-table-column>
                </el-table>
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
/* 模糊化效果 + 遮罩层 */
.blur-active .el-container {
  filter: blur(5px);
  transition: filter 0.3s ease;
  pointer-events: none; /* 阻止交互 */
}

/* 半透明遮罩层（增强聚焦效果） */
.blur-active::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  z-index: 999; /* 确保遮罩在内容之上、弹窗之下 */
}

/* 确保 Element UI 弹窗在遮罩层之上 */
.el-message-box {
  z-index: 1000 !important;
}
</style>
