import { WebPlugin, Plugins } from '@capacitor/core';
import { HttpPluginContract,
         HttpOptions,
         HttpResponse,
         HttpSetCookieOptions,
         HttpGetCookiesOptions,
         HttpGetCookiesResult,
         HttpDeleteCookieOptions, 
         HttpClearCookiesOptions 
        } from './definitions';
const { HttpPlugin } = Plugins


export class HttpPluginWeb extends WebPlugin implements HttpPluginContract {
  constructor() {
    super({
      name: 'HttpPlugin',
      platforms: ['web']
    });
  }

  async request(_: HttpOptions): Promise<HttpResponse> {
    throw new Error('request unimplimented for web')
  }
  async setCookie(_: HttpSetCookieOptions): Promise<void> {
    throw new Error('setCookie unimplimented for web')
  }
  async getCookies(_: HttpGetCookiesOptions): Promise<HttpGetCookiesResult> {
    throw new Error('getCookies unimplimented for web')
  }
  async deleteCookie(_: HttpDeleteCookieOptions): Promise<void> {
    throw new Error('deleteCookie unimplimented for web')
  }
  async clearCookies(_: HttpClearCookiesOptions): Promise<void> {
    throw new Error('clearCookies unimplimented for web')
  }
}

export class HttpPluginNative extends WebPlugin implements HttpPluginContract {
  constructor() {
    super({
      name: 'HttpPluginNative',
      platforms: ['web']
    });
  }

  async request(options: HttpOptions): Promise<HttpResponse> {
    return HttpPlugin.request(options)
  }
  async setCookie(options: HttpSetCookieOptions): Promise<void> {
    return HttpPlugin.setCookie(options)
  }
  async getCookies(options: HttpGetCookiesOptions): Promise<HttpGetCookiesResult> {
    return HttpPlugin.getCookies(options)
  }
  async deleteCookie(options: HttpDeleteCookieOptions): Promise<void> {
    return HttpPlugin.deleteCookie(options)
  }
  async clearCookies(options: HttpClearCookiesOptions): Promise<void> {
    return HttpPlugin.clearCookies(options)
  }
}

const HttpPluginWebImpl = new HttpPluginWeb();
const HttpPluginNativeImpl = new HttpPluginNative();

export { HttpPluginWebImpl, HttpPluginNativeImpl };
import { registerWebPlugin } from '@capacitor/core';

registerWebPlugin(HttpPluginWebImpl);
registerWebPlugin(HttpPluginNativeImpl);


