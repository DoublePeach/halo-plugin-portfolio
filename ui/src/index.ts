import { definePlugin } from '@halo-dev/console-shared'
import { IconFolder } from '@halo-dev/components'
import { markRaw } from 'vue'
import './styles/admin.css'
import ProjectListView from './views/ProjectListView.vue'
import ProjectEditView from './views/ProjectEditView.vue'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'Root',
      route: {
        path: '/portfolio-projects',
        name: 'PortfolioProjects',
        component: ProjectListView,
        meta: {
          title: '作品集管理',
          searchable: true,
          permissions: ['plugin:portfolio:project:view'],
          menu: {
            name: '作品集管理',
            group: 'content',
            icon: markRaw(IconFolder),
            priority: 30,
          },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: '/portfolio-projects/edit/:name?',
        name: 'PortfolioProjectEdit',
        component: ProjectEditView,
        meta: {
          title: '编辑项目',
          permissions: ['plugin:portfolio:project:manage'],
          menu: {
            name: '编辑项目',
            group: 'content',
            hide: true,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})
