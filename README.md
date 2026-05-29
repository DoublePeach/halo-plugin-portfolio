# Halo 个人作品集插件

面向 Halo 2.22.x 的个人项目作品集插件，包含：

- Console 后台：项目 CRUD 管理
- 公开前台：`/portfolio` 独立 SPA 展示
- 核心项目、时间线、按领域/技术栈/来源分组、详情弹窗

## 环境要求

- JDK 21
- Node.js 20+
- pnpm 9+

## 本地开发

```bash
# 构建插件 JAR（包含 Console UI 与 public-app）
./gradlew build

# 或使用 Halo 开发容器（需 Docker）
./gradlew haloServer
```

开发时也可分别构建前端：

```bash
cd ui && pnpm install && pnpm build
cd ../public-app && pnpm install && pnpm build
```

## 安装到现有博客

1. 在 `build/libs/` 找到插件 JAR
2. Halo 后台 → 插件 → 安装并启用
3. 访问 `https://你的域名/portfolio`
4. Console → 内容 → 作品集管理

## 主要路由与 API

| 类型 | 路径 |
|------|------|
| 公开页面 | `/portfolio` |
| 公开 API | `/apis/api.portfolio.plugin.halo.run/v1alpha1/projects/*` |
| 管理 CRUD | `/apis/portfolio.plugin.halo.run/v1alpha1/portfolioprojects` |
| 静态资源 | `/plugins/portfolio/assets/app/*` |

## 项目结构

```
src/main/java/run/halo/portfolio/   # 后端 Java
src/main/resources/
  extensions/                        # 角色、ReverseProxy、示例数据
  templates/portfolio.html           # SPA 壳
  static/app/                        # 前台构建产物
ui/                                  # Console 管理界面
public-app/                          # 前台 SPA 源码
```

## 编码说明

项目全链路使用 UTF-8，示例数据与界面文案均为中文，可直接在后台继续维护项目内容。
