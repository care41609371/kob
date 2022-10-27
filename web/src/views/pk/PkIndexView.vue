<template>
    <PlayGroud v-if="$store.state.pk.status === 'playing'" />
    <MatchGroud v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser !== 'none'"/>
</template>

<script>
import PlayGroud from "@/components/PlayGroud"
import {onMounted, onUnmounted} from "vue"
import {useStore} from "vuex"
import MatchGroud from "@/components/MatchGroud";
import ResultBoard from '@/components/ResultBoard'

export default {
    components: {
        PlayGroud,
        MatchGroud,
        ResultBoard,
    },
    setup() {
        const store = useStore();
        // const url = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;
        const url = `wss://www.kingofbots.fun/websocket/${store.state.user.token}/`;

        store.commit("updateIsRecord", false);
        store.commit('updateLoser', 'none')

        let socket = null;
        onMounted(() => {
            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            });
            socket = new WebSocket(url);

            socket.onopen = () => {
                store.commit('updateSocket', socket);
            };

            socket.onclose = () => {

            };

            socket.onmessage = (msg) => {
                const data = JSON.parse(msg.data);
                if (data.event === 'matching-success') {
                    store.commit('updateOpponent', {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    store.commit('updateGame', data.game);
                    setTimeout(() => {
                        store.commit('updateStatus', "playing");
                    }, 1000);
                }  else if (data.event === 'move') {
                    const [snake0, snake1] = store.state.pk.gameObject.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                } else if (data.event === 'result') {
                    const [snake0, snake1] = store.state.pk.gameObject.snakes;
                    if (data.loser === 'all' || data.loser === 'A') {
                        snake0.status = 'die';
                    }
                    if (data.loser === 'all' || data.loser === 'B') {
                        snake1.status = 'die';
                    }
                    store.commit('updateLoser', data.loser);
                }
            };
        });

        onUnmounted(() => {
            socket.close();
            store.commit('updateStatus', "matching");
        });
    }
}
</script>

<style scoped>

</style>