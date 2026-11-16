<template>
  <div class="login-page">
    <div class="brand">
      <h1 class="brand-name">回脉</h1>
      <p class="subtitle">家庭友好 · 知识导向的短视频平台</p>
    </div>

    <van-form class="form" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          {{ mode === 'login' ? '登 录' : '注 册' }}
        </van-button>
        <p class="switch-mode" @click="switchMode">
          {{ mode === 'login' ? '没有账号？去注册' : '已有账号？去登录' }}
        </p>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { login, register } from '../api/modules/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const mode = ref<'login' | 'register'>('login')
const loading = ref(false)
const form = reactive({ username: '', password: '' })

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
}

async function handleSubmit() {
  loading.value = true
  try {
    const res =
      mode.value === 'login'
        ? await login({ ...form })
        : await register({ ...form })
    userStore.setLogin(res)
    showSuccessToast({
      message: mode.value === 'login' ? '登录成功' : '注册成功',
      teleport: '#app'
    })
    const redirect = (route.query.redirect as string) || '/feed'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(180deg, #1e40af 0%, #0a0f1a 55%);
  padding: 24px 0;
}
.brand {
  text-align: center;
  margin-bottom: 32px;
}
.brand-name {
  margin: 0 0 10px;
  font-size: 40px;
  font-weight: 700;
  letter-spacing: 6px;
}
.subtitle {
  margin: 0;
  color: var(--huimai-muted);
  font-size: 13px;
  letter-spacing: 1px;
}
.submit-wrap {
  margin: 24px 16px 0;
}
.switch-mode {
  text-align: center;
  color: var(--huimai-gold);
  font-size: 13px;
  margin-top: 16px;
}
</style>
