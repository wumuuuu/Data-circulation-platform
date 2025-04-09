import { computed } from 'vue';

// 定义所有菜单项（只需在此定义一次）
const ALL_MENUS = Object.freeze([
  { index: '1', name: '主页', icon: 'home', roles: ['Admin', '普通用户', '数据所有方', '审核人员'] },
  { index: '2', name: '申请', icon: 'document-add', roles: ['Admin', '普通用户', '数据所有方'] },
  { index: '3', name: '处理', icon: 'setting', roles: ['Admin', '普通用户', '数据所有方', '审核人员'] },
  { index: '4', name: '数据所有方审批', icon: 'checked', roles: ['Admin', '数据所有方'] },
  { index: '5', name: '审核员审批', icon: 'view', roles: ['Admin', '审核人员'] },
  { index: '6', name: '管理', icon: 'user', roles: ['Admin'] }
]);

/**
 * 获取当前用户可访问的菜单项
 * @param {string} role - 当前用户角色
 * @returns {ComputedRef<Array>} 可访问的菜单项
 */
export function useMenu(role) {
  const availableMenus = computed(() => {
    return ALL_MENUS.filter(menu => menu.roles.includes(role));
  });

  return {
    availableMenus,
    // 如果需要也可以暴露全部菜单
    allMenus: ALL_MENUS
  };
}