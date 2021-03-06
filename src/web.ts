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
    let res: HttpResponse
    try {
      res = await HttpPlugin.request(options)
    } catch(e) {
      console.error('Plugin Native Error', e)
      throw toError(e)
    }

    const contentType = res.headers['Content-Type'] || res.headers['content-type']
    if (contentType && contentType.includes('application/json')) {
      res.data = JSON.parse(res.data)
    }

    if (res.status < 200 || res.status > 299) {
      throw toError(res)
    }
    return res
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

function toError(e: any): Error {
  if(e instanceof Error) {
    return e
  } else if (typeof e === 'object') {
    return new Error(JSON.stringify(e))
  } else {
    return new Error(e)
  }
}

const HttpPluginWebImpl = new HttpPluginWeb();
const HttpPluginNativeImpl = new HttpPluginNative();

export { HttpPluginWebImpl, HttpPluginNativeImpl };
import { registerWebPlugin } from '@capacitor/core';

registerWebPlugin(HttpPluginWebImpl);
registerWebPlugin(HttpPluginNativeImpl);


