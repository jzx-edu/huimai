<template>
  <div class="publish">
    <div class="nav">
      <van-icon name="arrow-left" @click="back" />
      <span class="nav-title">发布作品</span>
    </div>

    <div class="form no-scrollbar">
      <!-- 视频选择 -->
      <div class="block">
        <div class="label">选择视频</div>
        <van-uploader
          v-model="uploaderFile"
          accept="video/*"
          :max-count="1"
          :after-read="afterRead"
          @delete="clearFile"
        >
          <div class="upload-placeholder">
            <van-icon name="photograph" />
            <span>点击选择视频</span>
          </div>
        </van-uploader>
        <video v-if="previewUrl" :src="previewUrl" class="preview" controls playsinline />
      </div>

      <van-cell-group inset class="block">
        <van-field v-model="title" label="标题" placeholder="给作品起个标题" maxlength="64" />
        <van-field
          v-model="description"
          label="描述"
          type="textarea"
          placeholder="介绍一下你的视频（可选）"
          rows="2"
          autosize
        />
        <van-field
          model-value="分类"
          label="分类"
          :model-value="categoryName"
          is-link
          readonly
          @click="showCategoryPicker = true"
        />
        <van-field label="知识型内容">
          <template #input>
            <van-switch v-model="isKnowledge" />
          </template>
        </van-field>
        <van-field
          v-model="tagText"
          label="标签"
          placeholder="多个标签用逗号分隔"
        />
      </van-cell-group>

      <!-- 内容来源 -->
      <div v-if="isKnowledge" class="block sources-block">
        <div class="label">权威出处（可选）</div>
        <div v-for="(s, i) in sources" :key="i" class="source-row">
          <van-field
            :model-value="sourceTypeLabel(s.type)"
            readonly
            is-link
            class="type-field"
            @click="pickSourceType(i)"
          />
          <van-field
            v-model="s.reference"
            placeholder="如 古兰经 2:255"
            class="ref-field"
          />
          <van-icon name="delete" @click="sources.splice(i, 1)" />
        </div>
        <van-button plain size="small" icon="plus" @click="addSource">
          添加出处
        </van-button>
      </div>
    </div>

    <div class="footer safe-bottom">
      <van-button round block type="primary" :loading="submitting" @click="submit">
        发布视频
      </van-button>
    </div>

    <!-- 分类选择 -->
    <van-popup v-model:show="showCategoryPicker" teleport="#popup-root" position="bottom">
      <van-picker
        :columns="categoryColumns"
        @confirm="onCategoryConfirm"
        @cancel="showCategoryPicker = false"
      />
    </van-popup>

    <!-- 出处类型选择 -->
    <van-popup v-model:show="showTypePicker" teleport="#popup-root" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import type { UploaderFileListItem } from 'vant'
import { getCategories } from '../api/modules/category'
import { publishVideo } from '../api/modules/video'
import type { CategoryItem } from '../types'

const router = useRouter()

const categories = ref<CategoryItem[]>([])
const uploaderFile = ref<UploaderFileListItem[]>([])
const rawFile = ref<File | null>(null)
const previewUrl = ref('')
const title = ref('')
const description = ref('')
const categoryId = ref<number | undefined>()
const isKnowledge = ref(false)
const tagText = ref('')
const sources = ref<{ type: string; reference: string }[]>([])
const submitting = ref(false)

const showCategoryPicker = ref(false)
const showTypePicker = ref(false)
let editingTypeIndex = -1

const categoryColumns = computed(() =>
  categories.value.map((c) => ({ text: c.name, value: c.id }))
)
const typeColumns = [
  { text: '古兰经出处', value: 'quran' },
  { text: '圣训出处', value: 'hadith' },
  { text: '其他参考', value: 'other' }
]

const categoryName = computed(
  () => categories.value.find((c) => c.id === categoryId.value)?.name || '请选择'
)

onMounted(async () => {
  categories.value = await getCategories()
})

function afterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const fileItem = Array.isArray(item) ? item[0] : item
  rawFile.value = (fileItem.file as File) || null
  if (rawFile.value) {
    previewUrl.value = URL.createObjectURL(rawFile.value)
  }
}

function clearFile() {
  rawFile.value = null
  previewUrl.value = ''
}

function onCategoryConfirm({ selectedValues }: { selectedValues: (string | number)[] }) {
  categoryId.value = Number(selectedValues[0])
  showCategoryPicker.value = false
}

function sourceTypeLabel(t: string) {
  return typeColumns.find((x) => x.value === t)?.text || '选择类型'
}

function addSource() {
  sources.value.push({ type: 'quran', reference: '' })
}

function pickSourceType(i: number) {
  editingTypeIndex = i
  showTypePicker.value = true
}

function onTypeConfirm({ selectedValues }: { selectedValues: (string | number)[] }) {
  if (editingTypeIndex >= 0) {
    sources.value[editingTypeIndex].type = String(selectedValues[0])
  }
  showTypePicker.value = false
}

function back() {
  router.back()
}

async function submit() {
  if (!rawFile.value) {
    return showFailToast('请先选择视频')
  }
  if (!title.value.trim()) {
    return showFailToast('请填写标题')
  }
  const tags = tagText.value
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
  const validSources = sources.value
    .filter((s) => s.reference.trim())
    .map((s) => `${s.type}|${s.reference.trim()}`)

  submitting.value = true
  try {
    await publishVideo({
      file: rawFile.value,
      title: title.value.trim(),
      description: description.value,
      categoryId: categoryId.value,
      isKnowledge: isKnowledge.value ? 1 : 0,
      tags,
      sources: validSources
    })
    showSuccessToast('发布成功')
    resetForm()
    router.replace('/feed')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  uploaderFile.value = []
  clearFile()
  title.value = ''
  description.value = ''
  tagText.value = ''
  categoryId.value = undefined
  isKnowledge.value = false
  sources.value = []
}
</script>

<style scoped>
.publish {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #0a0f1a;
}
.nav {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: calc(env(safe-area-inset-top) + 12px) 16px 10px;
  font-size: 20px;
  flex-shrink: 0;
}
.nav-title {
  font-size: 16px;
  font-weight: 600;
}
.form {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 12px;
}
.block {
  margin: 14px 0;
}
.label {
  font-size: 13px;
  color: var(--huimai-muted);
  padding: 0 16px 8px;
}
.upload-placeholder {
  width: 100px;
  height: 130px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: #1c2434;
  border-radius: 8px;
  color: var(--huimai-muted);
  font-size: 12px;
  margin-left: 16px;
}
.preview {
  width: 160px;
  margin: 12px 0 0 16px;
  border-radius: 8px;
}
.sources-block {
  padding: 0 16px;
}
.source-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.type-field {
  width: 110px;
  flex-shrink: 0;
}
.ref-field {
  flex: 1;
}
.footer {
  padding: 10px 16px;
  border-top: 1px solid #1c2434;
  background: #0a0f1a;
}
</style>
