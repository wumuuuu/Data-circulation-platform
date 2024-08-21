<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox} from 'element-plus';
import { addUser, checkUser, handleAgree, handleReject, ListApplications } from '@/service/ApplicationProcessAPI.js'
import { OpenTheSignProcess } from '@/service/SignProcessService.js'

const activeMenu = ref('3');
const router = useRouter();

// 文件列表
const fileList = ref([])
const keyList = ref([])

//选择申请类型
const options = ['签名申请', '确权申请', '仲裁申请'];
const ApplicationType =ref(null);

// 模拟申请数据
const tableData = ref([]);
const username = localStorage.getItem('username');

const memberSearch = ref(null);

// 准备上传的签名人和权限
const signer = ref({
  members: []
});

//提交进行计算的表单
const form = ref({
  fileList: [],
  keyList: [],
  signer: []
});


onMounted(() => {
  fetchApplications();
});

// 获取申请列表
const fetchApplications = async () => {
  await ListApplications(tableData);
};

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

// 处理私钥文件选择（但不上传）
const handleDataSelection = async (file) => {
  console.log(file instanceof File);
  if (fileList.value.length >= 1) {
    ElMessage.warning('只能上传一个数据文件');
    return false; // 阻止文件添加
  }
  ElMessage.success("文件添加成功");
  fileList.value.push(file);
  form.value.fileList.push(file);
  await addUser(file, signer);

  return false; // 阻止自动上传
};

const handleKeySelection = (file) => {

  if (keyList.value.length >= 1) {
    // 如果文件列表中已经有一个文件，提示用户只能上传一个文件
    ElMessage.warning('只能上传一个数据文件');
    return false; // 阻止文件添加
  }
  ElMessage.success("私钥添加成功");
  keyList.value.push(file);  // 手动添加文件到列表
  form.value.keyList.push(file);
  return false; // 阻止自动上传
};

// 自定义删除文件的逻辑
const removeDataFile = () => {
  const index = 0; // 始终删除第一个文件
  if (fileList.value.length > 0) {
    fileList.value.splice(index, 1); // 删除索引为 0 的文件
    ElMessage.success('第一个文件已删除');
  } else {
    ElMessage.warning('没有可删除的文件');
  }
};
const removeKeyFile = () => {
  const index = 0; // 始终删除第一个文件
  if (keyList.value.length > 0) {
    keyList.value.splice(index, 1); // 删除索引为 0 的文件
    ElMessage.success('第一个文件已删除');
  } else {
    ElMessage.warning('没有可删除的文件');
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
  }
};

// 显示申请详情
const handleClick = (row) => {
  // 格式化日期，假设你想使用某种特定格式
  const startDate = row.startDate ? new Date(row.startDate).toLocaleString() : 'N/A';
  const endDate = row.endDate ? new Date(row.endDate).toLocaleString() : 'N/A';

  // 拼接详细信息
  const details = `
    申请详情: ${row.text}<br/>
    时间范围: ${startDate} ~ ${endDate}<br/>
  `;
  ElMessageBox.alert(details, '申请详情', {
    dangerouslyUseHTMLString: true, // 启用 HTML 字符串渲染
    confirmButtonText: '确定',
    callback: () => {}
  });
};

// 同意申请
const onAgree = async (id) => {
  await handleAgree(id, tableData);
};


// 拒绝申请
const onReject = async (id) => {
  // 查找对应的申请记录
  await handleReject(id, tableData);
};

// 删除成员
const removeMember = (member) => {
  const index = signer.value.members.findIndex(m => m.name === member.name);
  if (index !== -1) {
    signer.value.members.splice(index, 1);
  }
};

// 添加成员，并在添加前向后端查找是否有此用户
const addMember = async (memberSearch) => {
  if (memberSearch) {
    // 检查成员列表中是否已经存在该用户
    await checkUser(signer, memberSearch);
  }
};

// 提交表单
const onSubmit = async () => {
  form.value.signer.push(...signer.value.members);
  // form.value.signer.push(signer.value);
  // const uploadRef = $refs.uploadRef;
  // if (dataFile.value && privateKeyFile.value) {
  //   let Type = ApplicationType.value;
  //   if (Type === '签名申请') {
  //     form.value.signer.push(...signer.value.members);
      await OpenTheSignProcess(form);
      console.log(form.value);
  //
  //   } else if (Type === '确权申请') {
  //
  //   } else {
  //
  //   }
  // } else {
  //   alert('请上传数据文件和私钥文件');
  // }
  // uploadRef.submit(); // 提交上传文件
  // console.log('表单提交:', signer.value);
};

// 重置表单
const onReset = () => {
  signer.value.members = [];
  fileList.value = []; // 清空文件列表
  keyList.value = []; // 清空私钥列表
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
                <div class="sign">需要处理的申请</div>
                <el-divider />
                <div style="height: 66vh;">
                  <el-table height="66vh" :data="paginatedData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="applicationTime" label="申请时间" align="center" />
                    <el-table-column prop="username" label="用户名" align="center" />
                    <el-table-column prop="applicationType" label="申请类型" align="center" />
                    <el-table-column fixed="right" label="操作" align="center">
                      <template #default="scope">
                        <el-button link type="primary" size="small" @click="handleClick(scope.row)">详细</el-button>
                        <el-button link type="primary" size="small" @click="onAgree(scope.row.id)">
                          同意
                        </el-button>
                        <el-button link type="primary" size="small" @click="onReject(scope.row.id)">
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
                <div class="sign">流程申请</div>
                <el-divider />
                <el-form :model="signer" label-width="100px">


                  <!-- 参与成员部分 -->
                  <el-row class="form-row">
                    <el-col :span="6" class="label-col">参与成员：</el-col>
                    <el-col :span="12" class="input-col">
                      <el-input v-model="memberSearch" placeholder="输入用户名"></el-input>
                    </el-col>
                    <el-col :span="5" class="button-col">
                      <el-button type="primary" @click="addMember(memberSearch)">添加</el-button>
                    </el-col>
                  </el-row>

                  <div style="height: 40vh; margin-bottom: 20px">
                      <el-table  max-height="45vh" :data="signer.members" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                        <el-table-column prop="username" label="成员名称" align="center" />
                        <el-table-column label="数据权限" align="center">
                          <template #default="scope">
                            <el-select v-model="scope.row.role"  style="width:100%">
                              <el-option label="可查看" value="可查看"></el-option>
                              <el-option label="可下载" value="可下载"></el-option>
                              <el-option label="可流转" value="可流转"></el-option>
                            </el-select>
                          </template>
                        </el-table-column>
                        <el-table-column fixed="right" label="操作" align="center">
                          <template #default="scope">
                            <el-button type="text" size="small" @click="removeMember(scope.row)">删除</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                  </div>

                  <!-- 上传文件和添加私钥部分 -->
                  <div style="height: 17vh;margin-bottom: 20px">
                    <el-row style="margin-bottom: 20px">
                      <el-col style="display: flex; justify-content: center;">
                        <el-segmented v-model="ApplicationType" :options="options" block style="width: 86%"/>
                      </el-col>
                    </el-row>
                    <el-row class="form-row">
                      <el-col :span="12" class="input-col">
                        <el-upload
                          class="upload-demo"
                          :before-upload="handleDataSelection"
                        >
                          <el-button size="large" type="primary">上传数据文件</el-button>
                        </el-upload>

                      </el-col>
                      <el-col :span="12" class="input-col">
                        <el-upload
                          class="upload-demo"
                          :before-upload="handleKeySelection"
                        >
                          <el-button size="large" type="primary">添加私钥计算</el-button>
                        </el-upload>

                      </el-col>
                    </el-row>
                    <el-row class="form-row">
                      <el-col :span="12" class="input-col">
                        <!-- 自定义文件列表显示 -->
                        <ul v-if="fileList.length > 0">
                          {{ fileList[0].name }}
                         <el-button type="text" @click="removeDataFile">删除</el-button>
                        </ul>
                      </el-col>
                      <el-col :span="12" class="input-col">
                        <!-- 自定义文件列表显示 -->
                        <ul v-if="keyList.length > 0">
                          {{ keyList[0].name }}
                          <el-button type="text" @click="removeKeyFile">删除</el-button>
                        </ul>
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
