
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
const activeMenu = ref('1');
const router = useRouter();

const form = ref({
  dataId: '',
  memberSearch: '',
  members: []
});

const handleCommand = (command) => {
  if (command === 'logout') {
    router.push('/'); // 返回到登录界面
  }
};
const handleSelect = (index) => {
  // 根据 index 处理导航，例如跳转到不同的页面
  switch (index) {
    case '1':
      router.push('/home');
      break;
    case '2':
      router.push('/home');
      break;
    case '3':
      router.push('/');
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
    <el-container style="height: 97.8vh; width: 100%;">
      <!-- 侧边栏 -->
      <el-aside width="250px" class="custom-aside">
        <div class="logo"> <strong>数据流转平台</strong></div>
        <el-menu :default-active="activeMenu" class="custom-menu" @select="handleSelect">
          <el-menu-item index="1">
            <span>主页</span>
          </el-menu-item>
          <el-menu-item index="2">
            <span>数据</span>
          </el-menu-item>
          <el-menu-item index="3">
            <span>登录</span>
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
                <el-card style="height: 85vh;">
                  <div class = "qianming">
                    流程申请
                  </div>
                  <el-divider />
                  <el-form :model="form" label-width="100px">
                    <!-- 数据编号部分 -->
                    <el-row class="form-row">
                      <el-col :span="6" class="label-col">
                        数据编号：
                      </el-col>
                      <el-col :span="12" class="input-col">
                        <el-input v-model="form.dataId"></el-input>
                      </el-col>
                      <el-col :span="5" class="button-col">
                        <el-button type="primary" @click="addData">添加</el-button>
                      </el-col>
                    </el-row>

                    <!-- 参与成员部分 -->
                    <el-row class="form-row">
                      <el-col :span="6" class="label-col">
                        参与成员：
                      </el-col>
                      <el-col :span="12" class="input-col">
                        <el-input v-model="form.memberSearch" placeholder="输入用户名查找"></el-input>
                      </el-col>
                      <el-col :span="5" class="button-col">
                        <el-button type="primary" @click="addMember">添加</el-button>
                      </el-col>
                    </el-row>

                    <el-table :data="form.members" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                      <el-table-column prop="Name" label="已添加的成员" />
                    </el-table>
                    <el-row>
                      <el-col :offset="7">
                        <el-button type="primary" @click="onSubmit">提交</el-button>
                        <el-button @click="onReset">重置</el-button>
                      </el-col>
                    </el-row>
                  </el-form>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card style="height: 85vh;">
                  <div class = "qianming">
                    正在进行的签名
                  </div>
                  <el-divider />
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
                  <el-button type="primary" plain style="width: 100%;">添加私钥计算</el-button>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card style="height: 85vh;">
                  <div class = "qianming">
                    正在进行的确权
                  </div>
                  <el-divider />
                  <el-table :data="tableData" border style="width: 100%" :header-cell-style="{'text-align': 'center'}">
                    <el-table-column prop="Name" label="用户名" />
                    <el-table-column prop="Status" label="状态"/>
                  </el-table>
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
