<template>
    <div class="container">
        <div class="row">
            <div class="col-8">
                <div class="playgroud">
                    <GameMap />
                </div>
            </div>
            <div class="col-1">
                <div class="container content-field" style="width: 20vw; margin-top: 50px">
                    <div class="card">
                        <div class="card-body content">
                            <div class="red">红色方:&nbsp; {{red}}</div>
                            <div class="blue">蓝色方:&nbsp; {{blue}}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>

<script>
import GameMap from '@/components/GameMap.vue';
import {ref} from 'vue'
import {useStore} from "vuex";

export default {
    components: {
        GameMap,
    },
    setup() {
        const store = useStore();
        let blue = ref('');
        let red = ref('');

        if (parseInt(store.state.pk.a_id) === parseInt(store.state.user.id)) {
            blue = store.state.user.username;
            red = store.state.pk.opponent_username;
        } else {
            red = store.state.user.username;
            blue = store.state.pk.opponent_username;
        }

        return {
            blue,
            red,
        }
    }
}
</script>

<style scoped>
div.playgroud {
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
}
.content-field {
    font-style: italic;
}
.content {
    text-align: left;
    margin-left: 30px;
}
.red {
    font-size: 19px;
    font-weight: 600;
    color: #F94848;
}
.blue {
    font-size: 19px;
    font-weight: 600;
    color: #4876EC;
}
</style>