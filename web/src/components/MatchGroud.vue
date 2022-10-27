<template>
    <div class="matchgroud">
        <div class="container">
            <div class="row">
                <div class="col-4">
                    <div class="photo">
                        <img :src="$store.state.user.photo" alt="">
                    </div>
                    <div class="username">
                        {{$store.state.user.username}}
                    </div>
                </div>
                <div class="col-4 select">
                    <select v-model="select_bot" class="form-select" aria-label="Default select example">
                        <option value="-1" selected>亲自出马</option>
                        <option v-for="bot in bots" :key="bot.id"  :value="bot.id">
                            {{bot.title}}
                        </option>
                    </select>
                </div>
                <div class="col-4">
                    <div class="photo">
                        <img :src="$store.state.pk.opponent_photo" alt="">
                    </div>
                    <div class="username">
                        {{$store.state.pk.opponent_username}}
                    </div>
                </div>
            </div>
            <div class="row">
                <div style="text-align: center; padding-top: 5vh">
                    <button @click="change_btn_text" type="button" class="btn btn-danger btn-lg">{{ btn_text }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {ref} from "vue";
import {useStore} from "vuex";
import $ from "jquery";
export default {
    components: {
    },
    setup() {
        let btn_text = ref('开始匹配');
        let bots = ref([]);
        let select_bot = ref("-1");
        const store = useStore();

        const change_btn_text = () => {
            if (btn_text.value === '开始匹配') {
                btn_text.value = '取消匹配';
                store.state.pk.socket.send(JSON.stringify({
                    event: 'start-matching',
                    bot_id: select_bot.value,
                }));
            } else {
                btn_text.value = '开始匹配';
                store.state.pk.socket.send(JSON.stringify({
                    event: 'stop-matching',
                }));
            }
        };

        const refreshBots = () => {
            $.ajax({
                url: "https://www.kingofbots.fun/api/user/bot/getlist/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    bots.value = resp;
                },
            })
        };

        refreshBots();

        return {
            btn_text,
            change_btn_text,
            bots,
            select_bot
        }
    }
}
</script>

<style scoped>
div.matchgroud {
    /*background-image: url("@/assets/images/img.png");*/
    /*background-size: 80vw 80vh;*/
    /*background-repeat: no-repeat;*/
    width: 70vw;
    height: 70vh;
    margin: 40px auto;
}
.photo {
    padding-top: 20vh;
    text-align: center;
}
img {
    width: 25vh;
    border-radius: 50%;
}
.username {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    padding-top: 2vh;
}
.select {
    padding-top: 20vh;
}
</style>