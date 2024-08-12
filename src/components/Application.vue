
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const activeMenu = ref('2');
const router = useRouter();

const form = ref({
  dataId: '',
  memberSearch: '',
  members: []
});

//点击登出并跳转到登录界面
const handleCommand = (command) => {
  if (command === 'logout') {
    // 清理登录状态，例如移除 token 或用户信息
    localStorage.clear();
    console.log(localStorage);
    // 然后跳转到登录页面
    router.push('/');
  }
};
const handleSelect = (index) => {
  // 根据 index 处理导航，例如跳转到不同的页面
  switch (index) {
    case '1':
      router.push('/home');
      break;
    case '2':
      router.push('/Application');
      break;
    case '3':
      router.push('/approvalProcess');
      break;
  }
};
const addData = () => {
  console.log('数据编号已添加:', form.value.dataId);
};

const addMember = () => {
  if (form.value.memberSearch) {
    form.value.members.push({ name: form.value.memberSearch });
    form.value.memberSearch = ''; // 清空输入框
  }
};

const onSubmit = () => {
  console.log('表单提交:', form.value);
};

const onReset = () => {
  form.value.dataId = '';
  form.value.memberSearch = '';
  form.value.members = [];
};
</script>
<template>
  <el-container style="height: 100vh; width: 100%;">
    <!-- 侧边栏 -->
    <el-aside width="230px" class="custom-aside">
      <div class="logo"> <strong>数据流转平台</strong></div>
      <el-menu :default-active="activeMenu" class="custom-menu" @select="handleSelect">
        <el-menu-item index="1">
          <span >主页</span>
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
      <span class="el-dropdown-link">
        用户名
      </span>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">登出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 主内容区 -->
      <el-container>
        <!-- 右侧未申请流程展示区 -->
        <el-aside width="100%" style="padding: 20px;">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card style="height: 87vh;">
                <div class = "qianming">
                  正在进行的仲裁
                </div>
                <el-divider />
                <div style="height: 65vh; overflow: auto;">
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                </div>
                <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card style="height: 87vh;">
                <div class = "qianming">
                  正在进行的签名
                </div>
                <el-divider />
                <div style="height: 65vh; overflow: auto;">
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                </div>
                <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card style="height: 87vh;">
                <div class = "qianming">
                  正在进行的确权
                </div>
                <el-divider />
                <div style="height: 65vh; overflow: auto;">
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                </div>
                <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
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
