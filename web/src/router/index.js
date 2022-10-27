import { createRouter, createWebHistory } from 'vue-router'
import PkIndexView from "@/views/pk/PkIndexView"
import RanklistIndexView from "@/views/ranklist/RanklistIndexView"
import NotFound from "@/views/error/NotFound"
import RecordIndexView from "@/views/record/RecordIndexView"
import RecordContentView from "@/views/record/RecordContentView";
import UserBotIndexView from "@/views/user/bot/UserBotIndexView"
import UserAccountLoginView from "@/views/user/account/UserAccountLoginView";
import UserAccountRegisterView from "@/views/user/account/UserAccountRegisterView";
import store from "@/store/index";

const routes = [
    {
        path: "/",
        name: "home",
        redirect: "/pk/",
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/user/account/login/",
        name: "user_account_login",
        component: UserAccountLoginView,
        meta: {
            requestAuth: false,
        }
    },
    {
        path: "/user/account/Register/",
        name: "user_account_register",
        component: UserAccountRegisterView,
        meta: {
            requestAuth: false,
        }
    },
    {
        path: "/pk/",
        name: "pk_index",
        component: PkIndexView,
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/ranklist/",
        name: "ranklist_index",
        component: RanklistIndexView,
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/record/:recordId/",
        name: "record_content",
        component: RecordContentView,
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/record/",
        name: "record_index",
        component: RecordIndexView,
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/userbot/",
        name: "user_bot",
        component: UserBotIndexView,
        meta: {
            requestAuth: true,
        }
    },
    {
        path: "/404/",
        name: "404",
        component: NotFound,
        meta: {
            requestAuth: false,
        }
    },
    {
        path: "/:catchAll(.*)",
        redirect: "/404/",
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    let flag = true;
    const jwt_token = localStorage.getItem("jwt_token");

    if (jwt_token) {
        store.commit("updateToken", jwt_token);
        store.dispatch("getInfo", {
            success() {
            },
            error() {
                store.commit('updateShowLoginRegister', true);
                store.commit("logout");
                localStorage.removeItem('jwt_token');
                router.push({ name: 'user_account_login' });
            }
        })
    } else {
        store.commit('updateShowLoginRegister', true);
        flag = false;
    }

    if (to.meta.requestAuth && !store.state.user.is_login) {
        if (flag) {
            next();
        } else {
            next({name: "user_account_login"});
        }
    } else {
        next();
    }
})

export default router
