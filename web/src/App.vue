<template>
    <NavBar />
    <router-view />
</template>

<script>
import NavBar from "@/components/NavBar.vue"
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default {
    components: {
    NavBar
    },
}
</script>

<style>
body {
  /*background-image: url("./assets/images/background.png");*/
    background: #C8EBDF;
    background-size: cover;
    /*background-color: #EDF0F2;*/
}
</style>
