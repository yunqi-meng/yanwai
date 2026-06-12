<template>
  <div class="friends-container">
    <div class="friends-header">
      <h2>👥 好友系统</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="好友列表" name="list">
        <div class="friend-list">
          <div
            v-for="friend in friendList"
            :key="friend.userId"
            class="friend-item"
          >
            <div class="friend-avatar">
              <el-avatar :size="48">{{ friend.nickname?.charAt(0) || '?' }}</el-avatar>
            </div>
            <div class="friend-info">
              <span class="nickname">{{ friend.nickname }}</span>
              <span class="stats">分析: {{ friend.totalAnalysis }} | 传说: {{ friend.legendCount }}</span>
            </div>
            <el-button type="danger" size="small" @click="handleRemoveFriend(friend.userId)">
              删除
            </el-button>
          </div>
          <el-empty v-if="friendList.length === 0" description="暂无好友" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="好友申请" name="requests">
        <div class="request-list">
          <div
            v-for="request in pendingRequests"
            :key="request.id"
            class="request-item"
          >
            <div class="request-info">
              <span class="nickname">{{ request.fromNickname }}</span>
              <span class="message" v-if="request.message">{{ request.message }}</span>
              <span class="time">{{ formatTime(request.createdAt) }}</span>
            </div>
            <div class="request-actions">
              <el-button type="primary" size="small" @click="handleRequest(request.id, true)">
                同意
              </el-button>
              <el-button type="danger" size="small" @click="handleRequest(request.id, false)">
                拒绝
              </el-button>
            </div>
          </div>
          <el-empty v-if="pendingRequests.length === 0" description="暂无好友申请" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="添加好友" name="add">
        <div class="add-friend">
          <el-input
            v-model="searchUserId"
            placeholder="输入用户ID"
            type="number"
          >
            <template #append>
              <el-button @click="handleAddFriend">发送申请</el-button>
            </template>
          </el-input>
          <el-input
            v-model="addMessage"
            placeholder="申请消息（可选）"
            type="textarea"
            :rows="2"
            style="margin-top: 12px"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFriendList, getPendingRequests, handleFriendRequest, sendFriendRequest, removeFriend } from '@/api/social'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('list')
const friendList = ref([])
const pendingRequests = ref([])
const searchUserId = ref('')
const addMessage = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const fetchFriendList = async () => {
  try {
    const res = await getFriendList()
    if (res.code === 200) {
      friendList.value = res.data
    }
  } catch (error) {
    console.error('获取好友列表失败', error)
  }
}

const fetchPendingRequests = async () => {
  try {
    const res = await getPendingRequests()
    if (res.code === 200) {
      pendingRequests.value = res.data
    }
  } catch (error) {
    console.error('获取好友申请失败', error)
  }
}

const handleRequest = async (requestId, accept) => {
  try {
    const res = await handleFriendRequest(requestId, accept)
    if (res.code === 200) {
      ElMessage.success(res.message)
      fetchPendingRequests()
      fetchFriendList()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAddFriend = async () => {
  if (!searchUserId.value) {
    ElMessage.warning('请输入用户ID')
    return
  }
  try {
    const res = await sendFriendRequest(Number(searchUserId.value), addMessage.value)
    if (res.code === 200) {
      ElMessage.success(res.message)
      searchUserId.value = ''
      addMessage.value = ''
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('发送申请失败')
  }
}

const handleRemoveFriend = async (friendId) => {
  try {
    await ElMessageBox.confirm('确定删除该好友？', '提示', { type: 'warning' })
    const res = await removeFriend(friendId)
    if (res.code === 200) {
      ElMessage.success(res.message)
      fetchFriendList()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchFriendList()
  fetchPendingRequests()
})
</script>

<style scoped lang="scss">
.friends-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.friends-header {
  text-align: center;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
  }
}

.friend-list, .request-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.friend-item, .request-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.friend-avatar {
  margin-right: 12px;
}

.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;

  .nickname {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .stats {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.request-info {
  flex: 1;
  display: flex;
  flex-direction: column;

  .nickname {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .message {
    font-size: 14px;
    color: #666;
    margin-top: 4px;
  }

  .time {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.request-actions {
  display: flex;
  gap: 8px;
}

.add-friend {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>
