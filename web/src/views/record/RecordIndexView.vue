<template>
    <ContentField>
        <table class="table table-hover table-striped" style="text-align: center">
            <thead>
                <tr>
                    <th class="col-md-3" style="text-align: left">
                        <div style="margin-left: 5vw">玩家A</div>
                    </th>
                    <th class="col-md-2" style="text-align: left">
                        <div style="margin-left: 3vw">玩家B</div>
                    </th>
                    <th class="col-md-2">对局结果</th>
                    <th class="col-md-2">对局时间</th>
                    <th class="col-md-3">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="record in records" :key="record.record.id">
                    <td style="text-align: left">
                        <div style="margin-left: 2vw">
                            <img :src="record.a_photo" alt="" class="record-user-photo">
                            &nbsp;
                            <span>{{ record.a_username }}</span>
                        </div>
                    </td>
                    <td style="text-align: left">
                        <img :src="record.b_photo" alt="" class="record-user-photo">
                        &nbsp;
                        <span>{{ record.b_username }}</span>
                    </td>
                    <td>{{ record.result }}</td>
                    <td>{{ record.record.createTime }}</td>
                    <td>
                        <button type="button" @click="open_record_content(record.record.id)" class="btn btn-success btn-sm">查看录像</button>
                    </td>
                </tr>
            </tbody>
        </table>
        <nav aria-label="Page navigation example" style="float: right">
            <ul class="pagination">
                <li class="page-item" @click="click_page(-2)">
                    <a class="page-link" href="#" aria-label="Previous">
                        <span aria-hidden="true">&laquo;</span>
                    </a>
                </li>
                <li v-for="page in pages" :key="page.number" :class="'page-item ' + page.is_active" @click="click_page(page.number)">
                    <a class="page-link" href="#">{{ page.number }}</a>
                </li>
                <li class="page-item" @click="click_page(-1)">
                    <a class="page-link" href="#" aria-label="Next">
                        <span aria-hidden="true">&raquo;</span>
                    </a>
                </li>
            </ul>
        </nav>
    </ContentField>
</template>

<script>
import ContentField from "@/components/ContentField.vue"
import {useStore} from "vuex"
import {ref} from 'vue'
import $ from 'jquery'
import router from "@/router/index";

export default {
    components: {
        ContentField,
    },
    setup() {
        const store = useStore();
        let current_page = 1;
        let records = ref([]);
        let total_records = 0;
        let pages = ref([]);
        let page_capacity = -1;

        const update_pages = () => {
              let max_pages = parseInt(Math.ceil(total_records / page_capacity));
              let new_pages = [];
              for (let i = current_page - 2; i <= current_page + 2; i++) {
                  if (i >= 1 && i <= max_pages) {
                      new_pages.push({
                          number: i,
                          is_active: i === current_page ? 'active' : ''
                      });
                  }
              }

              pages.value = new_pages;
        };

        const click_page = page => {
            if (page === -2) {
                page = current_page - 1;
            } else if (page === -1) {
                page = current_page + 1;
            }
            let max_pages = parseInt(Math.ceil(total_records / page_capacity));
            if (page >= 1 && page <= max_pages) {
                pull_page(page)
            }
        }

        const pull_page = page => {
            current_page = page
            $.ajax({
                // url: "http://127.0.0.1:3000/api/record/getlist/",
                url: "https://www.kingofbots.fun/api/record/getlist/",
                type: "get",
                data: {
                    page: page
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    page_capacity = resp.page_capacity
                    records.value = resp.records
                    total_records = resp.records_count
                    update_pages();
                },
                error() {
                }
            })

        }

        pull_page(current_page)

        const stringTo2D = graph => {
            let g = [];
            for (let i = 0, k = 0; i < 13; i++) {
                let line = []
                for (let j = 0; j < 14; j++, k++) {
                    if (graph[k] === '1') {
                        line.push(1);
                    } else {
                        line.push(0);
                    }
                }
                g.push(line);
            }
            return g;
        };

        const open_record_content = recordId => {
            for (const record of records.value) {
                if (record.record.id === recordId) {
                    store.commit("updateIsRecord", true);
                    store.commit("updateGame", {
                        graph: stringTo2D(record.record.graph),
                        a_id: record.record.aid,
                        a_sx: record.record.asx,
                        a_sy: record.record.asy,
                        b_id: record.record.bid,
                        b_sx: record.record.bsx,
                        b_sy: record.record.bsy,
                    });
                    store.commit('updateOpponent', {
                        photo: "",
                        username: record.a_username === store.state.user.username ? record.b_username : record.a_username,
                    })
                    store.commit("updateSteps", {
                        a_steps: record.record.asteps,
                        b_steps: record.record.bsteps,
                    });
                    store.commit("updateRecordLoser", record.record.loser);
                    router.push({
                        name: "record_content",
                        params: {
                            recordId
                        }
                    })
                    break;
                }
            }
        };

        return {
            records,
            pages,
            click_page,
            open_record_content
        }
    }
}
</script>

<style scoped>
.record-user-photo {
    width: 2.3vw;
    border-radius: 50%;
}
</style>