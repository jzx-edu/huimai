<template>
  <div class="shop">
    <div class="header">
      <div class="top-bar">
        <van-search
          v-model="keyword"
          class="search"
          placeholder="搜索商品"
          shape="round"
          @search="onSearch"
          @clear="onSearch"
        />
        <div class="order-entry" @click="openOrders">
          <van-icon name="orders-o" />
          <span>订单</span>
        </div>
      </div>
      <div class="chips no-scrollbar">
        <span
          v-for="c in categories"
          :key="c"
          class="chip"
          :class="{ active: activeCategory === c }"
          @click="selectCategory(c)"
        >
          {{ c }}
        </span>
      </div>
    </div>

    <div class="goods-grid no-scrollbar">
      <van-loading v-if="loading" class="loading" size="24" vertical>加载中...</van-loading>
      <van-empty v-else-if="goodsList.length === 0" description="没有相关商品" />
      <div
        v-for="g in goodsList"
        v-else
        :key="g.id"
        class="goods-card"
        @click="openGoods(g)"
      >
        <div class="cover" :style="g.coverImage ? {} : { background: g.gradient }">
          <img v-if="g.coverImage" :src="assetUrl(g.coverImage)" alt="" />
          <van-icon v-else :name="g.icon" />
        </div>
        <div class="info">
          <div class="name">{{ g.name }}</div>
          <div class="line">
            <span class="price">¥{{ formatPrice(g.price) }}</span>
            <span class="sold">已售 {{ g.soldCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情 / 下单 -->
    <van-popup
      v-model:show="detailVisible"
      position="bottom"
      round
      teleport="#popup-root"
      close-on-click-overlay
    >
      <div v-if="currentProduct" class="detail">
        <div class="d-cover" :style="currentProduct.coverImage ? {} : { background: currentProduct.gradient }">
          <img v-if="currentProduct.coverImage" :src="assetUrl(currentProduct.coverImage)" alt="" />
          <van-icon v-else :name="currentProduct.icon" />
        </div>
        <div class="d-body">
          <div class="d-name">{{ currentProduct.name }}</div>
          <div class="d-price-row">
            <span class="d-price">¥{{ formatPrice(currentProduct.price) }}</span>
            <span class="d-stock">库存 {{ currentProduct.stock }}</span>
          </div>
          <div class="d-desc">{{ currentProduct.description }}</div>
          <div class="d-buy">
            <span class="d-label">数量</span>
            <van-stepper
              v-model="buyQuantity"
              :min="1"
              :max="currentProduct.stock > 0 ? currentProduct.stock : 1"
              integer
              theme="round"
              button-size="26"
            />
          </div>
          <van-button
            class="d-btn"
            type="primary"
            block
            round
            :loading="submitting"
            :disabled="currentProduct.stock <= 0"
            @click="submitOrder"
          >
            {{ currentProduct.stock <= 0 ? '已售罄' : '立即下单' }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 我的订单 -->
    <van-popup
      v-model:show="orderSheetVisible"
      position="bottom"
      round
      teleport="#popup-root"
      :style="{ height: '82%' }"
    >
      <div class="orders">
        <div class="o-title">我的订单</div>
        <div class="o-list no-scrollbar">
          <van-empty v-if="orders.length === 0" description="还没有订单" />
          <div v-for="o in orders" :key="o.id" class="o-card">
            <div class="o-head">
              <span class="o-no">订单号 {{ o.orderNo }}</span>
              <span class="o-status" :class="statusClass(o.status)">{{ statusText(o.status) }}</span>
            </div>
            <div class="o-main">
              <div class="o-cover" :style="o.productCoverImage ? {} : { background: o.productGradient }">
                <img v-if="o.productCoverImage" :src="assetUrl(o.productCoverImage)" alt="" />
                <van-icon v-else :name="o.productIcon" />
              </div>
              <div class="o-info">
                <div class="o-name">{{ o.productName || '商品已下架' }}</div>
                <div class="o-qty">x{{ o.quantity }}</div>
              </div>
              <div class="o-amount">¥{{ formatPrice(o.totalAmount) }}</div>
            </div>
            <div class="o-time">{{ o.createdAt }}</div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showSuccessToast } from 'vant'
import {
  createOrder,
  getMyOrders,
  getProduct,
  getProducts
} from '../api/modules/product'
import { assetUrl } from '../utils/url'
import type { Product, ProductOrder } from '../types'

const keyword = ref('')
const activeCategory = ref('全部')

const categories = ['全部', '图书音像', '知识课程', '健康食品', '家居生活', '文创精品']

const goodsList = ref<Product[]>([])
const loading = ref(false)

// 商品详情 / 下单状态
const detailVisible = ref(false)
const currentProduct = ref<Product | null>(null)
const buyQuantity = ref(1)
const submitting = ref(false)

// 我的订单状态
const orderSheetVisible = ref(false)
const orders = ref<ProductOrder[]>([])

async function loadProducts() {
  loading.value = true
  try {
    goodsList.value = await getProducts({
      category: activeCategory.value === '全部' ? undefined : activeCategory.value,
      keyword: keyword.value.trim() || undefined
    })
  } finally {
    loading.value = false
  }
}

function selectCategory(c: string) {
  activeCategory.value = c
  loadProducts()
}

function onSearch() {
  loadProducts()
}

async function openGoods(g: Product) {
  // 拉取详情，确保库存与描述为最新
  currentProduct.value = await getProduct(g.id)
  buyQuantity.value = 1
  detailVisible.value = true
}

async function submitOrder() {
  if (!currentProduct.value || submitting.value) return
  submitting.value = true
  try {
    await createOrder({
      productId: currentProduct.value.id,
      quantity: buyQuantity.value
    })
    showSuccessToast('下单成功')
    detailVisible.value = false
    loadProducts()
  } finally {
    submitting.value = false
  }
}

async function openOrders() {
  orderSheetVisible.value = true
  orders.value = await getMyOrders()
}

function formatPrice(n: number) {
  return Number(n).toFixed(2)
}

function statusText(s: number) {
  return ['待支付', '已支付', '已取消'][s] ?? '未知'
}

function statusClass(s: number) {
  return ['s-pending', 's-paid', 's-cancel'][s] ?? ''
}

onMounted(loadProducts)
</script>

<style scoped>
.shop {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #0a0f1a;
}
.header {
  /* 真机刘海/灵动岛：搜索栏下移到状态栏下方 */
  padding-top: env(safe-area-inset-top);
  background: #0f1524;
}
.top-bar {
  display: flex;
  align-items: center;
}
.top-bar .search {
  flex: 1;
}
.order-entry {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 12px;
  color: #c3ccdb;
}
.order-entry .van-icon {
  font-size: 20px;
}
.order-entry span {
  font-size: 11px;
  margin-top: 2px;
}
.chips {
  display: flex;
  gap: 8px;
  padding: 0 12px 10px;
  overflow-x: auto;
}
.chip {
  flex-shrink: 0;
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 13px;
  background: #1c2434;
  color: #c3ccdb;
}
.chip.active {
  background: var(--huimai-primary);
  color: #fff;
}
.goods-grid {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  align-content: start;
}
.loading {
  grid-column: 1 / -1;
  margin-top: 60px;
}
.goods-card {
  border-radius: 10px;
  overflow: hidden;
  background: #141b2c;
  border: 1px solid #1f2940;
}
.cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover .van-icon {
  font-size: 40px;
  color: rgba(255, 255, 255, 0.92);
}
.info {
  padding: 8px 9px 10px;
}
.name {
  font-size: 13px;
  color: #eef2f9;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}
.price {
  font-size: 15px;
  font-weight: 700;
  color: var(--huimai-gold);
}
.sold {
  font-size: 11px;
  color: #6b7891;
}

/* 商品详情 */
.detail {
  background: #0f1524;
}
.d-cover {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.d-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.d-cover .van-icon {
  font-size: 64px;
  color: rgba(255, 255, 255, 0.92);
}
.d-body {
  padding: 16px;
}
.d-name {
  font-size: 17px;
  font-weight: 700;
  color: #eef2f9;
}
.d-price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 10px;
}
.d-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--huimai-gold);
}
.d-stock {
  font-size: 12px;
  color: #6b7891;
}
.d-desc {
  margin-top: 14px;
  font-size: 13px;
  line-height: 1.7;
  color: #9aa7bd;
}
.d-buy {
  display: flex;
  align-items: center;
  margin-top: 18px;
}
.d-label {
  font-size: 14px;
  color: #c3ccdb;
  margin-right: 14px;
}
.d-btn {
  margin-top: 20px;
}

/* 我的订单 */
.orders {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #0a0f1a;
}
.o-title {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #eef2f9;
  padding: 14px 0;
}
.o-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 16px;
}
.o-card {
  background: #141b2c;
  border: 1px solid #1f2940;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 10px;
}
.o-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.o-no {
  font-size: 12px;
  color: #6b7891;
}
.o-status {
  font-size: 12px;
  font-weight: 600;
}
.s-pending {
  color: var(--huimai-gold);
}
.s-paid {
  color: var(--huimai-primary);
}
.s-cancel {
  color: #6b7891;
}
.o-main {
  display: flex;
  align-items: center;
  margin-top: 10px;
}
.o-cover {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.o-cover img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}
.o-cover .van-icon {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.92);
}
.o-info {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}
.o-name {
  font-size: 14px;
  color: #eef2f9;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.o-qty {
  font-size: 12px;
  color: #6b7891;
  margin-top: 6px;
}
.o-amount {
  font-size: 15px;
  font-weight: 700;
  color: var(--huimai-gold);
}
.o-time {
  margin-top: 10px;
  font-size: 11px;
  color: #6b7891;
}
</style>
