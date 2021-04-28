import Vue from 'vue'
import App from './App'
import Router from './router'
import 'normalize.css'
import Elementui from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.use(Elementui);
Vue.config.productionTip = false;
import NProgress from 'nprogress' // Progress 进度条
import 'nprogress/nprogress.css'// Progress 进度条样式

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router: Router,
  components: { App},
  template: '<App/>'
})
