FROM node:22-alpine AS web-build
WORKDIR /workspace/web
RUN corepack enable
COPY web/package.json web/pnpm-lock.yaml web/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY web ./
RUN pnpm build

FROM nginx:stable-alpine
COPY nginx/conf/nginx.conf /etc/nginx/nginx.conf
COPY --from=web-build /workspace/web/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]