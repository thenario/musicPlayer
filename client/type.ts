export interface IUser {
  user_id: number
  user_email: string
  user_name: string
}

export interface ILoginRt {
  message: string
  success: boolean
  user: IUser
  token: string
}

export interface ILogoutRt {
  message: string
  success: boolean
}

export interface IRegisterRt {
  message: string
  success: boolean
  user: IUser
}
