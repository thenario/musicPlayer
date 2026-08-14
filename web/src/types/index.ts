export interface IAxiosRes<T = any> {
  success: boolean
  data: T
  message: string
  code: number
}

export interface IError {
  success: boolean
  message: string
}

export interface IUser {
  user_id: number | string
  user_email: string
  user_name: string
  user_cover?: string
}

export interface IPagination {
  total_items: number
  total_pages: number
  current_page: number
  page_limit: number
}

export interface ILogin {
  message: string
  success: boolean
  user: IUser
  token: string
}

export interface ILogout {
  message: string
  success: boolean
}

export interface IRegister {
  message: string
  success: boolean
  user: IUser
}
