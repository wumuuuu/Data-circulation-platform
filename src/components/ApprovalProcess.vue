<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox} from 'element-plus';
// 调用后台接口查找用户名是否存在
import { checkUserExists } from '@/api/user.js';
const activeMenu = ref('3');
const router = useRouter();
// 文件列表
const fileList = ref([])
const keyList = ref([])
const dataFile = ref(null);
const privateKeyFile = ref(null);
// 模拟申请数据
const tableData = ref([
  { id: 1, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 2, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 3, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 4, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 5, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 6, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 7, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 8, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 9, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 10, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 11, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 12, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 13, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 14, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 15, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 16, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
  { id: 17, date: '2024-08-11', username: 'User1', type: '签名申请', details: '需要签名的数据详情1' },
  { id: 18, date: '2024-08-11', username: 'User2', type: '确权申请', details: '需要确权的数据详情2' },
]);

// 表单数据
const form = ref({
  memberSearch: '',
  members: [
    { name: '张三' },
    { name: '李四' },
    { name: '王五' },
    { name: '赵六' },
    { name: '钱七' },
    { name: '孙八' },
    { name: '张三' },
    { name: '李四' },
    { name: '王五' },
    { name: '赵六' },
    { name: '钱七' },
    { name: '孙八' }
  ]
});

// 分页相关数据
const currentPage = ref(1); // 当前页
const pageSize = ref(12); // 每页显示条数

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

// 上传文件前的验证
const handleDataFileUpload = (file) => {
  dataFile.value = file;
  fileList.value = [file];
  return false; // 阻止自动上传
};

const handleKeyFileUpload = (file) => {
  privateKeyFile.value = file;
  keyList.value = [file];
  return false; // 阻止自动上传
};


// 文件删除时的处理函数
const handleRemove = (file, uploadFiles) => {
  // 打印被删除的文件信息和当前文件列表到控制台
  console.log(file, uploadFiles)
}

// 文件数量超过限制时的处理函数
const handleExceed = (files, uploadFiles) => {
  // 显示警告消息，提示用户上传的文件数量已超过限制
  ElMessage.warning(`只能上传一个文件`)
}

// 在文件删除之前执行的钩子函数
const beforeRemove = (uploadFile, uploadFiles) => {
  // 弹出确认对话框，用户确认后返回 true 执行删除操作，取消则返回 false
  return ElMessageBox.confirm(
    `删除${uploadFile.name} 文件?`
  ).then(
    () => true,  // 确认删除
    () => false  // 取消删除
  )
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

// 显示申请详情
const handleClick = (row) => {
  ElMessageBox.alert(row.details, '申请详情', {
    confirmButtonText: '确定',
    callback: () => {}
  });
};

// 同意申请的处理函数
const handleAgree = (id) => {
  const row = tableData.value.find(item => item.id === id); // 根据ID找到对应的记录
  ElMessage({
    message: `已通过${row.username}的${row.type}`,
    type: 'success',
  });
  tableData.value = tableData.value.filter(item => item.id !== id); // 移除记录
};

// 拒绝申请的处理函数
const handleReject = (id) => {
  const row = tableData.value.find(item => item.id === id); // 根据ID找到对应的记录
  ElMessage({
    message: `已拒绝${row.username}的${row.type}`,
    type: 'warning',
  });
  tableData.value = tableData.value.filter(item => item.id !== id); // 移除记录
};

// 删除成员
const removeMember = (member) => {
  const index = form.value.members.findIndex(m => m.name === member.name);
  if (index !== -1) {
    form.value.members.splice(index, 1);
  }
};

// 添加成员，并在添加前向后端查找是否有此用户
const addMember = async () => {
  if (form.value.memberSearch) {
    // 检查成员列表中是否已经存在该用户
    const userAlreadyExists = form.value.members.some(member => member.name === form.value.memberSearch);
    if (userAlreadyExists) {
      alert('该用户已在成员列表中，不能重复添加。');
      form.value.memberSearch = ''; // 清空输入框
      return; // 终止添加流程
    }
    try {
      // 向后端发送请求，查找是否存在此用户
      const response = await checkUserExists(form.value.memberSearch);
      if (response.code === 0) {
        // 如果用户存在，将其添加到成员列表中
        form.value.members.push({ name: form.value.memberSearch });
        form.value.memberSearch = ''; // 清空输入框
        alert('用户添加成功。');
      } else {
        // 如果用户不存在，提示用户
        alert('用户不存在，请检查用户名。');
      }
    } catch (error) {
      // 如果请求出错，提示错误信息
      alert('无法检查用户，请稍后重试。');
    }
  }
};


// 提交表单
const onSubmit = () => {
  const uploadRef = $refs.uploadRef;
  if (dataFile.value && privateKeyFile.value) {
    // 在这里处理任务计算，使用 dataFile.value 和 privateKeyFile.value
    console.log('提交表单，进行任务计算');
    console.log('数据文件:', dataFile.value);
    console.log('私钥文件:', privateKeyFile.value);
  } else {
    alert('请上传数据文件和私钥文件');
  }

  uploadRef.submit(); // 提交上传文件
  console.log('表单提交:', form.value);
};

// 重置表单
const onReset = () => {
  form.value.dataId = '';
  form.value.memberSearch = '';
  form.value.members = [];
  fileList.value = []; // 清空文件列表
};
</script>


<template>
  <el-container style="height: 100vh; width: 100%;">
    <!-- 侧边栏 -->
    <el-aside width="230px" class="custom-aside">
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
          <span class="el-dropdown-link">用户名</span>
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
                <div class="qianming">需要处理的申请</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="date" label="申请时间" align="center" />
                    <el-table-column prop="username" label="用户名" align="center" />
                    <el-table-column prop="type" label="申请类型" align="center" />
                    <el-table-column fixed="right" label="操作" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="handleClick(scope.row)">详细</el-button>
                        <el-button link type="primary" size="small" @click="handleAgree(scope.row.id)">
                          同意
                        </el-button>
                        <el-button link type="primary" size="small" @click="handleReject(scope.row.id)">
                          拒绝
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
                <div class="qianming">流程申请</div>
                <el-divider />
                <el-form :model="form" label-width="100px">


                  <!-- 参与成员部分 -->
                  <el-row class="form-row">
                    <el-col :span="6" class="label-col">参与成员：</el-col>
                    <el-col :span="12" class="input-col">
                      <el-input v-model="form.memberSearch" placeholder="输入用户名"></el-input>
                    </el-col>
                    <el-col :span="5" class="button-col">
                      <el-button type="primary" @click="addMember">添加</el-button>
                    </el-col>
                  </el-row>

                  <div style="height: 45vh; margin-bottom: 20px">
                      <el-table  max-height="45vh" :data="form.members" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                        <el-table-column prop="name" label="成员名称" align="center" />
                        <el-table-column fixed="right" label="操作" align="center">
                          <template #default="scope">
                            <el-button type="text" size="small" @click="removeMember(scope.row)">删除</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                  </div>

                  <!-- 上传文件和添加私钥部分 -->
                  <div style="height: 13vh;">
                    <el-row class="form-row">
                      <el-col :span="12" class="input-col">
                        <el-upload
                          v-model:file-list="fileList"
                          class="upload-demo"
                          multiple
                          :before-upload="handleDataFileUpload"
                          :on-remove="handleRemove"
                          :before-remove="beforeRemove"
                          :limit="1"
                          :on-exceed="handleExceed"
                        >
                          <el-button size="large" type="primary">上传数据文件</el-button>
                        </el-upload>
                      </el-col>
                      <el-col :span="12" class="input-col">
                        <el-upload
                          v-model:file-list="keyList"
                          class="upload-demo"
                          multiple
                          :before-upload="handleKeyFileUpload"
                          :on-remove="handleRemove"
                          :before-remove="beforeRemove"
                          :limit="1"
                          :on-exceed="handleExceed"
                        >
                          <el-button size="large" type="primary">添加私钥</el-button>
                        </el-upload>
                      </el-col>

                    </el-row>
                  </div>


                  <el-row class="form-row">
                    <el-col :span="24" class="input-col">
                      <el-button type="primary" @click="onSubmit">提交</el-button>
                      <el-button @click="onReset">重置</el-button>
                    </el-col>
                  </el-row>
                </el-form>
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
.qianming{
  text-align: center;
  font-size: 22px;
}
.form-row {
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px; /* 行之间的间隔 */
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
