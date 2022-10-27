<template>
    <div class="container">
        <div class="row">
            <div class="col-3">
                <div class="card" style="margin-top: 20px">
                    <div class="card-body">
                        <img :src="$store.state.user.photo" style="width: 100%;" alt="">
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card" style="margin-top: 20px">
                    <div class="card-header">
                        <span style="font-size: 120%;">我的Bot</span>
                        <button type="button" class="btn btn-success float-end" data-bs-toggle="modal" data-bs-target="#add-bot-btn">新建Bot</button>
                        <!--Modal-->
                        <div class="modal fade  modal-lg" id="add-bot-btn" data-bs-backdrop="static" data-bs-keyboard="false"  tabindex="-1">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">创建Bot</h5>
                                    </div>
                                    <div class="modal-body">
                                        <div class="mb-3">
                                            <label for="add-bot-title" class="form-label">名称</label>
                                            <input v-model="bot.title" type="email" id="add-bot-title" class="form-control" placeholder="请输入名称">
                                        </div>
                                        <div class="mb-3">
                                            <label for="add-bot-description" class="form-label">简介</label>
                                            <textarea v-model="bot.description" class="form-control" id="add-bot-description" placeholder="请输入Bot简介" rows="2"></textarea>
                                        </div>
                                        <div class="mb-3">
                                            <label for="add-bot-code" class="form-label">代码</label>
                                            <VAceEditor
                                                :options="{fontSize: 20}"
                                                v-model:value="bot.content"
                                                @init="editorInit"
                                                lang="c_cpp"
                                                theme="textmate"
                                                style="height: 320px" />
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <div class="error-message">{{ bot.error_message }}</div>
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="clearErrorMessage">取消</button>
                                        <button type="button" class="btn btn-primary" @click="addBot">创建</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <table class="table table-hover" style="text-align: center;">
                            <thead>
                                <tr>
                                    <th class="col-md-4">
                                        <div style="text-align: left; margin-left: 5vw">名称</div>
                                    </th>
                                    <th class="col-md-4">创建时间</th>
                                    <th class="col-md-4">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="o in bots" :key="o.id">
                                    <td>
                                        <div style="text-align: left; margin-left: 3vw">{{ o.title }}</div>
                                    </td>
                                    <td>{{ o.createTime }}</td>
                                    <td>
                                        <button type="button" class="btn btn-outline-info btn-sm" style="margin-right: 20px" data-bs-toggle="modal" :data-bs-target="'#update-bot-btn-' + o.id">修改</button>
                                        <!--Modal-->
                                        <div class="modal fade modal-lg" :id="'update-bot-btn-' + o.id" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">修改Bot</h5>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label for="update-bot-title" class="form-label">名称</label>
                                                            <input v-model="o.title" type="email" id="update-bot-title" class="form-control">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label for="update-bot-description" class="form-label">简介</label>
                                                            <textarea v-model="o.description" class="form-control" id="update-bot-description" rows="2"></textarea>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label for="update-bot-code" class="form-label">代码</label>
                                                            <VAceEditor
                                                                :options="{fontSize: 20}"
                                                                v-model:value="o.content"
                                                                @init="editorInit"
                                                                lang="c_cpp"
                                                                theme="textmate"
                                                                style="height: 320px" />
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <div class="error-message">{{ o.error_message }}</div>
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="refreshBots">取消</button>
                                                        <button type="button" class="btn btn-primary" @click="updateBot(o)">修改</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" :data-bs-target="'#confirmed-remove-' + o.id">删除</button>
                                        <!-- Modal -->
                                        <div class="modal fade" :id="'confirmed-remove-' + o.id" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-body">
                                                        删除后信息不可恢复，您确认删除 {{ o.title }} 吗
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary float-begin" data-bs-dismiss="modal">取消</button>
                                                        <button type="button" @click="removeBot(o)" class="btn btn-danger">确认</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {ref, reactive} from "vue"
import $ from 'jquery'
import {useStore} from "vuex"
import {Modal} from 'bootstrap/dist/js/bootstrap'
import { VAceEditor } from 'vue3-ace-editor'
import ace from 'ace-builds'

export default {
    components: {
        VAceEditor
    },
    setup() {
        ace.config.set(
            "basePath",
            "https://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/");
        // document.getElementById('editor').style.fontSize='20px';

        const store = useStore();
        let bots = ref([]);

        let bot = reactive({
           title: "",
           description: "",
           content: "",
           error_message: "",
        });

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

        const clearErrorMessage = () => {
            bot.error_message = '';
        };

        const addBot = () => {
            clearErrorMessage();
            $.ajax({
                url: "https://www.kingofbots.fun/api/user/bot/add/",
                type: "post",
                data: {
                    title: bot.title,
                    description: bot.description,
                    content: bot.content,
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        bot.title = '';
                        bot.description = '';
                        bot.content = '';
                        Modal.getInstance('#add-bot-btn').hide();
                        refreshBots();
                    } else {
                        bot.error_message = resp.error_message;
                    }
                },
            })
        };

        const updateBot = (bot) => {
            clearErrorMessage();
            $.ajax({
                url: "https://www.kingofbots.fun/api/user/bot/update/",
                type: "post",
                data: {
                    id: bot.id,
                    title: bot.title,
                    description: bot.description,
                    content: bot.content,
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        Modal.getInstance('#update-bot-btn-' + bot.id).hide();
                        refreshBots();
                    } else {
                        bot.error_message = resp.error_message;
                    }
                },
            })
        };

        const removeBot = (bot) => {
            $.ajax({
                url: "https://www.kingofbots.fun/api/user/bot/remove/",
                type: "post",
                data: {
                    id: bot.id,
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        Modal.getInstance('#confirmed-remove-' + bot.id).hide();
                        refreshBots();
                    }
                },
            })
        };

        refreshBots();

        return {
            bots,
            refreshBots,
            bot,
            addBot,
            removeBot,
            clearErrorMessage,
            updateBot
        }
    }
}
</script>

<style scoped>
div.error-message {
    color: red;
}
</style>