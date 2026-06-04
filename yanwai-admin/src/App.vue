<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup>
</script>

<style>
:root {
  --color-primary: #6366F1;
  --color-primary-light: #818CF8;
  --color-primary-lighter: #A5B4FC;
  --color-primary-dark: #4F46E5;
  --color-primary-bg: #EEF2FF;

  --color-success: #10B981;
  --color-success-light: #D1FAE5;
  --color-warning: #F59E0B;
  --color-warning-light: #FEF3C7;
  --color-danger: #EF4444;
  --color-danger-light: #FEE2E2;
  --color-info: #3B82F6;
  --color-info-light: #DBEAFE;

  --color-text-primary: #1E293B;
  --color-text-regular: #475569;
  --color-text-secondary: #94A3B8;
  --color-text-placeholder: #CBD5E1;

  --color-bg-page: #F1F5F9;
  --color-bg-white: #FFFFFF;
  --color-bg-hover: #F8FAFC;

  --color-border: #E2E8F0;
  --color-border-light: #F1F5F9;

  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --radius-xl: 20px;

  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.08);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.12);

  --space-xs: 8px;
  --space-sm: 12px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 40px;

  --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-md: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;
  --font-size-3xl: 30px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: var(--font-family);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  background-color: var(--color-bg-page);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.fade-page-enter-active,
.fade-page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ── Element Plus Card Overrides ── */
.el-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: var(--shadow-sm) !important;
  transition: all 0.25s ease;
}

.el-card:hover {
  box-shadow: var(--shadow-md) !important;
}

.el-card__header {
  padding: var(--space-md) var(--space-lg) !important;
  border-bottom: 1px solid var(--color-border-light) !important;
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-text-primary);
}

.el-card__body {
  padding: var(--space-lg) !important;
}

/* ── Element Plus Table Overrides ── */
.el-table {
  border-radius: var(--radius-sm) !important;
  overflow: hidden;
}

.el-table th.el-table__cell {
  background-color: var(--color-bg-page) !important;
  color: var(--color-text-regular);
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid var(--color-border) !important;
}

.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #FAFBFC;
}

.el-table__body tr:hover > td.el-table__cell {
  background-color: var(--color-bg-hover) !important;
}

/* ── Element Plus Button Overrides ── */
.el-button {
  border-radius: var(--radius-sm) !important;
  font-weight: 500;
  transition: all 0.2s ease;
}

.el-button--primary {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

.el-button--primary:hover {
  background-color: var(--color-primary-light) !important;
  border-color: var(--color-primary-light) !important;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.35);
}

.el-button--success {
  background-color: var(--color-success) !important;
  border-color: var(--color-success) !important;
}

.el-button--warning {
  background-color: var(--color-warning) !important;
  border-color: var(--color-warning) !important;
}

.el-button--danger {
  background-color: var(--color-danger) !important;
  border-color: var(--color-danger) !important;
}

/* ── Element Plus Input Overrides ── */
.el-input__wrapper {
  border-radius: var(--radius-sm) !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;
  transition: all 0.2s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--color-primary-lighter) inset !important;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(99, 102, 241, 0.12) !important;
}

.el-select .el-input__wrapper {
  border-radius: var(--radius-sm) !important;
}

/* ── Element Plus Tag Overrides ── */
.el-tag {
  border-radius: 20px !important;
  padding: 0 10px !important;
  height: 24px !important;
  line-height: 22px !important;
  font-size: 12px !important;
  font-weight: 500;
  border: none !important;
}

.el-tag--success {
  background-color: var(--color-success-light) !important;
  color: #059669 !important;
}

.el-tag--warning {
  background-color: var(--color-warning-light) !important;
  color: #D97706 !important;
}

.el-tag--danger {
  background-color: var(--color-danger-light) !important;
  color: #DC2626 !important;
}

.el-tag--info {
  background-color: var(--color-info-light) !important;
  color: #2563EB !important;
}

/* ── Element Plus Pagination Overrides ── */
.el-pagination .btn-prev,
.el-pagination .btn-next,
.el-pager li {
  border-radius: var(--radius-sm) !important;
  transition: all 0.2s ease;
}

.el-pager li.is-active {
  background-color: var(--color-primary) !important;
  color: #fff !important;
}

/* ── Element Plus Dialog Overrides ── */
.el-dialog {
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-xl) !important;
}

.el-dialog__header {
  padding: var(--space-lg) var(--space-lg) var(--space-md) !important;
  border-bottom: 1px solid var(--color-border-light);
  margin-right: 0 !important;
}

.el-dialog__title {
  font-size: var(--font-size-lg) !important;
  font-weight: 600 !important;
  color: var(--color-text-primary);
}

.el-dialog__body {
  padding: var(--space-lg) !important;
}

.el-dialog__footer {
  padding: var(--space-md) var(--space-lg) var(--space-lg) !important;
  border-top: 1px solid var(--color-border-light);
}

/* ── Element Plus Form Overrides ── */
.el-form-item__label {
  color: var(--color-text-regular);
  font-weight: 500;
}

/* ── Element Plus Tabs Overrides ── */
.el-tabs__item {
  font-weight: 500;
  transition: color 0.2s ease;
}

.el-tabs__active-bar {
  background-color: var(--color-primary) !important;
}

.el-tabs__item.is-active {
  color: var(--color-primary) !important;
}

/* ── Element Plus Menu Overrides (used inside layout scoped) ── */

/* ── Element Plus Switch Overrides ── */
.el-switch.is-checked .el-switch__core {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

/* ── Element Plus Page Header Overrides ── */
.el-page-header {
  margin-bottom: var(--space-md);
}

.el-page-header__content {
  font-weight: 600;
  color: var(--color-text-primary);
}

/* ── Element Plus Divider Overrides ── */
.el-divider__text {
  background-color: var(--color-bg-white) !important;
  font-weight: 600;
  color: var(--color-text-regular);
}

/* ── Element Plus Breadcrumb Overrides ── */
.el-breadcrumb__inner {
  font-weight: 500;
}

/* ── Element Plus Popconfirm Overrides ── */
.el-popconfirm__action .el-button--primary {
  margin-left: 8px;
}

/* ── Skeleton / Loading Animation ── */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.el-loading-mask {
  backdrop-filter: blur(2px);
}

/* ── Scrollbar Styling ── */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

/* ── Transition utilities ── */
.fade-in {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
