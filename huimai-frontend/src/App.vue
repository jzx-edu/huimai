<template>
  <router-view />
  <!-- 弹层统一挂载点：与路由内容隔离，避免路由切换时 teleport 节点与组件节点错位 -->
  <div id="popup-root"></div>
</template>

<script setup lang="ts"></script>
