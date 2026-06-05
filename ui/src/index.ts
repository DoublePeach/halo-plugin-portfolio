import { definePlugin } from '@halo-dev/console-shared'
import { IconFolder } from '@halo-dev/components'
import { markRaw } from 'vue'
import './styles/admin.css'
import PortfolioListView from './views/PortfolioListView.vue'
import PortfolioFormView from './views/PortfolioFormView.vue'
import PortfolioDetailView from './views/PortfolioDetailView.vue'
import ProjectEditView from './views/ProjectEditView.vue'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'Root',
      route: {
        path: '/portfolios',
        name: 'Portfolios',
        component: PortfolioListView,
        meta: {
          title: '作品集管理',
          searchable: true,
          permissions: ['plugin:portfolio:portfolio:view'],
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
        path: '/portfolios/create',
        name: 'PortfolioCreate',
        component: PortfolioFormView,
        meta: {
          title: '新建作品集',
          permissions: ['plugin:portfolio:portfolio:manage'],
          menu: { name: '新建作品集', group: 'content', hide: true },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: '/portfolios/:name/projects/:projectName/edit',
        name: 'PortfolioProjectEdit',
        component: ProjectEditView,
        meta: {
          title: '编辑项目',
          permissions: ['plugin:portfolio:project:manage'],
          menu: { name: '编辑项目', group: 'content', hide: true },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: '/portfolios/:name/projects/create',
        name: 'PortfolioProjectCreate',
        component: ProjectEditView,
        meta: {
          title: '新建项目',
          permissions: ['plugin:portfolio:project:manage'],
          menu: { name: '新建项目', group: 'content', hide: true },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: '/portfolios/:name/edit',
        name: 'PortfolioEdit',
        component: PortfolioFormView,
        meta: {
          title: '编辑作品集',
          permissions: ['plugin:portfolio:portfolio:manage'],
          menu: { name: '编辑作品集', group: 'content', hide: true },
        },
      },
    },
    {
      parentName: 'Root',
      route: {
        path: '/portfolios/:name',
        name: 'PortfolioDetail',
        component: PortfolioDetailView,
        meta: {
          title: '作品集详情',
          permissions: ['plugin:portfolio:portfolio:view'],
          menu: { name: '作品集详情', group: 'content', hide: true },
        },
      },
    },
  ],
  extensionPoints: {},
})
