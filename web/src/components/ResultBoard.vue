<template>
    <div class="result-board">
        <div class="result-board-text" v-if="$store.state.pk.loser === 'all'">
            Draw
        </div>
        <div class="result-board-text" v-else-if="$store.state.pk.loser === 'A' && parseInt($store.state.user.id) === $store.state.pk.a_id">
            Lose
        </div>
        <div class="result-board-text" v-else-if="$store.state.pk.loser === 'B' && parseInt($store.state.user.id) === $store.state.pk.b_id">
            Lose
        </div>
        <div class="result-board-text" v-else>
            Win
        </div>
        <div class="result-board-btn">
            <button @click="restart" type="button" class="btn btn-danger btn-lg">
                再来!
            </button>
        </div>
    </div>
</template>

<script>
import {useStore} from "vuex";

export default {
    setup() {
        const store = useStore();
        const restart = () => {
            store.commit('updateStatus', "matching");
            store.commit('updateLoser', "none");
            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            });
        };
        return {
            restart,
        }
    }
}
</script>

<style scoped>
.result-board {
    height: 30vh;
    width: 30vw;
    position: absolute;
    top: 35vh;
    left: 35vw;
    background-color: rgba(50, 50, 50, 0.5);
}
.result-board-text {
    text-align: center;
    padding-top: 5vh;
    font-weight: 600;
    font-size: 50px;
    color: aliceblue;
    font-style: italic;
}
.result-board-btn {
    text-align: center;
    padding-top: 6vh;
}
</style>