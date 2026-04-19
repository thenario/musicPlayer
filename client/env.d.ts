interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_APP_TITLE: string
  readonly VITE_USER_COVER_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
