<script setup>
import {ref} from "vue";

const show = ref(true)
</script>


<template>
    <!-- 包装内置的 Transition 组件 -->
    <Transition name="slide-fade" :duration="550">
        <div v-if="show">
            <slot></slot> <!-- 向内传递插槽内容 -->
        </div>
    </Transition>
</template>

<style>
.slide-fade-enter-active {
    transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
    transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from, .slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
}

</style>
