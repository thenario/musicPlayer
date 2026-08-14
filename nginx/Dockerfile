# 从镜像官网引入镜像
# 镜像仓库院/命名空间/软件名字：标签版本
FROM docker.m.daocloud.io/library/nginx:stable-alpine
# Run运行指令
# 清空默认配置
RUN rm /etc/nginx/conf.d/default.conf
# 把本地的nginx配置复制到虚拟环境当中
# copy 本地地址  虚拟环境的地址
COPY conf/nginx.conf /etc/nginx/nginx.conf
# 口头声明，没有实际作用，代表暴露80端口
EXPOSE 80
# 指定容器声称时默认要运行的程序
# nginx程序名
# -g global，不用修改配置文件即可注入配置指令
# daemon off非守护进程，防止指令执行完毕容器直接关机
CMD ["nginx", "-g", "daemon off;"]

# 运行示例：docker run -d -p 80:80 my-nginx
# -d表示后台运行，-p表示暴露的端口，本机：虚拟环境，容器名称
# 不加后续指令的话就是运行cmd的内容
