import $ from "jquery";

export default {
    state: {
        id: "",
        username: "",
        photo: "",
        token: "",
        is_login: false,
        show_login_register: false,
    },
    mutations: {
        updateUser(state, user) {
            state.id = user.id;
            state.username = user.username;
            state.photo = user.photo;
            state.is_login = user.is_login;
        },
        updateToken(state, token) {
            state.token = token;
        },
        logout(state) {
            state.id = '';
            state.username = '';
            state.photo = '';
            state.token = '';
            state.is_login = false;
            state.show_login_register = true;
        },
        updatePullingInfo(state, flag) {
            state.pulling_info = flag;
        },
        updateShowLoginRegister(state, flag) {
            state.show_login_register = flag;
        }
    },
    getters: {
    },
    actions: {
        login(context, data) {
            $.ajax({
                // url: "http://127.0.0.1:3000/api/user/account/token/",
                url: "https://www.kingofbots.fun/api/user/account/token/",
                type: "post",
                data: {
                    username: data.username,
                    password: data.password,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        context.commit('updateShowLoginRegister', false);
                        localStorage.setItem("jwt_token", resp.token);
                        context.commit('updateToken', resp.token);
                        data.success(resp);
                    } else {
                        data.error(resp);
                    }
                },
                error(resp) {
                    data.error(resp);
                }
            })

        },
        getInfo(context, data) {
            $.ajax({
                // url: "http://127.0.0.1:3000/api/user/account/info/",
                url: "https://www.kingofbots.fun/api/user/account/info/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + context.state.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        context.commit('updateUser', {
                            ...resp,
                            is_login: true,
                        });
                        data.success();
                    } else {
                        data.error();
                    }
                },
                error(resp) {
                    data.error(resp);
                }
            })
        },
        logout(context) {
            localStorage.removeItem("jwt_token");
            context.commit('logout');
        }
    },
    modules: {
    }
}
