declare module "@capacitor/core" {
  interface PluginRegistry {
    HttpPlugin: HttpPluginPlugin;
  }
}

export interface HttpPluginPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
