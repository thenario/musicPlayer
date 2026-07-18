/// <reference types="vite/client" />
//引入vite里面的包以支持对图片，vite环境变量（import.meta.env）的识别

declare module '*.vue' {
  //从vue核心库里，引入定义组件的专属类型工具
  import type { DefineComponent } from 'vue'
  //定义一个常量叫 component
  //DefineComponent<Props, RawBindings, D (Data)>
  //<{},{},any>的意思是：默认这个组件的Props是空的，Data是匿名的
  const component: DefineComponent<{}, {}, any>
  //把这个组件作为默认导出
  export default component
}
