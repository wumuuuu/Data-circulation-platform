import { getAllUpdateRecords, getUserPermissions } from '@/api/api.js'

export const ListDataRecord = async (tableData)=>{
  try {
    const response = await getAllUpdateRecords();
    if (response && response.code === 200) {
      tableData.value = response.data;
    } else {
      console.error('Error fetching DataRecord:', response);
    }
  } catch (error) {
    console.error('Error fetching DataRecord:', error);
  }
}

export const QueryPermissions = async (dataId)=>{
  try {
    const response = await getUserPermissions(localStorage.getItem('username'), dataId);
    if (response && response.code === 200) {
      console.log(response);
    } else {
      console.error('Error fetching DataRecord:', response);
    }
    return response.data;
  } catch (error) {
    console.error('Error fetching DataRecord:', error);
  }
}