<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :modal="modal"
    draggable
    destroy-on-close
    :close-on-click-modal="false"
    append-to-body
    :before-close="beforeClose"
    @close="dialogClose"
    @closed="dialogClosed"
  >
    <slot />
    <template #footer>
      <span class="dialog-footer">
        <slot name="footer" />
      </span>
    </template>
  </el-dialog>
</template>
<script setup name="Dialog">
import { ElDialog } from "element-plus";
// eslint-disable-next-line no-undef
defineProps({
  modelValue: {
    type: [Boolean],
    default: false,
  },
  title: {
    type: [String],
    default: "",
  },
  width: {
    type: [String],
    default: "",
  },
  modal: {
    type: [Boolean],
    default: false,
  },
  beforeClose: [Function],
});
// eslint-disable-next-line no-undef
const emits = defineEmits(["update:modelValue", "onClosed", "closed"]);
function dialogClose() {
  emits("update:modelValue", false);
}
function dialogClosed() {
  emits("onClosed", false);
  emits("closed", false);
}
</script>

<style lang="scss">
</style>
