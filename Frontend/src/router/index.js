import {createRouter, createWebHistory} from 'vue-router'
import UploadView from "@/views/UploadView.vue";
import ImageView from "@/views/ImageView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: UploadView },
    { path: '/view/:id', component: ImageView }
  ],
})

export default router
