
import Vue from 'vue'
import Router from 'vue-router'


import Index from "../page/index/Index";
Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '',
      redirect: '/index'
    },
    {
      path: '/index',
      name: 'index',
      component: Index,
    }
  ],

})
