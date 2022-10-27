<template>
    <ContentField>
        <table class="table table-hover table-striped" style="text-align: center;">
            <thead>
            <tr>
                <th class="col-md-2">玩家</th>
                <th class="col-md-2">天梯分</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in users" :key="user.id">
                <td style="text-align: left; margin-left: 20vw">
                    <div style="margin-left: 15vw">
                        <img :src="user.photo" alt="" class="ranklist-user-photo">
                        &nbsp;
                        <span>{{ user.username }}</span>
                    </div>
                </td>
                <td>{{ user.rating }}</td>
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

export default {
    components: {
        ContentField,
    },
    setup() {
        const store = useStore();
        let current_page = 1;
        let users = ref([]);
        let total_users = 0;
        let pages = ref([]);
        let page_capacity = -1;

        const update_pages = () => {
            let max_pages = parseInt(Math.ceil(total_users / page_capacity));
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
            let max_pages = parseInt(Math.ceil(total_users / page_capacity));
            if (page >= 1 && page <= max_pages) {
                pull_page(page)
            }
        }

        const pull_page = page => {
            current_page = page
            $.ajax({
                url: "https://www.kingofbots.fun/api/get/ranklist/",
                type: "get",
                data: {
                    page: page
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    page_capacity = resp.page_capacity
                    users.value = resp.users
                    total_users = resp.users_count
                    update_pages();
                },
                error() {
                }
            })

        }

        pull_page(current_page)

        return {
            users,
            pages,
            click_page
        }
    }
}
</script>

<style scoped>
.ranklist-user-photo {
    width: 2.3vw;
    border-radius: 50%;
}
</style>